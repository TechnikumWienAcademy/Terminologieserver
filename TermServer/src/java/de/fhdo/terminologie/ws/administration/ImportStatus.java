package de.fhdo.terminologie.ws.administration;

/**
 * Objects of this class only hold information about the status of an import.
 * @author Stefan Puraner
 */
public class ImportStatus {
    
    public boolean cancel = false;
    public boolean importRunning = false;
    public boolean exportRunning = false;
    public int importCount = 0;
    public int importTotal = 0;
    private String currentTask = "";

    public boolean isCancel(){
        return cancel;
    }

    public void setCancel(boolean cancel){
        this.cancel = cancel;
    }

    public boolean isImportRunning(){
        return importRunning;
    }

    public void setImportRunning(boolean importRunning){
        this.importRunning = importRunning;
    }

    public boolean isExportRunning(){
        return exportRunning;
    }

    public void setExportRunning(boolean exportRunning){
        this.exportRunning = exportRunning;
    }

    public int getImportCount(){
        return importCount;
    }

    public void setImportCount(int importCount){
        this.importCount = importCount;
    }

    public int getImportTotal(){
        return importTotal;
    }

    public void setImportTotal(int importTotal){
        this.importTotal = importTotal;
    }

    public String getCurrentTask(){
        return currentTask;
    }

    public void setCurrentTask(String currentTask){
        this.currentTask = currentTask;
    }
}