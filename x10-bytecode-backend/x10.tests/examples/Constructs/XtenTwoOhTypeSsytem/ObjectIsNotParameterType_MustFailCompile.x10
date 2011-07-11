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

/**
 * A value of an unconstrained type parameter T cannot be assigned to a variable of type Object.
 *
 * @author vj 
 */
public class ObjectIsNotParameterType_MustFailCompile extends x10Test {
	class GenericWrapper[T] {
		  public def testAssign(x:T) {
		    var dummy:Object = null;
		  // bad!!
		    x=dummy;
		  }
		}
	
	public def run()=true;

	public static def main(Rail[String]) {
		new ObjectIsNotParameterType_MustFailCompile().execute();
	}

  
}