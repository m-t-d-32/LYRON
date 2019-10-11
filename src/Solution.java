import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentException;

public class Solution {
	
	private static DFA getDFA(String regex, String DFAName, String outLookBan, String outLookAllow) throws PLDLParsingException, PLDLAnalysisException, REParsingException {
		DFA dfa = new SimpleREApply(regex).getNFA().toDFA();
		dfa.simplify();
		if (outLookBan != null) {
			Set<String> bannedStrs = new HashSet<>();
			for (int i = 0; i < outLookBan.length(); ++i) {
				bannedStrs.add(String.valueOf(outLookBan.charAt(i)));
			}
			dfa.setBannedLookingForwardStrs(bannedStrs);
		}
		else if (outLookAllow != null) {
			Set<String> allowedStrs = new HashSet<>();
			for (int i = 0; i < outLookAllow.length(); ++i) {
				allowedStrs.add(String.valueOf(outLookAllow.charAt(i)));
			}
			dfa.setAllowedLookingForwardStrs(allowedStrs);
		}
		dfa.setName(DFAName);
		return dfa;
	}
	private static DFA getFastDFA(String fastDFAStr, String DFAName, String outLookBan, String outLookAllow) {
		DFA dfa = DFA.fastDFA(fastDFAStr);
		if (outLookBan != null) {
			Set<String> bannedStrs = new HashSet<>();
			for (int i = 0; i < outLookBan.length(); ++i) {
				bannedStrs.add(String.valueOf(outLookBan.charAt(i)));
			}
			dfa.setBannedLookingForwardStrs(bannedStrs);
		}
		else if (outLookAllow != null) {
			Set<String> allowedStrs = new HashSet<>();
			for (int i = 0; i < outLookAllow.length(); ++i) {
				allowedStrs.add(String.valueOf(outLookAllow.charAt(i)));
			}
			dfa.setAllowedLookingForwardStrs(allowedStrs);
		}
		dfa.setName(DFAName);
		return dfa;
	}
	
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

    public static void main(String[] args) throws PLDLParsingException, DocumentException, PLDLAnalysisException, REParsingException, IOException {
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
        
        //Graphviz.setFilePath("C:\\Program Files (x86)\\Graphviz 2.28\\bin\\dot.exe", "images");
        List<DFA> cDFAs = new ArrayList<>();
        String _DigitLetter = getDigit() + getLetter() + "_";
        String _Digit = getDigit();
        cDFAs.add(getFastDFA("int", "int", _DigitLetter, null));
        cDFAs.add(getFastDFA("main", "main", _DigitLetter, null));
        cDFAs.add(getFastDFA("if", "if", _DigitLetter, null));
        cDFAs.add(getFastDFA("else", "else", _DigitLetter, null));
        cDFAs.add(getFastDFA("while", "while", _DigitLetter, null));
        cDFAs.add(getFastDFA("for", "for", _DigitLetter, null));
        cDFAs.add(getFastDFA("void", "void", _DigitLetter, null));
        cDFAs.add(getFastDFA(";", "semicolon", null, null));
        cDFAs.add(getFastDFA("(", "left parenthesis", null, null));
        cDFAs.add(getFastDFA(")", "right parenthesis", null, null));
        cDFAs.add(getFastDFA("{", "left brace", null, null));
        cDFAs.add(getFastDFA("}", "right brace", null, null));
        cDFAs.add(getFastDFA("[", "left bracket", null, null));
        cDFAs.add(getFastDFA("]", "right bracket", null, null));
        cDFAs.add(getFastDFA("+=", "plus equals", null, null));
        cDFAs.add(getFastDFA("++", "plus plus", null, null));
        cDFAs.add(getFastDFA("+", "plus", "+=", null));
        cDFAs.add(getFastDFA("-=", "minus equals", null, null));
        cDFAs.add(getFastDFA("--", "minus minus", null, null));
        cDFAs.add(getFastDFA("-", "minus", "-=", null));
        cDFAs.add(getFastDFA("*=", "multiple equals", null, null));
        cDFAs.add(getFastDFA("*", "multiple", "=", null));
        cDFAs.add(getFastDFA("/=", "divide equals", null, null));
        cDFAs.add(getFastDFA("/", "divide", "/*=", null));
        cDFAs.add(getFastDFA("%=", "mod equals", null, null));
        cDFAs.add(getFastDFA("%", "mod", "%=", null));
        cDFAs.add(getFastDFA("^=", "xor equals", null, null));
        cDFAs.add(getFastDFA("^", "xor", "=", null));
        cDFAs.add(getFastDFA("==", "equals", null, null));
        cDFAs.add(getFastDFA("=", "assignment", "=", null));  
        cDFAs.add(getFastDFA("!=", "not equals", null, null));
        cDFAs.add(getFastDFA("!", "deny", "=", null));       
        cDFAs.add(getFastDFA("<<=", "left shift equals", null, null));
        cDFAs.add(getFastDFA("<=", "less or equal", null, null));
        cDFAs.add(getFastDFA("<<", "left shift", "=", null));
        cDFAs.add(getFastDFA("<", "less", "<=", null));
        cDFAs.add(getFastDFA(">>=", "right shift equals", null, null));
        cDFAs.add(getFastDFA(">=", "more or equal", null, null));
        cDFAs.add(getFastDFA(">>", "right shift", "=", null));
        cDFAs.add(getFastDFA(">", "more", "<=", null));
        cDFAs.add(getDFA("[1-9][0-9]*|0", "num", _Digit, null));
        cDFAs.add(getDFA("[_a-zA-Z][_a-zA-Z0-9]*", "var", _DigitLetter, null));
        cDFAs.add(getDFA("\".*\"", "string", null, null));
        cDFAs.add(getDFA("\'.*\'", "char", null, null));
        cDFAs.add(getDFA("/\\*.*\\*/", "comment1", null, null));
        cDFAs.add(getDFA("//.*", "comment2", null, "\r\n"));
        AnalysisByDFAs analysis = new AnalysisByDFAs(cDFAs);
        Set<Character> emptyChars = new HashSet<>();
        emptyChars.add(' ');
        emptyChars.add('\t');
        emptyChars.add('\n');
        emptyChars.add('\r');
        emptyChars.add('\f');
        
        File file = new File("test.txt");  
        Long filelength = file.length();  
        byte[] filecontent = new byte[filelength.intValue()];  
        FileInputStream in = new FileInputStream(file);  
        in.read(filecontent);
        String s = new String(filecontent);
        //System.out.println(s);
        analysis.Analysis(s, emptyChars);
    }
}