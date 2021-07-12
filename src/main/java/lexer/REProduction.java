package lexer;

import parser.CFGProduction;
import symbol.Symbol;

import java.io.Serializable;
import java.util.List;

public abstract class REProduction extends CFGProduction implements Serializable {

    public REProduction(CFGProduction part) {
        super(part);
    }

    public abstract NFA getNFANode(List<NFA> nodes, List<Symbol> childs) ;

}
