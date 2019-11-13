package translator;

import exception.PLDLParsingException;
import parser.AnalysisTree;
import parser.CFGProduction;

import java.util.ArrayList;
import java.util.List;

public abstract class MovementProduction extends CFGProduction {

    public MovementProduction(CFGProduction production) {
        super(production);
    }

    public abstract void doMovement(AnalysisTree movementTree, AnalysisTree parsingTree, ResultTuple4 results) throws PLDLParsingException;
}
