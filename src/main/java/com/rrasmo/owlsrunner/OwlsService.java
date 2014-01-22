package com.rrasmo.owlsrunner;

import impl.owls.process.execution.ProcessExecutionEngineImpl;

import java.net.URI;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.mindswap.owl.OWLDataValue;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLValue;
import org.mindswap.owls.process.AnyOrder;
import org.mindswap.owls.process.AtomicProcess;
import org.mindswap.owls.process.Binding;
import org.mindswap.owls.process.BindingList;
import org.mindswap.owls.process.Choice;
import org.mindswap.owls.process.CompositeProcess;
import org.mindswap.owls.process.Condition;
import org.mindswap.owls.process.Conditional;
import org.mindswap.owls.process.ControlConstruct;
import org.mindswap.owls.process.ControlConstructBag;
import org.mindswap.owls.process.ControlConstructList;
import org.mindswap.owls.process.IfThenElse;
import org.mindswap.owls.process.Output;
import org.mindswap.owls.process.OutputBinding;
import org.mindswap.owls.process.OutputBindingList;
import org.mindswap.owls.process.Parameter;
import org.mindswap.owls.process.ParameterValue;
import org.mindswap.owls.process.Perform;
import org.mindswap.owls.process.Produce;
import org.mindswap.owls.process.RepeatUntil;
import org.mindswap.owls.process.RepeatWhile;
import org.mindswap.owls.process.Result;
import org.mindswap.owls.process.Sequence;
import org.mindswap.owls.process.Split;
import org.mindswap.owls.process.SplitJoin;
import org.mindswap.owls.process.ValueData;
import org.mindswap.owls.process.ValueOf;
import org.mindswap.owls.service.Service;
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.query.ABoxQuery;
import org.mindswap.query.ValueMap;
import org.mindswap.swrl.AtomList;

import com.rrasmo.owlsrunner.graph.AOEndGraphNode;
import com.rrasmo.owlsrunner.graph.AOStartGraphNode;
import com.rrasmo.owlsrunner.graph.ChoEndGraphNode;
import com.rrasmo.owlsrunner.graph.ChoStartGraphNode;
import com.rrasmo.owlsrunner.graph.CompPerfGraphNode;
import com.rrasmo.owlsrunner.graph.ConditionalGraphNode;
import com.rrasmo.owlsrunner.graph.GraphNode;
import com.rrasmo.owlsrunner.graph.GraphNodePair;
import com.rrasmo.owlsrunner.graph.IteEndGraphNode;
import com.rrasmo.owlsrunner.graph.IteStartGraphNode;
import com.rrasmo.owlsrunner.graph.MultipleChildGraphNode;
import com.rrasmo.owlsrunner.graph.PerfGraphNode;
import com.rrasmo.owlsrunner.graph.ProdGraphNode;
import com.rrasmo.owlsrunner.graph.RUGraphNode;
import com.rrasmo.owlsrunner.graph.RWGraphNode;
import com.rrasmo.owlsrunner.graph.SingleChildGraphNode;
import com.rrasmo.owlsrunner.graph.SpGraphNode;
import com.rrasmo.owlsrunner.graph.SpJEndGraphNode;
import com.rrasmo.owlsrunner.graph.SpJStartGraphNode;

/**
 * A class for each single service. It manages the control graph and does several tasks related to the service.
 * 
 * @author Rafael Ramos
 */
public class OwlsService {
    
    OwlsRunner runner;
    OWLKnowledgeBase kb;
    MyProcessExecutionEngine exec;
    Service serviceM;
    Hashtable<String,GraphNode> graph;
    GraphNode start;
    String prefix;
    String uri;
    Perform rootPerform;
    HashMap<Perform,ValueMap> performValues;
    
