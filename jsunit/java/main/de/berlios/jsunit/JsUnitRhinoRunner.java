/*
 * Copyright (C) 2006,2007 Joerg Schaible
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

import org.mozilla.javascript.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;


/**
 * Manage a JsUnit environment based on Rhino.
 * 
 * @author J&ouml;rg Schaible
 * @since upcoming
 */
public class JsUnitRhinoRunner {

    private Context context;
    private final Scriptable scope;
    private final boolean printSummary;

    private static void loadResource(Context context, Scriptable scope, final String name) {
        final InputStream is = JsUnitRhinoRunner.class.getResourceAsStream("/" + name);
        if (is != null) {
            try {
                Reader reader = new InputStreamReader(is, "ISO-8859-1");
                context.evaluateReader(scope, reader, name, 1, null);
            } catch (final JavaScriptException e) {
                throw new JsUnitRuntimeException("Cannot evaluate JavaScript code of JsUnit", e);
            } catch (final UnsupportedEncodingException e) {
                throw new InternalError("Missing standard character set ISO-8859-1");
            } catch (final IOException e) {
                throw new InternalError("Cannot load resource " + name);
            } finally {
                close(is);
            }

        } else {
            throw new InternalError("Cannot find resource " + name);
        }
    }

    /**
     * Constructs a JsUnitRhinoRunner. A JavaScript context is created and initialized with the
     * JsUnit code.
     *
     * @throws JsUnitRuntimeException if the JavaScript code of JsUnit has errors.
     * @since upcoming
     */
    public JsUnitRhinoRunner() {
        this(false);
    }
    /**
     * Constructs a JsUnitRhinoRunner. A JavaScript context is created and initialized with the
     * JsUnit code.
     * 
     * @throws JsUnitRuntimeException if the JavaScript code of JsUnit has errors.
     * @since upcoming
     */
    public JsUnitRhinoRunner(boolean printSummary) {
        this.printSummary = printSummary;
        context = Context.enter();
        context.setOptimizationLevel(-1);
        scope = context.initStandardObjects(null, false);

        // Define some global functions particular to the shell. Note
        // that these functions are not part of ECMA.
        String[] names = { "print"};
        try {
            ((ScriptableObject)scope).defineFunctionProperties(names, JsUnitRhinoRunner.class,
                                                        ScriptableObject.DONTENUM);
        } catch (PropertyException e) {
            // ignore as only used for debugging
        }

        loadResource(context, scope, "JsUtil.js");
        loadResource(context, scope, "JsUnit.js");
        Context.exit();
    }

    public static void print(String val) {
        System.out.println(val);
    }

    /**
     * This exists to allow Rhino scripts to access the call stack
     */
    public static void generateException() {
        throw new RuntimeException();
    }

    /**
     * Load additional code into the JavaScript context. The provided reader is read until
     * execution and closed afterwards.
     * 
     * @param reader the reader providing the code
     * @param name an identifying name of the code (normally the file name)
     * @throws JsUnitException
     * @throws IOException
     * @since upcoming
     */
    public void load(final Reader reader, String name) throws JsUnitException, IOException {
        if (reader == null) {
            throw new IllegalArgumentException("The reader is null");
        }
        if (name == null) {
            name = "anonymous";
        }
        context = Context.enter(context);
        try {
            context.setOptimizationLevel(-1);
            context.evaluateReader(scope, reader, name, 1, null);
        } catch (final JavaScriptException e) {
            throw new JsUnitException("Cannot evaluate JavaScript code of " + name, e);
        } finally {
            close(reader);
            Context.exit();
        }
    }

    /**
     * Evaluate the given JavaScript in the current context.
     * 
     * @param code the JavaScript
     * @param name the name of the script (may be null)
     * @return the evaluated value
     * @throws JsUnitException if the code was not valid
     * @throws IllegalArgumentException if <code>code</code>is <code>null</code>
     * @since upcoming
     */
    public Object eval(final String code, String name) throws JsUnitException {
        if (code == null) {
            throw new IllegalArgumentException("The code is null");
        }
        if (name == null) {
            name = "anonymous";
        }
        context = Context.enter(context);
        context.setOptimizationLevel(-1);
        try {
            final Object result = context.evaluateString(scope, code, name, 1, null);
            return result;// Context.toString(result);
        } catch (final JavaScriptException e) {
            throw new JsUnitException("Cannot evaluate JavaScript code of " + name, e);
        } finally {
            Context.exit();
        }
    }

