import java.io.Serializable;
import java.util.*;

public class TransformTable implements Serializable{
	
    private final Map<Integer, Map<Symbol, Movement>> table;
    
    private final Set<Integer> endStatements;
    
    private final CFG cfg;
    
    public Map<Integer, Map<Symbol, Movement>> getTableMap() {
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

    public void add(int statementIndex, Symbol nextSymbol, int nextStatementIndex) {
        Movement movement;
        if (nextSymbol.getType() == Symbol.UNTERMINATOR) {
            movement = new Movement(Movement.GOTO, nextStatementIndex);
        } else {
            movement = new Movement(Movement.SHIFT, nextStatementIndex);
        }
        if (!table.containsKey(statementIndex)) {
            table.put(statementIndex, new HashMap<>());
        }
        table.get(statementIndex).put(nextSymbol, movement);
    }

    public void add(int statementIndex, Symbol nextSymbol, CFGProduction production) {
        Movement movement = new Movement(production);
        if (!table.containsKey(statementIndex)) {
            table.put(statementIndex, new HashMap<>());
        }
        table.get(statementIndex).put(nextSymbol, movement);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        Set<Symbol> allSymbols = new HashSet<>();
        for (Integer i : table.keySet()) {
            allSymbols.addAll(table.get(i).keySet());
        }
        List<Integer> sortedStatementIndexes = new ArrayList<>(table.keySet());
        Collections.sort(sortedStatementIndexes);
        List<Symbol> certainAllSymbols = new ArrayList<>(allSymbols);
        for (Symbol s : certainAllSymbols) {
            result.append("\t");
            result.append(s.getName());
        }
        result.append("\n");
        for (Integer i : sortedStatementIndexes) {
            result.append(i);
            for (Symbol s : certainAllSymbols) {
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
    
    public AnalysisTree getAnalysisTree(List<SymbolExtra> symbols) throws PLDLAnalysisException, PLDLParsingException {
    	AnalysisTree tree = new AnalysisTree();
    	Stack<Integer> statementStack = new Stack<>();
    	Stack<AnalysisNode> nodeStack = new Stack<>();
    	statementStack.push(0);
    	int beginI = 0;
    	if (endStatements.size() <= 0) {
    		return null;
    	}
    	Symbol beginSymbol = table.get(endStatements.iterator().next()).get(cfg.getSymbolPool().getTerminator("eof")).getRegressionProduction().getBeforeSymbol();
    	while(beginI != symbols.size() - 1 || !symbols.get(beginI).getSymbol().equals(beginSymbol)){
    		int nowStatement = statementStack.peek();
    		SymbolExtra nowSymbolExtra = beginI < symbols.size() ? symbols.get(beginI) : new TerminatorExtra(cfg.getSymbolPool().getTerminator("eof"));
    		Movement movement =  table.get(nowStatement).get(nowSymbolExtra.getSymbol());
    		if (movement == null) {
    			throw new PLDLAnalysisException("程序分析到第 " + (beginI + 1) + " 个符号：" + nowSymbolExtra + " 时既无法移进，也无法规约。", null);
    		}
    		else {
	    		switch(movement.getMovement()) { 
	    			case Movement.SHIFT:
	    				nodeStack.push(new AnalysisNode(nowSymbolExtra));
	    			case Movement.GOTO:
	    				statementStack.push(movement.getShiftTo());
	    				++beginI;
	    				break;
	    			case Movement.REGRESSION:
	    				CFGProduction production = movement.getRegressionProduction();
	    				AnalysisNode node = new AnalysisNode(new UnterminatorExtra((Unterminator) production.getBeforeSymbol()));
	    				node.setChildren(new ArrayList<>());
	    				Stack<AnalysisNode> tempStack = new Stack<>();
		    			for (Symbol _: production.getAfterSymbols()) {
		    				statementStack.pop();
		    				tempStack.push(nodeStack.pop());
		    			}
		    			for (Symbol _: production.getAfterSymbols()) {
		    				node.getChildren().add(tempStack.pop());
		    			}
		    			nodeStack.push(node);
		    			--beginI;
		    			symbols.set(beginI, node.getValue());
		    			break;
	    		}
    		}
    	}
    	if (nodeStack.size() != 1) {
    		throw new PLDLAnalysisException("程序最终没有规约结束。符号栈中剩余：" + nodeStack, null);
    	}
    	else {
    		tree.setRoot(nodeStack.pop());
    	}
    	return tree;
    }
}
