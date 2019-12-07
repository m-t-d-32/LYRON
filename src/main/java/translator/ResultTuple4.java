package translator;

import transformer.VariableTable;

import java.util.ArrayList;
import java.util.List;

public class ResultTuple4 {

    public List<Tuple4> getTuple4s() {
        return tuple4s;
    }

    private List<Tuple4> tuple4s = new ArrayList<>();

    private VariableTable variableTable = new VariableTable();

    public void append(String s1, String s2, String s3, String s4) {
        tuple4s.add(new Tuple4(s1, s2, s3, s4));
    }

    public void append(Tuple4 tuple4) {
        tuple4s.add(tuple4);
    }

    public String addTempVar() {
        return variableTable.addTempVar();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Tuple4 t4 : tuple4s) {
            stringBuilder.append(t4.toString());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
