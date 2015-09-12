/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
//import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author darko.sos
 */
public class MainJFrame extends javax.swing.JFrame {

    AppData appData = new AppData();
    DBRelease release = new DBRelease();

    String sDescription = "";
    String sBranchName = "";
    String sMain = "";
    String sFinID = "xxxx.xx";
    String sReleaseNumber = "xxxx.xx.xx.XX";

    DefaultListModel<Script> scriptListModel = new DefaultListModel();

    /**
     * Creates new form MainJFrame
     */
    public MainJFrame() {
        initComponents();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        jTextFieldYear.setText(String.format("%d", year));
        LoadAppData();
        getContentPane().setBackground(new Color(162, 191, 133));
    }

    void UpdateListModel()
    {
        scriptListModel.clear();
        for (Script rs : release.getReleaseScripts()) {
            scriptListModel.addElement(rs);
        }
    }

    void SetStatus(String s)
    {
        jTextStatus.setText(s);
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
             SetStatus(cnfe.getMessage());
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
           SetStatus(ioe.getMessage());
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
                Object readObj = ois.readObject();
                release = (DBRelease) readObj;
                ois.close();
                fis.close();
            }
            catch(IOException ioe) {
                System.out.println("File read exception:" + ioe.getMessage());
                SetStatus(ioe.getMessage());
            }
            catch(ClassNotFoundException cnfe) {
                System.out.println("Class not found");
                SetStatus(cnfe.getMessage());
            }
        }
        else {
            System.out.println("No Selection ");
            SetStatus("No Selection");
        }
        
        ShowReleaseOnDialog();
    }

    public void SaveRelease()
    {
        ReadReleaseFromDialog();
        String sName = release.toString();
        if ("".equals(sName) || sName == null)
        {
            JOptionPane.showMessageDialog(this, "The name of the main file is empty");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(sName + ".rls");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(release);
            oos.close();
            fos.close();
        }
        catch(IOException ioe){
            System.out.println(ioe.toString());
            SetStatus(ioe.getMessage());
        }
    }

    void ReadReleaseFromDialog()
    {
        release.setReleaseNumber(sReleaseNumber);
        release.setFinbetRelNo(sFinID);
        release.setMainName(sMain);
    }

    void ShowReleaseOnDialog()
    {
        sFinID = release.getFinbetRelNo();
        sReleaseNumber = release.getReleaseNumber();
        sDescription = release.getDescription();
        sMain = release.getMainName();
        //
        jTextFieldReleaseNumber.setText(sReleaseNumber);
        jTextFieldFinID.setText(sFinID);
        jTextFieldDescription.setText(sDescription);
        jTextFieldMain.setText(sMain);
        
        UpdateListModel();

        DeriveBranchName();
    }

    void DeriveReleaseText()
    {
        sMain = "main_" + sReleaseNumber + ".sql";
        jTextFieldMain.setText(sMain);
    }

    void DeriveBranchName()
    {
        sBranchName = appData.getCurrentAlterNumber().toString() + "xx - " + sFinID + " - " + sReleaseNumber + " - " + sDescription;
        jTextFieldBranchName.setText(sBranchName);
    }
    /*
    void AddScript()
    {
        JDialogAddScript addscr = new JDialogAddScript(this, true);

        addscr.setLocationRelativeTo(this);
        addscr.setVisible(true);

        if (addscr.bSuccess) {
            String s = addscr.sScriptName;
            // create subfolder "scripts" if it not exists
            File dir = new File("./scripts");
            if (!dir.exists()) {
                dir.mkdir();
            }
            File f = new File("./scripts/" + s);
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                bw.write(addscr.sScriptText);
                bw.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            release.AddFile(f);
            scriptListModel.addElement(s);
        }
    }
    */
    void AddScript()
    {
        JDialogAddScript addscr = new JDialogAddScript(this, true);

        addscr.setLocationRelativeTo(this);
        addscr.setVisible(true);

        if (addscr.bSuccess) {
            Script script = new Script(addscr.scr.getName(), addscr.scr.getContent());
            release.AddScript(script);
            scriptListModel.addElement(script);
        }
    }

    void EditScript(int i)
    {
        Script scr = release.getScript(i);
        JDialogAddScript addscr = new JDialogAddScript(this, true, scr);
        addscr.setLocationRelativeTo(this);
        addscr.setVisible(true);

        if (addscr.bSuccess) {
            release.ReplaceScript(i, scr);
            scriptListModel.set(i, scr);
        }
    }
    
    void RemoveScript(int i)
    {
        if (i<0)
            return;
        int n = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exclude this file?",
                "Please Confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (n == JOptionPane.YES_OPTION) {
            release.RemoveScript(i);
            
            UpdateListModel();
        }
    }
    
    boolean MoveScriptUp(int i)
    {
        if (i<=0 || i>=scriptListModel.size())
            return false;

        release.SwapScripts(i, i-1);

        UpdateListModel();

        return true;
    }
    
    boolean MoveScriptDown(int i)
    {
        if (i<0 || i>=scriptListModel.size()-1)
            return false;

        release.SwapScripts(i, i+1);

        UpdateListModel();

        return true;
    }

    void CreateMain()
    {
        BufferedReader reader;
        try {
            reader = Files.newBufferedReader(Paths.get("main_template.sql"));
        }
        catch(IOException ioe) {
            System.out.println("File main_template.sql not found");
            SetStatus(ioe.getMessage());
            return;
        }

        BufferedWriter writer;
        try {
            writer = Files.newBufferedWriter(Paths.get(sMain));
        }
        catch(IOException ioe) {
            System.out.println("Could not create main file");
            SetStatus(ioe.getMessage());
            return;
        }

        if (reader != null) {
            //sMainTemplate = String.format(sMainTemplate, sFinID, sReleaseNumber, sDescription);
            String inline;
            try {
                while ((inline = reader.readLine()) != null) {
                    if (inline.startsWith("DEFINE fin_id=")) {
                        writer.write("DEFINE fin_id='".concat(sFinID).concat("'"));
                        writer.newLine();
                    }
                    else if (inline.startsWith("DEFINE release_number=")) {
                        writer.write("DEFINE release_number='".concat(sReleaseNumber).concat("'"));
                        writer.newLine();
                    }
                    else if (inline.startsWith("DEFINE release_description=")) {
                        writer.write("DEFINE release_description='".concat(sDescription).concat("'"));
                        writer.newLine();
                    }
                    else if (inline.startsWith("REM release section")) {
                        for (Script s : release.getReleaseScripts()) {
                           writer.write("@".concat(s.getName()));
                           writer.newLine();
                        }
                    }
                    else {
                        writer.write(inline);
                        writer.newLine();
                    }
                }
            } catch (IOException ex) {
                SetStatus(ex.getMessage());
            }
            // close files
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        jTextStatus = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButtonEditFile = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemLoad = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
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

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel1.setText("Year");

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel2.setText("Finbet ID");

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
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

        jLabel4.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel4.setText("Description");

        jTextFieldDescription.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDescriptionActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel5.setText("Main release name");

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel6.setText("Branch name");

        jListFiles.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        jListFiles.setModel(scriptListModel);
        jListFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListFiles.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListFilesKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(jListFiles);

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
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

        jButtonAddFile.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButtonAddFile.setText("+");
        jButtonAddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddFileActionPerformed(evt);
            }
        });

        jButtonRemoveFile.setText("-");
        jButtonRemoveFile.setActionCommand("Remove");
        jButtonRemoveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveFileActionPerformed(evt);
            }
        });

        jButtonFileUp.setText("Up");
        jButtonFileUp.setFocusable(false);
        jButtonFileUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFileUpActionPerformed(evt);
            }
        });

        jButtonFileDown.setText("Down");
        jButtonFileDown.setFocusable(false);
        jButtonFileDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFileDownActionPerformed(evt);
            }
        });

        jTextStatus.setText("status");

        jButton1.setText("Create");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButtonEditFile.setText("Edit");
        jButtonEditFile.setFocusable(false);
        jButtonEditFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditFileActionPerformed(evt);
            }
        });

        jMenu1.setText("File");

        jMenuItemLoad.setText("Load");
        jMenuItemLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemLoad);

        jMenuItem1.setText("Save");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

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
                    .addComponent(jTextStatus)
                    .addComponent(jTextFieldMain)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonFileUp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonAddFile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonRemoveFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonFileDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonEditFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                .addGap(61, 61, 61)
                                .addComponent(jButtonLoadRelease)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonSaveRelease))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel7)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel5)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton1))
                                .addComponent(jLabel4)
                                .addComponent(jTextFieldBranchName)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel6)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 350, Short.MAX_VALUE)
                                    .addComponent(jButtonCreateReleaseFolder))
                                .addComponent(jTextFieldDescription)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldFinID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextFieldReleaseNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonSaveRelease)
                        .addComponent(jButtonLoadRelease))
                    .addComponent(jTextFieldYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonCreateReleaseFolder)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldBranchName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonAddFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonEditFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonFileUp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonFileDown))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        sDescription = jTextFieldDescription.getText();
        release.setDescription(sDescription);
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
            int i = jListFiles.getSelectedIndex();
            RemoveScript(i);
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
        int i = jListFiles.getSelectedIndex();
        boolean moved = MoveScriptUp(i);
        if (moved)
            jListFiles.setSelectedIndex(i-1);
    }//GEN-LAST:event_jButtonFileUpActionPerformed

    private void jButtonFileDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFileDownActionPerformed
        int i = jListFiles.getSelectedIndex();
        boolean moved = MoveScriptDown(i);
        if (moved)
            jListFiles.setSelectedIndex(i+1);
    }//GEN-LAST:event_jButtonFileDownActionPerformed

    private void jMenuItemLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadActionPerformed
        LoadRelease();
    }//GEN-LAST:event_jMenuItemLoadActionPerformed

    private void jButtonRemoveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveFileActionPerformed
        int i = jListFiles.getSelectedIndex();
        RemoveScript(i);
    }//GEN-LAST:event_jButtonRemoveFileActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        SaveRelease();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jButtonEditFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditFileActionPerformed
        int i = jListFiles.getSelectedIndex();
        if (i>=0)
            EditScript(i);
    }//GEN-LAST:event_jButtonEditFileActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        CreateMain();
    }//GEN-LAST:event_jButton1ActionPerformed

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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAddFile;
    private javax.swing.JButton jButtonCreateReleaseFolder;
    private javax.swing.JButton jButtonEditFile;
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
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItemLoad;
    private javax.swing.JMenuItem jMenuItemSettings;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldBranchName;
    private javax.swing.JTextField jTextFieldDescription;
    private javax.swing.JTextField jTextFieldFinID;
    private javax.swing.JTextField jTextFieldMain;
    private javax.swing.JTextField jTextFieldReleaseNumber;
    private javax.swing.JTextField jTextFieldYear;
    private javax.swing.JTextField jTextStatus;
    // End of variables declaration//GEN-END:variables
}
