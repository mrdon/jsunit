/*
JsUnitBuilder - a tool to create JsUnit test suites
Copyright (C) 2002 Jeff Sabin

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation in version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Test suites built with JsUnit are derivative works derived from tested 
classes and functions in their production; they are not affected by this 
license.
*/

package com.nextpage.test;
  
import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;


/**
 * Writing unit tests for the JavaScript testing framework JsUnit (Joerg Schaible's version)
 * is somewhat awkward. This is because the author tried to match the framework very closely
 * to Junit and JavaScript does not map real well. This class, therefore, is 
 * an attempt to make writing these unit tests very simple. It searches a specified
 * directory for all .JS files, parses out the test functions and creates the
 * specified .HTML file adding appropriate calls to the JsUnit framework. This
 * method guarantees that all unit tests are found rather than requiring the
 * unit test author to remember to add his test to the prototype list and to the
 * suite.<p>
 * 
 * This class presupposes the following conventions:<br>
 * <ol>
 * <li>All .JS files to be tested reside in a single directory.
 * <li>All .JS test files reside in a single directory (different from those being tested).
 * <li>All test files are named to match the file being tested with the word "Test" appended.
 * For example, TreeTest.js contains unit tests for Tree.js.
 * <li>All test functions are name [filename]_[function name]. For example, 
 * the test function testAddNode which resides in the TreeTest.js file would actually
 * be named TreeTest_testAddNode.  Like this<br>
 * <code>
 *      function TreeTest_testAddNode(){<br>
 *          // do something here<br>
 *      }<br>
 * </code>
 * </ol>
 * <p>
 * 
 * Therefore, instead of writing a unit test that looks like this <p>
 * 
 *  // Tree Test Class <br>
 *  function TreeTestClass(name){ <br>
 *      this.constructor.call( this, name ); <br>
 *  } <br>
 *  function TreeTestClass_testAdd(){ <br>
 *          this.assertEquals(4, add(2,2)); <br>
 *          this.assertEquals(5, add(2,2)); <br>
 *  } <br>
 *  function TreeTestClasss_test2Add(){ <br>
 *      this.assertEquals(6, add(2,3)); <br>
 *  } <br>
 *  TreeTestClass.prototype = new TestCase(); <br>
 *  TreeTestClass.prototype.testAdd = TreeTestClass_testAdd; <br>
 *  TreeTestClass.prototype.test2Add = TreeTestClass_test2Add; <p>
 * 
 * You would write this instead<p>
 * 
 * function TreeTestClass_testAdd(){ <br>
 *          this.assertEquals(4, add(2,2)); <br>
 *          this.assertEquals(5, add(2,2)); <br>
 *  } <br>
 *  function TreeTestClasss_test2Add(){ <br>
 *      this.assertEquals(6, add(2,3)); <br>
 *  } <br>
 *
 * The output of this utility is an HTML file that will execute all tests.<b>
 * 
 * @author Jeff Sabin
 * @version 1.0
 * 
 */ 
public class JsUnitBuilder {

    /** Search for all JavaScript functions whoes name is of the format 
     * <file name>_<test function name> and return test function name.
     */
    private static final String FUNCTION_NAME_REGEX = "(function\\s)(.*)(_)(.*)(\\()";

    /** Directory of Javascript Files to be tested */
    private File    _toTestDir;

    /** Directoy of test files */
    private File    _testDir;

    /** Name of the output file */
    private String  _output;

    /** Directory of JsUnit framework files */
    private File    _jsunitDir;

