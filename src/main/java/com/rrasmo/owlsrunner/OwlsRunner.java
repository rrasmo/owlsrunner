package com.rrasmo.owlsrunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.mindswap.owl.EntityFactory;
import org.mindswap.owl.OWLDataValue;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLValue;
import org.mindswap.owls.process.Input;
import org.mindswap.owls.process.Local;
import org.mindswap.owls.process.Output;
import org.mindswap.owls.process.Perform;
import org.mindswap.owls.process.Result;
import org.mindswap.query.ValueMap;

import se.liu.ida.JessTab.JConsolePanel;

import com.rrasmo.owlsrunner.graph.CompPerfGraphNode;
import com.rrasmo.owlsrunner.graph.ConditionalGraphNode;
import com.rrasmo.owlsrunner.graph.GraphNode;
import com.rrasmo.owlsrunner.graph.PerfGraphNode;
import com.rrasmo.owlsrunner.graph.ProdGraphNode;
import com.rrasmo.owlsrunner.gui.RunnerGUI;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.NamespaceManager;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.triplestore.TripleStoreModel;
import edu.stanford.smi.protegex.owl.model.util.ImportHelper;

/**
 * The main class. It manages the model, the open services, and the control of their execution
 * 
 * @author Rafael Ramos
 */
public class OwlsRunner {
    
    RunnerGUI gui;
    JenaOWLModel model;
    Hashtable<String,OwlsService> services;
    GraphNode selectedNode;
    Vector<String> enabledNodes;
    JessRuler ruler;
    Vector<edu.stanford.smi.protegex.owl.model.OWLIndividual> nonEditableXSets;
    String dotPath;
    
    /**
     * Constructor. Initializes variables and shows the GUI.
     */
    public OwlsRunner() {
        //initialize variables        
        try {
            model = ProtegeOWL.createJenaOWLModel();
        }   
        catch(Exception ex) {
            System.err.println(ex.getMessage());
        }
        gui = new RunnerGUI(this);        
        services = new Hashtable<String,OwlsService>();
        enabledNodes = new Vector<String>();
        ruler = new JessRuler(this);
        nonEditableXSets = new Vector<edu.stanford.smi.protegex.owl.model.OWLIndividual>();
        createParameterClassAndProperty();
        loadDotPath();
        
        //run the gui        
        gui.setVisible(true);
        
        System.out.println("OWL-S Runner is ready");
    }
    
