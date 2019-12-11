package app;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import lexer.Lexer;
import org.dom4j.DocumentException;
import parser.AnalysisTree;
import parser.CFG;
import parser.TransformTable;
import symbol.Symbol;
import generator.Generator;
import generator.ResultTuple4;
import translator.Translator;
import util.PreParse;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class ConsoleApplication {

    private static void consoleCalling(String pldlFileName, String codeFilename)
            throws DocumentException, PLDLAnalysisException, PLDLParsingException, IOException {
        System.out.println("XML文件解析中...");
        PreParse preparse = new PreParse(pldlFileName, "Program");
        System.out.println("XML文件解析成功。");

        System.out.println("正在构建词法分析器...");
        Lexer lexer = new Lexer(preparse.getTerminatorRegexes(), preparse.getBannedStrs());
        Set<Character> emptyChars = new HashSet<>();
        emptyChars.add(' ');
        emptyChars.add('\t');
        emptyChars.add('\n');
        emptyChars.add('\r');
        emptyChars.add('\f');
        System.out.println("词法分析器构建成功。");

        System.out.println("正在构建语法分析器...");
        CFG cfg = preparse.getCFG();
        TransformTable table = cfg.getTable();
        System.out.println("基于LR（1）分析的语法分析器构建成功。");

        System.out.println("特定语言类型的内部编译器架构形成。");

        System.out.println("正在读取代码文件...");
        FileInputStream in = new FileInputStream(codeFilename);
        int size = in.available();
        byte[] buffer = new byte[size];
        in.read(buffer);
        in.close();
        String codestr = new String(buffer, StandardCharsets.UTF_8);

        System.out.println("正在对代码进行词法分析...");
        List<Symbol> symbols = lexer.analysis(codestr, emptyChars);
        symbols = cfg.revertToStdAbstractSymbols(symbols);
        symbols = cfg.eraseComments(symbols);

        System.out.println("正在对代码进行语法分析构建分析树...");
        AnalysisTree tree = table.getAnalysisTree(symbols);
        ResultTuple4 rt4 = new ResultTuple4();

        System.out.println("正在对分析树进行语义赋值生成注释分析树...");
        Translator translator = preparse.getTranslator();
        translator.doTreesMovements(tree);

        System.out.println("正在根据注释分析树生成四元式...");
        Generator generator = preparse.getGenerator();
        generator.doTreesMovements(tree, rt4);

        System.out.println("正在进行四元式符号表构建和变量声明检查...");
        //rt4 = generator.transformResultTuples(rt4);

        System.out.println("生成四元式成功，以下打印生成的所有四元式和标号：");
        System.out.println(rt4);
    }

    public static void main(String[] args){
        String pldlFileName = null, codeFileName = null;
        try {
            if (args.length == 2) {
                pldlFileName = args[0];
                codeFileName = args[1];
                consoleCalling(pldlFileName, codeFileName);
            } else if (args.length == 0) {
                System.out.println("请输入程序语言定义文件的路径：");
                Scanner sc = new Scanner(System.in);
                pldlFileName = sc.nextLine();
                System.out.println("请输入要解析的代码文件的路径：");
                codeFileName = sc.nextLine();
                consoleCalling(pldlFileName, codeFileName);
            } else {
                System.err.println("参数用法：第一个参数是程序语言定义文件，第二个参数是要解析的代码文件。");
                System.exit(-1);
            }
        } catch (IOException e) {
            System.err.println("文件无法打开或读取，请检查输入的路径。");
            e.printStackTrace();
        } catch (PLDLParsingException e) {
            System.err.println("程序语言定义存在问题，请检查文法定义。");
            e.printStackTrace();
        } catch (PLDLAnalysisException e) {
            System.err.println("代码文件可能存在问题，请检查代码文件，如果你认为代码没有问题，请检查程序语言定义与代码是否匹配。");
            e.printStackTrace();
        } catch (DocumentException e) {
            System.err.println("程序语言定义存在问题，这不是一个正确的XML文件，请检查格式。注意XML中的转义字符。");
            e.printStackTrace();
        }

    }
}