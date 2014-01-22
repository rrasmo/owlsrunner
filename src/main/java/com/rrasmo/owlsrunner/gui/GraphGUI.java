package com.rrasmo.owlsrunner.gui;

import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.mindswap.owls.process.Perform;

import att.grappa.Element;
import att.grappa.Graph;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaBox;
import att.grappa.GrappaConstants;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.GrappaSupport;
import att.grappa.Node;
import att.grappa.Parser;
import att.grappa.Subgraph;

import com.rrasmo.owlsrunner.graph.AOEndGraphNode;
import com.rrasmo.owlsrunner.graph.AOStartGraphNode;
import com.rrasmo.owlsrunner.graph.ChoEndGraphNode;
import com.rrasmo.owlsrunner.graph.ChoStartGraphNode;
import com.rrasmo.owlsrunner.graph.CompPerfGraphNode;
import com.rrasmo.owlsrunner.graph.ConditionalGraphNode;
import com.rrasmo.owlsrunner.graph.GraphNode;
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
 *
 * @author Rafael Ramos
 */
public class GraphGUI {
    
    String serviceName;
    String fileName;
    Graph grappaGraph;
    Hashtable<String,GrappaPanel> grappaPanels;
    GrappaPanel currentGrappaPanel;
    RunnerGUI gui;
    Element currentSelection;    
    
    public GraphGUI(String theServiceName, Hashtable<String,GraphNode> theGraph, RunnerGUI theGui) {
        serviceName = theServiceName;
        fileName = serviceName + ".dot";
        gui = theGui;
        grappaGraph = null;
        grappaPanels = new Hashtable<String,GrappaPanel>();
        currentSelection = null;
        createDotFile(theGraph);
        createGrappaPanels();
        currentGrappaPanel = grappaPanels.get(serviceName + ":Root");
        deleteDotFile();
    }
    
    private void deleteDotFile() {
        boolean success = (new File(fileName)).delete();
        if (!success) {
            System.err.println("File deletion failed");
        }
    }
    
    private void createDotFile(Hashtable<String,GraphNode> graph) {
        PrintWriter pw = null;
        
        try {
            pw = new PrintWriter(new FileWriter(fileName));
            
            pw.println("digraph \"" + serviceName + "\" {");
            pw.println("node [shape=box, style=filled, color=lemonchiffon1];");
            pw.println("edge [color=lemonchiffon4];");

            //print root subgraph with the RootPerform
            String rootNodeName = serviceName + ":RootPerform";
            PerfGraphNode rootNode = (PerfGraphNode) graph.get(rootNodeName);
            pw.println("subgraph \"" + serviceName + ":Root\" {");
            printNodeToFile(rootNode,pw);
            pw.println("}");
            
            //get the list of CompositePerformGraphNodes            
            Vector<CompPerfGraphNode> compositeNodesList = new Vector<CompPerfGraphNode>();
            Iterator nodeIterator = graph.values().iterator();
            while(nodeIterator.hasNext()) {
                GraphNode node = (GraphNode) nodeIterator.next();
                if(node instanceof CompPerfGraphNode) {
                    compositeNodesList.add((CompPerfGraphNode) node);
                }
            }
            
            //for each composite process, make a subgraph and print its nodes inside
            for(int i = 0; i < compositeNodesList.size(); i++) {
                CompPerfGraphNode subgraphNode = compositeNodesList.elementAt(i);
                String subgraphName = subgraphNode.getName();
                
                pw.println("subgraph \"" + subgraphName + "\" {");
                
                //print nodes
                nodeIterator = graph.values().iterator();
                while(nodeIterator.hasNext()) {
                    GraphNode node = (GraphNode) nodeIterator.next();
                    if(node.getParentPerform() == subgraphNode) {
                        printNodeToFile(node,pw);                        
                    }
                }       
                
                //print start and end marks
                pw.println("\"" + subgraphNode.start.getName() + "\"->\"" + subgraphNode.start.getName() + "\" [color=white, taillabel=\"START\", fontcolor=blue, labelangle=90, labeldistance=2];");
                pw.println("\"" + subgraphNode.end.getName() + "\"->\"" + subgraphNode.end.getName() + "\" [color=white, headlabel=\"END\", fontcolor=blue, labelangle=\"-90\", labeldistance=2];");
                
                pw.println("}");
            }
            
            pw.println("}");   // end digraph
            pw.flush();
            pw.close();
        }
        catch (Exception ex)
	{
            System.err.println("Couldn't create graph file");
            System.err.println(ex.getMessage());
            pw.flush();
            pw.close();
	}
    }
    
