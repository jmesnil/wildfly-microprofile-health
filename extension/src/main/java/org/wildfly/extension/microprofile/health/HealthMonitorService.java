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

import org.jboss.as.controller.OperationContext;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class HealthMonitorService implements Service<HealthMonitor> {

   public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("eclipse", "microprofile", "health", "monitor");

   private HealthMonitor monitor;

   static void install(OperationContext context) {
      HealthMonitorService service = new HealthMonitorService();
      context.getServiceTarget().addService(SERVICE_NAME, service)
              .install();
   }

   @Override
   public void start(StartContext startContext) throws StartException {
      monitor = new HealthMonitor();
   }

   @Override
   public void stop(StopContext stopContext) {
      monitor = null;
   }

   @Override
   public HealthMonitor getValue() throws IllegalStateException, IllegalArgumentException {
      return monitor;
   }
}
