/*
 * Copyright (C) 2006,2007 Joerg Schaible
 * Created on 30.09.2006 by Joerg Schaible
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

import de.berlios.jsunit.JsUnitException;
import de.berlios.jsunit.JsUnitRhinoRunner;
import de.berlios.jsunit.JsUnitRuntimeException;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The JsUnit Mojo.
 * 
 * @author J&ouml;rg Schaible
 * @since upcoming
 * @goal jsunit-test
 * @phase test
 * @description Runs JsUnit tests for JavaScript code.
 */
public class JsUnitMojo extends AbstractMojo {

    private static final String RUN_ALLTESTS = "ALLTESTS";
    private static final String RUN_TESTSUITES = "TESTSUITES";
    private static final String RUN_TESTCASES = "TESTCASES";
    private static final List RUN_TYPES = new ArrayList(Arrays.asList(new String[]{
        RUN_ALLTESTS, RUN_TESTSUITES, RUN_TESTCASES}));

    private static final String[] DEFAULT_INCLUDES = new String[]{
        "**/*Test.js", "**/AllTests.js"};

    /**
     * The Maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @description "The current Maven project"
     */
    private MavenProject project;

    /**
     * Set this to 'true' to bypass unit tests entirely. Its use is NOT RECOMMENDED, but quite
     * convenient on occasion.
     * 
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skip;

    /**
     * Set this to 'true' to bypass unit tests execution, but still load them to ensure the
     * syntax. Its use is NOT RECOMMENDED, but quite convenient on occasion.
     * 
     * @parameter expression="${maven.test.skip.exec}"
     */
    private boolean skipExec;

    /**
     * Set this to true to ignore a failure during testing. Its use is NOT RECOMMENDED, but
     * quite convenient on occasion.
     * 
     * @parameter expression="${maven.test.failure.ignore}"
     */
    private boolean testFailureIgnore;

    /**
     * Base directory where all reports are written to.
     * 
     * @parameter expression="${project.build.directory}/surefire-reports"
     */
    private File reportsDirectory;

    /**
     * The source directory for the JavaScripts.
     * 
     * @parameter expression="${basedir}/src/main/webapp/js"
     * @required
     */
    private File sourceDirectory;

    /**
     * The test source directory containing test class sources.
     * 
     * @parameter expression="${basedir}/src/test/js"
     * @required
     */
    private File testSourceDirectory;

    /**
     * Option to print summary of test suites or just print the test cases that have errors.
     * 
     * @parameter expression="${surefire.printSummary}" default-value="true"
     */
    private boolean printSummary;

    /**
     * An array with the JavaScript sources.
     * 
     * @parameter
     */
    private String[] sources;

    /**
     * An array with the JsUnit suites.
     * 
     * @parameter
     * @required
     */
    private JsUnitSuite[] testSuites;

    private Log logger;

