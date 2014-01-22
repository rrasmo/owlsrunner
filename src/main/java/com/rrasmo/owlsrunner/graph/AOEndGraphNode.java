package com.rrasmo.owlsrunner.graph;

import java.util.Hashtable;
import java.util.Vector;

import org.mindswap.owls.process.ControlConstruct;

import com.rrasmo.owlsrunner.OwlsRunner;

/**
 *
 * @author Rafael Ramos
 */
public class AOEndGraphNode extends SingleChildGraphNode {
    
    int incomingBranches;
    int completedBranches;
    Vector<String> incomingBranchNodes;
    Vector<String> remainingBranchNodes;
    Hashtable<String,GraphNode> branchFirstNodes;
    
    public AOEndGraphNode(String theName, String theType, ControlConstruct theCC, CompPerfGraphNode theParentPerform, OwlsRunner theRunner) {
        super(theName,theType,theCC,theParentPerform,theRunner);
        branchFirstNodes = new Hashtable<String,GraphNode>();
        incomingBranchNodes = new Vector<String>();
        remainingBranchNodes = new Vector<String>();
        incomingBranches = 0;
        completedBranches = 0;
    }
    
    //overrides GraphNode.enable()
    @Override
    public boolean enable(String requester) {
        //update completed branches
        completedBranches++;
        remainingBranchNodes.remove(requester);
        
        //if all branches are completed, enable
        if(completedBranches == incomingBranches) {
            init();
            enabled = true;
            runner.addEnabledNode(this);
            return true;
        }
        //else enable the remaining branches
        else {
            for(int i = 0; i < remainingBranchNodes.size(); i++) {
                GraphNode firstNode = branchFirstNodes.get(remainingBranchNodes.elementAt(i));
                firstNode.enabled = true;
                runner.addEnabledNode(firstNode);
            }
            return false;
        }        
    }
    
    public void incIncomingBranches() {
        incomingBranches++;
    }
    
    public void addIncomingBranchNode(String nodeName) {
        incomingBranchNodes.add(nodeName);
    }
    
    public void addBranchFirstNode(String nodeName, GraphNode node) {
        branchFirstNodes.put(nodeName,node);
    }
    
    public void init() {
        remainingBranchNodes = incomingBranchNodes;
        completedBranches = 0;
    }
    
    @Override
    public void reset() {
        super.reset();
        init();
    }
    
}
