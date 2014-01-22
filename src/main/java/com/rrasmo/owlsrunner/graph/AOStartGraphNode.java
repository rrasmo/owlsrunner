package com.rrasmo.owlsrunner.graph;

import org.mindswap.owls.process.ControlConstruct;

import com.rrasmo.owlsrunner.OwlsRunner;

/**
 *
 * @author Rafael Ramos
 */
public class AOStartGraphNode extends MultipleChildGraphNode {
    
    public AOStartGraphNode(String theName, String theType, ControlConstruct theCC, CompPerfGraphNode theParentPerform, OwlsRunner theRunner) {
        super(theName,theType,theCC,theParentPerform,theRunner);
    }
    
}