/**
 * (c) IBM Corporation 2007
 * Author: Vijay Saraswat
 * 
 * 
 * A purely sequential breadth-first search, with no parallellism associated overhead. 
 * Intended to be used as the basis for speedup numbers.
 */


import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


public class BFSRef {
  public static int BATCH_SIZE=32;
  public static final 
  AtomicReferenceFieldUpdater<V,V> UPDATER = AtomicReferenceFieldUpdater.newUpdater(V.class, V.class, "parent");
  public final class V  extends Frame {
    public final int index;
    public volatile V parent;
    public V [] neighbors;
    public V next; // link for batching
    public V(int i){index=i;}
    volatile int PC=0;
    public void compute(Worker w)  {
      V node=this; 
      final int BS=BATCH_SIZE, pid = w.index;
      int nb=0;
      for (;;) {
        for (V v : node.neighbors) {
          if (UPDATER.get(v)==null && UPDATER.compareAndSet(v,null,this)) {
            v.next = batch[pid];
            batch[pid] = v;
            if (++nb >= BS) {
                final temp = batch[pid];
		async clocked(c) {next; temp.compute();}
		batch[pid]=null;
		nb=0;
            }
          }
        }  
        V nxt = node.next;
        if (nxt == null) break;
        node.next=null;
        node=nxt;
      }
    }
    public boolean verify() {
      final V root = G[1];
      V p = parent, oldP=null;
      int count=0;
      try { 
        while (! (p==null || reachesRoot[p.index] || p ==this || p == root || count == N)) {
          oldP=p;
          p = p.parent;
          count++;
        }
        boolean result = (count < N && p != null && ( p==root || reachesRoot[p.index]));
        reachesRoot[index]=result;
        return result;
      } finally {
        if (reporting) {
          if (count > N-10) {
            System.err.println(Thread.currentThread() + " finds possibly bad guy " + this +
                "count=" + count + " p=" + p );
          }
          if (! reachesRoot[index])
            System.err.println(Thread.currentThread() + " finds bad guy " + this +
                "count=" + count + " p=" + p  + " oldP=" + oldP);
        }
      }
    }
    @Override
    public String toString() {
      String s="[" + (neighbors.length==0? "]" : "" + neighbors[0].index);
      for (int i=1; i < neighbors.length; i++) s += ","+neighbors[i].index;
      return "v(" + index + ",degree="+neighbors.length+ ",n=" + s+"])";
    }
  }
  
  class E{
    public int v1,v2;
    public boolean in_tree;
    public E(int u1, int u2){ v1=u1;v2=u2;in_tree=false;}
  }
  
  int m;
  V[] G;
  E[] El;
  E[] El1;
  
  //AtomicIntegerArray visitCount ;
  int ncomps=0;
  
  static int[] Ns = new int[] {1000*1000, 2*1000*1000, 3*1000*1000, 4*1000*1000, 5*1000*1000};
  int N, M;
  
  int randSeed = 17673573; 
  int rand32() { return randSeed = 1664525 * randSeed + 1013904223;}
  public BFSRef (int n, int m, char graphType){
    N=n;
    M=m;
    if (graphType=='E') {
      G = new V[N]; for(int i=0;i<N;i++) G[i]=new V(i);
      randomEdgeGraph(G, new E[M]);
    } else if (graphType=='T') {
      N=n*n;
      G = new V[N]; for(int i=0;i<N;i++) G[i]=new V(i);
      torusGraph(n,G);
    } else if (graphType=='K') {
      G = new V[N]; for(int i=0;i<N;i++) G[i]=new V(i);
      kGraph(G);
    } else if (graphType=='R') {
      G = new V[N]; for(int i=0;i<N;i++) G[i]=new V(i);
      //rGraph(G,El);
    }
  }
  public void torusGraph (int k, final V [] graph){
    System.out.println("Generating graph...");
    int n = k*k,i,j,l,s;
    int [] buff = new int [n];
      V [][] adj = new V [n][4];//Java support arrays of arrays
      for(i=0;i<n;i++) buff[i]=i;

    /*  for(i=0;i<n/2;i++){
      l=(int)(Math.random()*n)%n;
       s=(int)(Math.random()*n)%n;
      j=buff[l];
      buff[l]=buff[s];
      buff[s]=j;
      }*/
  
      for(i=0;i<k;i++)
          for(j=0;j<k;j++)
       adj[buff[i*k+j]] = new V[]{graph[buff[((k+i-1)%k)*k+j]], 
              graph[buff[((i+1)%k)*k+j]],
                            graph[buff[i*k+((k+j-1)%k)]], 
                            graph[buff[i*k+((j+1)%k)]]};
    
       for(i=0;i<n;i++){
      // vj -- why is this? graph[i].parent=i;
            graph[i].neighbors=adj[i];
       }
       reachesRoot = new boolean[n];
       System.out.println("Graph generated.");
  }

