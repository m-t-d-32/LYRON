package lexer;

import exception.PLDLAnalysisException;
import symbol.AbstractTerminal;
import symbol.Symbol;
import symbol.Terminal;

import java.io.Serializable;
import java.util.*;

public class Lexer implements Serializable {

    private DFA dfa = null;

    public Lexer(List<Map.Entry<String, NFA>> regexes, Map<String, String> bannedStrs) {
        Map<String, NFA> regexesNFAs = new HashMap<>();
        Map<String, Integer> regexesOrders = new HashMap<>();
        Map<String, Set<String>> regexesBannedOrders = new HashMap<>();
        for (int i = 0; i < regexes.size(); ++i){
            regexesOrders.put(regexes.get(i).getKey(), i);
            regexesNFAs.put(regexes.get(i).getKey(), regexes.get(i).getValue());
        }
        if (bannedStrs != null) {
            for (String name : bannedStrs.keySet()) {
                regexesBannedOrders.put(name, new HashSet<>());
                for (char c : bannedStrs.get(name).toCharArray()) {
                    regexesBannedOrders.get(name).add(String.valueOf(c));
                }
            }
        }
        NFA allNFA = NFA.getJoinedNFA(regexesNFAs);
        //allNFA.draw(new File("images/nfa.png"));
        dfa = allNFA.toDFA(regexesOrders, regexesBannedOrders);
        //dfa.draw(new File("images/dfa_raw.png"));
        dfa.simplify();
        //dfa.draw(new File("images/dfa.png"));
    }

    public List<Symbol> analysis(String str, Set<Character> emptyChars) throws PLDLAnalysisException {
        int pointer = 0;
        List<Symbol> result = new ArrayList<>();
        if (str.length() > 0) {
            while (pointer < str.length()) {
                if (!emptyChars.contains(str.charAt(pointer))) {
                    String substring = str.substring(pointer);
                    Map.Entry<String, Integer> analysisResult = dfa.analysis(substring);
                    if (analysisResult != null) {
                        Terminal simpleResult = new Terminal(new AbstractTerminal(analysisResult.getKey()));
                        simpleResult.addProperty("val", str.substring(pointer, pointer + analysisResult.getValue()));
                        result.add(simpleResult);
                        //System.out.println("matched: " + str.substring(pointer, pointer + analysisResult.getValue()) + " by " + analysisResult.getKey());
                        pointer += analysisResult.getValue();
                    } else {
                        throw new PLDLAnalysisException("词法分析错误出现在第  " + getRow(pointer, str) + " 行，第 " + getColumn(pointer, str) + " 列，字符[" + str.charAt(pointer) + "]", null);
                    }
                } else {
                    ++pointer;
                }
            }
        }
        else {
            Map.Entry<String, Integer> analysisResult = dfa.analysis(str);
            if (analysisResult != null) {
                Terminal simpleResult = new Terminal(new AbstractTerminal(analysisResult.getKey()));
                simpleResult.addProperty("name", str.substring(pointer, pointer + analysisResult.getValue()));
                result.add(simpleResult);
                //System.out.println("matched: " + str.substring(pointer, pointer + analysisResult.getValue()) + " by " + analysisResult.getKey());
            }
        }
        return result;
    }

    private int getColumn(int pointer, String str) {
        int result = 0;
        for (int i = pointer; i >= 0; --i) {
            if (str.charAt(i) == '\n') {
                return result;
            }
            else {
                ++result;
            }
        }
        return result;
    }

    private int getRow(int pointer, String str) {
        int result = 0;
        for (int i = 0; i < pointer; ++i) {
            char temp = str.charAt(i);
            if (temp == '\n') {
                ++result;
            }
        }
        return result + 1;
    }
}
