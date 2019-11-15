package translator;

import exception.PLDLAnalysisException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultTuple4 {

    private List<Tuple4> tuple4s = new ArrayList<>();

    private Set<String> vars = new HashSet<>();

    private int tempVarCount = 0;

    public void append(String s1, String s2, String s3, String s4){
        tuple4s.add(new Tuple4(s1, s2, s3, s4));
    }

    public String addTempVar(){
        return "Temp" + String.valueOf(++tempVarCount);
    }

    public void addVar(String varname) throws PLDLAnalysisException {
        if (vars.contains(varname)){
            throw new PLDLAnalysisException("变量" + varname + "重复定义！", null);
        }
        vars.add("_" + varname);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Tuple4 t4: tuple4s){
            stringBuilder.append(t4.toString());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
