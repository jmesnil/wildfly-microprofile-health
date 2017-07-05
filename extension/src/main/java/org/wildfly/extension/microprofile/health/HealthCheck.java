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

import org.eclipse.microprofile.health.HealthCheckProcedure;
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
public class HealthCheck {

   private static final ServiceName BASE_SERVICE_NAME = ServiceName.JBOSS.append("eclipse", "microprofile", "health", "checks");

   public static void install(OperationContext context, String name, HealthCheckProcedure procedure) {
      HealthCheckService service = new HealthCheckService(procedure);
      context.getServiceTarget().addService(BASE_SERVICE_NAME.append(name), service)
              .addDependency(HealthMonitorService.SERVICE_NAME, HealthMonitor.class, service.healthMonitor)
              .install();
   }

   public static void uninstall(OperationContext context, String name) {
      context.removeService(BASE_SERVICE_NAME.append(name));
   }

   private static class HealthCheckService implements Service<HealthCheckProcedure> {

      private final InjectedValue<HealthMonitor> healthMonitor = new InjectedValue<>();

      private final HealthCheckProcedure procedure;

      private HealthCheckService(HealthCheckProcedure procedure) {
         this.procedure = procedure;
      }

      @Override
      public void start(StartContext startContext) throws StartException {
         healthMonitor.getValue().addHealthCheckProcedure(procedure);
      }

      @Override
      public void stop(StopContext stopContext) {
         healthMonitor.getValue().removeHealthCheckProcedure(procedure);
      }

      @Override
      public HealthCheckProcedure getValue() throws IllegalStateException, IllegalArgumentException {
         return procedure;
      }
   }
}
