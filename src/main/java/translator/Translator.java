package translator;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import lexer.Lexer;
import lexer.NFA;
import lexer.SimpleREApply;
import parser.AnalysisNode;
import parser.AnalysisTree;
import parser.CFG;
import parser.CFGProduction;
import symbol.*;

import java.util.*;

public class Translator implements MovementCreator {

    private CFG cfg = null;

    private Set<Character> emptyChars = new HashSet<>();

    private Map<CFGProduction, List<AnalysisTree>> movementsMap = new HashMap<>();

    private Map<String, Integer> tempStorages = new HashMap<>();

    private Lexer lexer = null;

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
        Set<String> terminatorStrs = new HashSet<>(Arrays.asList("$$", "$", "(", ")", "=", "newTemp", "val", "num", "print", "go"));
        Set<String> unterminatorStrs = new HashSet<>(Arrays.asList("H", "Var", "E"));
        SymbolPool pool = new SymbolPool();
        try {
            pool.initTerminatorString(terminatorStrs);
            pool.initUnterminatorString(unterminatorStrs);
            List<CFGProduction> res = new ArrayList<>(Arrays.asList(
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> print ( H )", pool)) {

                        /* For Debug */
                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) {
                            AnalysisNode rightTreeNode = (AnalysisNode) movementTree.getChildren().get(2).getValue().getProperties().get("rightTreeNode");
                            Symbol rightTreeNodeValue = rightTreeNode.getValue();
                            String name = (String) movementTree.getChildren().get(2).getValue().getProperties().get("name");
                            System.out.println(rightTreeNodeValue.getProperties().get(name));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> go ( Var )", pool)) {
                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) throws PLDLParsingException, PLDLAnalysisException {
                            AnalysisNode rightTreeNode = (AnalysisNode) movementTree.getChildren().get(2).getValue().getProperties().get("rightTreeNode");
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
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> H = H", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree) {
                            //键值对
                            AnalysisNode H1rightTreeNode = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");   //右树节点
                            String H1name = (String) movementTree.getChildren().get(0).getValue().getProperties().get("name");   //索引名
                            AnalysisNode H2rightTreeNode = (AnalysisNode) movementTree.getChildren().get(2).getValue().getProperties().get("rightTreeNode");
                            String H2name = (String) movementTree.getChildren().get(2).getValue().getProperties().get("name");
                            H1rightTreeNode.getValue().getProperties().put(H1name, H2rightTreeNode.getValue().getProperties().get(H2name));
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
        MovementProduction movementProduction = (MovementProduction) movementNode.getProduction();
        movementProduction.doMovement(movementNode, analysisNode);
    }

    public void addToMovementsMap(CFGProduction production, List<AnalysisTree> trees){
        movementsMap.put(production, trees);
    }
}
