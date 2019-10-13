package parser;

public class AnalysisTree {
	AnalysisNode root;
	
	AnalysisTree(){
		root = null;
	}
	
	AnalysisNode getRoot() {
		return root;
	}
	
	void setRoot(AnalysisNode root) {
		this.root = root;
	}
	
	@Override
	public String toString() {
		return root.toString(0);
	}
}
