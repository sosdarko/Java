/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author darko.sos
 */
public class PLSProcedureArgument {
    String name;
    String plsType;
    String DirectionType;
    String cppDirectionType;
    String cisDirectionType;
    String cisParameterType;
    String cisFBParameterType;
    String cisFBAddParamFunction;
    String plsInitValue;
    
    static final Map<String , String> CISTYPES = new HashMap<String , String>() {{
        put("BFILE",                    "BFile");
        put("BINARY_DOUBLE",            "BinaryDouble");
        put("BINARY_FLOAT",             "BinaryFloat");
        put("BINARY_INTEGER",           "Int32");
        put("BLOB",                     "Blob");
        put("CHAR",                     "Char");
        put("CLOB",                     "Clob");
        put("DATE",                     "Date");
        put("DOUBLE PRECISION",         "Double");
        put("FLOAT",                    "Decimal");
        put("INTEGER",                  "Int32");
        put("INTERVAL DAY TO SECOND",   "IntervalDS");
        put("INTERVAL YEAR TO MONTH",   "IntervalYM");
        put("LONG",                     "Long");
        put("LONG RAW",                 "LongRaw");
        put("NUMBER",                   "Int64");
        put("PLS_INTEGER",              "Int32");
        put("RAW",                      "Raw");
        put("REAL",                     "Decimal");
        put("SMALLINT",                 "Int16");
        put("STRING",                   "Varchar2");
        put("TIME",                     "TimeStamp");
        put("TIME WITH TIME ZONE",      "TimeStampTZ");
        put("TIMESTAMP",                "TimeStamp");
        put("TIMESTAMP WITH LOCAL TIME ZONE", "TimeStampLTZ");
        put("TIMESTAMP WITH TIME ZONE", "TimeStampTZ");
        put("VARCHAR2",                 "Varchar2");
    }};

    static final HashSet<String> STRTYPES = new HashSet<>();
    static final HashSet<String> NUMTYPES = new HashSet<>();
    static final HashSet<String> DATTYPES = new HashSet<>();

    private void Initialize()
    {
        STRTYPES.add("CHAR");
        STRTYPES.add("STRING");
        STRTYPES.add("VARCHAR2");
        //
        NUMTYPES.add("BINARY_DOUBLE");
        NUMTYPES.add("BINARY_FLOAT");
        NUMTYPES.add("BINARY_INTEGER");
        NUMTYPES.add("FLOAT");
        NUMTYPES.add("DOUBLE PRECISION");
        NUMTYPES.add("INTEGER");
        NUMTYPES.add("NUMBER");
        NUMTYPES.add("PLS_INTEGER");
        NUMTYPES.add("REAL");
        NUMTYPES.add("SMALLINT");
        //
        DATTYPES.add("DATE");
        DATTYPES.add("TIME");
        DATTYPES.add("TIME WITH TIME ZONE");
        DATTYPES.add("TIMESTAMP");
        DATTYPES.add("TIMESTAMP WITH LOCAL TIME ZONE");
        DATTYPES.add("TIMESTAMP WITH TIME ZONE");
    }
    
    public PLSProcedureArgument() {
        Initialize();
    }

    @Override
    public String toString() {
        return name + " " + DirectionType;
    }

    public PLSProcedureArgument(String pName, String pDirectionType, String pType) {
        Initialize();
        this.name = pName;
        this.plsType = pType;
        this.DirectionType = pDirectionType;
        if (null != pDirectionType)
            switch (pDirectionType) {
            case "IN":
                this.cppDirectionType = "PARAM_DIR_IN";
                this.cisDirectionType = "ParameterDirection.Input";
                this.cisFBAddParamFunction = "Add";
                break;
            case "OUT":
                this.cppDirectionType = "PARAM_DIR_OUT";
                this.cisDirectionType = "ParameterDirection.Output";
                this.cisFBAddParamFunction = "AddOutput";
                break;
            case "IN/OUT":
                this.cppDirectionType = "PARAM_DIR_INOUT";
                this.cppDirectionType = "ParameterDirection.InputOutput";
                this.cisFBAddParamFunction = "AddOutput";
                break;
        }
        cisParameterType = CISTYPES.get(pType);
        if (cisParameterType == null)
            cisParameterType = "Unknown";
        //
        switch (cisParameterType) {
            case "Char":
            case "Varchar2":
                this.cisFBParameterType = "String";
                break;
            case "Int64":
            case "Int32":
            case "Int16":
                this.cisFBParameterType = cisParameterType;
                break;
            case "Date":
                this.cisFBParameterType = "DateTime";
                break;
            default:
                this.cisFBParameterType = "TBC";
        }
        this.cisFBAddParamFunction += this.cisFBParameterType;
        if (IsStringType())
            this.plsInitValue = "''";
        else if (IsNumericType())
            this.plsInitValue = "0";
        else if (IsDateTimeType())
            this.plsInitValue = "sysdate";
    }

    public final boolean IsStringType() {
        return STRTYPES.contains(plsType);
    }

    public final boolean IsNumericType() {
        return NUMTYPES.contains(plsType);
    }
    
    public final boolean IsDateTimeType() {
        return DATTYPES.contains(plsType);
    }

    public String getName() {
        return name;
    }

    public String getDirectionType() {
        return DirectionType;
    }

    public String getType() {
        return plsType;
    }

    public String getCppDirectionType() {
        return cppDirectionType;
    }

    public String getCisDirectionType() {
        return cisDirectionType;
    }

    public String getCisParameterType() {
        return "OracleDbType."+cisParameterType;
    }

    public String getCisFBParameterType() {
        return cisFBParameterType;
    }
    
    public String getPlSQLInitialValue() {
        return plsInitValue;
    }

    public void setName(String name) {
        this.name = name;
    }
/*
    public void setType(String type) {
        this.DirectionType = type;
        if (null != type)
            switch (type) {
            case "IN":
                this.cppDirectionType = "PARAM_DIR_IN";
                this.cisDirectionType = "ParameterDirection.Input";
                break;
            case "OUT":
                this.cppDirectionType = "PARAM_DIR_OUT";
                this.cisDirectionType = "ParameterDirection.Output";
                break;
            case "IN/OUT":
                this.cppDirectionType = "PARAM_DIR_INOUT";
                this.cppDirectionType = "ParameterDirection.InputOutput";
                break;
    }
*/
    public String getCisFBAddParamFunction() {
        return cisFBAddParamFunction;
    }
}
