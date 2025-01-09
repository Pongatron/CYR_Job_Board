package DatabaseInteraction;

import java.util.ArrayList;

public class InsertQueryBuilder {

    private String table;
    private ArrayList<String> values;

    public InsertQueryBuilder(){
        values = new ArrayList<>();
    }

    public void insertInto(String table){
        this.table = table;
    }
    public void values(String ... values){
        for(String s : values){
            this.values.add(s);
        }
    }

    public String build(){
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");

        sb.append(table);

        sb.append(" VALUES ('");
        for(int i = 0; i < values.size(); i++){
            sb.append(values.get(i));
            if (i < values.size() - 1) {
                sb.append("', '");
            }
        }
        sb.append("');");

        System.out.println("InsertQueryBuilder: "+sb.toString());
        return sb.toString();
    }

}
