package com.rrasmo.owlsrunner.graph;

import java.util.Iterator;
import java.util.Vector;

import org.mindswap.owls.process.ControlConstruct;

import com.rrasmo.owlsrunner.OwlsRunner;

/**
 * A superclass for all types of nodes.
 * 
 * @author Rafael Ramos
 */
public abstract class GraphNode {
    
    String name;
    String type;
    ControlConstruct cc;
    MultipleChildGraphNode decisivePreviousNode;
    CompPerfGraphNode parentPerform;
    boolean enabled;
    boolean ending;
    boolean stop;
    public OwlsRunner runner;
    
    public GraphNode(String theName, String theType, ControlConstruct theCC, CompPerfGraphNode theParentPerform, OwlsRunner theRunner) {
        name = theName;
        type = theType;
        cc = theCC;
        parentPerform = theParentPerform;
        runner = theRunner;
        enabled = false;
        ending = false;
        stop = false;
    }
    
    /**
     * When a child of a Choice_Start or AnyOrder_Start is disabled, this method disables its siblings.
     */
    public void closeSiblings() {
        if(decisivePreviousNode != null) {
            Iterator it = decisivePreviousNode.nextNodes.values().iterator();
            while(it.hasNext()) {
                //disable all childs of the parent but this
                GraphNode sibling = (GraphNode) it.next();
                if(sibling != this) {
                    sibling.enabled = false;
                    runner.removeEnabledNode(sibling);
                }
            }
        }
   
    }
    
    /**
     * Sets a node as disabled.
     */
    public void disable() {
        closeSiblings();
        enabled = false;
        runner.removeEnabledNode(this);
    }
    
    /**
     * Advances in the graph by enabling the next nodes.
     * @param newNodes contains the new enabled nodes at the end
     * @return true if service is finished, false otherwise
     */
    public abstract boolean advance(Vector<GraphNode> newNodes);
        
    /**
     * Sets a node as enabled.
     * @param requester is the node who has called this method
     * @return true if successful
     */
    public boolean enable(String requester) {
        enabled = true;
        runner.addEnabledNode(this);
        return true;
    }    
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public ControlConstruct getCC() {
        return cc;
    }
    
    public void setDecisivePreviousNode(MultipleChildGraphNode parent) {
        decisivePreviousNode = parent;
    }
    
    public MultipleChildGraphNode getDecisivePreviousNode() {
        return decisivePreviousNode;
    }
    
    public boolean getEnabled() {
        return enabled;
    }    
   
    public void setEnding(boolean val) {
        ending = val;
    }
    
    public CompPerfGraphNode getParentPerform() {
        return parentPerform;
    }    
   
    public void reset() {
        if(enabled) {
            enabled = false;
            runner.removeEnabledNode(this);
        }
    }    
    
    public boolean getStop() {
        return stop;
    }
    
    public void setStop(boolean val) {
        stop = val;
    }
    
}
