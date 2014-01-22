package com.rrasmo.owlsrunner.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import se.liu.ida.JessTab.JConsolePanel;

import com.rrasmo.owlsrunner.OwlUtils;
import com.rrasmo.owlsrunner.OwlsRunner;
import com.rrasmo.owlsrunner.graph.ConditionalGraphNode;
import com.rrasmo.owlsrunner.graph.GraphNode;
import com.rrasmo.owlsrunner.graph.PerfGraphNode;
import com.rrasmo.owlsrunner.graph.ProdGraphNode;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.PopupMenuMouseListener;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protege.util.WaitCursor;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.ui.cls.ClassTree;
import edu.stanford.smi.protegex.owl.ui.individuals.AssertedInstancesListPanel;
import edu.stanford.smi.protegex.owl.ui.individuals.IndividualsTabClassesPanel;
import edu.stanford.smi.protegex.owl.ui.tooltips.ClassDescriptionToolTipGenerator;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;

/**
 * The main class of the GUI.
 * 
 * @author  Rafael Ramos
 */
public class RunnerGUI extends javax.swing.JFrame {
    
    OwlsRunner runner;
    Project project;
    IndividualsTabClassesPanel classesPanel;
    AssertedInstancesListPanel individualsPanel;
    Hashtable<String,GraphGUI> graphDisplays;
    Hashtable<String,JScrollPane> servicePanels;
    Hashtable<String,InfoPanel> infoPanels;
    
