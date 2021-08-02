package symbol;

import java.io.Serializable;
import java.util.HashMap;

public class Nonterminal extends Symbol implements Serializable {

    public Nonterminal(AbstractNonterminal u) {
        setAbstractSymbol(u);
        setProperties(new HashMap<>());
    }

    @Override
    public int getType() {
        return Symbol.NONTERMINAL;
    }

}
