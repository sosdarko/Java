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

    public Branch(String name, String connString) {
        this.name = name;
        this.connString = connString;
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

    @Override
    public String toString() {
        return name;
    }
}
