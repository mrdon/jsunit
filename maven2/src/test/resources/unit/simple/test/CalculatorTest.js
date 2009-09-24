/*
Copyright (C) 2006,2007 Joerg Schaible
Created on 02.10.2006 by Joerg Schaible

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/**
 * A test for the simple Calculator.
 */
function CalculatorTest(name) {
    TestCase.call(this, name);
}

function CalculatorTest_testAdd() {
    var calculator = new Calculator();
    this.assertEquals(5, calculator.add(5));
    this.assertEquals(5, calculator.get());
}

function CalculatorTest_testClear() {
    var calculator = new Calculator();
    calculator.sub(5);
    this.assertEquals(0, calculator.clear());
    this.assertEquals(0, calculator.get());
}

function CalculatorTest_testInit() {
    var calculator = new Calculator();
    this.assertEquals(0, calculator.get());
}

function CalculatorTest_testSub() {
    var calculator = new Calculator();
    this.assertEquals(-5, calculator.sub(5));
    this.assertEquals(-5, calculator.get());
}

CalculatorTest.prototype = new TestCase();
CalculatorTest.prototype.testAdd = CalculatorTest_testAdd;
CalculatorTest.prototype.testClear = CalculatorTest_testClear;
CalculatorTest.prototype.testInit = CalculatorTest_testInit;
CalculatorTest.prototype.testSub = CalculatorTest_testSub;
