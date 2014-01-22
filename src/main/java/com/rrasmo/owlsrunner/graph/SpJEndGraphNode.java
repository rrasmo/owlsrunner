package com.rrasmo.owlsrunner.graph;

import org.mindswap.owls.process.ControlConstruct;

import com.rrasmo.owlsrunner.OwlsRunner;

/**
 *
 * @author Rafael Ramos
 */
public class SpJEndGraphNode extends SingleChildGraphNode {
    
    int incomingBranches;
    int completedBranches;
    
    public SpJEndGraphNode(String theName, String theType, ControlConstruct theCC, CompPerfGraphNode theParentPerform, OwlsRunner theRunner) {
        super(theName,theType,theCC,theParentPerform,theRunner);
        incomingBranches = 0;
        completedBranches = 0;
    }
    
    //overrides GraphNode.enable()
    @Override
    public boolean enable(String requester) {
        //update completed branches
        completedBranches++;
        
        //if all branches are completed, enable
        if(completedBranches == incomingBranches) {
            enabled = true;
            runner.addEnabledNode(this);
            return true;
        }
        else {
            return false;
        }        
    }
        
    public void incIncomingBranches() {
        incomingBranches++;
    }
    
    @Override
    public void reset() {
        super.reset();
        completedBranches = 0;
    }
    
}
