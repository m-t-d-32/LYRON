package symbol;

import java.io.Serializable;
import java.util.Set;

public class AbstractNonterminal extends AbstractSymbol implements Serializable {

    private String name = null;

    private Set<AbstractTerminal> firstSet = null;

    public AbstractNonterminal(String _name) {
        name = _name;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return AbstractSymbol.NONTERMINAL;
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

    public void setFirstSet(Set<AbstractTerminal> firstSet) {
        this.firstSet = firstSet;
    }

    public Set<AbstractTerminal> getFirstSet() {
        return firstSet;
    }

}
