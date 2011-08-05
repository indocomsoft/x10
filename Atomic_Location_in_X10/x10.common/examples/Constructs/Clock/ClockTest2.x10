
// Automatically generated by the command
// m4 ClockTest2.m4 > ClockTest2.x10
// Do not edit
/**
 * Test for 'now'.  Very likely to fail if now is not translated
 * properly (but depends theoretically on the scheduler).
 */
public class ClockTest2 {

    int val=0;
    static final int N=10;

    public boolean run() {
        final clock c = clock.factory.clock();
        for (int i=0;i<N;i++) {
          async(here) clocked(c) finish async(here) {  
            async(here){ 
              atomic { 
                val++; 
              }  
            } 
          }
          next;
          int temp;
          atomic {temp=val;}
          if (temp != i+1) return false;
        } 
        if (c.dropped())
            return false;
        c.drop();
        if(!c.dropped())
            return false;
        

        return true;
    }
    
    public static void main(String[] args) {
        final boxedBoolean b=new boxedBoolean();
        try {
            finish async b.val=(new ClockTest2()).run();
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