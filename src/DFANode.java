import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DFANode {
	
	private String serialCode = null;
	
	private Map<String, DFANode> stateTransformTable;
	
	public Map<String, DFANode> getStateTransformTable() {
		return stateTransformTable;
	}

	private boolean isFinal = false;
	
	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public DFANode() {
		serialCode = StringGenerator.getNextCode();
		stateTransformTable = new HashMap<>();
	}
	
	public String getSerial() {
		return serialCode;
	}
	
	public void setTransformTableByText(Set<String> result){
		for (String trans: stateTransformTable.keySet()) {
			DFANode end = stateTransformTable.get(trans);
			String willAdd = getSerial() + " -> " + end.getSerial() + "[label=\"" + trans + "\"]";
			if (!result.contains(willAdd)) {
				result.add(willAdd);
				end.setTransformTableByText(result);
			}
		}
	}
	
	public void setLinkedNodes(Set<DFANode> result){
		result.add(this);
		for (String trans: stateTransformTable.keySet()) {
			DFANode end = stateTransformTable.get(trans);
			if (!result.contains(end)) {
				result.add(end);
				end.setLinkedNodes(result);
			}
		}
	}
	
	public void addToTransformTable(String s, DFANode next) {
		stateTransformTable.put(s, next);
	}
	
	@Override
	public String toString() {
		return getSerial();
	}
	
}
