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
public class PLSProcedureArgument {
    String name;
    String type;
    String cppType;

    public PLSProcedureArgument() {
    }

    @Override
    public String toString() {
        return name + " " + type;
    }

    public PLSProcedureArgument(String name, String type) {
        this.name = name;
        this.type = type;
        if ("IN".equals(type))
            this.cppType = "PARAM_DIR_IN";
        else
            if ("OUT".equals(type))
                this.cppType = "PARAM_DIR_OUT";
            else
                if ("IN/OUT".equals(type))
                    this.cppType = "PARAM_DIR_INOUT";
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCppType() {
        return cppType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
        if ("IN".equals(type))
            this.cppType = "PARAM_DIR_IN";
        else
            if ("OUT".equals(type))
                this.cppType = "PARAM_DIR_OUT";
            else
                if ("IN/OUT".equals(type))
                    this.cppType = "PARAM_DIR_INOUT";
    }
}
