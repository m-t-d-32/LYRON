package generator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VariableType {

    private TypePool pool;

    public List<Map.Entry<String, String>> getFields() {
        return fields;
    }

    public void setFields(List<Map.Entry<String, String>> fields) {
        this.fields = fields;
    }

    List<Map.Entry<String, String>> fields;

    public VariableType(TypePool pool){
        this.pool = pool;
        fields = new ArrayList<>();
    }

    public void addField(String fieldName, String typename){
        fields.add(new AbstractMap.SimpleEntry<>(fieldName, typename));
    }

}
