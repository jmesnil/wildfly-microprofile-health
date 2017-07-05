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

import static org.eclipse.microprofile.health.HealthStatus.State.UP;
import static org.wildfly.extension.microprofile.health.SubsystemExtension.SUBSYSTEM_NAME;
import static org.wildfly.extension.microprofile.health.SubsystemExtension.getResourceDescriptionResolver;

import java.util.Collection;
import java.util.Map;

import org.eclipse.microprofile.health.HealthStatus;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class CheckOperation implements OperationStepHandler {

   public static final OperationDefinition DEFINITION = new SimpleOperationDefinitionBuilder("check", getResourceDescriptionResolver(SUBSYSTEM_NAME))
           .setRuntimeOnly()
           .setReplyType(ModelType.OBJECT)
           .setReplyValueType(ModelType.OBJECT)
           .build();

   public static ModelNode computeResult(Collection<HealthStatus> statuses) {
      ModelNode result = new ModelNode();
      boolean globalOutcome = true;
      result.get("checks").setEmptyList();
      for (HealthStatus status : statuses) {
         ModelNode statusNode = new ModelNode();
         statusNode.get("id").set(status.getName());
         HealthStatus.State state = status.getState();
         globalOutcome = globalOutcome & state == UP;
         statusNode.get("result").set(state.toString());
         if (status.getAttributes().isPresent()) {
            statusNode.get("data").setEmptyObject();
            Map<String, Object> attributes = status.getAttributes().get();
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
               statusNode.get("data").get(entry.getKey()).set(entry.getValue().toString());
            }
         }
         result.get("checks").add(statusNode);
      }
      result.get("outcome").set(globalOutcome ? "UP" : "DOWN");
      return result;
   }

   @Override
   public void execute(OperationContext operationContext, ModelNode modelNode) throws OperationFailedException {
      ServiceController<?> healthMonitorService = operationContext.getServiceRegistry(false).getRequiredService(HealthMonitorService.SERVICE_NAME);
      HealthMonitor healthMonitor = HealthMonitor.class.cast(healthMonitorService.getValue());

      Collection<HealthStatus> statuses = healthMonitor.check();
      ModelNode result = computeResult(statuses);
      operationContext.getResult().set(result);
   }
}
