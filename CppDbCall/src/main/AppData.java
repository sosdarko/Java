/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 *
 * @author darko.sos
 */
public class AppData {
    ArrayList<Branch> branches;
    
    String tnsNamesPath;

    public AppData() {
        branches = new ArrayList<>();
    }
    
    void Save()
    {
       try{
         FileOutputStream fos= new FileOutputStream("appdata.txt");
         ObjectOutputStream oos= new ObjectOutputStream(fos);
         oos.writeObject(branches);
         oos.writeObject(tnsNamesPath);
         oos.close();
         fos.close();
       }
       catch(IOException ioe){
            ioe.printStackTrace();
       }
    }
    
    void Load()
    {
        try
        {
            FileInputStream fis = new FileInputStream("appdata.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            branches = (ArrayList) ois.readObject();
            tnsNamesPath = (String) ois.readObject();
            ois.close();
            fis.close();
        }
        catch(IOException ioe)
        {
             //ioe.printStackTrace();
             return;
        }
        catch(ClassNotFoundException c)
        {
             System.out.println("Class not found");
             c.printStackTrace();
             return;
        }
    }

    public ArrayList<Branch> getBranches() {
        return branches;
    }
    
    public void addBranch(String bname, String connstr) {
        branches.add(new Branch(bname, connstr));
    }
    
    public void addBranch(Branch b) {
        branches.add(b);
    }
    
    public boolean delBranch(String bname) {
        int n = 0;
        Branch b = branches.get(n);
        while (n < branches.size() && (b.name == null ? bname != null : !b.name.equals(bname))) {
            n = n + 1;
            b = branches.get(n);
        }
        if (b != null && b.name.equals(bname)) {
            branches.remove(b);
            return true;
        }
        return false;
    }
    
    public boolean delBranch(Branch b) {
        return branches.remove(b);
    }

    public String getTnsNamesPath() {
        return tnsNamesPath;
    }

    public void setTnsNamesPath(String tnsNamesPath) {
        this.tnsNamesPath = tnsNamesPath;
    }
}