    /**
     * Creates a new OwlsService object from the OWL-S file in the given URI, with the given prefix.
     * @param owlsUri
     * @param thePrefix
     * @param theRunner
     */
    public OwlsService(String owlsUri, String thePrefix, OwlsRunner theRunner) {
        kb = OWLFactory.createKB();
        exec = new MyProcessExecutionEngine();
        prefix = thePrefix;
        uri = owlsUri;
        runner = theRunner;
        performValues = new HashMap<Perform,ValueMap>();

        //load the OWL-S into the KB
        try {
            serviceM = kb.readService(owlsUri);
        }
        catch(Exception ex) {
            System.err.println(ex.getMessage());
        }        

        //make a RootPerform instance with the main process
        rootPerform = kb.createPerform(URI.create(owlsUri + "#RootPerform"));
        rootPerform.setProcess(serviceM.getProcess());

        //build the protocol graph        
        buildGraph();
        //printGraph();
    }
    
    /**
     * Resets all nodes in the graph and removes all data.
     */
    public void reset() {
        //reset state variables in every node
        Iterator nodeIterator = graph.values().iterator();
        while(nodeIterator.hasNext()) {
            GraphNode node = (GraphNode) nodeIterator.next();
            node.reset();
        }

        //empty the performValues table
        performValues.clear();
    }
    
    /**
     * Builds the graph.
     */
    private void buildGraph() {
        graph = new Hashtable<String,GraphNode>();
        org.mindswap.owls.process.Process process = serviceM.getProcess();
            
        if(process instanceof AtomicProcess) {
            PerfGraphNode performNode = new PerfGraphNode(prefix + ":RootPerform","PerformNode",rootPerform,null,runner);            
            performNode.setEnding(true);
            graph.put(performNode.getName(),performNode); 
            
            start = performNode;
        }        
        else if(process instanceof CompositeProcess) {
            CompositeProcess cProcess = (CompositeProcess) process;
            
            CompPerfGraphNode compositePerformNode = new CompPerfGraphNode(prefix + ":RootPerform","CompositePerformNode",rootPerform,null,runner);            
            compositePerformNode.setEnding(true);
            graph.put(compositePerformNode.getName(),compositePerformNode);      

            ControlConstruct component = cProcess.getComposedOf();

            GraphNodePair subgraph = makeGraph(component,compositePerformNode);
            if(subgraph != null) {                
                compositePerformNode.start = subgraph.firstNode;
                compositePerformNode.end = subgraph.lastNode;
                subgraph.lastNode.setEnding(true);
            }
            
            start = compositePerformNode;
        }
    }

