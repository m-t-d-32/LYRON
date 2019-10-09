public abstract class Symbol {
	
    public static final int TERMINATOR = 0x01, UNTERMINATOR = 0xff;

    @Override
    public boolean equals(Object obj) {
        Symbol argument = (Symbol) (obj);
        return getType() == argument.getType() && getName().equals(argument.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public abstract String toString();

    abstract int getType();

    abstract String getName();
}
