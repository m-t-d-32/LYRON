package parser;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import symbol.Terminator;
import symbol.*;

import java.io.Serializable;
import java.util.*;

public class TransformTable implements Serializable{

    private static final long serialVersionUID = 8448616257039658718L;

    private final Map<Integer, Map<AbstractSymbol, Movement>> table;
    
    private final Set<Integer> endStatements;
    
    private final CFG cfg;
    
    public Map<Integer, Map<AbstractSymbol, Movement>> getTableMap() {
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
        if (nextAbstractSymbol.getType() == AbstractSymbol.UNTERMINATOR) {
            movement = new Movement(Movement.GOTO, nextStatementIndex);
        } else {
            movement = new Movement(Movement.SHIFT, nextStatementIndex);
        }
        if (!table.containsKey(statementIndex)) {
            table.put(statementIndex, new HashMap<>());
        }
        if (table.get(statementIndex).containsKey(nextAbstractSymbol)){
            System.err.println("移进/归约冲突发生");
            System.err.println(table.get(statementIndex).get(nextAbstractSymbol).getRegressionProduction());
        }
        table.get(statementIndex).put(nextAbstractSymbol, movement);
    }

    public void add(int statementIndex, AbstractSymbol nextAbstractSymbol, CFGProduction production) {
        Movement movement = new Movement(production);
        if (!table.containsKey(statementIndex)) {
            table.put(statementIndex, new HashMap<>());
        }
        if (table.get(statementIndex).containsKey(nextAbstractSymbol)){
            System.err.println("归约/归约冲突发生");
            System.err.println(table.get(statementIndex).get(nextAbstractSymbol).getRegressionProduction());
        }
        table.get(statementIndex).put(nextAbstractSymbol, movement);
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
                        if (endStatements.contains(i) && s.equals(cfg.getSymbolPool().getTerminator("eof"))) {
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

    public AnalysisTree getAnalysisTree(List<Symbol> symbols) throws PLDLAnalysisException, PLDLParsingException {
        AnalysisTree tree = new AnalysisTree();
        Stack<Integer> statementStack = new Stack<>();
        Stack<AnalysisNode> nodeStack = new Stack<>();
        Stack<Symbol> symbolStack = new Stack<>();
        statementStack.push(0);
        if (endStatements.size() <= 0) {
            return null;
        }
        int i = 0;
        while(true){
            int nowStatement = statementStack.peek();
            Symbol nowSymbol = i < symbols.size() ? symbols.get(i) : new Terminator(cfg.getSymbolPool().getTerminator("eof"));
            Movement movement = table.get(nowStatement).get(nowSymbol.getAbstractSymbol());
            if (movement == null) {
                throw new PLDLAnalysisException("程序分析到第 " + (i + 1) + " 个符号：" + nowSymbol + " 时既无法移进，也无法归约。", null);
            } else if (movement.getMovement() == Movement.SHIFT) {
                nodeStack.push(new AnalysisNode(nowSymbol));
                symbolStack.push(nowSymbol);
                statementStack.push(movement.getShiftTo());
                if (i < symbols.size()){
                    ++i;
                }
                else {
                    throw new PLDLAnalysisException("程序分析到第 " + (i + 1) + " 个符号：" + nowSymbol + " 时移进失败。", null);
                }
            } else if (movement.getMovement() == Movement.REGRESSION) {
                CFGProduction production = movement.getRegressionProduction();
//                System.out.println("归约：" + production.toString());
                AnalysisNode node = new AnalysisNode(new Unterminator((AbstractUnterminator) production.getBeforeAbstractSymbol()));
                node.setProduction(production);
                node.setChildren(new ArrayList<>());
                AbstractTerminator nullTerminator = cfg.getSymbolPool().getTerminator("null");
                Stack<AnalysisNode> tempStack = new Stack<>();
                for (AbstractSymbol symbol : production.getAfterAbstractSymbols()) {
                    if (symbol != nullTerminator) {
                        symbolStack.pop();
                        statementStack.pop();
                        tempStack.push(nodeStack.pop());
                    }
                }
                for (AbstractSymbol symbol : production.getAfterAbstractSymbols()) {
                    if (symbol != nullTerminator) {
                        AnalysisNode childNode = tempStack.pop();
                        childNode.setParent(node);
                        node.getChildren().add(childNode);
                    }
                }
                nodeStack.push(node);
                symbolStack.push(node.getValue());

                if (endStatements.contains(nowStatement) && i == symbols.size()){
                    break;
                }

                movement = table.get(statementStack.peek()).get(node.getValue().getAbstractSymbol());

                if (movement.getMovement() != Movement.GOTO){
                    throw new PLDLAnalysisException("程序分析到第 " + (i + 1) + " 个符号：" + nowSymbol + " 时既无法移进，也无法归约。", null);
                }
                else {
                    statementStack.push(movement.getShiftTo());
                }
            }
        }
        if (nodeStack.size() != 1) {
            throw new PLDLAnalysisException("程序最终没有归约结束。符号栈中剩余：" + nodeStack, null);
        }
        else {
            tree.setRoot(nodeStack.pop());
        }
//        System.out.println(tree);
        return tree;
    }
}
