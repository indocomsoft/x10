/**
 * Test for arrays, regions and dists.
 * Based on original arraycopy3 by vj.
 *
 * @author kemal 1/2005
 */

public class ArrayCopy3 {
	
        /**
         * Throws an error iff b is false.
         */
	static void chk(boolean b) {
		if(!b) throw new Error();
	}
	
	/**
	 * Returns true iff point x is not in the domain of
	 * dist D
	 */
	static boolean outOfRange(final dist D, final point x) {
		boolean gotException=false;
		try{
			async(D[x]){}; // dummy op just to use D[x]
		} catch (Throwable e) {
			gotException=true;
		}
		return gotException;
	}
	
	/**
	 * Returns true iff A[i]==B[i] for all points i 
	 */
	
	public void arrayEqual(final int[.] A, final int[.] B) {
		final dist D=A.distribution;
		final dist E=B.distribution;
		// Spawn an activity for each index to 
		// fetch the B[i] value 
		// Then compare it to the A[i] value
		finish
		ateach(point i:D) chk(A[i]==future(E[i]){B[i]}.force());
	}
	
	/**
	 * Set A[i]=B[i] for all points i.
	 * Return false iff some assertion failed.
	 */
	
	public void arrayCopy(final int[.] A, final int[.] B) {
		final dist D=A.distribution;
		final dist E=B.distribution;
		// Allows message aggregation
		

		final dist D_1=dist.factory.unique(D.places()); 
		// number of times elems of a are accessed
		final int[.] accessed_a = new int[D];
		// number of times elems of b are accessed
		final int[.] accessed_b = new int[E];
		
		finish
		ateach (point x:D_1) {
			final place px=D_1[x];
			
			chk(here==px);
			final region LocalD = (D|px).region;

			for ( place py : (E|LocalD).places() ) {
				final region RemoteE = (E|py).region;
				final region Common = LocalD&&RemoteE;
				final dist D_common= D|Common;
				// the future's can be aggregated
				for(point i:D_common) {
					async(py) atomic accessed_b[i]+=1;
					final int temp=
						future(py){B[i]}.force();
					// the following may need to be bracketed in
					// atomic, unless the disambiguator
					// knows about dists
					A[i]=temp;
					atomic accessed_a[i]+=1;
				}
				// check if dist ops are working
				final dist D_notCommon= D-D_common;
				chk((D_common||D_notCommon).equals(D));
				final dist E_common= E|Common;
				final dist E_notCommon= E-E_common;
					
				chk((E_common||E_notCommon).equals(E));
				for(point k:D_common) {
					chk(D_common[k]==px);
					chk(outOfRange(D_notCommon,k));
					chk(E_common[k]==py);
					chk(outOfRange(E_notCommon,k));
					chk(D[k]==px && E[k]==py);
				}

				for (point k: D_notCommon) { 
					chk(outOfRange(D_common,k));
					chk(!outOfRange(D_notCommon,k));
					chk(outOfRange(E_common,k));
					chk(!outOfRange(E_notCommon,k));
					chk(!(D[k]==px && E[k]==py));
				}
				
			}
		}
		// ensure each A[i] was accessed exactly once
		finish ateach(point i:D) chk(accessed_a[i]==1);
		// ensure each B[i] was accessed exactly once
		finish ateach(point i:E) chk(accessed_b[i]==1);
	}

    const int N=3;

    /**
     * For all combinations of dists of arrays B and A,
     * do an array copy from B to A, and verify.
     */
    public boolean run() {
         final region R= [0:N-1,0:N-1,0:N-1,0:N-1];
         final region TestDists= [0:dist2.N_DIST_TYPES-1,0:dist2.N_DIST_TYPES-1];
         for(point distP[dX,dY]: TestDists) {
		
             final dist D=dist2.getDist(dX,R);
             final dist E=dist2.getDist(dY,R);
             chk(D.region.equals(E.region)&&D.region.equals(R)); 
             final int[.] A= new int[D];
             final int[.] B= new int[E]
	      (point p[i,j,k,l]){int x=((i*N+j)*N+k)*N+l; return x*x+1;};
             arrayCopy(A,B);
             arrayEqual(A,B);
         }
         return true;
    }

	/**
	 * main method
	 */
	
    public static void main(String[] args) {
        final boxedBoolean b=new boxedBoolean();
        try {
                finish async b.val=(new ArrayCopy3()).run();
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

/**
 * utility for creating a dist from a
 * a dist type int value and a region
 */
class dist2 {
   const int BLOCK=0;
   const int CYCLIC=1;
   const int BLOCKCYCLIC=2;
   const int CONSTANT=3;
   const int RANDOM=4;
   const int ARBITRARY=5;
   public const int N_DIST_TYPES=6;

   /**
    * Return a dist with region r, of type disttype
    *
    */

   public static dist getDist(int distType, region r) {
      switch(distType) {
         case BLOCK: return dist.factory.block(r);
         case CYCLIC: return dist.factory.cyclic(r);
         case BLOCKCYCLIC: return dist.factory.blockCyclic(r,3);
         case CONSTANT: return dist.factory.constant(r, here);
         case RANDOM: return dist.factory.random(r);
         case ARBITRARY: return dist.factory.arbitrary(r);
         default: throw new Error();
      }
     
   } 
}