    /**
     * Runs the TestSuite &quot;AllTests&quot;. The result of the test is written in XML format
     * into the given writer. Since the result is a complete XML document, the writer is closed
     * by the method (even in case of an exception).
     * 
     * @param writer the writer receiving the result
     * @throws IOException if writing to the <code>writer</code> fails
     * @throws IllegalArgumentException if <code>writer</code>is <code>null</code>
     * @throws JsUnitRuntimeException if the JavaScript code of the method itself fails
     * @since upcoming
     */
    public void runAllTests(final Writer writer) throws IOException {
        if (writer == null) {
            throw new IllegalArgumentException("The writer is null");
        }
        context = Context.enter(context);
        context.setOptimizationLevel(-1);
        try {
            try {
                String runScript = ""
                    + "var stringWriter = new StringWriter();\n"
                    + "var runner = new EmbeddedTextTestRunner(" + getPrinterScript() + ");\n"
                    + "var collector = new AllTestsCollector(this);\n"
                    + "runner.run(collector.collectTests());\n"
                    + "stringWriter.get();\n";
                final String xml = (String)context.evaluateString(scope, runScript, "AllTests", 1, null);
                writer.write(xml);
            } catch (final EcmaError e) {
                throw new JsUnitRuntimeException("JavaScript error running tests", e);
            } catch (final JavaScriptException e) {
                throw new JsUnitRuntimeException("Cannot evaluate internal JavaScript code", e);
            }
        } finally {
            Context.exit();
            close(writer);
        }
    }

    private String getPrinterScript() {
        String printer = "new XMLResultPrinter(stringWriter)";
        if (printSummary) {
            printer = "new AggregateResultPrinter([" + printer + ",new ClassicResultPrinter(java.lang.System.out)])";
        }
        return printer;
    }

    /**
     * Runs all JavaScript TestSuites in the context. The method will collect any JavaScript
     * <code>TestSuite</code> in the context in a collecting <code>TestSuite</code> and run
     * it. The result of the test is written in XML format into the given writer. Since the
     * result is a complete XML document, the writer is closed by the method (even in case of an
     * exception).
     * 
     * @param writer the writer receiving the result
     * @param name the name of the collecting <code>TestSuite</code> (may be null)
     * @throws IOException if writing to the <code>writer</code> fails
     * @throws IllegalArgumentException if <code>writer</code>is <code>null</code>
     * @throws JsUnitRuntimeException if the JavaScript code of the method itself fails
     * @since upcoming
     */
    public void runTestSuites(final Writer writer, String name) throws IOException {
        if (writer == null) {
            throw new IllegalArgumentException("The writer is null");
        }
        name = name == null ? "AllTestSuites" : name;
        context = Context.enter(context);
        context.setOptimizationLevel(-1);
        try {
            try {
                final String xml = (String)context.evaluateString(scope, ""
                    + "var stringWriter = new StringWriter();\n"
                    + "var runner = new EmbeddedTextTestRunner(" + getPrinterScript() + ");\n"
                    + "var collector = new TestSuiteCollector(this);\n"
                    + "runner.run(collector.collectTests(), \""+ name + "\");\n"
                    + "stringWriter.get();\n", name, 1, null);
                writer.write(xml);
            } catch (final EcmaError e) {
                throw new JsUnitRuntimeException("JavaScript error running tests", e);
            } catch (final JavaScriptException e) {
                throw new JsUnitRuntimeException("Cannot evaluate internal JavaScript code", e);
            }
        } finally {
            Context.exit();
            close(writer);
        }
    }

    /**
     * Runs all JavaScript TestCases in the context. The method will collect any JavaScript
     * <code>TestCase</code> in the context in a collecting <code>TestSuite</code> and run
     * it. The result of the test is written in XML format into the given writer. Since the
     * result is a complete XML document, the writer is closed by the method (even in case of an
     * exception).
     * 
     * @param writer the writer receiving the result
     * @param name the name of the collecting <code>TestSuite</code> (may be null)
     * @throws IOException if writing to the <code>writer</code> fails
     * @throws IllegalArgumentException if <code>writer</code>is <code>null</code>
     * @throws JsUnitRuntimeException if the JavaScript code of the method itself fails
     * @since upcoming
     */
    public void runTestCases(final Writer writer, String name) throws IOException {
        if (writer == null) {
            throw new IllegalArgumentException("The writer is null");
        }
        name = name == null ? "AllTestCases" : name;
        context = Context.enter(context);
        context.setOptimizationLevel(-1);
        try {
            try {
                final String xml = (String)context.evaluateString(scope, ""
                    + "var stringWriter = new StringWriter();\n"
                    + "var runner = new EmbeddedTextTestRunner(" + getPrinterScript() + ");\n"
                    + "var collector = new TestCaseCollector(this);\n"
                    + "runner.run(collector.collectTests(), \""+ name + "\");\n"
                    + "stringWriter.get();\n", name, 1, null);
                writer.write(xml);
            } catch (final EcmaError e) {
                throw new JsUnitRuntimeException("JavaScript error running tests", e);
            } catch (final JavaScriptException e) {
                throw new JsUnitRuntimeException("Cannot evaluate internal JavaScript code", e);
            }
        } finally {
            Context.exit();
            close(writer);
        }
    }

    private static void close(final Writer writer) {
        try {
            writer.close();
        } catch (final IOException e) {
            // ignore
        }
    }

    private static void close(final Reader reader) {
        try {
            reader.close();
        } catch (final IOException e) {
            // ignore
        }
    }

    private static void close(final InputStream is) {
        try {
            is.close();
        } catch (final IOException e) {
            // ignore
        }
    }

}
