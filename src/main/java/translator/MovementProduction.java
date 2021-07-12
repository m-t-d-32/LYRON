package translator;

import exception.PLDLAnalysisException;
import exception.PLDLParsingException;
import parser.AnalysisNode;
import parser.CFGProduction;

import java.io.Serializable;

public abstract class MovementProduction extends CFGProduction implements Serializable {

    public MovementProduction(CFGProduction production) {
        super(production);
    }

    public abstract void doMovement(AnalysisNode movementRoot, AnalysisNode parsingTreeRoot) throws PLDLParsingException, PLDLAnalysisException;
}
