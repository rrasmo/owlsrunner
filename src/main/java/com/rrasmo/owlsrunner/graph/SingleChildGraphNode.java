package com.rrasmo.owlsrunner.graph;

import java.util.Vector;

import org.mindswap.owls.process.ControlConstruct;

import com.rrasmo.owlsrunner.OwlsRunner;

/**
 *
 * @author Rafael Ramos
 */
public abstract class SingleChildGraphNode extends GraphNode {
    
    public GraphNode nextNode;
    
    public SingleChildGraphNode(String theName, String theType, ControlConstruct theCC, CompPerfGraphNode theParentPerform, OwlsRunner theRunner) {
        super(theName,theType,theCC,theParentPerform,theRunner);
    }
    
    public boolean advance(Vector<GraphNode> newNodes) {
        //enable the child
        if(nextNode != null) {
            nextNode.enable(name);            
            if(nextNode.enabled) {
                newNodes.add(nextNode);
            }
        }
        return ending;
    }
    
}