    private void printNodeToFile(GraphNode node, PrintWriter pw) {
        String nodeName = node.getName();
        
        //print the node according to its type
        pw.print("\"" + nodeName + "\" [tip=\"" + nodeName + "\"");
        
        if(node instanceof PerfGraphNode) {
            if(node instanceof CompPerfGraphNode) {
                pw.print(", peripheries=2");
            }
            String label = ((Perform) node.getCC()).getProcess().getName();
            if(label==null) {
                label = ((Perform) node.getCC()).getProcess().getLocalName();
            }
            pw.println(", label=" + label + "];");            
        }
        else if(node instanceof ChoStartGraphNode) {
            pw.println(", shape=square, fixedsize=true, height=\"0.25\", width=\"0.5\", distortion=\"-1.0\", label=\"\"];");
            pw.println("\"" + nodeName + "\"->\"" + nodeName + "\" [color=white, label=\"Choice\"];");
        }
        else if(node instanceof ChoEndGraphNode) {
            pw.println(", shape=square, fixedsize=true, height=\"0.25\", width=\"0.5\", distortion=\"1.0\", label=\"\"];");
        }
        else if(node instanceof SpGraphNode) {
            pw.println(", shape=square, fixedsize=true, height=\"0.25\", width=\"0.5\", distortion=\"-1.0\", label=\"\"];");
            pw.println("\"" + nodeName + "\"->\"" + nodeName + "\" [color=white, label=\"Split\"];");
        }
        else if(node instanceof SpJStartGraphNode) {
            pw.println(", shape=square, fixedsize=true, height=\"0.25\", width=\"0.5\", distortion=\"-1.0\", label=\"\"];");
            pw.println("\"" + nodeName + "\"->\"" + nodeName + "\" [color=white, label=\"SplitJoin\"];");
        }
        else if(node instanceof SpJEndGraphNode) {
            pw.println(", shape=square, fixedsize=true, height=\"0.25\", width=\"0.5\", distortion=\"1.0\", label=\"\"];");
        }
        else if(node instanceof AOStartGraphNode) {
            pw.println(", shape=square, fixedsize=true, height=\"0.25\", width=\"0.5\", distortion=\"-1.0\", label=\"\"];");
            pw.println("\"" + nodeName + "\"->\"" + nodeName + "\" [color=white, label=\"AnyOrder\"];");
        }
        else if(node instanceof AOEndGraphNode) {
            pw.println(", shape=square, fixedsize=true, height=\"0.25\", width=\"0.5\", distortion=\"1.0\", label=\"\"];");
        }
        else if(node instanceof IteStartGraphNode) {
            pw.println(", shape=diamond, fixedsize=true, height=\"0.5\", width=\"0.5\", label=\"\"];");
            pw.println("\"" + nodeName + "\"->\"" + nodeName + "\" [color=white, label=\"IfThenElse\"];");
        }
        else if(node instanceof IteEndGraphNode) {
            pw.println(", shape=square, fixedsize=true, height=\"0.25\", width=\"0.5\", distortion=\"1.0\", label=\"\"];");
        }
        else if(node instanceof RWGraphNode) {
            pw.println(", shape=diamond, fixedsize=true, height=\"0.5\", width=\"0.5\", label=\"\"];");
            pw.println("\"" + nodeName + "\"->\"" + nodeName + "\" [color=white, label=\"RepeatWhile\"];");
        }
        else if(node instanceof RUGraphNode) {
            pw.println(", shape=diamond, fixedsize=true, height=\"0.5\", width=\"0.5\", label=\"\"];");
            pw.println("\"" + nodeName + "\"->\"" + nodeName + "\" [color=white, label=\"RepeatUntil\"];");
        }
        else if(node instanceof ProdGraphNode) {
            pw.println(", shape=circle, fixedsize=true, height=\"0.5\", width=\"0.5\", label=\"\"];");
            pw.println("\"" + nodeName + "\"->\"" + nodeName + "\" [color=white, label=\"Produce\"];");
        }                 

        //print the node's outgoing links
        GraphNode child = null;
        if(node instanceof SingleChildGraphNode) {
            if((child = ((SingleChildGraphNode) node).nextNode) != null) {
                pw.println("\"" + nodeName + "\"->\"" + child.getName() + "\"");
            }
        }
        else if(node instanceof SpGraphNode) {            
            String mainBranchNodeName = null;
            GraphNode mainBranchNode = ((SpGraphNode) node).getMainBranchNode();            
            if(mainBranchNode != null) {
                mainBranchNodeName = mainBranchNode.getName();
            }
            
            Iterator childIterator = ((SpGraphNode) node).nextNodes.keySet().iterator();
            while(childIterator.hasNext()) {
                String childName = (String) childIterator.next();
                if(mainBranchNodeName != null && childName.equals(mainBranchNodeName)) {
                    pw.println("\"" + nodeName + "\"->\"" + childName + "\"");
                }
                else {
                    pw.println("\"" + nodeName + "\"->\"" + childName + "\" [style=dotted]");
                }                
            }
        }
        else if(node instanceof MultipleChildGraphNode) {
            Iterator childIterator = ((MultipleChildGraphNode) node).nextNodes.keySet().iterator();
            while(childIterator.hasNext()) {
                pw.println("\"" + nodeName + "\"->\"" + (String) childIterator.next() + "\"");
            }
        }
        else if(node instanceof ConditionalGraphNode) {
            if((child = ((ConditionalGraphNode) node).trueNextNode) != null) {
                pw.println("\"" + nodeName + "\"->\"" + ((ConditionalGraphNode) node).trueNextNode.getName() + "\" [taillabel=\"T\"]");
            }
            if((child = ((ConditionalGraphNode) node).falseNextNode) != null) {
                pw.println("\"" + nodeName + "\"->\"" + ((ConditionalGraphNode) node).falseNextNode.getName() + "\" [taillabel=\"F\"]");
            }
        }
        
    }
    
