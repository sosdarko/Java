/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Component;
import java.awt.Cursor;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import oracle.jdbc.pool.OracleDataSource;
import javax.swing.JFileChooser;

/**
 *
 * @author darko.sos
 */
public class AppJFrame extends javax.swing.JFrame {

    AppData appData = new AppData();

    Branches branchesModel;
    Branch currentBranch = null;
    
    ArrayList<String> myUsers = new ArrayList<>();
    ArrayList<String> myUserPackages = new ArrayList<>();
    ArrayList<String> myUserPackageProcedures = new ArrayList<>();
    
    OracleDataSource myOds;
    Connection myConnection;
    /**
     * Creates new form AppJFrame
     */
    public AppJFrame() {
        appData.Load();
        branchesModel = new Branches(appData.getBranches());
        initComponents();
        jTextTNSNamesDir.setText(appData.getTnsNamesPath());
    }
    
    public void PopulateUsers()
    {
        Statement stmt = null;
        String query = "select username from all_users";

        myUsers.clear();
        jComboUser.removeAll();

        try {
            stmt = myConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String userName = rs.getString("username");
                myUsers.add(userName);
            }
        } catch (SQLException e ) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, e);
            return;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            }
        }
        
        Collections.sort(myUsers);
        DefaultComboBoxModel model = new DefaultComboBoxModel(myUsers.toArray());
        jComboUser.setModel(model);
    }
    
    public void PopulatePackages(String userName)
    {
        Statement stmt = null;
        String query =
                "select object_name from all_objects where owner = '"
                + userName + "' and object_type = 'PACKAGE'";

        myUserPackages.clear();
        jComboPackages.removeAll();

        try {
            if (myConnection.isClosed()) {
                Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, "Not connected");
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        try {
            stmt = myConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String objectName = rs.getString("object_name");
                myUserPackages.add(objectName);
                //jComboPackages.addItem(objectName);
            }
        } catch (SQLException e ) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        Collections.sort(myUserPackages);
        Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, String.format("Found %d packages", myUserPackages.size()));
        DefaultComboBoxModel model = new DefaultComboBoxModel(myUserPackages.toArray());
        jComboPackages.setModel(model);
    }

    public void PopulateProcedures(String userName, String pckName)
    {
        Statement stmt = null;
        String query =
                "select procedure_name from all_procedures where owner = '"
                + userName + "' and object_name = '" + pckName +"'"
                + "and subprogram_id > 0 order by 1";

        Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, query);
        myUserPackageProcedures.clear();
        jComboProcedures.removeAll();

        try {
            if (myConnection.isClosed()) {
                Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, "Not connected");
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        try {
            stmt = myConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String procedureName = rs.getString("procedure_name");
                myUserPackageProcedures.add(procedureName);
            }
        } catch (SQLException e ) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, String.format("Found %d procedures", myUserPackageProcedures.size()));
        Collections.sort(myUserPackageProcedures);
        DefaultComboBoxModel model = new DefaultComboBoxModel(myUserPackageProcedures.toArray());
        jComboProcedures.setModel(model);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBranches = new javax.swing.JComboBox();
        jButtonAddBranch = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButtonConnect = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextTNSNamesDir = new javax.swing.JTextField();
        jButtonChooseTNSDir = new javax.swing.JButton();
        jComboUser = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jComboPackages = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jComboProcedures = new javax.swing.JComboBox();
        jButtonRemoveBranch = new javax.swing.JButton();
        jButtonCreateCode = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextCode = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Generate C++ call from DB Call");
        setLocationByPlatform(true);
        setName("MainFrame"); // NOI18N
        setResizable(false);

        jComboBranches.setModel(branchesModel);
        jComboBranches.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBranchesActionPerformed(evt);
            }
        });

        jButtonAddBranch.setText("+");
        jButtonAddBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddBranchActionPerformed(evt);
            }
        });

        jLabel1.setText("Connection");

        jButtonConnect.setText("Connect");
        jButtonConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConnectActionPerformed(evt);
            }
        });

        jLabel2.setText("tnsnames dir");

        jButtonChooseTNSDir.setText(">");
        jButtonChooseTNSDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChooseTNSDirActionPerformed(evt);
            }
        });

        jComboUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboUserActionPerformed(evt);
            }
        });

        jLabel3.setText("User:");

        jComboPackages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboPackagesActionPerformed(evt);
            }
        });

        jLabel4.setText("Package:");

        jLabel5.setText("Proc/Func:");

        jButtonRemoveBranch.setText("-");
        jButtonRemoveBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveBranchActionPerformed(evt);
            }
        });

        jButtonCreateCode.setText("Create Code");
        jButtonCreateCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateCodeActionPerformed(evt);
            }
        });

        jTextCode.setColumns(20);
        jTextCode.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        jTextCode.setRows(5);
        jTextCode.setTabSize(4);
        jScrollPane1.setViewportView(jTextCode);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonConnect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextTNSNamesDir))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(jComboBranches, 0, 217, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButtonChooseTNSDir)
                                .addGap(38, 38, 38))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jButtonAddBranch)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonRemoveBranch, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jButtonCreateCode, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jComboUser, 0, 186, Short.MAX_VALUE)
                            .addComponent(jComboPackages, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboProcedures, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonAddBranch, jButtonRemoveBranch});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextTNSNamesDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonChooseTNSDir))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBranches, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddBranch)
                    .addComponent(jLabel1)
                    .addComponent(jButtonRemoveBranch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonConnect)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboPackages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboProcedures, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addComponent(jButtonCreateCode)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBranchesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBranchesActionPerformed
        Branch b = (Branch) jComboBranches.getSelectedItem();
        //Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, b.toString());
        currentBranch = b;
    }//GEN-LAST:event_jComboBranchesActionPerformed

    private void jButtonAddBranchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddBranchActionPerformed
        AddBranchJFrame bf = new AddBranchJFrame();
        bf.setLocationRelativeTo(this);
        bf.setModal(true);
        bf.setVisible(true);
        if (bf.bSuccess) {
            Branch b;
            String bname = bf.getBranchName();
            String user = bf.getUsername();
            String pass = bf.getPassword();
            String tns = bf.getTNSEntry();
            b = new Branch(bname, user, pass, tns);
            appData.addBranch(b);
            appData.Save();
            branchesModel = new Branches(appData.getBranches());
            jComboBranches.setSelectedItem(b);
            jComboBranches.repaint();
            currentBranch = b;
        }
    }//GEN-LAST:event_jButtonAddBranchActionPerformed

    private void jButtonConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConnectActionPerformed
        if (currentBranch == null)
            return;

        if (myConnection != null) {
            try {
                if (!myConnection.isClosed()) {
                    myConnection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            myOds = new OracleDataSource();
        } catch (SQLException ex) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        myOds.setTNSEntryName(currentBranch.getTnsEntry());
        myOds.setUser(currentBranch.getUserName());
        myOds.setPassword(currentBranch.getPassword());
        Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, "connecting using " + currentBranch.getConnString());
        myOds.setDriverType("oci");
        try {
            myConnection = myOds.getConnection();
            Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, "connected");
            PopulateUsers();
        } catch (SQLException ex) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonConnectActionPerformed

    private void jButtonChooseTNSDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChooseTNSDirActionPerformed
        JFileChooser fc = new JFileChooser("tnsnames.ora");
        fc.showOpenDialog(this);
        File f = fc.getSelectedFile();
        if (f != null) {
            String filepath = f.getAbsolutePath();
            jTextTNSNamesDir.setText(filepath);
            appData.setTnsNamesPath(filepath);
            appData.Save();
        }
    }//GEN-LAST:event_jButtonChooseTNSDirActionPerformed

    private void jComboUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboUserActionPerformed
        String user = jComboUser.getSelectedItem().toString();
        PopulatePackages(user);
    }//GEN-LAST:event_jComboUserActionPerformed

    private void jComboPackagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboPackagesActionPerformed
        String user = jComboUser.getSelectedItem().toString();
        String pck = jComboPackages.getSelectedItem().toString();
        PopulateProcedures(user, pck);
    }//GEN-LAST:event_jComboPackagesActionPerformed

    private void jButtonCreateCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateCodeActionPerformed
        String userName = jComboUser.getSelectedItem().toString();
        String pckName = jComboPackages.getSelectedItem().toString();
        String prcName = jComboProcedures.getSelectedItem().toString();
        if (userName == null || pckName == null || prcName == null)
            return;
        //
        String sIndent = "  ";

        String qProcedures =
                "select OBJECT_ID, SUBPROGRAM_ID from all_procedures where owner = ?"
                + " and object_name = ?"
                + " and procedure_name = ?";

        String qArguments =
                "    select" +
                "      argument_name, in_out, pls_type," +
                "      count(*) over (partition by owner) cnt," +
                "      max(length(argument_name)) over (partition by owner) mlen" +
                "    from all_arguments" +
                "    where" +
                "      object_id = ? and" +
                "      subprogram_id = ?" +
                "    order by position";
        //
        ArrayList<PLSProcedureArgument> procArguments = new ArrayList<>();
        //
        PreparedStatement stmProcedures = null;
        PreparedStatement stmArguments = null;
        // check connection
        try {
            if (myConnection.isClosed()) {
                Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, "Not connected");
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        // execute query
        try {
            stmProcedures = myConnection.prepareStatement(qProcedures);
            stmArguments = myConnection.prepareStatement(qArguments);
            //
            stmProcedures.setString(1, userName);
            stmProcedures.setString(2, pckName);
            stmProcedures.setString(3, prcName);
            ResultSet rs = stmProcedures.executeQuery();
            ResultSet rsa = null;
            boolean bFirst;
            int maxLen=0;
            while (rs.next()) {
                int OBJECT_ID = rs.getInt("OBJECT_ID");
                int SUBPROGRAM_ID = rs.getInt("SUBPROGRAM_ID");
                jTextCode.append(String.format("/* %d %d  */\n", OBJECT_ID, SUBPROGRAM_ID));
                jTextCode.append("bool " + prcName + "(\n");
                jTextCode.append(sIndent + "CFinRRDbConnection* pConn,\n\n");
                // get arguments
                stmArguments.setInt(1, OBJECT_ID);
                stmArguments.setInt(2, SUBPROGRAM_ID);
                rsa = stmArguments.executeQuery();
                // fill list
                while (rsa.next()) {
                    procArguments.add(new PLSProcedureArgument(rsa.getString(1), rsa.getString(2)));
                    maxLen = rsa.getInt("mlen");
                }
                //
                bFirst = true;
                for (PLSProcedureArgument p : procArguments) {
                    if (bFirst)
                        jTextCode.append(sIndent + "CFinVarParam& " + p.getName());
                    else
                        jTextCode.append(",\n" + sIndent + "CFinVarParam& " + p.getName());
                    bFirst = false;
                }
                jTextCode.append(
                        "\n)\n{\n" + sIndent + "SP_INIT(\"" + userName + "." + pckName + "." + prcName + "\");\n\n");
                //
                for (PLSProcedureArgument p : procArguments) {
                    jTextCode.append(sIndent + "SP_ADDPARAM(" + String.format("%1$-" + (maxLen+1) + "s", p.getName()) + ", " + p.getCppType() + ");\n");
                }
                jTextCode.append("\n" + sIndent + "SP_EXECUTE(pConn);\n\n");
                // out params
                for (PLSProcedureArgument p : procArguments) {
                    if ("OUT".equals(p.getType()) || "IN/OUT".equals(p.getType()))
                        jTextCode.append(sIndent + "SP_GETPARAM(" + p.getName() + ");\n");
                }
                jTextCode.append("\n" + sIndent + "return (0==(long)Success);\n}\n");
            }
        } catch (SQLException e ) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            if (stmProcedures != null) {
                try {
                    stmProcedures.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (stmArguments != null) {
                try {
                    stmArguments.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_jButtonCreateCodeActionPerformed

    private void jButtonRemoveBranchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveBranchActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        boolean delBranch = appData.delBranch(currentBranch);
        if (delBranch)
            branchesModel = new Branches(appData.getBranches());
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        appData.Save();
    }//GEN-LAST:event_jButtonRemoveBranchActionPerformed

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AppJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AppJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AppJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AppJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AppJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddBranch;
    private javax.swing.JButton jButtonChooseTNSDir;
    private javax.swing.JButton jButtonConnect;
    private javax.swing.JButton jButtonCreateCode;
    private javax.swing.JButton jButtonRemoveBranch;
    private javax.swing.JComboBox jComboBranches;
    private javax.swing.JComboBox jComboPackages;
    private javax.swing.JComboBox jComboProcedures;
    private javax.swing.JComboBox jComboUser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextCode;
    private javax.swing.JTextField jTextTNSNamesDir;
    // End of variables declaration//GEN-END:variables
} 
