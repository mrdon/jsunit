/*
 * Copyright (C) 2006,2007 Joerg Schaible
 * Created on 02.10.2006 by Joerg Schaible
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.berlios.jsunit.maven2;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;


/**
 * @author J&ouml;rg Schaible
 */
public class JsUnitMojoTest extends AbstractMojoTestCase {

    /**
     * Tests the environment.
     * 
     * @throws Exception if test fails
     */
    public void testEnvironment() throws Exception {
        final File testPom = new File(getBasedir(), "src/test/resources/unit/environment.xml");
        assertTrue(testPom.isFile());
        final JsUnitMojo mojo = (JsUnitMojo)lookupMojo("jsunit-test", testPom);
        assertNotNull(mojo);
    }

    /**
     * Tests a simple JsUnit setup.
     * 
     * @throws Exception if test fails
     */
    public void testSimpleTest() throws Exception {
        final File testPom = new File(getBasedir(), "src/test/resources/unit/simple.xml");
        assertTrue(testPom.isFile());
        final JsUnitMojo mojo = (JsUnitMojo)lookupMojo("jsunit-test", testPom);
        mojo.execute();
    }
}