    private void createGrappaPanels() {                
        //open the file, parse it and create the graph
        InputStream input = null;
        try {
            input = new FileInputStream(fileName);
        }
        catch(FileNotFoundException fnf) {
            System.err.println(fnf.toString());
        }
        
        Parser program = new Parser(input,System.err);
        try {
            program.parse();
            input.close();
        }
        catch(Exception ex) {
            System.err.println("Exception: " + ex.getMessage());
            try {
                input.close();
            }
            catch(Exception e) {
                System.err.println("Exception: " + e.getMessage());
            }
        }
        
        grappaGraph = program.getGraph();
        grappaGraph.setEditable(false);
        grappaGraph.setErrorWriter(new PrintWriter(System.err, true));
        
        //filter the graph with dot, if present
        Object connector = null;
        try{
            connector = Runtime.getRuntime().exec(gui.runner.getDotPath());
        }
        catch(Exception e){
            System.err.println("ERROR! Could not execute Dot at the given path");
        }
        if (connector != null) {
            if (!GrappaSupport.filterGraph(grappaGraph,connector)) {
                System.err.println("ERROR: somewhere in filterGraph");
            }
        }
        
        //create one grappaPanel for each subgraph in the grappaGraph
        for(Enumeration e = grappaGraph.subgraphElements(); e.hasMoreElements();) {
            Subgraph subgraph = (Subgraph) e.nextElement();
            String subgraphName = subgraph.getName();
            
            GrappaPanel panel = createGrappaPanel(subgraph);
            grappaPanels.put(subgraphName,panel);
        }
    }
    
