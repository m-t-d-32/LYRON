package parser;

import exception.PLDLParsingException;
import symbol.AbstractSymbol;
import symbol.AbstractTerminal;
import symbol.AbstractNonterminal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CFGStatement implements Serializable {

    private Set<PointedCFGProduction> pointedProductions;
    
    private CFG cfg;

    CFGStatement(CFG cfg) {
        this.cfg = cfg;
        pointedProductions = new HashSet<>();
    }

    void add(PointedCFGProduction pointedProduction) {
        pointedProductions.add(pointedProduction);
    }

    @Override
    public boolean equals(Object obj) {
        CFGStatement argument = (CFGStatement) obj;
        return pointedProductions.size() == argument.pointedProductions.size() &&
                pointedProductions.containsAll(argument.pointedProductions);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (PointedCFGProduction pointedProduction : pointedProductions) {
            hash ^= pointedProduction.hashCode();
        }
        return hash;
    }

    Set<PointedCFGProduction> getPointedProductions() {
        return pointedProductions;
    }

    private Set<AbstractTerminal> getFirstsOfSymbolList(List<AbstractSymbol> abstractSymbols) {
        Set<AbstractTerminal> result = new HashSet<>();
        for (AbstractSymbol abstractSymbol : abstractSymbols) {
            if (abstractSymbol.getType() == AbstractSymbol.NONTERMINAL) {
                result.addAll(((AbstractNonterminal) abstractSymbol).getFirstSet());
                if (!((AbstractNonterminal) abstractSymbol).getCanEmpty()) {
                    break;
                }
            } else {
                result.add((AbstractTerminal) abstractSymbol);
                if (!abstractSymbol.getName().equals("null")) {
                    break;
                }
            }
        }
        try {
            result.remove(cfg.getSymbolPool().getTerminal("null"));
        } catch (PLDLParsingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void makeClosure() {
        Set<PointedCFGProduction> checkPointedProductions = pointedProductions;
        List<PointedCFGProduction> iterPointedProductions = new ArrayList<>(pointedProductions);
        for (int i = 0; i < iterPointedProductions.size(); ++i) {
            PointedCFGProduction pointedProduction = iterPointedProductions.get(i);
            if (!pointedProduction.finished()) {
                AbstractSymbol abstractSymbol = pointedProduction.getNextSymbol();
                if (abstractSymbol.getType() == AbstractSymbol.NONTERMINAL) {
                    List<AbstractSymbol> outlookAbstractSymbols = new ArrayList<>();
                    for (int j = pointedProduction.getPointer() + 1;
                         j < pointedProduction.getProduction().getAfterAbstractSymbols().size();
                         ++j) {
                        outlookAbstractSymbols.add(pointedProduction.getProduction().getAfterAbstractSymbols().get(j));
                    }
                    outlookAbstractSymbols.add(pointedProduction.getOutlookAbstractTerminal());
                    Set<AbstractTerminal> firstsOfList = getFirstsOfSymbolList(outlookAbstractSymbols);
                    for (CFGProduction production : cfg.getBeginProductions().get(abstractSymbol)) {
                        for (AbstractTerminal outlookSymbol : firstsOfList) {
                            PointedCFGProduction generatedProduction = new PointedCFGProduction(production, outlookSymbol);
                            if (!checkPointedProductions.contains(generatedProduction)) {
                                checkPointedProductions.add(generatedProduction);
                                iterPointedProductions.add(generatedProduction);
                            }
                        }
                    }
                }
            }
        }
        pointedProductions = checkPointedProductions;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("-------\n");
        for (PointedCFGProduction pointedProduction : pointedProductions) {
            result.append(pointedProduction.toString());
            result.append("\n");
        }
        result.append("-------");
        return result.toString();
    }
}