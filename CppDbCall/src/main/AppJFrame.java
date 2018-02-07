/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import oracle.jdbc.pool.OracleDataSource;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

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

    String sIndent = "  ";
    boolean bIsPackage;
    boolean bParseSource;
    private String sCodeType;

    private class DBProcedure {
        public int OBJECT_ID;
        public int SUBPROGRAM_ID;
        public String userName;
        public String pckName;
        public String prcName;
        public ArrayList<PLSProcedureArgument> Arguments;
        public int maxLen;
        @Override
        public String toString() {
            if ("<standalone>".equals(pckName))
                return userName + "." + prcName;
            else
                return userName + "." + pckName + "." + prcName;
        }
        public int IsFunction;
        public String procType;
    }

    private static final String qPCKProcs =
        "select OBJECT_ID, SUBPROGRAM_ID, OBJECT_TYPE from all_procedures where owner = ?" +
        " and object_name = ?" +
        " and procedure_name = ?";
    private static final String qSTAProcs =
        "select OBJECT_ID, SUBPROGRAM_ID, OBJECT_TYPE from all_procedures where owner = ?" +
        " and object_name = ? and object_type in ('PROCEDURE', 'FUNCTION')";
    private static final String qArguments =
        "    select" +
        "      argument_name, in_out, data_type," +
        "      count(*) over (partition by owner) cnt," +
        "      max(length(argument_name)) over (partition by owner) mlen" +
        "    from all_arguments" +
        "    where" +
        "      object_id = ? and" +
        "      subprogram_id = ?" +
        "    order by position";
    private static final String qSource =
        "select line, text from all_source where type = ? and owner = ? and name = ? order by 1";
    /**
     * Creates new form AppJFrame
     */
    public AppJFrame() {
        appData.Load();
        branchesModel = new Branches(appData.getBranches());
        initComponents();
        if (branchesModel.getSize() > 0)
            jComboBranches.setSelectedIndex(0);
        jTextTNSNamesDir.setText(appData.getTnsNamesPath());
        jRadioCpp.setSelected(true);
        sCodeType = "C++";
        jCheckFilterUsers.setSelected(false);
        bParseSource = true;
        jCheckParseSource.setSelected(bParseSource);
    }

    public void ShowError(String text)
    {
        JOptionPane.showMessageDialog(this, text);
    }

    public void PopulateUsers()
    {
        Statement stmt = null;
        String query = "select username from all_users";

        if (jCheckFilterUsers.isSelected()) {
            query += "\n where username in (select owner from all_procedures where OBJECT_TYPE in ('PROCEDURE', 'FUNCTION', 'PACKAGE'))";
        }

        myUsers.clear();
        jComboUser.removeAll();

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
        this.setCursor(Cursor.getDefaultCursor());
        //
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
            myUserPackages.add("<standalone>");
            while (rs.next()) {
                String objectName = rs.getString("object_name");
                myUserPackages.add(objectName);
                //jComboPackages.addItem(objectName);
            }
        }
        catch (SQLException e ) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, e);
        }
        finally {
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
        String query;
        if (!this.bIsPackage) {
            query =
                "select object_name procedure_name from all_procedures where owner = '" + userName
                + "' and object_type in ('PROCEDURE', 'FUNCTION') order by 1";
        }
        else {
            query =
                "select procedure_name from all_procedures where owner = '"
                + userName + "' and object_name = '" + pckName +"'"
                + "and subprogram_id > 0 order by 1";
        }

        Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, query);
        myUserPackageProcedures.clear();
        jComboProcedures.removeAll();
        // test connection
        try {
            if (myConnection.isClosed()) {
                Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, "Not connected");
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        // execute query
        Statement stmt = null;
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

    private void EditBranch()
    {
        AddBranchJFrame bf = new AddBranchJFrame();
        bf.setLocationRelativeTo(this);
        bf.setModal(true);
        // fill dialog fields
        bf.setBranchName(currentBranch.getName());
        bf.setUsername(currentBranch.getUserName());
        bf.setPassword(currentBranch.getPassword());
        bf.setTNSEntry(currentBranch.getTnsEntry());
        bf.setURL(currentBranch.getURL());
        //
        bf.setVisible(true);
        if (bf.bSuccess) {
            Branch b;
            String bname = bf.getBranchName();
            String user = bf.getUsername();
            String pass = bf.getPassword();
            String tns = bf.getTNSEntry();
            String url = bf.getURL();
            b = new Branch(bname, user, pass, tns, url);
            appData.copyBranch(currentBranch, b);
            appData.Save();
        }
    }

    public void AppendCode(String sCode, int nIndentLevel)
    {
        if (nIndentLevel > 0) {
            for (int i=1; i<=nIndentLevel; i++)
                jTextCode.append(sIndent);
        }
        jTextCode.append(sCode);
    }
    
    public void AppendCode(String sCode)
    {
        AppendCode(sCode, 0);
    }

    private void CopyCode2Clipboard()
    {
        String myString = jTextCode.getText();
        StringSelection stringSelection = new StringSelection(myString);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }

    private int CountPattern(String sLine, String sPattern)
    {
        Pattern p = Pattern.compile(sPattern);
        Matcher m = p.matcher(sLine);
        int count = 0;
        while (m.find()){
            count +=1;
        }

        return count;
    }
    
    private ArrayList<String> GetSourceArguments(ArrayList<String> lSource, int nSubprogram)
    {
        ArrayList<String> args = null;

        int c = 0;
        int i = 0;
        int j;
        int k;
        int cntStartComment = 0;
        int cntEndComment = 0;
        String src;
        String line;
        System.out.printf("Looking for subprogram_id=%d\n", nSubprogram);
        while (i < lSource.size() && c < nSubprogram) {
            i += 1;
            line = lSource.get(i).trim().toUpperCase();
            cntStartComment += CountPattern(line, "\\/\\*"); // /*
            cntEndComment += CountPattern(line, "\\*\\/"); // */
            if (cntStartComment != cntEndComment) {
                System.out.printf("Comment on line %d: ", i);
                System.out.print(line + "\n");
                continue;
            }
            if (line.startsWith("PROCEDURE") || line.startsWith("FUNCTION")) {
                System.out.print("Found: " + line + "\n");
                c += 1;
            }
        }
        if (c == nSubprogram) {
            j = i+1;
            while (j < lSource.size()) {
                line = lSource.get(j).trim().toUpperCase();
                if (line.startsWith("PROCEDURE") || line.startsWith("FUNCTION")) {
                    break;
                }
                j += 1;
            }
            src = "";
            for (k=i; k<j; k++) {
                if (!lSource.get(k).trim().startsWith("--")) {
                    src += lSource.get(k).trim() + " ";
                }
            }
            args = new ArrayList<>(Arrays.asList(src.split(",")));
            j = args.get(0).indexOf('(');
            if (j>0 && j<args.get(0).length()) {
                args.set(0, args.get(0).substring(j+1));
            }
            j = args.get(args.size()-1).indexOf(')');
            if (j>0 && j<args.get(args.size()-1).length()) {
                args.set(args.size()-1, args.get(args.size()-1).substring(1, j));
            }
            for (i=0; i<args.size(); i++) {
                args.set(i, args.get(i).trim().split(" ")[0]);
                //System.out.print(args.get(i));
            }
        }

        return args;
    }
    
    private boolean GetProceduresFromDB(ArrayList<DBProcedure> pProcedures)
    {
        if (jComboUser.getSelectedItem() == null || jComboPackages.getSelectedItem() == null || jComboProcedures.getSelectedItem() == null) {
            ShowError("Please choose procedure/function");
            return false;
        }
        String userName = jComboUser.getSelectedItem().toString();
        String pckName = jComboPackages.getSelectedItem().toString();
        String prcName = jComboProcedures.getSelectedItem().toString();
        if (userName == null || pckName == null || prcName == null)
            return false;
        //
        pProcedures.clear();
        String qProcedures;
        if (this.bIsPackage)
            qProcedures = AppJFrame.qPCKProcs;
        else
            qProcedures = AppJFrame.qSTAProcs;
        PreparedStatement stmProcedures = null;
        PreparedStatement stmArguments = null;
        PreparedStatement stmSource = null;
        int c = 0;
        ArrayList<String> srcArgs = null;
        // check connection
        try {
            if (myConnection.isClosed()) {
                Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, "Not connected");
                ShowError("Not connected to DB");
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
            ShowError(ex.getMessage());
            return false;
        }
        try {
            stmProcedures = myConnection.prepareStatement(qProcedures);
            stmArguments = myConnection.prepareStatement(AppJFrame.qArguments);
            //
            if (this.bIsPackage) {
                stmProcedures.setString(1, userName);
                stmProcedures.setString(2, pckName);
                stmProcedures.setString(3, prcName);
            }
            else {
                stmProcedures.setString(1, userName);
                stmProcedures.setString(2, prcName);
            }
            ResultSet rs = stmProcedures.executeQuery();
            ResultSet rsa;
            String argName;
            while (rs.next()) {
                DBProcedure dbp = new DBProcedure();
                dbp.OBJECT_ID = rs.getInt("OBJECT_ID");
                dbp.SUBPROGRAM_ID = rs.getInt("SUBPROGRAM_ID");
                dbp.userName = userName;
                dbp.pckName = pckName;
                dbp.prcName = prcName;
                if (!this.bIsPackage)
                    dbp.procType = rs.getString("OBJECT_TYPE");
                else
                    // this can chage later if argument without name is found
                    dbp.procType = "PROCEDURE";
                //
                if (this.bParseSource) {
                    stmSource = myConnection.prepareStatement(AppJFrame.qSource);
                    stmSource.setString(1, "PACKAGE");
                    stmSource.setString(2, userName);
                    stmSource.setString(3, pckName);
                    ResultSet rss;
                    rss = stmSource.executeQuery();
                    ArrayList<String> src = new ArrayList<>();
                    while(rss.next()) {
                        src.add(rss.getString(2));
                    }
                    srcArgs = GetSourceArguments(src, dbp.SUBPROGRAM_ID);
                }
                //
                stmArguments.setInt(1, dbp.OBJECT_ID);
                stmArguments.setInt(2, dbp.SUBPROGRAM_ID);
                //System.out.print(String.format("Query executed: %s", AppJFrame.qArguments));
                rsa = stmArguments.executeQuery();
                // fill list
                dbp.Arguments = new ArrayList<>();
                c = 0;
                if (srcArgs != null) {
                    System.out.print(String.format("SrcArgs.length=%d", srcArgs.size()));
                }
                while (rsa.next()) {
                    argName = rsa.getString(1);
                    if (argName != null) {
                    if (srcArgs != null && c < srcArgs.size()) {
                        if (argName.equals(srcArgs.get(c).toUpperCase()))
                            argName = srcArgs.get(c);
                    }
                    else
                        dbp.procType = "FUNCTION";
                    dbp.Arguments.add(new PLSProcedureArgument(argName, rsa.getString(2), rsa.getString(3)));
                    dbp.maxLen = rsa.getInt("mlen");
                    }
                    c += 1;
                }
                pProcedures.add(dbp);
            }
        }
        catch (SQLException e ) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, e);
            ShowError(e.getMessage());
            return false;
        } finally {
            if (stmProcedures != null) {
                try {
                    stmProcedures.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    ShowError(ex.getMessage());
                    return false;
                }
            }
            if (stmArguments != null) {
                try {
                    stmArguments.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    ShowError(ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Generate C++ code
     */
    public void GenerateCPPCode2()
    {
        ArrayList<DBProcedure> lProcList = new ArrayList<>();
        boolean bSuccess = GetProceduresFromDB(lProcList);
        if (!bSuccess)
            return;
        jTextCode.setText("");
        for (DBProcedure proc : lProcList) {
            jTextCode.append(String.format("/* %d %d  */\n", proc.OBJECT_ID, proc.SUBPROGRAM_ID));
            jTextCode.append("bool " + proc.prcName + "(\n");
            jTextCode.append(sIndent + "CFinRRDbConnection* pConn,\n\n");
            boolean bFirst = true;
            for (PLSProcedureArgument p : proc.Arguments) {
                if (bFirst)
                    jTextCode.append(sIndent + "CFinVarParam& " + p.getName());
                else
                    jTextCode.append(",\n" + sIndent + "CFinVarParam& " + p.getName());
                bFirst = false;
            }
            if (this.bIsPackage)
                jTextCode.append(
                    "\n)\n{\n" + sIndent + "SP_INIT(\"" + proc.userName + "." + proc.pckName + "." + proc.prcName + "\");\n\n");
            else
                jTextCode.append(
                    "\n)\n{\n" + sIndent + "SP_INIT(\"" + proc.userName + "." + proc.prcName + "\");\n\n");
            //
            for (PLSProcedureArgument p : proc.Arguments) {
                jTextCode.append(sIndent + "SP_ADDPARAM(" + String.format("%1$-" + (proc.maxLen+1) + "s", p.getName()) + ", " + p.getCppDirectionType() + ");\n");
            }
            jTextCode.append("\n" + sIndent + "SP_EXECUTE(pConn);\n\n");
            // out params
            for (PLSProcedureArgument p : proc.Arguments) {
                if ("OUT".equals(p.getDirectionType()) || "IN/OUT".equals(p.getDirectionType()))
                    jTextCode.append(sIndent + "SP_GETPARAM(" + p.getName() + ");\n");
            }
            jTextCode.append("\n" + sIndent + "return (0==(long)Success);\n}\n");
        }
    }
    
    public void GenerateCPPCode()
    {
        String userName = jComboUser.getSelectedItem().toString();
        String pckName = jComboPackages.getSelectedItem().toString();
        String prcName = jComboProcedures.getSelectedItem().toString();
        if (userName == null || pckName == null || prcName == null)
            return;
        //
        String qProcedures;
        if (this.bIsPackage)
            qProcedures = AppJFrame.qPCKProcs;
        else
            qProcedures = AppJFrame.qSTAProcs;
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
            ShowError(ex.getMessage());
            return;
        }
        jTextCode.setText("");
        // execute query
        try {
            stmProcedures = myConnection.prepareStatement(qProcedures);
            stmArguments = myConnection.prepareStatement(AppJFrame.qArguments);
            //
            if (this.bIsPackage) {
                stmProcedures.setString(1, userName);
                stmProcedures.setString(2, pckName);
                stmProcedures.setString(3, prcName);
            }
            else {
                stmProcedures.setString(1, userName);
                stmProcedures.setString(2, prcName);
            }
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
                procArguments.clear();
                while (rsa.next()) {
                    procArguments.add(new PLSProcedureArgument(rsa.getString(1), rsa.getString(2), rsa.getString(3)));
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
                if (this.bIsPackage)
                    jTextCode.append(
                        "\n)\n{\n" + sIndent + "SP_INIT(\"" + userName + "." + pckName + "." + prcName + "\");\n\n");
                else
                    jTextCode.append(
                        "\n)\n{\n" + sIndent + "SP_INIT(\"" + userName + "." + prcName + "\");\n\n");
                //
                for (PLSProcedureArgument p : procArguments) {
                    jTextCode.append(sIndent + "SP_ADDPARAM(" + String.format("%1$-" + (maxLen+1) + "s", p.getName()) + ", " + p.getCppDirectionType() + ");\n");
                }
                jTextCode.append("\n" + sIndent + "SP_EXECUTE(pConn);\n\n");
                // out params
                for (PLSProcedureArgument p : procArguments) {
                    if ("OUT".equals(p.getDirectionType()) || "IN/OUT".equals(p.getDirectionType()))
                        jTextCode.append(sIndent + "SP_GETPARAM(" + p.getName() + ");\n");
                }
                jTextCode.append("\n" + sIndent + "return (0==(long)Success);\n}\n");
            }
        } catch (SQLException e ) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, e);
            ShowError(e.getMessage());
        } finally {
            if (stmProcedures != null) {
                try {
                    stmProcedures.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    ShowError(ex.getMessage());
                }
            }
            if (stmArguments != null) {
                try {
                    stmArguments.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    ShowError(ex.getMessage());
                }
            }
        }
    }

    /**
     * Generate C# code, DAP version
     */
    public void GenerateCSharpCode()
    {
        ArrayList<DBProcedure> lProcList = new ArrayList<>();
        boolean bSuccess = GetProceduresFromDB(lProcList);
        if (!bSuccess)
            return;
        jTextCode.setText("");
        String sCode;
        for (DBProcedure proc : lProcList) {
            int OBJECT_ID = proc.OBJECT_ID;
            int SUBPROGRAM_ID = proc.SUBPROGRAM_ID;
            int maxLen = proc.maxLen;
            jTextCode.append(String.format("/* %d %d */\n", OBJECT_ID, SUBPROGRAM_ID));
            if (this.bIsPackage)
                AppendCode(String.format("using (command = new OracleCommand(\"%1$s.%2$s.%3$s\")) {\n", proc.userName, proc.pckName, proc.prcName), 0);
            else
                AppendCode(String.format("using (command = new OracleCommand(\"%1$s.%2$s\")) {\n", proc.userName, proc.prcName), 0);
            AppendCode("try {\n", 1);
            AppendCode("command.Connection = connection;\n", 2);
            AppendCode("command.CommandType = CommandType.StoredProcedure;\n", 2);
            AppendCode("// Adding Parameters\n", 2);
            for (PLSProcedureArgument p : proc.Arguments) {
                if ("IN".equals(p.getDirectionType())) {
                    sCode = String.format("%1$-" + (maxLen+1) + "s", p.getName())
                                + " = command.Parameters.Add(\"" + p.getName() + "\", "
                                + p.getCisParameterType() + ");\n";
                }
                else { // out or in/out parameter
                    if (p.IsStringType()) {
                        sCode = String.format("%1$-" + (maxLen+1) + "s", p.getName())
                                + " = command.Parameters.Add(\"" + p.getName() + "\", "
                                + p.getCisParameterType() + ", 2000);\n";
                    }
                    else {
                        sCode = String.format("%1$-" + (maxLen+1) + "s", p.getName())
                                + " = command.Parameters.Add(\"" + p.getName() + "\", "
                                + p.getCisParameterType() + ");\n";
                    }
                }
                AppendCode(sCode, 2);
                if (!"IN".equals(p.getDirectionType())) {
                    AppendCode(p.getName() + ".Direction = ParameterDirection.Output;\n", 2);
                }
            }
            // initialize params
            AppendCode("// Parameter Initialization\n", 2);
            for (PLSProcedureArgument p : proc.Arguments) {
                AppendCode(p.getName() + ".Value = #." + p.getName() + ";\n", 2);
            }
            AppendCode("// Execution\n", 2);
            AppendCode("connection.Open();\n", 2);
            AppendCode("command.ExecuteNonQuery();\n}\n", 2);
            AppendCode("catch(Exception ex) {}\n", 1);
            AppendCode("}\n", 0);
        }
    }

    /**
     * Generate C# code, Finbet version
     */
    public void GenerateCSharpFinbetCode()
    {
        ArrayList<DBProcedure> lProcList = new ArrayList<>();
        boolean bSuccess = GetProceduresFromDB(lProcList);
        if (!bSuccess)
            return;
        jTextCode.setText("");
        String sCode;
        int nArgumentsCnt;
        int k;
        for (DBProcedure proc : lProcList) {
            int OBJECT_ID = proc.OBJECT_ID;
            int SUBPROGRAM_ID = proc.SUBPROGRAM_ID;
            jTextCode.append(String.format("/* %d %d */\n", OBJECT_ID, SUBPROGRAM_ID));
            AppendCode(String.format("public %1$sResponse %1$s(%1$sContract input) {\n", proc.prcName), 1);
            AppendCode(String.format("return GeneralHandle(\"%1$s\", () => {\n", proc.prcName), 2);
            AppendCode("delegate(DbParameters dbParams) {\n", 3);
            AppendCode("// Adding Parameters\n", 3);
            for (PLSProcedureArgument p : proc.Arguments) {
                if ("IN".equals(p.getDirectionType())) {
                    sCode = String.format(
                            "dbParams.%1$s(\"%2$s\", input.%2$s);\n", p.getCisFBAddParamFunction(), p.getName());
                }
                else { // out or in/out parameter
                    if (p.IsStringType()) {
                        sCode = String.format(
                            "dbParams.%1$s(\"%2$s\", 2000);\n", p.getCisFBAddParamFunction(), p.getName());
                    }
                    else {
                        sCode = String.format(
                            "dbParams.%1$s(\"%2$s\");\n", p.getCisFBAddParamFunction(), p.getName());
                    }
                }
                AppendCode(sCode, 4);
            }
            AppendCode("},\n", 3);
            // initialize params
            AppendCode("// Parameter Initialization\n", 3);
            AppendCode("delegate(DbParametersReader dbReader) {\n", 3);
            for (PLSProcedureArgument p : proc.Arguments) {
                if (!"IN".equals(p.getDirectionType())) {
                    AppendCode(String.format("if (dbReader[\"%1$s\"].Value != DBNull.Value)\n", p.getName()), 4);
                    AppendCode(String.format("ret.%1$s = (%2$s)dbReader[\"%1$s\"].Value;\n", p.getName(), p.getCisFBParameterType()), 5);
                }
            }
            AppendCode("}, _connection);\n", 3);
            AppendCode("return ret;\n", 2);
            AppendCode("}, input);\n", 1);
            AppendCode("}\n", 0);
            // XML call
            AppendCode("/* for XML\n", 0);
            AppendCode(String.format("<sql name=\"%1$s\">\n",proc.prcName));
            AppendCode("BEGIN\n");
            AppendCode(proc.toString() + "(\n", 1);
            nArgumentsCnt = proc.Arguments.size();
            k = 0;
            for (PLSProcedureArgument p : proc.Arguments) {
                k += 1;
                if (k<nArgumentsCnt)
                    AppendCode(String.format("%1$s => :%1$s,\n", p.getName()), 2);
                else
                    AppendCode(String.format("%1$s => :%1$s\n", p.getName()), 2);
            }
            AppendCode(");\n",1);
            AppendCode("END;\n");
            AppendCode("</sql>\n");
            AppendCode("*/\n");
        }
    }

    public void GeneratePlSQLCode()
    {
        ArrayList<DBProcedure> lProcList = new ArrayList<>();
        boolean bSuccess = GetProceduresFromDB(lProcList);
        if (!bSuccess)
            return;
        jTextCode.setText("");
        String sCode;
        int nCount;
        for (DBProcedure proc : lProcList) {
            int OBJECT_ID = proc.OBJECT_ID;
            int SUBPROGRAM_ID = proc.SUBPROGRAM_ID;
            jTextCode.append(String.format("/* %d %d */\n", OBJECT_ID, SUBPROGRAM_ID));
            AppendCode("DECLARE\n", 0);
            for (PLSProcedureArgument p : proc.Arguments) {
                if (p.IsStringType())
                    sCode = String.format("%1$s %2$s(2000);\n", p.getName(), p.getType());
                else
                    sCode = String.format("%1$s %2$s;\n", p.getName(), p.getType());
                AppendCode(sCode, 1);
            }
            AppendCode("BEGIN\n", 0);
            // initialize params
            AppendCode("-- Parameter Initialization\n", 1);
            for (PLSProcedureArgument p : proc.Arguments) {
                if (!"OUT".equals(p.getDirectionType())) {
                    AppendCode(String.format("%1$s := %2$s;\n", p.getName(), p.getPlSQLInitialValue()), 1);
                }
            }
            AppendCode("-- Make call\n", 1);
            AppendCode(proc.toString() + "(\n", 1);
            nCount = 1;
            for (PLSProcedureArgument p : proc.Arguments) {
                if (nCount < proc.Arguments.size())
                    AppendCode(p.getName() + ",\n", 2);
                else
                    AppendCode(p.getName() + "\n", 2);
                nCount = nCount + 1;
            }
            AppendCode(");\n", 1);
            AppendCode("-- Print out parameters\n", 1);
            for (PLSProcedureArgument p : proc.Arguments) {
                if (!"IN".equals(p.getDirectionType())) {
                    if (p.IsStringType())
                        AppendCode(String.format("DBMS_OUTPUT.PUT_LINE('%1$s='||%1$s);\n", p.getName()), 1);
                    else
                        AppendCode(String.format("DBMS_OUTPUT.PUT_LINE('%1$s='||to_char(%1$s));\n", p.getName()), 1);
                }
            }
            AppendCode("END;\n/\n", 0);
        }
    }

    public void GenerateUDBACall() {
        ArrayList<DBProcedure> lProcList = new ArrayList<>();
        boolean bSuccess = GetProceduresFromDB(lProcList);
        if (!bSuccess)
            return;
        jTextCode.setText("");
        String sCode;
        int nCount;
        for (DBProcedure proc : lProcList) {
            int OBJECT_ID = proc.OBJECT_ID;
            int SUBPROGRAM_ID = proc.SUBPROGRAM_ID;
            jTextCode.append(String.format("/* %d %d */\n", OBJECT_ID, SUBPROGRAM_ID));
            AppendCode("-- log in params\n", 0);
            AppendCode("udbash.LogEntry(\n", 0);
            AppendCode("lnIDUDBA, 10,\n", 1);
            AppendCode(String.format("'%1$s', -- Source system\n", proc.userName), 1);
            AppendCode("'Package', -- Source type\n", 1);
            AppendCode(String.format("'%1$s', -- Source object\n", proc.toString()), 1);
            AppendCode("0, -- Code from source\n", 1);
            nCount = 1;
            for (PLSProcedureArgument p : proc.Arguments) {
                if (!"OUT".equals(p.getDirectionType())) {
                    sCode = String.format("';%1$s='||%1$s", p.getName());
                    if (nCount > 1)
                        sCode = "||" + sCode + "\n";
                    AppendCode(sCode, 1);
                    nCount += 1;
                }
            }
            AppendCode(",NULL, -- Code\n", 1);
            AppendCode("NULL -- Message\n", 1);
            AppendCode(");\n", 0);
            // udba call for out parameters
            AppendCode("-- log out params\n", 0);
            AppendCode("udbash.LogEntry(\n", 0);
            AppendCode("lnIDUDBA, 0,\n", 1);
            AppendCode(String.format("'%1$s', -- Source system\n", proc.userName), 1);
            AppendCode("'DB', -- Source type\n", 1);
            AppendCode(String.format("'%1$s', -- Source object\n", proc.toString()), 1);
            AppendCode("0, -- Code from source\n", 1);
            nCount = 1;
            for (PLSProcedureArgument p : proc.Arguments) {
                if (!"IN".equals(p.getDirectionType())) {
                    sCode = String.format("';%1$s='||%1$s", p.getName());
                    if (nCount > 1)
                        sCode = "||" + sCode + "\n";
                    AppendCode(sCode, 1);
                    nCount += 1;
                }
            }
            AppendCode(",NULL, -- Code\n", 1);
            AppendCode("NULL -- Message\n", 1);
            AppendCode(");\n", 0);
        }
    }

    private boolean ExtractErrorCodes(String pOwner, String pName, String pType) {
        // check connection
        try {
            if (myConnection.isClosed()) {
                Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, "Not connected");
                ShowError("Not connected to DB");
            }
        } catch (SQLException ex) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
            ShowError(ex.getMessage());
            return false;
        }
        jTextCode.setText("");
        PreparedStatement stmSource = null;
        ArrayList<String> src = new ArrayList<>();
        ArrayList<PLSCodeInfo> infoList = new ArrayList<>();
        String allErrors;
        int nPos;
        int nPos2;
        try {
            stmSource = myConnection.prepareStatement(AppJFrame.qSource);
            stmSource.setString(1, pType);
            stmSource.setString(2, pOwner);
            stmSource.setString(3, pName);
            ResultSet rss;
            rss = stmSource.executeQuery();
            while(rss.next()) {
                src.add(rss.getString(2));
            }
            // remove comments
            String line;
            for (int i=0; i < src.size(); i++) {
                line = src.get(i);
                nPos = line.indexOf("--");
                if (nPos == 0)
                    src.set(i, "\n");
                if (nPos > 0) {
                    src.set(i, line.substring(0,nPos) + "\n");
                }
            }
            boolean bInComment = false;
            for (int i=0; i < src.size(); i++) {
                line = src.get(i);
                if (bInComment) {
                    nPos2 = line.indexOf("*/");
                    if (nPos2 >= 0) {
                        line = line.substring(nPos2+2);
                        src.set(i, line);
                        bInComment = false;
                    }
                    else {
                        src.set(i, "\n");
                        continue;
                    }
                }
                nPos = line.indexOf("/*");
                while (nPos>=0) {
                    bInComment = true;
                    // on the same line
                    nPos2 = line.indexOf("*/");
                    if (nPos2 > 0) {
                        line = line.substring(0,nPos) + line.substring(nPos2+2);
                        src.set(i, line);
                        bInComment = false;
                    }
                    else {
                        src.set(i, line.substring(0,nPos));
                        break;
                    }
                    nPos = line.indexOf("/*");
                }
            }
            int i = 0; // current line of source
            int j = -1; // index of info list
            String sType;
            String sName;
            String sCode;
            String[] arr;
            Pattern errCodePattern = Pattern.compile("[^0-9][0-9]{8}[^0-9]");
            Matcher matcher;
            for (String s : src) {
                sType = "";
                sName = "";
                if (s.toUpperCase().contains("PROCEDURE")) {
                    sType = "PROCEDURE";
                }
                if (s.toUpperCase().contains("FUNCTION")) {
                    sType = "FUNCTION";
                }
                if (!"".equals(sType)) {
                    arr = s.trim().split("\\W+");
                    if (arr.length > 1)
                        sName = arr[1];
                    infoList.add(new PLSCodeInfo(sType, sName, i, i));
                    j += 1; // now j is the last index of the info list
                    if (j>0) {
                        infoList.get(j-1).setCodeEnd(i);
                    }
                }
                else {
                    matcher = errCodePattern.matcher(s);
                    while (matcher.find()) {
                        sCode = matcher.group();
                        infoList.get(j).addCodeToErrorCodeList(sCode.substring(1, sCode.length()-1));
                    }
                }
                i += 1;
            }
            infoList.get(j).setCodeEnd(i-1);
        }
        catch (SQLException e ) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, e);
            ShowError(e.getMessage());
            return false;
        } finally {
            if (stmSource != null) {
                allErrors = "";
                for (PLSCodeInfo info : infoList) {
                    AppendCode(info.toString() + "\n");
                    if (info.HaveErrorCodes())
                        allErrors += info.FormatErrorCodeList() + ",";
                    //AppendCode(info.GetTestSQL() + "\n");
                }
                String sCheckSQL;
                sCheckSQL = PLSCodeInfo.GetValidationTestSQL(allErrors.substring(0, allErrors.length()-1));
                AppendCode(sCheckSQL + "\n");
                sCheckSQL = PLSCodeInfo.GetExistenceTestSQL(allErrors.substring(0, allErrors.length()-1));
                AppendCode(sCheckSQL + "\n");
                // write source freed from comments
                File file = new File("source.txt");
                try (FileWriter fileWriter = new FileWriter(file)) {
                    for (String line : src) { fileWriter.write(line); }
                    fileWriter.close();
                }
                catch(IOException ioe) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ioe);
                    ShowError(ioe.getMessage());
                }
                // write info
                /*file = new File("codeinfo.txt");
                try (FileWriter fileWriter = new FileWriter(file)) {
                    for (PLSCodeInfo info : infoList) { fileWriter.write(info.toString() + "\n"); }
                    fileWriter.close();
                }
                catch(IOException ioe) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ioe);
                    ShowError(ioe.getMessage());
                }*/
                try {
                    stmSource.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    ShowError(ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    private void ExecuteProcedure() throws SQLException {
        ArrayList<DBProcedure> lProcList = new ArrayList<>();
        boolean bSuccess = GetProceduresFromDB(lProcList);
        if (!bSuccess)
            return;
        String sCode;
        String sParamPlaceholders;
        int nCount;
        CallableStatement cStmt;
        Integer sqlType;
        for (DBProcedure proc : lProcList) {
            nCount = proc.Arguments.size();
            sParamPlaceholders = String.join(",", Collections.nCopies(nCount, "?"));
            sCode = String.format("{call %1$s(%2$s)}", proc.toString(), sParamPlaceholders);
            jTextCode.setText(sCode);
            cStmt = myConnection.prepareCall(sCode);
            // initialize params
            nCount = 1;
            for (PLSProcedureArgument p : proc.Arguments) {
                // IN and IN OUT
                if (!"OUT".equals(p.getDirectionType())) {
                    if (p.IsStringType())
                        cStmt.setString(nCount, "");
                    else if (p.IsNumericType())
                        cStmt.setInt(nCount, 0);
                    else if (p.IsDateTimeType())
                        cStmt.setDate(nCount, null);
                }
                // OUT
                else {
                    sqlType = p.getSqlType();
                    cStmt.registerOutParameter(nCount, sqlType);
                }
                nCount = nCount + 1;
            }
            cStmt.execute();
            //nCount = 1;
            for (PLSProcedureArgument p : proc.Arguments) {
                if (!"IN".equals(p.getDirectionType())) {
                    if (p.IsStringType())
                        AppendCode(String.format("%1$s=%2$s\n", p.getName(), cStmt.getString(p.getName())), 1);
                    else if (p.IsNumericType())
                        AppendCode(String.format("%1$s=%2$d\n", p.getName(), cStmt.getInt(p.getName())), 1);
                    else
                        AppendCode(String.format("%1$s=%2$tT\n", p.getName(), cStmt.getDate(p.getName())), 1);
                }
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
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        buttonGroupCodeType = new javax.swing.ButtonGroup();
        jComboBranches = new javax.swing.JComboBox();
        jButtonAddBranch = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButtonConnect = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextTNSNamesDir = new javax.swing.JTextField();
        jButtonChooseTNSDir = new javax.swing.JButton();
        jButtonRemoveBranch = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextCode = new javax.swing.JTextArea();
        jButtonEditBranch = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jComboProcedures = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboUser = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jComboPackages = new javax.swing.JComboBox();
        jCheckFilterUsers = new javax.swing.JCheckBox();
        jButtonErrorCodes = new javax.swing.JButton();
        jButtonExecute = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jRadioCisFinbet = new javax.swing.JRadioButton();
        jRadioCpp = new javax.swing.JRadioButton();
        jRadioCisDAP = new javax.swing.JRadioButton();
        jRadioPlSql = new javax.swing.JRadioButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jTextUser = new javax.swing.JTextField();
        jTextPackage = new javax.swing.JTextField();
        jTextProcedure = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jCheckParseSource = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jButtonCreateCode = new javax.swing.JButton();
        jButtonCopy = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Generate C++/C# call from DB Call");
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

        jTextTNSNamesDir.setEnabled(false);

        jButtonChooseTNSDir.setText(">");
        jButtonChooseTNSDir.setEnabled(false);
        jButtonChooseTNSDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChooseTNSDirActionPerformed(evt);
            }
        });

        jButtonRemoveBranch.setText("-");
        jButtonRemoveBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveBranchActionPerformed(evt);
            }
        });

        jTextCode.setColumns(20);
        jTextCode.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        jTextCode.setRows(5);
        jTextCode.setTabSize(4);
        jScrollPane1.setViewportView(jTextCode);

        jButtonEditBranch.setText("*");
        jButtonEditBranch.setMaximumSize(new java.awt.Dimension(41, 23));
        jButtonEditBranch.setMinimumSize(new java.awt.Dimension(41, 23));
        jButtonEditBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditBranchActionPerformed(evt);
            }
        });

        jLabel5.setText("Proc/Func:");

        jLabel3.setText("User:");

        jComboUser.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                jComboUserPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        jComboUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboUserActionPerformed(evt);
            }
        });

        jLabel4.setText("Package:");

        jComboPackages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboPackagesActionPerformed(evt);
            }
        });

        jCheckFilterUsers.setText("Filter users");
        jCheckFilterUsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckFilterUsersActionPerformed(evt);
            }
        });

        jButtonErrorCodes.setText("Error codes");
        jButtonErrorCodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonErrorCodesActionPerformed(evt);
            }
        });

        jButtonExecute.setText("Execute");
        jButtonExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecuteActionPerformed(evt);
            }
        });

        jLabel7.setText("(may take a while)");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboUser, 0, 191, Short.MAX_VALUE)
                    .addComponent(jComboPackages, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboProcedures, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckFilterUsers)
                        .addGap(22, 22, 22))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonExecute)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jButtonErrorCodes))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jCheckFilterUsers))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboPackages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4))
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboProcedures, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jButtonErrorCodes)
                    .addComponent(jButtonExecute))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setText("Source type:");

        buttonGroupCodeType.add(jRadioCisFinbet);
        jRadioCisFinbet.setText("C# Finbet");
        jRadioCisFinbet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioCisFinbetActionPerformed(evt);
            }
        });

        buttonGroupCodeType.add(jRadioCpp);
        jRadioCpp.setSelected(true);
        jRadioCpp.setText("C++");
        jRadioCpp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioCppActionPerformed(evt);
            }
        });

        buttonGroupCodeType.add(jRadioCisDAP);
        jRadioCisDAP.setText("C# DAP");
        jRadioCisDAP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioCisDAPActionPerformed(evt);
            }
        });

        buttonGroupCodeType.add(jRadioPlSql);
        jRadioPlSql.setText("PlSQL");
        jRadioPlSql.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioPlSqlActionPerformed(evt);
            }
        });

        buttonGroupCodeType.add(jRadioButton1);
        jRadioButton1.setText("Udba call");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioCpp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioCisDAP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioCisFinbet)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioPlSql)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioCisDAP)
                    .addComponent(jRadioCisFinbet)
                    .addComponent(jRadioCpp)
                    .addComponent(jLabel6)
                    .addComponent(jRadioPlSql)
                    .addComponent(jRadioButton1))
                .addContainerGap())
        );

        jTextUser.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        jTextUser.setToolTipText("User friendly name");

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jComboUser, org.jdesktop.beansbinding.ELProperty.create("${selectedItem}"), jTextUser, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jTextPackage.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        jTextPackage.setToolTipText("Package friendly name");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jComboPackages, org.jdesktop.beansbinding.ELProperty.create("${selectedItem}"), jTextPackage, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jTextProcedure.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        jTextProcedure.setToolTipText("Procedure friendly name");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jComboProcedures, org.jdesktop.beansbinding.ELProperty.create("${selectedItem}"), jTextProcedure, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jCheckParseSource.setText("Parse source");
        jCheckParseSource.setToolTipText("If you check this option, package specification will be parsed for parameter's friendly names");
        jCheckParseSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckParseSourceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckParseSource)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckParseSource)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButtonCreateCode.setText("Generate");
        jButtonCreateCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateCodeActionPerformed(evt);
            }
        });

        jButtonCopy.setText("Copy");
        jButtonCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCopyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jButtonCreateCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCopy, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButtonCreateCode)
                .addComponent(jButtonCopy))
        );

        jButton1.setText("Clear");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jTextUser, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jTextPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextProcedure))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel2)
                                .addGap(10, 10, 10)
                                .addComponent(jTextTNSNamesDir, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(jButtonChooseTNSDir))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBranches, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonEditBranch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(jButtonAddBranch)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonRemoveBranch, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 23, Short.MAX_VALUE)))
                .addGap(10, 10, 10))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jButtonConnect, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel2))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jTextTNSNamesDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonChooseTNSDir))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBranches, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonAddBranch)
                        .addComponent(jButtonEditBranch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonRemoveBranch)))
                .addGap(17, 17, 17)
                .addComponent(jButtonConnect)
                .addGap(6, 6, 6)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextPackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextProcedure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        bindingGroup.bind();

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
            String url = bf.getURL();
            b = new Branch(bname, user, pass, tns, url);
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
        String sConnStr;
        if (!"".equals(currentBranch.getURL())) {
            myOds.setURL(currentBranch.getURL());
            sConnStr = currentBranch.getURL();
            myOds.setDriverType("thin");
        }
        else {
            myOds.setTNSEntryName(currentBranch.getTnsEntry());
            sConnStr = currentBranch.getTnsEntry();
            myOds.setDriverType("oci8");
        }
        myOds.setUser(currentBranch.getUserName());
        myOds.setPassword(currentBranch.getPassword());
        Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, "connecting using {0}", currentBranch.getConnString());
        try {
            myConnection = myOds.getConnection();
            Logger.getLogger(AppJFrame.class.getName()).log(Level.INFO, "connected");
            PopulateUsers();
        } catch (SQLException ex) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
            ShowError(String.format("Connect using %s, %s, with following error:\n%s", myOds.getUser(), sConnStr, ex.getMessage()));
            try {
                ShowError("URL=" + myOds.getURL());
            } catch (SQLException ex1) {
                Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex1);
            }
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
        // this was moved to jComboUserPopupMenuWillBecomeInvisible
        // to speed up chosing process
        //PopulatePackages(user);
        //jComboPackagesActionPerformed(null);
    }//GEN-LAST:event_jComboUserActionPerformed

    private void jComboPackagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboPackagesActionPerformed
        String user = jComboUser.getSelectedItem().toString();
        String pck = jComboPackages.getSelectedItem().toString();
        if ("<standalone>".equals(pck))
            this.bIsPackage = false;
        else
            this.bIsPackage = true;
        PopulateProcedures(user, pck);
    }//GEN-LAST:event_jComboPackagesActionPerformed

    private void jButtonCreateCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateCodeActionPerformed
        if (null != sCodeType)
            switch (sCodeType) {
            case "C++":
                GenerateCPPCode2();
                break;
            case "C# DAP":
                GenerateCSharpCode();
                break;
            case "C# Finbet":
                GenerateCSharpFinbetCode();
                break;
            case "PLSQL":
                GeneratePlSQLCode();
                break;
            case "UDBA":
                GenerateUDBACall();
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

    private void jButtonEditBranchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditBranchActionPerformed
        EditBranch();
    }//GEN-LAST:event_jButtonEditBranchActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jTextCode.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jRadioCppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioCppActionPerformed
        sCodeType = "C++";
    }//GEN-LAST:event_jRadioCppActionPerformed

    private void jRadioCisDAPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioCisDAPActionPerformed
        sCodeType = "C# DAP";
    }//GEN-LAST:event_jRadioCisDAPActionPerformed

    private void jRadioCisFinbetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioCisFinbetActionPerformed
        sCodeType = "C# Finbet";
    }//GEN-LAST:event_jRadioCisFinbetActionPerformed

    private void jButtonCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCopyActionPerformed
        CopyCode2Clipboard();
    }//GEN-LAST:event_jButtonCopyActionPerformed

    private void jCheckFilterUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckFilterUsersActionPerformed
        if (myConnection != null)
            PopulateUsers();
    }//GEN-LAST:event_jCheckFilterUsersActionPerformed

    private void jCheckParseSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckParseSourceActionPerformed
        this.bParseSource = jCheckParseSource.isSelected();
    }//GEN-LAST:event_jCheckParseSourceActionPerformed

    private void jRadioPlSqlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioPlSqlActionPerformed
        sCodeType = "PLSQL";
    }//GEN-LAST:event_jRadioPlSqlActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        sCodeType = "UDBA";
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jButtonErrorCodesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonErrorCodesActionPerformed
        String userName = jComboUser.getSelectedItem().toString();
        String pckName = jComboPackages.getSelectedItem().toString();
        String prcName = jComboProcedures.getSelectedItem().toString();
        if (userName != null) {
            ExtractErrorCodes(userName, pckName, "PACKAGE BODY");
        }
    }//GEN-LAST:event_jButtonErrorCodesActionPerformed

    private void jButtonExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecuteActionPerformed
        try {
            ExecuteProcedure();
        } catch (SQLException ex) {
            Logger.getLogger(AppJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonExecuteActionPerformed

    private void jComboUserPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jComboUserPopupMenuWillBecomeInvisible
        Object selItem = jComboUser.getSelectedItem();
        if (selItem != null) {
            PopulatePackages(selItem.toString());
            jComboPackagesActionPerformed(null);
        }
    }//GEN-LAST:event_jComboUserPopupMenuWillBecomeInvisible

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
    private javax.swing.ButtonGroup buttonGroupCodeType;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAddBranch;
    private javax.swing.JButton jButtonChooseTNSDir;
    private javax.swing.JButton jButtonConnect;
    private javax.swing.JButton jButtonCopy;
    private javax.swing.JButton jButtonCreateCode;
    private javax.swing.JButton jButtonEditBranch;
    private javax.swing.JButton jButtonErrorCodes;
    private javax.swing.JButton jButtonExecute;
    private javax.swing.JButton jButtonRemoveBranch;
    private javax.swing.JCheckBox jCheckFilterUsers;
    private javax.swing.JCheckBox jCheckParseSource;
    private javax.swing.JComboBox jComboBranches;
    private javax.swing.JComboBox jComboPackages;
    private javax.swing.JComboBox jComboProcedures;
    private javax.swing.JComboBox jComboUser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioCisDAP;
    private javax.swing.JRadioButton jRadioCisFinbet;
    private javax.swing.JRadioButton jRadioCpp;
    private javax.swing.JRadioButton jRadioPlSql;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextCode;
    private javax.swing.JTextField jTextPackage;
    private javax.swing.JTextField jTextProcedure;
    private javax.swing.JTextField jTextTNSNamesDir;
    private javax.swing.JTextField jTextUser;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