    private GrappaPanel createGrappaPanel(Subgraph subgraph) {
        GrappaPanel panel = new GrappaPanel(subgraph);
        
        //set a Listener with the appropriate actions
        panel.addGrappaListener(new GrappaAdapter() {
            @Override
            public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, int clickCount, GrappaPanel panel) {
                //do things if left button is clicked, nothing else
                if((modifiers&InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                    if(subg.getGraph().isSelectable()) {
                        if(modifiers == InputEvent.BUTTON1_MASK) {
                            
                            //if double click, switch to the panel with the child or parent subgraph
                            if(clickCount == 2 && elem != null) {
                                String elemName = elem.getName();
                                if(elem instanceof Node) {                                    
                                    if(grappaPanels.containsKey(elemName)) {
                                        switchCurrentGrappaPanel(elemName);
                                    }                         
                                }
                                else if(elem instanceof Subgraph) {
                                    Node parentNode = grappaGraph.findNodeByName(elemName);
                                    if(parentNode != null) {
                                        switchCurrentGrappaPanel(parentNode.getSubgraph().getName());
                                    }
                                }
                                return;
                            }
                            
                            // select element
                            if(elem == null) {
                                if(currentSelection != null) {                                    
                                    currentSelection.highlight &= ~HIGHLIGHT_MASK;                                    
                                    currentSelection = null;
                                    subg.getGraph().repaint();
                                }
                            }
                            else {
                                if(currentSelection != null) {
                                    if(currentSelection == elem) return;
                                    currentSelection.highlight &= ~HIGHLIGHT_MASK;                                    
                                    currentSelection = null;
                                }

                                if(elem instanceof Node || elem instanceof Subgraph) {
                                    elem.highlight |= SELECTION_MASK;
                                    currentSelection = elem;
                                    subg.getGraph().repaint();

                                    //perform the real action: select the node in the OwlsRunner
                                    gui.selectNode(elem.getName());
                                }
                            }
                        }	
                    }
                }
            }
            //override the other listeners
            @Override
            public void grappaPressed(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, GrappaPanel panel) {}
            @Override
            public void grappaReleased(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, Element pressedElem, GrappaPoint pressedPt, int pressedModifiers, GrappaBox outline, GrappaPanel panel) {}
            @Override
            public void grappaDragged(Subgraph subg, GrappaPoint currentPt, int currentModifiers, Element pressedElem, GrappaPoint pressedPt, int pressedModifiers, GrappaBox outline, GrappaPanel panel) {}

            @Override
            public String grappaTip(Subgraph subg, Element elem, GrappaPoint pt, int modifiers, GrappaPanel panel) {
                //show a tooltip with the name of the node
                String tip = null;

                if(elem != null) {
                    if(elem.getType() == NODE) {
                        Node node = (Node)elem;
                        if((tip = (String)node.getAttributeValue(TIP_ATTR)) == null) {
                            if(subg.getShowNodeLabels()) {
                                tip = node.getName();
                            } else {
                                if((tip = (String)node.getAttributeValue(LABEL_ATTR)) == null || tip.equals("\\N")) {
                                    tip = node.getName();
                                }
                            }
                            tip = "Node: " + tip;
                        }
                    }
                }

                return(tip);
            }
        });            
                       
        panel.setScaleToFit(true);
      
        return panel;
    }
    
    private void switchCurrentGrappaPanel(String panelName) {
        currentGrappaPanel = grappaPanels.get(panelName);
        grappaGraph.repaint();
        gui.updateServiceGraphGUI(this);
    }
    
    public void setNodeEnabledState(String nodeName, boolean enabled) {
        Node grappaNode = grappaGraph.findNodeByName(nodeName);
        if(grappaNode != null) {
            grappaNode.setAttribute("color",enabled?"darkseagreen1":"lemonchiffon1");
            grappaNode.setAttribute(GrappaConstants.COLOR_ATTR,grappaNode.getAttributeValue(GrappaConstants.COLOR_ATTR));
        }
        grappaGraph.repaint();
    }
    
    public void zoomIn() {
        currentGrappaPanel.setScaleToFit(false);
        currentGrappaPanel.setScaleToSize(null);
        currentGrappaPanel.multiplyScaleFactor(1.25);
        currentGrappaPanel.clearOutline();
        currentGrappaPanel.repaint();
    }
    
    public void zoomOut() {
        currentGrappaPanel.setScaleToFit(false);
        currentGrappaPanel.setScaleToSize(null);
        currentGrappaPanel.multiplyScaleFactor(0.8);
        currentGrappaPanel.clearOutline();
        currentGrappaPanel.repaint();
    }
    
    public void resetZoom() {
        currentGrappaPanel.setScaleToFit(true);
        currentGrappaPanel.clearOutline();
        currentGrappaPanel.repaint();
    }
    
}