    /**
     * Scans recursively the protocol tree to convert it into a graph.
     * @param cc
     * @param parentPerform
     * @return references to the start and end nodes of a subgraph
     */
    private GraphNodePair makeGraph(ControlConstruct cc, CompPerfGraphNode parentPerform) {
        //this function builds recursively the graph
        
        GraphNodePair pair = new GraphNodePair();
        
        if(cc instanceof Perform) {
            org.mindswap.owls.process.Process process = ((Perform) cc).getProcess();
            if(process instanceof AtomicProcess) {
                //return a pair with one node
                PerfGraphNode performNode = new PerfGraphNode(prefix + ":" + cc.getLocalName(),"PerformNode",cc,parentPerform,runner);
                graph.put(performNode.getName(),performNode);       //insert the new node into the Hashtable

                pair.firstNode = performNode;                   //set the pair with the only node
                pair.lastNode = performNode;
            }
            else if(process instanceof CompositeProcess) {
                CompPerfGraphNode compositePerformNode = new CompPerfGraphNode(prefix + ":" + cc.getLocalName(),"CompositePerformNode",cc,parentPerform,runner);                
                graph.put(compositePerformNode.getName(),compositePerformNode);       //insert the new node into the Hashtable
                
                ControlConstruct component = ((CompositeProcess) process).getComposedOf();
                
                GraphNodePair subgraph = makeGraph(component,compositePerformNode);
                if(subgraph != null) {                
                    compositePerformNode.start = subgraph.firstNode;
                    compositePerformNode.end = subgraph.lastNode;
                    subgraph.lastNode.setEnding(true);
                }
                
                pair.firstNode = compositePerformNode;                   //set the pair with the only node
                pair.lastNode = compositePerformNode;
            }            
        }
        else if(cc instanceof Sequence) {
            //chain every subgraph in the sequence
            ControlConstructList components = ((Sequence) cc).getComponents();
            GraphNode previousNode = null;
            if(components != null) {
                for(int i = 0; i < components.size(); i++) {
                    GraphNodePair subgraph = makeGraph(components.constructAt(i),parentPerform);
                    if(subgraph != null) {                  //if the subgraph is null, ignore it
                        if(previousNode == null) {
                            pair.firstNode = subgraph.firstNode;        //grab the first node from the first subgraph
                        }
                        else {
                            linkLastNode(previousNode,subgraph.firstNode);  //link the last of the previous to the first of this
                        }
                        previousNode = subgraph.lastNode;
                    }
                }
            }
            if(pair.firstNode == null) {    //if there were no components, the sequence is null
                return null;
            }
            pair.lastNode = previousNode;       //grab the last node from the last subgraph
        }
        else if(cc instanceof Choice) {
            //make start and end nodes for the choice
            ChoStartGraphNode choiceStartNode = new ChoStartGraphNode(prefix + ":" + cc.getLocalName() + "_Start","ChoiceStartNode",cc,parentPerform,runner);
            ChoEndGraphNode choiceEndNode = new ChoEndGraphNode(prefix + ":" + cc.getLocalName() + "_End","ChoiceEndNode",cc,parentPerform,runner);
            
            //place every subgraph between the start node and the end node
            ControlConstructBag components = ((Choice) cc).getComponents();
            if(components != null) {
                for(int i = 0; i < components.size(); i++) {
                    GraphNodePair subgraph = makeGraph(components.constructAt(i),parentPerform);
                    if(subgraph != null) {                  
                        choiceStartNode.nextNodes.put(subgraph.firstNode.getName(),subgraph.firstNode);
                        linkLastNode(subgraph.lastNode,choiceEndNode);

                        subgraph.firstNode.setDecisivePreviousNode(choiceStartNode);
                    }
                    else {
                        choiceStartNode.nextNodes.put(choiceEndNode.getName(),choiceEndNode);

                        choiceEndNode.setDecisivePreviousNode(choiceStartNode);
                    }
                }
            }
            if(choiceStartNode.nextNodes.isEmpty()) {    //if there were no branches, the choice is null
                return null;
            }
            
            graph.put(choiceStartNode.getName(),choiceStartNode);   //insert the new nodes into the Hashtable
            graph.put(choiceEndNode.getName(),choiceEndNode);
            
            pair.firstNode = choiceStartNode;       //set the pair with the start and end nodes
            pair.lastNode = choiceEndNode;
        }
        else if(cc instanceof Split) {
            //make one node for the split
            SpGraphNode splitNode = new SpGraphNode(prefix + ":" + cc.getLocalName(),"SplitNode",cc,parentPerform,runner);
            
            //place every subgraph under the split node
            ControlConstructBag components = ((Split) cc).getComponents();
            if(components != null) {
                for(int i = 0; i < components.size(); i++) {
                    GraphNodePair subgraph = makeGraph(components.constructAt(i),parentPerform);
                    if(subgraph != null) {                  //if the subgraph is null, ignore it
                        splitNode.nextNodes.put(subgraph.firstNode.getName(),subgraph.firstNode);
                        //the last nodes of the subgraphs are dead ends, so they aren't linked
                    }
                }
            }
            if(splitNode.nextNodes.isEmpty()) {    //if there were no branches, the split is null
                return null;
            }
            
            graph.put(splitNode.getName(),splitNode);   //insert the new node into the Hashtable
            
            pair.firstNode = splitNode;       //set the pair with the only node
            pair.lastNode = splitNode;
        }
        else if(cc instanceof SplitJoin) {
            //make start and end nodes for the SplitJoin
            SpJStartGraphNode splitJoinStartNode = new SpJStartGraphNode(prefix + ":" + cc.getLocalName() + "_Start","SplitJoinStartNode",cc,parentPerform,runner);
            SpJEndGraphNode splitJoinEndNode = new SpJEndGraphNode(prefix + ":" + cc.getLocalName() + "_End","SplitJoinEndNode",cc,parentPerform,runner);
            
            //place every subgraph between the start node and the end node
            ControlConstructBag components = ((SplitJoin) cc).getComponents();
            if(components != null) {
                for(int i = 0; i < components.size(); i++) {
                    GraphNodePair subgraph = makeGraph(components.constructAt(i),parentPerform);
                    if(subgraph != null) {                  //if the subgraph is null, ignore it
                        splitJoinStartNode.nextNodes.put(subgraph.firstNode.getName(),subgraph.firstNode);
                        linkLastNode(subgraph.lastNode,splitJoinEndNode);

                        splitJoinEndNode.incIncomingBranches();
                    }
                }
            }
            if(splitJoinStartNode.nextNodes.isEmpty()) {    //if there were no branches, the SplitJoin is null
                return null;
            }
            
            graph.put(splitJoinStartNode.getName(),splitJoinStartNode);   //insert the new nodes into the Hashtable
            graph.put(splitJoinEndNode.getName(),splitJoinEndNode);
            
            pair.firstNode = splitJoinStartNode;       //set the pair with the start and end nodes
            pair.lastNode = splitJoinEndNode;
        }
        else if(cc instanceof AnyOrder) {
            //make start and end nodes for the AnyOrder
            AOStartGraphNode anyOrderStartNode = new AOStartGraphNode(prefix + ":" + cc.getLocalName() + "_Start","AnyOrderStartNode",cc,parentPerform,runner);
            AOEndGraphNode anyOrderEndNode = new AOEndGraphNode(prefix + ":" + cc.getLocalName() + "_End","AnyOrderEndNode",cc,parentPerform,runner);
            
            //place every subgraph between the start node and the end node
            ControlConstructBag components = ((AnyOrder) cc).getComponents();
            if(components != null) {
                for(int i = 0; i < components.size(); i++) {
                    GraphNodePair subgraph = makeGraph(components.constructAt(i),parentPerform);
                    if(subgraph != null) {                  //if the subgraph is null, ignore it
                        anyOrderStartNode.nextNodes.put(subgraph.firstNode.getName(),subgraph.firstNode);
                        linkLastNode(subgraph.lastNode,anyOrderEndNode);

                        subgraph.firstNode.setDecisivePreviousNode(anyOrderStartNode);                    
                        anyOrderEndNode.incIncomingBranches();
                        anyOrderEndNode.addIncomingBranchNode(subgraph.lastNode.getName());
                        anyOrderEndNode.addBranchFirstNode(subgraph.firstNode.getName(),subgraph.firstNode);
                    }
                }
            }
            anyOrderEndNode.init();
            if(anyOrderStartNode.nextNodes.isEmpty()) {    //if there were no branches, the AnyOrder is null
                return null;
            }
            
            graph.put(anyOrderStartNode.getName(),anyOrderStartNode);   //insert the new nodes into the Hashtable
            graph.put(anyOrderEndNode.getName(),anyOrderEndNode);
            
            pair.firstNode = anyOrderStartNode;       //set the pair with the start and end nodes
            pair.lastNode = anyOrderEndNode;
        }
        else if(cc instanceof IfThenElse) {
            //make start and end nodes for the IfThenElse
            IteStartGraphNode ifThenElseStartNode = new IteStartGraphNode(prefix + ":" + cc.getLocalName() + "_Start","IfThenElseStartNode",cc,parentPerform,runner);
            IteEndGraphNode ifThenElseEndNode = new IteEndGraphNode(prefix + ":" + cc.getLocalName() + "_End","IfThenElseEndNode",cc,parentPerform,runner);
            
            //place the "then" subgraph into the "true" branch
            GraphNodePair subgraph = makeGraph(((IfThenElse) cc).getThen(),parentPerform);
            if(subgraph != null) {                  //if the subgraph is not null, link it
                ifThenElseStartNode.trueNextNode = subgraph.firstNode;
                linkLastNode(subgraph.lastNode,ifThenElseEndNode);
            }
            else {                                  //else bypass it
                ifThenElseStartNode.trueNextNode = ifThenElseEndNode;
            }
            
            //place the "else" subgraph into the "false" branch
            subgraph = makeGraph(((IfThenElse) cc).getElse(),parentPerform);
            if(subgraph != null) {                  //if the subgraph is not null, link it
                ifThenElseStartNode.falseNextNode = subgraph.firstNode;
                linkLastNode(subgraph.lastNode,ifThenElseEndNode);
            }
            else {                                  //else bypass it
                ifThenElseStartNode.falseNextNode = ifThenElseEndNode;
            }           
            
            if(ifThenElseStartNode.trueNextNode == null && ifThenElseStartNode.falseNextNode == null) {    //if there were no branches, the IfThenElse is null
                return null;
            }
            
            graph.put(ifThenElseStartNode.getName(),ifThenElseStartNode);   //insert the new nodes into the Hashtable
            graph.put(ifThenElseEndNode.getName(),ifThenElseEndNode);
            
            pair.firstNode = ifThenElseStartNode;       //set the pair with the start and end nodes
            pair.lastNode = ifThenElseEndNode;            
        }
        else if(cc instanceof RepeatWhile) {
            //make one node for the RepeatWhile
            RWGraphNode repeatWhileNode = new RWGraphNode(prefix + ":" + cc.getLocalName(),"RepeatWhileNode",cc,parentPerform,runner);
            
            //place the loop subgraph after the node
            GraphNodePair subgraph = makeGraph(((RepeatWhile) cc).getComponent(),parentPerform);
            if(subgraph != null) {                  //if the subgraph is not null, link it
                repeatWhileNode.trueNextNode = subgraph.firstNode;
                linkLastNode(subgraph.lastNode,repeatWhileNode);
            }
            else {                                  //else make a loopback
                repeatWhileNode.trueNextNode = repeatWhileNode;
            }            
            //the RepeatWhile subgraph can't be null, not even if its component is null
            
            graph.put(repeatWhileNode.getName(),repeatWhileNode);   //insert the new node into the Hashtable
            
            pair.firstNode = repeatWhileNode;       //set the pair with the only node
            pair.lastNode = repeatWhileNode;  
        }
        else if(cc instanceof RepeatUntil) {
            //make one node for the RepeatUntil
            RUGraphNode repeatUntilNode = new RUGraphNode(prefix + ":" + cc.getLocalName(),"RepeatUntilNode",cc,parentPerform,runner);
            
            //place the loop subgraph before the node
            GraphNodePair subgraph = makeGraph(((RepeatUntil) cc).getComponent(),parentPerform);
            if(subgraph != null) {                  //if the subgraph is not null, link it
                linkLastNode(subgraph.lastNode,repeatUntilNode);
                repeatUntilNode.falseNextNode = subgraph.firstNode;                
            }
            else {                                  //else make a loopback
                repeatUntilNode.falseNextNode = repeatUntilNode;
            }            
            //the RepeatUntil subgraph can't be null, not even if its component is null
            
            graph.put(repeatUntilNode.getName(),repeatUntilNode);   //insert the new node into the Hashtable
            
            pair.firstNode = subgraph.firstNode;       //set the pair with the first node of the loop and the repeatUntilNode
            pair.lastNode = repeatUntilNode;           
        }
        else if(cc instanceof Produce) {
            //return a pair with one node
            ProdGraphNode produceNode = new ProdGraphNode(prefix + ":" + cc.getLocalName(),"ProduceNode",cc,parentPerform,runner);
            graph.put(produceNode.getName(),produceNode);       //insert the new node into the Hashtable

            pair.firstNode = produceNode;                   //set the pair with the only node
            pair.lastNode = produceNode;
        }
        else {
            return null;
        }
             
        return pair;
    }
    
