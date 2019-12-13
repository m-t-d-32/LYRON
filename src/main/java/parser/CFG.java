package parser;

import exception.PLDLParsingException;
import exception.PLDLParsingWarning;
import symbol.*;

import java.util.*;

public class CFG {

    private static final String newMarkinStr = "S";

    private List<CFGProduction> CFGProductions;

    public void setCFGProductions(List<? extends CFGProduction> prods) {
        CFGProductions = new ArrayList<>();
        CFGProductions.addAll(prods);
    }

    private AbstractUnterminator CFGmarkin;
    
    private SymbolPool symbolPool;
    
    public CFG(SymbolPool pool,
               Collection<? extends CFGProduction> productions,
               String markinStr) throws PLDLParsingException {
        symbolPool = pool;
        CFGProductions = new ArrayList<>();
        CFGProductions.addAll(productions);
        if (markinStr == null) {
            CFGmarkin = (AbstractUnterminator) CFGProductions.get(0).getBeforeAbstractSymbol();
            PLDLParsingWarning.setLog("警告：您没有传递任何参数作为开始符号，因而自动将第一个产生式的左部符号 " + CFGmarkin.getName() + " 作为开始符号。");
        } else {
            markinStr = markinStr.trim();
            if (pool.getUnterminatorsStr().contains(markinStr)) {
                CFGmarkin = new AbstractUnterminator(markinStr);
            } else {
                throw new PLDLParsingException("解析失败：开始符号不是非终结符。", null);
            }
        }

    }

