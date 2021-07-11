package app;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import generator.Generator;
import generator.ResultTuple4;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class ConsoleApplication {

    private PreParse preParse = null;
    private Lexer lexer = null;
    private CFG cfg = null;
    private TransformTable table = null;
    private Set<Character> emptyChars = null;

    public ResultTuple4 getResults() {
        return rt4;
    }

    private ResultTuple4 rt4 = null;

    public void LLBegin(InputStream xmlStream) throws PLDLParsingException, PLDLAnalysisException, DocumentException {
        System.out.println("XML文件解析中...");
        preParse = new PreParse(xmlStream, "Program");
        System.out.println("XML文件解析成功。");

        System.out.println("正在构建词法分析器...");
        lexer = new Lexer(preParse.getTerminatorRegexes(), preParse.getBannedStrs());
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
                (cfg.getCFGUnterminators().size() + cfg.getCFGTerminators().size()) + "项");
        System.out.println("基于LR（1）分析的语法分析器构建成功。");

        System.out.println("特定语言类型的内部编译器架构形成。");
    }

    public void LLParse(InputStream codeStream)
            throws PLDLAnalysisException, PLDLParsingException, IOException {

//        System.out.println("正在读取代码文件...");
        int size = codeStream.available();
        byte[] buffer = new byte[size];
        int readin = codeStream.read(buffer);
        if (readin != size){
            throw new IOException("代码文件读取大小与文件大小不一致。");
        }
        codeStream.close();
        String codestr = new String(buffer, StandardCharsets.UTF_8);

//        System.out.println("正在对代码进行词法分析...");
        List<Symbol> symbols = lexer.analysis(codestr, emptyChars);
        symbols = cfg.revertToStdAbstractSymbols(symbols);
        symbols = cfg.eraseComments(symbols);

//        System.out.println("正在对代码进行语法分析构建分析树...");
        AnalysisTree tree = table.getAnalysisTree(symbols);
        rt4 = new ResultTuple4();

//        System.out.println("正在对分析树进行语义赋值生成注释分析树...");
        Translator translator = preParse.getTranslator();
        translator.checkMovementsMap();
        translator.doTreesMovements(tree);

//        System.out.println("正在根据注释分析树生成四元式...");
        Generator generator = preParse.getGenerator();
        generator.doTreesMovements(tree, rt4);
//        System.out.println("生成四元式成功");
    }

    public void LLEnd(OutputStream outputStream){
        PrintStream backupStream = System.out;
        System.setOut(new PrintStream(outputStream));

        System.out.println(rt4);
        System.setOut(backupStream);
        System.out.println("生成完毕。");
    }

    public void LLMain(String[] args){
        System.out.println("开始：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
        String pldlFileName, codeFileName, outFileName, fourtupleFileName;
        List<String> wrongTestFiles = new ArrayList<>();
        try {
            if (args.length == 3) {
                pldlFileName = args[0];
                codeFileName = args[1];
                outFileName = args[2];
                LLBegin(new FileInputStream(pldlFileName));
                LLParse(new FileInputStream(codeFileName));
                LLEnd(new FileOutputStream(outFileName));
            } else if (args.length == 0) {
                pldlFileName = "sample/LYRON-SysY-Backend/xml/sysy.xml";
                LLBegin(new FileInputStream(pldlFileName));
                System.out.println("初始化完毕：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));

                File []testfolders = {
                        new File("sample/LYRON-SysY-Backend/compiler2021/公开用例与运行时库/function_test2020"),
                        new File("sample/LYRON-SysY-Backend/compiler2021/公开用例与运行时库/function_test2021"),
//                        new File("sample/LYRON-SysY-Backend/compiler2021/公开用例与运行时库/functional_test"),
                        new File("sample/LYRON-SysY-Backend/compiler2021/公开用例与运行时库/performance_test2021_pre")
                };

                for (File folder: testfolders){
                    File []testfiles = folder.listFiles();
                    try{
                        for (File f: testfiles){
                            if (f.getName().endsWith("sy")){
                                codeFileName = f.getAbsolutePath();
                                wrongTestFiles.add(codeFileName);
                                LLParse(new FileInputStream(codeFileName));
                                wrongTestFiles.remove(codeFileName);
                                fourtupleFileName = codeFileName + ".4tu";
                                LLEnd(new FileOutputStream(fourtupleFileName));
                            }
                        }
                    }
                    catch (Exception e){
                        System.out.println(e);
                    }
                }
            } else {
                System.err.println("参数用法：第一个参数是程序语言定义文件，第二个参数是要解析的代码文件，第三个参数是四元式保存位置。");
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
            if (e.getNestedException().getClass().equals(FileNotFoundException.class)){
                System.err.println("文件无法打开或读取，请检查输入的路径。");
            }
            else {
                System.err.println("程序语言定义存在问题，这不是一个正确的XML文件，请检查格式。注意XML中的转义字符。");
            }
            e.printStackTrace();
        }

        if (wrongTestFiles.isEmpty()){
            System.out.println("所有文件执行成功");
        }
        else {
            System.out.println("执行出错的测试文件：");
            for (String wrongFilename : wrongTestFiles) {
                System.out.println(wrongFilename);
            }
        }
        System.out.println("执行完毕：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
    }

    public static void main(String[] args) {
         new ConsoleApplication().LLMain(args);
    }
}