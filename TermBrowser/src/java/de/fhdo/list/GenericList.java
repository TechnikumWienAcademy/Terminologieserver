/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2013 FH Dortmund: Peter Haas, Robert Muetzner
 * government-funded by the Ministry of Health of Germany
 * government-funded by the Ministry of Health, Equalities, Care and Ageing of North Rhine-Westphalia and the European Union
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *  
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhdo.list;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Footer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Separator;
import org.zkoss.zul.South;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Diese Klasse ist die Hauptklasse der generischen Liste. Sie wird dazu
 * verwendet, die Liste zu initialisieren.
 *
 * @author Robert M�tzner <robert.muetzner@fh-dortmund.de>
 */
public class GenericList extends Window implements IDoubleClick, IUpdateData
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private List<GenericListRowType> dataList;
    private List<GenericListHeaderType> listHeader;
    private ListModelList listModel;
    //private boolean newEditDelete;
    private boolean button_new;
    private boolean button_edit;
    private boolean button_delete;
    private ListModelList listModelFilter;
    private GenericListItemRenderer listitemRenderer;
    private IGenericListActions listActions;
    private IUpdateData updateDataListener;
    private int lastSelectedIndex;
    private boolean oneFilter;
    private List<Button> customButtonList;
    private String listId;
    private int countButtonsAtBegin;
    private boolean showCount;
    private boolean alertOnEmptySelection = true;

    private HashMap<GenericListHeaderType, Object> appliedFilters = new HashMap<GenericListHeaderType, Object>();

    public GenericList()
    {
        listHeader = new LinkedList<GenericListHeaderType>();
        lastSelectedIndex = -1;
        oneFilter = false;
        customButtonList = new LinkedList<Button>();

    }

    public void onListSelected(SelectEvent event)
    {
        showButtonMode();
        Component c = event.getReference();
        Listitem item = (Listitem) c;
        
        if (listActions != null)
        {
            Object o = getSelection();

            if (o != null)
            {
                listActions.onSelected(listId, ((GenericListRowType) item.getValue()).getData());
            }
            else
            {
                if(this.alertOnEmptySelection)
                {
                    Clients.alert("Keine Auswahl! Bitte w�hlen Sie einen Eintrag aus der Liste aus");
                }
            }
        }
    }

    private void createListModel(boolean checkmark, boolean multiple)
    {
        listModel = new ListModelList(dataList);
        listModel.setMultiple(multiple);

        Listbox lb = (Listbox) getFellow("listbox");
        lb.setModel(listModel);
        lb.setCheckmark(checkmark);

        if (listitemRenderer == null)
        {
            listitemRenderer = new GenericListItemRenderer(listHeader);
            listitemRenderer.setDoubleClickEvent(this);
            listitemRenderer.setUpdateDataEvent(this);
            lb.setItemRenderer(listitemRenderer);
        }

        showButtonMode();
        showCountFooter();

    }

    private void showCountFooter()
    {
        if (showCount)
        {
            String s = "";

            if (listModelFilter != null && listModelFilter.size() != listModel.size())
            {
                s = "Anzahl: " + listModelFilter.size() + " / " + listModel.size();
            }
            else
            {
                s = "Anzahl: " + listModel.size();
            }

            ((Footer) getFellow("footer_category")).setLabel(s);
            ((Grid) getFellow("gridCount")).setVisible(s.length() > 0);
        }
    }

    private void showButtonMode()
    {
        try
        {
            Object o = getSelection();
            boolean disabled = (o == null);

            ((Button) getFellow("buttonEdit")).setDisabled(disabled);
            ((Button) getFellow("buttonDelete")).setDisabled(disabled);

            ((Button) getFellow("buttonNew")).setDisabled(false);

            for (int i = 0; i < customButtonList.size(); ++i)
            {
                customButtonList.get(i).setDisabled(disabled);
            }
        }
        catch (Exception e)
        {
            logger.error("Fehler in showButtonMode(): " + e.getLocalizedMessage());
        }
    }
    
    public Object getSelection()
    {
        logger.debug("getSelection()");

        Object object = null;

        try
        {
            Listbox box = (Listbox) getFellow("listbox");

            lastSelectedIndex = box.getSelectedIndex();
            if (lastSelectedIndex >= 0)
            {
                //object = (Task) taskListModel.getListModel().getElementAt(box.getSelectedIndex());
                Listitem li = box.getSelectedItem();

                if (li != null)
                {
                    object = li.getValue();
                }
            }
        }
        catch (Exception ex)
        {
            logger.warn("[GenericList.java] getSelection-Fehler: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        return object;
    }

    public void addEntry(GenericListRowType row)
    {
        logger.debug("addEntry()");
        listModel.add(listModel.size(), row);

        Listbox lb = getListbox();
        if (lb.getItemCount() > 0)
        {
            Clients.scrollIntoView(lb.getItems().get(lb.getItemCount() - 1));
        }

        showCountFooter();
    }

    public void addEntry(GenericListRowType row, int index)
    {
        logger.debug("addEntryWithIndex()");
        listModel.add(index, row);

        Listbox lb = getListbox();
        if (lb.getItemCount() > 0)
        {
            Clients.scrollIntoView(lb.getItems().get(lb.getItemCount() - 1));
        }

        showCountFooter();
    }

    public void updateEntry(GenericListRowType row)
    {
        logger.debug("updateEntry(): " + row.getClass().getCanonicalName());
        if (lastSelectedIndex >= 0)
        {
            listModel.remove(lastSelectedIndex);
            listModel.add(lastSelectedIndex, row);
        }
    }

    public void updateEntry(GenericListRowType row, int index)
    {
        lastSelectedIndex = index;
        updateEntry(row);
    }

    public void onNew() throws InterruptedException
    {
        if (listActions != null)
        {
            listActions.onNewClicked(listId);
        }
    }

    public void onDoubleClick(Object o)
    {
        try
        {
            onEdit();
        }
        catch (InterruptedException ex)
        {
            logger.error(ex.getLocalizedMessage());
        }
    }

    public void onEdit() throws InterruptedException
    {
        logger.debug("onEdit()");

        if (listActions != null)
        {
            Object o = getSelection();

            if (o != null)
            {
                listActions.onEditClicked(listId, ((GenericListRowType) o).getData());
            }
            else
            {
                if(this.alertOnEmptySelection)
                {
                    Clients.alert("Keine Auswahl! Bitte w�hlen Sie einen Eintrag aus der Liste aus");
                }
            }
        }
    }

    public void onDelete() throws InterruptedException
    {
        if (listActions != null)
        {
            try
            {
                // Dialog + Eintrag l�schen
                if (logger.isDebugEnabled())
                {
                    logger.debug("onDeleteClicked");
                }

                Object o = getSelection();

                if (o != null)
                {

                    if (Messagebox.show("M�chten Sie den ausgew�hlten Eintrag wirklich l�schen?", "L�schen", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION) == Messagebox.YES)
                    {
                        // Liste dynamisch erneuern
                        listModel.remove(o);

                        listActions.onDeleted(listId, ((GenericListRowType) o).getData());

                        showCountFooter();
                    }
                    else
                    {
                        logger.info("Nein geklickt");
                    }
                }
            }
            catch (Exception ex)
            {
                logger.error(ex.getMessage());
            }
        }
    }

    private void filterChanged(GenericListHeaderType head, Date Begin, Date End)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("filterChanged");
            if (Begin != null)
            {
                logger.debug("begin: " + Begin.toString());
            }
            if (End != null)
            {
                logger.debug("end: " + End.toString());
            }
        }

        //logger.debug("filterChanged: " + filter + ", index: " + head.getIndex());
        listModelFilter = new ListModelList();

        if (listModel != null)
        {
            for (int i = 0; i < listModel.getSize(); ++i)
            {
                GenericListRowType row = (GenericListRowType) listModel.get(i);
                GenericListCellType cell = row.getCells()[head.getIndex()];
                Object compareWith = cell.getData();

                if (filterObjectContains(compareWith, Begin, End))
                {
                    // Diesen verwenden
                    listModelFilter.add(row);
                }
            }
        }

        Listbox lb = (Listbox) getFellow("listbox");
        lb.setModel(listModelFilter);

        showButtonMode();
        showCountFooter();

        /*filterDateBegin = Begin;
     filterDateEnd = End;

     // Filter anwenden
     //taskList = null;
     reloadData = true;
     initData();

     // Model updaten (IUpdate)
     if (updateListener != null)
     updateListener.update(listModel);*/
    }

    private boolean isFilterRemoved(Object filter)
    {

        if (filter instanceof LinkedList)
        {
            LinkedList list = (LinkedList) filter;
            if (list.size() == 0)
            {
                return true;
            }
        }

        if (filter instanceof String)
        {
            String filterString = (String) filter;
            if (filterString.equals(""))
            {
                return true;
            }
        }

        return false;
    }

    private void filterChanged(GenericListHeaderType head, Object filter)
    {
        // Filter anwenden
        logger.debug("filterChanged: " + filter + ", index: " + head.getIndex());

        if (isFilterRemoved(filter))
        {
            appliedFilters.remove(head);
        }
        else
        {
            appliedFilters.put(head, filter);
        }

        //listModelFilter = new ListModelList();
        listModelFilter = new ListModelList(dataList);

        Iterator<Map.Entry<GenericListHeaderType, Object>> itFilterSet = appliedFilters.entrySet().iterator();

        while (itFilterSet.hasNext())
        {
            Map.Entry<GenericListHeaderType, Object> filterEntry = itFilterSet.next();

            if (listModelFilter != null)
            {
                //for (int i = 0; i <= listModelFilter.getSize(); ++i)
                for (int i = listModelFilter.getSize() - 1; i >= 0; i--)
                {
                    GenericListRowType row = (GenericListRowType) listModelFilter.get(i);
                    GenericListCellType cell = row.getCells()[filterEntry.getKey().getIndex()];
                    Object compareWith = cell.getDisplayData();

                    if (!filterObjectContains(compareWith, filterEntry.getValue(), false))
                    {
                        // Diesen verwenden
                        listModelFilter.remove(row);

                    }
                }
            }
        }

        Listbox lb = (Listbox) getFellow("listbox");
        lb.setModel(listModelFilter);

        showButtonMode();
        showCountFooter();
    }

    private boolean filterObjectContains(Object obj, Date from, Date to)
    {
        if (obj == null || (from == null && to == null))
        {
            return true;
        }

        logger.debug("filterObjectContains(), " + obj.getClass().getCanonicalName());

        Date datum = null;
        if (obj instanceof java.sql.Timestamp)
        {
            datum = (Date) obj;
        }
        else if (obj instanceof Date)
        {
            datum = (Date) obj;
        }

        if (datum != null)
        {
            logger.debug("Compare with: " + datum.toString());

            if (from != null && to != null)
            {
                if (datum.after(from) && datum.before(to))
                {
                    return true;
                }
            }
            else if (from != null && to == null)
            {
                if (datum.after(from))
                {
                    return true;
                }
            }
            else if (from == null && to != null)
            {
                if (datum.before(to))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean filterObjectContains(Object obj, Object filter, boolean equals)
    {
        if (obj == null || filter == null)
        {
            return true;
        }

        // TODO weitere Typen ber�cksichtigen (Date, Boolean, ...)
        if (filter instanceof String)
        {
            String sFilter = filter.toString();
            if (sFilter.length() > 0)
            {
                if (equals)
                {
                    if (obj.toString().equals(sFilter))
                    {
                        return true;
                    }
                }
                else
                {
                    String s = obj.toString().toLowerCase();

                    if (s.contains(sFilter.toLowerCase()))
                    {
                        return true;
                    }
                }
            }
            else
            {
                return true;
            }
        }
        else if (filter instanceof Boolean)
        {
            Boolean bFilter = (Boolean) filter;
            Boolean bCompare = null;
            if (obj instanceof Checkbox)
            {
                bCompare = (Boolean) ((Checkbox) obj).isChecked();
            }
            else
            {
                bCompare = (Boolean) obj;
            }

            /*logger.debug("filter.toString(): " + filter.toString());
       logger.debug("obj.toString(): " + obj.toString());
       logger.debug("bFilter: " + bFilter);
       logger.debug("bCompare: " + bCompare);*/
            if (bFilter != null)
            {
                if (bFilter.booleanValue() == bCompare.booleanValue())
                {
                    return true;
                }
            }

        }
        else if (filter instanceof List)
        {
            List<String> listFilter = (List<String>) filter;
            if (listFilter.size() > 0)
            {
                for (int i = 0; i < listFilter.size(); ++i)
                {
                    if (filterObjectContains(obj, listFilter.get(i), true))
                    {
                        return true;
                    }
                }
            }
            else
            {
                return true;
            }

        }

        return false;
    }

    /**
     * @return the dataList
     */
    public List<GenericListRowType> getDataList()
    {
        return dataList;
    }

    /**
     * @param dataList the dataList to set
     * @param checkmark
     */
    public void setDataList(List<GenericListRowType> dataList, boolean checkmark, boolean multiple)
    {
        this.dataList = dataList;
        createListModel(checkmark, multiple);
    }

    /**
     * @param dataList the dataList to set
     */
    public void setDataList(List<GenericListRowType> dataList)
    {
        this.setDataList(dataList, false, false);
    }

    /**
     * @return the listHeader
     */
    public List<GenericListHeaderType> getListHeader()
    {
        return listHeader;
    }

    private int compareContent(boolean ascending, Object cell1, Object cell2)
    {
        int result = 0;

        if (cell1 instanceof Integer && cell2 instanceof Integer)
        {
            int wert1 = (Integer) cell1;
            int wert2 = (Integer) cell2;

            if (wert1 > wert2)
            {
                result = 1;
            }
            else if (wert1 < wert2)
            {
                result = -1;
            }
        }
        else if (cell1 instanceof Date && cell2 instanceof Date)
        {
            Date wert1 = (Date) cell1;
            Date wert2 = (Date) cell2;

            if (wert1.after(wert2))
            {
                result = 1;
            }
            else if (wert1.before(wert2))
            {
                result = -1;
            }
        } // TODO weitere Typen hinzuf�gen
        else if (cell1 != null && cell2 != null)
        {
            String s1 = cell1.toString();
            String s2 = cell2.toString();

            result = s1.compareToIgnoreCase(s2);
        }

        if (ascending == false)
        {
            result = (-1) * result;
        }

        //logger.debug("Compare '" + s1 + "' with '" + s2 + "': " + result);
        return result;

    }

    /**
     * @param listHeader the listHeader to set
     */
    public void setListHeader(List<GenericListHeaderType> listHeader)
    {
        oneFilter = false;
        this.listHeader = listHeader;

        // Header zuweisen
        Listhead listhead = (Listhead) getFellow("listHeader");
        listhead.getChildren().clear();

        if (listHeader != null)
        {
            // Bestimmen, ob es einen Filter gibt
            for (int i = 0; i < listHeader.size(); ++i)
            {
                if (listHeader.get(i).isShowFilter())
                {
                    oneFilter = true;
                    break;
                }
            }

            for (int i = 0; i < listHeader.size(); ++i)
            {
                listHeader.get(i).setIndex(i);
                final GenericListHeaderType head = listHeader.get(i);

                Listheader lh = new Listheader(head.getName());
                lh.setImage(head.getImage());
                if (head.getWidth() > 0)
                {
                    lh.setWidth(head.getWidth() + "px");
                }

                if (head.isAllowSorting())
                {
                    lh.setSortAscending(new Comparator()
                    {
                        public int compare(Object o1, Object o2)
                        {
                            int index = head.getIndex();
                            //logger.debug("Asc Index: " + head.getIndex() + ", Compare Type: " + o1.getClass().getCanonicalName());
                            if (o1 != null && o2 != null)
                            {
                                Object cell1 = ((GenericListRowType) o1).getCells()[index];
                                Object cell2 = ((GenericListRowType) o2).getCells()[index];

                                return compareContent(true, ((GenericListCellType) cell1).getDisplayData(), ((GenericListCellType) cell2).getDisplayData());
                            }

                            return 0;
                        }
                    });

                    lh.setSortDescending(new Comparator()
                    {
                        public int compare(Object o1, Object o2)
                        {
                            //logger.debug("Desc Index: " + head.getIndex() + ", Compare Type: " + o1.getClass().getCanonicalName());
                            int index = head.getIndex();
                            if (o1 != null && o2 != null)
                            {
                                Object cell1 = ((GenericListRowType) o1).getCells()[index];
                                Object cell2 = ((GenericListRowType) o2).getCells()[index];

                                return compareContent(false, ((GenericListCellType) cell1).getDisplayData(), ((GenericListCellType) cell2).getDisplayData());
                            }

                            return 0;
                        }
                    });
                }
                /*if(head.isAllowSorting())
         {
         lh.setSort("auto()");
         }*/

                // TODO weitere Filter einf�gen
                if (head.isShowFilter())
                {

                    logger.debug("head.isShowFilter() = true");

                    if (head.getDatatype() instanceof String && head.getDatatype().toString().equalsIgnoreCase("String"))
                    {
                        logger.debug("Filter Textbox anlegen");

                        // Textfeld-Filter
                        Textbox tbFilter = new Textbox();
                        tbFilter.setId("tbFilter_" + i);
                        tbFilter.setWidth("97%");
                        tbFilter.setMold("rounded");
                        tbFilter.setInstant(true);
                        tbFilter.addEventListener(Events.ON_CHANGING, new EventListener<Event>()
                        {
                            public void onEvent(Event event) throws Exception
                            {
                                // Filter ge�ndert
                                filterChanged(head, ((InputEvent) event).getValue());
                            }
                        });

                        lh.appendChild(new Separator());
                        lh.appendChild(tbFilter);
                    }
                    else if (head.getDatatype() instanceof String[])
                    {
                        // Filter f�r String-Liste anlegen (Bandbox mit Listbox)
                        // Hier k�nnen mehrere Elemente aus der Listebox selektiert werden
                        logger.debug("Filter Combobox anlegen");
                        String[] sItems = (String[]) head.getDatatype();

                        final Bandbox bbFilter = new Bandbox();
                        bbFilter.setMold("rounded");
                        bbFilter.setAutodrop(true);
                        bbFilter.setReadonly(true);
                        bbFilter.setWidth("99%");
                        bbFilter.setId("bbFilter_" + i);

                        Bandpopup bpFilter = new Bandpopup();
                        bpFilter.setWidth("300px");

                        final Listbox lbFilter = new Listbox();
                        lbFilter.setId("lbFilter_" + i);
                        lbFilter.setMultiple(true);
                        lbFilter.setCheckmark(true);

                        Listhead lhFilter = new Listhead();
                        Listheader lheaderFilter = new Listheader(head.getName());
                        lheaderFilter.setHeight("20px");
                        Div divFilter = new Div();
                        divFilter.setAlign("right");
                        divFilter.setStyle("float:right; margin-top:-3px;");

                        Button buttonClear = new Button("");
                        buttonClear.setImage("/rsc/img/genericlist/clear.png");
                        buttonClear.setHeight("26px");
                        buttonClear.addEventListener(Events.ON_CLICK, new EventListener<Event>()
                        {
                            public void onEvent(Event t) throws Exception
                            {
                                if (bbFilter != null)
                                {
                                    for (Listitem li : lbFilter.getItems())
                                    {
                                        li.setSelected(false);
                                    }
                                    bbFilter.setOpen(false);
                                    // Event ausl�sen
                                    Events.sendEvent(bbFilter, new OpenEvent(Events.ON_OPEN, bbFilter, false));
                                }
                            }
                        });
                        divFilter.getChildren().add(buttonClear);

                        divFilter.getChildren().add(new Separator("vertical"));

                        Button buttonFilter = new Button("OK");
                        buttonFilter.setHeight("26px");
                        buttonFilter.addEventListener(Events.ON_CLICK, new EventListener<Event>()
                        {
                            public void onEvent(Event t) throws Exception
                            {
                                if (bbFilter != null)
                                {
                                    bbFilter.setOpen(false);
                                    // Event ausl�sen
                                    Events.sendEvent(bbFilter, new OpenEvent(Events.ON_OPEN, bbFilter, false));
                                }
                            }
                        });
                        divFilter.getChildren().add(buttonFilter);
                        lheaderFilter.getChildren().add(divFilter);
                        lhFilter.getChildren().add(lheaderFilter);

                        lbFilter.getChildren().add(lhFilter);

                        for (int j = 0; j < sItems.length; ++j)
                        {
                            Listitem liFilter = new Listitem(sItems[j]);
                            lbFilter.getChildren().add(liFilter);
                        }

                        bpFilter.getChildren().add(lbFilter);
                        bbFilter.getChildren().add(bpFilter);

                        lh.appendChild(new Separator());
                        lh.appendChild(bbFilter);

                        bbFilter.addEventListener(Events.ON_OPEN, new EventListener<Event>()
                        {
                            public void onEvent(Event e) throws Exception
                            {
                                if (e instanceof org.zkoss.zk.ui.event.OpenEvent)
                                {
                                    org.zkoss.zk.ui.event.OpenEvent event = (org.zkoss.zk.ui.event.OpenEvent) e;

                                    if (event.isOpen() == false)
                                    {
                                        logger.debug("ON_OPEN = false");

                                        // Filter
                                        List<String> filterList = new LinkedList<String>();

                                        // 1. Text setzen in Bandbox
                                        String s = "";

                                        Set<Listitem> items = lbFilter.getSelectedItems();
                                        if (items != null)
                                        {
                                            Iterator<Listitem> it = items.iterator();
                                            while (it.hasNext())
                                            {
                                                Listitem li = it.next();
                                                String sValue = li.getLabel();
                                                if (items.size() == 1)
                                                {
                                                    s = sValue;
                                                }
                                                else
                                                {
                                                    s = "...";
                                                }
                                                filterList.add(sValue);
                                            }
                                        }

                                        bbFilter.setText(s);

                                        // TODO 2. Filter �ndern
                                        // Filter ge�ndert
                                        //filterChanged(head, s)
                                        filterChanged(head, filterList);
                                        //taskListModel.filterChangedMultiple("t.priorityCd", filterList);
                                    }
                                    else
                                    {
                                        logger.debug("bandbox opened");
                                    }
                                }
                            }
                        });
                    }
                    else if (head.getDatatype() instanceof String
                            && (head.getDatatype().toString().equalsIgnoreCase("DateTime")
                            || head.getDatatype().toString().equalsIgnoreCase("Date")))
                    {
                        // Datums-Auswahl anzeigen
                        logger.debug("Filter Datebox anlegen");


                        /*Textbox tbFilter = new Textbox();
             tbFilter.setId("tbFilter_" + i);
             tbFilter.setWidth("97%");
             tbFilter.setMold("rounded");
             tbFilter.setInstant(true);
             tbFilter.addEventListener(Events.ON_CHANGING, new EventListener<Event>()
             {
             public void onEvent(Event event) throws Exception
             {
             // Filter ge�ndert
             filterChanged(head, ((InputEvent) event).getValue());
             }
             });*/
                        final Bandbox bbDatumFilter = new Bandbox();
                        bbDatumFilter.setId("bbDatumFilter_" + i);
                        bbDatumFilter.setMold("rounded");
                        bbDatumFilter.setAutodrop(true);
                        bbDatumFilter.setReadonly(true);
                        bbDatumFilter.setWidth("99%");

                        Bandpopup bpFilter = new Bandpopup();
                        bpFilter.setWidth("340px");
                        final Radiogroup rgFilter = new Radiogroup();

                        Radio radio = new Radio("kein Filter");
                        radio.addEventListener(Events.ON_CLICK, new EventListener<Event>()
                        {
                            public void onEvent(Event t) throws Exception
                            {
                                // Event ausl�sen
                                bbDatumFilter.setOpen(false);
                                Events.sendEvent(bbDatumFilter, new OpenEvent(Events.ON_OPEN, bbDatumFilter, false));
                            }
                        });

                        rgFilter.appendChild(radio);
                        rgFilter.appendChild(new Separator());
                        rgFilter.appendChild(new Radio("kleiner als"));
                        rgFilter.appendChild(new Separator("vertical"));
                        final Datebox dbFilter1 = new Datebox();
                        dbFilter1.addEventListener(Events.ON_CHANGE, new EventListener<Event>()
                        {
                            public void onEvent(Event t) throws Exception
                            {
                                rgFilter.setSelectedIndex(1);
                                // Event ausl�sen
                                bbDatumFilter.setOpen(false);
                                Events.sendEvent(bbDatumFilter, new OpenEvent(Events.ON_OPEN, bbDatumFilter, false));
                            }
                        });
                        rgFilter.appendChild(dbFilter1);
                        rgFilter.appendChild(new Separator());

                        rgFilter.appendChild(new Radio("gr��er als"));
                        rgFilter.appendChild(new Separator("vertical"));
                        final Datebox dbFilter2 = new Datebox();
                        dbFilter2.addEventListener(Events.ON_CHANGE, new EventListener<Event>()
                        {
                            public void onEvent(Event t) throws Exception
                            {
                                rgFilter.setSelectedIndex(2);
                                // Event ausl�sen
                                bbDatumFilter.setOpen(false);
                                Events.sendEvent(bbDatumFilter, new OpenEvent(Events.ON_OPEN, bbDatumFilter, false));
                            }
                        });
                        rgFilter.appendChild(dbFilter2);
                        rgFilter.appendChild(new Separator());

                        rgFilter.appendChild(new Radio("zwischen"));
                        rgFilter.appendChild(new Separator("vertical"));
                        final Datebox dbFilter3 = new Datebox();
                        dbFilter3.addEventListener(Events.ON_CHANGE, new EventListener<Event>()
                        {
                            public void onEvent(Event t) throws Exception
                            {
                                rgFilter.setSelectedIndex(3);
                            }
                        });
                        rgFilter.appendChild(dbFilter3);
                        rgFilter.appendChild(new Label(" und "));
                        final Datebox dbFilter4 = new Datebox();
                        dbFilter4.addEventListener(Events.ON_CHANGE, new EventListener<Event>()
                        {
                            public void onEvent(Event t) throws Exception
                            {
                                rgFilter.setSelectedIndex(3);
                            }
                        });
                        rgFilter.appendChild(dbFilter4);
                        rgFilter.appendChild(new Separator());

                        rgFilter.appendChild(new Radio("Vorlage"));
                        final Listbox lbVorlageFilter = new Listbox();
                        lbVorlageFilter.setId("lbVorlageFilter_" + i);
                        lbVorlageFilter.appendChild(new Listitem("Heute"));
                        lbVorlageFilter.appendChild(new Listitem("7 Tage"));
                        lbVorlageFilter.appendChild(new Listitem("1 Monat"));
                        lbVorlageFilter.appendChild(new Listitem("1 Jahr"));
                        lbVorlageFilter.addEventListener(Events.ON_SELECT, new EventListener<Event>()
                        {
                            public void onEvent(Event t) throws Exception
                            {
                                rgFilter.setSelectedIndex(4);

                                // Event ausl�sen
                                bbDatumFilter.setOpen(false);
                                Events.sendEvent(bbDatumFilter, new OpenEvent(Events.ON_OPEN, bbDatumFilter, false));
                            }
                        });
                        rgFilter.appendChild(lbVorlageFilter);

                        bpFilter.appendChild(rgFilter);
                        bbDatumFilter.appendChild(bpFilter);
                        lh.appendChild(new Separator());
                        lh.appendChild(bbDatumFilter);

                        bbDatumFilter.addEventListener(Events.ON_OPEN, new EventListener<Event>()
                        {
                            public void onEvent(Event e) throws Exception
                            {
                                if (e instanceof org.zkoss.zk.ui.event.OpenEvent)
                                {
                                    org.zkoss.zk.ui.event.OpenEvent event = (org.zkoss.zk.ui.event.OpenEvent) e;

                                    if (event.isOpen() == false)
                                    {
                                        logger.debug("Datum ON_OPEN = false");

                                        try
                                        {
                                            // 1. Text setzen in Bandbox
                                            String s = "";

                                            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                            Date begin = null, end = null;
                                            if (rgFilter.getSelectedIndex() == 1)
                                            {
                                                end = dbFilter1.getValue();
                                                s = "< " + sdf.format(end);
                                            }
                                            else if (rgFilter.getSelectedIndex() == 2)
                                            {
                                                begin = dbFilter2.getValue();
                                                s = "> " + sdf.format(begin);
                                            }
                                            else if (rgFilter.getSelectedIndex() == 3)
                                            {
                                                begin = dbFilter3.getValue();
                                                end = dbFilter4.getValue();
                                                s = "...";
                                            }
                                            else if (rgFilter.getSelectedIndex() == 4)
                                            {
                                                if (lbVorlageFilter.getSelectedIndex() == 0)
                                                {
                                                    // Heute
                                                    long ms = new Date().getTime();
                                                    begin = new Date(ms - (ms % 86400000));
                                                    end = new Date(ms - (ms % 86400000) + 86400000);
                                                    s = "Heute";
                                                }
                                                else if (lbVorlageFilter.getSelectedIndex() == 1)
                                                {
                                                    // Diese Woche
                                                    long ms = new Date().getTime();
                                                    end = new Date(ms - (ms % 86400000));
                                                    begin = new Date(ms - (ms % 86400000) - (7 * 86400000));
                                                    s = "7 Tage";
                                                }
                                                else if (lbVorlageFilter.getSelectedIndex() == 2)
                                                {
                                                    // 1 Monat
                                                    long ms = new Date().getTime();
                                                    end = new Date(ms - (ms % 86400000));
                                                    begin = new Date(ms - (ms % 86400000) - (30l * 86400000l));
                                                    s = "1 Monat";
                                                }
                                                else if (lbVorlageFilter.getSelectedIndex() == 3)
                                                {
                                                    // 1 Jahr
                                                    long ms = new Date().getTime();
                                                    end = new Date(ms - (ms % 86400000));
                                                    begin = new Date(ms - (ms % 86400000) - (365l * 86400000l));
                                                    s = "1 Jahr";
                                                }
                                            }

                                            bbDatumFilter.setText(s);
                                            //taskListModel.filterChangedDate(begin, end);

                                            // TODO 2. Filter �ndern
                                            // Filter ge�ndert
                                            //filterChanged(head, s)
                                            filterChanged(head, begin, end);
                                            //taskListModel.filterChangedMultiple("t.priorityCd", filterList);
                                        }
                                        catch (Exception ex)
                                        {
                                            logger.warn("Fehler in GenericList.java, onEvent(): " + ex.getLocalizedMessage());
                                        }

                                    }
                                    else
                                    {
                                        logger.debug("bandbox opened");
                                    }
                                }
                            }
                        });
                    }
                    else if (head.getDatatype() instanceof String
                            && (head.getDatatype().toString().equalsIgnoreCase("boolean")
                            || head.getDatatype().toString().equalsIgnoreCase("bool")))
                    {
                        // Datums-Auswahl anzeigen
                        logger.debug("Filter Checkbox anlegen");

                        Div div = new Div();
                        div.appendChild(new Separator());

                        final Image img = new Image("/rsc/img/genericlist/checkbox_state3_16x16.png");
                        img.setAttribute("checkState", 0);
                        img.addEventListener(Events.ON_CLICK, new EventListener<Event>()
                        {
                            public void onEvent(Event t) throws Exception
                            {
                                int state = (Integer) img.getAttribute("checkState");

                                if (state == 0)
                                {
                                    img.setSrc("/rsc/img/genericlist/checkbox_checked_16x16.png");
                                    img.setAttribute("checkState", 1);
                                    filterChanged(head, true);
                                }
                                else if (state == 1)
                                {
                                    img.setSrc("/rsc/img/genericlist/checkbox_unchecked_16x16.png");
                                    img.setAttribute("checkState", 2);
                                    filterChanged(head, false);
                                }
                                else
                                {
                                    img.setSrc("/rsc/img/genericlist/checkbox_state3_16x16.png");
                                    img.setAttribute("checkState", 0);
                                    filterChanged(head, null);
                                }
                                //rgFilter.setSelectedIndex(0);

                            }
                        });

                        div.appendChild(img);

                        /*final Radiogroup rgFilter = new Radiogroup();
             rgFilter.setId("radiogroupFilter_" + i);
             rgFilter.appendChild(new Radio());
             rgFilter.appendChild(new Radio());
             rgFilter.appendChild(new Radio());
             rgFilter.setSelectedIndex(0);
            
             rgFilter.addEventListener(Events.ON_CHECK, new EventListener<Event>()
             {
             public void onEvent(Event t) throws Exception
             {
             logger.debug("Filter changed: " + rgFilter.getSelectedIndex());
                
             if(rgFilter.getSelectedIndex() == 0)
             filterChanged(head, null);
             else if(rgFilter.getSelectedIndex() == 1)
             filterChanged(head, Boolean.TRUE);
             else 
             filterChanged(head, Boolean.FALSE);
             }
             });
            
             div.appendChild(rgFilter);

             Hbox hboxFilter = new Hbox();
             hboxFilter.setStyle("margin-left:2px;");
             Image img = new Image("/rsc/img/genericlist/checkbox_state3_16x16.png");
             img.addEventListener(Events.ON_CLICK, new EventListener<Event>()
             {
             public void onEvent(Event t) throws Exception
             {
             rgFilter.setSelectedIndex(0);
             filterChanged(head, null);
             }
             });
             hboxFilter.appendChild(img);

             img = new Image("/rsc/img/genericlist/checkbox_checked_16x16.png");
             img.setStyle("margin-left:-1px;");
             img.addEventListener(Events.ON_CLICK, new EventListener<Event>()
             {
             public void onEvent(Event t) throws Exception
             {
             rgFilter.setSelectedIndex(1);
             filterChanged(head, Boolean.TRUE);
             }
             });
             hboxFilter.appendChild(img);

             img = new Image("/rsc/img/genericlist/checkbox_unchecked_16x16.png");
             img.setStyle("margin-left:-1px;");
             img.addEventListener(Events.ON_CLICK, new EventListener<Event>()
             {
             public void onEvent(Event t) throws Exception
             {
             rgFilter.setSelectedIndex(2);
             filterChanged(head, Boolean.FALSE);
             }
             });
             hboxFilter.appendChild(img);

             div.appendChild(hboxFilter);*/
                        lh.appendChild(new Separator());
                        lh.appendChild(div);
                    }
                    else
                    {
                        // Gr��e anpassen ohne Filter
                        logger.debug("oneFilter: " + oneFilter);
                        if (oneFilter)
                        {
                            lh.appendChild(new Separator());
                            lh.appendChild(new Separator());
                            lh.appendChild(new Separator());
                            lh.appendChild(new Separator());
                        }
                    }

                    /*<listheader label="Aufgabe" >
           <image src="/rsc/img/list/ui-text-field.png" />
           <separator/>
           <textbox id="filter_name" width="97%" mold="rounded"
           onChanging="winContent.contentFrame.filterChangedName(event)" instant="true"/>
           </listheader>*/
                }
                else
                {
                    // Gr��e anpassen ohne Filter
                    logger.debug("oneFilter: " + oneFilter);
                    if (oneFilter)
                    {
                        lh.appendChild(new Separator());
                        lh.appendChild(new Separator());
                        lh.appendChild(new Separator());
                        lh.appendChild(new Separator());
                    }
                }

                listhead.getChildren().add(lh);
            }
        }

        if (listitemRenderer != null)
        {
            listitemRenderer.setListHeader(listHeader);
        }
    }

    public void addCustomButton(Button button)
    {
        logger.debug("addCustomButton");

        button.setDisabled(true);
        button.setHeight("24px");

        if (customButtonList.contains(button) == false)
        {
            customButtonList.add(button);
        }
        else
        {
            logger.debug("Button bereits vorhanden");
        }

        Div div = (Div) getFellow("divEditButtons");
        div.setVisible(true);

        if (countButtonsAtBegin == 0)
        {
            countButtonsAtBegin = div.getChildren().size();
        }

        setSouthHeight();

        Separator sep = new Separator();
        sep.setSpacing("4px");
        sep.setOrient("vertical");

        div.appendChild(sep);
        div.appendChild(button);
    }

    /**
     * Entfernt alle benutzerdefinierte Buttons
     */
    public void removeCustomButtons()
    {
        Div div = (Div) getFellow("divEditButtons");

        if (countButtonsAtBegin == 0)
        {
            countButtonsAtBegin = div.getChildren().size();
        }

        int anzahl = div.getChildren().size();
        for (int i = countButtonsAtBegin; i < anzahl; ++i)
        {
            div.getChildren().remove(countButtonsAtBegin);
        }

        customButtonList.clear();
    }

    /**
     * @return the listActions
     */
    public IGenericListActions getListActions()
    {
        return listActions;
    }

    /**
     * @param listActions the listActions to set
     */
    public void setListActions(IGenericListActions listActions)
    {
        this.listActions = listActions;
    }

    /**
     * @return the button_new
     */
    public boolean isButton_new()
    {
        return button_new;
    }

    /**
     * @param button_new the button_new to set
     */
    public void setButton_new(boolean button_new)
    {
        this.button_new = button_new;

        Div div = (Div) getFellow("divEditButtons");
        div.setVisible(button_new || button_edit || button_delete || customButtonList.size() > 0);

        setSouthHeight();

        Button button = (Button) getFellow("buttonNew");
        button.setVisible(button_new);
    }

    private void setSouthHeight()
    {
        South south = (South) getFellow("south");

        Div div = (Div) getFellow("divEditButtons");
        if (div.isVisible())
        {
            south.setSize("33px");
            south.setVisible(true);
        }
        else
        {
            south.setSize("0px");
            south.setVisible(false);
        }
        //south
    }

    /**
     * @return the button_edit
     */
    public boolean isButton_edit()
    {
        return button_edit;
    }

    /**
     * @param button_edit the button_edit to set
     */
    public void setButton_edit(boolean button_edit)
    {
        this.button_edit = button_edit;

        Div div = (Div) getFellow("divEditButtons");
        div.setVisible(button_new || button_edit || button_delete || customButtonList.size() > 0);

        setSouthHeight();

        Button button = (Button) getFellow("buttonEdit");
        button.setVisible(button_edit);
    }

    /**
     * @return the button_delete
     */
    public boolean isButton_delete()
    {
        return button_delete;
    }

    /**
     * @param button_delete the button_delete to set
     */
    public void setButton_delete(boolean button_delete)
    {
        this.button_delete = button_delete;

        Div div = (Div) getFellow("divEditButtons");
        div.setVisible(button_new || button_edit || button_delete || customButtonList.size() > 0);

        setSouthHeight();

        Button button = (Button) getFellow("buttonDelete");
        button.setVisible(button_delete);
    }

    public void onCellUpdated(int cellIndex, Object data, GenericListRowType row)
    {
        if (updateDataListener != null)
        {
            updateDataListener.onCellUpdated(cellIndex, data, row);
        }
    }

    /**
     * @param updateDataListener the updateDataListener to set
     */
    public void setUpdateDataListener(IUpdateData updateDataListener)
    {
        this.updateDataListener = updateDataListener;
    }

    /**
     * @return the listId
     */
    public String getListId()
    {
        return listId;
    }

    /**
     * @param listId the listId to set
     */
    public void setListId(String listId)
    {
        this.listId = listId;
    }

    public Listbox getListbox()
    {
        return (Listbox) getFellow("listbox");
    }

    /**
     * @param showCount the showCount to set
     */
    public void setShowCount(boolean showCount)
    {
        this.showCount = showCount;

        showCountFooter();
    }

    public void setAlertOnEmptySelection(boolean alertOnEmptySelection)
    {
        this.alertOnEmptySelection = alertOnEmptySelection;
    }
}
