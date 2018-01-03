/**
 * A ToolPalette for the world builder
 * 
 * @author Brian Dahlem
 * @version 1
 */

import greenfoot.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ToolPalette extends JFrame
{   
    static ToolPalette theTP = new ToolPalette();
    
    private WorldBuilder world;
    private File lastPath; 
    
    /**
     * Creates new form ToolPalette
     */
    public ToolPalette() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        widthSpinner = new javax.swing.JSpinner();
        heightSpinner = new javax.swing.JSpinner();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        problemButton = new javax.swing.JRadioButton();
        solutionButton = new javax.swing.JRadioButton();
        jSeparator3 = new javax.swing.JSeparator();
        saveButton = new javax.swing.JButton();
        loadButton = new javax.swing.JButton();
        generateButton = new javax.swing.JButton();

        setAlwaysOnTop(true);
        setResizable(false);

        jLabel1.setText("WorldBuilder Tools");

        jLabel2.setText("World Size:");

        jLabel3.setText("Width:");

        jLabel4.setText("Height:");

        widthSpinner.setModel(new javax.swing.SpinnerNumberModel(5, 1, 20, 1));
        widthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                widthSpinnerStateChanged(evt);
            }
        });

        heightSpinner.setModel(new javax.swing.SpinnerNumberModel(5, 1, 13, 1));
        heightSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                heightSpinnerStateChanged(evt);
            }
        });

        jLabel5.setText("Edit:");

        buttonGroup1.add(problemButton);
        problemButton.setSelected(true);
        problemButton.setText("Problem");
        problemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                problemButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(solutionButton);
        solutionButton.setText("Solution");
        solutionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                solutionButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save World...");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        loadButton.setText("Load World...");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        generateButton.setText("Generate Solution");
        generateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addComponent(jSeparator1)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(problemButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(solutionButton))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(widthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(heightSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(generateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(heightSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(widthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(problemButton)
                    .addComponent(solutionButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generateButton)
                .addGap(25, 25, 25)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadButton)
                .addContainerGap(182, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

    /**
     * handle the world width spinner being modified
     */
    private void widthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {                                          
        resizeWorld((Integer)widthSpinner.getValue(), (Integer)heightSpinner.getValue());
    }                                         

    /**
     * Handle the world height spinner being modified
     */
    private void heightSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {                                           
        resizeWorld((Integer)widthSpinner.getValue(), (Integer)heightSpinner.getValue());
    }                                          

    /**
     * Handle the save world button being pressed
     */
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // Allow the user to choose a file to save to
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("XML Files (*.xml)", "xml"));
        if (lastPath != null) {
            fc.setSelectedFile(lastPath);
        }
        int retrival = fc.showSaveDialog(null);
        
        // Tell the world builder to save the world
        if (retrival == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            world.saveWorldXML(file);
            
            lastPath = file;
        }
    }                                          

    /**
     * Handle the load world button being pressed
     */
    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // Allow the user to choose the file to load
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("XML Files (*.xml)", "xml"));
        if (lastPath != null) {
            fc.setSelectedFile(lastPath);
        }
        int retrival = fc.showOpenDialog(null);
        
        // Tell the worldbuilder to load the selected world
        if (retrival == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            world.loadWorldXML(file);
            
            lastPath = file;
        }
    }                                          

    /**
     * Handle the problem radio being selected
     */
    private void problemButtonActionPerformed(java.awt.event.ActionEvent evt) {                                              
        world.showProblem();
    }                                             
    
    /**
     * Handle the solution radio being selected
     */
    private void solutionButtonActionPerformed(java.awt.event.ActionEvent evt) {                                               
        world.showSolution();
    }                                              

    /**
     * Handle a generate button press to generate the problem
     * or solution
     */
    private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {                                               
        if (problemButton.isSelected()) {
            world.generateSolution();
            world.showSolution();
            solutionButton.setSelected(true);
        }
        else {
            world.generateProblem();
            world.showProblem();
            problemButton.setSelected(true);
        }
    }                                              
    
    /**
     * Indicate that the solution is currently being shown in
     * the world builder
     */
    public void solutionShown() {
        solutionButton.setSelected(true);
        generateButton.setText("Generate Problem");
    }
    
    /**
     * Indicate that the Problem is currently being shown in
     * the world builder
     */
    public void problemShown() {
        problemButton.setSelected(true);
        generateButton.setText("Generate Solution");
    }
    
    /**
     * Change the size of the world
     */
    public void resizeWorld(int width, int height) {
        world.resize(width, height);        
    }
    
    /**
     * Display the current size of the world
     */
    public void setWorldSize(int width, int height) {
        widthSpinner.setValue(width);
        heightSpinner.setValue(height);
    }
    
    /**
     * Set the world that is currently being edited
     */
    public void setWorld(WorldBuilder world) {
        this.world = world;
    }
    
    /**
     * Retrieve the active tool palette
     */
    public static ToolPalette getToolPalette() {
        return theTP;
    }
    
    // Variables declaration - do not modify                     
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton generateButton;
    private javax.swing.JSpinner heightSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton loadButton;
    private javax.swing.JRadioButton problemButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JRadioButton solutionButton;
    private javax.swing.JSpinner widthSpinner;
    // End of variables declaration                
}
