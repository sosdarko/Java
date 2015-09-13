/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.Serializable;

/**
 *
 * @author sos
 */
public class Script implements Serializable {
    String Name;
    String Content;
    String Type;
    String Subype;
    String DBObjectType;
    String DBObjectName;
    //
    static final long serialVersionUID = 1L;

    public Script(String Name, String Content, String Type, String Subype, String DBObjectType, String DBObjectName) {
        this.Name = Name;
        this.Content = Content;
        this.Type = Type;
        this.Subype = Subype;
        this.DBObjectType = DBObjectType;
        this.DBObjectName = DBObjectName;
    }

    public Script(Script orig) {
        this.Name = orig.getName();
        this.Content = orig.getContent();
        this.Type = orig.getType();
        this.Subype = orig.getSubype();
        this.DBObjectType = orig.getDBObjectType();
        this.DBObjectName = orig.getDBObjectName();
    }
    
    public Script(String Name, String Content) {
        this.Name = Name;
        this.Content = Content;
        this.Type = "";
        this.Subype = "";
        this.DBObjectType = "";
        this.DBObjectName = "";
    }

    public Script() {
        this.Name = "";
        this.Content = "";
        this.Type = "";
        this.Subype = "";
        this.DBObjectType = "";
        this.DBObjectName = "";
    }

    public String getContent() {
        return Content;
    }

    public String getName() {
        return Name;
    }

    public void setContent(String Content) {
        this.Content = Content;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public void setSubype(String Subype) {
        this.Subype = Subype;
    }

    public void setDBObjectType(String DBObjectType) {
        this.DBObjectType = DBObjectType;
    }

    public void setDBObjectName(String DBObjectName) {
        this.DBObjectName = DBObjectName;
    }

    public String getType() {
        return Type;
    }

    public String getSubype() {
        return Subype;
    }

    public String getDBObjectType() {
        return DBObjectType;
    }

    public String getDBObjectName() {
        return DBObjectName;
    }

    @Override
    public String toString() {
        return Name;
    }
}
