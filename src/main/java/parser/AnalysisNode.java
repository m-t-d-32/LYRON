package parser;

import symbol.Symbol;

import java.io.Serializable;
import java.util.List;

public class AnalysisNode implements Serializable {
    private Symbol value;

    public Symbol getValue() {
        return value;
    }

    private List<AnalysisNode> children;

    public CFGProduction getProduction() {
        return production;
    }

    public void setProduction(CFGProduction production) {
        this.production = production;
    }

    private CFGProduction production;

    public void setParent(AnalysisNode parent) {
        this.parent = parent;
    }

    private AnalysisNode parent;

    public AnalysisNode(Symbol value) {
        this.value = value;
        this.parent = null;
        this.children = null;
    }

    public void setChildren(List<AnalysisNode> symbols) {
        children = symbols;
    }

    public List<AnalysisNode> getChildren(){
        return children;
    }

    public String toString(int tabCount) {
        StringBuilder result = new StringBuilder();
        result.append("|");
        for (int i = 0; i < tabCount; ++i) {
            result.append("-");
        }
        result.append(value.getAbstractSymbol().getName());
        result.append("\n");
        if (children != null) {
            for (AnalysisNode node: children) {
                result.append(node.toString(tabCount + 1));
            }
        }
        return result.toString();
    }
}
