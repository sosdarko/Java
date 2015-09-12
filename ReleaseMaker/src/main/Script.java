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

    public Script(String Name, String Content) {
        this.Name = Name;
        this.Content = Content;
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

    @Override
    public String toString() {
        return Name;
    }
}
