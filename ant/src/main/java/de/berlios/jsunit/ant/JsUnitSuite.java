/*
 * Copyright (C) 2006,2007 Joerg Schaible
 * Created on 17.09.2006 by Joerg Schaible
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Vector;


/**
 * A JsUnit subtask modeling a test suite.
 * 
 * @author J&ouml;rg Schaible
 * @since upcoming
 */
public class JsUnitSuite {

    private String name;
    private File toDir;
    private TestRunType type = new TestRunType("TESTCASES");
    private final Vector fileSets = new Vector();
    private int errors;
    private int failures;
    private String charSet;

    /**
     * Set the name of the test suite.
     * 
     * @param name the name
     * @since upcoming
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Retrieves the name of the test suite.
     * 
     * @return the name
     * @since upcoming
     */
    public String getName() {
        return name;
    }

    /**
     * Set the type of the test suite.
     * 
     * @param type the type
     * @see JsUnitTask
     * @since upcoming
     */
    public void setType(final TestRunType type) {
        this.type = type;
    }

    /**
     * Set the directory for the generated XML reports.
     * 
     * @param toDir the target directory
     * @since upcoming
     */
    public void setToDir(final File toDir) {
        this.toDir = toDir;
    }

    /**
     * Add a FileSet with JsUnit tests.
     * 
     * @param fileSet the file set.
     * @since upcoming
     */
    public void addFileSet(final FileSet fileSet) {
        fileSets.addElement(fileSet);
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

    /**
     * The enumeration for the test type.
     * 
     * @since upcoming
     */
    public static final class TestRunType extends EnumeratedAttribute {
        /**
         * Default constructor.
         * 
         * @since upcoming
         */
        public TestRunType() {
            super();
        }

        TestRunType(final String value) {
            super();
            setValue(value);
        }

        public String[] getValues() {
            return new String[]{"ALLTESTS", "TESTSUITES", "TESTCASES"};
        }
    }

    /**
     * Run the test suite.
     * 
     * @param project the project
     * @param runner the prepared Rhino context
     * @throws BuildException if the test cannot run or have been aborted
     * @since upcoming
     */
    public void run(final Project project, final JsUnitRhinoRunner runner)
        throws BuildException {
        if (!toDir.isDirectory()) {
            toDir.mkdirs();
        }
        for (final Iterator iter = fileSets.iterator(); iter.hasNext();) {
            final FileSet fileSet = (FileSet)iter.next();
            final DirectoryScanner scanner = fileSet.getDirectoryScanner(project);
            final String[] files = scanner.getIncludedFiles();
            for (int i = 0; i < files.length; i++) {
                final File file = new File(scanner.getBasedir(), files[i]);
                try {
                    final InputStream in = new FileInputStream(file);
                    try {
                        final Reader reader = charSet != null ? new InputStreamReader(
                            in, charSet) : new InputStreamReader(in);
                        runner.load(reader, files[i]);
                        project.log("Loaded " + file.getPath(), Project.MSG_DEBUG);
                    } catch (final JsUnitException e) {
                        throw new BuildException("Cannot evaluate JavaScript code of "
                            + file.getPath(), e);
                    } catch (final IOException e) {
                        throw new BuildException("Cannot read complete " + file.getPath(), e);
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                } catch (final FileNotFoundException e) {
                    throw new BuildException("Cannot find " + file.getPath(), e);
                }
            }
        }
        final File file = new File(toDir, "TEST-" + name + ".xml");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer writer;
        try {
            writer = new OutputStreamWriter(new TeeOutputStream(
                new FileOutputStream(file), baos));
        } catch (final IOException e) {
            throw new BuildException("Cannot create file " + file.getName(), e);
        }
        try {
            switch (type.getIndex()) {
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
            project.log("Created test report " + file.getName(), Project.MSG_DEBUG);
        } catch (final IOException e) {
            throw new BuildException("Cannot write to file " + file.getName(), e);
        } finally {
            FileUtils.close(writer);
        }
        final String[] lines = (String[])StringUtils.lineSplit(baos.toString()).toArray(
            new String[0]);
        int idx = lines[1].indexOf("errors=\"") + 8;
        errors = Integer.parseInt(lines[1].substring(idx, lines[1].indexOf('"', idx)));
        idx = lines[1].indexOf("failures=\"") + 10;
        failures = Integer.parseInt(lines[1].substring(idx, lines[1].indexOf('"', idx)));
    }

    /**
     * Retrieve the number of errors.
     * 
     * @return the error count
     * @since upcoming
     */
    public int getErrors() {
        return this.errors;
    }

    /**
     * Retrieve the number of failures.
     * 
     * @return the failure count
     * @since upcoming
     */
    public int getFailures() {
        return this.failures;
    }

    private static class TeeOutputStream extends FilterOutputStream {
        private final OutputStream tee;

        TeeOutputStream(OutputStream out, OutputStream tee) {
            super(out);
            this.tee = tee;
        }

        public void close() throws IOException {
            super.close();
            tee.close();
        }

        public void flush() throws IOException {
            super.flush();
            tee.flush();
        }

        public void write(int b) throws IOException {
            super.write(b);
            tee.write(b);
        }
    }
}
