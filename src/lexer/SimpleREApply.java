package lexer;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import exception.REParsingException;
import lexer.NFA;
import lexer.NFANode;
import lexer.RE;
import parser.CFG;
import parser.CFGProduction;
import symbol.AbstractTerminator;
import symbol.Symbol;
import symbol.SymbolPool;
import symbol.Terminator;

import java.util.*;

public class SimpleREApply extends RE {

	public SimpleREApply(String str) throws PLDLParsingException, PLDLAnalysisException, REParsingException {
		super(str);		
	}

	@Override
	protected void setCFG() {
		Set<String> terminatorStrs = new HashSet<>(Arrays.asList("|","(", ")", "*", "+", "[", "]", "-", "char", "^", "."));
		Set<String> unterminatorStrs = new HashSet<>(Arrays.asList("E", "T", "F", "Fx", "Fxs"));
		SymbolPool pool = new SymbolPool();
		try {
			pool.initTerminatorString(terminatorStrs);
			pool.initUnterminatorString(unterminatorStrs);
			List<REProduction> res = new ArrayList<>(Arrays.asList(
					new REProduction(CFGProduction.getCFGProductionFromCFGString("E -> E | T", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							//3 nodes
							NFANode beginNode = new NFANode();
							NFANode endNode = new NFANode();
							endNode.setFinal(true);
							beginNode.addToTransformTable("null", nodes.get(0).getRoot());
							beginNode.addToTransformTable("null", nodes.get(2).getRoot());
							for (NFANode node : nodes.get(0).getFinalNodes()) {
								node.setFinal(false);
								node.addToTransformTable("null", endNode);
							}
							for (NFANode node : nodes.get(2).getFinalNodes()) {
								node.setFinal(false);
								node.addToTransformTable("null", endNode);
							}
							NFA result = new NFA(beginNode);
							result.getFinalNodes().add(endNode);
							return result;
						}

					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("E -> T", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							return nodes.get(0);
						}

					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("T -> T F", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							//2 nodes
							NFANode beginNode = new NFANode();
							NFANode endNode = new NFANode();
							endNode.setFinal(true);
							beginNode.addToTransformTable("null", nodes.get(0).getRoot());
							for (NFANode node : nodes.get(0).getFinalNodes()) {
								node.setFinal(false);
								node.addToTransformTable("null", nodes.get(1).getRoot());
							}
							for (NFANode node : nodes.get(1).getFinalNodes()) {
								node.setFinal(false);
								node.addToTransformTable("null", endNode);
							}
							NFA result = new NFA(beginNode);
							result.getFinalNodes().add(endNode);
							return result;
						}


					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("T -> F", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							return nodes.get(0);
						}

					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("F -> ( E )", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							return nodes.get(1);
						}

					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("F -> F *", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							NFANode beginNode = new NFANode();
							NFANode endNode = new NFANode();
							endNode.setFinal(true);
							beginNode.addToTransformTable("null", nodes.get(0).getRoot());
							for (NFANode node : nodes.get(0).getFinalNodes()) {
								node.addToTransformTable("null", nodes.get(0).getRoot());
								node.addToTransformTable("null", endNode);
								node.setFinal(false);
							}
							beginNode.addToTransformTable("null", endNode);
							NFA result = new NFA(beginNode);
							result.getFinalNodes().add(endNode);
							return result;
						}

					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("F -> F +", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							NFANode beginNode = new NFANode();
							NFANode endNode = new NFANode();
							endNode.setFinal(true);
							beginNode.addToTransformTable("null", nodes.get(0).getRoot());
							for (NFANode node : nodes.get(0).getFinalNodes()) {
								node.addToTransformTable("null", nodes.get(0).getRoot());
								node.addToTransformTable("null", endNode);
								node.setFinal(false);
							}
							NFA result = new NFA(beginNode);
							result.getFinalNodes().add(endNode);
							return result;
						}

					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("F -> Fx", cfg)) {
						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							return nodes.get(0);
						}
					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("Fx -> .", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							NFANode beginNode = new NFANode();
							for (char c = 1; c < 127; ++c) {
								beginNode.addToTransformTable(String.valueOf(c), nodes.get(0).getRoot());
							}
							nodes.get(0).getRoot().setFinal(true);
							NFA result = new NFA(beginNode);
							result.getFinalNodes().add(nodes.get(0).getRoot());
							return result;
						}

					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("Fx -> char", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							NFANode beginNode = new NFANode();
							beginNode.addToTransformTable((String) childs.get(0).getProperties().get("name"), nodes.get(0).getRoot());
							nodes.get(0).getRoot().setFinal(true);
							NFA result = new NFA(beginNode);
							result.getFinalNodes().add(nodes.get(0).getRoot());
							return result;
						}

					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("Fx -> char - char", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							String tempBegin = (String) childs.get(0).getProperties().get("name");
							String tempEnd = (String) childs.get(2).getProperties().get("name");
							char beginSymbol = (char) Math.min(tempBegin.charAt(0), tempEnd.charAt(0));
							char endSymbol = (char) Math.max(tempBegin.charAt(0), tempEnd.charAt(0));

							NFANode beginNode = new NFANode();
							for (char t = beginSymbol; t <= endSymbol; ++t) {
								beginNode.addToTransformTable(String.valueOf(t), nodes.get(0).getRoot());
							}
							nodes.get(0).getRoot().setFinal(true);
							NFA result = new NFA(beginNode);
							result.getFinalNodes().add(nodes.get(0).getRoot());
							return result;
						}
					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("Fxs -> Fxs Fx", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							//2 nodes							
							NFA result = nodes.get(0);
							NFANode beginNode = result.getRoot();
							beginNode.getStateTransformTable().putAll(nodes.get(1).getRoot().getStateTransformTable());
							for (NFANode end : nodes.get(1).getFinalNodes()) {
								for (NFANode trueEnd : nodes.get(0).getFinalNodes()) {
									end.addToTransformTable("null", trueEnd);
									end.setFinal(false);
								}
							}
							return result;
						}

					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("Fxs -> Fx", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							return nodes.get(0);
						}

					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("F -> [ Fxs ]", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							return nodes.get(1);
						}

					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("F -> [ ^ Fxs ]", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<Symbol> childs) {
							//Assert: nodes.get(2).getRoot().getStateTransformTable().values().size() == 1							

							NFANode beginNode = new NFANode();
							Set<String> chars = new HashSet<>();
							for (char c = 1; c < 127; ++c) {
								chars.add(String.valueOf(c));
							}
							chars.removeAll(nodes.get(2).getRoot().getStateTransformTable().keySet());
							NFANode next = nodes.get(2).getRoot().getStateTransformTable().values()
									.iterator().next().iterator().next();
							for (String s : chars) {
								beginNode.addToTransformTable(s, next);
							}
							NFA result = new NFA(beginNode);
							result.setFinalNodes(nodes.get(2).getFinalNodes());
							return result;
						}

					}));
			cfg = new CFG(pool, res, "E");
		} catch (PLDLParsingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}	
		
	}

	private Terminator getCharTerminator(char c) throws PLDLParsingException {
		AbstractTerminator abstractTerminator = cfg.getSymbolPool().getTerminator("char");
		Terminator sym = new Terminator(abstractTerminator);
		sym.addProperty("name", String.valueOf(c));
		return sym;
	}

	@Override
	protected List<Symbol> getSymbols() throws REParsingException {
		List<Symbol> result = new ArrayList<>();
		String reString = getReString();
		for (int i = 0; i < reString.length(); ++i) {
			char c = reString.charAt(i);
			try {
				if (c == '\\'){
					++i;
					if (i >= reString.length()){
						throw new REParsingException("\\后面没有任何符号，这不是一个正确的正则表达式。", null);
					}
					else {
						c = reString.charAt(i);
						switch(c){
							case '-':
							case '+':
							case '*':
							case '|':
							case '[':
							case ']':
							case '(':
							case '^':
							case '.':
							case ')': result.add(getCharTerminator(c)); break;
							case 'r': result.add(getCharTerminator('\r')); break;
							case 'n': result.add(getCharTerminator('\n')); break;
							case 't': result.add(getCharTerminator('\t')); break;
							case 'f': result.add(getCharTerminator('\f')); break;
							case '\\': result.add(getCharTerminator('\\')); break;
						}
					}
				}
				else if (c == '-' || c == '+' || c == '*' || c == '|' || c == '(' || c == ')' || c == '[' || c == ']' || c == '^' || c == '.') {
					AbstractTerminator abstractTerminator = cfg.getSymbolPool().getTerminator(String.valueOf(c));
					Terminator sym = new Terminator(abstractTerminator);
					result.add(sym);
				}
				else if (c != '\n' && c != '\r') {
					result.add(getCharTerminator(c));
				}
			}
			catch (PLDLParsingException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
}
