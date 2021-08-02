package symbol;

import java.io.Serializable;

public abstract class AbstractSymbol implements Serializable {

    public static final int TERMINAL = 0x01, NONTERMINAL = 0xff;

    @Override
    public boolean equals(Object obj) {
        AbstractSymbol argument = (AbstractSymbol) (obj);
        return getType() == argument.getType() && getName().equals(argument.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public abstract String toString();

    public abstract int getType();

    public abstract String getName();
}
