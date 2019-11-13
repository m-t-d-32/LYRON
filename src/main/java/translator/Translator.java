package translator;

import exception.PLDLParsingException;
import exception.REParsingException;
import lexer.Lexer;
import lexer.NFA;
import lexer.NFANode;
import lexer.REProduction;
import parser.AnalysisNode;
import parser.AnalysisTree;
import parser.CFG;
import parser.CFGProduction;
import symbol.AbstractTerminator;
import symbol.Symbol;
import symbol.SymbolPool;
import symbol.Terminator;

import java.util.*;

public class Translator {

    private CFG cfg = null;

    private Lexer lexer = null;

    Translator() {
    }

    public AnalysisTree getMovementTree(String str) {
        return null;
    }


    protected void setCFG() {
        Set<String> terminatorStrs = new HashSet<>(Arrays.asList("$$", "$", "(", ")", "=", ",", "newTemp", "gen", "val", "num", "op"));
        Set<String> unterminatorStrs = new HashSet<>(Arrays.asList("F", "G", "H", "Var", "E", "L"));
        SymbolPool pool = new SymbolPool();
        try {
            pool.initTerminatorString(terminatorStrs);
            pool.initUnterminatorString(unterminatorStrs);
            List<CFGProduction> res = new ArrayList<>(Arrays.asList(
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("G -> Var", pool)) {

                        @Override
                        public void doMovement(AnalysisTree movementTree, AnalysisTree analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getRoot().getValue().addProperty("val", movementTree.getRoot().getChildren().get(0).getValue().getProperties().get("val"));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("H -> Var ( val )", pool)) {

                        @Override
                        public void doMovement(AnalysisTree movementTree, AnalysisTree analysisTree, ResultTuple4 resultCOMM) {
                            String valVal = (String) movementTree.getRoot().getChildren().get(2).getValue().getProperties().get("val");
                            //以左树节点中的变量为索引，取出左树节点中的右树节点中的值
                            Symbol varSymbol = (Symbol) movementTree.getRoot().getChildren().get(0).getValue().getProperties().get("val");//右树节点
                            String varVal = (String) varSymbol.getProperties().get(valVal);
                            //左树节点中放入右树节点的值(node[name] -> val)
                            movementTree.getRoot().getValue().addProperty("node", varSymbol);   //右树节点
                            movementTree.getRoot().getValue().addProperty("name", valVal);  //索引名
                            movementTree.getRoot().getValue().addProperty("val", varVal);   //值
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> newTemp", pool)) {

                        @Override
                        public void doMovement(AnalysisTree movementTree, AnalysisTree analysisTree, ResultTuple4 resultCOMM) {
                            String tempName = resultCOMM.addTempVar();
                            Symbol leftVal = new Terminator(null);
                            leftVal.addProperty("val", tempName);
                            movementTree.getRoot().getValue().addProperty("val", leftVal);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> $$", pool)) {

                        @Override
                        public void doMovement(AnalysisTree movementTree, AnalysisTree analysisTree, ResultTuple4 resultCOMM) {
                            //Var这个左树节点中存储的是右树节点
                            Symbol leftVal = analysisTree.getRoot().getValue();
                            movementTree.getRoot().getValue().addProperty("val", leftVal);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> $ num", pool)) {

                        @Override
                        public void doMovement(AnalysisTree movementTree, AnalysisTree analysisTree, ResultTuple4 resultCOMM) throws PLDLParsingException {
                            Integer num = Integer.valueOf((String) movementTree.getRoot().getChildren().get(2).getValue().getProperties().get("val"));
                            --num;
                            if (num < 0 || num >= analysisTree.getRoot().getChildren().size()) {
                                throw new PLDLParsingException("$后面的数字超出这条产生式右部元素的范围", null);
                            }
                            Symbol leftVal = analysisTree.getRoot().getChildren().get(num).getValue();
                            movementTree.getRoot().getValue().addProperty("val", leftVal);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("Var -> val", pool)) {

                        @Override
                        public void doMovement(AnalysisTree movementTree, AnalysisTree analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getRoot().getValue().addProperty("val", movementTree.getRoot().getChildren().get(0).getValue().getProperties().get("val"));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> G = G", pool)) {

                        @Override
                        public void doMovement(AnalysisTree movementTree, AnalysisTree analysisTree, ResultTuple4 resultCOMM) {
                            //为了安全，我们复制
                            Symbol afterVarSymbol = (Symbol) movementTree.getRoot().getChildren().get(0).getValue();
                            Symbol newSymbol = new Terminator((AbstractTerminator) afterVarSymbol.getAbstractSymbol());
                            movementTree.getRoot().setValue(newSymbol);
                            for (String str : afterVarSymbol.getProperties().keySet()) {
                                newSymbol.addProperty(str, afterVarSymbol.getProperties().get(str));
                            }
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> H = H", pool)) {

                        @Override
                        public void doMovement(AnalysisTree movementTree, AnalysisTree analysisTree, ResultTuple4 resultCOMM) {
                            //键值对
                            Symbol varSymbol = (Symbol) movementTree.getRoot().getValue().getProperties().get("node");   //右树节点
                            String valVal = (String) movementTree.getRoot().getValue().getProperties().get("name");   //索引名
                            String afterVarVal = (String) movementTree.getRoot().getChildren().get(0).getValue().getProperties().get("val");
                            varSymbol.getProperties().put(valVal, afterVarVal);
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("L -> H", pool)) {

                        @Override
                        public void doMovement(AnalysisTree movementTree, AnalysisTree analysisTree, ResultTuple4 resultCOMM) {
                            movementTree.getRoot().getValue().addProperty("val", movementTree.getRoot().getChildren().get(0).getValue().getProperties().get("val"));
                        }
                    },
                    new MovementProduction(CFGProduction.getCFGProductionFromCFGString("E -> gen ( op , L , L , L )", pool)) {

                        @Override
                        public void doMovement(AnalysisTree movementTree, AnalysisTree analysisTree, ResultTuple4 resultCOMM) {
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
                            String val1 = (String) movementTree.getRoot().getChildren().get(2).getValue().getProperties().get("val");
                            String val2 = (String) movementTree.getRoot().getChildren().get(4).getValue().getProperties().get("val");
                            String val3 = (String) movementTree.getRoot().getChildren().get(6).getValue().getProperties().get("val");
                            String val4 = (String) movementTree.getRoot().getChildren().get(8).getValue().getProperties().get("val");
                            resultCOMM.append(val1, val2, val3, val4);
                        }
                    }
            ));
            cfg = new CFG(pool, res, "E");
        } catch (PLDLParsingException e) {
            e.printStackTrace();
        }

    }


}
