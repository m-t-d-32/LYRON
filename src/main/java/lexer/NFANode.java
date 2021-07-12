package lexer;

import util.StringGenerator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NFANode implements Serializable {

    private String serialCode = null;

    private Map<String, Set<NFANode> > stateTransformTable;

    private String finalName = null;

    private boolean isFinal = false;

    public String getFinalName() {
        return finalName;
    }

    public void setFinalName(String finalName) {
        this.finalName = finalName;
    }

    public Map<String, Set<NFANode>> getStateTransformTable() {
        return stateTransformTable;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public NFANode() {
        serialCode = StringGenerator.getNextCode();
        stateTransformTable = new HashMap<>();
    }

    public String getSerial() {
        return serialCode;
    }

    public void addToTransformTable(String s, NFANode next) {
        if (!stateTransformTable.containsKey(s)) {
            stateTransformTable.put(s, new HashSet<NFANode>());
        }
        stateTransformTable.get(s).add(next);
    }

    @Override
    public String toString() {
        return getSerial();
    }

}