    /**
     * Creates an OwlsService, imports the ontologies and initializes its GUI elements.
     * @param owlsUri
     * @param prefix
     * @return true if successful
     */
    public boolean openService(String owlsUri, String prefix) {
        //the prefix must be unique
        if(services.containsKey(prefix)) {
            return false;
        }
        System.out.println("Opening " + prefix + " service...");
        
        //create a new OwlsService and add it to the table        
        OwlsService service = new OwlsService(owlsUri,prefix,this);
        services.put(prefix,service);
                
        //import the ontology elements of the OWL-S into the model, with the given prefix
        try { 
            NamespaceManager nm = model.getNamespaceManager();
            nm.setPrefix(owlsUri + "#",prefix);
            ImportHelper ih = new ImportHelper(model);
            ih.addImport(URI.create(owlsUri));
            ih.importOntologies();
            
            //add an OWLIndividual for the RootPerform with the main process
            OWLNamedClass performClass = model.getOWLNamedClass("process:Perform");
            edu.stanford.smi.protegex.owl.model.OWLObjectProperty processProperty = model.getOWLObjectProperty("process:process");
            edu.stanford.smi.protegex.owl.model.OWLIndividual rootPerformIndividual = performClass.createOWLIndividual(prefix + ":RootPerform");
            rootPerformIndividual.addPropertyValue(processProperty,OwlUtils.getIndividualM2P(model,service.serviceM.getProcess()));

            //update the editable state to all XSets
            TripleStoreModel tsm = model.getTripleStoreModel();
            tsm.updateEditableResourceState();
            updateNonEditableXSets();            
        }
        catch(Exception ex){
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
        
        //create the Input Sets, Output Sets and Local Sets for this service in the model
        createIOLSets(prefix);

        //create the GUI elements for the new service
        gui.createServiceMenuItems(prefix);        
        gui.createServicePanel(prefix,service.graph);
        gui.updateOntologiesTabbedPane();
        gui.createInfoPanels(service.graph);        
        
        //enable the start node
        service.start.enable(null);
        
        gui.selectNode(prefix + ":RootPerform");
        
        System.out.println(prefix + " service is ready");
        
        return true;                
    }
    
    /**
     * Resets the OwlsService, leaving it ready for a new execution.
     * @param serviceName
     */
    public void resetService(String serviceName) {
        System.out.println("Resetting " + serviceName + "...");
        
        //reset all nodes and enable the first
        OwlsService service = services.get(serviceName);
        service.reset();        
                
        service.start.enable(null);
        gui.updateExecuteButtons();
    }
    
    /**
     * Removes an OwlsService and all the data belonging to the service.
     * @param serviceName
     */
    public void closeService(String serviceName) {
        System.out.println("Closing " + serviceName + "..."); 
        String serviceUri = services.get(serviceName).uri;
        
        //remove enabled nodes of this service        
        Object[] en = enabledNodes.toArray();
        for(int i = 0; i < en.length; i++) {
            String nodeName = (String) en[i];
            if(OwlUtils.prefixOfNode(nodeName).equals(serviceName)) {
                enabledNodes.remove(nodeName);
            }
        }
        
        //remove non-editable xSets of this service
        Object[] nexs = nonEditableXSets.toArray();
        for(int i = 0; i < nexs.length; i++) {
            edu.stanford.smi.protegex.owl.model.OWLIndividual xSet = (edu.stanford.smi.protegex.owl.model.OWLIndividual) nexs[i];
            if(OwlUtils.prefixOfNode((String) xSet.getLabels().toArray()[0]).equals(serviceName)) {
                nonEditableXSets.remove(xSet);
            }
        }
        
        //remove the OwlsService object
        services.remove(serviceName);        
        
        //remove the InputSet/OutputSet/LocalSet classes and instances of this service
        Iterator xSetsIterator = model.getOWLNamedClass("ParameterSet").getSubclasses(false).iterator();
        while(xSetsIterator.hasNext()) {
            OWLNamedClass xSetClass = (OWLNamedClass) xSetsIterator.next();
            if(OwlUtils.prefixOfNode((String) xSetClass.getLabels().toArray()[0]).equals(serviceName)) {
                Iterator xSetInstancesIterator = xSetClass.getInstances(false).iterator();
                while(xSetInstancesIterator.hasNext()) {
                    edu.stanford.smi.protegex.owl.model.OWLIndividual xSet = (edu.stanford.smi.protegex.owl.model.OWLIndividual) xSetInstancesIterator.next();
                    xSet.delete();
                }
                xSetClass.delete();
            }
        }
        
        //remove the input/output/local properties of the InputSets/OutputSets/LocalSets
        Iterator parametersIterator = model.getRDFProperty("parameter").getSubproperties(false).iterator();
        while(parametersIterator.hasNext()) {
            RDFProperty property = (RDFProperty) parametersIterator.next();
            if(OwlUtils.prefixOfNode((String) property.getLabels().toArray()[0]).equals(serviceName)) {
                property.delete();
            }
        }
        
        //remove the RootPerform individual
        model.getOWLIndividual(serviceName + ":RootPerform").delete();
                        
        //remove all clases and instances belonging to this service; do it in their TripleStore
        TripleStoreModel tsm = model.getTripleStoreModel();
        NamespaceManager nm = model.getNamespaceManager();
        tsm.setActiveTripleStore(tsm.getTripleStoreByDefaultNamespace(nm.getNamespaceForPrefix(serviceName)));
        Iterator resourceIterator = model.getResourcesWithPrefix(serviceName).iterator();
        while(resourceIterator.hasNext()) {
            RDFResource resource = (RDFResource) resourceIterator.next();
            if(!resource.hasRDFType(model.getOWLOntologyClass())) {
                resource.delete();
            }
        }
        tsm.setActiveTripleStore(tsm.getTopTripleStore());
                
        //remove the imported service ontology
        model.getDefaultOWLOntology().removeImports(serviceUri);
        
        selectedNode = null;
        
        //remove all GUI elements belonging to this service
        gui.closeServiceGUI(serviceName);        
    }    
    
    /**
     * Sets the node with the given name as selected.
     * @param nodeName
     * @return true if successful
     */
    public boolean setSelectedNode(String nodeName) {
        OwlsService selectedService = services.get(OwlUtils.prefixOfNode(nodeName));
        if(selectedService != null) {
            GraphNode node = selectedService.graph.get(nodeName);
            if(node != null) {
                selectedNode = node;
                return true;
            }            
        }
        return false;
    }
    
    public GraphNode getSelectedNode() {
        return selectedNode;
    }
    
    public boolean isSelectedNodeEnabled() {
        if(selectedNode != null) {
            return selectedNode.getEnabled();
        }
        else {
            return false;
        }
    }
    
    /**
     * Updates data in OwlsRunner, OwlsGUI and JessRuler when a node is enabled.
     * Also applies perform bindings.
     * @param node
     */
    public void addEnabledNode(GraphNode node) {
        //if it's a Perform, apply its bindings
        if(node instanceof PerfGraphNode) {
            PerfGraphNode perfNode = (PerfGraphNode) node;
            OwlsService service = serviceOfNode(node);
            
            ValueMap values = service.applyPerformBindings(perfNode);
            if(values.size() > 0) {
                service.storePerformValues(perfNode,values);
                storeInputs(perfNode,values);
                storeOutputs(perfNode,values);
            }
            
            //set its InputSet editable, if it's not (e.g. in the 2nd iteration of a loop)
            setXSetEditable(perfNode.getPerformInputSet(),true);            
        }        
        
        ruler.assertEnabledNodeFact(node);
        
        String nodeName = node.getName();
        enabledNodes.add(nodeName);
        gui.setNodeEnabledState(nodeName,true);
    }
    
    /**
     * Updates data in OwlsRunner, OwlsGUI and JessRuler when a node is disabled.
     * @param node
     */
    public void removeEnabledNode(GraphNode node) {
        ruler.retractEnabledNodeFact(node);
        String nodeName = node.getName();
        enabledNodes.remove(nodeName);
        gui.setNodeEnabledState(nodeName,false);
    }
    
    /**
     * Executes a node according to its type, and updates the graph.
     * If 'auto' is true, the Jess engine will be run at the end.
     * @param node
     * @param auto
     * @return true if successful
     */
    public boolean executeNode(GraphNode node, boolean auto) {
        if(!node.getEnabled()) {
            System.err.println("Node " + node.getName() + " not enabled");
            return false;
        }
        System.out.println((auto?"Auto":"") + "Executing node " + node.getName() + "...");
        node.setStop(false);
        
        OwlsService service = serviceOfNode(node);        
        
        //execute the node according to its type
        if(node instanceof CompPerfGraphNode) {
            CompPerfGraphNode compositePerformNode = (CompPerfGraphNode) node;
            
            setXSetEditable(compositePerformNode.getPerformInputSet(),false);
            compositePerformNode.disable();
            
            //make a valueMap from the InputSet
            ValueMap inputValueMap = loadInputs(compositePerformNode);                 
            if(inputValueMap == null) {
                setXSetEditable(compositePerformNode.getPerformInputSet(),true);
                compositePerformNode.enable(null);
                if(auto) {
                    node.setStop(true);
                }
                return false;                
            }                        
                       
            try {
                service.checkPreconditions(compositePerformNode,inputValueMap);                        
                service.storePerformValues(compositePerformNode,inputValueMap);
                storeLocals(compositePerformNode,inputValueMap);
                //enable the first node in the subprocess
                compositePerformNode.start.enable(compositePerformNode.getName());     
            }
            catch(Exception e) {
                System.err.println("Composite Process execution failed");
                System.err.println(e.getMessage());
                
                //enable the node to try again
                setXSetEditable(compositePerformNode.getPerformInputSet(),true);
                compositePerformNode.enable(null);
                if(auto) {
                    node.setStop(true);
                }
                return false;
            }            
        }
        else if(node instanceof PerfGraphNode) {
            PerfGraphNode performNode = (PerfGraphNode) node;
            
            setXSetEditable(performNode.getPerformInputSet(),false);
            performNode.disable();
            
            //make a valueMap from the InputSet
            ValueMap inputValueMap = loadInputs(performNode);                 
            if(inputValueMap == null) {
                setXSetEditable(performNode.getPerformInputSet(),true);
                performNode.enable(null);
                if(auto) {
                    node.setStop(true);
                }
                return false;                
            }            
           
            service.storePerformValues(performNode,inputValueMap);
            
            //launch the execution of the process in a new thread
            new Thread(new ProcesExecutionLauncher(performNode,inputValueMap,auto)).start();
        }
        else if(node instanceof ConditionalGraphNode) {
            ConditionalGraphNode conditionalNode = (ConditionalGraphNode) node;
            
            conditionalNode.disable();
            
            //set the conditionValue of the node to true or false, which determines which path to take in the advance()
            boolean conditionValue = service.checkCondition(conditionalNode);
            System.out.println("Condition is " + conditionValue);
            
            Vector<GraphNode> newNodes = new Vector<GraphNode>();
            boolean finished = conditionalNode.advance(newNodes);     
                  
            //if this was the last node of a composite process, generate its outputs and advance in the upper process
            if(finished) {
                finishCompositeProcess(conditionalNode.getParentPerform());
            }            
        }
        else if(node instanceof ProdGraphNode) {
            ProdGraphNode produceNode = (ProdGraphNode) node;
            
            produceNode.disable();
            
            //apply the bindings in the Produce
            ValueMap values = service.applyProduceBindings(produceNode);
            service.storePerformValues(produceNode.getParentPerform(),values);
            storeOutputs(produceNode.getParentPerform(),values);
            
            Vector<GraphNode> newNodes = new Vector<GraphNode>();
            boolean finished = produceNode.advance(newNodes);     
                        
            //if this was the last node of a composite process, generate its outputs and advance in the upper process
            if(finished) {
                finishCompositeProcess(produceNode.getParentPerform());
            }
        }  
        else {            
            //just advance in the graph
            node.disable();
            
            Vector<GraphNode> newNodes = new Vector<GraphNode>();
            boolean finished = node.advance(newNodes);     
                             
            //if this was the last node of a composite process, generate its outputs and advance in the upper process
            if(finished) {
                finishCompositeProcess(node.getParentPerform());
            }
        }
            
        
        if(auto) {
            //fire user rules with the new facts (data returned and new enabled nodes)
            ruler.run();
        }       
                    
        return true;
    }
        
    /**
     * A Runnable class used to invoke processes to the Web Service.
     */
    class ProcesExecutionLauncher implements Runnable {
        
        PerfGraphNode node;
        ValueMap inputValueMap;
        OwlsService service;
        boolean auto;        
        
        public ProcesExecutionLauncher(PerfGraphNode theNode, ValueMap theInputValueMap, boolean theAuto) {
            node = theNode;
            inputValueMap = theInputValueMap;
            service = serviceOfNode(node);
            auto = theAuto;
        }
        
        /**
         * Invokes the process and calls the ProcessExecutionFinisher.
         * This method is run in a separate thread.
         */
        public void run() {
            //this is executed in a new thread
            System.out.println("launching");
                        
            
            //invocation to the web service
            ValueMap outputValueMap = null;
            try {
                //this is the blocking call
                outputValueMap = service.executeProcess(node,inputValueMap);
                
                //schedule the finisher to be executed in the main thread
                java.awt.EventQueue.invokeLater(new ProcessExecutionFinisher(node,outputValueMap,null,auto));
            }
            catch(Exception e) {
                //manage the exception in the main thread
                java.awt.EventQueue.invokeLater(new ProcessExecutionFinisher(node,null,e,auto));                
            }         
        }
    }
    
    /**
     * A Runnable class used to manage the response of the Web Service
     */
    class ProcessExecutionFinisher implements Runnable {
        
        PerfGraphNode node;
        ValueMap outputValueMap;
        OwlsService service;
        Exception ex;
        boolean auto;
        
        public ProcessExecutionFinisher(PerfGraphNode theNode, ValueMap theOutputValueMap, Exception theException, boolean theAuto) {
            node = theNode;
            outputValueMap = theOutputValueMap;
            service = serviceOfNode(node);
            ex = theException;
            auto = theAuto;
        }
        
        /**
         * Stores the outputs and updates the graph.
         * This method is run in the main thread.
         */
        public void run() {
            //this is executed in the main thread
            System.out.println("finishing");
       
            if(ex != null) {
                System.err.println("Process execution failed");
                System.err.println(ex.getMessage());
                
                //enable the node to try again
                setXSetEditable(node.getPerformInputSet(),true);
                node.enable(null);
                if(auto) {
                    node.setStop(true);
                }
            }
            else {            
                //store the returned outputs
                if(outputValueMap != null) {
                    service.storePerformValues(node,outputValueMap);
                    storeOutputs(node,outputValueMap);          
                    storeLocals(node,outputValueMap);
                }
                
                Vector<GraphNode> newNodes = new Vector<GraphNode>();
                boolean finished = node.advance(newNodes);     
                                
                //if this was the last node of a composite process, generate its outputs and advance in the upper process
                if(finished) {
                    //if this Perform is RootPerform, finish the service
                    if(node == service.start) {
                        gui.showTerminationMessage(OwlUtils.prefixOfNode(node.getName()));
                        return;
                    }
                    else {
                        finishCompositeProcess(node.getParentPerform());
                    }
                }                
                
                if(auto) {
                    //fire user rules with the new facts (data returned and new enabled nodes)
                    ruler.run();
                }
            }
             
            gui.updateExecuteButtons();
        }
    }
    
    /**
     * Applies the Results of the node and updates the graph.
     * @param node
     */
    void finishCompositeProcess(CompPerfGraphNode node) {
        System.out.println("Finishing composite perform " + node.getName());
        
        //generate the outputs from the Results and store them in the OutputSet
        ValueMap outputValueMap = applyResults(node);        
        storeOutputs(node,outputValueMap);

        //if this is the Root Perform, the service is finished
        if(OwlUtils.localNameOfNode(node.getName()).equals("RootPerform")) {
            gui.showTerminationMessage(OwlUtils.prefixOfNode(node.getName()));
            return;
        }
        
        Vector<GraphNode> newNodes = new Vector<GraphNode>();
        boolean finished = node.advance(newNodes);     
       
        //if this was the last node of a composite process, generate its outputs and advance in the upper process
        if(finished) {
            finishCompositeProcess(node.getParentPerform());
        }
    }    
     
    /**
     * Executes a node in automatic mode.
     * @param node
     */
    public void autoExecuteNode(GraphNode node) {
        //this method is called from the AutoExecute button of the GUI
        
        executeNode(node,true);        
    }
        
    /**
     * Schedules a node to be executed in automatic mode.
     * @param nodeName
     */
    public void scheduleNodeAutoExecution(String nodeName) {
        //this method is called from the (execute ?) Jess function
        
        OwlsService service = services.get(OwlUtils.prefixOfNode(nodeName));
        if(service != null) {
            GraphNode node = service.graph.get(nodeName);
            if(node != null && !node.getStop()) {                    
                //System.out.println("Scheduling autoExecution of node " + nodeName);
                
                //schedule the node to be executed when Jess reasoning ends
                java.awt.EventQueue.invokeLater(new NodeAutoExecutionLauncher(node));
            }
        }
    }
        
    /**
     * A Runnable class used to start executions of nodes from the EventQueue
     */
    class NodeAutoExecutionLauncher implements Runnable {
        
        GraphNode node;
        
        public NodeAutoExecutionLauncher(GraphNode theNode) {
            node = theNode;
        }        
        
        /**
         * Executes a node in automatic mode.
         * This method is run in the main thread.
         */
        public void run() {
            executeNode(node,true);
        }
        
    }
    
    void createParameterClassAndProperty() {       
        //the ParameterSet class, a superclass for InputSet, OutputSet and LocalSet classes
        OWLNamedClass parameterSetClass = model.createOWLNamedClass("ParameterSet");
        
        //the "parameter" property, a superproperty for all properties in the ParameterSets
        edu.stanford.smi.protegex.owl.model.RDFProperty parameterProperty = model.createRDFProperty("parameter");
    }
    
    /**
     * Creates InputSets, OutputSets and LocalSets for a service.
     * @param prefix
     * @return true if successful
     */
    boolean createIOLSets(String prefix) {
        //take the required classes and properties
        OWLNamedClass performClass = model.getOWLNamedClass("process:Perform");
        edu.stanford.smi.protegex.owl.model.OWLObjectProperty processProperty = model.getOWLObjectProperty("process:process");
        edu.stanford.smi.protegex.owl.model.OWLObjectProperty hasInputProperty = model.getOWLObjectProperty("process:hasInput");        
        edu.stanford.smi.protegex.owl.model.OWLObjectProperty hasOutputProperty = model.getOWLObjectProperty("process:hasOutput");        
        edu.stanford.smi.protegex.owl.model.OWLObjectProperty hasLocalProperty = model.getOWLObjectProperty("process:hasLocal");        
        edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty parameterTypeProperty = model.getOWLDatatypeProperty("process:parameterType");           
        OWLNamedClass parameterSetClass = model.getOWLNamedClass("ParameterSet");
        edu.stanford.smi.protegex.owl.model.RDFProperty parameterProperty = model.getRDFProperty("parameter");
        if(performClass == null || processProperty == null || hasInputProperty == null || hasOutputProperty == null || hasLocalProperty == null || parameterTypeProperty == null || parameterSetClass == null || parameterProperty == null) {
            System.err.println("Missing OWL entities");
            return false;
        }
                
        //iterate through the Performs of the service with that prefix
        Iterator performIterator = performClass.getInstances(false).iterator();
        while(performIterator.hasNext()) {
            edu.stanford.smi.protegex.owl.model.OWLIndividual perf = (edu.stanford.smi.protegex.owl.model.OWLIndividual) performIterator.next();
            
            if(perf.getNamespacePrefix().equals(prefix)) {
                //take its graph node
                PerfGraphNode perfNode = (PerfGraphNode) services.get(prefix).graph.get(perf.getName());
                if(perfNode == null) {
                    System.err.println("Missing perform node: " + perf.getName());
                    continue;
                }
                                
                //take its process
                edu.stanford.smi.protegex.owl.model.OWLIndividual proc = (edu.stanford.smi.protegex.owl.model.OWLIndividual) perf.getPropertyValue(processProperty);
                if(proc == null) {
                    System.err.println("Perform without process: " + perf.getName());
                    continue;
                }
                
                
                //INPUT SET                
                
                //create the InputSet class for this process; if it already exists (i.e. another perform with the same process has created it), then use it
                String inputSetClassName = prefix + proc.getLocalName() + "InputSet";
                OWLNamedClass inputSetClass = model.getOWLNamedClass(inputSetClassName);
                if(inputSetClass == null) {
                    inputSetClass = model.createOWLNamedSubclass(inputSetClassName,parameterSetClass);
                    
                    //add it one property for each Input of the process, of its correct type
                    Iterator inputIterator = proc.getPropertyValues(hasInputProperty).iterator();
                    while(inputIterator.hasNext()) {
                        edu.stanford.smi.protegex.owl.model.OWLIndividual input = (edu.stanford.smi.protegex.owl.model.OWLIndividual) inputIterator.next();
                        String inputName = input.getLocalName();
                        String propertyName = prefix + inputName;
                        
                        //if the property already exists, use it; else create it
                        edu.stanford.smi.protegex.owl.model.OWLProperty property = model.getOWLProperty(propertyName);
                        if(property != null) {
                            property.addUnionDomainClass(inputSetClass);
                        }
                        else {                                                
                            RDFSLiteral parameterTypeValue = (RDFSLiteral) input.getPropertyValue(parameterTypeProperty);
                            String parameterTypeString = model.getResourceNameForURI(parameterTypeValue.getString());

                            //if the parameterType is an existing class in the model, make an object property; else, make a datatype property
                            OWLNamedClass parameterTypeClass = model.getOWLNamedClass(parameterTypeString);
                            if(parameterTypeClass != null) {
                                property = model.createOWLObjectProperty(propertyName);
                                property.setRange(parameterTypeClass);
                            }
                            else {
                                property = model.createOWLDatatypeProperty(propertyName);
                                property.setRange(model.getRDFResource(parameterTypeString));
                            }
                            property.setDomain(inputSetClass);
                            property.setFunctional(true);
                            property.addLabel(inputName,null);    
                            property.addSuperproperty(parameterProperty);
                        }                      
                    }                    
                    inputSetClass.addLabel(proc.getName(),null);
                }                
                
                //create an individual of the InputSet class for this perform
                String inputSetInstanceName = prefix + perf.getLocalName() + "InputSet";
                edu.stanford.smi.protegex.owl.model.OWLIndividual inputSetInstance = inputSetClass.createOWLIndividual(inputSetInstanceName);
                inputSetInstance.addLabel(perf.getName(),null);
                inputSetInstance.setComment("InputSet");
                                
                //add a reference to it in the perform graph node                
                perfNode.setPerformInputSet(inputSetInstance);

                
                //OUTPUT SET
                
                //create the OutputSet class for this process; if it already exists (i.e. another perform with the same process has created it), then use it
                String outputSetClassName = prefix + proc.getLocalName() + "OutputSet";
                OWLNamedClass outputSetClass = model.getOWLNamedClass(outputSetClassName);
                if(outputSetClass == null) {
                    outputSetClass = model.createOWLNamedSubclass(outputSetClassName,parameterSetClass);
                    
                    //add it one property for each Output of the process, of its correct type
                    Iterator outputIterator = proc.getPropertyValues(hasOutputProperty).iterator();
                    while(outputIterator.hasNext()) {
                        edu.stanford.smi.protegex.owl.model.OWLIndividual output = (edu.stanford.smi.protegex.owl.model.OWLIndividual) outputIterator.next();
                        String outputName = output.getLocalName();
                        String propertyName = prefix + outputName;
                        
                        //if the property already exists, use it; else create it
                        edu.stanford.smi.protegex.owl.model.OWLProperty property = model.getOWLProperty(propertyName);
                        if(property != null) {
                            property.addUnionDomainClass(outputSetClass);
                        }
                        else {   
                            RDFSLiteral parameterTypeValue = (RDFSLiteral) output.getPropertyValue(parameterTypeProperty);
                            String parameterTypeString = model.getResourceNameForURI(parameterTypeValue.getString());

                            //if the parameterType is an existing class in the model, make an object property; else, make a datatype property
                            OWLNamedClass parameterTypeClass = model.getOWLNamedClass(parameterTypeString);
                            if(parameterTypeClass != null) {
                                property = model.createOWLObjectProperty(propertyName);
                                property.setRange(parameterTypeClass);
                            }
                            else {
                                property = model.createOWLDatatypeProperty(propertyName);
                                property.setRange(model.getRDFResource(parameterTypeString));                  
                            }
                            property.setDomain(outputSetClass);
                            property.setFunctional(true);
                            property.addLabel(outputName,null);
                            property.addSuperproperty(parameterProperty);
                        }                      
                    }
                    outputSetClass.addLabel(proc.getName(),null);
                }                
                
                //create an individual of the OutputSet class for this perform, and make it non-editable
                String outputSetInstanceName = prefix + perf.getLocalName() + "OutputSet";
                edu.stanford.smi.protegex.owl.model.OWLIndividual outputSetInstance = outputSetClass.createOWLIndividual(outputSetInstanceName);
                outputSetInstance.addLabel(perf.getName(),null);
                outputSetInstance.setComment("OutputSet");                
                setXSetEditable(outputSetInstance,false);
                
                //add a reference to it in the perform graph node
                perfNode.setPerformOutputSet(outputSetInstance);               

                
                //LOCAL SET
                
                //create the LocalSet class for this process; if it already exists (i.e. another perform with the same process has created it), then use it
                String localSetClassName = prefix + proc.getLocalName() + "LocalSet";
                OWLNamedClass localSetClass = model.getOWLNamedClass(localSetClassName);
                if(localSetClass == null) {
                    localSetClass = model.createOWLNamedSubclass(localSetClassName,parameterSetClass);
                    
                    //add it one property for each Local of the process, of its correct type
                    Iterator localIterator = proc.getPropertyValues(hasLocalProperty).iterator();
                    while(localIterator.hasNext()) {
                        edu.stanford.smi.protegex.owl.model.OWLIndividual local = (edu.stanford.smi.protegex.owl.model.OWLIndividual) localIterator.next();
                        String localName = local.getLocalName();
                        String propertyName = prefix + localName;
                        
                        //if the property already exists, use it; else create it
                        edu.stanford.smi.protegex.owl.model.OWLProperty property = model.getOWLProperty(propertyName);
                        if(property != null) {
                            property.addUnionDomainClass(localSetClass);
                        }
                        else {   
                            RDFSLiteral parameterTypeValue = (RDFSLiteral) local.getPropertyValue(parameterTypeProperty);
                            String parameterTypeString = model.getResourceNameForURI(parameterTypeValue.getString());

                            //if the parameterType is an existing class in the model, make an object property; else, make a datatype property
                            OWLNamedClass parameterTypeClass = model.getOWLNamedClass(parameterTypeString);
                            if(parameterTypeClass != null) {
                                property = model.createOWLObjectProperty(propertyName);
                                property.setRange(parameterTypeClass);
                            }
                            else {
                                property = model.createOWLDatatypeProperty(propertyName);
                                property.setRange(model.getRDFResource(parameterTypeString));                  
                            }
                            property.setDomain(localSetClass);
                            property.setFunctional(true);
                            property.addLabel(localName,null);
                            property.addSuperproperty(parameterProperty);
                        }                      
                    }
                    localSetClass.addLabel(proc.getName(),null);
                }                
                
                //create an individual of the LocalSet class for this perform, and make it non-editable
                String localSetInstanceName = prefix + perf.getLocalName() + "LocalSet";
                edu.stanford.smi.protegex.owl.model.OWLIndividual localSetInstance = localSetClass.createOWLIndividual(localSetInstanceName);
                localSetInstance.addLabel(perf.getName(),null);
                localSetInstance.setComment("LocalSet");
                setXSetEditable(localSetInstance,false);
                
                //add a reference to it in the perform graph node
                perfNode.setPerformLocalSet(localSetInstance);               
            }            
        }            

        
        //map all InputSets, OutputSets and LocalSets as facts in the rule engine
        ruler.mapClass(parameterSetClass);
      
        return true;
    }
    
    /**
     * Removes the values of the properties of a ParameterSet.
     * @param parameterSet
     */
    public void emptyParameterSet(edu.stanford.smi.protegex.owl.model.OWLIndividual parameterSet) {
        edu.stanford.smi.protegex.owl.model.RDFProperty typeProperty = model.getRDFTypeProperty();
        edu.stanford.smi.protegex.owl.model.RDFProperty labelProperty = model.getRDFSLabelProperty();
        edu.stanford.smi.protegex.owl.model.RDFProperty commentProperty = model.getRDFSCommentProperty();
        
        //iterate through the properties to remove its values (keep type, label and comment)
        Iterator propertyIterator = parameterSet.getRDFProperties().iterator();
        while(propertyIterator.hasNext()) {
            edu.stanford.smi.protegex.owl.model.RDFProperty prop = (edu.stanford.smi.protegex.owl.model.RDFProperty) propertyIterator.next();
            if(!prop.equals(typeProperty) && !prop.equals(labelProperty) && !prop.equals(commentProperty)) {
                Object value = parameterSet.getPropertyValue(prop);
                if(value != null) {
                    parameterSet.removePropertyValue(prop,value);
                }
            }
        }
    }
    
    /**
     * Marks a ParameterSet as editable or non-editable.
     * @param xSet
     * @param editable
     */
    public void setXSetEditable(edu.stanford.smi.protegex.owl.model.OWLIndividual xSet, boolean editable) {
        if(xSet.isEditable() != editable) {
            xSet.setEditable(editable);
            if(editable) {
                nonEditableXSets.remove(xSet);
            }
            else {
                nonEditableXSets.add(xSet);
            }
            gui.reloadXSetDisplay(xSet);
        }
    }
     
    /**
     * Restores the editable state of the ParameterSets, according to the nonEditableXSets list
     */
    void updateNonEditableXSets() {
        for(Iterator it = nonEditableXSets.iterator(); it.hasNext();) {
            edu.stanford.smi.protegex.owl.model.OWLIndividual xSet = (edu.stanford.smi.protegex.owl.model.OWLIndividual) it.next();
            xSet.setEditable(false);
            gui.reloadXSetDisplay(xSet);                    
        }
    } 
    
    /**
     * Gets the values in the InputSet of a given node, and makes a ValueMap.
     * @param node
     * @return the ValueMap
     */
    ValueMap loadInputs(PerfGraphNode node) {
        ValueMap inputValueMap = new ValueMap();
        OwlsService service = services.get(OwlUtils.prefixOfNode(node.getName()));
        OWLKnowledgeBase kb = service.kb;        //the M (Mindswap) service kb
        org.mindswap.owls.process.Process process = ((Perform) node.getCC()).getProcess();      //the M process
        edu.stanford.smi.protegex.owl.model.OWLIndividual inputSet = node.getPerformInputSet();     //the P (Protege) input set
        
        //iterate through the properties of the input set to get their values and put them into the value map
        Iterator propertyIterator = inputSet.getPossibleRDFProperties().iterator();
        while(propertyIterator.hasNext()) {
            edu.stanford.smi.protegex.owl.model.RDFProperty prop = (edu.stanford.smi.protegex.owl.model.RDFProperty) propertyIterator.next();
            
            //the label tells the input which the property corresponds to
            if(prop.getLabels().size() == 1) {
                String inputName = prop.getLabels().toArray()[0].toString();
                Input input = process.getInput(inputName);
                if(input != null) {
                    
                    //take the value and convert it to the Mindswap format
                    Object value = inputSet.getPropertyValue(prop);
                    if(value != null) {
                        if(value instanceof edu.stanford.smi.protegex.owl.model.OWLIndividual) {
                            edu.stanford.smi.protegex.owl.model.OWLIndividual individualP = (edu.stanford.smi.protegex.owl.model.OWLIndividual) value;                            
                            org.mindswap.owl.OWLIndividual individualM = OwlUtils.createOrGetIndividualP2M(kb,individualP);                            
                            
                            inputValueMap.setValue(input,individualM);
                        }
                        else {
                            inputValueMap.setValue(input,EntityFactory.createDataValue(value,input.getParamType().getURI()));
                        }                        
                    }  
                    else {
                        //all inputs must be set
                        System.err.println("Missing values for input " + inputName);
                        return null;
                    }
                }
            }            
        }
        
        return inputValueMap;
    }
      
    /**
     * Sets the values of the InputSet of a node from the given ValueMap.
     * @param node
     * @param inputValueMap
     */
    void storeInputs(PerfGraphNode node, ValueMap inputValueMap) {
        if(inputValueMap == null) {
            return;
        }
        
        OwlsService service = serviceOfNode(node);
        OWLKnowledgeBase kb = service.kb;        //the M (Mindswap) service kb
        org.mindswap.owls.process.Process process = ((Perform) node.getCC()).getProcess();      //the M process
        edu.stanford.smi.protegex.owl.model.OWLIndividual inputSet = node.getPerformInputSet();     //the P (Protege) input set
        
        //iterate through the properties of the input set to fill their values with those of the input value map
        Iterator propertyIterator = inputSet.getPossibleRDFProperties().iterator();
        while(propertyIterator.hasNext()) {
            edu.stanford.smi.protegex.owl.model.RDFProperty prop = (edu.stanford.smi.protegex.owl.model.RDFProperty) propertyIterator.next();
            
            //the label tells the input which the property corresponds to
            if(prop.getLabels().size() == 1) {
                String inputName = prop.getLabels().toArray()[0].toString();
                Input input = process.getInput(inputName);
                
                if(input != null) {
                    OWLValue valueM = inputValueMap.getValue(input);
                    
                    if(valueM != null) {
                        if(prop instanceof edu.stanford.smi.protegex.owl.model.OWLObjectProperty) {
                            if(valueM instanceof org.mindswap.owl.OWLIndividual) {                        
                                org.mindswap.owl.OWLIndividual individualM = (org.mindswap.owl.OWLIndividual) valueM;
                                edu.stanford.smi.protegex.owl.model.OWLIndividual individualP = OwlUtils.createOrGetIndividualM2P(model,individualM,false);
                                inputSet.setPropertyValue(prop,individualP);
                            }
                            else {
                                //if the value is not an object, clear the property                                
                                inputSet.setPropertyValue(prop,null);
                            }
                        }
                        else if(prop instanceof edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty) {
                            if(valueM instanceof OWLDataValue) {
                                String literalString = ((org.mindswap.owl.OWLDataValue) valueM).getLexicalValue();
                                RDFSLiteral literalP = model.createRDFSLiteral(literalString,prop.getRangeDatatype());
                                inputSet.setPropertyValue(prop,literalP);
                            }
                            else {
                                //if the value is not datatype, clear the property                                
                                inputSet.setPropertyValue(prop,null);
                            }
                        }
                    }
                } 
            }
        }                
    }    
    
    /**
     * Sets the values of the OutputSet of a node from the given ValueMap.
     * @param node
     * @param outputValueMap
     */
    void storeOutputs(PerfGraphNode node, ValueMap outputValueMap) {
        if(outputValueMap == null) {
            return;
        }
        
        OwlsService service = serviceOfNode(node);
        OWLKnowledgeBase kb = service.kb;        //the M (Mindswap) service kb
        org.mindswap.owls.process.Process process = ((Perform) node.getCC()).getProcess();      //the M process
        edu.stanford.smi.protegex.owl.model.OWLIndividual outputSet = node.getPerformOutputSet();     //the P (Protege) output set
        
        //iterate through the properties of the output set to fill their values with those of the output value map
        Iterator propertyIterator = outputSet.getPossibleRDFProperties().iterator();
        while(propertyIterator.hasNext()) {
            edu.stanford.smi.protegex.owl.model.RDFProperty prop = (edu.stanford.smi.protegex.owl.model.RDFProperty) propertyIterator.next();
            
            //the label tells the output which the property corresponds to
            if(prop.getLabels().size() == 1) {
                String outputName = prop.getLabels().toArray()[0].toString();
                Output output = process.getOutput(outputName);
                
                if(output != null) {
                    OWLValue valueM = outputValueMap.getValue(output);
                                        
                    if(valueM != null) {
                        if(prop instanceof edu.stanford.smi.protegex.owl.model.OWLObjectProperty) {
                            if(valueM instanceof org.mindswap.owl.OWLIndividual) {
                                org.mindswap.owl.OWLIndividual individualM = (org.mindswap.owl.OWLIndividual) valueM;
                                edu.stanford.smi.protegex.owl.model.OWLIndividual individualP = OwlUtils.createOrGetIndividualM2P(model,individualM,false);
                                outputSet.setPropertyValue(prop,individualP);
                            }
                            else {
                                //if the value is not an object, clear the property                                
                                outputSet.setPropertyValue(prop,null);
                            }
                        }
                        else if(prop instanceof edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty) {
                            if(valueM instanceof OWLDataValue) {
                                String literalString = ((org.mindswap.owl.OWLDataValue) valueM).getLexicalValue();
                                RDFSLiteral literalP = model.createRDFSLiteral(literalString,prop.getRangeDatatype());
                                outputSet.setPropertyValue(prop,literalP); 
                            }
                            else {
                                //if the value is not datatype, clear the property                                
                                outputSet.setPropertyValue(prop,null);
                            }
                        }
                    }
                } 
            }
        }                
    }    
    
    /**
     * Sets the values of the LocalSet of a node from the given ValueMap.
     * @param node
     * @param localValueMap
     */
    void storeLocals(PerfGraphNode node, ValueMap localValueMap) {
        if(localValueMap == null) {
            return;
        }
        
        OwlsService service = serviceOfNode(node);
        OWLKnowledgeBase kb = service.kb;        //the M (Mindswap) service kb
        org.mindswap.owls.process.Process process = ((Perform) node.getCC()).getProcess();      //the M process
        edu.stanford.smi.protegex.owl.model.OWLIndividual localSet = node.getPerformLocalSet();     //the P (Protege) local set
        
        //iterate through the properties of the local set to fill their values with those of the local value map
        Iterator propertyIterator = localSet.getPossibleRDFProperties().iterator();
        while(propertyIterator.hasNext()) {
            edu.stanford.smi.protegex.owl.model.RDFProperty prop = (edu.stanford.smi.protegex.owl.model.RDFProperty) propertyIterator.next();
            
            //the label tells the local which the property corresponds to
            if(prop.getLabels().size() == 1) {
                String localName = prop.getLabels().toArray()[0].toString();
                Local local = (Local) process.getLocals().getParameter(localName);
                
                if(local != null) {
                    OWLValue valueM = localValueMap.getValue(local);
                    
                    if(valueM != null) {
                        if(prop instanceof edu.stanford.smi.protegex.owl.model.OWLObjectProperty) {
                            if(valueM instanceof org.mindswap.owl.OWLIndividual) {                         
                                org.mindswap.owl.OWLIndividual individualM = (org.mindswap.owl.OWLIndividual) valueM;
                                edu.stanford.smi.protegex.owl.model.OWLIndividual individualP = OwlUtils.createOrGetIndividualM2P(model,individualM,false);
                                localSet.setPropertyValue(prop,individualP);
                            }
                            else {
                                //if the value is not an object, clear the property                                
                                localSet.setPropertyValue(prop,null);
                            }
                        }
                        else if(prop instanceof edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty) {
                            if(valueM instanceof OWLDataValue) {
                                String literalString = ((org.mindswap.owl.OWLDataValue) valueM).getLexicalValue();
                                RDFSLiteral literalP = model.createRDFSLiteral(literalString,prop.getRangeDatatype());
                                localSet.setPropertyValue(prop,literalP); 
                            }
                            else {
                                //if the value is not datatype, clear the property                                
                                localSet.setPropertyValue(prop,null);
                            }
                        }
                    }
                } 
            }
        }                
    }
    
    /**
     * Chooses a suitable Result and applies its bindings.
     * @param node
     * @return the new values
     */
    ValueMap applyResults(CompPerfGraphNode node) {
        OwlsService service = serviceOfNode(node);
        ValueMap values = (ValueMap) service.performValues.get((Perform) node.getCC());    

        //find a Result whose conditions are true
        Result result = service.checkResultConditions(node);
        
        if(result != null) {
            //apply its bindings
            System.out.println("chosen result: " + result.getLocalName());
            ValueMap newValues = service.applyResultBindings(node,result);
            values.addMap(newValues);  
            service.storePerformValues(node,values);
        }
        
        return values;  
    }    
    
    public void printPerformValues() {
        if(selectedNode != null) {
            OwlsService selectedService = services.get(OwlUtils.prefixOfNode(selectedNode.getName()));
            if(selectedService != null) {
                selectedService.printPerformValues();
            }
        }                  
    }
    
    public void createJessConsolePanel(JConsolePanel jcp) {
        gui.createJessConsolePanel(jcp);
    }
    
    public void restartJess() {
        ruler.restart();
    }
    
    /**
     * Sets the attribute dotPath with the String read from the dotPath.txt file
     */
    void loadDotPath() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/dotPath.txt"));
            dotPath = reader.readLine();
            reader.close();
        }
        catch(IOException ex) {
            //ex.printStackTrace();
            System.err.println(ex.getMessage());
        }
    }
    
    /**
     * Sets the dotPath attribute to the given path, and stores it in the dotPath.txt file
     * @param path
     */
    public void setDotPath(String path) {
        dotPath = path;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("data/dotPath.txt"));
            writer.write(dotPath);
            writer.close();
        }
        catch(IOException ex) {
            //ex.printStackTrace();
            System.err.println(ex.getMessage());
        }
    }
    
    public String getDotPath() {
        return dotPath;
    }
    
    GraphNode getNodeByName(String nodeName) {
        OwlsService service = services.get(OwlUtils.prefixOfNode(nodeName));
        return (GraphNode) service.graph.get(nodeName);
    }
    
    public OwlsService serviceOfNode(GraphNode node) {
        return services.get(OwlUtils.prefixOfNode(node.getName()));
    }
       
    public JenaOWLModel getModel() {
        return model;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OwlsRunner();
            }
        });
    }
    
}
