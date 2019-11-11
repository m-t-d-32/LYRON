import lexer.Lexer;
import parser.AnalysisTree;
import parser.CFG;
import parser.TransformTable;
import symbol.Symbol;
import util.Graphics;
import util.PreParse;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        PreParse preparse = new PreParse("c.xml", "Program");
        Lexer lexer = new Lexer(preparse.getTerminatorRegexes(), preparse.getBannedStrs());
        Set<Character> emptyChars = new HashSet<>();
        emptyChars.add(' ');
        emptyChars.add('\t');
        emptyChars.add('\n');
        emptyChars.add('\r');
        emptyChars.add('\f');
        CFG cfg = preparse.getCFG();
        TransformTable table = cfg.getTable();
        System.out.println(table.getTableMap().size());

        FileInputStream in = new FileInputStream("test.c");
        int size = in.available();
        byte[] buffer = new byte[size];
        in.read(buffer);
        in.close();
        String s = new String(buffer, StandardCharsets.UTF_8);
        List<Symbol> symbols = lexer.analysis(s, emptyChars);
        System.out.println(symbols.size());
        symbols = cfg.revertToStdAbstractSymbols(symbols);
        symbols = cfg.eraseComments(symbols);
        System.out.println(symbols);
        AnalysisTree tree = table.getAnalysisTree(symbols);
        System.out.println(tree);
        //Scanner in = new Scanner(System.in);
        //AnalysisTree tree = table.getAnalysisTree(lexer.analysis(in.nextLine(), emptyChars));
        //Translator translator = new Translator(tree);
        //List<String> results = translator.doTranslate();
        //System.out.println(results);
    }
}