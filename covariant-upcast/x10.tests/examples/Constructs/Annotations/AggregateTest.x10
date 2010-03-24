/*
 *  This file is part of the X10 project (http://x10-lang.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  (C) Copyright IBM Corporation 2006-2010.
 */

//LIMITATION: aggregate interface not found

import harness.x10Test;

public class AggregateTest extends x10Test {
    var x: int = 1;
    public def run(): boolean = {
	finish @aggregate { async { this.x=2;}}
	return true;
    }

    public static def main(var args: Rail[String]): void = {
        new AggregateTest().execute();
    }
}
