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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.spi.HealthCheckResponseProvider;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class WildFlyResponseFactory implements HealthCheckResponseProvider{
    @Override
    public HealthCheckResponseBuilder createResponseBuilder() {
        return new WildFlyResponseBuilder();
    }

    private static class WildFlyResponseBuilder extends HealthCheckResponseBuilder {

        private String name;
        private boolean state;
        private ModelNode data = new ModelNode();


        @Override
        public HealthCheckResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder withData(String key, String value) {
            data.get(key).set(value);
            return this;
        }

        @Override
        public HealthCheckResponseBuilder withData(String key, long value) {
            data.get(key).set(value);
            return this;
        }

        @Override
        public HealthCheckResponseBuilder withData(String key, boolean value) {
            data.get(key).set(value);
            return this;
        }

        @Override
        public HealthCheckResponseBuilder up() {
            return state(true);
        }

        @Override
        public HealthCheckResponseBuilder down() {
            return state(false);
        }

        @Override
        public HealthCheckResponseBuilder state(boolean up) {
            this.state = up;
            return this;
        }

        @Override
        public HealthCheckResponse build() {
            return new WildFlyResponse(this.name, this.state, this.data);
        }

        private class WildFlyResponse extends HealthCheckResponse {
            private final String name;
            private final boolean up;
            private final Map<String, Object> data;

            public WildFlyResponse(String name, boolean up, ModelNode model)
            {
                this.name = name;
                this.up = up;
                this.data = setData(model);
            }

            private Map<String,Object> setData(ModelNode model) {
                if (!model.isDefined()) {
                    return null;
                }
                Map<String, Object> data = new HashMap<>();
                for (String key : model.keys()) {
                    final ModelNode modelValue = model.get(key);
                    final Object value;
                    switch (modelValue.getType()) {
                        case LONG:
                            value = modelValue.asLong();
                            break;
                        case BOOLEAN:
                            value = modelValue.asBoolean();
                            break;
                        default:
                            value = modelValue.asString();
                    }
                    data.put(key, value);
                }
                return data;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public State getState() {
                return up ? State.UP : State.DOWN;
            }

            @Override
            public Optional<Map<String, Object>> getData() {
                return Optional.ofNullable(data);
            }
        }
    }
}
