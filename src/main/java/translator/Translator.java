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

public class Translator {

    private CFG cfg = null;

    private Set<Character> emptyChars = new HashSet<>();

    private Map<CFGProduction, List<AnalysisTree> > movementsMap = new HashMap<>();

    private Lexer lexer = null;

    public Translator() throws PLDLParsingException, PLDLAnalysisException {
        List<Map.Entry<String, NFA>> terminatorsNFA = new ArrayList<>();
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("$$", NFA.fastNFA("$$")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("$", NFA.fastNFA("$")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("(", NFA.fastNFA("(")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>(")", NFA.fastNFA(")")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("=", NFA.fastNFA("=")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>(",", NFA.fastNFA(",")));
        terminatorsNFA.add(new AbstractMap.SimpleEntry<>("newTemp", NFA.fastNFA("newTemp")));
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
        Set<String> terminatorStrs = new HashSet<>(Arrays.asList("$$", "$", "(", ")", "=", ",", "newTemp", "gen", "val", "num", "_"));
        Set<String> unterminatorStrs = new HashSet<>(Arrays.asList("F", "G", "H", "Var", "E", "L", "L_"));
        SymbolPool pool = new SymbolPool();
        try {
            pool.initTerminatorString(terminatorStrs);
            pool.initUnterminatorString(unterminatorStrs);
            List<CFGProduction> res = new ArrayList<>(Arrays.asList(
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("G -> Var", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getValue().addProperty("val", movementTree.getChildren().get(0).getValue().getProperties().get("val"));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("H -> Var ( val )", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            String valVal = (String) movementTree.getChildren().get(2).getValue().getProperties().get("val");
                            //以左树节点中的变量为索引，取出左树节点中的右树节点中的值
                            Symbol varSymbol = (Symbol) movementTree.getChildren().get(0).getValue().getProperties().get("val");//右树节点
                            String varVal = (String) varSymbol.getProperties().get(valVal);
                            //左树节点中放入右树节点的值(node[name] -> val)
                            movementTree.getValue().addProperty("node", varSymbol);   //右树节点
                            movementTree.getValue().addProperty("name", valVal);  //索引名
                            movementTree.getValue().addProperty("val", varVal);   //值
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> newTemp", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            String tempName = resultCOMM.addTempVar();
                            Symbol leftVal = new Terminator(null);
                            leftVal.addProperty("val", tempName);
                            movementTree.getValue().addProperty("val", leftVal);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> $$", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            //Var这个左树节点中存储的是右树节点
                            Symbol leftVal = analysisTree.getValue();
                            movementTree.getValue().addProperty("val", leftVal);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> $ num", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) throws PLDLParsingException {
                            Integer num = Integer.valueOf((String) movementTree.getChildren().get(1).getValue().getProperties().get("val"));
                            --num;
                            if (num < 0 || num >= analysisTree.getChildren().size()) {
                                throw new PLDLParsingException("$后面的数字超出这条产生式右部元素的范围", null);
                            }
                            Symbol leftVal = analysisTree.getChildren().get(num).getValue();
                            movementTree.getValue().addProperty("val", leftVal);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> val", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getValue().addProperty("val", movementTree.getChildren().get(0).getValue().getProperties().get("val"));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> G = G", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            //为了安全，我们复制
                            Symbol afterVarSymbol = movementTree.getChildren().get(2).getValue();
                            Symbol newSymbol = (Symbol) movementTree.getChildren().get(0).getValue().getProperties().get("val");
                            newSymbol.setProperties(new HashMap<>());
                            for (String str : afterVarSymbol.getProperties().keySet()) {
                                newSymbol.addProperty(str, afterVarSymbol.getProperties().get(str));
                            }
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> H = G", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            //键值对
                            Symbol varSymbol = (Symbol) movementTree.getValue().getProperties().get("node");   //右树节点
                            String valVal = (String) movementTree.getValue().getProperties().get("name");   //索引名
                            Symbol afterVarSymbol = movementTree.getChildren().get(0).getValue();
                            varSymbol.getProperties().put(valVal, afterVarSymbol);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> H = H", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            //键值对
                            Symbol varSymbol = (Symbol) movementTree.getValue().getProperties().get("node");   //右树节点
                            String valVal = (String) movementTree.getValue().getProperties().get("name");   //索引名
                            String afterVarVal = (String) movementTree.getChildren().get(0).getValue().getProperties().get("val");
                            varSymbol.getProperties().put(valVal, afterVarVal);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("L -> H", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getValue().addProperty("val", movementTree.getChildren().get(0).getValue().getProperties().get("val"));
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
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("L_ -> L", pool)) {

                        @Override
                        public void doMovement(AnalysisNode movementTree, AnalysisNode analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getValue().addProperty("val", movementTree.getChildren().get(0).getValue().getProperties().get("val"));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("L_ -> _", pool)) {

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

    public void doMovements(AnalysisTree analysisTree, ResultTuple4 resultCOMM) throws PLDLParsingException {
        rr_doMovements(analysisTree.getRoot(), resultCOMM);
    }

    private void rr_doMovements(AnalysisNode analysisNode, ResultTuple4 resultCOMM) throws PLDLParsingException {
        if (analysisNode.getChildren() != null){
            for (AnalysisNode childNode: analysisNode.getChildren()){
                if (childNode.getValue().getAbstractSymbol().getType() != AbstractSymbol.TERMINATOR) {
                    rr_doMovements(childNode, resultCOMM);
                }
            }
        }
        doMovement(movementsMap.get(analysisNode.getProduction()), analysisNode, resultCOMM);
    }

    private void doMovement(List<AnalysisTree> movementTrees, AnalysisNode analysisNode, ResultTuple4 resultCOMM) throws PLDLParsingException {
        for (AnalysisTree movementTree: movementTrees) {
            rr_doMovement(movementTree.getRoot(), analysisNode, resultCOMM);
        }
    }

    private void rr_doMovement(AnalysisNode movementNode, AnalysisNode analysisNode, ResultTuple4 resultCOMM) throws PLDLParsingException {
        if (movementNode.getChildren() != null){
            for (AnalysisNode childNode: movementNode.getChildren()){
                if (childNode.getValue().getAbstractSymbol().getType() != AbstractSymbol.TERMINATOR){
                    rr_doMovement(childNode, analysisNode, resultCOMM);
                }
            }
        }
        MovementProduction movementProduction = (MovementProduction) movementNode.getProduction();
        movementProduction.doMovement(movementNode, analysisNode, resultCOMM);
    }

    public void addToMovementsMap(CFGProduction production, List<AnalysisTree> trees){
        movementsMap.put(production, trees);
    }
}
