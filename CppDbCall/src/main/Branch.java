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
    String URL;
    String DriverType;

    static final long serialVersionUID = 1L;

    public Branch() {}
    
    public Branch(String name, String connString) {
        this.name = name;
        this.connString = connString;
    }

    public Branch(String name, String UserName, String Password, String TnsEntry, String URL) {
        this.name = name;
        this.UserName = UserName;
        this.Password = Password;
        this.TnsEntry = TnsEntry;
        if (URL == null)
            this.URL = "";
        else
            this.URL = URL;
        this.connString = UserName+ "/" + Password + "@" + TnsEntry;
        CalculateType();
    }

    public void Copy(Branch from) {
        this.name = from.getName();
        this.UserName = from.getUserName();
        this.Password = from.getPassword();
        this.TnsEntry = from.getTnsEntry();
        this.URL = from.getURL();
        this.connString = from.getConnString();
        CalculateType();
    }

    private void CalculateType()
    {
        if ("".equals(this.URL))
            DriverType = "oci8";
        else
            DriverType = "thin";
    }
    
    public String getConnString() {
        return connString;
    }
    
    public String getUserAtDB() {
        return UserName + "@" + TnsEntry;
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

    public String getURL() {
        return URL;
    }

    public String getDriverType() {
        return DriverType;
    }
    
    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
