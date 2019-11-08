package parser;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import parser.*;
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
        table.get(statementIndex).put(nextAbstractSymbol, movement);
    }

    public void add(int statementIndex, AbstractSymbol nextAbstractSymbol, CFGProduction production) {
        Movement movement = new Movement(production);
        if (!table.containsKey(statementIndex)) {
            table.put(statementIndex, new HashMap<>());
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
            result.append("\t");
            result.append(s.getName());
        }
        result.append("\n");
        for (Integer i : sortedStatementIndexes) {
            result.append(i);
            for (AbstractSymbol s : certainAllAbstractSymbols) {
                result.append("\t");
                
                if (table.get(i).containsKey(s)) {
                	try {                		
						if (endStatements.contains(i) && s.equals(cfg.getSymbolPool().getTerminator("eof"))) {
							result.append("acc");
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
    	statementStack.push(0);
    	int beginI = 0;
    	if (endStatements.size() <= 0) {
    		return null;
    	}
    	AbstractSymbol beginAbstractSymbol = table.get(endStatements.iterator().next()).get(cfg.getSymbolPool().getTerminator("eof")).getRegressionProduction().getBeforeAbstractSymbol();
    	while(beginI != symbols.size() - 1 || !symbols.get(beginI).getAbstractSymbol().equals(beginAbstractSymbol)){
    		int nowStatement = statementStack.peek();
    		Symbol nowSymbol = beginI < symbols.size() ? symbols.get(beginI) : new Terminator(cfg.getSymbolPool().getTerminator("eof"));
    		Movement movement =  table.get(nowStatement).get(nowSymbol.getAbstractSymbol());
    		if (movement == null) {
    			throw new PLDLAnalysisException("程序分析到第 " + (beginI + 1) + " 个符号：" + nowSymbol + " 时既无法移进，也无法归约。", null);
    		}
    		else {
	    		switch(movement.getMovement()) {
	    			case Movement.SHIFT:
	    				nodeStack.push(new AnalysisNode(nowSymbol));
	    			case Movement.GOTO:
	    				statementStack.push(movement.getShiftTo());
	    				++beginI;
	    				break;
	    			case Movement.REGRESSION:
	    				CFGProduction production = movement.getRegressionProduction();
	    				AnalysisNode node = new AnalysisNode(new Unterminator((AbstractUnterminator) production.getBeforeAbstractSymbol()));
	    				node.setProduction(production);
	    				node.setChildren(new ArrayList<>());
	    				AbstractTerminator nullTerminator = cfg.getSymbolPool().getTerminator("null");
	    				Stack<AnalysisNode> tempStack = new Stack<>();
		    			for (AbstractSymbol symbol : production.getAfterAbstractSymbols()) {
		    				if (symbol != nullTerminator) {
								statementStack.pop();
								tempStack.push(nodeStack.pop());
							}
		    			}
		    			for (AbstractSymbol symbol : production.getAfterAbstractSymbols()) {
		    				if (symbol != nullTerminator) {
								node.getChildren().add(tempStack.pop());
							}
		    			}
		    			nodeStack.push(node);
		    			--beginI;
		    			symbols.set(beginI, node.getValue());
		    			break;
	    		}
    		}
    	}
    	if (nodeStack.size() != 1) {
    		throw new PLDLAnalysisException("程序最终没有归约结束。符号栈中剩余：" + nodeStack, null);
    	}
    	else {
    		tree.setRoot(nodeStack.pop());
    	}
    	return tree;
    }
}
