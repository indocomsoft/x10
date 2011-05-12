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

 /**  Check that a class can have a depclause, after the property list.
 
 *@author vj,10/30/2006
 *
 */

 

 public class ClassDepClause_MustFailCompile(i:int, j:int){i == j}    { 
  public static type ClassDepClause_MustFailCompile(i:int, j:int)=ClassDepClause_MustFailCompile{self.i==i,self.j==j};
  public def this(i: int, j: int): ClassDepClause_MustFailCompile(i,j) = { property(i,j);}
  public def run(): boolean = { 
	  var x: ClassDepClause_MustFailCompile(2,3) =  new ClassDepClause_MustFailCompile(2,3);
      return true;
    }
    
   public static def main(var args: Rail[String]): void = {
        new ClassDepClause_MustFailCompile(1,1).run ();
    }
 }
