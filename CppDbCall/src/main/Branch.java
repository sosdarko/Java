/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.Serializable;

/**
 *
 * @author darko.sos
 */
public class Branch implements Serializable {
    String name;
    String connString;
    String UserName;
    String Password;
    String TnsEntry;

    public Branch(String name, String connString) {
        this.name = name;
        this.connString = connString;
    }

    public Branch(String name, String UserName, String Password, String TnsEntry) {
        this.name = name;
        this.UserName = UserName;
        this.Password = Password;
        this.TnsEntry = TnsEntry;
        this.connString = UserName+ "/" + Password + "@" + TnsEntry;
    }

    public String getConnString() {
        return connString;
    }

    public String getName() {
        return name;
    }

    public void setConnString(String connString) {
        this.connString = connString;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTnsEntry() {
        return TnsEntry;
    }

    public void setPassword(String sPassword) {
        this.Password = sPassword;
    }

    public void setTnsEntry(String sTnsEntry) {
        this.TnsEntry = sTnsEntry;
    }

    public String getUserName() {
        return UserName;
    }

    public String getPassword() {
        return Password;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    @Override
    public String toString() {
        return name;
    }
} 
