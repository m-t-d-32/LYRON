package generator;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
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
import symbol.Terminal;
import translator.MovementCreator;
import translator.MovementProduction;

import java.io.Serializable;
import java.util.*;

public class Generator implements MovementCreator, Serializable {

    private CFG cfg = null;

    private Set<Character> emptyChars = new HashSet<>();

    private Map<CFGProduction, List<AnalysisTree>> beforeMovementsMap = new HashMap<>();
    private Map<CFGProduction, List<AnalysisTree>> afterMovementsMap = new HashMap<>();

    private Lexer lexer;

    public Generator() throws PLDLParsingException, PLDLAnalysisException {
        List<Map.Entry<String, NFA>> terminalsNFA = new ArrayList<>();
        terminalsNFA.add(new AbstractMap.SimpleEntry<>("$$", NFA.fastNFA("$$")));
        terminalsNFA.add(new AbstractMap.SimpleEntry<>("$", NFA.fastNFA("$")));
        terminalsNFA.add(new AbstractMap.SimpleEntry<>("(", NFA.fastNFA("(")));
        terminalsNFA.add(new AbstractMap.SimpleEntry<>(")", NFA.fastNFA(")")));
        terminalsNFA.add(new AbstractMap.SimpleEntry<>(",", NFA.fastNFA(",")));
        terminalsNFA.add(new AbstractMap.SimpleEntry<>("print", NFA.fastNFA("print")));
        terminalsNFA.add(new AbstractMap.SimpleEntry<>("gen", NFA.fastNFA("gen")));
        terminalsNFA.add(new AbstractMap.SimpleEntry<>("_", NFA.fastNFA("_")));
        terminalsNFA.add(new AbstractMap.SimpleEntry<>("val", new SimpleREApply("[_a-zA-Z][_a-zA-Z0-9]*").getNFA()));
        terminalsNFA.add(new AbstractMap.SimpleEntry<>("num", new SimpleREApply("[1-9][0-9]*|0").getNFA()));

        lexer = new Lexer(terminalsNFA, null);
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
        Set<String> terminalStrs = new HashSet<>(Arrays.asList("$$", "$", "(", ")", ",", "val", "num", "print", "gen"));
        Set<String> nonterminalStrs = new HashSet<>(Arrays.asList("Program", "H", "Var", "E"));
        SymbolPool pool = new SymbolPool();
        try {
            pool.initTerminalString(terminalStrs);
            pool.initNonterminalString(nonterminalStrs);
            List<CFGProduction> res = new ArrayList<>(Arrays.asList(
                    new GenerateProduction(CFGProduction.getCFGProductionFromCFGString("Program -> E", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, List<String> resultCOMM) {
                            movementTree.getValue().setProperties(new HashMap<>());
                            for (String str : movementTree.getChildren().get(0).getValue().getProperties().keySet()) {
                                movementTree.getValue().getProperties().put(str, movementTree.getChildren().get(0).getValue().getProperties().get(str));
                            }
                        }
                    },
                    new GenerateProduction(CFGProduction.getCFGProductionFromCFGString("E -> print ( H )", pool)) {

                        /* For Debug */
                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, List<String> resultCOMM) {
                            AnalysisNode rightTreeNode = (AnalysisNode) movementTree.getChildren().get(2).getValue().getProperties().get("rightTreeNode");
                            Symbol rightTreeNodeValue = rightTreeNode.getValue();
                            String name = (String) movementTree.getChildren().get(2).getValue().getProperties().get("name");
                            System.out.println(rightTreeNodeValue.getProperties().get(name));
                        }
                    },
                    new GenerateProduction(CFGProduction.getCFGProductionFromCFGString("E -> gen ( H , H , H , H )", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, List<String> resultCOMM) {
                            /*
                                0： gen
                                1: (
                                2: L
                                3: ,
                                4: L
                                5: ,
                                6: L
                                7: ,
                                8: L
                                9: )
                             */
                            String val1 = (String) movementTree.getChildren().get(2).getValue().getProperties().get("val");
                            if (val1 == null || val1.equals("null")){
                                val1 = "NULL";
                            }
                            String val2 = (String) movementTree.getChildren().get(4).getValue().getProperties().get("val");
                            if (val2 == null || val2.equals("null")){
                                val2 = "NULL";
                            }
                            String val3 = (String) movementTree.getChildren().get(6).getValue().getProperties().get("val");
                            if (val3 == null || val3.equals("null")){
                                val3 = "NULL";
                            }
                            String val4 = (String) movementTree.getChildren().get(8).getValue().getProperties().get("val");
                            if (val4 == null || val4.equals("null")){
                                val4 = "NULL";
                            }
                            resultCOMM.add(val1 + ", " + val2 + ", " + val3 + ", " + val4);
                        }
                    },
                    new GenerateProduction(CFGProduction.getCFGProductionFromCFGString("H -> Var ( val )", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, List<String> resultCOMM) {
                            String name = (String) movementTree.getChildren().get(2).getValue().getProperties().get("val");
                            AnalysisNode rightTreeNode = (AnalysisNode) movementTree.getChildren().get(0).getValue().getProperties().get("rightTreeNode");
                            movementTree.getValue().getProperties().put("val", rightTreeNode.getValue().getProperties().get(name));
                        }
                    },
                    new GenerateProduction(CFGProduction.getCFGProductionFromCFGString("Var -> $$", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, List<String> resultCOMM) {
                            //Var这个左树节点中存储的是右树节点
                            movementTree.getValue().addProperty("rightTreeNode", analysisTree);
                        }
                    },
                    new GenerateProduction(CFGProduction.getCFGProductionFromCFGString("Var -> $ num", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, List<String> resultCOMM) throws PLDLParsingException {
                            int num = Integer.parseInt((String) movementTree.getChildren().get(1).getValue().getProperties().get("val"));
                            --num;
                            if (num < 0 || num >= analysisTree.getChildren().size()) {
                                throw new PLDLParsingException("$后面的数字超出这条产生式右部元素的范围", null);
                            }
                            AnalysisNode rightTreeNode = analysisTree.getChildren().get(num);
                            movementTree.getValue().addProperty("rightTreeNode", rightTreeNode);
                        }
                    },
                    new GenerateProduction(CFGProduction.getCFGProductionFromCFGString("H -> val", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, List<String> resultCOMM) {
                            movementTree.getValue().addProperty("val", movementTree.getChildren().get(0).getValue().getProperties().get("val"));
                        }
                    },
                    new GenerateProduction(CFGProduction.getCFGProductionFromCFGString("H -> num", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, List<String> resultCOMM) {
                            movementTree.getValue().addProperty("val", movementTree.getChildren().get(0).getValue().getProperties().get("val"));
                        }
                    }
            ));
            cfg = new CFG(pool, res, "Program");
        } catch (PLDLParsingException e) {
            e.printStackTrace();
        }

    }

    public void doTreesMovements(AnalysisTree analysisTree, List<String> resultCOMM) throws PLDLParsingException, PLDLAnalysisException {
        Set<AnalysisNode> unfoldedAnalysisNode = new HashSet<>();
        Stack<AnalysisNode> beforeMovementTreeStack = new Stack<>();
        beforeMovementTreeStack.push(analysisTree.getRoot());
        while (!beforeMovementTreeStack.isEmpty()){
            AnalysisNode beforeAnalysisNode = beforeMovementTreeStack.peek();
            if (!unfoldedAnalysisNode.contains(beforeAnalysisNode)) {
                doTreeMovement(beforeMovementsMap.get(beforeAnalysisNode.getProduction()), beforeAnalysisNode, resultCOMM);
                if (beforeAnalysisNode.getChildren() != null) {
                    for (int i = beforeAnalysisNode.getChildren().size() - 1; i >= 0; --i) {
                        AnalysisNode childNode = beforeAnalysisNode.getChildren().get(i);
                        if (childNode.getValue().getAbstractSymbol().getType() != AbstractSymbol.TERMINAL) {
                            beforeMovementTreeStack.push(childNode);
                        }
                    }
                }
                unfoldedAnalysisNode.add(beforeAnalysisNode);
            }
            else {
                doTreeMovement(afterMovementsMap.get(beforeAnalysisNode.getProduction()), beforeAnalysisNode, resultCOMM);
                beforeMovementTreeStack.pop();
            }
        }
    }

    private void doTreeMovement(List<AnalysisTree> movementTrees, AnalysisNode analysisNode, List<String> resultCOMM) throws PLDLParsingException, PLDLAnalysisException {
        for (AnalysisTree movementTree : movementTrees) {
            rr_doTreeMovement(movementTree.getRoot(), analysisNode, resultCOMM);
        }
    }

    private void rr_doTreeMovement(AnalysisNode movementNode, AnalysisNode analysisNode, List<String> resultCOMM) throws PLDLParsingException, PLDLAnalysisException {
        if (movementNode.getChildren() != null) {
            for (AnalysisNode childNode : movementNode.getChildren()) {
                if (childNode.getValue().getAbstractSymbol().getType() != AbstractSymbol.TERMINAL) {
                    rr_doTreeMovement(childNode, analysisNode, resultCOMM);
                }
            }
        }
        GenerateProduction GenerateProduction = (GenerateProduction) movementNode.getProduction();
        GenerateProduction.doMovement(movementNode, analysisNode, resultCOMM);
    }

    public void addToMovementsMap(CFGProduction production,
                                  List<AnalysisTree> beforeTrees,
                                  List<AnalysisTree> afterTrees) {
        beforeMovementsMap.put(production, beforeTrees);
        afterMovementsMap.put(production, afterTrees);
    }
}
