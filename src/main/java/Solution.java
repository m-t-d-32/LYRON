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
import parser.AnalysisTree;
import parser.CFG;
import translator.Translator;
import util.Graphics;
import util.PreParse;

public class Solution {
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

        PreParse preparse = new PreParse("calculator.xml", "S");
        Lexer lexer = new Lexer(preparse.getTerminatorRegexes(), preparse.getBannedStrs());
        CFG cfg = preparse.getCFG();
        Set<Character> emptyChars = new HashSet<>();
        emptyChars.add(' ');
        emptyChars.add('\t');
        emptyChars.add('\n');
        emptyChars.add('\r');
        emptyChars.add('\f');

        Scanner in = new Scanner(System.in);
        AnalysisTree tree = cfg.getTable().getAnalysisTree(lexer.analysis(in.nextLine(), emptyChars));
        //System.out.println(tree);
        Translator translator = new Translator(tree);
        List<String> results = new ArrayList<>();
        translator.doTranslate(results);
        System.out.println(results);
    }
}