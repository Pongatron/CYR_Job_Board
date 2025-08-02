package DatabaseInteraction;

import java.util.ArrayList;

public class UpdateQueryBuilder {

    private String table;
    private ArrayList<String> colNames;
    private ArrayList<String> colValues;
    private ArrayList<String> conditions;

    public UpdateQueryBuilder(){
        colNames = new ArrayList<>();
        colValues = new ArrayList<>();
        conditions = new ArrayList<>();
    }

    public void updateTable(String table){
        this.table = table;
    }
    public void setColNames(String ... values){
        for(String s : values){
            this.colNames.add(s);
        }
    }
    public void setValues(String ... values){
        for(String s : values){
            if(s.contains("'")) {
                System.out.println("copntains '");
                s = s.replace("'", "''");
            }
            this.colValues.add(s);
        }
    }
    public void where(String ... conditions){
        for(String s : conditions){
            this.conditions.add(s);
        }
    }

    public String build(){
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(table);
        sb.append(" SET ");
        for(int i = 0; i < colNames.size(); i++){
            sb.append(colNames.get(i));
            sb.append(" = ");
            if(!colValues.get(i).isBlank()){
                sb.append("'");
                sb.append(colValues.get(i));
                sb.append("'");
            }
            else{
                sb.append("null");
            }

            if (i < colNames.size() - 1) {
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
        return sb.toString();
    }
}