    /** 
     * Builds JavaScript test xml
     * 
     * @param toTestDir     directory of .js files to be tested
     * @param testDir       directory of .js files that contain the unit tests
     * @param output        name of output file 
     * @param jsunitDir     directory to the JsUnit framework .js files
     * 
     * @exception IllegalArgumentException if parameters are null or invalid
     */
    public JsUnitBuilder(File toTestDir, File testDir, String output, File jsunitDir) 
            throws IllegalArgumentException{
        if (toTestDir == null || testDir == null || output == null)
            throw new IllegalArgumentException("Arguments are null");
        if (!toTestDir.exists())
            throw new IllegalArgumentException("Directory to be tested does not exist");
        if (!testDir.exists())
            throw new IllegalArgumentException("Testing directory does not exist");
        if (!jsunitDir.exists())
            throw new IllegalArgumentException("JsUnit framework directory does not exists");

        _toTestDir = toTestDir;
        _output = output;
        _testDir = testDir;
        _jsunitDir = jsunitDir;
    }

    /**
     * Write beginning HTML tags an JavaScript includes.
     * 
     * @param writer    output to write to
     */ 
    private void writeHeader(PrintWriter writer){
        writer.println("<HTML>");
        writer.println("<HEAD>");
        writer.println("    <TITLE>Runs all JavaScript unit tests</TITLE>");

        // Include JsUnit framework
        writer.println("<!-- Include JsUnit Framework -->");
        writer.println("<script language=\"JavaScript1.3\" src=\"" + 
            _jsunitDir.getAbsolutePath() + "/JsUtil.js\"></script>");
        writer.println("<script language=\"JavaScript1.3\" src=\"" + 
            _jsunitDir.getAbsolutePath() + "/JsUnit.js\"></script>");
        writer.println("<script language=\"JavaScript1.3\" src=\"" + 
            _jsunitDir.getAbsolutePath() + "/JsUnitHTML.js\"></script>");
    }

    /**
     * Write all JavaScript includes to the output
     * 
     * @param writer        output to write to
     * @param testFiles     array of test files
     * @param toTestFiles   array of files to be tested
     * 
     */
    private void writeIncludes(PrintWriter writer, File[] testFiles, File[] toTestFiles){
        writer.println();
        writer.println("<!-- Include Javascript files to test -->");
        for(int i = 0; i < toTestFiles.length; i++){
            writer.println("<script language=\"JavaScript1.3\" src=\"" + 
                toTestFiles[i].getAbsolutePath() + "\"></script>");
        }

        writer.println();
        writer.println("<!-- Include Javascript test files -->");
        for(int i = 0; i < testFiles.length; i++){
            writer.println("<script language=\"JavaScript1.3\" src=\"" + 
                testFiles[i].getAbsolutePath() + "\"></script>");
        }
    }

    /**
     * Display a warning if any files to be tested do not have a corresponding 
     * test file.
     * 
     * @param testFiles     array of test files
     * @param toTestFiles   array of files to be tested
     */
    private void warnOfMissingTests(File[] testFiles, File[] toTestFiles){
        for(int i = 0; i < toTestFiles.length; i++){
            String toTest = toTestFiles[i].getName().toLowerCase();

            // Add test to file name
            toTest = stripExtension(toTest) + "test.js";
            
            boolean found = false;
            for(int j = 0; j < testFiles.length; j++){
                if (toTest.equals(testFiles[j].getName().toLowerCase()))
                    found = true;
            }
            if (!found)
                System.out.println("WARNING: No test found for " + toTestFiles[i].getName());
        }
    }

    /**
     * Returns the filename without the extension
     * 
     * @param filename  file name to strip extension from
     * 
     * @return file name without extension
     */
    private String stripExtension(String filename){
        int index = filename.lastIndexOf(".");
        return filename.substring(0, index);
    }

