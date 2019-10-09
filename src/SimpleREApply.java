import java.util.*;

import org.dom4j.DocumentException;

public class SimpleREApply extends RE{

	public SimpleREApply(String str) throws PLDLParsingException, PLDLAnalysisException, DocumentException{
		super(str);		
	}

	@Override
	protected void setCFG() {
		Set<String> terminatorStrs = new HashSet<>(Arrays.asList("|","(", ")", "*", "+", "-", "char"));
		Set<String> unterminatorStrs = new HashSet<>(Arrays.asList("E", "T", "F"));
		SymbolPool pool = new SymbolPool();
		try {
			pool.initTerminatorString(terminatorStrs);
			pool.initUnterminatorString(unterminatorStrs);
			cfg = new CFG(pool, null, "E");
			List<REProduction> res = new ArrayList<REProduction>(Arrays.asList(
					new REProduction(CFGProduction.getCFGProductionFromCFGString("E -> E | T", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<SymbolExtra> childs) {
							//3 nodes
							NFANode beginNode = new NFANode();
							NFANode endNode = new NFANode();
							endNode.setFinal(true);
							beginNode.addToTransformTable("null", nodes.get(0).getRoot());
							beginNode.addToTransformTable("null", nodes.get(2).getRoot());
							for (NFANode node: nodes.get(0).getFinalNodes()) {
								node.setFinal(false);
								node.addToTransformTable("null", endNode);
							}
							for (NFANode node: nodes.get(2).getFinalNodes()) {
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
						public NFA getNFANode(List<NFA> nodes, List<SymbolExtra> childs) {
							return nodes.get(0);
						}
											
					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("T -> T F", cfg)) {
						
						@Override
						public NFA getNFANode(List<NFA> nodes, List<SymbolExtra> childs) {
							//2 nodes
							NFANode beginNode = new NFANode();
							NFANode endNode = new NFANode();
							endNode.setFinal(true);
							beginNode.addToTransformTable("null", nodes.get(0).getRoot());
							for (NFANode node: nodes.get(0).getFinalNodes()) {
								node.setFinal(false);
								node.addToTransformTable("null", nodes.get(1).getRoot());
							}
							for (NFANode node: nodes.get(1).getFinalNodes()) {
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
						public NFA getNFANode(List<NFA> nodes, List<SymbolExtra> childs) {
							return nodes.get(0);
						}
											
					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("F -> char", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<SymbolExtra> childs) {
							NFANode beginNode = new NFANode();
							beginNode.addToTransformTable((String) childs.get(0).getProperties().get("name"), nodes.get(0).getRoot());
							nodes.get(0).getRoot().setFinal(true);
							NFA result = new NFA(beginNode);
							result.getFinalNodes().add(nodes.get(0).getRoot());
							return result;
						}
											
					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("F -> ( E )", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<SymbolExtra> childs) {
							return nodes.get(1);
						}
										
					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("F -> F *", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<SymbolExtra> childs) {
							NFANode beginNode = new NFANode();
							NFANode endNode = new NFANode();
							endNode.setFinal(true);
							beginNode.addToTransformTable("null", nodes.get(0).getRoot());
							for (NFANode node: nodes.get(0).getFinalNodes()) {
								node.addToTransformTable("null", nodes.get(0).getRoot());
								node.addToTransformTable("null", endNode);
							}
							beginNode.addToTransformTable("null", endNode);
							NFA result = new NFA(beginNode);
							result.getFinalNodes().add(endNode);
							return result;
						}
											
					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("F -> F +", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<SymbolExtra> childs) {
							NFANode beginNode = new NFANode();
							NFANode endNode = new NFANode();
							endNode.setFinal(true);
							beginNode.addToTransformTable("null", nodes.get(0).getRoot());
							for (NFANode node: nodes.get(0).getFinalNodes()) {
								node.addToTransformTable("null", nodes.get(0).getRoot());
								node.addToTransformTable("null", endNode);
							}
							NFA result = new NFA(beginNode);
							result.getFinalNodes().add(endNode);
							return result;
						}
											
					},
					new REProduction(CFGProduction.getCFGProductionFromCFGString("F -> char - char", cfg)) {

						@Override
						public NFA getNFANode(List<NFA> nodes, List<SymbolExtra> childs) {
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
					}));
			cfg.setCFGProductions(res);
		} catch (PLDLParsingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}	
		
	}

	@Override
	protected List<SymbolExtra> getSymbols() {
		List<SymbolExtra> result = new ArrayList<>();
		String reString = getReString();
		for (int i = 0; i < reString.length(); ++i) {
			char c = reString.charAt(i);
			try {
				if (c == '-' || c == '+' || c == '*' || c == '|' || c == '(' || c == ')') {
					Terminator terminator = cfg.getSymbolPool().getTerminator(String.valueOf(c));
					TerminatorExtra sym = new TerminatorExtra(terminator);
					result.add(sym);
				}
				else if (c != ' ' && c != '\n' && c != '\r') {
					Terminator terminator = cfg.getSymbolPool().getTerminator("char");
					TerminatorExtra sym = new TerminatorExtra(terminator);
					sym.addProperty("name", String.valueOf(c));
					result.add(sym);
				}
			}
			catch (PLDLParsingException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
}
