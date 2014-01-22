package com.rrasmo.owlsrunner;

import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLClass;
import org.mindswap.owl.OWLProperty;

import edu.stanford.smi.protegex.owl.model.*;

import java.net.URI;
import java.util.*;

/**
 * A class to provide useful functions.
 * 
 * @author Rafael Ramos
 */
public class OwlUtils {
    
    /**
     * Looks for the given Protege individual in the Mindswap kb.
     * @param kb
     * @param individualP
     * @return the M individual
     */
    public static org.mindswap.owl.OWLIndividual getIndividualP2M(OWLKnowledgeBase kb, edu.stanford.smi.protegex.owl.model.OWLIndividual individualP) {
        org.mindswap.owl.OWLIndividual individualM = null;
        
        String individualURI = individualP.getURI();
        individualM = kb.getIndividual(URI.create(individualURI));
        
        return individualM;
    }
    
    
    /**
     * Creates an individual in the Mindswap kb from the given Protege individual.
     * @param kb
     * @param individualP
     * @return the M individual
     */
    public static org.mindswap.owl.OWLIndividual createOrGetIndividualP2M(OWLKnowledgeBase kb, edu.stanford.smi.protegex.owl.model.OWLIndividual individualP) {
        org.mindswap.owl.OWLIndividual individualM;
        
        //if it already exists, get it and return it
        individualM = getIndividualP2M(kb,individualP);
        if(individualM != null) {
            return individualM;
        }
        
        //get the M class using the uri of the P class of the P individual
        OWLClass classM = kb.getClass(URI.create(individualP.getProtegeType().getURI()));
        
        //create an M individual of this class
        individualM = kb.createInstance(classM);
        
        //iterate through the properties to copy their values into those of the M individual
        Iterator propertyIterator = individualP.getRDFProperties().iterator();
        while(propertyIterator.hasNext()) {
            RDFProperty propP = (RDFProperty) propertyIterator.next();
            
            if(propP instanceof edu.stanford.smi.protegex.owl.model.OWLObjectProperty) {
                org.mindswap.owl.OWLObjectProperty objectPropM = kb.getObjectProperty(URI.create(propP.getURI()));
                
                //iterate through the values of this property
                Iterator valueIterator = individualP.getPropertyValues(propP).iterator();
                while(valueIterator.hasNext()) {
                    Object valueP = valueIterator.next();
                    
                    if(valueP instanceof edu.stanford.smi.protegex.owl.model.OWLIndividual) {
                        //convert the object value to M and add it
                        org.mindswap.owl.OWLIndividual valueM = createOrGetIndividualP2M(kb,(edu.stanford.smi.protegex.owl.model.OWLIndividual) valueP);
                        if(valueM != null) {
                            individualM.addProperty(objectPropM,valueM);
                        }
                    }
                }                
            }
            else if(propP instanceof edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty) {
                org.mindswap.owl.OWLDataProperty dataPropM = kb.getDataProperty(URI.create(propP.getURI()));
                
                //iterate through the values of this property
                Iterator valueIterator = individualP.getPropertyValues(propP).iterator();
                while(valueIterator.hasNext()) {
                    //copy the value as a string
                    String valueP = valueIterator.next().toString();
                    individualM.addProperty(dataPropM,valueP);
                }
            }
        }
        
        return individualM;
    }    
    
    /**
     * Looks for the given Mindswap individual in the Protege model.
     * @param model
     * @param individualM
     * @return the P individual
     */
    public static edu.stanford.smi.protegex.owl.model.OWLIndividual getIndividualM2P(OWLModel model, org.mindswap.owl.OWLIndividual individualM) {
        edu.stanford.smi.protegex.owl.model.OWLIndividual individualP = null;
        
        URI individualURI = individualM.getURI();
        if(individualURI != null) {
            individualP = model.getOWLIndividual(model.getResourceNameForURI(individualURI.toString()));            
        }
        
        return individualP;
    }
    
    /**
     * Creates an individual in the Protege model from the given Mindswap individual.
     * @param model
     * @param individualM
     * @param editable
     * @return the P individual
     */
    public static edu.stanford.smi.protegex.owl.model.OWLIndividual createOrGetIndividualM2P(OWLModel model, org.mindswap.owl.OWLIndividual individualM, boolean editable) {
        edu.stanford.smi.protegex.owl.model.OWLIndividual individualP = null;
        
        //if it already exists, get it and return it
        individualP = getIndividualM2P(model,individualM);
        if(individualP != null) {
            return individualP;
        }
        
        //get the P class using the uri of the M class of the M individual
        OWLNamedClass classP = model.getOWLNamedClass(model.getResourceNameForURI(individualM.getType().getURI().toString()));
                
        //create an P individual of this class
        individualP = classP.createOWLIndividual(null);
        individualP.setEditable(editable);
        
        //iterate through the properties to copy their values into those of the P individual
        Iterator propertyIterator = individualM.getProperties().keySet().iterator();
        while(propertyIterator.hasNext()) {
            OWLProperty propM = (OWLProperty) propertyIterator.next();
            
            if(propM instanceof org.mindswap.owl.OWLObjectProperty) {
                edu.stanford.smi.protegex.owl.model.OWLObjectProperty propP = model.getOWLObjectProperty(model.getResourceNameForURI(propM.getURI().toString()));
                
                //iterate through the values of this property
                Iterator valuesIterator = individualM.getProperties((org.mindswap.owl.OWLObjectProperty) propM).iterator();
                while(valuesIterator.hasNext()) {
                    Object valueM = valuesIterator.next();
                    
                    if(valueM instanceof org.mindswap.owl.OWLIndividual) {
                        edu.stanford.smi.protegex.owl.model.OWLIndividual valueP = createOrGetIndividualM2P(model,(org.mindswap.owl.OWLIndividual) valueM,editable);
                        if(valueP != null) {
                            individualP.addPropertyValue(propP,valueP);
                        }
                    }
                }                
            }
            else if(propM instanceof org.mindswap.owl.OWLDataProperty) {
                edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty propP = model.getOWLDatatypeProperty(model.getResourceNameForURI(propM.getURI().toString()));
                
                //iterate through the values of this property
                Iterator valuesIterator = individualM.getProperties((org.mindswap.owl.OWLDataProperty) propM).iterator();
                while(valuesIterator.hasNext()) {
                    Object valueM = valuesIterator.next();
                    
                    if(valueM instanceof org.mindswap.owl.OWLDataValue) {     
                        String literalString = ((org.mindswap.owl.OWLDataValue) valueM).getLexicalValue();
                        RDFSLiteral literalP = model.createRDFSLiteral(literalString,propP.getRangeDatatype());
                        individualP.addPropertyValue(propP,literalP);
                    }
                }                
            }
        }
                    
        return individualP;
    }
    
    public static String prefixOfNode(String nodeName) {
        StringTokenizer tok = new StringTokenizer(nodeName,":");
        return tok.nextToken();
    }
    
    public static String localNameOfNode(String nodeName) {
        StringTokenizer tok = new StringTokenizer(nodeName,":");
        tok.nextToken();
        return tok.nextToken();
    }
    
}
