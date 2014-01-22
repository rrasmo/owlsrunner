package com.rrasmo.owlsrunner.graph;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.mindswap.owls.process.ControlConstruct;

import com.rrasmo.owlsrunner.OwlsRunner;

/**
 *
 * @author Rafael Ramos
 */
public abstract class MultipleChildGraphNode extends GraphNode {
    
    public Hashtable<String,GraphNode> nextNodes;
    
    public MultipleChildGraphNode(String theName, String theType, ControlConstruct theCC, CompPerfGraphNode theParentPerform, OwlsRunner theRunner) {
        super(theName,theType,theCC,theParentPerform,theRunner);
        nextNodes = new Hashtable<String,GraphNode>();
    }
    
    public boolean advance(Vector<GraphNode> newNodes) {
        //enable all childs
        Iterator it = nextNodes.values().iterator();
        while(it.hasNext()) {
            GraphNode nextNode = (GraphNode) it.next();
            nextNode.enable(name);            
            if(nextNode.enabled) {
                newNodes.add(nextNode);
            }
        }
        return ending;
    }
    
}
