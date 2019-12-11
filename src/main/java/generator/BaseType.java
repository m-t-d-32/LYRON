package generator;

import java.util.Objects;

public class BaseType extends VariableType {

    /* 只是表示当今处理器能够处理的数据类型，不表示大小 */
    public static final int TYPE_INT = 0x00;
    public static final int TYPE_FLOAT = 0xff;

    public int getProcessorType() {
        return processorType;
    }

    public void setProcessorType(int processorType) {
        this.processorType = processorType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    private int processorType;
    private int length;

    public BaseType(TypePool pool) {
        super(pool);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseType baseType = (BaseType) o;
        return Objects.equals(processorType, baseType.processorType) &&
                Objects.equals(length, baseType.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processorType, length);
    }

    @Override
    public int getType() {
        return VariableType.BASE_TYPE;
    }
}
