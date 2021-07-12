package parser;

import java.io.Serializable;

public class AnalysisTree implements Serializable {
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
