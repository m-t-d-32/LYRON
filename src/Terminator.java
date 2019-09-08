
public class Terminator extends Symbol {

    private String name = null;

    public Terminator(String _name) {
        name = _name;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return Symbol.TERMINATOR;
    }

    @Override
    public String toString() {
        return "终结符：" + name;
    }

    public static Terminator getNullTerminator() {
        return new Terminator("null");
    }
}