    /**
     * Sets a link between node1 and node2, according to the type of node1.
     * @param node1
     * @param node2
     */
    private void linkLastNode(GraphNode node1, GraphNode node2) {
        if(node1 instanceof SingleChildGraphNode) {
            ((SingleChildGraphNode) node1).nextNode = node2;
        }
        else if(node1 instanceof MultipleChildGraphNode) {
            ((MultipleChildGraphNode) node1).nextNodes.put(node2.getName(),node2);    
            
            if(node1 instanceof SpGraphNode) {
                ((SpGraphNode) node1).setMainBranchNode(node2);
            }
        }
        else if(node1.getType().equals("RepeatWhileNode")) {
            ((ConditionalGraphNode) node1).falseNextNode = node2; 
        }
        else if(node1.getType().equals("RepeatUntilNode")) {
            ((ConditionalGraphNode) node1).trueNextNode = node2; 
        }
        else {
            //this shouldn't happen
            System.err.println("error");
        }        
    }
        
    /**
     * Invokes a process to the Web Service with a given set of inputs.
     * @param node
     * @param inputValueMap
     * @return outputValueMap
     * @throws java.lang.Exception
     */
    public ValueMap executeProcess(PerfGraphNode node, ValueMap inputValueMap) throws Exception {
        ValueMap outputValueMap = null;        
        org.mindswap.owls.process.Process proc = ((Perform) node.getCC()).getProcess();      
      
        //this is the blocking call to the OWL-S API method execute()
        outputValueMap = exec.execute(proc,inputValueMap);
        
        return outputValueMap;
    }
    
