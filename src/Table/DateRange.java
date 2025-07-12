package Table;

import java.time.LocalDate;
import java.util.ArrayList;

public class DateRange {


    private LocalDate dueDate;
    private ArrayList<LocalDate> buildDays;
    private ArrayList<LocalDate> finishDays;
    private ArrayList<LocalDate> installDays;



    private boolean isDueDateSaturday = false;

    public DateRange(LocalDate dueDate, ArrayList<LocalDate> buildDays, ArrayList<LocalDate> finishDays, ArrayList<LocalDate> installDays, boolean isSaturday){
        this.dueDate = dueDate;
        this.buildDays = buildDays;
        this.finishDays = finishDays;
        this.installDays = installDays;
        if(isSaturday){
            isDueDateSaturday = true;
        }
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
    public ArrayList<LocalDate> getBuildDays() {
        return buildDays;
    }
    public ArrayList<LocalDate> getFinishDays() {
        return finishDays;
    }
    public ArrayList<LocalDate> getInstallDays() {
        return installDays;
    }
    public boolean isDueDateSaturday() {
        return isDueDateSaturday;
    }
}
