package com.rrasmo.owlsrunner.gui;

import java.util.Iterator;

import org.mindswap.owl.OWLIndividualList;
import org.mindswap.owls.process.Binding;
import org.mindswap.owls.process.BindingList;
import org.mindswap.owls.process.Condition;
import org.mindswap.owls.process.ConditionList;
import org.mindswap.owls.process.Output;
import org.mindswap.owls.process.OutputBinding;
import org.mindswap.owls.process.OutputBindingList;
import org.mindswap.owls.process.Parameter;
import org.mindswap.owls.process.ParameterList;
import org.mindswap.owls.process.ParameterValue;
import org.mindswap.owls.process.Perform;
import org.mindswap.owls.process.Result;
import org.mindswap.owls.process.ResultList;
import org.mindswap.owls.process.ValueData;
import org.mindswap.owls.process.ValueOf;
import org.mindswap.owls.vocabulary.OWLS;

import com.rrasmo.owlsrunner.OwlUtils;
import com.rrasmo.owlsrunner.graph.PerfGraphNode;

import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.widget.FormWidget;
import edu.stanford.smi.protege.widget.SlotWidget;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFSClass;

/**
 *
 * @author  Rafael Ramos
 */
public class PerformInfoPanel extends InfoPanel {
            
    InstanceDisplay inputSetDisplay;
    InstanceDisplay outputSetDisplay;
    InstanceDisplay localSetDisplay;
    RunnerGUI gui;
    
    public PerformInfoPanel(PerfGraphNode node, RunnerGUI theGUI) {
        initComponents();       
        gui = theGUI;
        
        OWLIndividual processP = OwlUtils.getIndividualM2P(node.runner.getModel(),((Perform) node.getCC()).getProcess());        
        processField.setText(processP.getName());        
        
        initInputs(node);
        initOutputs(node);
        initLocals(node);        
        initPreconditions(node);
        initBindings(node);
        initResults(node);
    }
    
    private void initInputs(PerfGraphNode node) {
        OWLIndividual inputSet = node.getPerformInputSet();
        customizeClsWidget(inputSet);        
        inputSetDisplay = new InstanceDisplay(gui.project,false,false);           
        inputSetDisplay.setInstance(inputSet);
        
        inputsTab.add(inputSetDisplay);
    }
    
    private void initOutputs(PerfGraphNode node) {
        OWLIndividual outputSet = node.getPerformOutputSet();
        customizeClsWidget(outputSet);        
        outputSetDisplay = new InstanceDisplay(gui.project,false,false);           
        outputSetDisplay.setInstance(outputSet);
        
        outputsTab.add(outputSetDisplay);
    }
    
    private void initLocals(PerfGraphNode node) {
        OWLIndividual localSet = node.getPerformLocalSet();
        customizeClsWidget(localSet);        
        localSetDisplay = new InstanceDisplay(gui.project,false,false);           
        localSetDisplay.setInstance(localSet);
        
        localsTab.add(localSetDisplay);
    }
        
    private void initPreconditions(PerfGraphNode node) {
        ConditionList preconditions = ((Perform) node.getCC()).getProcess().getConditions();
        for(int i = 0; i < preconditions.size(); i++) {
            Condition precondition = preconditions.conditionAt(i);
            String preconditionString = precondition.getBody().toString();
            preconditionsTextArea.append(preconditionString + "\n");             
        }
    }
    
    private void initBindings(PerfGraphNode node) {
        Perform perf = (Perform) node.getCC();
        BindingList bindings = perf.getBindings();
        for(int i = 0; i < bindings.size(); i++) {
            Binding binding = bindings.bindingAt(i);
            Parameter param = binding.getParameter();
            String paramString = param.getLocalName();
            String valueString = null;
            ParameterValue paramValue = binding.getValue();
            if(paramValue instanceof ValueData) {
                valueString = ((ValueData) paramValue).getData().toString();
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
                valueString = otherPerform.getLocalName() + "." + otherParam.getLocalName();    
            }
            
            bindingsTextArea.append(paramString + " <- " + valueString + "\n");
        }
    }
        
