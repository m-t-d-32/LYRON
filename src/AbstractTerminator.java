
public class AbstractTerminator extends AbstractSymbol {

    private String name = null;

    public AbstractTerminator(String _name) {
        name = _name;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return AbstractSymbol.TERMINATOR;
    }

    @Override
    public String toString() {
        return "终结符：" + name;
    }

    public static AbstractTerminator getNullTerminator() {
        return new AbstractTerminator("null");
    }
}
