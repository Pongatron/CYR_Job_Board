package Table;

public class DateRange {


    private int dueDateCol = -1;
    private int buildDays = -1;
    private int finishDays = -1;
    private int installDays = -1;



    private boolean isDueDateSaturday = false;

    public DateRange(int dueDateCol, int buildDays, int finishDays, int installDays, boolean isSaturday){
        this.dueDateCol = dueDateCol;
        this.buildDays = buildDays;
        this.finishDays = finishDays;
        this.installDays = installDays;
        if(isSaturday){
            isDueDateSaturday = true;
        }
    }

    public int getDueDateCol() {
        return dueDateCol;
    }
    public int getBuildDays() {
        return buildDays;
    }
    public int getFinishDays() {
        return finishDays;
    }
    public int getInstallDays() {
        return installDays;
    }
    public boolean isDueDateSaturday() {
        return isDueDateSaturday;
    }
}
