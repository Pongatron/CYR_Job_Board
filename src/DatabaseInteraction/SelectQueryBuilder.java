package DatabaseInteraction;

import java.util.ArrayList;

public class SelectQueryBuilder {

    private ArrayList<String> columns;
    private ArrayList<String> tables;
    private ArrayList<String> conditions;

    public SelectQueryBuilder(){
        columns = new ArrayList<>();
        tables = new ArrayList<>();
        conditions = new ArrayList<>();
    }

    public void select(String ... columns){
        for(String s : columns){
            this.columns.add(s);
        }
    }
    public void from(String ... tables){
        for(String s : tables){
            this.tables.add(s);
        }
    }
    public void where(String ... conditions){
        for(String s : conditions){
            this.conditions.add(s);
        }
    }

    public String build(){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for(int i = 0; i < columns.size(); i++){
            sb.append(columns.get(i));
            if (i < columns.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(" FROM ");
        for(int i = 0; i < tables.size(); i++){
            sb.append(tables.get(i));
            if (i < tables.size() - 1) {
                sb.append(", ");
            }
        }
        if(!conditions.isEmpty()) {
            sb.append(" WHERE ");
            for (int i = 0; i < conditions.size(); i++) {
                sb.append(conditions.get(i));
                if (i < conditions.size() - 1) {
                    sb.append(" AND ");
                }
            }
        }
        sb.append(";");
        System.out.println("SelectQueryBuilder: "+sb.toString());
        return sb.toString();
    }

}
