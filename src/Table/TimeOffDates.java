package Table;

import java.time.LocalDate;
import java.util.ArrayList;

public class TimeOffDates {

    private String person;
    private ArrayList<LocalDate> offDays;



    private boolean isDueDateSaturday = false;

    public TimeOffDates(String person, ArrayList<LocalDate> offDays){
        this.person = person;
        this.offDays = offDays;
    }

    public String getPerson() {
        return person;
    }
    public ArrayList<LocalDate> getOffDays() {
        return offDays;
    }
}
