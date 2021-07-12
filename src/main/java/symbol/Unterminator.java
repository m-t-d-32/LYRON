package symbol;

import java.io.Serializable;
import java.util.HashMap;

public class Unterminator extends Symbol implements Serializable {

    public Unterminator(AbstractUnterminator u) {
        setAbstractSymbol(u);
        setProperties(new HashMap<>());
    }

    @Override
    public int getType() {
        return Symbol.UNTERMINATOR;
    }

}
