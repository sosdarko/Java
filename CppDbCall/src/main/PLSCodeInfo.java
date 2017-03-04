/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;

/**
 *
 * @author darko.sos
 */
public class PLSCodeInfo {
    String sCodeType;
    String sCodeName;
    int nCodeStart;
    int nCodeEnd;
    ArrayList<String> sErrorCodeList = new ArrayList<>();

    public PLSCodeInfo(String sCodeType, String sCodeName, int nCodeStart, int nCodeEnd, ArrayList<String> sErrorCodeList) {
        this.sCodeType = sCodeType;
        this.sCodeName = sCodeName;
        this.nCodeStart = nCodeStart;
        this.nCodeEnd = nCodeEnd;
        this.sErrorCodeList = sErrorCodeList;
    }

    public PLSCodeInfo(String sCodeType, String sCodeName, int nCodeStart, int nCodeEnd) {
        this.sCodeType = sCodeType;
        this.sCodeName = sCodeName;
        this.nCodeStart = nCodeStart;
        this.nCodeEnd = nCodeEnd;
    }

    public void InitializeInfoList() {
        if (sErrorCodeList != null)
            sErrorCodeList.clear();
        else
            sErrorCodeList = new ArrayList<>();
    }
    
    public void setErrorCodeList(ArrayList<String> sErrorCodeList) {
        this.sErrorCodeList = sErrorCodeList;
    }

    public void addCodeToErrorCodeList(String sCode) {
        if (!sErrorCodeList.contains(sCode))
            this.sErrorCodeList.add(sCode);
    }

    public void setCodeEnd(int nCodeEnd) {
        this.nCodeEnd = nCodeEnd;
    }

    public boolean HaveErrorCodes() {
        return (sErrorCodeList != null && sErrorCodeList.size()>0);
    }
    
    public String FormatErrorCodeList() {
        if (HaveErrorCodes())
            return String.join(", ", sErrorCodeList);
        else
            return "";
    }

    public static final String GetValidationTestSQL(String sList) {
        String sql;
        sql =
                "select * from fo.foerrorcode where idfoerrorcode in \n" +
                "(select column_value from table(fo.ARRNUMBER(\n" +
                sList + ")));";
        return sql;
    }

    public static final String GetExistenceTestSQL(String sList) {
        String sql;
        sql =
                "select column_value from table(fo.ARRNUMBER(\n" +
                sList + "))\n" + " minus \n" +
                "select idfoerrorcode from fo.foerrorcode;";
        return sql;
    }

    public String GetTestSQL() {
        String sql;

        sql = "";
        if (HaveErrorCodes()) {
            sql =
                    "select * from fo.foerrorcode where idfoerrorcode in \n" +
                    "(select column_value from table(fo.ARRNUMBER(\n" +
                    FormatErrorCodeList() + ")));";
        }
        return sql;
    }
    
    @Override
    public String toString() {
        String out = String.format("TYPE=%1$s NAME=%2$s LINES=%3$d:%4$d", sCodeType, sCodeName, nCodeStart, nCodeEnd);
        if (sErrorCodeList != null && sErrorCodeList.size()>0) { out = out + " CODES=" + String.join(", ", sErrorCodeList); }
        return out;
    }
}
