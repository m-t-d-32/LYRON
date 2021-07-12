package symbol;

import java.io.Serializable;
import java.util.HashMap;

public class Terminator extends Symbol implements Serializable {

    public Terminator(AbstractTerminator t) {
        setAbstractSymbol(t);
        setProperties(new HashMap<>());
    }

    @Override
    public int getType() {
        return Symbol.UNTERMINATOR;
    }
}
