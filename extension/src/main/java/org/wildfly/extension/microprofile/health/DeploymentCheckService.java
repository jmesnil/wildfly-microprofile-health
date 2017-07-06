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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.STATUS;

import java.util.concurrent.Executors;

import org.eclipse.microprofile.health.HealthCheckProcedure;
import org.eclipse.microprofile.health.HealthResponse;
import org.eclipse.microprofile.health.HealthStatus;
import org.jboss.as.controller.LocalModelControllerClient;
import org.jboss.as.controller.ModelControllerClientFactory;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class DeploymentCheckService implements Service<HealthCheckProcedure> {

   private static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("eclipse", "microprofile", "health", "checks", "deployment");
   private final InjectedValue<HealthMonitor> healthMonitor = new InjectedValue<>();
   private final InjectedValue<ModelControllerClientFactory> modelControllerClientFactory = new InjectedValue<>();

   public static void install(OperationContext context) {
      DeploymentCheckService service = new DeploymentCheckService();
      context.getServiceTarget().addService(SERVICE_NAME, service)
              .addDependency(HealthMonitorService.SERVICE_NAME, HealthMonitor.class, service.healthMonitor)
              .addDependency(ServiceName.parse("org.wildfly.managment.model-controller-client-factory"), ModelControllerClientFactory.class, service.modelControllerClientFactory)
              .install();
   }

   public static void uninstall(OperationContext context) {
      uninstall(context, SERVICE_NAME);
   }

   public static void uninstall(OperationContext context, ServiceName suffix) {
      context.removeService(SERVICE_NAME.append(suffix));
   }

   private ModelControllerClient client;
   private HealthCheckProcedure procedure;

   private DeploymentCheckService() {

   }

   @Override
   public void start(StartContext startContext) throws StartException {
      final LocalModelControllerClient client = modelControllerClientFactory.getValue().createClient(Executors.newSingleThreadExecutor());
      procedure = new HealthCheckProcedure() {
         @Override
         public HealthStatus perform() {
            ModelNode readDeploymentStatus = new ModelNode();
            readDeploymentStatus.get(OP).set(READ_ATTRIBUTE_OPERATION);
            readDeploymentStatus.get(OP_ADDR).set("deployment", "*");
            readDeploymentStatus.get(NAME).set(STATUS);

            ModelNode response = client.execute(readDeploymentStatus);
            System.out.println("response = " + response);

            HealthResponse status = HealthResponse.named("deployment");
            boolean ok = true;
            if (!response.get("outcome").asString().equals("success")) {
               return status.down();
            }
            for (ModelNode deployment : response.get("result").asList()) {
               boolean deployed = deployment.get("result").asString().equals("OK");
               if (!deployed) {
                  ok = false;
               }
               status.withAttribute(deployment.get("address").asPropertyList().get(0).getValue().asString(), deployed ? "UP" : "DOWN");
            }
            return ok ? status.up() : status.down();
         }
      };
      healthMonitor.getValue().addHealthCheckProcedure(procedure);

   }

   @Override
   public void stop(StopContext stopContext) {
      healthMonitor.getValue().removeHealthCheckProcedure(procedure);
      procedure = null;
   }

   @Override
   public HealthCheckProcedure getValue() throws IllegalStateException, IllegalArgumentException {
      return procedure;
   }
}
