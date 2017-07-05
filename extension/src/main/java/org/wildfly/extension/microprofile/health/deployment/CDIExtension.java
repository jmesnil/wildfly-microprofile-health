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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.resource.spi.ConfigProperty;

import org.eclipse.microprofile.health.HealthCheckProcedure;
import org.eclipse.microprofile.health.HealthStatus;
import org.wildfly.extension.microprofile.health.HealthMonitor;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class CDIExtension implements Extension {

   private List<AnnotatedType> delegates = new ArrayList<>();

   public <T> void observeResources(@Observes ProcessAnnotatedType<T> event) {

      AnnotatedType<T> annotatedType = event.getAnnotatedType();
      Class<T> javaClass = annotatedType.getJavaClass();
      for (Class<?> intf : javaClass.getInterfaces()) {
         if (intf.getName().equals(HealthCheckProcedure.class.getName())) {
            System.out.println(">> Discovered health check procedure " + javaClass);
            delegates.add(annotatedType);
         }
      }
      InjectionPoint ip = null;
   }

   @ConfigProperty
   private void afterBeanDiscovery(@Observes final AfterBeanDiscovery abd, BeanManager beanManager) {
      try {
         for (AnnotatedType delegate : delegates) {
            Set<Bean<?>> beans = beanManager.getBeans(delegate.getBaseType());
            Iterator<Bean<?>> iterator = beans.iterator();
            while (iterator.hasNext()) {
               Object bean = iterator.next().create(null); // FIXME
               HealthCheckProcedure healthCheckProcedure = HealthCheckProcedure.class.cast(bean);
               System.out.println(">> Added health bean impl " + bean);
               // TODO remove the health check procedure when the deployment is undeployed
               HealthMonitor.INSTANCE.addHealthChechProcedure(healthCheckProcedure);
            }
         }

      } catch (Exception e) {
         throw new RuntimeException("Failed to register health bean", e);
      }
   }
}