    private void initResults(PerfGraphNode node) {
        Perform perf = (Perform) node.getCC();
        ResultList results = perf.getProcess().getResults();
        for(int i = 0; i < results.size(); i++) {
            Result result = results.resultAt(i);
            
            String titleString = "RESULT " + i + "\n";
            
            String conditionsString = "inCondition:\n";            
            ConditionList conditions = result.getConditions();            
            for(int j = 0; j < conditions.size(); j++) {
                Condition condition = conditions.conditionAt(j);
                conditionsString = conditionsString + "\t" + condition.getBody().toString() + "\n";
            }
            if(conditions.size() == 0) {
                conditionsString = "";
            }
            
            String resultVarsString = "hasResultVar:\n";            
            ParameterList parameters = result.getParameters();     
            for(int j = 0; j < parameters.size(); j++) {
                Parameter parameter = parameters.parameterAt(j);
                resultVarsString = resultVarsString + "\t" + parameter.getLocalName() + "\n";
            }
            if(parameters.size() == 0) {
                resultVarsString = "";
            }
            
            String bindingsString = "withOutput:\n";            
            OutputBindingList bindings = result.getBindings();
            for(int j = 0; j < bindings.size(); j++) {
                OutputBinding binding = bindings.outputBindingAt(j);
                Output output = binding.getOutput();
                String outputString = output.getLocalName();
                String valueString = null;
                ParameterValue paramValue = binding.getValue();
                if(paramValue instanceof ValueData) {                
                    valueString = ((ValueData) paramValue).getData().toString();
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
                    valueString = otherPerform.getLocalName() + "." + param.getLocalName();                
                }

                bindingsString = bindingsString + "\t" + outputString + " <- " + valueString + "\n";
            }
            if(bindings.size() == 0) {
                bindingsString = "";
            }
            
            String effectsString = "hasEffect:\n";      
            OWLIndividualList effects = result.getEffects();
            for(int j = 0; j < effects.size(); j++) {
                org.mindswap.owls.generic.expression.Expression effect = (org.mindswap.owls.generic.expression.Expression) effects.individualAt(j);
                effectsString = effectsString + "\t" + effect.getBody().toString() + "\n";
            }
            if(effects.size() == 0) {
                effectsString = "";
            }
                        
            resultsTextArea.append(titleString + conditionsString + resultVarsString + bindingsString + effectsString + "\n");
        }        
    }
        
