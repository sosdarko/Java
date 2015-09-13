/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author darko.sos
 */
public class DBRelease implements Serializable {
    String sTfsAlterBranch;
    File sReleaseFolder;
    String sReleaseNumber;
    ArrayList<File> aReleaseFiles;
    ArrayList<Script> aReleaseScripts;
    String sMainName;
    String sFinbetRelNo;
    String sDescription;
    //
    static final long serialVersionUID = 1L;

    public DBRelease()
    {
        aReleaseFiles = new ArrayList<>();
        aReleaseScripts = new ArrayList<>();
    }
    
    @Override
    public String toString()
    {
        if (sMainName.endsWith(".sql"))
            return sMainName.substring(0, sMainName.length()-4);
        else
            return sMainName;
    }

    void MakeReleaseFolder(String folderName)
    {
        sReleaseFolder = new File(sTfsAlterBranch, folderName);
        sReleaseFolder.mkdir();
    }

    void AddFile(String name)
    {
        aReleaseFiles.add(new File(sReleaseFolder, name));
    }
    
    void AddFile(File f)
    {
        aReleaseFiles.add(f);
    }

    void RemoveFile(String name)
    {
        aReleaseFiles.remove(new File(sReleaseFolder, name));
    }

    void AddScript(Script s)
    {
        aReleaseScripts.add(s);
    }

    void SwapScripts(int i, int j)
    {
        Collections.swap(aReleaseScripts, i, j);
    }
    
    boolean RemoveScript(String scriptName)
    {
        boolean found = false;
        if (aReleaseScripts != null && aReleaseScripts.size() > 0) {
            int index = 0;
            String s = aReleaseScripts.get(index).getName();
            while (index < aReleaseScripts.size()) {
                if (aReleaseScripts.get(index).getName().equals(scriptName)) {
                    aReleaseFiles.remove(index);
                    found = true;
                    break;
                }
            }
        }
        return found;
    }
    
    boolean RemoveScript(Script script)
    {
        return aReleaseScripts.remove(script);
    }
    
    boolean RemoveScript(int i)
    {
        boolean removed = false;
        try {aReleaseScripts.remove(i);}
        catch (IndexOutOfBoundsException e) {removed = false;}

        return removed;
    }
    
    boolean ReplaceScript(int index, Script scr)
    {
        return (aReleaseScripts.set(index, scr) != null);
    }

    public String getMainName() {
        return sMainName;
    }

    public ArrayList<File> getReleaseFiles() {
        return aReleaseFiles;
    }

    public ArrayList<Script> getReleaseScripts() {
        return aReleaseScripts;
    }
    
    public File getReleaseFolder() {
        return sReleaseFolder;
    }

    public String getTfsAlterBranch() {
        return sTfsAlterBranch;
    }

    public void setMainName(String sMainName) {
        this.sMainName = sMainName;
    }

    public void setReleaseFiles(ArrayList<File> aReleaseFiles) {
        this.aReleaseFiles = aReleaseFiles;
    }

    public void setReleaseFolder(File sReleaseFolder) {
        this.sReleaseFolder = sReleaseFolder;
    }

    public void setTfsAlterBranch(String sTfsAlterBranch) {
        this.sTfsAlterBranch = sTfsAlterBranch;
    }

    public String getReleaseNumber() {
        return sReleaseNumber;
    }

    public void setReleaseNumber(String sReleaseNumber) {
        this.sReleaseNumber = sReleaseNumber;
    }

    public String getFinbetRelNo() {
        return sFinbetRelNo;
    }

    public void setFinbetRelNo(String sFinbetRelNo) {
        this.sFinbetRelNo = sFinbetRelNo;
    }

    public String getDescription() {
        return sDescription;
    }

    public void setDescription(String sDescription) {
        this.sDescription = sDescription;
    }
    
    public Script getScript(int index) {
        return aReleaseScripts.get(index);
    }
}
