package generator;

import java.util.Objects;

public class PointerType extends VariableType {

    private VariableType pointToType;

    public PointerType(TypePool pool) {
        super(pool);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointerType pointerType = (PointerType) o;
        return Objects.equals(pointToType, pointerType.pointToType);
    }

    @Override
    public int hashCode() {
        return pointToType.hashCode();
    }

    @Override
    public int getType() {
        return VariableType.POINTER_TYPE;
    }

    public VariableType getPointToType() {
        return pointToType;
    }

    public void setPointToType(VariableType pointToType) {
        this.pointToType = pointToType;
    }
}
