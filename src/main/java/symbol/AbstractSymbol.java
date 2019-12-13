package symbol;

public abstract class AbstractSymbol {

    public static final int TERMINATOR = 0x01, UNTERMINATOR = 0xff;

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
