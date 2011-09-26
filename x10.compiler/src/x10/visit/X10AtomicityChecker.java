package x10.visit;

import java.util.Map;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;
import x10.types.X10ClassType;
import x10.util.CollectionFactory;
import x10.util.X10TypeUtils;

/**
 * This class checks typing rules of data-centric synchronization.
 * 
 * @author Sai Zhang
 */
public class X10AtomicityChecker extends X10TypeChecker {
	
	private x10.ExtensionInfo extensionInfo;

	public X10AtomicityChecker(Job job, TypeSystem ts, NodeFactory nf) {
		this(job, ts, nf, CollectionFactory.<Node, Node>newHashMap());
	}
	
	public X10AtomicityChecker(Job job, TypeSystem ts, NodeFactory nf, Map<Node, Node> memo) {
	    this(job, ts, nf, memo, false);
	}
	public X10AtomicityChecker(Job job, TypeSystem ts, NodeFactory nf,
			Map<Node, Node> memo, boolean isFragmentChecker) {
		super(job, ts, nf, memo);
		this.extensionInfo = (x10.ExtensionInfo) job.extensionInfo();
		this.memo = memo;
	}
	
	public Node override(Node parent, Node n) {
		return null;
	}

	protected NodeVisitor enterCall(Node n) {
		return this;
	}
	
	protected Node leaveCall(Node old, Node n, NodeVisitor v) {
		//System.out.println("x10 atomicity checking");
	    final TypeChecker tc = (TypeChecker) v;
	    
	    //get the current class, for compatibility, only checks the compiled classes
	    X10ClassType clzType = this.context.currentClass();
	    boolean isCompiled = true;
	    if(clzType != null && clzType.fullName()!= null && X10TypeUtils.skipProcessingClass(clzType)) {
	    	isCompiled = false;
	    }
	    
	    Node m = n;
	    //check the typing rules for data-centric synchronization
	    if(this.extensionInfo.getOptions().x10_config.DATA_CENTRIC && isCompiled) {
	    	m = m.del().checkAtomicity(tc);
	    	m = m.del().checkLinkProperty(tc);
	    }
	    return m;
	}

}