    /**
     * Returns the names of all unit tests in a test file. This method assumes
     * that the file is small enough that it can all be read in memory (bad assumption?).
     * 
     * @param testFile file that contains the tests
     * 
     * @return array of unit tests names found in the file or an empty array if found
     *      no tests or could not open the file
     */
    private String[] getUnitTests(File testFile){
        BufferedReader reader = null;
        ArrayList tests = new ArrayList();
        StringBuffer buffer = new StringBuffer();
        String line = null;
        try{
            // Read the file into a buffer
            reader = new BufferedReader(new FileReader(testFile));
            while( (line = reader.readLine()) != null){
                buffer.append(line);
                buffer.append('\n');
            }

            String content = buffer.toString();
            Pattern pattern = Pattern.compile(FUNCTION_NAME_REGEX , Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);

            boolean found = true;
            int i = 0;
            while (found = matcher.find()){
                tests.add(matcher.group(4));
                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return (String[])tests.toArray(new String[tests.size()]);
    }

    private void createTestFile(){
        File[] testFiles = _testDir.listFiles(new JavaScriptFileFilter());
        File[] toTestFiles = _toTestDir.listFiles(new JavaScriptFileFilter());

        warnOfMissingTests(testFiles, toTestFiles);

        PrintWriter writer = null;
        try{
            writer = new PrintWriter(new FileOutputStream(new File(_testDir, _output)));
        }
        catch(Exception e){
            System.out.println("Could not create file " + _output);
            return;
        }

        writeHeader(writer);
        writeIncludes(writer, testFiles, toTestFiles);

        writer.println();
        writer.println("<SCRIPT language=\"JavaScript1.3\">");

        // For each test file
        // 1) Write constructor
        // 2) Add prototype entry for each test in file
        // 3) Add test to suite
        for(int i = 0; i < testFiles.length; i++){
            String fileName = stripExtension(testFiles[i].getName());
            writer.println("    <!-- " + fileName + " test case -->");
            writer.println("    function " + fileName + "(name){");
            writer.println("        this.constructor.call( this, name );");
            writer.println("    }");
            writer.println("    " + fileName + ".prototype = new TestCase();");

            String[] testNames = getUnitTests(testFiles[i]);
            for(int j = 0; j < testNames.length; j++){
                writer.println("    " + fileName + ".prototype." + 
                    testNames[j] + " = " + fileName + "_" + testNames[j]);
            }
            writer.println();
        }

        // Create suite
        writer.println();
        writer.println("    <!-- Create Test Suite -->");
        writer.println("    function JavaScriptTestSuite(){");

        // Add each test to suite
        for(int i = 0; i < testFiles.length; i++){
            String testName = stripExtension(testFiles[i].getName());
            writer.println("        this.addTestSuite( " + testName + " );");
        }
        writer.println("    }");
        writer.println("    JavaScriptTestSuite.prototype = new TestSuite();");


        // Run the suite
        writer.println();
        writer.println("    var runner = new HTMLTestRunner();");
        writer.println("    runner.addSuite(new JavaScriptTestSuite());");
        writer.println("    runner.start(\"all\");");
        writer.println("</SCRIPT>");
        writer.println("</HEAD>");
        writer.println("</html>");

        writer.flush();
        writer.close();
    }

    private static void displayHelp(){
        System.out.println("Builds JsUnit HTML file to run all JavaScript unit tests.");
        System.out.println();
        System.out.println("USAGE:");
        System.out.println("java JsUnitBuilder <toTestDir> <testDir> <output> <JsUnitDir>");
        System.out.println("where");
        System.out.println("    toTestDir   - directory contains .js files to test.");
        System.out.println("    testDir     - directory containing .js test files.");
        System.out.println("    output      - name of the output files (needs to end in .html or .htm.");
        System.out.println("    JsUnitDir   - directory containing JsUnit framework (.js files)");
    }

    /** 
     * args[0] - Directory of .js files to be tested<br>
     * args[1] - Directory of .js files that contain the unit tests<br>
     * args[2] - Name of output file (created in the same directory as args[1]<br>
     * args[3] - Directory of JsUnit library<br>
     */
    public static void main (String args[]) {
        if (args.length < 4){
            displayHelp();
            return;
        }
        JsUnitBuilder testBuilder = new JsUnitBuilder(new File(args[0]), new File(args[1]), args[2], new File(args[3]));
        testBuilder.createTestFile();
    }
}

class JavaScriptFileFilter implements FileFilter {

    public boolean accept(File pathname) {
        if (pathname == null)
            return false;

        if (pathname.getName().toLowerCase().endsWith(".js")){
            return true;
        }
        return false;
    }
}
