/**************************************************************************
*                                                                         *
*             Java Grande Forum Benchmark Suite - Version 2.0             *
*                                                                         *
*                            produced by                                  *
*                                                                         *
*                  Java Grande Benchmarking Project                       *
*                                                                         *
*                                at                                       *
*                                                                         *
*                Edinburgh Parallel Computing Centre                      *
*                                                                         * 
*                email: epcc-javagrande@epcc.ed.ac.uk                     *
*                                                                         *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 1999.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/


import crypt.*;
import jgfutil.*; 

public class JGFCryptBenchSizeA { 

  public boolean run() {

    JGFInstrumentor.printHeader(2,0);

    JGFCryptBench cb = new JGFCryptBench(); 
    cb.JGFrun(0);
    return true;
 
  }
   /**
    * main method
    */
   
    public static void main(String[] args) {
        final boxedBoolean b=new boxedBoolean();
        try {
                finish async b.val=(new JGFCryptBenchSizeA()).run();
        } catch (Throwable e) {
                e.printStackTrace();
                b.val=false;
        }
        System.out.println("++++++ "+(b.val?"Test succeeded.":"Test failed."));
        x10.lang.Runtime.setExitCode(b.val?0:1);
    }
    static class boxedBoolean {
        boolean val=false;
    }
}