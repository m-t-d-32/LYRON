package app;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import generator.Generator;
import lexer.Lexer;
import org.dom4j.DocumentException;
import parser.AnalysisTree;
import parser.CFG;
import parser.TransformTable;
import symbol.Symbol;
import translator.Translator;
import util.PreParse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConsoleApplication {

    private PreParse preParse = null;
    private Lexer lexer = null;
    private CFG cfg = null;
    private TransformTable table = null;
    private Set<Character> emptyChars = null;

    private List<String> rt4 = null;

    public void LLBeginFormXML(InputStream xmlStream) throws PLDLParsingException, PLDLAnalysisException, DocumentException {
        System.out.println("XML文件解析中...");
        preParse = new PreParse(xmlStream, "Program");
        System.out.println("XML文件解析成功。");

        System.out.println("正在构建词法分析器...");
        lexer = new Lexer(preParse.getTerminalRegexes(), preParse.getBannedStrs());
        emptyChars = new HashSet<>();
        emptyChars.add(' ');
        emptyChars.add('\t');
        emptyChars.add('\n');
        emptyChars.add('\r');
        emptyChars.add('\f');
        System.out.println("词法分析器构建成功。");

        System.out.println("正在构建语法分析器...");
        cfg = preParse.getCFG();
        table = cfg.getTable();
        System.out.println("表项共" + table.getTableMap().size() + "*" +
                (cfg.getCFGNonterminals().size() + cfg.getCFGTerminals().size()) + "项");
        System.out.println("基于LR（1）分析的语法分析器构建成功。");

        System.out.println("特定语言类型的内部编译器架构形成。");
    }

    public void LLBeginFromModel(InputStream modelStream) throws Exception {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        FSTObjectInput in = conf.getObjectInput(modelStream);
        preParse = (PreParse) in.readObject(PreParse.class);
        lexer = (Lexer) in.readObject(Lexer.class);
        cfg = (CFG) in.readObject(CFG.class);
        table = (TransformTable) in.readObject(TransformTable.class);

        emptyChars = new HashSet<>();
        emptyChars.add(' ');
        emptyChars.add('\t');
        emptyChars.add('\n');
        emptyChars.add('\r');
        emptyChars.add('\f');
    }

    public void LLParse(InputStream codeStream)
            throws PLDLAnalysisException, PLDLParsingException, IOException {

        System.out.println("正在读取代码文件...");
        int size = codeStream.available();
        byte[] buffer = new byte[size];
        int readin = codeStream.read(buffer);
        if (readin != size){
            throw new IOException("代码文件读取大小与文件大小不一致。");
        }
        codeStream.close();
        String codestr = new String(buffer, StandardCharsets.UTF_8);

        System.out.println("正在对代码进行词法分析...");
        List<Symbol> symbols = lexer.analysis(codestr, emptyChars);
        symbols = cfg.revertToStdAbstractSymbols(symbols);
        symbols = cfg.eraseComments(symbols);

        System.out.println("正在对代码进行语法分析构建分析树...");
        AnalysisTree tree = table.getAnalysisTree(symbols);
        rt4 = new ArrayList<>();

        System.out.println("正在对分析树进行语义赋值生成注释分析树...");
        Translator translator = preParse.getTranslator();
        translator.checkMovementsMap();
        translator.doTreesMovements(tree);

        System.out.println("正在根据注释分析树生成四元式...");
        Generator generator = preParse.getGenerator();
        generator.doTreesMovements(tree, rt4);
        System.out.println("生成四元式成功");
    }

    private void printSymbols(List<Symbol> symbols) {
        for (int i = 0; i < symbols.size(); ++i){
            System.out.println("第" + (i + 1) + "个符号是：" + symbols.get(i));
        }
    }

    public void LLEnd(OutputStream outputStream){
        PrintStream backupStream = System.out;
        System.setOut(new PrintStream(outputStream));

        for (String s: rt4){
            System.out.println(s);
        }
        System.setOut(backupStream);
        System.out.println("生成完毕。");
    }

    public void LLMain(String[] args){
        System.out.println("欢迎使用LYRON！");
        System.out.println("请选择程序执行方式");
        System.out.println("1.输入xml文件，生成程序定义模型文件（model）");
        System.out.println("2.输入model文件和对应的代码文件，生成四元式文件（4tu）");

        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        int i;

        try {
           i = Integer.parseInt(input);
        }
        catch (NumberFormatException e){
            i = 0;
        }

        if (i == 1){
            System.out.println("请输入xml文件路径");
            String xmlinputfilename = sc.nextLine();
            System.out.println("请输入模型文件保存路径");
            String modeloutputfilename = sc.nextLine();
            try {
                InputStream xmlinput = new FileInputStream(xmlinputfilename);
                LLBeginFormXML(xmlinput);
                OutputStream modeloutput = new FileOutputStream(modeloutputfilename);
                LLSaveModel(modeloutput);
            } catch (FileNotFoundException e) {
                System.out.println("打开XML文件时发生了以下错误：");
                e.printStackTrace();
                System.exit(-1);
            } catch (DocumentException | PLDLAnalysisException | PLDLParsingException e) {
                System.out.println("解析XML文件时发生了以下错误：");
                e.printStackTrace();
                System.exit(-2);
            } catch (IOException e) {
                System.out.println("写入模型文件时发生了以下错误：");
                e.printStackTrace();
                System.exit(-3);
            }
        }
        else if (i == 2){
            System.out.println("请输入模型文件路径");
            String modelinputfilename = sc.nextLine();
            System.out.println("请输入代码文件路径");
            String codeinputfilename = sc.nextLine();
            System.out.println("请输入四元式保存路径");
            String fourtuplefilename = sc.nextLine();
            InputStream modelinput = null;
            InputStream codeinput = null;
            OutputStream fourtupleoutput = null;
            try {
                modelinput = new FileInputStream(modelinputfilename);
            } catch (FileNotFoundException e) {
                System.out.println("打开模型文件时发生了以下错误：");
                e.printStackTrace();
                System.exit(-4);
            }
            try {
                codeinput = new FileInputStream(codeinputfilename);
            } catch (FileNotFoundException e) {
                System.out.println("打开代码文件时发生了以下错误：");
                e.printStackTrace();
                System.exit(-5);
            }
            try {
                LLBeginFromModel(modelinput);
            } catch (Exception e) {
                System.out.println("读取模型文件时发生了以下错误：");
                e.printStackTrace();
                System.exit(-6);
            }
            try {
                LLParse(codeinput);
            } catch (PLDLAnalysisException | PLDLParsingException | IOException e) {
                System.out.println("解析代码文件时发生了以下错误：");
                e.printStackTrace();
                System.exit(-7);
            }
            try {
                fourtupleoutput = new FileOutputStream(fourtuplefilename);
                LLEnd(fourtupleoutput);
            } catch (FileNotFoundException e) {
                System.out.println("写入四元式文件时发生了以下错误：");
                e.printStackTrace();
                System.exit(-8);
            }
        }
        else {
            System.out.println("输入非法，程序将退出.");
            System.exit(-1);
        }
    }

    public void LLSaveModel(OutputStream fileOutputStream) throws IOException {
        System.out.println("保存模型中……");
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        FSTObjectOutput out = conf.getObjectOutput(fileOutputStream);
        out.writeObject(preParse, PreParse.class);
        out.writeObject(lexer, Lexer.class);
        out.writeObject(cfg, CFG.class);
        out.writeObject(table, TransformTable.class);
        out.flush();
        fileOutputStream.close();
        System.out.println("保存模型成功");
    }

    public static void main(String[] args) {
         new ConsoleApplication().LLMain(args);
    }
}