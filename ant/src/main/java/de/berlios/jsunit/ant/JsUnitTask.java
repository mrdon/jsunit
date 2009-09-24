/*
 * Copyright (C) 2006,2007 Joerg Schaible
 * Created on 15.09.2006 by Joerg Schaible
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
package de.berlios.jsunit.ant;

import de.berlios.jsunit.JsUnitException;
import de.berlios.jsunit.JsUnitRhinoRunner;
import de.berlios.jsunit.JsUnitRuntimeException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * An Ant task for JsUnit. The task allows the execution of JavaScript unit tests and creates
 * XML reports, that can be processed by the Ant junitreport task. Define the task as follows:
 * 
 * <pre>
 *      &lt;taskdef name=&quot;jsunit&quot; className=&quot;de.berlios.jsunit.ant.JsUnitTask&quot; /&gt;
 *      
 *      &lt;jsunit dir=&quot;sourceDir&quot;&gt;
 *          &lt;source file=&quot;money/IMoney.js&quot; /&gt;
 *          &lt;testsuite name=&quot;MyTestSuite&quot; todir=&quot;build/test-reports&quot; type=&quot;TESTSUITES&quot;&gt;
 *              &lt;fileset dir=&quot;.&quot;&gt;
 *                  &lt;include name=&quot;* /**Test.js&quot; /&gt;
 *              &lt;/fileset&gt;
 *          &lt;/testsuite&gt;
 *      &lt;/jsunit&gt;
 * </pre>
 * 
 * <p>
 * You may declare multiple <code>source</code> tags, the scripts are loaded into the declared
 * order. You may also declare multiple <code>testsuite</code> sections, each one will
 * generate a separate XML report. The type of the test suite can be one of the following
 * values:
 * </p>
 * <dl>
 * <dt>ALLTESTS</dt>
 * <dd>Looks for a class AllTests derived from TestSuite and runs its suite.</dd>
 * <dt>TESTSUITES</dt>
 * <dd>Looks for all classes ending with TestSuite and that are derived from TestSuite and run
 * their suites.</dd>
 * <dt>TESTCASES</dt>
 * <dd>Looks for all classes ending with TestCase and that are derived from TestCase and runs
 * them (the default).</dd>
 * </dl>
 * 
 * @author J&ouml;rg Schaible
 * @since upcoming
 */
public class JsUnitTask extends Task {

    private File dir = new File(".");
    private final List sources = new ArrayList();
    private final List testSuites = new ArrayList();
    private boolean haltOnError = true;
    private boolean haltOnFailure = true;

    public void execute() throws BuildException {
        final Project project = getProject();
        if (!dir.isDirectory()) {
            throw new BuildException("Source directory not found");
        }
        if (testSuites.isEmpty()) {
            throw new BuildException("No test suites defined");
        }
        int errors = 0;
        int failures = 0;
        for (final Iterator iterTest = testSuites.iterator(); iterTest.hasNext();) {
            JsUnitRhinoRunner runner = null;
            try {
                runner = new JsUnitRhinoRunner();
            } catch (final JsUnitRuntimeException e) {
                throw new BuildException("Cannot evaluate JavaScript code of JsUnit", e);
            }
            for (final Iterator iterSource = sources.iterator(); iterSource.hasNext();) {
                final SourceFile source = (SourceFile)iterSource.next();
                final File file = source.getFile();
                final String charSet = source.getCharacterSet();
                try {
                    final InputStream in = new FileInputStream(file);
                    try {
                        final Reader reader = charSet != null ? new InputStreamReader(
                            in, charSet) : new InputStreamReader(in);
                        runner.load(reader, file.getName());
                        project.log("Loaded " + file.getName(), Project.MSG_DEBUG);
                    } catch (final JsUnitException e) {
                        throw new BuildException("Cannot evaluate JavaScript code of "
                            + file.getName(), e);
                    } catch (final IOException e) {
                        throw new BuildException("Cannot read complete " + file.getName(), e);
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                } catch (final FileNotFoundException e) {
                    throw new BuildException("Cannot find " + file.getName(), e);
                }
            }
            final JsUnitSuite suite = (JsUnitSuite)iterTest.next();
            System.out.println("Run suite " + suite.getName());
            suite.run(project, runner);
            errors += suite.getErrors();
            failures += suite.getFailures();
        }
        if (errors + failures > 0) {
            final String msg = "There have been "
                + errors
                + " errors and "
                + failures
                + " failures testing JavaScript";
            if ((errors > 0 && isHaltOnError()) || isHaltOnFailure()) {
                throw new BuildException(msg);
            } else {
                project.log(msg, Project.MSG_ERR);
            }
        }
    }

    /**
     * Sets the source directory.
     * 
     * @param dir the directory.
     * @since upcoming
     */
    public void setDir(final File dir) {
        this.dir = dir;
    }

    /**
     * Requests the haltOnError flag.
     * 
     * @return <code>true</code> if set
     * @since upcoming
     */
    public boolean isHaltOnError() {
        return this.haltOnError;
    }

    /**
     * Sets haltOnError flag.
     * 
     * @param haltOnError the value
     * @since upcoming
     */
    public void setHaltOnError(boolean haltOnError) {
        this.haltOnError = haltOnError;
    }

    /**
     * Requests the haltOnFailure flag.
     * 
     * @return <code>true</code> if set
     * @since upcoming
     */
    public boolean isHaltOnFailure() {
        return this.haltOnFailure;
    }

    /**
     * Sets haltOnFailure flag.
     * 
     * @param haltOnFailure the value
     * @since upcoming
     */
    public void setHaltOnFailure(boolean haltOnFailure) {
        this.haltOnFailure = haltOnFailure;
    }

    /**
     * Creates a new test suite.
     * 
     * @return the test suite
     * @since upcoming
     */
    public JsUnitSuite createTestSuite() {
        final JsUnitSuite suite = new JsUnitSuite();
        testSuites.add(suite);
        return suite;
    }

    /**
     * Creates a new source.
     * 
     * @return the source file reference
     * @since upcoming
     */
    public SourceFile createSource() {
        final SourceFile source = new SourceFile();
        sources.add(source);
        return source;
    }

    /**
     * A utility bean for a SourceFile.
     * 
     * @since upcoming
     */
    final public class SourceFile {
        private File file;
        private String charSet;

        /**
         * Sets the file name.
         * 
         * @param name the file name
         * @since upcoming
         */
        public void setFile(final String name) {
            file = new File(dir, name);
        }

        /**
         * Requests the file name.
         * 
         * @return the file name
         * @since upcoming
         */
        public File getFile() {
            return file;
        }

        /**
         * Sets the character set.
         * 
         * @param characterSet the name of the character set
         * @since upcoming
         */
        public void setCharacterSet(final String characterSet) {
            charSet = characterSet;
        }

        /**
         * Requests the character set
         * 
         * @return the name of the character set or <code>null</code>
         * @since upcoming
         */
        public String getCharacterSet() {
            return charSet;
        }
    }
}