    /**
     * Adds the given values to the node entry in the performValues table, 
     * @param node
     * @param values
     */
    public void storePerformValues(PerfGraphNode node, ValueMap values) {
        Perform perf = (Perform) node.getCC();      
        ValueMap existingValues = (ValueMap) performValues.get(perf);
        if(existingValues == null) {
            performValues.put(perf,values);
        }
        else {
            existingValues.addMap(values);
        }
    }
            
    class MyProcessExecutionEngine extends ProcessExecutionEngineImpl {
        //this class is used to call some protected functions in ProcessExecutionEngine
        
        public void doCheckPreconditions(org.mindswap.owls.process.Process process, ValueMap values) {
            checkPreconditions(process,values);            
        }
        
        public void doInitEnv(OWLKnowledgeBase defaultKB) {
            initEnv(defaultKB);
        }
        
        public boolean doIsTrue(Condition condition, ValueMap binding) {
            return isTrue(condition,binding);
        }
    }
    
    /**
     * Evaluates preconditions of a Perform.
     * Throws an Exception if they are false.
     * @param node
     * @param values contains values for Locals at the end
     */
    public void checkPreconditions(PerfGraphNode node, ValueMap values) {
        org.mindswap.owls.process.Process proc = ((Perform) node.getCC()).getProcess();
        
        exec.doInitEnv(proc.getKB());
        exec.doCheckPreconditions(proc,values);
    }
    
