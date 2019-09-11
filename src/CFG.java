import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CFG {

    private static final String newMarkinStr = "S";

    private List<CFGProduction> CFGProductions;

    private Unterminator CFGmarkin;

    public CFG(List<String> CFGProductionStrs,
               Set<String> CFGTerminators,
               Set<String> CFGUnterminators,
               String markinStr) throws PLDLParsingException {
        if (CFGTerminators.contains("null")) {
            throw new PLDLParsingException("null是PLDL语言的保留字，用于表示空串，因而不能表示其他终结符，请更换终结符的名字。", null);
        }
        if (CFGUnterminators.contains("null")) {
            throw new PLDLParsingException("null是PLDL语言的保留字，用于表示空串，因而不能表示其他非终结符，请更换非终结符的名字。", null);
        }
        SymbolPool.initTerminatorString(CFGTerminators);
        SymbolPool.initUnterminatorString(CFGUnterminators);
        this.CFGProductions = new ArrayList<>();
        for (int i = 0; i < CFGProductionStrs.size(); ++i) {
            String CFGProductionStr = CFGProductionStrs.get(i);
            try {
                CFGProduction production = CFGProduction.GetCFGProductionFromCFGString(CFGProductionStr);
                production.setSerialNumber(i + 1);
                CFGProductions.add(production);
            } catch (PLDLParsingException e) {
                throw new PLDLParsingException("解析失败：上下文无关文法中存在语法错误（位于第 " + String.valueOf(i + 1) + " 行)", e);
            }
        }
        if (markinStr == null) {
            CFGmarkin = (Unterminator) CFGProductions.get(0).getBeforeSymbol();
            PLDLParsingWarning.setLog("警告：您没有传递任何参数作为开始符号，因而自动将第一个产生式的左部符号 " + CFGmarkin.getName() + " 作为开始符号。");
        } else {
            markinStr = markinStr.trim();
            if (CFGUnterminators.contains(markinStr)) {
                CFGmarkin = new Unterminator(markinStr);
            } else {
                throw new PLDLParsingException("解析失败：开始符号不是非终结符。", null);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("上下文无关文法包含以下产生式：");
        for (CFGProduction prod : CFGProductions) {
            result.append("\n");
            result.append(prod.toString());
        }
        return result.toString();
    }

    public Set<String> getCFGTerminators() {
        return SymbolPool.getTerminatorsStr();
    }

    public Set<String> getCFGUnterminators() {
        return SymbolPool.getUnterminatorsStr();
    }

    public List<CFGProduction> getCFGProductions() {
        return CFGProductions;
    }

    public Unterminator getMarkin() {
        return CFGmarkin;
    }

    public void setMarkin(Unterminator markin) {
        this.CFGmarkin = markin;
    }

    public void augmentCFG() {
        if (getCFGUnterminators().contains(newMarkinStr)) {
            PLDLParsingWarning.setLog("该文法已经进行过增广，不能再次增广。");
        } else {
            SymbolPool.addUnterminatorStr(newMarkinStr);
            CFGProduction augmentCFGProduction = new CFGProduction();
            Unterminator beforeSymbol = new Unterminator(newMarkinStr);
            augmentCFGProduction.setBeforeSymbol(beforeSymbol);
            List<Symbol> afterSymbols = new ArrayList<>();
            afterSymbols.add(CFGmarkin);
            augmentCFGProduction.setAfterSymbol(afterSymbols);
            augmentCFGProduction.setSerialNumber(0);
            CFGProductions.add(augmentCFGProduction);
            CFGmarkin = beforeSymbol;
        }
    }

    public void setCanEmpty() throws PLDLParsingException {
        Map<Unterminator, Set<CFGProduction>> productions = new HashMap<>();
        Set<Symbol> tempSetOfEmpty = new HashSet<>(), setOfEmpty = new HashSet<>();
        Symbol nullSymbol = SymbolPool.getTerminator("null");
        tempSetOfEmpty.add(nullSymbol);
        for (CFGProduction cfgproduction : CFGProductions) {
            if (!productions.containsKey(cfgproduction.getBeforeSymbol())) {
                productions.put((Unterminator) cfgproduction.getBeforeSymbol(), new HashSet<>());
            }
            productions.get(cfgproduction.getBeforeSymbol()).add((CFGProduction) cfgproduction.clone());
        }
        while (!tempSetOfEmpty.isEmpty()) {
            Set<Symbol> nextTempSetOfEmpty = new HashSet<>();
            for (Unterminator unterminator : productions.keySet()) {
                Set<CFGProduction> everyProductions = productions.get(unterminator);
                for (CFGProduction production : everyProductions) {
                    List<Symbol> afterSymbols = production.getAfterSymbols();
                    for (Symbol s : tempSetOfEmpty) {
                        afterSymbols.remove(s);
                    }
                    if (afterSymbols.size() <= 0) {
                        nextTempSetOfEmpty.add(unterminator);
                        break;
                    }
                }
            }
            productions.keySet().removeAll(nextTempSetOfEmpty);
            setOfEmpty.addAll(nextTempSetOfEmpty);
            tempSetOfEmpty = nextTempSetOfEmpty;
        }
        for (Symbol s : setOfEmpty) {
            ((Unterminator) s).setCanEmpty(true);
        }
    }

    public void setBeginProductions() {
        for (CFGProduction cfgproduction : CFGProductions) {
            Unterminator beforeSymbol = (Unterminator) cfgproduction.getBeforeSymbol();
            if (beforeSymbol.getBeginProductions() == null) {
                beforeSymbol.setBeginProductions(new HashSet<>());
            }
            beforeSymbol.getBeginProductions().add(cfgproduction);
        }
    }

    public void setFirstSet() throws PLDLParsingException {
        setCanEmpty();
        Map<Unterminator, Set<Unterminator>> signalPasses = new HashMap<>();
        Map<Unterminator, Set<Terminator>> firstSet = new HashMap<>();
        Map<Unterminator, Set<Terminator>> tempFirstSet = new HashMap<>();
        Terminator nullSymbol = SymbolPool.getTerminator("null");
        for (CFGProduction production : CFGProductions) {
            for (Symbol s : production.getAfterSymbols()) {
                if (s.getType() == Symbol.UNTERMINATOR) {
                    if (!signalPasses.containsKey(s)) {
                        signalPasses.put((Unterminator) s, new HashSet<>());
                    }
                    signalPasses.get(s).add((Unterminator) production.getBeforeSymbol());
                    if (!((Unterminator) s).getCanEmpty()) {
                        break;
                    }
                } else if (!s.equals(nullSymbol)) {
                    if (!tempFirstSet.containsKey(production.getBeforeSymbol())) {
                        tempFirstSet.put((Unterminator) production.getBeforeSymbol(), new HashSet<>());
                    }
                    tempFirstSet.get(production.getBeforeSymbol()).add((Terminator) s);
                    break;
                } else {
                    break;
                }
            }
        }
        while (!tempFirstSet.isEmpty()) {
            Map<Unterminator, Set<Terminator>> newTempFirstSet = new HashMap<>();
            for (Unterminator unterminator : tempFirstSet.keySet()) {
                if (!firstSet.containsKey(unterminator)) {
                    firstSet.put(unterminator, new HashSet<>());
                }
                firstSet.get(unterminator).addAll(tempFirstSet.get(unterminator));
                if (signalPasses.containsKey(unterminator)) {
                    for (Unterminator signalReceiver : signalPasses.get(unterminator)) {
                        if (!newTempFirstSet.containsKey(signalReceiver)) {
                            newTempFirstSet.put(signalReceiver, new HashSet<>());
                        }
                        newTempFirstSet.get(signalReceiver).addAll(tempFirstSet.get(unterminator));
                    }
                }
            }
            tempFirstSet.clear();
            for (Unterminator unterminator : newTempFirstSet.keySet()) {
                if (firstSet.containsKey(unterminator)) {
                    newTempFirstSet.get(unterminator).removeAll(firstSet.get(unterminator));
                }
                if (newTempFirstSet.get(unterminator).size() > 0) {
                    tempFirstSet.put(unterminator, newTempFirstSet.get(unterminator));
                }
            }
        }
        for (Unterminator unterminator : SymbolPool.getUnterminators()) {
            if (!firstSet.containsKey(unterminator)) {
                firstSet.put(unterminator, new HashSet<>());
            }
            if (unterminator.getCanEmpty()) {
                firstSet.get(unterminator).add(nullSymbol);
            }
            unterminator.setFirstSet(firstSet.get(unterminator));
        }
    }


    public TransformTable getTable() throws PLDLParsingException {
        setBeginProductions();
        setFirstSet();
        SymbolPool.addTerminatorStr("eof");
        List<CFGStatement> iterStatements = new ArrayList<>();
        //Set<CFGStatement> checkStatements = new HashSet<>();
        Map<CFGStatement, Integer> checkStatements = new HashMap<>();

        CFGStatement beginStatement = new CFGStatement();
        for (CFGProduction production : CFGProductions) {
            if (production.getBeforeSymbol().equals(CFGmarkin)) {
                beginStatement.add(new PointedCFGProduction(production, SymbolPool.getTerminator("eof")));
            }
        }
        beginStatement.makeClosure();
        iterStatements.add(beginStatement);
        checkStatements.put(beginStatement, 0);

        TransformTable result = new TransformTable();

        for (int i = 0; i < iterStatements.size(); ++i) {
            CFGStatement nowStatement = iterStatements.get(i);
            Set<PointedCFGProduction> pointedProductions = nowStatement.getPointedProductions();
            Map<Symbol, Set<PointedCFGProduction>> classifiedPointedProductions = new HashMap<>();
            for (PointedCFGProduction pointedProduction : pointedProductions) {
                if (!pointedProduction.finished()) {
                    if (!classifiedPointedProductions.containsKey(pointedProduction.getNextSymbol())) {
                        classifiedPointedProductions.put(pointedProduction.getNextSymbol(), new HashSet<>());
                    }
                    classifiedPointedProductions.get(pointedProduction.getNextSymbol()).add(pointedProduction);
                } else {
                    if (!classifiedPointedProductions.containsKey(SymbolPool.getTerminator("null"))) {
                        classifiedPointedProductions.put(SymbolPool.getTerminator("null"), new HashSet<>());
                    }
                    classifiedPointedProductions.get(SymbolPool.getTerminator("null")).add(pointedProduction);
                }
            }
            for (Symbol s : classifiedPointedProductions.keySet()) {
                if (s.equals(SymbolPool.getTerminator("null"))) {
                    for (PointedCFGProduction pointedProduction : classifiedPointedProductions.get(s)) {
                        result.add(i, pointedProduction.getOutlookTerminator(), pointedProduction.getProduction());
                        if (pointedProduction.getOutlookTerminator().equals(SymbolPool.getTerminator("eof")) 
                        		&& pointedProduction.getProduction().getBeforeSymbol().equals(CFGmarkin)){
                        	result.addEndStatement(i);
                        }
                    }
                } else {
                    CFGStatement statement = new CFGStatement();
                    for (PointedCFGProduction pointedProduction : classifiedPointedProductions.get(s)) {
                        statement.add(pointedProduction.next());
                    }
                    statement.makeClosure();
                    if (!checkStatements.containsKey(statement)) {
                        checkStatements.put(statement, iterStatements.size());
                        iterStatements.add(statement);
                    }
                    result.add(i, s, checkStatements.get(statement));
                }
            }
        }
        //System.out.println(iterStatements.size());
        return result;
    }
}