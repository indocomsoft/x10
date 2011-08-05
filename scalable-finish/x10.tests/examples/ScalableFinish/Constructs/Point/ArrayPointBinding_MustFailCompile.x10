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
 * Cannot bind point components in array declaration.
 *
 * @author igor, 1/2006
 */

public class ArrayPointBinding_MustFailCompile   {

    public def run(): boolean = {

        p(i,j): Rail[Point] = new Rail[Point](1);
        p(0) = [1,2];

        return (i == 1 && j == 2);
    }

    public static def main(args: Rail[String]): void = {
        new ArrayPointBinding_MustFailCompile().run ();
    }
}
