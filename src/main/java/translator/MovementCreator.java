package translator;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import parser.AnalysisTree;
import parser.CFGProduction;

import java.util.List;

public interface MovementCreator {

    void doTreesMovements(AnalysisTree analysisTree, ResultTuple4 resultCOMM) throws PLDLParsingException;

    AnalysisTree getMovementTree(String str) throws PLDLAnalysisException, PLDLParsingException;
}