    /**
     * Evaluates the condition of a conditional node.
     * @param node
     * @return the result of the evaluation
     */
    public boolean checkCondition(ConditionalGraphNode node) {
        Condition condition = ((Conditional) node.getCC()).getCondition();
        ValueMap values = (ValueMap) performValues.get((Perform) node.getParentPerform().getCC());
        
        /*System.out.println("condition: " + condition.getBody().toQuery().toString());                
        System.out.println("Values="+values);
        AtomList atoms = condition.getBody();
        System.out.println("Atoms = " + atoms);
        atoms = atoms.apply( values );
        System.out.println("Atoms = " + atoms);
        ABoxQuery query = atoms.toQuery();
        System.out.println("Query = " + query);
        query = atoms.toQuery();
        System.out.println("Query = " + query);*/
        
        //evaluate the condition with the values of the parent perform
        boolean conditionValue = exec.doIsTrue(condition,values);        
        node.setConditionValue(conditionValue);
        
        return conditionValue;        
    }
    
    /**
     * Evaluates the conditions of every Result of a Perform, until it finds one that fulfills them
     * @param node
     * @return a suitable Result, or null
     */
    public Result checkResultConditions(CompPerfGraphNode node) {
        org.mindswap.owls.process.Process process = ((Perform) node.getCC()).getProcess();      //the M process        
        ValueMap values = (ValueMap) performValues.get((Perform) node.getCC());    //the stored values
        
        //iterate through the results to find one whose conditions are true
        Iterator resultIterator = process.getResults().iterator();
        while(resultIterator.hasNext()) {
            Result result = (Result) resultIterator.next();
            //System.out.println("checking result");
            
            //check all inConditionS. If there aren't any, the result is valid
            Iterator conditionIterator = result.getConditions().iterator();
            boolean conditionIsTrue = true;
            while(conditionIterator.hasNext() && conditionIsTrue) {
                Condition condition = (Condition) conditionIterator.next();
                
                //System.out.println("Values="+values);
                AtomList atoms = condition.getBody();
                //System.out.println("Atoms = " + atoms);
                atoms = atoms.apply( values );
                //System.out.println("Atoms = " + atoms);
                ABoxQuery query = atoms.toQuery();
                //System.out.println("Query = " + query);
                query = atoms.toQuery(result.getParameters());
                //System.out.println("Query = " + query);
                
                List resultVarBindings = kb.query(query);
                
                if(resultVarBindings.isEmpty()) {
                    conditionIsTrue = false;
                }
                else {
                    values.addMap((ValueMap) resultVarBindings.get(0));
                }
            }
            
            //if this result's conditions are true, return it
            if(conditionIsTrue) {
                return result;
            }
        }
        
        return null;
    }
        
