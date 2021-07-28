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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConsoleApplication {

    private PreParse preParse = null;
    private Lexer lexer = null;
    private CFG cfg = null;
    private TransformTable table = null;
    private Set<Character> emptyChars = null;

    public List<String> getResults() {
        return rt4;
    }

    private List<String> rt4 = null;

    public void LLBeginFormXML(InputStream xmlStream) throws PLDLParsingException, PLDLAnalysisException, DocumentException, IOException {
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
                (cfg.getCFGUnterminals().size() + cfg.getCFGTerminals().size()) + "项");
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
        rt4 = new ArrayList<>();

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

        for (String s: rt4){
            System.out.println(s);
        }
        System.setOut(backupStream);
        System.out.println("生成完毕。");
    }

    public void LLMain(String[] args){
        System.out.println("开始：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
        String codeFileName, fourtupleFileName;
        List<String> wrongTestFiles = new ArrayList<>();
        try {
            if (args[0].equals("xml")){
                LLBeginFormXML(new FileInputStream(args[1]));
                if (args.length >= 4 &&
                    args[2].equals("save-model")){
                    LLSaveModel(new FileOutputStream(args[3]));
                }
            }
            else if (args[0].equals("model")){
                LLBeginFromModel(new FileInputStream(args[1]));
            }
            else {
                throw new Exception("参数错误");
            }

            System.out.println("初始化完毕：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));

            File []testfolders = {
                    new File("sample/LYRON-SysY-Backend/compiler2021/公开用例与运行时库/function_test2020"),
                    new File("sample/LYRON-SysY-Backend/compiler2021/公开用例与运行时库/function_test2021"),
                    new File("sample/LYRON-SysY-Backend/compiler2021/公开用例与运行时库/functional_test"),
                    new File("sample/LYRON-SysY-Backend/compiler2021/公开用例与运行时库/performance_test2021_pre")
            };

            for (File folder: testfolders){
                File []testfiles = folder.listFiles();
                try{
                    for (File f: testfiles){
                        if (f.getName().endsWith("sy")){
                            codeFileName = f.getAbsolutePath();
                            System.out.println(codeFileName);
                            wrongTestFiles.add(codeFileName);
                            LLParse(new FileInputStream(codeFileName));
                            wrongTestFiles.remove(codeFileName);
                            fourtupleFileName = codeFileName + ".4tu";
                            new File(fourtupleFileName).delete();
                            LLEnd(new FileOutputStream(fourtupleFileName));
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
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