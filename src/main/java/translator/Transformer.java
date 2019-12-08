package translator;

import exception.PLDLAnalysisException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Transformer {
    private Set<String> isVars;

    public Transformer() {
        this.isVars = new HashSet<>();
    }

    public Transformer(Set<String> isVars) {
        this.isVars = isVars;
    }

    public ResultTuple4 transformResultTuples(ResultTuple4 srcResultTuples) throws PLDLAnalysisException {
        ResultTuple4 result = new ResultTuple4();
        List<Tuple4> tuple4s = srcResultTuples.getTuple4s();
        VariableTable table = result.getVariableTable();
        for (Tuple4 tuple4 : tuple4s) {
            switch (tuple4.get(0)) {
                case "define":
                    table.addVar(tuple4.get(2), tuple4.get(3));
                    break;
                case "in":
                    table.deepIn();
                    break;
                case "out":
                    table.shallowOut();
                    break;
                default:
                    Tuple4 newTuple = new Tuple4(tuple4);
                    for (int i = 1; i < 4; ++i) {
                        if (isVars.contains(newTuple.get(i))) {
                            newTuple.set(i, table.getVar(newTuple.get(i)));
                        }
                    }
                    result.append(newTuple);
                    break;
            }
        }
        table.setTempVarCount(srcResultTuples.getVariableTable().getTempVarCount());
        return result;
    }
}
