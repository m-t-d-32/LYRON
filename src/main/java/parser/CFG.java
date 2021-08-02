package parser;

import exception.PLDLParsingException;
import exception.PLDLParsingWarning;
import symbol.*;

import java.io.Serializable;
import java.util.*;

public class CFG implements Serializable {

    private List<CFGProduction> CFGProductions;

    private AbstractNonterminal CFGmarkin;

    private SymbolPool symbolPool;

    private Map<AbstractNonterminal, Set<CFGProduction>> beginProductions;

    public Map<AbstractNonterminal, Set<CFGProduction>> getBeginProductions() {
        return beginProductions;
    }

    public CFG(SymbolPool pool,
               Collection<? extends CFGProduction> productions,
               String markinStr) throws PLDLParsingException {
        symbolPool = pool;
        CFGProductions = new ArrayList<>();
        CFGProductions.addAll(productions);
        if (markinStr == null) {
            CFGmarkin = (AbstractNonterminal) CFGProductions.get(0).getBeforeAbstractSymbol();
            PLDLParsingWarning.setLog("警告：您没有传递任何参数作为开始符号，因而自动将第一个产生式的左部符号 " + CFGmarkin.getName() + " 作为开始符号。");
        } else {
            markinStr = markinStr.trim();
            if (pool.getNonterminalsStr().contains(markinStr)) {
                CFGmarkin = new AbstractNonterminal(markinStr);
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

    public Set<String> getCFGTerminals() {
        return symbolPool.getTerminalsStr();
    }

    public Set<String> getCFGNonterminals() {
        return symbolPool.getNonterminalsStr();
    }

    public void setCanEmpty() throws PLDLParsingException {
        Map<AbstractNonterminal, Set<CFGProduction>> productions = new HashMap<>();
        Set<AbstractSymbol> tempSetOfEmpty = new HashSet<>(), setOfEmpty = new HashSet<>();
        AbstractSymbol nullAbstractSymbol = symbolPool.getTerminal("null");
        tempSetOfEmpty.add(nullAbstractSymbol);
        for (CFGProduction cfgproduction : CFGProductions) {
            if (!productions.containsKey(cfgproduction.getBeforeAbstractSymbol())) {
                productions.put((AbstractNonterminal) cfgproduction.getBeforeAbstractSymbol(), new HashSet<>());
            }
            productions.get(cfgproduction.getBeforeAbstractSymbol()).add(new CFGProduction(cfgproduction));
        }
        while (!tempSetOfEmpty.isEmpty()) {
            Set<AbstractSymbol> nextTempSetOfEmpty = new HashSet<>();
            for (AbstractNonterminal abstractNonterminal : productions.keySet()) {
                Set<CFGProduction> everyProductions = productions.get(abstractNonterminal);
                for (CFGProduction production : everyProductions) {
                    List<AbstractSymbol> afterAbstractSymbols = production.getAfterAbstractSymbols();
                    for (AbstractSymbol s : tempSetOfEmpty) {
                        afterAbstractSymbols.remove(s);
                    }
                    if (afterAbstractSymbols.size() <= 0) {
                        nextTempSetOfEmpty.add(abstractNonterminal);
                        break;
                    }
                }
            }
            productions.keySet().removeAll(nextTempSetOfEmpty);
            setOfEmpty.addAll(nextTempSetOfEmpty);
            tempSetOfEmpty = nextTempSetOfEmpty;
        }
        for (AbstractSymbol s : setOfEmpty) {
            ((AbstractNonterminal) s).setCanEmpty(true);
        }
    }

    public void setBeginProductions() {
        beginProductions = new HashMap<>();
        for (CFGProduction cfgproduction : CFGProductions) {
            AbstractNonterminal beforeSymbol = (AbstractNonterminal) cfgproduction.getBeforeAbstractSymbol();
            if (beginProductions.get(beforeSymbol) == null) {
                beginProductions.put(beforeSymbol, new HashSet<>());
            }
            beginProductions.get(beforeSymbol).add(cfgproduction);
        }
    }

    public void setFirstSet() throws PLDLParsingException {
        setCanEmpty();
        Map<AbstractNonterminal, Set<AbstractNonterminal>> signalPasses = new HashMap<>();
        Map<AbstractNonterminal, Set<AbstractTerminal>> firstSet = new HashMap<>();
        Map<AbstractNonterminal, Set<AbstractTerminal>> tempFirstSet = new HashMap<>();
        AbstractTerminal nullSymbol = symbolPool.getTerminal("null");
        for (CFGProduction production : CFGProductions) {
            for (AbstractSymbol s : production.getAfterAbstractSymbols()) {
                if (s.getType() == AbstractSymbol.NONTERMINAL) {
                    if (!signalPasses.containsKey(s)) {
                        signalPasses.put((AbstractNonterminal) s, new HashSet<>());
                    }
                    signalPasses.get(s).add((AbstractNonterminal) production.getBeforeAbstractSymbol());
                    if (!((AbstractNonterminal) s).getCanEmpty()) {
                        break;
                    }
                } else if (!s.equals(nullSymbol)) {
                    if (!tempFirstSet.containsKey(production.getBeforeAbstractSymbol())) {
                        tempFirstSet.put((AbstractNonterminal) production.getBeforeAbstractSymbol(), new HashSet<>());
                    }
                    tempFirstSet.get(production.getBeforeAbstractSymbol()).add((AbstractTerminal) s);
                    break;
                } else {
                    break;
                }
            }
        }
        while (!tempFirstSet.isEmpty()) {
            Map<AbstractNonterminal, Set<AbstractTerminal>> newTempFirstSet = new HashMap<>();
            for (AbstractNonterminal abstractNonterminal : tempFirstSet.keySet()) {
                if (!firstSet.containsKey(abstractNonterminal)) {
                    firstSet.put(abstractNonterminal, new HashSet<>());
                }
                firstSet.get(abstractNonterminal).addAll(tempFirstSet.get(abstractNonterminal));
                if (signalPasses.containsKey(abstractNonterminal)) {
                    for (AbstractNonterminal signalReceiver : signalPasses.get(abstractNonterminal)) {
                        if (!newTempFirstSet.containsKey(signalReceiver)) {
                            newTempFirstSet.put(signalReceiver, new HashSet<>());
                        }
                        newTempFirstSet.get(signalReceiver).addAll(tempFirstSet.get(abstractNonterminal));
                    }
                }
            }
            tempFirstSet.clear();
            for (AbstractNonterminal abstractNonterminal : newTempFirstSet.keySet()) {
                if (firstSet.containsKey(abstractNonterminal)) {
                    newTempFirstSet.get(abstractNonterminal).removeAll(firstSet.get(abstractNonterminal));
                }
                if (newTempFirstSet.get(abstractNonterminal).size() > 0) {
                    tempFirstSet.put(abstractNonterminal, newTempFirstSet.get(abstractNonterminal));
                }
            }
        }
        for (AbstractNonterminal abstractNonterminal : symbolPool.getNonterminals()) {
            if (!firstSet.containsKey(abstractNonterminal)) {
                firstSet.put(abstractNonterminal, new HashSet<>());
            }
            if (abstractNonterminal.getCanEmpty()) {
                firstSet.get(abstractNonterminal).add(nullSymbol);
            }
            abstractNonterminal.setFirstSet(firstSet.get(abstractNonterminal));
        }
    }


    public TransformTable getTable() throws PLDLParsingException {
        setBeginProductions();
        setFirstSet();
        symbolPool.addTerminalStr("eof");
        List<CFGStatement> iterStatements = new ArrayList<>();
        Map<CFGStatement, Integer> checkStatements = new HashMap<>();

        CFGStatement beginStatement = new CFGStatement(this);
        for (CFGProduction production : CFGProductions) {
            if (production.getBeforeAbstractSymbol().equals(CFGmarkin)) {
                beginStatement.add(new PointedCFGProduction(production, symbolPool.getTerminal("eof")));
            }
        }
        beginStatement.makeClosure();
        iterStatements.add(beginStatement);
        checkStatements.put(beginStatement, 0);

        TransformTable result = new TransformTable(this);

        for (int i = 0; i < iterStatements.size(); ++i) {
            CFGStatement nowStatement = iterStatements.get(i);
            Set<PointedCFGProduction> pointedProductions = nowStatement.getPointedProductions();
            Map<AbstractSymbol, Set<PointedCFGProduction>> classifiedPointedProductions = new HashMap<>();
            for (PointedCFGProduction pointedProduction : pointedProductions) {
                if (!pointedProduction.finished()) {
                    if (!classifiedPointedProductions.containsKey(pointedProduction.getNextSymbol())) {
                        classifiedPointedProductions.put(pointedProduction.getNextSymbol(), new HashSet<>());
                    }
                    classifiedPointedProductions.get(pointedProduction.getNextSymbol()).add(pointedProduction);
                } else {
                    if (!classifiedPointedProductions.containsKey(symbolPool.getTerminal("null"))) {
                        classifiedPointedProductions.put(symbolPool.getTerminal("null"), new HashSet<>());
                    }
                    classifiedPointedProductions.get(symbolPool.getTerminal("null")).add(pointedProduction);
                }
            }
            if (classifiedPointedProductions.containsKey(symbolPool.getTerminal("null"))) {
                for (PointedCFGProduction pointedProduction : classifiedPointedProductions.get(symbolPool.getTerminal("null"))) {
                    result.add(i, pointedProduction.getOutlookAbstractTerminal(), pointedProduction.getProduction());
                    if (pointedProduction.getOutlookAbstractTerminal().equals(symbolPool.getTerminal("eof"))
                            && pointedProduction.getProduction().getBeforeAbstractSymbol().equals(CFGmarkin)) {
                        result.addEndStatement(i);
                    }
                }
            }
            for (AbstractSymbol s : classifiedPointedProductions.keySet()) {
                if (!s.equals(symbolPool.getTerminal("null"))) {
                    CFGStatement statement = new CFGStatement(this);
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
        return result;
    }

    public SymbolPool getSymbolPool() {
        return symbolPool;
    }

    public List<Symbol> revertToStdAbstractSymbols(List<Symbol> symbols) throws PLDLParsingException {
        //Deprecated: case it was implemented in eraseSymbols
        List<Symbol> result = new ArrayList<>();
        for (Symbol symbol: symbols){
            AbstractTerminal realAbstractTerminal = symbolPool.getTerminal(symbol.getAbstractSymbol().getName());
            symbol.setAbstractSymbol(realAbstractTerminal);
            result.add(symbol);
        }
        return result;
    }

    public List<Symbol> eraseComments(List<Symbol> symbols) throws PLDLParsingException {
        List<Symbol> result = new ArrayList<>();
        for (Symbol symbol: symbols){
            AbstractTerminal realAbstractTerminal = symbolPool.getTerminal(symbol.getAbstractSymbol().getName());
            if (!realAbstractTerminal.getIsComment()){
                result.add(symbol);
            }
        }
        return result;
    }

    public AbstractNonterminal getBeginAbstractSymbol(){
        return CFGmarkin;
    }
}