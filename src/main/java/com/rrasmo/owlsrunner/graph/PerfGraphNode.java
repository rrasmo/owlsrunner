package com.rrasmo.owlsrunner.graph;

import org.mindswap.owls.process.ControlConstruct;

import com.rrasmo.owlsrunner.OwlsRunner;

/**
 *
 * @author Rafael Ramos
 */
public class PerfGraphNode extends SingleChildGraphNode {
    
    edu.stanford.smi.protegex.owl.model.OWLIndividual performInputSet;
    edu.stanford.smi.protegex.owl.model.OWLIndividual performOutputSet;
    edu.stanford.smi.protegex.owl.model.OWLIndividual performLocalSet;
    
    public PerfGraphNode(String theName, String theType, ControlConstruct theCC, CompPerfGraphNode theParentPerform, OwlsRunner theRunner) {
        super(theName,theType,theCC,theParentPerform,theRunner);
    }    
    
    public void setPerformInputSet(edu.stanford.smi.protegex.owl.model.OWLIndividual thePerformInputSet) {
        performInputSet = thePerformInputSet;
    }
    
    public void setPerformOutputSet(edu.stanford.smi.protegex.owl.model.OWLIndividual thePerformOutputSet) {
        performOutputSet = thePerformOutputSet;
    }
    
    public void setPerformLocalSet(edu.stanford.smi.protegex.owl.model.OWLIndividual thePerformLocalSet) {
        performLocalSet = thePerformLocalSet;
    }
    
    public edu.stanford.smi.protegex.owl.model.OWLIndividual getPerformInputSet() {
        return performInputSet;
    }
    
    public edu.stanford.smi.protegex.owl.model.OWLIndividual getPerformOutputSet() {
        return performOutputSet;
    }
    
    public edu.stanford.smi.protegex.owl.model.OWLIndividual getPerformLocalSet() {
        return performLocalSet;
    }
      
    @Override
    public void reset() {
        super.reset();
        runner.emptyParameterSet(performInputSet);
        runner.setXSetEditable(performInputSet,true);
        runner.emptyParameterSet(performOutputSet);
    }    
    
}
