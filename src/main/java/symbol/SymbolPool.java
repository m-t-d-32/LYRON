package symbol;

import exception.PLDLParsingException;
import exception.PLDLParsingWarning;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SymbolPool implements Serializable {

    private Map<String, AbstractNonterminal> nonterminals = null;
    
    private Map<String, AbstractTerminal> terminals = null;

    public void initNonterminalString(Set<String> nonterminalStrs) throws PLDLParsingException {
        if (nonterminals != null) {
            PLDLParsingWarning.setLog("非终结符集合已经初始化过，重新初始化可能会导致不可预料的问题。");
        }
        if (nonterminalStrs.contains("null")) {
            throw new PLDLParsingException("null是PLDL语言的保留字，用于表示空串，因而不能表示其他非终结符，请更换非终结符的名字。", null);
        }
        nonterminals = new HashMap<>();
        for (String str : nonterminalStrs) {
            nonterminals.put(str, new AbstractNonterminal(str));
        }
    }

    public void initTerminalString(Set<String> terminalStrs) throws PLDLParsingException {
        if (terminals != null) {
            PLDLParsingWarning.setLog("终结符集合已经初始化过，重新初始化可能会导致不可预料的问题。");
        }
        if (terminalStrs.contains("null")){
            throw new PLDLParsingException("null是PLDL语言的保留字，用于表示空串，因而不能表示其他终结符，请更换终结符的名字。", null);
        }
        terminals = new HashMap<>();
        for (String str : terminalStrs) {
            terminals.put(str, new AbstractTerminal(str));
        }
        terminals.put("null", AbstractTerminal.getNullTerminal());
    }

    public Set<String> getTerminalsStr() {
        return terminals.keySet();
    }

    public Collection<AbstractTerminal> getTerminals() {
        return terminals.values();
    }

    public Set<String> getNonterminalsStr() {
        return nonterminals.keySet();
    }

    public Collection<AbstractNonterminal> getNonterminals() {
        return nonterminals.values();
    }

    public void addNonterminalStr(String str) {
        if (!nonterminals.containsKey(str)) {
            nonterminals.put(str, new AbstractNonterminal(str));
        }
    }

    public void addTerminalStr(String str) {
        if (!terminals.containsKey(str)) {
            terminals.put(str, new AbstractTerminal(str));
        }
    }

    public void addCommentStr(String comment) {
        if (!terminals.containsKey(comment)){
            AbstractTerminal terminal = new AbstractTerminal(comment);
            terminal.setIsComment(true);
            terminals.put(comment, terminal);
        }
    }

    public AbstractNonterminal getNonterminal(String name) throws PLDLParsingException {
        if (nonterminals.containsKey(name)) {
            return nonterminals.get(name);
        }
        throw new PLDLParsingException("符号 " + name + " 不能识别为非终结符。", null);
    }

    public AbstractTerminal getTerminal(String name) throws PLDLParsingException {
        if (terminals.containsKey(name)) {
            return terminals.get(name);
        }
        throw new PLDLParsingException("符号 " + name + " 不能识别为终结符。", null);
    }

    public AbstractSymbol getSymbol(String name) throws PLDLParsingException {
        if (nonterminals.containsKey(name)) {
            return getNonterminal(name);
        } else if (terminals.containsKey(name)) {
            return getTerminal(name);
        }
        throw new PLDLParsingException("符号 " + name + " 既不能识别为终结符，也不能识别为非终结符。", null);
    }

}