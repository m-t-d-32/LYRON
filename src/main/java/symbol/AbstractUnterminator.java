package symbol;

import java.util.Set;

public class AbstractUnterminator extends AbstractSymbol {

    private String name = null;

    private Set<AbstractTerminator> firstSet = null;

    public AbstractUnterminator(String _name) {
        name = _name;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return AbstractSymbol.UNTERMINATOR;
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

    public void setFirstSet(Set<AbstractTerminator> firstSet) {
        this.firstSet = firstSet;
    }

    public Set<AbstractTerminator> getFirstSet() {
        return firstSet;
    }

}
