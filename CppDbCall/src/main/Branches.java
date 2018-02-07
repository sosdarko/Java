/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

/**
 *
 * @author darko.sos
 */
public class Branches implements ComboBoxModel<Branch> {
    ArrayList<Branch> b;
    int nSelectedItem;
    ArrayList<ListDataListener> listeners = new ArrayList<>();

    public Branches() {
        nSelectedItem = -1;
    }

    public Branches(ArrayList<Branch> _b)
    {
        nSelectedItem = -1;
        b = _b;
    }

    @Override
    public Object getSelectedItem() {
        if (nSelectedItem < 0)
            return null;
        else {
            if (nSelectedItem < b.size()) {
                return b.get(nSelectedItem);
            }
            else {
                if (b.size() > 0) {
                    return b.get(0);
                }
                else
                    return null;
            }
        }
    }

    @Override
    public void setSelectedItem(Object anItem) {
        Branch branch = (Branch) anItem;
        if (b.contains(branch)) {
            nSelectedItem = b.indexOf(branch);
        }
        else
            nSelectedItem = -1;
    }

    @Override
    public int getSize() {
        return b.size();
    }

    @Override
    public Branch getElementAt(int index) {
        return b.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }
}
