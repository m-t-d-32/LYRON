import java.util.List;

public class AnalysisNode {
	Symbol value;
	
	public Symbol getValue() {
		return value;
	}

	public void setValue(Symbol value) {
		this.value = value;
	}

	List<AnalysisNode> children;
	AnalysisNode parent;
	
	public AnalysisNode(Symbol value) {
		this.value = value;
		this.parent = null;
		this.children = null;
	}
	
	public boolean getIsLeaf() {
		return children == null;
	}
	
	public void setChildren(List<AnalysisNode> symbols) {
		children = symbols;
	}
	
	public List<AnalysisNode> getChildren(){
		return children;
	}
	
	public AnalysisNode getParent() {
		return parent;
	}

	public String toString(int tabCount) {
		StringBuilder result = new StringBuilder();
		result.append("|");
		for (int i = 0; i < tabCount; ++i) {
			result.append("-");
		}
		result.append(value.getAbstractSymbol().getName() + "\'");
		result.append("\n");
		if (children != null) {
			for (AnalysisNode node: children) {
				result.append(node.toString(tabCount + 1));
			}
		}
		return result.toString();
	}	
}
