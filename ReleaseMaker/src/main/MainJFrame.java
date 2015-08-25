/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author darko.sos
 */
public class MainJFrame extends javax.swing.JFrame {

    AppData appData = new AppData();
    DBRelease release = new DBRelease();

    String sDescritption = "";
    String sBranchName = "";
    String sMain = "";
    String sFinID = "xxxx.xx";
    String sReleaseNumber = "xxxx.xx.xx.XX";

    ListModel scriptListModel = new DefaultListModel();

    /**
     * Creates new form MainJFrame
     */
    public MainJFrame() {
        initComponents();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        jTextFieldYear.setText(String.format("%d", year));
        LoadAppData();
    }

    final void LoadAppData()
    {
        try {
            FileInputStream fis = new FileInputStream("appdata.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            appData = (AppData) ois.readObject();
            ois.close();
            fis.close();
        }
        catch(IOException ioe) {
             System.out.println("File appdata.txt not found");
        }
        catch(ClassNotFoundException cnfe) {
             System.out.println("Class not found");
             cnfe.printStackTrace();
        }
    }

    final void SaveAppData()
    {
       try {
         FileOutputStream fos= new FileOutputStream("appdata.txt");
         ObjectOutputStream oos= new ObjectOutputStream(fos);
         oos.writeObject(appData);
         oos.close();
         fos.close();
       }
       catch(IOException ioe){
            ioe.printStackTrace();
       }
    }

    public void LoadRelease()
    {
        JFileChooser chooser = new JFileChooser(); 
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Select release");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("Release files", "rls"));
        //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //chooser.setAcceptAllFileFilterUsed(false);
        //    
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
            try {
                FileInputStream fis = new FileInputStream(chooser.getSelectedFile());
                ObjectInputStream ois = new ObjectInputStream(fis);
                release = (DBRelease) ois.readObject();
                ois.close();
                fis.close();
            }
            catch(IOException ioe) {
                 System.out.println("File not found");
            }
            catch(ClassNotFoundException cnfe) {
                 System.out.println("Class not found");
                 cnfe.printStackTrace();
            }
        }
        else {
            System.out.println("No Selection ");
        }
    }

    public void SaveRelease()
    {
        String sName = release.toString();
        if ("".equals(sName) || sName == null)
        {
            JOptionPane.showMessageDialog(this, "The name of the main file is empty");
            return;
        }
        try {
            FileOutputStream fos= new FileOutputStream(sName + ".rls");
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(release);
            oos.close();
            fos.close();
       }
       catch(IOException ioe){
            ioe.printStackTrace();
       }
    }

    void ShowRelease()
    {
        jTextFieldReleaseNumber.setText(release.getReleaseNumber());
    }
    
    void DeriveReleaseText()
    {
        sMain = "main_" + sReleaseNumber + ".sql";
        jTextFieldMain.setText(sMain);
        release.setMainName(sMain);
    }

    void DeriveBranchName()
    {
        sBranchName = appData.getCurrentAlterNumber().toString() + "xx - " + sFinID + " - " + sReleaseNumber + " - " + sDescritption;
        jTextFieldBranchName.setText(sBranchName);
    }
    
