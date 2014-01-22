package com.rrasmo.owlsrunner.graph;

import java.util.Vector;

import org.mindswap.owls.process.ControlConstruct;

import com.rrasmo.owlsrunner.OwlsRunner;

/**
 *
 * @author Rafael Ramos
 */
public abstract class ConditionalGraphNode extends GraphNode {
    
    public GraphNode trueNextNode;
    public GraphNode falseNextNode;
    boolean conditionValue;
    
    public ConditionalGraphNode(String theName, String theType, ControlConstruct theCC, CompPerfGraphNode theParentPerform, OwlsRunner theRunner) {
        super(theName,theType,theCC,theParentPerform,theRunner);
        conditionValue = false;
    }
    
    public boolean advance(Vector<GraphNode> newNodes) {
        //enable one childe depending on the condition                
        if(conditionValue == true && trueNextNode != null) {
            trueNextNode.enable(name);
            if(trueNextNode.enabled) {
                newNodes.add(trueNextNode);
            }
        }
        else if(conditionValue == false && falseNextNode != null) {
            falseNextNode.enable(name);
            if(falseNextNode.enabled) {
                newNodes.add(falseNextNode);
            }
        }       
        return ending && newNodes.isEmpty();
    }
    
    public void setConditionValue(boolean val) {
        conditionValue = val;
    }
    
    @Override
    public void reset() {
        super.reset();
        conditionValue = false;
    }
    
}
