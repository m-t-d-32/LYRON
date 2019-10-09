import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.dom4j.DocumentException;

public abstract class RE {
	
	private String reString = null;
	
	public String getReString() {
		return reString;
	}

	public void setReString(String reString) throws PLDLAnalysisException, PLDLParsingException {
		this.reString = reString;
		setNFA();
	}

	private NFA letNFA= null;
	
	protected CFG cfg = null;
	
	public RE(String reString) throws PLDLAnalysisException, PLDLParsingException, DocumentException {
		setCFG();
		this.reString = reString;
		setNFA();
	}
	
	protected abstract void setCFG();
	
	protected abstract List<SymbolExtra> getSymbols();
	
	public NFA getNFA() {
		return letNFA;
	}

	public void setNFA() throws PLDLAnalysisException, PLDLParsingException {
		if (letNFA == null) {
			TransformTable table = cfg.getTable();
			Map<Integer, Map<Symbol, Movement>> tableMap = table.getTableMap();
			Set<Integer> endStatements = table.getEndStatements();
	    	Stack<Integer> statementStack = new Stack<>();
	    	Stack<NFA> nodeStack = new Stack<>();
	    	Stack<SymbolExtra> symbolStack = new Stack<>();
	    	statementStack.push(0);
	    	List<SymbolExtra> symbols = getSymbols();
	    	int beginI = 0;
	    	if (endStatements.size() <= 0) {
	    		throw new PLDLAnalysisException("状态数为0或者小于0，程序失败。", null);
	    	}
	    	Symbol beginSymbol = tableMap.get(endStatements.iterator().next()).get(cfg.getSymbolPool().getTerminator("eof")).getRegressionProduction().getBeforeSymbol();
	    	while(beginI != symbols.size() - 1 || !symbols.get(beginI).getSymbol().equals(beginSymbol)){
	    		int nowStatement = statementStack.peek();
	    		SymbolExtra nowSymbolExtra = beginI < symbols.size() ? symbols.get(beginI) : new TerminatorExtra(cfg.getSymbolPool().getTerminator("eof"));
	    		Movement movement =  tableMap.get(nowStatement).get(nowSymbolExtra.getSymbol());
	    		if (movement == null) {
	    			throw new PLDLAnalysisException("程序分析到第 " + (beginI + 1) + " 个符号：" + nowSymbolExtra + " 时既无法移进，也无法规约。", null);
	    		}
	    		else {
		    		switch(movement.getMovement()) { 
		    			case Movement.SHIFT:
		    				nodeStack.push(new NFA());
		    				symbolStack.push(nowSymbolExtra);
		    			case Movement.GOTO:
		    				statementStack.push(movement.getShiftTo());
		    				++beginI;
		    				break;
		    			case Movement.REGRESSION:
		    				REProduction production = (REProduction) movement.getRegressionProduction();
		    				List<NFA> tempNFA = new ArrayList<>();
		    				List<SymbolExtra> tempSymbol = new ArrayList<>();
			    			for (Symbol _: production.getAfterSymbols()) {
			    				statementStack.pop();
			    				tempNFA.add(nodeStack.pop());
			    				tempSymbol.add(symbolStack.pop());
			    			}
			    			Collections.reverse(tempNFA);
			    			Collections.reverse(tempSymbol);
			    			nodeStack.push(production.getNFANode(tempNFA, tempSymbol));
			    			SymbolExtra newSymbolExtra = new UnterminatorExtra((Unterminator) production.getBeforeSymbol());
			    			symbolStack.push(newSymbolExtra);
			    			--beginI;
			    			symbols.set(beginI, newSymbolExtra);
			    			break;
		    		}
	    		}
	    	}
	    	if (nodeStack.size() != 1) {
	    		throw new PLDLAnalysisException("程序最终没有规约结束。符号栈中剩余：" + nodeStack, null);
	    	}
	    	else {
	    		letNFA = nodeStack.pop();
	    	}
		}
	}
}
