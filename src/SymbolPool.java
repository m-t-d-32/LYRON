import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SymbolPool {
	
    private Map<String, AbstractUnterminator> unterminators = null;
    
    private Map<String, AbstractTerminator> terminators = null;

    public void initUnterminatorString(Set<String> unterminatorStrs) throws PLDLParsingException {
        if (unterminators != null) {
            PLDLParsingWarning.setLog("非终结符集合已经初始化过，重新初始化可能会导致不可预料的问题。");
        }
        unterminators = new HashMap<>();
        for (String str : unterminatorStrs) {
            unterminators.put(str, new AbstractUnterminator(str));
        }
    }

    public void initTerminatorString(Set<String> terminatorStrs) throws PLDLParsingException {
        if (terminators != null) {
            PLDLParsingWarning.setLog("终结符集合已经初始化过，重新初始化可能会导致不可预料的问题。");
        }
        terminators = new HashMap<>();
        for (String str : terminatorStrs) {
            terminators.put(str, new AbstractTerminator(str));
        }
        terminators.put("null", AbstractTerminator.getNullTerminator());
    }

    public Set<String> getTerminatorsStr() {
        return terminators.keySet();
    }

    public Collection<AbstractTerminator> getTerminators() {
        return terminators.values();
    }

    public Set<String> getUnterminatorsStr() {
        return unterminators.keySet();
    }

    public Collection<AbstractUnterminator> getUnterminators() {
        return unterminators.values();
    }

    public void addUnterminatorStr(String str) {
        if (!unterminators.containsKey(str)) {
            unterminators.put(str, new AbstractUnterminator(str));
        }
    }

    public void addTerminatorStr(String str) {
        if (!terminators.containsKey(str)) {
            terminators.put(str, new AbstractTerminator(str));
        }
    }

    public AbstractUnterminator getUnterminator(String name) throws PLDLParsingException {
        if (unterminators.containsKey(name)) {
            return unterminators.get(name);
        }
        throw new PLDLParsingException("符号 " + name + " 不能识别为非终结符。", null);
    }

    public AbstractTerminator getTerminator(String name) throws PLDLParsingException {
        if (terminators.containsKey(name)) {
            return terminators.get(name);
        }
        throw new PLDLParsingException("符号 " + name + " 不能识别为终结符。", null);
    }

    public AbstractSymbol getSymbol(String name) throws PLDLParsingException {
        if (unterminators.containsKey(name)) {
            return getUnterminator(name);
        } else if (terminators.containsKey(name)) {
            return getTerminator(name);
        }
        throw new PLDLParsingException("符号 " + name + " 既不能识别为终结符，也不能识别为非终结符。", null);
    }
}