package translator;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import exception.PLDLParsingWarning;
import lexer.Lexer;
import lexer.NFA;
import lexer.SimpleREApply;
import parser.AnalysisNode;
import parser.AnalysisTree;
import parser.CFG;
import parser.CFGProduction;
import symbol.AbstractSymbol;
import symbol.Symbol;
import symbol.SymbolPool;
import symbol.Terminator;

import java.io.Serializable;
import java.util.*;

public class Translator implements MovementCreator, Serializable {

    private CFG cfg = null;

    private Set<Character> emptyChars = new HashSet<>();

    private Map<CFGProduction, List<AnalysisTree>> movementsMap = new HashMap<>();

    public Map<String, Set<String>> getTempStorages() {
        Map<String, Set<String> > results = new HashMap<>();
        for(String key: tempStorages.keySet()){
            results.put(key, new HashSet<>());
            for (int i = 0; i < tempStorages.get(key); ++i){
                results.get(key).add("t_" + key + String.valueOf(i));
            }
        }
        return results;
    }

    private Map<String, Integer> tempStorages = new HashMap<>();

    private Lexer lexer;

    public Translator() throws PLDLParsingException, PLDLAnalysisException {
        List<Map.Entry<String, NFA>> terminatorsNFA = new ArrayList<>();
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("$$", NFA.fastNFA("$$")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("$", NFA.fastNFA("$")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("(", NFA.fastNFA("(")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>(")", NFA.fastNFA(")")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("=", NFA.fastNFA("=")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("newTemp", NFA.fastNFA("newTemp")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("print", NFA.fastNFA("print")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("go", NFA.fastNFA("go")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("val", new SimpleREApply("[a-zA-Z][a-zA-Z0-9]*").getNFA()));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("num", new SimpleREApply("[1-9][0-9]*|0").getNFA()));

        lexer = new Lexer(terminatorsNFA, null);
        emptyChars.add(' ');
        emptyChars.add('\t');
        emptyChars.add('\n');
        emptyChars.add('\r');
        emptyChars.add('\f');
        setCFG();
    }

    public AnalysisTree getMovementTree(String str) throws PLDLAnalysisException, PLDLParsingException {
        List<Symbol> symbols = lexer.analysis(str, emptyChars);
        return cfg.getTable().getAnalysisTree(symbols);
    }


    protected void setCFG() {
        Set<String> terminatorStrs = new HashSet<>(Arrays.asList("$$", "$", "(", ")", "=", "newTemp", "val", "num", "print", "go", "+"));
        Set<String> unterminatorStrs = new HashSet<>(Arrays.asList("H", "Var", "E", "G"));
        SymbolPool pool = new SymbolPool();
        try {
            pool.initTerminatorString(terminatorStrs);
            pool.initUnterminatorString(unterminatorStrs);
            List<CFGProduction> res = new ArrayList<>(Arrays.asList(
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> print ( H )", pool)) {

                        /* For Debug */
                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) throws PLDLAnalysisException {
                            AnalysisNode rightTreeNode = (AnalysisNode) movementTree.getChildren().get(2).getValue().getProperties().get("rightTreeNode");
                            Symbol rightTreeNodeValue = rightTreeNode.getValue();
                            String name = (String) movementTree.getChildren().get(2).getValue().getProperties().get("name");
                            if (rightTreeNodeValue.getProperties().containsKey(name)) {
                                System.out.println(rightTreeNodeValue.getProperties().get(name));
                            }
                            else {
                                throw new PLDLAnalysisException("节点属性不存在。节点" + rightTreeNodeValue + "不具有属性" + name, null);
                            }
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> go ( $ num )", pool)) {
                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) throws PLDLParsingException, PLDLAnalysisException {
                            int num = Integer.parseInt((String) movementTree.getChildren().get(3).getValue().getProperties().get("val"));
                            --num;
                            if (num < 0 || num >= analysisTree.getChildren().size()) {
                                throw new PLDLParsingException("$后面的数字超出这条产生式右部元素的范围", null);
                            }
                            AnalysisNode rightTreeNode = analysisTree.getChildren().get(num);
                            List<AnalysisTree> trees = movementsMap.get(rightTreeNode.getProduction());
                            if (trees != null) {
                                doTreeMovement(movementsMap.get(rightTreeNode.getProduction()), rightTreeNode);
                            }
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("H -> Var ( val )", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) {
                            String name = (String) movementTree.getChildren().get(2).getValue().getProperties().get("val");
                            AnalysisNode rightTreeNode = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");
                            movementTree.getValue().addProperty("rightTreeNode", rightTreeNode);   //右树节点
                            movementTree.getValue().addProperty("name", name);  //索引名
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("G -> G + H", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) {
                            AnalysisNode G2rightTreeNode = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");   //右树节点
                            String G2name = (String) movementTree.getChildren().get(0).getValue().getProperties().get("name");   //索引名
                            String G2Value = (String) G2rightTreeNode.getValue().getProperties().get(G2name);
                            AnalysisNode HrightTreeNode = (AnalysisNode) movementTree.getChildren().get(2).getValue().getProperties().get("rightTreeNode");   //右树节点
                            String Hname = (String) movementTree.getChildren().get(2).getValue().getProperties().get("name");   //索引名
                            String HValue = (String) HrightTreeNode.getValue().getProperties().get(Hname);

                            AnalysisNode node = new AnalysisNode(new Terminator(null));
                            node.getValue().addProperty("val", G2Value + HValue);
                            movementTree.getValue().addProperty("rightTreeNode", node);   //右树节点
                            movementTree.getValue().addProperty("name", "val");  //索引名
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("G -> H", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) {
                            AnalysisNode HrightTreeNode = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");   //右树节点
                            String Hname = (String) movementTree.getChildren().get(0).getValue().getProperties().get("name");   //索引名
                            movementTree.getValue().addProperty("rightTreeNode", HrightTreeNode);   //右树节点
                            movementTree.getValue().addProperty("name", Hname);  //索引名
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> H = newTemp ( val )", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) {
                            String name = (String) movementTree.getChildren().get(4).getValue().getProperties().get("val");
                            if (tempStorages.containsKey(name)){
                                tempStorages.put(name, tempStorages.get(name) + 1);
                            }
                            else {
                                tempStorages.put(name, 0);
                            }

                            //键值对
                            AnalysisNode HrightTreeNode = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");   //右树节点
                            String H1name = (String) movementTree.getChildren().get(0).getValue().getProperties().get("name");   //索引名
                            HrightTreeNode.getValue().getProperties().put(H1name, "t_" + name + String.valueOf(tempStorages.get(name)));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> $$", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) {
                            //Var这个左树节点中存储的是右树节点
                            movementTree.getValue().addProperty("rightTreeNode", analysisTree);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> $ num", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) throws PLDLParsingException {
                            int num = Integer.parseInt((String) movementTree.getChildren().get(1).getValue().getProperties().get("val"));
                            --num;
                            if (num < 0 || num >= analysisTree.getChildren().size()) {
                                throw new PLDLParsingException("$后面的数字超出这条产生式右部元素的范围", null);
                            }
                            AnalysisNode rightTreeNode = analysisTree.getChildren().get(num);
                            movementTree.getValue().addProperty("rightTreeNode", rightTreeNode);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> H = G", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) throws PLDLAnalysisException {
                            //键值对
                            AnalysisNode H1rightTreeNode = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");   //右树节点
                            String H1name = (String) movementTree.getChildren().get(0).getValue().getProperties().get("name");   //索引名
                            AnalysisNode H2rightTreeNode = (AnalysisNode) movementTree.getChildren().get(2).getValue().getProperties().get("rightTreeNode");
                            String H2name = (String) movementTree.getChildren().get(2).getValue().getProperties().get("name");
                            if (H2rightTreeNode.getValue().getProperties().containsKey(H2name)) {
                                H1rightTreeNode.getValue().getProperties().put(H1name, H2rightTreeNode.getValue().getProperties().get(H2name));
                            }
                            else {
                                throw new PLDLAnalysisException("节点属性不存在。节点" + H2rightTreeNode.getValue() + "不具有属性" + H2name + "," +
                                        " 不能赋值给节点" + H1rightTreeNode.getValue() + "的属性" + H1name, null);
                            }
                        }
                    }
            ));
            cfg = new CFG(pool, res, "E");
        } catch (PLDLParsingException e) {
            e.printStackTrace();
        }

    }

    public void doTreesMovements(AnalysisTree rootTree) throws PLDLParsingException, PLDLAnalysisException {
        List<AnalysisTree> trees = movementsMap.get(rootTree.getRoot().getProduction());
        if (trees != null) {
            doTreeMovement(trees, rootTree.getRoot());
        }
    }

    private void doTreeMovement(List<AnalysisTree> movementTrees, AnalysisNode analysisNode) throws PLDLParsingException, PLDLAnalysisException {
        for (AnalysisTree movementTree : movementTrees) {
            rr_doTreeMovement(movementTree.getRoot(), analysisNode);
        }
    }

    private void rr_doTreeMovement(AnalysisNode movementNode, AnalysisNode analysisNode) throws PLDLParsingException, PLDLAnalysisException {
        if (movementNode.getChildren() != null) {
            for (AnalysisNode childNode : movementNode.getChildren()) {
                if (childNode.getValue().getAbstractSymbol().getType() != AbstractSymbol.TERMINATOR) {
                    rr_doTreeMovement(childNode, analysisNode);
                }
            }
        }
        try {
            MovementProduction movementProduction = (MovementProduction) movementNode.getProduction();
            movementProduction.doMovement(movementNode, analysisNode);
        }
        catch (PLDLAnalysisException e){
            throw new PLDLAnalysisException("在" + analysisNode.getProduction(), e);
        }
    }

    public Map<CFGProduction, List<AnalysisTree>> getMovementsMap() {
        return movementsMap;
    }

    public void checkMovementsMap(){
        for (CFGProduction production: movementsMap.keySet()){
            List<AnalysisTree> movementTrees = movementsMap.get(production);
            Set<Integer> unterminatorIndices = new HashSet<>();
            Set<Integer> trulyWentIndices = new HashSet<>();
            for (int i = 0; i < production.getAfterAbstractSymbols().size(); ++i){
                if (production.getAfterAbstractSymbols().get(i).getType() == AbstractSymbol.UNTERMINATOR){
                    unterminatorIndices.add(i);
                }
            }
            for (AnalysisTree movementTree : movementTrees) {
                try {
                    if (movementTree.getRoot().getProduction().getAfterAbstractSymbols().get(0).equals(cfg.getSymbolPool().getTerminator("go"))) {
                        String numVal = (String) movementTree.getRoot().getChildren().get(3).getValue().getProperties().get("val");
                        trulyWentIndices.add(Integer.valueOf(numVal) - 1);
                    }
                } catch (PLDLParsingException e) {
                    e.printStackTrace();
                }
            }
            unterminatorIndices.removeAll(trulyWentIndices);
            if (unterminatorIndices.size() > 0){
                for (int i: unterminatorIndices){
                    PLDLParsingWarning.setLog("在" + production + "中，非终结符节点" + String.valueOf(i + 1) + "("
                        + production.getAfterAbstractSymbols().get(i) + ")不会被遍历，如果你忘记使用go语句，请考虑使用。" +
                        "否则将无法获得该非终结符的综合属性。");
                }
            }
        }
    }

    public void addToMovementsMap(CFGProduction production, List<AnalysisTree> trees){
        movementsMap.put(production, trees);
    }
}