  public void kGraph(final V [] graph){
    int n=N;
    int k=4;
    final int TIMES=5;
    final int THRESHOLD=100;
    int neighbor;
    char [] visited = new char [n];
    int [] stack = new  int [n];
    int [] SUPER = new int [n];
    int i,j,u,v,nextn,top=-1,n_comp=0,rep,s;
    int [] counter = new int [n];
    final int [][] array = new int [n][k*TIMES];

    for(i=0;i<n;i++){
      counter[i]=0;
      visited[i]=0;
    }

    for(i=0;i<n;i++){
      for(j=counter[i];j<k;j++){
        if(i<n-THRESHOLD)
          neighbor=(int)(Math.random()*(n-i))%(n-i)+i;
        else 
          neighbor=(int)(Math.random()*THRESHOLD)%(THRESHOLD);
        rep=0;
        for(s=0;s<counter[i];s++) 
          if(array[i][s]==neighbor) rep=1;
        while(rep==1){
          rep=0;
          if(i<n-THRESHOLD)
            neighbor=(int)(Math.random()*(n-i))%(n-i)+i;
          else 
            neighbor=(int)(Math.random()*THRESHOLD)%(THRESHOLD);
          for(s=0;s<counter[i];s++) 
            if(array[i][s]==neighbor) rep=1;
        }

        while(counter[neighbor]>TIMES*k-1 || neighbor==i) neighbor=(neighbor+1)%n;
        array[i][counter[i]]=neighbor;
        counter[i]++;
        array[neighbor][counter[neighbor]]=i;
        counter[neighbor]++;
      }
    }
  

      /* now make the graph connected if it is not*/
      for(i=0;i<n;i++){
            if(visited[i]==0){
        visited[i]=1;
        stack[++top]=i;
        SUPER[n_comp++]=i;

        while(top!=-1){
              v = stack[top];
              top--;

              for (j=0; j<counter[v]; j++) {
                  nextn = array[v][j];
                  if(visited[nextn]==0) {  /* not seen yet */
              visited[nextn]=1;
              stack[++top]=nextn;
                  }
              }
          }
            }
        }
 
      for(i=1;i<n_comp;i++){
            u = SUPER[i];
            v = SUPER[i-1];
            array[u][counter[u]++]=v;
            array[v][counter[v]++]=u;
        }

       for(i=0;i<n;i++){
      
      /*graph[i].self=i;*/
      //graph[i].parent=i;
       //graph[i].degree=counter[i];
            graph[i].neighbors=new V [counter[i]];
  
            for(j=0;j<counter[i];j++){
                  graph[i].neighbors[j]=graph[array[i][j]];
            }
       }
       reachesRoot = new boolean[n];
      System.out.println("generating graph done\n");
  }

  
  void randomEdgeGraph(V[] G, E[] El) {
    for (int i=0; i <M; i++) El[i] = new E(Math.abs(rand32())%N, Math.abs(rand32())%N);
    
    int[] D = new int [N];
    /* D[i] is the degree of vertex i (duplicate edges are counted).*/
    for(int i=0;i<M;i++){
      D[El[i].v1]++;
      D[El[i].v2]++;
    }
    
    int[][] NB = new int[N][];/*NB[i][j] stores the jth neighbor of vertex i*/
    // leave room for making connected graph by +2
    for(int i=0;i<N;i++) NB[i]=new int [D[i]+2]; 
    
    /*Now D[i] is the index for storing the neighbors of vertex i
     into NB[i] NB[i][D[i]] is the current neighbor*/
    for(int i=0;i<N;i++) D[i]=0;
    
    m=0;
    for(int i=0;i<M;i++) {
      boolean r=false;
      E edge = El[i];
      int s = edge.v1, e = edge.v2;
      /* filtering out repeated edges*/
      for(int j=0;j<D[s] && !r ;j++) if(e==NB[s][j]) r=true;
      if(r){
        edge.v1=edge.v2=-1; /*mark as repeat*/
      } else {
        m++;
        NB[s][D[s]++]=e;
        NB[e][D[e]++]=s;
      }
    }  
    
    /* now make the graph connected*/
    /* first we find all the connected comps*/
    
    //visitCount = new AtomicIntegerArray(N);
    int[] stack = new int [N]; 
    int[] connected_comps  = new int [N], level = new int[N]; 
    
    int top=-1;
    ncomps=0;
    for(int i=0;i<N ;i++) {
      if (level[i]==1) continue;
      connected_comps[ncomps++]=i;
      stack[++top]=i;
      level[i]=1;
      while(top!=-1) {
        int v = stack[top];
        top--;
        
        for(int j=0;j<D[v];j++) {
          final int mm = NB[v][j];
          if(level[mm] !=1){
            top++;
            stack[top]=mm;
            level[mm]=1;
          }
        }
      }
    }
    
    if (reporting && graphOnly) System.out.println("ncomps="+ncomps);
    El1 = new E [m+ncomps-1]; 
    
    
    int j=0;
    //    Remove duplicated edges
    for(int i=0;i<M;i++) if(El[i].v1!=-1) El1[j++]=El[i]; 
    
    if (reporting && graphOnly) 
      if(j!=m) 
        System.out.println("Remove duplicates failed");
      else System.out.println("Remove duplicates succeeded,j=m="+j);
    
    /*add edges between neighboring connected comps*/
    for(int i=0;i<ncomps-1;i++) {
      NB[connected_comps[i]][D[connected_comps[i]]++]=connected_comps[i+1];
      NB[connected_comps[i+1]][D[connected_comps[i+1]]++]=connected_comps[i];
      El1[i+m]=new E (connected_comps[i], connected_comps[i+1]);
    }
    
    //visited = new boolean[N];
    
    for(int i=0;i<N;i++) {
      G[i].neighbors=new V [D[i]];
      for(j=0;j<D[i];j++) {
        G[i].neighbors[j]=G[NB[i][j]];
        
      }
      if (reporting || graphOnly)
        System.out.println("G[" + i + "]=" + G[i]);
    }     
    reachesRoot = new boolean[N];
  }

