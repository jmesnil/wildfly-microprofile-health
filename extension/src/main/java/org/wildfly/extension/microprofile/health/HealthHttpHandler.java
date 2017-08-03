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

import java.util.Collection;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.eclipse.microprofile.health.Response;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class HealthHttpHandler implements HttpHandler {

   private final HealthMonitor monitor;

   HealthHttpHandler(HealthMonitor monitor) {
      this.monitor = monitor;
   }

   @Override
   public void handleRequest(HttpServerExchange exchange) throws Exception {
      System.out.println("HealthHttpHandler.handleRequest");
      Collection<Response> responses = monitor.check();
      System.out.println("responses = " + responses);
      ModelNode result = CheckOperation.computeResult(responses);
      System.out.println("result = " + result.toJSONString(false));
      exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");
      boolean ok = result.get("outcome").asString() == "UP";
      boolean withChecks = result.get("checks").asList().size() > 0;
      final int statusCode;
      if (ok) {
         statusCode = withChecks ? 200 : 204;
      } else {
         statusCode = 503;
      }
      exchange.setStatusCode(statusCode)
              .getResponseSender().send(result.toJSONString(true));
   }

}
