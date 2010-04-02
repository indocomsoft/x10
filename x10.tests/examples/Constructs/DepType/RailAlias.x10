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

import harness.x10Test;
import x10.array.Array;
import x10.array.Region;

/**
 * 
 *
 * @author igor
 */
public class RailAlias extends x10Test {
     public def this(): RailAlias = { }
     public def run(): boolean = {
          var r: Region{rail} = [0..10];
          var a: Array[double]{rail} = Array.make[double](r, (x:Point)=>0.0);
          var d: double = a(1);
          for (val (p) in a.region) a(p) = 1.0; 
          return true;
     }
     public static def main(var a: Rail[String]): void = {
    	 new RailAlias().execute();
     }
}
