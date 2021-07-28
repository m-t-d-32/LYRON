package parser;

import symbol.AbstractSymbol;
import symbol.AbstractTerminal;

import java.io.Serializable;

public class PointedCFGProduction implements Serializable {

    private final CFGProduction cfgproduction;
    private int pointer;
    private AbstractTerminal outlookAbstractTerminal;

    public PointedCFGProduction(CFGProduction cfgproduction, AbstractTerminal outlookAbstractTerminal) {
        this.cfgproduction = cfgproduction;
        this.pointer = 0;
        this.outlookAbstractTerminal = outlookAbstractTerminal;
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
        PointedCFGProduction pointedProduction = new PointedCFGProduction(cfgproduction, outlookAbstractTerminal);
        pointedProduction.pointer = pointer + 1;
        return pointedProduction;
    }

    @Override
    public boolean equals(Object obj) {
        PointedCFGProduction argument = (PointedCFGProduction) obj;
        return argument.pointer == pointer
                && argument.cfgproduction.equals(cfgproduction)
                && argument.outlookAbstractTerminal.equals(outlookAbstractTerminal);
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
        result.append(outlookAbstractTerminal);
        result.append("）");
        return result.toString();
    }

    public AbstractTerminal getOutlookAbstractTerminal() {
        return outlookAbstractTerminal;
    }

    public int getPointer() {
        return pointer;
    }

    public CFGProduction getProduction() {
        return cfgproduction;
    }
}