package symbol;

import java.io.Serializable;
import java.util.HashMap;

public class Unterminal extends Symbol implements Serializable {

    public Unterminal(AbstractUnterminal u) {
        setAbstractSymbol(u);
        setProperties(new HashMap<>());
    }

    @Override
    public int getType() {
        return Symbol.UNTERMINAL;
    }

}
