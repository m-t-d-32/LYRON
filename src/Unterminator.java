import java.util.Set;

public class Unterminator extends Symbol {

    private String name = null;

    private Set<Terminator> firstSet = null;

    private Set<CFGProduction> beginProductions = null;

    public Unterminator(String _name) {
        name = _name;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return Symbol.UNTERMINATOR;
    }

    private boolean canEmpty = false;

    public boolean getCanEmpty() {
        return canEmpty;
    }

    public void setCanEmpty(boolean canEmpty) {
        this.canEmpty = canEmpty;
    }

    @Override
    public String toString() {
        return "非终结符：" + name;
    }

    public void setFirstSet(Set<Terminator> firstSet) {
        this.firstSet = firstSet;
    }

    public Set<Terminator> getFirstSet() {
        return firstSet;
    }

    public Set<CFGProduction> getBeginProductions() {
        return beginProductions;
    }

    public void setBeginProductions(Set<CFGProduction> beginProductions) {
        this.beginProductions = beginProductions;
    }
}