    /**
     * Applies the bindings to the inputs of a Perform.
     * @param node
     * @return the new values
     */
    public ValueMap applyPerformBindings(PerfGraphNode node) {
        ValueMap values = new ValueMap();
        ValueMap selfBinding = new ValueMap();

        Perform perf = (Perform) node.getCC();
        BindingList bindings = perf.getBindings();
        for(int i = 0; i < bindings.size(); i++) {
            Binding binding = bindings.bindingAt(i);
            Parameter param = binding.getParameter();
            ParameterValue paramValue = binding.getValue();
            if(paramValue instanceof ValueData) {
                values.setValue(param, ((ValueData) paramValue).getData());
            }
            else if(paramValue instanceof ValueOf) {
                ValueOf valueOf = (ValueOf) paramValue;

                Perform otherPerform = valueOf.getPerform();
                if(otherPerform.equals(OWLS.Process.ThisPerform)) {
                    otherPerform = perf;
                }
                else if(otherPerform.equals(OWLS.Process.TheParentPerform)) {
                    otherPerform = (Perform) node.getParentPerform().getCC();
                }
                Parameter otherParam = valueOf.getParameter();

                ValueMap performValue = (ValueMap) performValues.get(otherPerform);	
                if(performValue == null) {
                    System.err.println("Missing perform " + otherPerform);
                    continue;
                }
                OWLValue value = performValue.getValue(otherParam);
                if(value == null) {
                    if(otherPerform.equals(perf)) {
                        selfBinding.setValue(param,otherParam);
                    }
                    else {
                        System.err.println("There is no value for " + param + " (bound to Perform: " + otherPerform + " Parameter: " + otherParam + ")");
                        continue;
                    }                             
                }
                else {
                    values.setValue(param,value);
                }
            }
        }

        //apply bindings coming from this Perform
        for(Iterator i = selfBinding.getVariables().iterator(); i.hasNext(); ) {
            Parameter param = (Parameter) i.next();
            Parameter otherParam = (Parameter) selfBinding.getValue( param );

            OWLValue value = values.getValue(otherParam);
            if(value == null) {
                System.err.println("There is no value for " + param + " (bound to Perform: " + perf + " Parameter: " + otherParam + ")");
                continue;
            }
            values.setValue(param, value);
        }

        return values;        
    }    
    
    /**
     * Applies the bindings of a given Result to the outputs of a Perform.
     * @param node
     * @param result
     * @return a ValueMap with the new values
     */
    public ValueMap applyResultBindings(PerfGraphNode node, Result result) {
        ValueMap values = new ValueMap();
        
        Perform perf = (Perform) node.getCC();        
      
        OutputBindingList bindings = result.getBindings();
        for(int i = 0; i < bindings.size(); i++) {
            OutputBinding binding = bindings.outputBindingAt(i);
            Output output = binding.getOutput();
            OWLValue value = null;
            ParameterValue paramValue = binding.getValue();
            if(paramValue instanceof ValueData) {
                values.setValue(output, ((ValueData) paramValue).getData());
            }
            else if(paramValue instanceof ValueOf) {
                ValueOf valueOf = (ValueOf) paramValue;
                Perform otherPerform = valueOf.getPerform();
                if(otherPerform.equals(OWLS.Process.ThisPerform)) {
                    otherPerform = perf;
                }
                else if(otherPerform.equals(OWLS.Process.TheParentPerform)) {
                    otherPerform = (Perform) node.getParentPerform().getCC();
                }
                Parameter param = valueOf.getParameter();
                ValueMap performValue = (ValueMap) performValues.get(otherPerform);
                if(performValue != null) {
                    value = performValue.getValue(param);
                    values.setValue(output, value);
                }
            }
        }
        
        return values;
    }
    
