package lexer;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;

public class DFALinkState implements Serializable {

    public static final int STATE_SAME = 1, STATE_DIFF = 0, STATE_UNDEFINED = -1;

    HashSet<Map.Entry<DFANode, DFANode>> signal;

    public HashSet<Map.Entry<DFANode, DFANode>> getSignal() {
        return signal;
    }

    public HashSet<Map.Entry<DFANode, DFANode>> getSlot() {
        return slot;
    }

    HashSet<Map.Entry<DFANode, DFANode>> slot;

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
        signal.add(new AbstractMap.SimpleEntry<>(d1, d2));
    }

    void addSlot(DFANode d1, DFANode d2) {
        slot.add(new AbstractMap.SimpleEntry<>(d1, d2));
    }

    public void removeSlot(Map.Entry<DFANode, DFANode> pair) {
        slot.remove(pair);
    }

    public void clearSignal() {
        signal.clear();

    }

    @Override
    public String toString() {
        return state == STATE_SAME ? "same" : (state == STATE_DIFF ? "diff" : "undefined");
    }

}