  boolean[] reachesRoot;
  static BFSRef graph;
  static boolean reporting = false;
  static final long NPS = (1000L * 1000 * 1000);
  static boolean graphOnly =false, verification=true;
  public static void main(String[] args) {
    int procs;
    int num=-1;
    int D=4;
    char graphType='E';
    try {
      procs = Integer.parseInt(args[0]);
      System.out.println("Number of procs=" + procs);
      if (args.length > 1) {
        String s = args[1];
        if (s.equalsIgnoreCase("T")) {
          graphType='T'; 
        }
        else if (s.equalsIgnoreCase("R"))
          graphType='R'; 
        else if (s.equalsIgnoreCase("K"))
          graphType='K'; 
        System.out.println("graphType=" + graphType);
      }
      if (args.length > 2) {
        num = Integer.parseInt(args[2]);
        System.out.println("N=" + num);
      }
      
      if (args.length > 3) {
        D = Integer.parseInt(args[3]);
        System.out.println("D=" + D);
      }
      if (args.length > 4) {
        boolean b = Boolean.parseBoolean(args[4]);
        reporting=b;
      }
      if (args.length > 5) {
        boolean b = Boolean.parseBoolean(args[5]);
        graphOnly=b;
      }
      if (args.length > 6) {
        boolean b = Boolean.parseBoolean(args[6]);
        verification=b;
      }
      if (args.length > 7) {
        BATCH_SIZE=Integer.parseInt(args[7]);
        System.out.println("BATCH_SIZE=" + BATCH_SIZE);
      }
    }
    catch (Exception e) {
      System.out.println("Usage: java BFSRef <threads:nat> [<Type:T,E,K> [<N:nat> [<Degree:nat> [<reporting:boolean>" +
      "[<graphonly:boolean> [<verification:boolean> [<batchSize:boolean]]]]]]]");
      return;
    }
    
    if (num >= 0) {
      Ns = new int[] {num};
    }
    for (int i=Ns.length-1; i >= 0; i--) {
      
      final int N = Ns[i], M = D*(graphType=='T'?N:1)*N;
      // ensure the sole reference to the previous graph is nulled before gc.
      graph = null;
      System.gc();
      graph = new BFSRef(N,M, graphType);
      if (graphOnly) return;
         graph.batch = new V[procs];
      //System.out.printf("N:%8d ", N);
      for (int k=0; k < 20; ++k) {
        final V root = graph.G[1];
        root.parent=root;
        final BFSRef GGraph = graph;
        long s = System.nanoTime();
	finish {
	    @onCheckIn(Worker w) {
		final int pid = w.index;
		final V node = GGraph.batch[pid];
		if (node != null) {
                    async { next; node.compute();}
		    GGraph.batch[pid]=null;
		}
	    }
	    root.compute();
	}
        long t = (System.nanoTime() - s);
        double secs = ((double) t)/NPS;
        double MEps = 1000 * (M/(double) t);
        System.out.printf("N=%d t=%5.4f s %5.3f ME/s", N, secs, MEps);
        System.out.println();
        //if (! graph.verifyTraverse(root))
        //  System.out.printf("Test fails.");
        long verificationTime = - System.nanoTime();
        if (verification) {
          final BFSRef gGraph = graph;
          final int P = here.getPoolSize(), size = N/P;
          graph.verificationResult.value=true;
          for (int j=0; j < N; j++) graph.reachesRoot[j]=false;
	  finish foreach (int j=0; j < P; j++) 
	      for (int i=j*size; i <= (j+1)*size-1; ++i) 
		  if (! graph.G[i].verify()) {
		      graph.verificationResult.value=false;
		  }
          if (! graph.verificationResult.value)
            System.out.println(" false! ");
          else {
            verificationTime += System.nanoTime();
            double vSecs = ((double) verificationTime)/NPS;
            System.out.printf(" (verification %5.4f s)", vSecs );
            System.out.println();
          }
          //System.err.println("Standard check: " + graph.verifyTraverse(graph.G[1]));
        }
        graph.clearColor();
        
      }
      System.out.printf("Completed iterations for N=%d", N);
      System.out.println();
    }   
    
  }
  void clearColor() {
    for (int i = 0; i < N; ++i) {
      V v= graph.G[i];
      v.PC=0;
      UPDATER.set(v,null);
    }
      
  }
  V[] batch;
  static class BooleanRef {
    volatile boolean value=true;
  }
  public final class Verifier extends Frame {
    final int start, end; // both inclusive
    public Verifier(int s, int e) { start=s; end=e; }
    public void compute(Worker w) {
      w.popAndReturnFrame();
      for (int i=start; i <= end; i++) 
        if (! G[i].verify()) {
          verificationResult.value=false;
          return;
        }
    }
  }
  final BooleanRef verificationResult = new BooleanRef();
  
  
}
