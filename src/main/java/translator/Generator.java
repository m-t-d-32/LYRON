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

public class Generator implements MovementCreator {

    private CFG cfg = null;

    private Set<Character> emptyChars = new HashSet<>();

    private Map<CFGProduction, List<AnalysisTree>> beforeMovementsMap = new HashMap<>();
    private Map<CFGProduction, List<AnalysisTree>> afterMovementsMap = new HashMap<>();

    private Lexer lexer = null;

    public Generator() throws PLDLParsingException, PLDLAnalysisException {
        List<Map.Entry<String, NFA>> terminatorsNFA = new ArrayList<>();
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("$$", NFA.fastNFA("$$")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("$", NFA.fastNFA("$")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("(", NFA.fastNFA("(")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>(")", NFA.fastNFA(")")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("=", NFA.fastNFA("=")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>(",", NFA.fastNFA(",")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("newTemp", NFA.fastNFA("newTemp")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("print", NFA.fastNFA("print")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("gen", NFA.fastNFA("gen")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("_", NFA.fastNFA("_")));
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
        Set<String> terminatorStrs = new HashSet<>(Arrays.asList("$$", "$", "(", ")", "=", ",", "newTemp", "val", "num", "_", "print", "gen"));
        Set<String> unterminatorStrs = new HashSet<>(Arrays.asList("F", "G", "H", "Var", "E", "L", "L_"));
        SymbolPool pool = new SymbolPool();
        try {
            pool.initTerminatorString(terminatorStrs);
            pool.initUnterminatorString(unterminatorStrs);
            List<CFGProduction> res = new ArrayList<>(Arrays.asList(
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("G -> Var", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getValue().setProperties(new HashMap<>());
                            for (String str : movementTree.getChildren().get(0).getValue().getProperties().keySet()) {
                                movementTree.getValue().getProperties().put(str, movementTree.getChildren().get(0).getValue().getProperties().get(str));
                            }
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> print ( H )", pool)) {

                        /* For Debug */
                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            AnalysisNode rightTreeNode = (AnalysisNode) movementTree.getChildren().get(2).getValue().getProperties().get("rightTreeNode");
                            Symbol rightTreeNodeValue = rightTreeNode.getValue();
                            String name = (String) movementTree.getChildren().get(2).getValue().getProperties().get("name");
                            System.out.println(rightTreeNodeValue.getProperties().get(name));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> gen ( val , L_ , L_ , L_ )", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            /*
                                0： gen
                                1: (
                                2: op
                                3: ,
                                4: L
                                5: ,
                                6: L
                                7: ,
                                8: L
                                9: )
                             */
                            String val1 = (String) movementTree.getChildren().get(2).getValue().getProperties().get("val");
                            String val2 = (String) movementTree.getChildren().get(4).getValue().getProperties().get("val");
                            String val3 = (String) movementTree.getChildren().get(6).getValue().getProperties().get("val");
                            String val4 = (String) movementTree.getChildren().get(8).getValue().getProperties().get("val");
                            resultCOMM.append(val1, val2, val3, val4);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("H -> Var ( val )", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            String name = (String) movementTree.getChildren().get(2).getValue().getProperties().get("val");
                            AnalysisNode rightTreeNode = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");
                            movementTree.getValue().addProperty("rightTreeNode", rightTreeNode);   //右树节点
                            movementTree.getValue().addProperty("name", name);  //索引名
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> newTemp", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            String tempName = resultCOMM.addTempVar();
                            AnalysisNode virtualrightTreeNode = new AnalysisNode(new Terminator(null));
                            virtualrightTreeNode.getValue().addProperty("val", tempName);
                            movementTree.getValue().addProperty("rightTreeNode", virtualrightTreeNode);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> $$", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            //Var这个左树节点中存储的是右树节点
                            movementTree.getValue().addProperty("rightTreeNode", analysisTree);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> $ num", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) throws PLDLParsingException {
                            int num = Integer.parseInt((String) movementTree.getChildren().get(1).getValue().getProperties().get("val"));
                            --num;
                            if (num < 0 || num >= analysisTree.getChildren().size()) {
                                throw new PLDLParsingException("$后面的数字超出这条产生式右部元素的范围", null);
                            }
                            AnalysisNode rightTreeNode = analysisTree.getChildren().get(num);
                            movementTree.getValue().addProperty("rightTreeNode", rightTreeNode);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> val", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            String varname = (String) movementTree.getChildren().get(0).getValue().getProperties().get("val");
                            AnalysisNode rightTreeNode = null;
                            if (!analysisTree.getValue().getProperties().containsKey("var_" + varname)) {
                                rightTreeNode = new AnalysisNode(new Terminator(null));
                                rightTreeNode.getValue().addProperty("val", "var_" + varname);
                                analysisTree.getValue().getProperties().put("var_" + varname, rightTreeNode);
                            } else {
                                rightTreeNode = (AnalysisNode) analysisTree.getValue().getProperties().get("var_" + varname);
                            }
                            movementTree.getValue().addProperty("rightTreeNode", rightTreeNode);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> G = G", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            AnalysisNode rightNode1 = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");
                            AnalysisNode rightNode2 = (AnalysisNode) movementTree.getChildren().get(2).getValue().getProperties().get("rightTreeNode");
                            rightNode1.getValue().setProperties(new HashMap<>());
                            for (String str : rightNode2.getValue().getProperties().keySet()) {
                                rightNode1.getValue().addProperty(str, rightNode2.getValue().getProperties().get(str));
                            }
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> H = G", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            //键值对
                            AnalysisNode HrightTreeNode = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");   //右树节点
                            String Hname = (String) movementTree.getChildren().get(0).getValue().getProperties().get("name");   //索引名
                            AnalysisNode GrightTreeNode = (AnalysisNode) movementTree.getChildren().get(2).getValue().getProperties().get("rightTreeNode");
                            HrightTreeNode.getValue().getProperties().put(Hname, GrightTreeNode);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> H = H", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            //键值对
                            AnalysisNode H1rightTreeNode = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");   //右树节点
                            String H1name = (String) movementTree.getChildren().get(0).getValue().getProperties().get("name");   //索引名
                            AnalysisNode H2rightTreeNode = (AnalysisNode) movementTree.getChildren().get(2).getValue().getProperties().get("rightTreeNode");
                            String H2name = (String) movementTree.getChildren().get(2).getValue().getProperties().get("name");
                            H1rightTreeNode.getValue().getProperties().put(H1name, H2rightTreeNode.getValue().getProperties().get(H2name));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("L -> H", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getValue().setProperties(new HashMap<>());
                            AnalysisNode HrightTreeNode = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");
                            String Hname = (String) movementTree.getChildren().get(0).getValue().getProperties().get("name");
                            movementTree.getValue().getProperties().put("val", HrightTreeNode.getValue().getProperties().get(Hname));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("L_ -> L", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getValue().addProperty("val", movementTree.getChildren().get(0).getValue().getProperties().get("val"));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("L_ -> _", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getValue().addProperty("val", "_");
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("L_ -> num", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getValue().addProperty("val", movementTree.getChildren().get(0).getValue().getProperties().get("val"));
                        }
                    }
            ));
            cfg = new CFG(pool, res, "E");
        } catch (PLDLParsingException e) {
            e.printStackTrace();
        }

    }

    public void doTreesMovements(AnalysisTree analysisTree, ResultTuple4 resultCOMM) throws PLDLParsingException {
        rr_doTreesMovements(analysisTree.getRoot(), resultCOMM);
    }

    private void rr_doTreesMovements(AnalysisNode analysisNode, ResultTuple4 resultCOMM) throws PLDLParsingException {
        doTreeMovement(beforeMovementsMap.get(analysisNode.getProduction()), analysisNode, resultCOMM);
        if (analysisNode.getChildren() != null) {
            for (AnalysisNode childNode : analysisNode.getChildren()) {
                if (childNode.getValue().getAbstractSymbol().getType() != AbstractSymbol.TERMINATOR) {
                    rr_doTreesMovements(childNode, resultCOMM);
                }
            }
        }
        doTreeMovement(afterMovementsMap.get(analysisNode.getProduction()), analysisNode, resultCOMM);
    }

    private void doTreeMovement(List<AnalysisTree> movementTrees, AnalysisNode analysisNode, ResultTuple4 resultCOMM) throws PLDLParsingException {
        for (AnalysisTree movementTree : movementTrees) {
            rr_doTreeMovement(movementTree.getRoot(), analysisNode, resultCOMM);
        }
    }

    private void rr_doTreeMovement(AnalysisNode movementNode, AnalysisNode analysisNode, ResultTuple4 resultCOMM) throws PLDLParsingException {
        if (movementNode.getChildren() != null) {
            for (AnalysisNode childNode : movementNode.getChildren()) {
                if (childNode.getValue().getAbstractSymbol().getType() != AbstractSymbol.TERMINATOR) {
                    rr_doTreeMovement(childNode, analysisNode, resultCOMM);
                }
            }
        }
        MovementProduction movementProduction = (MovementProduction) movementNode.getProduction();
        movementProduction.doMovement(movementNode, analysisNode, resultCOMM);
    }

    public void addToMovementsMap(CFGProduction production,
                                  List<AnalysisTree> beforeTrees,
                                  List<AnalysisTree> afterTrees) {
        beforeMovementsMap.put(production, beforeTrees);
        afterMovementsMap.put(production, afterTrees);
    }
}
