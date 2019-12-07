package translator;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import parser.AnalysisTree;

public interface MovementCreator {

    void doTreesMovements(AnalysisTree analysisTree, ResultTuple4 resultCOMM) throws PLDLParsingException, PLDLAnalysisException;

    AnalysisTree getMovementTree(String str) throws PLDLAnalysisException, PLDLParsingException;
}
