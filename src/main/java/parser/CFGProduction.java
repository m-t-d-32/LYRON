package parser;

import exception.PLDLParsingException;
import symbol.AbstractSymbol;
import symbol.SymbolPool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class CFGProduction implements Serializable {

    private AbstractSymbol beforeAbstractSymbol = null;

    private List<AbstractSymbol> afterAbstractSymbols = null;
    
    public CFGProduction() {

    }
    
    public CFGProduction(CFGProduction another) {
        beforeAbstractSymbol = another.beforeAbstractSymbol;
        afterAbstractSymbols = new ArrayList<>();
        afterAbstractSymbols.addAll(another.afterAbstractSymbols);
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    private int serialNumber = -1;

    public static CFGProduction getCFGProductionFromCFGString(String CFGProductionString, SymbolPool pool) throws PLDLParsingException {
        CFGProduction resultProduction = new CFGProduction();
        if (CFGProductionString.contains("->")) {
            String[] splits = CFGProductionString.split("->");
            if (splits.length == 2) {
                String beforeStr = splits[0], afterStr = splits[1];
                String[] beforeStrs = beforeStr.trim().split(" +"), afterStrs = afterStr.trim().split(" +");
                if (beforeStrs.length == 1) {
                    try {
                        resultProduction.beforeAbstractSymbol = pool.getNonterminal(beforeStrs[0]);
                    } catch (PLDLParsingException e) {
                        throw new PLDLParsingException("产生式左部不是非终结符，因而这不是一个合法的产生式。", e);
                    }
                    resultProduction.afterAbstractSymbols = new ArrayList<>();
                    if (afterStrs.length == 1 && afterStrs[0].equals("null")) {
                        resultProduction.afterAbstractSymbols.add(pool.getTerminal("null"));
                        return resultProduction;
                    } else if (afterStrs.length > 0 && afterStrs[0].length() > 0) {
                        for (int i = 0; i < afterStrs.length; ++i) {
                            try {
                                resultProduction.afterAbstractSymbols.add(pool.getSymbol(afterStrs[i]));
                            } catch (PLDLParsingException e) {
                                throw new PLDLParsingException("产生式右部第 " + (i + 1) + " 个符号既不能识别为终结符，也不能识别为非终结符。是否忘记使用空格隔开？", e);
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
        result.append(beforeAbstractSymbol.toString());
        result.append("】->");
        for (AbstractSymbol afterAbstractSymbolName : afterAbstractSymbols) {
            result.append("【");
            result.append(afterAbstractSymbolName.toString());
            result.append("】");
        }
        return result.toString();
    }

    public AbstractSymbol getBeforeAbstractSymbol() {
        return beforeAbstractSymbol;
    }

    public List<AbstractSymbol> getAfterAbstractSymbols() {
        return afterAbstractSymbols;
    }

    @Override
    public boolean equals(Object obj) {
        CFGProduction argument = (CFGProduction) (obj);
        if (!beforeAbstractSymbol.equals(argument.beforeAbstractSymbol)) {
            return false;
        } else if (afterAbstractSymbols.size() != argument.afterAbstractSymbols.size()) {
            return false;
        }
        for (int i = 0; i < afterAbstractSymbols.size(); ++i) {
            if (afterAbstractSymbols.get(i).getType() != argument.afterAbstractSymbols.get(i).getType() ||
                    !afterAbstractSymbols.get(i).equals(argument.afterAbstractSymbols.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = beforeAbstractSymbol.hashCode();
        for (AbstractSymbol s : afterAbstractSymbols) {
            hash ^= s.hashCode();
        }
        return hash;
    }

}
