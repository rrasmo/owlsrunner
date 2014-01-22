package com.rrasmo.owlsrunner.gui;

import org.mindswap.owls.process.Conditional;

import com.rrasmo.owlsrunner.graph.ConditionalGraphNode;

/**
 *
 * @author  Rafael Ramos
 */
public class ConditionalInfoPanel extends InfoPanel {
            
    RunnerGUI gui;
    
    public ConditionalInfoPanel(ConditionalGraphNode node, RunnerGUI theGUI) {
        initComponents();       
        gui = theGUI;
                
        initCondition(node);
    }
    
    private void initCondition(ConditionalGraphNode node) {
        String conditionString = ((Conditional) node.getCC()).getCondition().getBody().toString();
        textArea.append(conditionString + "\n"); 
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        conditionTab = new javax.swing.JPanel();
        conditionLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();

        conditionLabel.setText("Condition"); // NOI18N

        textArea.setColumns(20);
        textArea.setEditable(false);
        textArea.setRows(5);
        scrollPane.setViewportView(textArea);

        javax.swing.GroupLayout conditionTabLayout = new javax.swing.GroupLayout(conditionTab);
        conditionTab.setLayout(conditionTabLayout);
        conditionTabLayout.setHorizontalGroup(
            conditionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(conditionTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(conditionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                    .addComponent(conditionLabel))
                .addContainerGap())
        );
        conditionTabLayout.setVerticalGroup(
            conditionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(conditionTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(conditionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Condition", conditionTab);

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
    private javax.swing.JLabel conditionLabel;
    private javax.swing.JPanel conditionTab;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
    
}
