package com.rrasmo.owlsrunner.graph;

import org.mindswap.owls.process.ControlConstruct;

import com.rrasmo.owlsrunner.OwlsRunner;

/**
 *
 * @author Rafael Ramos
 */
public class SpGraphNode extends MultipleChildGraphNode {
    
    GraphNode mainBranchNode;   //the first node of the only branch which is not a forked one
    
    public SpGraphNode(String theName, String theType, ControlConstruct theCC, CompPerfGraphNode theParentPerform, OwlsRunner theRunner) {
        super(theName,theType,theCC,theParentPerform,theRunner);
        mainBranchNode = null;
    }
    
    public void setMainBranchNode(GraphNode theMainBranchNode) {
        mainBranchNode = theMainBranchNode;
    }
    
    public GraphNode getMainBranchNode() {
        return mainBranchNode;
    }
    
}
