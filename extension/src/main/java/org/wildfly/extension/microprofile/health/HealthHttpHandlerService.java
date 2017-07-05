/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.extension.microprofile.health;

import io.undertow.server.handlers.PathHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class HealthHttpHandlerService implements Service<HealthHttpHandler> {

   private final String prefixPath;
   private final InjectedValue<PathHandler> pathHandlerInjectedValue = new InjectedValue<>();
   private HealthHttpHandler handler;

   public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("eclipse", "microprofile", "health", "http-handler");
   private static final String UNDERTOW_HTTP_INVOKER_CAPABILITY_NAME = "org.wildfly.undertow.http-invoker";

   static void install(OperationContext context, String prefixPath) {
      System.out.println("HealthHttpHandlerService.install");
      HealthHttpHandlerService service = new HealthHttpHandlerService(prefixPath);
      context.getServiceTarget().addService(SERVICE_NAME, service)
              .addDependency(context.getCapabilityServiceName(UNDERTOW_HTTP_INVOKER_CAPABILITY_NAME, PathHandler.class), PathHandler.class, service.pathHandlerInjectedValue)
              .install();
   }

   public HealthHttpHandlerService(String prefixPath) {
      this.prefixPath = prefixPath;
   }

   @Override
   public void start(StartContext context) throws StartException {
      System.out.println("HealthHttpHandlerService.start");
      handler = new HealthHttpHandler(HealthMonitor.INSTANCE);
      pathHandlerInjectedValue.getValue().addPrefixPath(prefixPath, handler);
   }

   @Override
   public void stop(StopContext context) {
      pathHandlerInjectedValue.getValue().removePrefixPath(prefixPath);
   }

   @Override
   public HealthHttpHandler getValue() throws IllegalStateException, IllegalArgumentException {
      return handler;
   }
}
