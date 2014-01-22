package com.rrasmo.owlsrunner;

import jess.Defglobal;
import jess.Rete;
import jess.Value;
import se.liu.ida.JessTab.JConsolePanel;
import se.liu.ida.JessTab.JessTab;
import se.liu.ida.JessTab.JessTabEngine;

import com.rrasmo.owlsrunner.graph.GraphNode;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

/**
 * A class which manages the Jess engine.
 * 
 * @author Rafael Ramos
 */
public class JessRuler {
    
    OwlsRunner runner;
    Rete engine;
    JessTabEngine jte;
    JConsolePanel jcp;
        
    /**
     * Constructor. Initializes the engine and loads the system functions.
     * @param theRunner
     */
    public JessRuler(OwlsRunner theRunner) {
        runner = theRunner;        
        jte = new JessTabEngine("Default");
        jcp = new JConsolePanel(jte);
        jte.setJConsolePanel(jcp);                
        engine = jte.getEngine();
        (new JessTabWrapper()).doSetProtegeKB(runner.model);        
        
        try {
            //put "runner" as a Defglobal so that the Jess functions can call its methods
            engine.addDefglobal(new Defglobal("*runner*",new Value(runner)));

            //load system functions
            engine.batch("data/ORJessFunctions.clp");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        
        //show the console panel and start the read-eval loop
        runner.createJessConsolePanel(jcp);
        jte.start();        
    }    
    
    /**
     * Runs the engine.
     */
    public void run() {
        try {            
            int n = engine.run();
            //System.out.println(n + " rules fired");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Maps the Protege class instances as Jess facts.
     * @param cla
     */
    public void mapClass(OWLNamedClass cla) {
        try {
            engine.eval("(mapclass " + cla.getName() + ")");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }    
        
    public void assertEnabledNodeFact(GraphNode node) {
        try {            
            engine.assertString("(enabled " + node.getName() + ")");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void retractEnabledNodeFact(GraphNode node) {
        try {            
            engine.retractString("(enabled " + node.getName() + ")");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Empties Jess memory and reloads system functions.
     */
    public void restart() {
        //clear Jess memory (facts, rules...) and reload system stuff
        try {
            engine.clear();            
            engine.addDefglobal(new Defglobal("*runner*",new Value(runner)));           
            engine.batch("data/ORJessFunctions.clp");
            engine.eval("(mapclass ParameterSet)");            
        }
        catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
        
    class JessTabWrapper extends JessTab {
        //this class is used to call the protected static method JessTab.setProtegeKB()
		
	public void doSetProtegeKB(KnowledgeBase kb) {
            this.setProtegeKB(kb);
	}
    }
    
}
