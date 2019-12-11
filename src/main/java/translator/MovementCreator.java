package translator;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import parser.AnalysisTree;

public interface MovementCreator {
    AnalysisTree getMovementTree(String trim) throws PLDLAnalysisException, PLDLParsingException;
}
