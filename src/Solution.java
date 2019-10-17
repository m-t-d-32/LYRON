import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import exception.REParsingException;
import lexer.Lexer;
import lexer.DFA;
import lexer.NFA;
import lexer.SimpleREApply;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import util.Graphics;

public class Solution {
	
	private static String getDigit() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < 9; ++i) {
			s.append(String.valueOf(i));
		}
		return s.toString();
	}
	private static String getLetter() {
		StringBuilder s = new StringBuilder();
		for (char c = 'a'; c < 'z'; ++c) {
			s.append(String.valueOf(c));
		}
		for (char c = 'A'; c < 'Z'; ++c) {
			s.append(String.valueOf(c));
		}
		return s.toString();
	}

	private static List<Map.Entry<String, NFA>> getNFAFromFile(String filename) throws DocumentException, PLDLParsingException, PLDLAnalysisException {
        List<Map.Entry<String, NFA>> result = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filename));
        Element root = document.getRootElement();
        if (root.getName().equals("re")) {
            String []regexes = root.element("regex").getText().trim().split("\n");
            for (String e: regexes){
                String []splits = e.split(":");
                String s = splits[0].trim();
                NFA nfa = new SimpleREApply(splits[1].trim()).getNFA();
                nfa.draw(new File("images/nfa_" + s + ".png"));
                result.add(new AbstractMap.SimpleEntry<>(s, nfa));
            }
            String []fastRegexes = root.element("faststring").getText().trim().split("\n");
            for (String e: fastRegexes){
                String []splits = e.split(":");
                String s = splits[0].trim();
                NFA nfa = NFA.fastNFA(splits[1].trim());
                nfa.draw(new File("images/nfa_" + s + ".png"));
                result.add(new AbstractMap.SimpleEntry<>(s, nfa));
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        new Thread() {
            @Override
            public void run() {
                try {
                    Graphics.main();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        String _DigitLetter = getDigit() + getLetter() + "_";
        List<Map.Entry<String, NFA>> regexes = getNFAFromFile("test.re");

        Lexer a = new Lexer(regexes, null);
        Set<Character> emptyChars = new HashSet<>();
        emptyChars.add(' ');
        emptyChars.add('\t');
        emptyChars.add('\n');
        emptyChars.add('\r');
        emptyChars.add('\f');
        a.analysis("", emptyChars);
        while (true) {
            a.analysis(new Scanner(System.in).next(), emptyChars);
        }
    }
}