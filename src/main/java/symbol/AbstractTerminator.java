package symbol;

import java.io.Serializable;

public class AbstractTerminator extends AbstractSymbol implements Serializable {

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

    private boolean isComment = false;

    public boolean getIsComment() {
        return isComment;
    }

    public void setIsComment(boolean comment) {
        isComment = comment;
    }
}
