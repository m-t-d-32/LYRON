package translator;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import parser.AnalysisTree;

import java.io.Serializable;

public interface MovementCreator extends Serializable {
    AnalysisTree getMovementTree(String trim) throws PLDLAnalysisException, PLDLParsingException;
}
