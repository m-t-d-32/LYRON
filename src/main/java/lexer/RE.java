package lexer;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import exception.REParsingException;
import parser.CFG;
import parser.Movement;
import parser.MovementsList;
import parser.TransformTable;
import symbol.Terminal;
import symbol.*;

import java.io.Serializable;
import java.util.*;

public abstract class RE implements Serializable {

    private final String reString;

    public String getReString() {
        return reString;
    }

    private NFA letNFA = null;

    protected CFG cfg = null;

    public RE(String reString) throws PLDLAnalysisException, PLDLParsingException {
        setCFG();
        this.reString = reString;
        setNFA();
    }

    protected abstract void setCFG();

    protected abstract List<Symbol> getSymbols() throws REParsingException;

    public NFA getNFA() {
        return letNFA;
    }

    private void setNFA() throws PLDLAnalysisException, PLDLParsingException {
        TransformTable table = cfg.getTable();
        Map<Integer, Map<AbstractSymbol, Set<Movement>>> tableMap = table.getTableMap();
        Set<Integer> endStatements = table.getEndStatements();
        if (endStatements.size() <= 0) {
            throw new PLDLAnalysisException("状态数为0或者小于0，程序失败。", null);
        }

        Stack<Integer> statementStack = new Stack<>();
        statementStack.push(0);
        Stack<NFA> nodeStack = new Stack<>();
        Stack<Symbol> symbolStack = new Stack<>();
        Stack<Symbol> streamStack = new Stack<>();
        List<Symbol> stream = getSymbols();
        Symbol endTerminal = new Terminal(cfg.getSymbolPool().getTerminal("eof"));
        streamStack.push(endTerminal);
        for (int i = stream.size() - 1; i >= 0; --i){
            streamStack.push(stream.get(i));
        }

        int i = 0;
        while (symbolStack.empty() || symbolStack.peek().getAbstractSymbol().getType() == AbstractSymbol.TERMINAL ||
                !symbolStack.peek().getAbstractSymbol().equals(cfg.getBeginAbstractSymbol())){
            int nowStatement = statementStack.peek();
            Symbol nowSymbol = streamStack.peek();
            Set<Movement> nowMovements = tableMap.get(nowStatement).get(nowSymbol.getAbstractSymbol());
            if (nowMovements == null || nowMovements.size() <= 0){
                //这里不回滚
                throw new PLDLAnalysisException("程序分析到第 " + (i + 1) + " 个符号：" + nowSymbol + " 时既无法移进，也无法归约。", null);
            }
            else {
                MovementsList movementsList = new MovementsList(nowMovements);
                Movement movement = movementsList.nextMovement();
                if (movement.getMovement() == Movement.SHIFT){
                    NFANode newNode = new NFANode();
                    newNode.setFinal(true);
                    nodeStack.push(new NFA(newNode));
                    symbolStack.push(nowSymbol);
                    statementStack.push(movement.getShiftTo());
                    streamStack.pop();
                    ++i;
                }
                else if (movement.getMovement() == Movement.GOTO){
                    statementStack.push(movement.getShiftTo());
                    streamStack.pop();
                    ++i;
                }
                else if (movement.getMovement() == Movement.REGRESSION){
                    REProduction production = (REProduction) movement.getRegressionProduction();
                    List<NFA> tempNFA = new ArrayList<>();
                    List<Symbol> tempSymbol = new ArrayList<>();
                    AbstractTerminal nullTerminal = cfg.getSymbolPool().getTerminal("null");
                    for (AbstractSymbol symbol : production.getAfterAbstractSymbols()) {
                        if (!symbol.equals(nullTerminal)) {
                            statementStack.pop();
                            tempNFA.add(nodeStack.pop());
                            tempSymbol.add(symbolStack.pop());
                        }
                    }
                    Collections.reverse(tempNFA);
                    Collections.reverse(tempSymbol);
                    nodeStack.push(production.getNFANode(tempNFA, tempSymbol));
                    Symbol newSymbol = new Nonterminal((AbstractNonterminal) production.getBeforeAbstractSymbol());
                    symbolStack.push(newSymbol);
                    streamStack.push(newSymbol);
                    --i;
                }
            }
        }
        letNFA = nodeStack.pop();
    }
}
