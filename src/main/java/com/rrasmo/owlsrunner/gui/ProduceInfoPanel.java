package com.rrasmo.owlsrunner.gui;

import org.mindswap.owls.process.Output;
import org.mindswap.owls.process.OutputBinding;
import org.mindswap.owls.process.OutputBindingList;
import org.mindswap.owls.process.Parameter;
import org.mindswap.owls.process.ParameterValue;
import org.mindswap.owls.process.Perform;
import org.mindswap.owls.process.Produce;
import org.mindswap.owls.process.ValueData;
import org.mindswap.owls.process.ValueOf;
import org.mindswap.owls.vocabulary.OWLS;

import com.rrasmo.owlsrunner.graph.ProdGraphNode;

/**
 *
 * @author  Rafael ramos
 */
public class ProduceInfoPanel extends InfoPanel {
            
    RunnerGUI gui;
    
    public ProduceInfoPanel(ProdGraphNode node, RunnerGUI theGUI) {
        initComponents();       
        gui = theGUI;
                
        initBindings(node);
    }
    
    private void initBindings(ProdGraphNode node) {
        Produce prod = (Produce) node.getCC();        
      
        OutputBindingList bindings = prod.getBindings();
        for(int i = 0; i < bindings.size(); i++) {
            OutputBinding binding = bindings.outputBindingAt(i);
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
                if(otherPerform.equals(OWLS.Process.TheParentPerform)) {
                    otherPerform = (Perform) node.getParentPerform().getCC();
                }
                Parameter param = valueOf.getParameter();
                valueString = otherPerform.getLocalName() + "." + param.getLocalName();                
            }
            
            textArea.append(outputString + " <- " + valueString + "\n");
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        bindingsTab = new javax.swing.JPanel();
        producedBindingsLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();

        producedBindingsLabel.setText("Produced bindings"); // NOI18N

        textArea.setColumns(20);
        textArea.setEditable(false);
        textArea.setRows(5);
        scrollPane.setViewportView(textArea);

        javax.swing.GroupLayout bindingsTabLayout = new javax.swing.GroupLayout(bindingsTab);
        bindingsTab.setLayout(bindingsTabLayout);
        bindingsTabLayout.setHorizontalGroup(
            bindingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bindingsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bindingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                    .addComponent(producedBindingsLabel))
                .addContainerGap())
        );
        bindingsTabLayout.setVerticalGroup(
            bindingsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bindingsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(producedBindingsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Bindings", bindingsTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bindingsTab;
    private javax.swing.JLabel producedBindingsLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
    
}
