/*
 * Copyright (C) 2006, 2007 Joerg Schaible
 * Created on 16.09.2006 by Joerg Schaible
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
package de.berlios.jsunit;

import org.jmock.MockObjectTestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;


/**
 * @author J&ouml;rg Schaible
 */
public class JsUnitRhinoRunnerTest extends MockObjectTestCase {

    private JsUnitRhinoRunner runner;
    private File sampleDir = new File(new File(new File(".."), "jsunit"), "samples");

    protected void setUp() throws Exception {
        super.setUp();
        runner = new JsUnitRhinoRunner();
    }

    public void testEmbeddedRhinoIsDetected() throws JsUnitException {
        assertEquals(Boolean.TRUE, runner.eval("JsUtil.prototype.isRhino", null));
        assertEquals(Boolean.FALSE, runner.eval("JsUtil.prototype.isShell", null));
    }

    public void testRunningAllTests() throws JsUnitException, IOException {
        loadSampleScripts();
        loadSampleScript("AllTests.js");
        final StringWriter writer = new StringWriter();
        runner.runAllTests(writer);
        final String xml = writer.toString();
        assertThat(xml, startsWith("<?xml version=\"1.0\" "));
        assertThat(
            xml,
            contains("<testsuite errors=\"0\" failures=\"1\" name=\"AllTests\" tests=\"29\" "));
    }

    public void testMissingAllTests() throws JsUnitException, IOException {
        loadSampleScripts();
        final StringWriter writer = new StringWriter();
        runner.runAllTests(writer);
        final String xml = writer.toString();
        assertThat(xml, startsWith("<?xml version=\"1.0\" "));
        assertThat(
            xml,
            contains("<testsuite errors=\"0\" failures=\"1\" name=\"AllTests\" tests=\"0\" "));
    }

    public void testRunningTestSuites() throws JsUnitException, IOException {
        loadSampleScripts();
        final StringWriter writer = new StringWriter();
        runner.runTestSuites(writer, "TestSuites");
        final String xml = writer.toString();
        assertThat(
            xml,
            contains("<testsuite errors=\"0\" failures=\"1\" name=\"TestSuites\" tests=\"29\" "));
    }

    public void testRunningWithoutTestSuite() throws IOException {
        final StringWriter writer = new StringWriter();
        runner.runTestSuites(writer, "TestSuites");
        final String xml = writer.toString();
        assertThat(
            xml,
            contains("<testsuite errors=\"0\" failures=\"1\" name=\"TestSuites\" tests=\"0\" "));
    }

    public void testRunningTestCases() throws JsUnitException, IOException {
        loadSampleScripts();
        final StringWriter writer = new StringWriter();
        runner.runTestCases(writer, "TestCases");
        final String xml = writer.toString();
        assertThat(
            xml,
            contains("<testsuite errors=\"0\" failures=\"1\" name=\"TestCases\" tests=\"29\" "));
    }

    public void testRunningWithoutTestCase() throws IOException {
        final StringWriter writer = new StringWriter();
        runner.runTestSuites(writer, "TestCases");
        final String xml = writer.toString();
        assertThat(
            xml,
            contains("<testsuite errors=\"0\" failures=\"1\" name=\"TestCases\" tests=\"0\" "));
    }

    private void loadSampleScripts() throws FileNotFoundException, JsUnitException, IOException {
        loadSampleScript("money/IMoney.js");
        loadSampleScript("money/Money.js");
        loadSampleScript("money/MoneyBag.js");
        loadSampleScript("money/MoneyTest.js");
        loadSampleScript("ArrayTest.js");
        loadSampleScript("SimpleTest.js");
    }

    private void loadSampleScript(final String filename)
                                                        throws FileNotFoundException,
                                                        JsUnitException, IOException {
        final FileReader reader = new FileReader(new File(sampleDir, filename));
        runner.load(reader, filename);
    }
}