    public void execute() throws MojoFailureException, MojoExecutionException {
        logger = getLog();
        if (!sourceDirectory.isDirectory()) {
            logger.info("No JavaScript source directory, skipping JsUnit tests");
            return;
        }
        if (!testSourceDirectory.isDirectory()) {
            logger.info("No JavaScript test source directory, skipping JsUnit tests");
            return;
        }
        if (skip) {
            logger.info("Skipping JsUnit tests");
        }
        if (testSuites.length == 0 && !skipExec) {
            throw new MojoFailureException("No test suites defined");
        }
        if (!skipExec && !reportsDirectory.isDirectory()) {
            if (!reportsDirectory.mkdirs()) {
                throw new MojoExecutionException("Cannot create report directry "
                    + reportsDirectory.toString());
            }
        }
        for (int i = 0; i < testSuites.length; i++) {
            final JsUnitSuite suite = testSuites[i];
            if (!RUN_TYPES.contains(suite.getType())) {
                throw new MojoFailureException("Unknown run type " + suite.getType());
            }
        }
        int errors = 0;
        int failures = 0;
        for (int i = 0; i < testSuites.length || (skipExec && testSuites.length == 0); i++) {
            JsUnitRhinoRunner runner = null;
            try {
                runner = new JsUnitRhinoRunner();
            } catch (final JsUnitRuntimeException e) {
                throw new MojoExecutionException("Cannot evaluate JavaScript code of JsUnit", e);
            }
            if (sources != null) {
                for (int j = 0; j < sources.length; j++) {
                    final File file = new File(sourceDirectory, sources[j]);
                    try {
                        runner.load(new FileReader(file), file.getName());
                        logger.debug("Loaded " + file.getName());
                    } catch (final FileNotFoundException e) {
                        throw new MojoExecutionException("Cannot find " + file.getName(), e);
                    } catch (final JsUnitException e) {
                        throw new MojoExecutionException("Cannot evaluate JavaScript code of "
                            + file.getName(), e);
                    } catch (final IOException e) {
                        throw new MojoExecutionException("Cannot read complete "
                            + file.getName(), e);
                    }
                }
            } else {
                logger.info("No JavaScript sources defined");
            }
            if (skipExec && testSuites.length == 0) {
                break;
            }
            final JsUnitSuite suite = testSuites[i];
            final String[] includes = suite.getIncludes().isEmpty()
                ? DEFAULT_INCLUDES
                : (String[])suite.getIncludes().toArray(new String[suite.getIncludes().size()]);
            suite.getIncludes();
            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(testSourceDirectory);
            scanner.setIncludes(includes);
            scanner.addDefaultExcludes();
            scanner.scan();
            final String[] files = scanner.getIncludedFiles();
            for (int j = 0; j < files.length; j++) {
                final File file = new File(scanner.getBasedir(), files[j]);
                try {
                    runner.load(new FileReader(file), files[i]);
                    logger.debug("Loaded " + file.getPath());
                } catch (final FileNotFoundException e) {
                    throw new MojoExecutionException("Cannot find " + file.getPath(), e);
                } catch (final JsUnitException e) {
                    throw new MojoExecutionException("Cannot evaluate JavaScript code of "
                        + file.getPath(), e);
                } catch (final IOException e) {
                    throw new MojoExecutionException(
                        "Cannot read complete " + file.getPath(), e);
                }
            }
            if (skipExec) {
                continue;
            }
            final String name = suite.getName();
            final File file = new File(reportsDirectory, "TEST-" + name + ".xml");
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Writer writer;
            try {
                writer = new OutputStreamWriter(new TeeOutputStream(
                    new FileOutputStream(file), baos));
            } catch (final IOException e) {
                throw new MojoExecutionException("Cannot create file " + file.getName(), e);
            }
            try {
                switch (RUN_TYPES.indexOf(suite.getType())) {
                case 0:
                    runner.runAllTests(writer);
                    break;
                case 1:
                    runner.runTestSuites(writer, name);
                    break;
                case 2:
                    runner.runTestCases(writer, name);
                    break;
                }
                logger.debug("Created test report " + file.getName());
            } catch (final IOException e) {
                throw new MojoExecutionException("Cannot write to file " + file.getName(), e);
            } finally {
                IOUtil.close(writer);
            }
            try {
                final String report = baos.toString();
                final Xpp3Dom dom = Xpp3DomBuilder.build(new StringReader(report));
                errors += Integer.parseInt(dom.getAttribute("errors"));
                failures += Integer.parseInt(dom.getAttribute("failures"));
            } catch (XmlPullParserException e) {
                throw new MojoExecutionException("Cannot parse report of test suite " + name, e);
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot read report of test suite " + name, e);
            }
        }
        if (errors + failures > 0) {
            final String msg = "There have been "
                + errors
                + " errors and "
                + failures
                + " failures testing JavaScript";
            if (testFailureIgnore) {
                logger.error(msg);
            } else {
                throw new MojoFailureException(msg);
            }
        }
    }

}
