package parser;

import symbol.Symbol;

import java.util.Stack;

public class AnalysingStatement {
    public Stack<Integer> getStatementStack() {
        return statementStack;
    }

    public Stack<AnalysisNode> getNodeStack() {
        return nodeStack;
    }

    public Stack<Symbol> getStreamStack() {
        return streamStack;
    }

    private Stack<Integer> statementStack = new Stack<>();
    private Stack<AnalysisNode> nodeStack = new Stack<>();
    private Stack<Symbol> streamStack = new Stack<>();

    public MovementsList getMovementsList() {
        return movementsList;
    }

    private MovementsList movementsList;

    public int getI() {
        return i;
    }

    private int i;

    public AnalysingStatement(int i, MovementsList movementsList, Stack<Integer> statementStack, Stack<AnalysisNode> nodeStack, Stack<Symbol> streamStack) {
        this.i = i;
        this.statementStack.addAll(statementStack);
        this.nodeStack.addAll(nodeStack);
        this.streamStack.addAll(streamStack);
        this.movementsList = movementsList;
    }


}
