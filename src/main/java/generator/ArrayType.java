package generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArrayType extends VariableType {

    public VariableType getPointToType() {
        return pointToType;
    }

    public void setPointToType(VariableType pointToType) {
        this.pointToType = pointToType;
    }

    private VariableType pointToType;

    public void setDimensionFactors(List<Integer> dimensionFactors) {
        this.dimensionFactors = dimensionFactors;
    }

    public List<Integer> getDimensionFactors() {
        return dimensionFactors;
    }

    private List<Integer> dimensionFactors = new ArrayList<>();

    public ArrayType(TypePool pool) {
        super(pool);
    }

    @Override
    public int getType() {
        return VariableType.ARRAY_TYPE;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < dimensionFactors.size(); ++i){
            stringBuilder.append(String.valueOf(dimensionFactors.get(i)));
        }
        stringBuilder.append(getPool().getTransformMap().get(pointToType));
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        return Objects.equals(pointToType, arrayType.pointToType) &&
                Objects.equals(dimensionFactors, arrayType.dimensionFactors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointToType, dimensionFactors);
    }
}
