package parser;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import exception.PLDLParsingWarning;
import symbol.*;

import java.io.Serializable;
import java.util.*;

public class TransformTable implements Serializable {

    private static final long serialVersionUID = 8448616257039658718L;

    private final Map<Integer, Map<AbstractSymbol, Set<Movement> >> table;
    
    private final Set<Integer> endStatements;
    
    private final CFG cfg;
    
    public Map<Integer, Map<AbstractSymbol, Set<Movement> >> getTableMap() {
        return table;
    }

    public TransformTable(CFG cfg) {
        this.table = new HashMap<>();
        this.endStatements = new HashSet<>();
        this.cfg = cfg;
    }
    
    public void addEndStatement(int endStatement) {
        this.endStatements.add(endStatement);
    }
    
    public Set<Integer> getEndStatements() {
        return endStatements;
    }

    public void add(int statementIndex, AbstractSymbol nextAbstractSymbol, int nextStatementIndex) {
        Movement movement;
        if (nextAbstractSymbol.getType() == AbstractSymbol.NONTERMINAL) {
            movement = new Movement(Movement.GOTO, nextStatementIndex);
        } else {
            movement = new Movement(Movement.SHIFT, nextStatementIndex);
        }
        if (!table.containsKey(statementIndex)) {
            table.put(statementIndex, new HashMap<>());
        }
        if (!table.get(statementIndex).containsKey(nextAbstractSymbol)){
            table.get(statementIndex).put(nextAbstractSymbol, new TreeSet<>());
        }
        table.get(statementIndex).get(nextAbstractSymbol).add(movement);
    }

