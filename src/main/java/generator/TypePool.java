package generator;

import exception.PLDLAnalysisException;

import java.util.Map;

public class TypePool {

    private Map<String, VariableType> typeMap;

    public void addToTypeMap(String typeName, VariableType typeBody){
        typeMap.put(typeName, typeBody);
    }

    public void addDefinedType(String typeName, String definedTypeName) throws PLDLAnalysisException {
        if (!typeMap.keySet().contains(definedTypeName)){
            throw new PLDLAnalysisException("类型" + definedTypeName + "没有定义", null);
        }
        typeMap.put(typeName, typeMap.get(definedTypeName));
    }

    public VariableType getType(String typeName) throws PLDLAnalysisException {
        if (!typeMap.keySet().contains(typeName)){
            throw new PLDLAnalysisException("类型" + typeName + "没有定义", null);
        }
        return typeMap.get(typeName);
    }
}
