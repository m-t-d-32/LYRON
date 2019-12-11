package generator;

import java.util.*;

public class ObjectType extends VariableType {

    public List<Map.Entry<String, String>> getFields() {
        return fields;
    }

    public void setFields(List<Map.Entry<String, String>> fields) {
        this.fields = fields;
    }

    List<Map.Entry<String, String>> fields;

    public ObjectType(TypePool pool) {
        super(pool);
        fields = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fields);
    }

    public void addField(String fieldName, String typename){
        fields.add(new AbstractMap.SimpleEntry<>(fieldName, typename));
    }

    @Override
    public int getType() {
        return VariableType.OBJECT_TYPE;
    }
}
