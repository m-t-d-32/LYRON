import java.util.HashSet;

import javafx.util.Pair;

public class DFALinkState {
	
	public static final int STATE_SAME = 1, STATE_DIFF = 0, STATE_UNDEFINED = -1;
	
	HashSet<Pair<DFANode, DFANode>> signal;
	
	public HashSet<Pair<DFANode, DFANode>> getSignal() {
		return signal;
	}

	public HashSet<Pair<DFANode, DFANode>> getSlot() {
		return slot;
	}

	HashSet<Pair<DFANode, DFANode>> slot;
	
	int state;
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	DFALinkState(){
		state = STATE_UNDEFINED;
		signal = new HashSet<>();
		slot = new HashSet<>();
	}
	
	void addSignal(DFANode d1, DFANode d2) {
		signal.add(new Pair<>(d1, d2));
	}
	
	void addSlot(DFANode d1, DFANode d2) {
		slot.add(new Pair<>(d1, d2));
	}

	public void removeSlot(Pair<DFANode, DFANode> pair) {
		slot.remove(pair);
	}

	public void clearSignal() {
		signal.clear();
		
	}
	
}
