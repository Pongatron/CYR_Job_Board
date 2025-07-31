package Table;

public class TimeOffValue {

    private String worker;
    private int col;

    public TimeOffValue(String worker, int col){
        this.worker = worker;
        this.col = col;
    }

    public String getWorker() {return worker;}
    public int getCol() {return col;}
}
