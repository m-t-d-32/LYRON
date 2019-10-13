package lexer;

import lexer.NFA;
import parser.CFGProduction;
import symbol.Symbol;

import java.util.List;

public abstract class REProduction extends CFGProduction {
	
	public REProduction(CFGProduction part) {
		super(part);
	}

	public abstract NFA getNFANode(List<NFA> nodes, List<Symbol> childs) ;
	
}
