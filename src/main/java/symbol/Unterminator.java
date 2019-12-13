package symbol;

import java.util.HashMap;

public class Unterminator extends Symbol {

    public Unterminator(AbstractUnterminator u) {
        setAbstractSymbol(u);
        setProperties(new HashMap<>());
    }

    @Override
    public int getType() {
        return Symbol.UNTERMINATOR;
    }

}
