/**
 * Using clocks to do simple producer consumer synchronization
 * for this task DAG (arrows point downward)
 * in pipelined fashion. On each clock period, 
 * each stage of the pipeline reads the previous clock period's
 * result from the previous stage and produces its new result
 * for the current clock period.
 *
 * <code>
    A   stage 0 A produces the stream 1,2,3,...
   / \
   B  C  stage 1 B is "double", C is "square" function
   \ /|
    D E  stage 2 D is \(x,y)(x+y+10), E is \(x)(x*7)
 * </code>
 *
 * @author kemal 4/2005
 */

value class boxedInt {
	int val;
}
public class ClockTest10 {
	int[] varA= new int[2];
	int[] varB= new int[2];
	int[] varC= new int[2];
	int[] varD= new int[2];
	int[] varE= new int[2];
	const int N=10;
	const int pipeDepth=2;

	static int ph(int x) { return x%2;}

	public boolean run() {
	     finish {	
      		final clock a = clock.factory.clock();
      		final clock b = clock.factory.clock();
      		final clock c = clock.factory.clock();
		async clocked(a) taskA(a); 
		async clocked(a,b) taskB(a,b); 
		async clocked(a,c) taskC(a,c); 
		async clocked(b,c) taskD(b,c); 
		async clocked(c) taskE(c); 
             }
	     return true;
	}

	void taskA(final clock a) { 
          for(point [k]:1:N) {
 	     varA[ph(k)]=k;
	     System.out.println(k+" A producing "+varA[ph(k)]);
             next;
          }
        }
	void taskB(final clock a, final clock b) { 
          for(point [k]:1:N) {
	     final boxedInt tmp=new boxedInt();
	     finish{
		now(a) {
	                tmp.val=varA[ph(k-1)]+varA[ph(k-1)];
			System.out.println(k+" B consuming oldA producing "+tmp.val);
	        }
                a.resume();
	     }
	     varB[ph(k)]=tmp.val;
	     System.out.println("B before next");
             next;
           }
        }
	void taskC(final clock a, final clock c) { 
          for(point [k]:1:N) {
	     final boxedInt tmp=new boxedInt();
	     finish{
		now(a) {
	                tmp.val=varA[ph(k-1)]*varA[ph(k-1)];
			System.out.println(k+" C consuming oldA "+ tmp.val);
	        }
                a.resume();
	     }
	     varC[ph(k)]=tmp.val;
	     System.out.println("C before next");
             next;
           }
        }
        
	void taskD(final clock b, final clock c) { 
	     
          for(point [k]:1:N) {
	     final boxedInt tmp=new boxedInt();
	     finish{
		now(c)now(b) {
	                tmp.val=varB[ph(k-1)]+varC[ph(k-1)]+10;
	                System.out.println(k+" D consuming oldB+oldC producing "+tmp.val);
	        }
                c.resume();
		b.resume();
	     }
	     varD[ph(k)]=tmp.val;
	     System.out.println(k+" D before next");
	     int n=k-pipeDepth;
	     chk(!(k==N) || varD[ph(k)]==n+n+n*n+10);
             next;
          }
        }
	void taskE(final clock c) { 
          for(point [k]:1:N) {
	     final boxedInt tmp=new boxedInt();
	     finish{
		now(c) {
	                tmp.val=varC[ph(k-1)]*7;
	                System.out.println(k+" E consuming oldC producing "+tmp.val);
	        }
                c.resume();
	     }
	     varE[ph(k)]=tmp.val;
	     System.out.println(k+" E before next");
	     int n=k-pipeDepth;
	     chk(!(k==N) || varE[ph(k)]==n*n*7);
             next;
          }
        }

        static void chk(boolean b) {
		if (!b) throw new Error();
	}

	public static void main(String args[]) {
		boolean b= (new ClockTest10()).run();
		System.out.println("++++++ "+(b?"Test succeeded.":"Test failed."));
		System.exit(b?0:1);
	}

}
