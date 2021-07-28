package symbol;

import java.io.Serializable;

public class AbstractTerminal extends AbstractSymbol implements Serializable {

    private String name = null;

    public AbstractTerminal(String _name) {
        name = _name;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return AbstractSymbol.TERMINAL;
    }

    @Override
    public String toString() {
        return "终结符：" + name;
    }

    public static AbstractTerminal getNullTerminal() {
        return new AbstractTerminal("null");
    }

    private boolean isComment = false;

    public boolean getIsComment() {
        return isComment;
    }

    public void setIsComment(boolean comment) {
        isComment = comment;
    }
}
