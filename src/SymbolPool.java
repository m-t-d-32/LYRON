import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SymbolPool {
    private static Map<String, Unterminator> unterminators = null;
    private static Map<String, Terminator> terminators = null;

    public static void initUnterminatorString(Set<String> unterminatorStrs) throws PLDLParsingException {
        if (unterminators != null) {
            PLDLParsingWarning.setLog("非终结符集合已经初始化过，重新初始化可能会导致不可预料的问题。");
        }
        unterminators = new HashMap<>();
        for (String str : unterminatorStrs) {
            unterminators.put(str, new Unterminator(str));
        }
    }

    public static void initTerminatorString(Set<String> terminatorStrs) throws PLDLParsingException {
        if (terminators != null) {
            PLDLParsingWarning.setLog("终结符集合已经初始化过，重新初始化可能会导致不可预料的问题。");
        }
        terminators = new HashMap<>();
        for (String str : terminatorStrs) {
            terminators.put(str, new Terminator(str));
        }
        terminators.put("null", Terminator.getNullTerminator());
    }

    public static Set<String> getTerminatorsStr() {
        return terminators.keySet();
    }

    public static Collection<Terminator> getTerminators() {
        return terminators.values();
    }

    public static Set<String> getUnterminatorsStr() {
        return unterminators.keySet();
    }

    public static Collection<Unterminator> getUnterminators() {
        return unterminators.values();
    }

    public static void addUnterminatorStr(String str) {
        if (!unterminators.containsKey(str)) {
            unterminators.put(str, new Unterminator(str));
        }
    }

    public static void addTerminatorStr(String str) {
        if (!terminators.containsKey(str)) {
            terminators.put(str, new Terminator(str));
        }
    }

    public static Unterminator getUnterminator(String name) throws PLDLParsingException {
        if (unterminators.containsKey(name)) {
            return unterminators.get(name);
        }
        throw new PLDLParsingException("符号 " + name + " 不能识别为非终结符。", null);
    }

    public static Terminator getTerminator(String name) throws PLDLParsingException {
        if (terminators.containsKey(name)) {
            return terminators.get(name);
        }
        throw new PLDLParsingException("符号 " + name + " 不能识别为终结符。", null);
    }

    public static Symbol getSymbol(String name) throws PLDLParsingException {
        if (unterminators.containsKey(name)) {
            return getUnterminator(name);
        } else if (terminators.containsKey(name)) {
            return getTerminator(name);
        }
        throw new PLDLParsingException("符号 " + name + " 既不能识别为终结符，也不能识别为非终结符。", null);
    }
}