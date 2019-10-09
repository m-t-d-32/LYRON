import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CFGStatement {

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

    private Set<Terminator> getFirstsOfSymbolList(List<Symbol> symbols) {
        Set<Terminator> result = new HashSet<>();
        for (Symbol symbol : symbols) {
            if (symbol.getType() == Symbol.UNTERMINATOR) {
                result.addAll(((Unterminator) symbol).getFirstSet());
                if (!((Unterminator) symbol).getCanEmpty()) {
                    break;
                }
            } else {
                result.add((Terminator) symbol);
                if (!symbol.getName().equals("null")) {
                    break;
                }
            }
        }
        try {
            result.remove(cfg.getSymbolPool().getTerminator("null"));
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
                Symbol symbol = pointedProduction.getNextSymbol();
                if (symbol.getType() == Symbol.UNTERMINATOR) {
                    List<Symbol> outlookSymbols = new ArrayList<>();
                    for (int j = pointedProduction.getPointer() + 1;
                         j < pointedProduction.getProduction().getAfterSymbols().size();
                         ++j) {
                        outlookSymbols.add(pointedProduction.getProduction().getAfterSymbols().get(j));
                    }
                    outlookSymbols.add(pointedProduction.getOutlookTerminator());
                    Set<Terminator> firstsOfList = getFirstsOfSymbolList(outlookSymbols);
                    for (CFGProduction production : ((Unterminator) symbol).getBeginProductions()) {
                        for (Terminator outlookSymbol : firstsOfList) {
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