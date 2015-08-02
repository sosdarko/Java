/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author darko.sos
 */
public class AppData implements java.io.Serializable {
    String CurrentDBAlterNode;
    String DBCodeNode;
    int CurrentAlterNumber;

    public AppData() {
        CurrentDBAlterNode = "";
        DBCodeNode = "";
        CurrentAlterNumber = 0;
    }
    
    public String getCurrentDBAlterNode() {
        return CurrentDBAlterNode;
    }

    public String getDBCodeNode() {
        return DBCodeNode;
    }

    public Integer getCurrentAlterNumber() {
        return CurrentAlterNumber;
    }

    public void setCurrentDBAlterNode(String CurrentDBAlterNode) {
        this.CurrentDBAlterNode = CurrentDBAlterNode;
    }

    public void setDBCodeNode(String CurrentDBCodeNode) {
        this.DBCodeNode = CurrentDBCodeNode;
    }

    public void setCurrentAlterNumber(int CurrentAlterNumber) {
        this.CurrentAlterNumber = CurrentAlterNumber;
    }

    public void CopyFrom(AppData ad)
    {
        this.CurrentAlterNumber = ad.getCurrentAlterNumber();
        this.CurrentDBAlterNode = ad.getCurrentDBAlterNode();
        this.DBCodeNode = ad.getDBCodeNode();
    }
}
