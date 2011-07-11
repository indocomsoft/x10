/**************************************************************************
*                                                                         *
*             Java Grande Forum Benchmark Suite - MPJ Version 1.0         *
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
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/


package series;
import jgfutil.*; 


public class JGFSeriesBench extends SeriesTest implements JGFSection2{ 

  private int size; 
  private int datasizes[]={10000,100000,1000000};

  public JGFSeriesBench() {
  }

  public void JGFsetsize(int size){
    this.size = size;
  }

  public void JGFinitialise(){
    array_rows = datasizes[size];

    buildTestData();
  }
 
  public void JGFkernel() {
    Do(); 
  }

  public void JGFvalidate(){
     double ref[][] = {{2.8729524964837996, 0.0},
                       {1.1161046676147888, -1.8819691893398025},
                       {0.34429060398168704, -1.1645642623320958},
                       {0.15238898702519288, -0.8143461113044298}};
/* 
// for 200 points 
    double ref[][] = {{2.8377707562588803, 0.0},
		       {1.0457844730995536, -1.8791032618587762},
		       {0.27410022422635033, -1.158835123403027},
		       {0.08241482176581083, -0.8057591902785817}};
*/ 
 
     final double[.] tmp_testArray = testArray;
    for (int i = 0; i < 4; i++){
      for (int j = 0; j < 2; j++){
        final int ii = i;
        final int jj = j;
        double cval = future(tmp_testArray.distribution[j,i]) { tmp_testArray[jj,ii] }.force();
	double error = Math.abs(cval - ref[i][j]);
	if (error > 1.0e-12 ){
	  System.out.println("Validation failed for coefficient " + j + "," + i);
	  System.out.println("Computed value = " + cval);
	  System.out.println("Reference value = " + ref[i][j]);
	  throw new Error("Validation failed");
	}
      }
    }
  }

  public void JGFtidyup(){
    freeTestData(); 
  }  



  public void JGFrun(int size) {

    JGFInstrumentor.addTimer("Section2:Series:Kernel", "coefficients",size);
    JGFsetsize(size); 
    JGFinitialise(); 
    JGFkernel(); 
    JGFvalidate(); 
    JGFtidyup(); 

    JGFInstrumentor.addOpsToTimer("Section2:Series:Kernel", (double) (array_rows * 2 - 1));
    JGFInstrumentor.printTimer("Section2:Series:Kernel"); 
  }
}