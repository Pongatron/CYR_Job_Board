package DatabaseInteraction;

import java.util.ArrayList;

public class InsertQueryBuilder {

    private String table;
    private ArrayList<String> values;
    private ArrayList<String> columns;

    public InsertQueryBuilder(){
        values = new ArrayList<>();
        columns = new ArrayList<>();
    }
    public void insertInto(String table){
        this.table = table;
    }
    public void setColumns(String ... columns){
        for(String s : columns){
            this.columns.add(s);
        }
    }
    public void setValues(String ... values){
        for(String s : values){
            this.values.add(s);
        }
    }

    public String build(){
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(table);
        sb.append("(");
        for(int i = 0; i < columns.size(); i++){
            sb.append(columns.get(i));
            if (i < columns.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");

        sb.append(" VALUES (");
        for(int i = 0; i < values.size(); i++){
            if(!values.get(i).isBlank()){
                sb.append("'");
                sb.append(values.get(i));
                sb.append("'");
            }
            else{
                sb.append("null");
            }

            if (i < values.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(");");
        return sb.toString();
    }

}