    private void customizeClsWidget(OWLIndividual instance) {
        //this method changes the label of the properties in the InstanceDisplays 
        //so that it shows the short name and the type
       
        RDFSClass cls = instance.getProtegeType();
        FormWidget fw = (FormWidget) gui.project.getDesignTimeClsWidget(cls); 
        if(!fw.getDescriptor().isDirectlyCustomizedByUser()) {
            Iterator propertyIterator = gui.project.getClsWidgetPropertyList(cls).getNames().iterator();
            while(propertyIterator.hasNext()) {
                OWLProperty property = gui.runner.getModel().getOWLProperty((String) propertyIterator.next());
                if(property != null) {
                    SlotWidget sw = fw.getSlotWidget(property);
                    sw.setLabel(property.getLabels().toArray()[0].toString() + " (" + property.getRange().getName() + ")");
                    fw.getDescriptor().setDirectlyCustomizedByUser(true);
                }
            }
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        inputsTab = new javax.swing.JPanel();
        outputsTab = new javax.swing.JPanel();
        localsTab = new javax.swing.JPanel();
        preconditionsTab = new javax.swing.JPanel();
        preconditionsLabel = new javax.swing.JLabel();
        preconditionsScrollPane = new javax.swing.JScrollPane();
        preconditionsTextArea = new javax.swing.JTextArea();
        resultsTab = new javax.swing.JPanel();
        resultsLabel = new javax.swing.JLabel();
        resultsScrollPane = new javax.swing.JScrollPane();
        resultsTextArea = new javax.swing.JTextArea();
        bindingsTab = new javax.swing.JPanel();
        bindingsLabel = new javax.swing.JLabel();
        bindingsScrollPane = new javax.swing.JScrollPane();
        bindingsTextArea = new javax.swing.JTextArea();
        processLabel = new javax.swing.JLabel();
        processField = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        inputsTab.setLayout(new javax.swing.BoxLayout(inputsTab, javax.swing.BoxLayout.LINE_AXIS));
        tabbedPane.addTab("Inputs", inputsTab);

        outputsTab.setLayout(new javax.swing.BoxLayout(outputsTab, javax.swing.BoxLayout.LINE_AXIS));
        tabbedPane.addTab("Outputs", outputsTab);

        localsTab.setLayout(new javax.swing.BoxLayout(localsTab, javax.swing.BoxLayout.LINE_AXIS));
        tabbedPane.addTab("Locals", localsTab);

        preconditionsLabel.setText("Preconditions"); // NOI18N

        preconditionsTextArea.setColumns(20);
        preconditionsTextArea.setEditable(false);
        preconditionsTextArea.setRows(5);
        preconditionsScrollPane.setViewportView(preconditionsTextArea);

        javax.swing.GroupLayout preconditionsTabLayout = new javax.swing.GroupLayout(preconditionsTab);
        preconditionsTab.setLayout(preconditionsTabLayout);
        preconditionsTabLayout.setHorizontalGroup(
            preconditionsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(preconditionsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(preconditionsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(preconditionsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                    .addComponent(preconditionsLabel))
                .addContainerGap())
        );
        preconditionsTabLayout.setVerticalGroup(
            preconditionsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(preconditionsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(preconditionsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(preconditionsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Preconditions", preconditionsTab);

        resultsLabel.setText("Results"); // NOI18N

        resultsTextArea.setColumns(20);
        resultsTextArea.setEditable(false);
        resultsTextArea.setRows(5);
        resultsScrollPane.setViewportView(resultsTextArea);

        javax.swing.GroupLayout resultsTabLayout = new javax.swing.GroupLayout(resultsTab);
        resultsTab.setLayout(resultsTabLayout);
        resultsTabLayout.setHorizontalGroup(
            resultsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resultsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resultsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                    .addComponent(resultsLabel))
                .addContainerGap())
        );
        resultsTabLayout.setVerticalGroup(
            resultsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resultsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Results", resultsTab);

        bindingsLabel.setText("Bindings"); // NOI18N

        bindingsTextArea.setColumns(20);
        bindingsTextArea.setEditable(false);
        bindingsTextArea.setRows(5);
        bindingsScrollPane.setViewportView(bindingsTextArea);

        javax.swing.GroupLayout bindingsTabLayout = new javax.swing.GroupLayout(bindingsTab);
        bindingsTab.setLayout(bindingsTabLayout);
        bindingsTabLayout.setHorizontalGroup(
            bindingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bindingsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bindingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bindingsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                    .addComponent(bindingsLabel))
                .addContainerGap())
        );
        bindingsTabLayout.setVerticalGroup(
            bindingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bindingsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bindingsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bindingsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Bindings", bindingsTab);

        processLabel.setText("Process:");

        processField.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(processLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processField, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(processLabel)
                    .addComponent(processField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bindingsLabel;
    private javax.swing.JScrollPane bindingsScrollPane;
    private javax.swing.JPanel bindingsTab;
    private javax.swing.JTextArea bindingsTextArea;
    private javax.swing.JPanel inputsTab;
    private javax.swing.JPanel localsTab;
    private javax.swing.JPanel outputsTab;
    private javax.swing.JLabel preconditionsLabel;
    private javax.swing.JScrollPane preconditionsScrollPane;
    private javax.swing.JPanel preconditionsTab;
    private javax.swing.JTextArea preconditionsTextArea;
    private javax.swing.JTextField processField;
    private javax.swing.JLabel processLabel;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JScrollPane resultsScrollPane;
    private javax.swing.JPanel resultsTab;
    private javax.swing.JTextArea resultsTextArea;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
    
}
