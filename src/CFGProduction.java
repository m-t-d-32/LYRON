import java.util.ArrayList;
import java.util.List;


public class CFGProduction implements Cloneable {

    private Symbol beforeSymbol = null;

    private List<Symbol> afterSymbols = null;

    public static CFGProduction GetCFGProductionFromCFGString(String CFGProductionString) throws PLDLParsingException {
        CFGProduction resultProduction = new CFGProduction();
        if (CFGProductionString.contains("->")) {
            String[] splits = CFGProductionString.split("->");
            if (splits.length == 2) {
                String beforeStr = splits[0], afterStr = splits[1];
                String[] beforeStrs = beforeStr.trim().split(" +"), afterStrs = afterStr.trim().split(" +");
                if (beforeStrs.length == 1) {
                    try {
                        resultProduction.beforeSymbol = SymbolPool.getUnterminator("_" + beforeStrs[0]);
                    } catch (PLDLParsingException e) {
                        throw new PLDLParsingException("产生式左部不是非终结符，因而这不是一个合法的产生式。", e);
                    }
                    resultProduction.afterSymbols = new ArrayList<>();
                    if (afterStrs.length == 1 && afterStrs[0].equals("null")) {
                        resultProduction.afterSymbols.add(SymbolPool.getTerminator("null"));
                        return resultProduction;
                    } else if (afterStrs.length > 0 && afterStrs[0].length() > 0) {
                        for (int i = 0; i < afterStrs.length; ++i) {
                            try {
                                resultProduction.afterSymbols.add(SymbolPool.getSymbol("_" + afterStrs[i]));
                            } catch (PLDLParsingException e) {
                                throw new PLDLParsingException("产生式右部第 " + String.valueOf(i + 1) + " 个符号既不能识别为终结符，也不能识别为非终结符。是否忘记使用空格隔开？", e);
                            }
                        }
                        return resultProduction;
                    } else {
                        throw new PLDLParsingException("产生式右部没有任何字符，如果你需要表示空产生式，请将产生式右部设置为null。", null);
                    }
                }
                throw new PLDLParsingException("产生式左部不是1个符号，请检查。注意：上下文无关文法的产生式左部必须有且只有一个非终结符。", null);
            }
        }
        throw new PLDLParsingException("产生式没有使用箭头\'->\'分割为两部分，因而无法解析。请将产生式写成\'A -> c B d\'这样的格式（这里的A、B是非终结符，c、d是终结符）", null);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("产生式：【");
        result.append(beforeSymbol.toString());
        result.append("】->");
        for (Symbol afterSymbolName : afterSymbols) {
            result.append("【");
            result.append(afterSymbolName.toString());
            result.append("】");
        }
        return result.toString();
    }

    public Symbol getBeforeSymbol() {
        return beforeSymbol;
    }

    public void setBeforeSymbol(Unterminator symbol) {
        beforeSymbol = symbol;
    }

    public List<Symbol> getAfterSymbols() {
        return afterSymbols;
    }

    public void setAfterSymbol(List<Symbol> symbols) {
        afterSymbols = symbols;
    }

    @Override
    public boolean equals(Object obj) {
        CFGProduction argument = (CFGProduction) (obj);
        if (!beforeSymbol.equals(argument.beforeSymbol)) {
            return false;
        } else if (afterSymbols.size() != argument.afterSymbols.size()) {
            return false;
        }
        for (int i = 0; i < afterSymbols.size(); ++i) {
            if (afterSymbols.get(i).getType() != argument.afterSymbols.get(i).getType() ||
                    !afterSymbols.get(i).equals(argument.afterSymbols.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Object clone() {
        CFGProduction clonedProduction = new CFGProduction();
        clonedProduction.beforeSymbol = beforeSymbol;
        clonedProduction.afterSymbols = new ArrayList<>();
        for (Symbol s : afterSymbols) {
            clonedProduction.afterSymbols.add(s);
        }
        return clonedProduction;
    }

    @Override
    public int hashCode() {
        int hash = beforeSymbol.hashCode();
        for (Symbol s : afterSymbols) {
            hash ^= s.hashCode();
        }
        return hash;
    }
}
