package parser;

import symbol.AbstractSymbol;
import symbol.AbstractTerminator;

public class PointedCFGProduction {

    private final CFGProduction cfgproduction;
    private int pointer;
    private AbstractTerminator outlookAbstractTerminator;

    public PointedCFGProduction(CFGProduction cfgproduction, AbstractTerminator outlookAbstractTerminator) {
        this.cfgproduction = cfgproduction;
        this.pointer = 0;
        this.outlookAbstractTerminator = outlookAbstractTerminator;
    }

    public AbstractSymbol getNextSymbol() {
        return cfgproduction.getAfterAbstractSymbols().get(pointer);
    }

    public boolean finished() {
        if (pointer >= cfgproduction.getAfterAbstractSymbols().size()) {
            return true;
        } else return cfgproduction.getAfterAbstractSymbols().get(0).getName().equals("null");
    }

    public PointedCFGProduction next() {
        PointedCFGProduction pointedProduction = new PointedCFGProduction(cfgproduction, outlookAbstractTerminator);
        pointedProduction.pointer = pointer + 1;
        return pointedProduction;
    }

    @Override
    public boolean equals(Object obj) {
        PointedCFGProduction argument = (PointedCFGProduction) obj;
        return argument.pointer == pointer
                && argument.cfgproduction.equals(cfgproduction)
                && argument.outlookAbstractTerminator.equals(outlookAbstractTerminator);
    }

    @Override
    public int hashCode() {
        return cfgproduction.hashCode() ^ pointer;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(cfgproduction.getBeforeAbstractSymbol().toString());
        result.append(" ->");
        for (int i = 0; i < pointer; ++i) {
            result.append(" ");
            result.append(cfgproduction.getAfterAbstractSymbols().get(i).toString());
        }
        result.append(" ·");
        for (int i = pointer; i < cfgproduction.getAfterAbstractSymbols().size(); ++i) {
            result.append(" ");
            result.append(cfgproduction.getAfterAbstractSymbols().get(i).toString());
        }
        result.append("（展望符：");
        result.append(outlookAbstractTerminator);
        result.append("）");
        return result.toString();
    }

    public AbstractTerminator getOutlookAbstractTerminator() {
        return outlookAbstractTerminator;
    }

    public int getPointer() {
        return pointer;
    }

    public CFGProduction getProduction() {
        return cfgproduction;
    }
}