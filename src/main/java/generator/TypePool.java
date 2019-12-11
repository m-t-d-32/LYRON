package generator;

import exception.PLDLAnalysisException;

import java.util.HashMap;
import java.util.Map;

public class TypePool {

    private Map<String, VariableType> typeMap = new HashMap<>();

    public Map<VariableType, String> getTransformMap() {
        return transformMap;
    }

    public void setTransformMap(Map<VariableType, String> transformMap) {
        this.transformMap = transformMap;
    }

    private Map<VariableType, String> transformMap = new HashMap<>();

    public void addToTypeMap(String typeName, VariableType typeBody){
        typeMap.put(typeName, typeBody);
    }

    public void addDefinedType(String typeName, String definedTypeName) throws PLDLAnalysisException {
        if (!typeMap.containsKey(definedTypeName)){
            throw new PLDLAnalysisException("类型" + definedTypeName + "没有定义", null);
        }
        else if (typeMap.containsKey(typeName)){
            throw new PLDLAnalysisException("类型" + typeName + "已经定义", null);
        }
        typeMap.put(typeName, typeMap.get(definedTypeName));
    }

    public VariableType getType(String typeName) throws PLDLAnalysisException {
        if (!typeMap.containsKey(typeName)){
            throw new PLDLAnalysisException("类型" + typeName + "没有定义", null);
        }
        return typeMap.get(typeName);
    }

    public boolean checkType(String typeName){
        if (!typeMap.containsKey(typeName)){
            return false;
        }
        return true;
    }

    public void initType(String typeName, int trueBaseName, int length){
        BaseType newBaseType = new BaseType(this);
        newBaseType.setProcessorType(trueBaseName);
        newBaseType.setLength(length);
        typeMap.put(typeName, newBaseType);
        transformMap.put(newBaseType, typeName);
    }

    public String linkArrayType(String newLinkerName, String definedTempName) {
        ArrayType resultType = new ArrayType(this);
        String resultName = newLinkerName + "_" + definedTempName;
        if (Character.isDigit(definedTempName.charAt(0))){
            resultType.getDimensionFactors().add(Integer.valueOf(definedTempName));
            resultType.getDimensionFactors().add(Integer.valueOf(newLinkerName));
        }
        else {
            ArrayType oldType = (ArrayType) typeMap.get(definedTempName);
            resultType.setDimensionFactors(oldType.getDimensionFactors());
            resultType.getDimensionFactors().add(Integer.valueOf(newLinkerName));
        }
        typeMap.put(resultName, resultType);
        if (!transformMap.containsKey(resultType)) {
            transformMap.put(resultType, resultName);
        }
        return resultName;
    }

    public void addToTransformMap(ArrayType arrayType, String typestr) {
        transformMap.put(arrayType, typestr);
    }
}
