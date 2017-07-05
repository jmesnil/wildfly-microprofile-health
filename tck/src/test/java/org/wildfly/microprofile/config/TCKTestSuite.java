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

package org.wildfly.microprofile.config;

import org.eclipse.microprofile.health.tck.MultipleProceduresFailedTest;
import org.eclipse.microprofile.health.tck.ResponseAttributesTest;
import org.eclipse.microprofile.health.tck.SingleProcedureFailedTest;
import org.eclipse.microprofile.health.tck.SingleProcedureSuccessfulTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        MultipleProceduresFailedTest.class,
        ResponseAttributesTest.class,
        SingleProcedureFailedTest.class,
        SingleProcedureSuccessfulTest.class
})
public class TCKTestSuite {
}