    /**
     * Creates a new GUI for the given OwlsRunner.
     * @param theRunner
     */
    public RunnerGUI(OwlsRunner theRunner) {
        initComponents();
        
        runner = theRunner;
        project = runner.getModel().getProject();
        graphDisplays = new Hashtable<String,GraphGUI>();
        servicePanels = new Hashtable<String,JScrollPane>();
        infoPanels = new Hashtable<String,InfoPanel>();

        individualsPanel = new AssertedInstancesListPanel(runner.getModel());
        individualsTab.add(individualsPanel);
        createClassesPanel();
        classesTab.add(classesPanel);
        
        //set an OWLToolTipGenerator to avoid the "LOST GENERATOR" warning
        OWLUI.setOWLToolTipGenerator(new ClassDescriptionToolTipGenerator());                
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane1 = new javax.swing.JSplitPane();
        splitPane2 = new javax.swing.JSplitPane();
        splitPane3 = new javax.swing.JSplitPane();
        ontologiesTabbedPane = new javax.swing.JTabbedPane();
        classesTab = new javax.swing.JPanel();
        individualsTab = new javax.swing.JPanel();
        centralPanel = new javax.swing.JPanel();
        selectedNodeLabel = new javax.swing.JLabel();
        selectedNodeField = new javax.swing.JTextField();
        executeButton = new javax.swing.JButton();
        autoExecuteButton = new javax.swing.JButton();
        mainInfoPanel = new javax.swing.JPanel();
        jessPanel = new javax.swing.JPanel();
        servicesTabbedPane = new javax.swing.JTabbedPane();
        menuBar = new javax.swing.JMenuBar();
        mainMenu = new javax.swing.JMenu();
        openServiceMenuItem = new javax.swing.JMenuItem();
        resetServiceMenu = new javax.swing.JMenu();
        closeServiceMenu = new javax.swing.JMenu();
        optionsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("OWL-S Runner");

        splitPane1.setDividerLocation(796);
        splitPane1.setDividerSize(5);

        splitPane2.setDividerLocation(480);
        splitPane2.setDividerSize(5);
        splitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        splitPane3.setDividerLocation(260);
        splitPane3.setDividerSize(5);

        classesTab.setLayout(new javax.swing.BoxLayout(classesTab, javax.swing.BoxLayout.LINE_AXIS));
        ontologiesTabbedPane.addTab("Classes", classesTab);

        individualsTab.setLayout(new javax.swing.BoxLayout(individualsTab, javax.swing.BoxLayout.LINE_AXIS));
        ontologiesTabbedPane.addTab("Individuals", individualsTab);

        splitPane3.setLeftComponent(ontologiesTabbedPane);

        selectedNodeLabel.setText("Selected node:");

        selectedNodeField.setEditable(false);

        executeButton.setText("Execute");
        executeButton.setEnabled(false);
        executeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeButtonActionPerformed(evt);
            }
        });

        autoExecuteButton.setText("Auto Execute");
        autoExecuteButton.setEnabled(false);
        autoExecuteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoExecuteButtonActionPerformed(evt);
            }
        });

        mainInfoPanel.setLayout(new javax.swing.BoxLayout(mainInfoPanel, javax.swing.BoxLayout.PAGE_AXIS));

        javax.swing.GroupLayout centralPanelLayout = new javax.swing.GroupLayout(centralPanel);
        centralPanel.setLayout(centralPanelLayout);
        centralPanelLayout.setHorizontalGroup(
            centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(centralPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, centralPanelLayout.createSequentialGroup()
                        .addComponent(autoExecuteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(executeButton)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, centralPanelLayout.createSequentialGroup()
                        .addComponent(selectedNodeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectedNodeField, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                        .addContainerGap())))
            .addComponent(mainInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
        );
        centralPanelLayout.setVerticalGroup(
            centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, centralPanelLayout.createSequentialGroup()
                .addComponent(mainInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectedNodeLabel)
                    .addComponent(selectedNodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(centralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(executeButton)
                    .addComponent(autoExecuteButton))
                .addContainerGap())
        );

        splitPane3.setRightComponent(centralPanel);

        splitPane2.setLeftComponent(splitPane3);

        jessPanel.setLayout(new javax.swing.BoxLayout(jessPanel, javax.swing.BoxLayout.LINE_AXIS));
        splitPane2.setRightComponent(jessPanel);

        splitPane1.setLeftComponent(splitPane2);
        splitPane1.setRightComponent(servicesTabbedPane);

        mainMenu.setText("Menu");

        openServiceMenuItem.setText("Open service");
        openServiceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openServiceMenuItemActionPerformed(evt);
            }
        });
        mainMenu.add(openServiceMenuItem);

        resetServiceMenu.setText("Reset service");
        mainMenu.add(resetServiceMenu);

        closeServiceMenu.setText("Close service");
        mainMenu.add(closeServiceMenu);

        optionsMenuItem.setText("Options");
        optionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsMenuItemActionPerformed(evt);
            }
        });
        mainMenu.add(optionsMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        mainMenu.add(exitMenuItem);

        menuBar.add(mainMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1173, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void autoExecuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoExecuteButtonActionPerformed
        if(runner.isSelectedNodeEnabled()) {
            runner.autoExecuteNode(runner.getSelectedNode());
            updateExecuteButtons();
        }
    }//GEN-LAST:event_autoExecuteButtonActionPerformed

    private void executeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeButtonActionPerformed
        if(runner.isSelectedNodeEnabled()) {
            runner.executeNode(runner.getSelectedNode(),false);
            updateExecuteButtons();
        }        
    }//GEN-LAST:event_executeButtonActionPerformed

    public void updateExecuteButtons() {
        executeButton.setEnabled(runner.isSelectedNodeEnabled());
        autoExecuteButton.setEnabled(runner.isSelectedNodeEnabled());
    }
    
    private void openServiceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openServiceMenuItemActionPerformed
        OpenServiceDialog dialog = new OpenServiceDialog(this,true);
        dialog.setVisible(true);
        if(dialog.owlsUri != null && dialog.prefix != null) {
            if(!runner.openService(dialog.owlsUri,dialog.prefix)) {
                JOptionPane.showMessageDialog(dialog,"The specified prefix already exists","Error",JOptionPane.ERROR_MESSAGE);
            }
        }        
    }//GEN-LAST:event_openServiceMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void optionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsMenuItemActionPerformed
        OptionsDialog dialog = new OptionsDialog(this,true,runner.getDotPath());
        dialog.setVisible(true);
        if(dialog.dotPath != null) {
            runner.setDotPath(dialog.dotPath);
        }
    }//GEN-LAST:event_optionsMenuItemActionPerformed
        
    /**
     * Removes all GUI elements of the given service.
     * @param serviceName
     */
    public void closeServiceGUI(String serviceName) {
        
        //remove graph elements
        graphDisplays.remove(serviceName);
        servicePanels.remove(serviceName);
        servicesTabbedPane.remove(servicesTabbedPane.indexOfTab(serviceName));
        
        //remove InfoPanels of the nodes of this service        
        Object[] nodeNames = infoPanels.keySet().toArray();
        for(int i = 0; i < nodeNames.length; i++) {
            String nodeName = (String) nodeNames[i];
            if(OwlUtils.prefixOfNode(nodeName).equals(serviceName)) {
                System.out.println("removing InfoPanel of " + nodeName);
                infoPanels.remove(nodeName);
            }
        }
        System.out.println(infoPanels.toString());        
        
        deleteServiceMenuItems(serviceName);        
        updateOntologiesTabbedPane();        
        updateCurrentInfoPanel(null);
        updateExecuteButtons();             
    }
    
    /**
     * Creates InfoPanels for a set of nodes.
     * @param nodes
     */
    public void createInfoPanels(Hashtable<String,GraphNode> nodes) {
        Iterator nodeIterator = nodes.values().iterator();
        while(nodeIterator.hasNext()) {
            GraphNode node = (GraphNode) nodeIterator.next();
            
            if(node instanceof PerfGraphNode) {
                PerformInfoPanel panel = new PerformInfoPanel((PerfGraphNode) node,this);
                infoPanels.put(node.getName(),panel);
            }
            else if(node instanceof ConditionalGraphNode) {
                ConditionalInfoPanel panel = new ConditionalInfoPanel((ConditionalGraphNode) node,this);
                infoPanels.put(node.getName(),panel);
            }
            else if(node instanceof ProdGraphNode) {
                ProduceInfoPanel panel = new ProduceInfoPanel((ProdGraphNode) node,this);
                infoPanels.put(node.getName(),panel);
            }
        }     
    }    
    
    /**
     * Shows the correct InfoPanel in the central panel every time a node is selected.
     * @param nodeName
     */
    public void updateCurrentInfoPanel(String nodeName) {
        mainInfoPanel.removeAll();
        if(nodeName != null) {
            InfoPanel panel = infoPanels.get(nodeName);
            if(panel != null) {
                mainInfoPanel.add(panel);
            }
        }
        mainInfoPanel.updateUI();
    }
    
    /**
     * Creates a service panel which displays the graph, in a new tab.
     * @param serviceName
     * @param graph
     */
    public void createServicePanel(String serviceName, Hashtable<String,GraphNode> graph) {
        GraphGUI gg = new GraphGUI(serviceName,graph,this);
        if(gg != null) {
            graphDisplays.put(serviceName,gg);            
            
            //the panel
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            
            //the toolbar
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            JButton zoomInButton = toolbar.add(new ZoomInAction(gg));
            zoomInButton.setToolTipText("Zoom in");
            JButton zoomOutButton = toolbar.add(new ZoomOutAction(gg));
            zoomOutButton.setToolTipText("Zoom out");
            JButton resetZoomButton = toolbar.add(new resetZoomAction(gg));
            resetZoomButton.setToolTipText("Reset zoom");
            panel.add(toolbar,BorderLayout.NORTH);
                        
            //a scrollPane with the current grappaPanel
            JScrollPane scrollPane = new JScrollPane(gg.currentGrappaPanel);
            panel.add(scrollPane,BorderLayout.CENTER);                        
            
            //put the panel in a new tab
            servicesTabbedPane.add(panel,serviceName);
            
            //store the scrollPane to change its grappaPanel later
            servicePanels.put(serviceName,scrollPane);            
        }        
    }
    
    /**
     * Changes the displayed graph when a composite node is entered or exitted.
     * @param gg
     */
    public void updateServiceGraphGUI(GraphGUI gg) {
        JScrollPane scrollPane = servicePanels.get(gg.serviceName);
        scrollPane.setViewportView(gg.currentGrappaPanel);
        scrollPane.updateUI();
    }
    
    /**
     * Requests the OwlsRunner to select a node.
     * @param nodeName
     */
    public void selectNode(String nodeName) {
        if(runner.setSelectedNode(nodeName)) {
            selectedNodeField.setText(runner.getSelectedNode().getName());
            updateExecuteButtons();
            updateCurrentInfoPanel(nodeName);
        }
    }
    
    /**
     * Sets or removes the red highlight of a node in the graphic.
     * @param nodeName
     * @param enabled
     */
    public void setNodeEnabledState(String nodeName, boolean enabled) {
        String serviceName = OwlUtils.prefixOfNode(nodeName);
        GraphGUI gg = graphDisplays.get(serviceName);
        if(gg != null) {
            gg.setNodeEnabledState(nodeName,enabled);
        }
    }
    
    /**
     * Updates the display of a ParameterSet to reflect its new state.
     * @param instance
     */
    public void reloadXSetDisplay(OWLIndividual instance) {
        PerformInfoPanel panel = (PerformInfoPanel) infoPanels.get(instance.getLabels().toArray()[0]);
        if(panel != null) {
            String xSetType = instance.getComments().toArray()[0].toString();
            if(xSetType.equals("InputSet")) {
                panel.inputSetDisplay.setInstance(instance);
            }
            else if(xSetType.equals("OutputSet")) {
                panel.outputSetDisplay.setInstance(instance);
            }
            else if(xSetType.equals("LocalSet")) {
                panel.localSetDisplay.setInstance(instance);
            }
        }
    }
    
    /**
     * Adds the service name to the Reset and Close emerging menus.
     * @param serviceName
     */
    public void createServiceMenuItems(String serviceName) {
        //create a MenuItem in the Reset menu
        JMenuItem resetServiceMenuItem = new JMenuItem(serviceName);
        resetServiceMenuItem.setName(serviceName);
        resetServiceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JMenuItem service = (JMenuItem) evt.getSource();
                runner.resetService(service.getName());
            }
        });
        resetServiceMenu.add(resetServiceMenuItem);
        
        //create a MenuItem in the Close menu
        JMenuItem closeServiceMenuItem = new JMenuItem(serviceName);
        closeServiceMenuItem.setName(serviceName);
        closeServiceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JMenuItem service = (JMenuItem) evt.getSource();
                runner.closeService(service.getName());
            }
        });
        closeServiceMenu.add(closeServiceMenuItem);
    }
    
    /**
     * Deletes the service name from the Reset and Close emerging menus.
     * @param serviceName
     */
    public void deleteServiceMenuItems(String serviceName) {
        //delete the MenuItem in the Reset menu
        Component[] resetComponents = resetServiceMenu.getMenuComponents();
        Component resetMenuItem = null;
        for(int i = 0; i < resetComponents.length && resetMenuItem == null; i++) {
            if(resetComponents[i].getName().equals(serviceName)) {
                resetMenuItem = resetComponents[i];
            }
        }
        if(resetMenuItem != null) {
            resetServiceMenu.remove(resetMenuItem);
        }
        
        //delete the MenuItem in the Close menu
        Component[] closeComponents = closeServiceMenu.getMenuComponents();
        Component closeMenuItem = null;
        for(int i = 0; i < closeComponents.length && closeMenuItem == null; i++) {
            if(closeComponents[i].getName().equals(serviceName)) {
                closeMenuItem = closeComponents[i];
            }
        }
        if(closeMenuItem != null) {
            closeServiceMenu.remove(closeMenuItem);
        }        
    }
        
    /**
     * Creates the Jess console.
     * @param jcp
     */
    public void createJessConsolePanel(JConsolePanel jcp) {
        jessPanel.add(jcp);
    }    
    
    /**
     * Updates the Classes and Individuals tabs to reflect the new state of the model.
     */
    public void updateOntologiesTabbedPane() {
        individualsTab.remove(individualsPanel);
        individualsPanel = new AssertedInstancesListPanel(runner.getModel());        
        individualsTab.add(individualsPanel);
        
        classesTab.remove(classesPanel);
        createClassesPanel();
        classesTab.add(classesPanel);
    }
    
    /**
     * Creates the Classes tab.
     */
    private void createClassesPanel() {
        classesPanel = new IndividualsTabClassesPanel(runner.getModel());
        
        //when a class is selected, display its instances in the individualsPanel
        classesPanel.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                WaitCursor cursor = new WaitCursor(classesPanel);
                try {
                    Collection selection = classesPanel.getSelection();                    
                    individualsPanel.setClses(selection);
                }
                finally {
                    cursor.hide();
                }
            }
        });
        
        //remove the unnecessary popup menus
        ClassTree classTree = (ClassTree) classesPanel.getTree();
        java.awt.event.MouseListener[] ml = classTree.getMouseListeners();
        for(int i = 0; i < ml.length; i++) {
            if(ml[i] instanceof PopupMenuMouseListener) {
                classTree.removeMouseListener(ml[i]);
            }
        }        
    }
    
    public void showTerminationMessage(String serviceName) {
        JOptionPane.showMessageDialog(this,serviceName + " service finished successfully","Message",JOptionPane.INFORMATION_MESSAGE);
    }
    
    private Icon getIcon(String fileName) {
        return new ImageIcon(this.getClass().getResource(fileName));
    }
    
    class ZoomInAction extends AbstractAction{
        GraphGUI gg;        
        public ZoomInAction(GraphGUI theGg){
            super("",getIcon("/icons/ZoomIn16.gif"));
            gg = theGg;
        }
        public void actionPerformed(ActionEvent ae){
            gg.zoomIn();
        }
    }
    
    class ZoomOutAction extends AbstractAction{
        GraphGUI gg;        
        public ZoomOutAction(GraphGUI theGg){
            super("",getIcon("/icons/ZoomOut16.gif"));
            gg = theGg;
        }
        public void actionPerformed(ActionEvent ae){
            gg.zoomOut();
        }
    }
    
    class resetZoomAction extends AbstractAction{
        GraphGUI gg;        
        public resetZoomAction(GraphGUI theGg){
            super("",getIcon("/icons/Zoom16.gif"));
            gg = theGg;
        }
        public void actionPerformed(ActionEvent ae){
            gg.resetZoom();
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton autoExecuteButton;
    private javax.swing.JPanel centralPanel;
    private javax.swing.JPanel classesTab;
    private javax.swing.JMenu closeServiceMenu;
    private javax.swing.JButton executeButton;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JPanel individualsTab;
    private javax.swing.JPanel jessPanel;
    private javax.swing.JPanel mainInfoPanel;
    private javax.swing.JMenu mainMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTabbedPane ontologiesTabbedPane;
    private javax.swing.JMenuItem openServiceMenuItem;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JMenu resetServiceMenu;
    private javax.swing.JTextField selectedNodeField;
    private javax.swing.JLabel selectedNodeLabel;
    private javax.swing.JTabbedPane servicesTabbedPane;
    private javax.swing.JSplitPane splitPane1;
    private javax.swing.JSplitPane splitPane2;
    private javax.swing.JSplitPane splitPane3;
    // End of variables declaration//GEN-END:variables

}
