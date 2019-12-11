package generator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class VariableType {

    public static final int BASE_TYPE = 0x01;
    public static final int OBJECT_TYPE = 0xff;
    public static final int POINTER_TYPE = 0x05;
    public static final int ARRAY_TYPE = 0xfe;

    public abstract int getType();

    public TypePool getPool() {
        return pool;
    }

    private TypePool pool;

    public VariableType(TypePool pool){
        this.pool = pool;
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();
}
