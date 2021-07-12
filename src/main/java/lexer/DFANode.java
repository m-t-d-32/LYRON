package lexer;

import util.StringGenerator;

import java.io.Serializable;
import java.util.*;

public class DFANode implements Serializable {

    private String serialCode = null;

    private Map<String, DFANode> stateTransformTable;

    private boolean isFinal = false;

    private List<String> finalNames = null;

    private Map<String, Set<String>> finalNamesToBannedStrs = null;

    public List<String> getFinalNames() {
        return finalNames;
    }

    public void setFinalNames(List<String> finalNames) {
        this.finalNames = finalNames;
    }

    public Map<String, Set<String>> getFinalNamesToBannedStrs() {
        return finalNamesToBannedStrs;
    }

    public void setFinalNamesToBannedStrs(Map<String, Set<String>> finalNamesToBannedStrs) {
        this.finalNamesToBannedStrs = finalNamesToBannedStrs;
    }

    public Map<String, DFANode> getStateTransformTable() {
        return stateTransformTable;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public DFANode() {
        serialCode = StringGenerator.getNextCode();
        stateTransformTable = new HashMap<>();
        finalNames = new ArrayList<>();
        finalNamesToBannedStrs = new HashMap<>();
    }

    public String getSerial() {
        return serialCode;
    }

    public void setLinkedNodes(Set<DFANode> result){
        result.add(this);
        for (String trans: stateTransformTable.keySet()) {
            DFANode end = stateTransformTable.get(trans);
            if (!result.contains(end)) {
                result.add(end);
                end.setLinkedNodes(result);
            }
        }
    }

    public void addToTransformTable(String s, DFANode next) {
        stateTransformTable.put(s, next);
    }

    @Override
    public String toString() {
        return getSerial();
    }

}
