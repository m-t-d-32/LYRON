package symbol;

import java.io.Serializable;
import java.util.HashMap;

public class Terminal extends Symbol implements Serializable {

    public Terminal(AbstractTerminal t) {
        setAbstractSymbol(t);
        setProperties(new HashMap<>());
    }

    @Override
    public int getType() {
        return Symbol.NONTERMINAL;
    }
}