    /**
     * Applies the bindings of a Produce node to the outputs of its parent Perform.
     * @param node
     * @return a ValueMap with the new values
     */
    public ValueMap applyProduceBindings(ProdGraphNode node) {
        ValueMap values = new ValueMap();
        
        Produce prod = (Produce) node.getCC();        
      
        OutputBindingList bindings = prod.getBindings();
        for(int i = 0; i < bindings.size(); i++) {
            OutputBinding binding = bindings.outputBindingAt(i);
            Output output = binding.getOutput();
            OWLValue value = null;
            ParameterValue paramValue = binding.getValue();
            if(paramValue instanceof ValueData) {
                values.setValue(output, ((ValueData) paramValue).getData());
            }
            else if(paramValue instanceof ValueOf) {
                ValueOf valueOf = (ValueOf) paramValue;
                Perform otherPerform = valueOf.getPerform();
                if(otherPerform.equals(OWLS.Process.TheParentPerform)) {
                    otherPerform = (Perform) node.getParentPerform().getCC();
                }
                Parameter param = valueOf.getParameter();
                ValueMap performValue = (ValueMap) performValues.get(otherPerform);
                if(performValue != null) {
                    value = performValue.getValue(param);
                    values.setValue(output, value);
                }
            }
        }
        
        return values;
    }
        
    void printGraph() {
        //this can be used for debugging purposes
        
        Iterator it = graph.values().iterator();
        while(it.hasNext()) {
            GraphNode node = (GraphNode) it.next();            
            System.out.println(node.getName() + " (" + node.getType() +")");
            
            if(node instanceof SingleChildGraphNode) {
                GraphNode child = ((SingleChildGraphNode) node).nextNode;
                if(child != null) {
                    System.out.println("    " + child.getName());
                }
            }
            else if(node instanceof MultipleChildGraphNode) {
                Iterator ite = ((MultipleChildGraphNode) node).nextNodes.keySet().iterator();
                while(ite.hasNext()) {
                    System.out.println("    " + (String) ite.next());
                }                
            }
            else if(node instanceof ConditionalGraphNode) {
                GraphNode childT = ((ConditionalGraphNode) node).trueNextNode;
                GraphNode childF = ((ConditionalGraphNode) node).falseNextNode;
                if(childT != null) {
                    System.out.println("    " + childT.getName() + " (T)");
                }
                if(childF != null) {
                    System.out.println("    " + childF.getName() + " (F)");
                }
            }            
        }
    }
    
    public void printPerformValues() {
        //this can be used for debugging purposes
        
        System.out.println("PERFORM VALUES");
        
        Iterator performIterator = performValues.keySet().iterator();
        while(performIterator.hasNext()) {
            Perform perf = (Perform) performIterator.next();
            System.out.println("  " + ((perf == rootPerform)?"RootPerform":perf.getLocalName()));
            
            printValueMap((ValueMap) performValues.get(perf));
        }
    }    
    
    public void printValueMap(ValueMap valueMap) {
        //this can be used for debugging purposes
        
        if(valueMap != null) {
            Iterator parameterIterator = valueMap.getVariables().iterator();
            while(parameterIterator.hasNext()) {
                Parameter param = (Parameter) parameterIterator.next();
                OWLValue value = valueMap.getValue(param);
                if(value != null) {
                    if(value instanceof OWLDataValue) {
                        System.out.println("    " + param.getLocalName() + " = " + ((OWLDataValue) value).getLexicalValue());
                    }
                    else if(value instanceof OWLIndividual) {
                        System.out.println("    " + param.getLocalName() + " = " + ((OWLIndividual) value).toRDF(false));
                    }
                }
            }
        }
    }
    
}
