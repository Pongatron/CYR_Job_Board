package DatabaseInteraction;

import java.util.ArrayList;

public class CreateTableQueryBuilder {
    private String tableName;
    private ArrayList<String> colNames;
    private ArrayList<String> datatypes;

    public CreateTableQueryBuilder(ArrayList<String> colNames, ArrayList<String> datatypes){
        this.colNames = colNames;
        this.datatypes = datatypes;

    }

    public void nameTable(String tableName){
        this.tableName = tableName;
    }

    public String build(){
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");

        sb.append(tableName + "(\n");

        for(int i = 0; i < colNames.size(); i++){
            sb.append(colNames.get(i) + " ");
            sb.append(datatypes.get(i));
            if (i < colNames.size() - 1) {
                sb.append(", ");
            }
        }



        sb.append(");");
        System.out.println("SelectQueryBuilder: "+sb.toString());
        return sb.toString();
    }
}
