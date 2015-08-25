/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author darko.sos
 */
public class DBRelease implements Serializable {
    String sTfsAlterBranch;
    File sReleaseFolder;
    String sReleaseNumber;
    ArrayList<File> sReleaseFiles;
    String sMainName;

    public DBRelease()
    {
        sReleaseFiles = new ArrayList<>();
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
        sReleaseFiles.add(new File(sReleaseFolder, name));
    }

    void RemoveFile(String name)
    {
        sReleaseFiles.remove(new File(sReleaseFolder, name));
    }

    public String getMainName() {
        return sMainName;
    }

    public ArrayList<File> getReleaseFiles() {
        return sReleaseFiles;
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

    public void setReleaseFiles(ArrayList<File> sReleaseFiles) {
        this.sReleaseFiles = sReleaseFiles;
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

}
