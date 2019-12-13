package parser;

public class AnalysisTree {
    AnalysisNode root;

    public AnalysisTree(){
        root = null;
    }

    public AnalysisNode getRoot() {
        return root;
    }

    public void setRoot(AnalysisNode root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return root.toString(0);
    }
}