    public CFG(SymbolPool pool,
               List<String> CFGProductionStrs,
               String markinStr) throws PLDLParsingException {
        symbolPool = pool;
        this.CFGProductions = new ArrayList<>();
        for (int i = 0; i < CFGProductionStrs.size(); ++i) {
            String CFGProductionStr = CFGProductionStrs.get(i);
            CFGProduction production = CFGProduction.getCFGProductionFromCFGString(CFGProductionStr, pool);
            production.setSerialNumber(i + 1);
            CFGProductions.add(production);
        }
        if (markinStr == null) {
            CFGmarkin = (AbstractUnterminator) CFGProductions.get(0).getBeforeAbstractSymbol();
            PLDLParsingWarning.setLog("警告：您没有传递任何参数作为开始符号，因而自动将第一个产生式的左部符号 " + CFGmarkin.getName() + " 作为开始符号。");
        } else {
            markinStr = markinStr.trim();
            if (pool.getUnterminatorsStr().contains(markinStr)) {
                CFGmarkin = new AbstractUnterminator(markinStr);
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
        return symbolPool.getTerminatorsStr();
    }

    public Set<String> getCFGUnterminators() {
        return symbolPool.getUnterminatorsStr();
    }

    public List<CFGProduction> getCFGProductions() {
        return CFGProductions;
    }

    public AbstractUnterminator getMarkin() {
        return CFGmarkin;
    }

    public void setMarkin(AbstractUnterminator markin) {
        this.CFGmarkin = markin;
    }

    public void augmentCFG() {
        if (getCFGUnterminators().contains(newMarkinStr)) {
            PLDLParsingWarning.setLog("该文法已经进行过增广，不能再次增广。");
        } else {
            symbolPool.addUnterminatorStr(newMarkinStr);
            CFGProduction augmentCFGProduction = new CFGProduction();
            AbstractUnterminator beforeSymbol = new AbstractUnterminator(newMarkinStr);
            augmentCFGProduction.setBeforeAbstractSymbol(beforeSymbol);
            List<AbstractSymbol> afterAbstractSymbols = new ArrayList<>();
            afterAbstractSymbols.add(CFGmarkin);
            augmentCFGProduction.setAfterAbstractSymbols(afterAbstractSymbols);
            augmentCFGProduction.setSerialNumber(0);
            CFGProductions.add(augmentCFGProduction);
            CFGmarkin = beforeSymbol;
        }
    }

    public void setCanEmpty() throws PLDLParsingException {
        Map<AbstractUnterminator, Set<CFGProduction>> productions = new HashMap<>();
        Set<AbstractSymbol> tempSetOfEmpty = new HashSet<>(), setOfEmpty = new HashSet<>();
        AbstractSymbol nullAbstractSymbol = symbolPool.getTerminator("null");
        tempSetOfEmpty.add(nullAbstractSymbol);
        for (CFGProduction cfgproduction : CFGProductions) {
            if (!productions.containsKey(cfgproduction.getBeforeAbstractSymbol())) {
                productions.put((AbstractUnterminator) cfgproduction.getBeforeAbstractSymbol(), new HashSet<>());
            }
            productions.get(cfgproduction.getBeforeAbstractSymbol()).add(new CFGProduction(cfgproduction));
        }
        while (!tempSetOfEmpty.isEmpty()) {
            Set<AbstractSymbol> nextTempSetOfEmpty = new HashSet<>();
            for (AbstractUnterminator abstractUnterminator : productions.keySet()) {
                Set<CFGProduction> everyProductions = productions.get(abstractUnterminator);
                for (CFGProduction production : everyProductions) {
                    List<AbstractSymbol> afterAbstractSymbols = production.getAfterAbstractSymbols();
                    for (AbstractSymbol s : tempSetOfEmpty) {
                        afterAbstractSymbols.remove(s);
                    }
                    if (afterAbstractSymbols.size() <= 0) {
                        nextTempSetOfEmpty.add(abstractUnterminator);
                        break;
                    }
                }
            }
            productions.keySet().removeAll(nextTempSetOfEmpty);
            setOfEmpty.addAll(nextTempSetOfEmpty);
            tempSetOfEmpty = nextTempSetOfEmpty;
        }
        for (AbstractSymbol s : setOfEmpty) {
            ((AbstractUnterminator) s).setCanEmpty(true);
        }
    }

    public void setBeginProductions() {
        for (CFGProduction cfgproduction : CFGProductions) {
            AbstractUnterminator beforeSymbol = (AbstractUnterminator) cfgproduction.getBeforeAbstractSymbol();
            if (beforeSymbol.getBeginProductions() == null) {
                beforeSymbol.setBeginProductions(new HashSet<>());
            }
            beforeSymbol.getBeginProductions().add(cfgproduction);
        }
    }

    public void setFirstSet() throws PLDLParsingException {
        setCanEmpty();
        Map<AbstractUnterminator, Set<AbstractUnterminator>> signalPasses = new HashMap<>();
        Map<AbstractUnterminator, Set<AbstractTerminator>> firstSet = new HashMap<>();
        Map<AbstractUnterminator, Set<AbstractTerminator>> tempFirstSet = new HashMap<>();
        AbstractTerminator nullSymbol = symbolPool.getTerminator("null");
        for (CFGProduction production : CFGProductions) {
            for (AbstractSymbol s : production.getAfterAbstractSymbols()) {
                if (s.getType() == AbstractSymbol.UNTERMINATOR) {
                    if (!signalPasses.containsKey(s)) {
                        signalPasses.put((AbstractUnterminator) s, new HashSet<>());
                    }
                    signalPasses.get(s).add((AbstractUnterminator) production.getBeforeAbstractSymbol());
                    if (!((AbstractUnterminator) s).getCanEmpty()) {
                        break;
                    }
                } else if (!s.equals(nullSymbol)) {
                    if (!tempFirstSet.containsKey(production.getBeforeAbstractSymbol())) {
                        tempFirstSet.put((AbstractUnterminator) production.getBeforeAbstractSymbol(), new HashSet<>());
                    }
                    tempFirstSet.get(production.getBeforeAbstractSymbol()).add((AbstractTerminator) s);
                    break;
                } else {
                    break;
                }
            }
        }
        while (!tempFirstSet.isEmpty()) {
            Map<AbstractUnterminator, Set<AbstractTerminator>> newTempFirstSet = new HashMap<>();
            for (AbstractUnterminator abstractUnterminator : tempFirstSet.keySet()) {
                if (!firstSet.containsKey(abstractUnterminator)) {
                    firstSet.put(abstractUnterminator, new HashSet<>());
                }
                firstSet.get(abstractUnterminator).addAll(tempFirstSet.get(abstractUnterminator));
                if (signalPasses.containsKey(abstractUnterminator)) {
                    for (AbstractUnterminator signalReceiver : signalPasses.get(abstractUnterminator)) {
                        if (!newTempFirstSet.containsKey(signalReceiver)) {
                            newTempFirstSet.put(signalReceiver, new HashSet<>());
                        }
                        newTempFirstSet.get(signalReceiver).addAll(tempFirstSet.get(abstractUnterminator));
                    }
                }
            }
            tempFirstSet.clear();
            for (AbstractUnterminator abstractUnterminator : newTempFirstSet.keySet()) {
                if (firstSet.containsKey(abstractUnterminator)) {
                    newTempFirstSet.get(abstractUnterminator).removeAll(firstSet.get(abstractUnterminator));
                }
                if (newTempFirstSet.get(abstractUnterminator).size() > 0) {
                    tempFirstSet.put(abstractUnterminator, newTempFirstSet.get(abstractUnterminator));
                }
            }
        }
        for (AbstractUnterminator abstractUnterminator : symbolPool.getUnterminators()) {
            if (!firstSet.containsKey(abstractUnterminator)) {
                firstSet.put(abstractUnterminator, new HashSet<>());
            }
            if (abstractUnterminator.getCanEmpty()) {
                firstSet.get(abstractUnterminator).add(nullSymbol);
            }
            abstractUnterminator.setFirstSet(firstSet.get(abstractUnterminator));
        }
    }


    public TransformTable getTable() throws PLDLParsingException {
        setBeginProductions();
        setFirstSet();
        symbolPool.addTerminatorStr("eof");
        List<CFGStatement> iterStatements = new ArrayList<>();
        //Set<parser.CFGStatement> checkStatements = new HashSet<>();
        Map<CFGStatement, Integer> checkStatements = new HashMap<>();

        CFGStatement beginStatement = new CFGStatement(this);
        for (CFGProduction production : CFGProductions) {
            if (production.getBeforeAbstractSymbol().equals(CFGmarkin)) {
                beginStatement.add(new PointedCFGProduction(production, symbolPool.getTerminator("eof")));
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
                    if (!classifiedPointedProductions.containsKey(symbolPool.getTerminator("null"))) {
                        classifiedPointedProductions.put(symbolPool.getTerminator("null"), new HashSet<>());
                    }
                    classifiedPointedProductions.get(symbolPool.getTerminator("null")).add(pointedProduction);
                }
            }
            for (AbstractSymbol s : classifiedPointedProductions.keySet()) {
                if (s.equals(symbolPool.getTerminator("null"))) {
                    for (PointedCFGProduction pointedProduction : classifiedPointedProductions.get(s)) {
                        result.add(i, pointedProduction.getOutlookAbstractTerminator(), pointedProduction.getProduction());
                        if (pointedProduction.getOutlookAbstractTerminator().equals(symbolPool.getTerminator("eof"))
                                && pointedProduction.getProduction().getBeforeAbstractSymbol().equals(CFGmarkin)){
                            result.addEndStatement(i);
                        }
                    }
                } else {
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
        //System.out.println(iterStatements.size());
        return result;
    }

    public SymbolPool getSymbolPool() {
        return symbolPool;
    }

    public List<Symbol> revertToStdAbstractSymbols(List<Symbol> symbols) throws PLDLParsingException {
        //Deprecated: case it was implemented in eraseSymbols
        List<Symbol> result = new ArrayList<>();
        for (Symbol symbol: symbols){
            AbstractTerminator realAbstractTerminator = symbolPool.getTerminator(symbol.getAbstractSymbol().getName());
            symbol.setAbstractSymbol(realAbstractTerminator);
            result.add(symbol);
        }
        return result;
    }

    public List<Symbol> eraseComments(List<Symbol> symbols) throws PLDLParsingException {
        List<Symbol> result = new ArrayList<>();
        for (Symbol symbol: symbols){
            AbstractTerminator realAbstractTerminator = symbolPool.getTerminator(symbol.getAbstractSymbol().getName());
            if (!realAbstractTerminator.getIsComment()){
                result.add(symbol);
            }
        }
        return result;
    }
}