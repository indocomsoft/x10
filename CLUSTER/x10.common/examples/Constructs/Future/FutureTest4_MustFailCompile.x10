/**
 *@author kemal 4/2005
 *
 * Tests that free variables used in e in future{e}
 * are declared final.
 *
 * Expected result: must fail at compile time.
 *
 */

public class FutureTest4_MustFailCompile {
	const int N=8;

	/** 
	 * testing free variables in future expression
	 */
	public boolean run() {

		final int[.] A = new int[dist.factory.block([0:N-1,0:N-1])]
		(point [i,j]) {return N*i+j;};
		int x;
		int s;
		x=0;
		s=0;
		for(int i=0;i<N;i++) {
			s+=i;
			//===>compiler error: i, s not final
			x+=future(A.distribution[i,s%N]) {A[i,s%N]}.force();
		}
		System.out.println("x="+x);
		if (x!=252) return false;
		x=0;
		s=0;
		for(int i=0;i<N;i++) {
			s+=i;
			{final int I=i; final int S=s;
			 // no compiler error
			 x+=future(A.distribution[I,S%N]) {A[I,S%N]}.force();
			}
		}
		System.out.println("x="+x);
		return (x==252);
	}


	/**
	 * main method
	 */
	
    public static void main(String[] args) {
        final boxedBoolean b=new boxedBoolean();
        try {
                finish async b.val=(new FutureTest4_MustFailCompile()).run();
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