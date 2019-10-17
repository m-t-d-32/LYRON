package lexer;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import exception.REParsingException;
import parser.CFG;
import parser.Movement;
import parser.TransformTable;
import symbol.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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
		Map<Integer, Map<AbstractSymbol, Movement>> tableMap = table.getTableMap();
		Set<Integer> endStatements = table.getEndStatements();
		Stack<Integer> statementStack = new Stack<>();
		Stack<NFA> nodeStack = new Stack<>();
		Stack<Symbol> symbolStack = new Stack<>();
		statementStack.push(0);
		List<Symbol> symbols = getSymbols();
		int beginI = 0;
		if (endStatements.size() <= 0) {
			throw new PLDLAnalysisException("状态数为0或者小于0，程序失败。", null);
		}
		AbstractSymbol beginAbstractSymbol = tableMap.get(endStatements.iterator().next()).get(cfg.getSymbolPool().getTerminator("eof")).getRegressionProduction().getBeforeAbstractSymbol();
		while(beginI != symbols.size() - 1 || !symbols.get(beginI).getAbstractSymbol().equals(beginAbstractSymbol)){
			int nowStatement = statementStack.peek();
			Symbol nowSymbol = beginI < symbols.size() ? symbols.get(beginI) : new Terminator(cfg.getSymbolPool().getTerminator("eof"));
			Movement movement =  tableMap.get(nowStatement).get(nowSymbol.getAbstractSymbol());
			if (movement == null) {
				throw new PLDLAnalysisException("程序分析到第 " + (beginI + 1) + " 个符号：" + nowSymbol + " 时既无法移进，也无法归约。", null);
			}
			else {
				switch(movement.getMovement()) {
					case Movement.SHIFT:
						NFANode newNode = new NFANode();
						newNode.setFinal(true);
						nodeStack.push(new NFA(newNode));
						symbolStack.push(nowSymbol);
					case Movement.GOTO:
						statementStack.push(movement.getShiftTo());
						++beginI;
						break;
					case Movement.REGRESSION:
						REProduction production = (REProduction) movement.getRegressionProduction();
						List<NFA> tempNFA = new ArrayList<>();
						List<Symbol> tempSymbol = new ArrayList<>();
						for (AbstractSymbol ignored : production.getAfterAbstractSymbols()) {
							statementStack.pop();
							tempNFA.add(nodeStack.pop());
							tempSymbol.add(symbolStack.pop());
						}
						Collections.reverse(tempNFA);
						Collections.reverse(tempSymbol);
						nodeStack.push(production.getNFANode(tempNFA, tempSymbol));
						Symbol newSymbol = new Unterminator((AbstractUnterminator) production.getBeforeAbstractSymbol());
						symbolStack.push(newSymbol);
						--beginI;
						symbols.set(beginI, newSymbol);
						break;
				}
			}
		}
		if (nodeStack.size() != 1) {
			throw new PLDLAnalysisException("程序最终没有归约结束。符号栈中剩余：" + nodeStack, null);
		}
		else {
			letNFA = nodeStack.pop();
		}
	}
}