    void AddScript()
    {
        JDialogAddScript addscr = new JDialogAddScript(this, true);

        addscr.setLocationRelativeTo(this);
        addscr.setVisible(true);

        if (addscr.bSuccess) {
            String s = addscr.sScriptName;
            File f = new File("/scripts/" + s);
            ;
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextFieldYear = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldFinID = new javax.swing.JTextField();
        jTextFieldReleaseNumber = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldDescription = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldMain = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldBranchName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListFiles = new javax.swing.JList();
        jLabel7 = new javax.swing.JLabel();
        jButtonCreateReleaseFolder = new javax.swing.JButton();
        jButtonLoadRelease = new javax.swing.JButton();
        jButtonSaveRelease = new javax.swing.JButton();
        jButtonAddFile = new javax.swing.JButton();
        jButtonRemoveFile = new javax.swing.JButton();
        jButtonFileUp = new javax.swing.JButton();
        jButtonFileDown = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItemSettings = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Release Maker");
        setLocationByPlatform(true);
        setResizable(false);

        jTextFieldYear.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldYearActionPerformed(evt);
            }
        });

        jLabel1.setText("Year");

        jLabel2.setText("Finbet ID");

        jLabel3.setText("Release no.");

        jTextFieldFinID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFinIDActionPerformed(evt);
            }
        });

        jTextFieldReleaseNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldReleaseNumberActionPerformed(evt);
            }
        });

        jLabel4.setText("Description");

        jTextFieldDescription.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDescriptionActionPerformed(evt);
            }
        });

        jLabel5.setText("Main release name");

        jLabel6.setText("Branch name");

        jListFiles.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        jListFiles.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", " " };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListFiles.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListFilesKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(jListFiles);

        jLabel7.setText("Script list");

        jButtonCreateReleaseFolder.setText("Create");
        jButtonCreateReleaseFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateReleaseFolderActionPerformed(evt);
            }
        });

        jButtonLoadRelease.setText("Load");
        jButtonLoadRelease.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadReleaseActionPerformed(evt);
            }
        });

        jButtonSaveRelease.setText("Save");
        jButtonSaveRelease.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveReleaseActionPerformed(evt);
            }
        });

        jButtonAddFile.setBackground(new java.awt.Color(153, 255, 153));
        jButtonAddFile.setText("+");
        jButtonAddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddFileActionPerformed(evt);
            }
        });

        jButtonRemoveFile.setBackground(new java.awt.Color(255, 153, 153));
        jButtonRemoveFile.setText("-");
        jButtonRemoveFile.setActionCommand("Remove");

        jButtonFileUp.setText("Up");
        jButtonFileUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFileUpActionPerformed(evt);
            }
        });

        jButtonFileDown.setText("Dw");
        jButtonFileDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFileDownActionPerformed(evt);
            }
        });

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenuItemSettings.setText("Settings");
        jMenuItemSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSettingsActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemSettings);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldMain)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldYear, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jTextFieldFinID, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jTextFieldReleaseNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonLoadRelease)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSaveRelease)
                        .addGap(45, 45, 45))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButtonFileUp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButtonAddFile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButtonRemoveFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButtonFileDown)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel4)
                                    .addComponent(jTextFieldBranchName)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 350, Short.MAX_VALUE)
                                        .addComponent(jButtonCreateReleaseFolder))
                                    .addComponent(jTextFieldDescription))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextFieldYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldFinID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldReleaseNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonLoadRelease)
                        .addComponent(jButtonSaveRelease)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jButtonCreateReleaseFolder))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldBranchName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonAddFile)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonFileUp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonFileDown)
                        .addGap(22, 22, 22)
                        .addComponent(jButtonRemoveFile))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldYearActionPerformed
        DeriveReleaseText();
    }//GEN-LAST:event_jTextFieldYearActionPerformed

    private void jTextFieldFinIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFinIDActionPerformed
        sFinID = jTextFieldFinID.getText();
        DeriveReleaseText();
        DeriveBranchName();
    }//GEN-LAST:event_jTextFieldFinIDActionPerformed

    private void jTextFieldDescriptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDescriptionActionPerformed
        sDescritption = jTextFieldDescription.getText();
        DeriveBranchName();
    }//GEN-LAST:event_jTextFieldDescriptionActionPerformed

    private void jTextFieldReleaseNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldReleaseNumberActionPerformed
        sReleaseNumber = jTextFieldReleaseNumber.getText();
        DeriveReleaseText();
        DeriveBranchName();
    }//GEN-LAST:event_jTextFieldReleaseNumberActionPerformed

    private void jMenuItemSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSettingsActionPerformed
        AppDataJDialog d = new AppDataJDialog(this, true, appData);
        
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        
        if (d.bSuccess) {
            appData.CopyFrom(d.getAppData());
            System.out.println("saving: " + appData.toString());
            SaveAppData();
        }
    }//GEN-LAST:event_jMenuItemSettingsActionPerformed

    private void jButtonCreateReleaseFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateReleaseFolderActionPerformed
        release.setTfsAlterBranch(appData.getCurrentDBAlterNode());
        release.MakeReleaseFolder(sBranchName);
    }//GEN-LAST:event_jButtonCreateReleaseFolderActionPerformed

    private void jListFilesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListFilesKeyTyped
        if (evt.getKeyChar()== KeyEvent.VK_DELETE) {
            int n = JOptionPane.showConfirmDialog(this, "Are you sure you want to exclude this file?", "Please Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (n == JOptionPane.YES_OPTION) {
                ;
            }
        }
    }//GEN-LAST:event_jListFilesKeyTyped

    private void jButtonSaveReleaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveReleaseActionPerformed
        SaveRelease();
    }//GEN-LAST:event_jButtonSaveReleaseActionPerformed

    private void jButtonLoadReleaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadReleaseActionPerformed
        LoadRelease();
    }//GEN-LAST:event_jButtonLoadReleaseActionPerformed

    private void jButtonAddFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddFileActionPerformed
        AddScript();
    }//GEN-LAST:event_jButtonAddFileActionPerformed

    private void jButtonFileUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFileUpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonFileUpActionPerformed

    private void jButtonFileDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFileDownActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonFileDownActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddFile;
    private javax.swing.JButton jButtonCreateReleaseFolder;
    private javax.swing.JButton jButtonFileDown;
    private javax.swing.JButton jButtonFileUp;
    private javax.swing.JButton jButtonLoadRelease;
    private javax.swing.JButton jButtonRemoveFile;
    private javax.swing.JButton jButtonSaveRelease;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList jListFiles;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItemSettings;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldBranchName;
    private javax.swing.JTextField jTextFieldDescription;
    private javax.swing.JTextField jTextFieldFinID;
    private javax.swing.JTextField jTextFieldMain;
    private javax.swing.JTextField jTextFieldReleaseNumber;
    private javax.swing.JTextField jTextFieldYear;
    // End of variables declaration//GEN-END:variables
}
