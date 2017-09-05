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

package org.wildfly.extension.microprofile.health.deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.Unmanaged;
import javax.enterprise.inject.spi.Unmanaged.UnmanagedInstance;
import javax.enterprise.inject.spi.WithAnnotations;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.wildfly.extension.microprofile.health.HealthMonitor;
import org.wildfly.extension.microprofile.health.MicroProfileHealthLogger;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class CDIExtension implements Extension {

   private final HealthMonitor healthMonitor;
   private List<AnnotatedType<? extends HealthCheck>> delegates = new ArrayList<>();
   private Collection<HealthCheck> healthChecks = new ArrayList<>();
   private Collection<UnmanagedInstance<HealthCheck>> healthCheckInstances = new ArrayList<>();

   public CDIExtension(HealthMonitor healthMonitor) {
      this.healthMonitor = healthMonitor;
   }

   /**
    * Discover all classes that implements HealthCheckProcedure
    */
   public void observeResources(@Observes @WithAnnotations({Health.class})  ProcessAnnotatedType<? extends HealthCheck> event) {
      AnnotatedType<? extends HealthCheck> annotatedType = event.getAnnotatedType();
      Class<? extends HealthCheck> javaClass = annotatedType.getJavaClass();
      MicroProfileHealthLogger.ROOT_LOGGER.debugf("Discovered health check procedure %s", javaClass);
      delegates.add(annotatedType);
   }

   /**
    * Instantiates <em>unmanaged instances</em> of HealthCheckProcedure and
    * handle manually their CDI creation lifecycle.
    * Add them to the {@link HealthMonitor}.
    */
   private void afterBeanDiscovery(@Observes final AfterBeanDiscovery abd, BeanManager bm) {
      for (AnnotatedType delegate : delegates) {
         try {
            Unmanaged<HealthCheck> unmanagedHealthCheck = new Unmanaged<HealthCheck>(bm, delegate.getJavaClass());
            UnmanagedInstance<HealthCheck> healthCheckInstance = unmanagedHealthCheck.newInstance();
            HealthCheck healthCheck =  healthCheckInstance.produce().inject().postConstruct().get();
            healthChecks.add(healthCheck);
            healthCheckInstances.add(healthCheckInstance);
            healthMonitor.addHealthCheck(healthCheck);
         } catch (Exception e) {
            throw new RuntimeException("Failed to register health bean", e);
         }
      }
   }

   /**
    * Called when the deployment is undeployed.
    *
    * Remove all the instances of {@link HealthCheck} from the {@link HealthMonitor}.
    * Handle manually their CDI destroy lifecycle.
    */
   public void close(@Observes final BeforeShutdown bs) {
      healthChecks.forEach(healthCheck -> healthMonitor.removeHealthCheck(healthCheck));
      healthChecks.clear();
      healthCheckInstances.forEach(instance -> instance.preDestroy().dispose());
      healthCheckInstances.clear();
   }
}