    public void add(int statementIndex, AbstractSymbol nextAbstractSymbol, CFGProduction production) {
        Movement movement = new Movement(production);
        if (!table.containsKey(statementIndex)) {
            table.put(statementIndex, new HashMap<>());
        }
        if (!table.containsKey(statementIndex)) {
            table.put(statementIndex, new HashMap<>());
        }
        if (!table.get(statementIndex).containsKey(nextAbstractSymbol)){
            table.get(statementIndex).put(nextAbstractSymbol, new TreeSet<>());
        }
        else if (table.get(statementIndex).get(nextAbstractSymbol).size() > 0){
            StringBuilder warningStr = new StringBuilder();
            warningStr.append("归约/归约冲突发生").append("\n");
            for (Movement movement1: table.get(statementIndex).get(nextAbstractSymbol)){
                warningStr.append(movement1).append("\n");
            }
            warningStr.append("与：").append(movement).append("\n");
            PLDLParsingWarning.setLog(warningStr.toString());
        }
        table.get(statementIndex).get(nextAbstractSymbol).add(movement);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        Set<AbstractSymbol> allAbstractSymbols = new HashSet<>();
        for (Integer i : table.keySet()) {
            allAbstractSymbols.addAll(table.get(i).keySet());
        }
        List<Integer> sortedStatementIndexes = new ArrayList<>(table.keySet());
        Collections.sort(sortedStatementIndexes);
        List<AbstractSymbol> certainAllAbstractSymbols = new ArrayList<>(allAbstractSymbols);
        for (AbstractSymbol s : certainAllAbstractSymbols) {
            result.append(",");
            result.append(s.getName());
        }
        result.append("\n");
        for (Integer i : sortedStatementIndexes) {
            result.append(i);
            for (AbstractSymbol s : certainAllAbstractSymbols) {
                result.append(",");
                
                if (table.get(i).containsKey(s)) {
                    try {
                        if (endStatements.contains(i) && s.equals(cfg.getSymbolPool().getTerminal("eof"))) {
                            result.append("acc(").append(table.get(i).get(s).toString()).append(")");
                        }
                        else {
                            result.append(table.get(i).get(s).toString());
                        }
                    } catch (PLDLParsingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            result.append("\n");
        }
        return result.toString();
    }

    private int doAnalysis(int i, Symbol nowSymbol, Movement movement,
                           Stack<Integer> statementStack, Stack<AnalysisNode> nodeStack, Stack<Symbol> streamStack)
            throws PLDLParsingException {
        if (movement.getMovement() == Movement.SHIFT){
            nodeStack.push(new AnalysisNode(nowSymbol));
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
            CFGProduction production = movement.getRegressionProduction();
            AnalysisNode node = new AnalysisNode(new Nonterminal((AbstractNonterminal) production.getBeforeAbstractSymbol()));
            node.setProduction(production);
            node.setChildren(new ArrayList<>());
            AbstractTerminal nullTerminal = cfg.getSymbolPool().getTerminal("null");
            Stack<AnalysisNode> tempStack = new Stack<>();
            for (AbstractSymbol symbol : production.getAfterAbstractSymbols()) {
                if (symbol != nullTerminal) {
                    statementStack.pop();
                    tempStack.push(nodeStack.pop());
                }
            }
            for (AbstractSymbol symbol : production.getAfterAbstractSymbols()) {
                if (symbol != nullTerminal) {
                    AnalysisNode childNode = tempStack.pop();
                    childNode.setParent(node);
                    node.getChildren().add(childNode);
                }
            }
            nodeStack.push(node);
            Symbol newSymbol = node.getValue();
            streamStack.push(newSymbol);
            --i;
        }
        return i;
    }

    public AnalysisTree getAnalysisTree(List<Symbol> stream) throws PLDLAnalysisException, PLDLParsingException {
        AnalysisTree tree = new AnalysisTree();
        if (endStatements.size() <= 0) {
            throw new PLDLAnalysisException("状态数为0或者小于0，程序失败。", null);
        }

        Stack<Integer> statementStack = new Stack<>();
        statementStack.push(0);
        Stack<AnalysisNode> nodeStack = new Stack<>();
        Stack<Symbol> streamStack = new Stack<>();
        Symbol endTerminal = new Terminal(cfg.getSymbolPool().getTerminal("eof"));
        streamStack.push(endTerminal);
        for (int i = stream.size() - 1; i >= 0; --i){
            streamStack.push(stream.get(i));
        }

        Stack<AnalysingStatement> rollbackStatements = new Stack<>();

        int i = 0;
        while (nodeStack.empty() || nodeStack.peek().getValue().getType() == AbstractSymbol.TERMINAL ||
                !nodeStack.peek().getValue().getAbstractSymbol().equals(cfg.getBeginAbstractSymbol())){
            int nowStatement = statementStack.peek();
            Symbol nowSymbol = streamStack.peek();
            Set<Movement> nowMovements = table.get(nowStatement).get(nowSymbol.getAbstractSymbol());
            if (nowMovements == null || nowMovements.size() <= 0){
                //回滚
                AnalysingStatement willCoverStatements = null;
                while (!rollbackStatements.isEmpty()) {
                    willCoverStatements = rollbackStatements.peek();
                    if (!willCoverStatements.getMovementsList().hasNextMovement()){
                        willCoverStatements = null;
                        rollbackStatements.pop();
                    }
                    else {
                        break;
                    }
                }
                if (willCoverStatements == null){
                    throw new PLDLAnalysisException("程序分析到第 " + (i + 1) + " 个符号：" + nowSymbol + " 时既无法移进，也无法归约。", null);
                }
                else {
                    statementStack = willCoverStatements.getStatementStack();
                    streamStack = willCoverStatements.getStreamStack();
                    nodeStack = willCoverStatements.getNodeStack();
                    MovementsList movementsList = willCoverStatements.getMovementsList();
                    Movement movement = movementsList.nextMovement();
                    i = willCoverStatements.getI();
                    i = doAnalysis(i, nowSymbol, movement, statementStack, nodeStack, streamStack);
                }
            }
            else {
                MovementsList movementsList = new MovementsList(nowMovements);
                Movement movement = movementsList.nextMovement();

                if (movementsList.hasNextMovement()){
                    rollbackStatements.add(new AnalysingStatement(i, movementsList, statementStack, nodeStack, streamStack));
                }
                i = doAnalysis(i, nowSymbol, movement, statementStack, nodeStack, streamStack);
            }
        }
        tree.setRoot(nodeStack.pop());
        return tree;
    }
}
