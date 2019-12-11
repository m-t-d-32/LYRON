package generator;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import parser.AnalysisNode;
import parser.CFGProduction;

public abstract class GenerateProduction extends CFGProduction{

    public GenerateProduction(CFGProduction production) {
        super(production);
    }

    public abstract void doMovement(AnalysisNode movementRoot, AnalysisNode parsingTreeRoot, ResultTuple4 resultCOMM) throws PLDLParsingException, PLDLAnalysisException;

}
