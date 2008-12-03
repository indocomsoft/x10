// (C) Copyright IBM Corporation 2008
// This file is part of X10 Test. *

import harness.x10Test;

import x10.compiler.ArithmeticOps;

/**
 * @author bdlucas 8/2008
 */

public class GenericMethods3 extends GenericTest {

    def m[T,U](u:U,t:T) = t;

    public def run() = {

        check("m[int,String](\"1\",1)", m[int,String]("1",1), 1);
        check("m[String,int](1,\"1\")", m[String,int](1,"1"), "1");

        return result;
    }

    public static def main(var args: Rail[String]): void = {
        new GenericMethods3().execute();
    }
}
