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
 * A simple Calculator.
 */
function Calculator() {
    this.accumulator = 0;
}

function Calculator_add(value) {
    return this.accumulator += value;
}

function Calculator_clear() {
    return this.accumulator = 0;
}

function Calculator_get() {
    return this.accumulator;
}

function Calculator_sub(value) {
    return this.accumulator -= value;
}
 
Calculator.prototype.add = Calculator_add;
Calculator.prototype.clear = Calculator_clear;
Calculator.prototype.get = Calculator_get;
Calculator.prototype.sub = Calculator_sub;
