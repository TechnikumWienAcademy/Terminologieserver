/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.tree;


/**
 *
 * @author Robert M�tzner
 */
public interface IUpdateData
{
   public void onCellUpdated(int cellIndex, Object data, GenericTreeRowType row);
}
