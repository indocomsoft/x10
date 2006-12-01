import x10.lang.Object;
import harness.x10Test;

/**
*  Implementing a 5-point stencil operation using foreach loop
* @author Tong
  11/29/2006
*/
public class StencilForeach2D extends x10Test {
	
        public boolean run() {
        	final region(:rank==2) R=[-1:256,-1:256], r=[0:255,0:255];
        	final point north=[0,1], south=[0,-1], west=[-1,0], east=[1,0];
        	final double [:rank==2] A=(double [:rank==2])new double [R];
        	final double h=0.1;
        	
        	finish foreach (point p: r) A[p]=(A[p+north]+A[p+south]+A[p+west]+A[p+east]-4*A[p])*h;
        	
	    return true;
	}
	
	public static void main(String[] args) {
		new StencilForeach2D().execute();
	}

}

