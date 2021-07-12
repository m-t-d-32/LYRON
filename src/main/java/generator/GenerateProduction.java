package generator;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import parser.AnalysisNode;
import parser.CFGProduction;

import java.io.Serializable;

public abstract class GenerateProduction extends CFGProduction implements Serializable {

    public GenerateProduction(CFGProduction production) {
        super(production);
    }

    public abstract void doMovement(AnalysisNode movementRoot, AnalysisNode parsingTreeRoot, ResultTuple4 resultCOMM) throws PLDLParsingException, PLDLAnalysisException;

}
