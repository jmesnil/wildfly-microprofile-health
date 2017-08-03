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

import org.eclipse.microprofile.health.Response;
import org.eclipse.microprofile.health.ResponseBuilder;
import org.eclipse.microprofile.health.spi.SPIFactory;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class WildFlyResponseFactory implements SPIFactory{
    @Override
    public ResponseBuilder createResponseBuilder() {
        return new WildFlyResponseBuilder();
    }

    private static class WildFlyResponseBuilder extends ResponseBuilder {

        private String name;
        private ModelNode attributes = new ModelNode();


        @Override
        public ResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public ResponseBuilder withAttribute(String key, String value) {
            attributes.get(key).set(value);
            return this;
        }

        @Override
        public ResponseBuilder withAttribute(String key, long value) {
            attributes.get(key).set(value);
            return this;
        }

        @Override
        public ResponseBuilder withAttribute(String key, boolean value) {
            attributes.get(key).set(value);
            return this;
        }

        @Override
        public Response up() {
            return state(true);
        }

        @Override
        public Response down() {
            return state(false);
        }

        @Override
        public Response state(boolean up) {
            return new WildFlyResponse(name, up, attributes);
        }

        private class WildFlyResponse extends Response {
            private final String name;
            private final boolean up;
            private final Map<String, Object> attributes;

            public WildFlyResponse(String name, boolean up, ModelNode model)
            {
                this.name = name;
                this.up = up;
                this.attributes = setAttributes(model);
            }

            private Map<String,Object> setAttributes(ModelNode model) {
                if (!model.isDefined()) {
                    return null;
                }
                Map<String, Object> attributes = new HashMap<>();
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
                    attributes.put(key, value);
                }
                return attributes;
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
            public Optional<Map<String, Object>> getAttributes() {
                return Optional.ofNullable(attributes);
            }
        }
    }
}
