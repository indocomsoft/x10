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

 

/**
 * Purpose: Checks dependent type constraint information are propagated along with the variable.
 * Issue: Constant to promotedoes not meet constraint of targeted type.
 * @author vcave
 **/
public class CastPrimitiveLitteralToPrimitiveConstrained_MustFailCompile   {

	public def run(): boolean {
		
		try { 
                        val j: int{self==0} = 1 as int{self==0};
		}catch(e: Throwable) {
			return false;
		}

		return true;
	}

	public static def main(args: Rail[String]): void {
		new CastPrimitiveLitteralToPrimitiveConstrained_MustFailCompile().run ();
	}

}
 