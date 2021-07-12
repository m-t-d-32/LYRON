package lexer;

import java.io.Serializable;
import java.util.*;

public class DFA implements Serializable {

    private DFANode root = null;

    private String name = null;

    private Set<DFANode> finalNodes = null;

    public Set<DFANode> getFinalNodes() {
        return finalNodes;
    }

    DFA(DFANode root){
        this.root = root;
        finalNodes = new HashSet<>();
        if (root.isFinal()) {
            finalNodes.add(root);
        }
    }

    public DFANode getRoot() {
        return root;
    }

    public void simplify() {
        HashMap<Map.Entry<DFANode, DFANode>, DFALinkState> map = new HashMap<>();
        Set<DFANode> nodes = new HashSet<>();
        root.setLinkedNodes(nodes);
        
        for (DFANode node: nodes) {
            for (DFANode anotherNode: nodes) {
                DFALinkState dfaLinkState = new DFALinkState();
                if (node == anotherNode){
                    dfaLinkState.setState(DFALinkState.STATE_SAME);
                }
                else {
                    dfaLinkState.setState(DFALinkState.STATE_UNDEFINED);
                }
                map.put(new AbstractMap.SimpleEntry<>(node, anotherNode), dfaLinkState);
            }
        }
        Stack<Map.Entry<DFANode, DFANode>> willProceed = new Stack<>();
        
        for (DFANode node: nodes) {
            for (DFANode anotherNode: nodes) {
                if (node != anotherNode) {
                    int willState = DFALinkState.STATE_UNDEFINED;
                    if (!node.getStateTransformTable().keySet().containsAll(anotherNode.getStateTransformTable().keySet())
                            || !(node.getStateTransformTable().size() == anotherNode.getStateTransformTable().size())
                            || node.isFinal() != anotherNode.isFinal()
                            || node.isFinal() && anotherNode.isFinal() && !node.getFinalNames().equals(anotherNode.getFinalNames())
                            || node.isFinal() && anotherNode.isFinal() && !node.getFinalNamesToBannedStrs().equals(anotherNode.getFinalNamesToBannedStrs())) {
                        willState = DFALinkState.STATE_DIFF;
                    }
                    else {
                        for (String s: node.getStateTransformTable().keySet()) {
                            DFANode nodeNext = node.getStateTransformTable().get(s);
                            DFANode nodeAnotherNode = anotherNode.getStateTransformTable().get(s);
                            map.get(new AbstractMap.SimpleEntry<>(node, anotherNode)).addSlot(nodeNext, nodeAnotherNode);
                            map.get(new AbstractMap.SimpleEntry<>(nodeNext, nodeAnotherNode)).addSignal(node, anotherNode);
                        }
                    }
                    if (willState == DFALinkState.STATE_DIFF) {
                        map.get(new AbstractMap.SimpleEntry<>(node, anotherNode)).setState(DFALinkState.STATE_DIFF);
                        willProceed.add(new AbstractMap.SimpleEntry<>(node, anotherNode));
                    }
                    else if (map.get(new AbstractMap.SimpleEntry<>(node, anotherNode)).getSlot().size() <= 0) {
                        map.get(new AbstractMap.SimpleEntry<>(node, anotherNode)).setState(DFALinkState.STATE_SAME);
                        willProceed.add(new AbstractMap.SimpleEntry<>(node, anotherNode));
                    }
                }
            }
        }
        
        while (!willProceed.empty()) {
            Map.Entry<DFANode, DFANode> pair = willProceed.pop();
            if (map.get(pair).getState() == DFALinkState.STATE_DIFF) {
                for (Map.Entry<DFANode, DFANode> signalPair: map.get(pair).getSignal()) {
                    if (map.get(signalPair).getState() == DFALinkState.STATE_UNDEFINED) {
                        map.get(signalPair).setState(DFALinkState.STATE_DIFF);
                        willProceed.add(signalPair);
                    }
                }
            }
            else if (map.get(pair).getState() == DFALinkState.STATE_SAME) {
                for (Map.Entry<DFANode, DFANode> signalPair: map.get(pair).getSignal()) {
                    map.get(signalPair).removeSlot(pair);
                    if (map.get(signalPair).getState() == DFALinkState.STATE_UNDEFINED &&
                            map.get(signalPair).getSlot().size() <= 0) {
                        map.get(signalPair).setState(DFALinkState.STATE_SAME);
                        willProceed.add(signalPair);
                    }
                }
                map.get(pair).clearSignal();
            }
        }
        
        Map<DFANode, Set<DFANode>> linkedNodes = new HashMap<>();
        for (DFANode node: nodes) {
            linkedNodes.put(node, new HashSet<>());
        }
        for (DFANode node: nodes) {
            for (DFANode anotherNode: nodes) {
                if (node != anotherNode && map.get(new AbstractMap.SimpleEntry<>(node, anotherNode)).getState() != DFALinkState.STATE_DIFF) {
                    linkedNodes.get(node).add(anotherNode);
                }
            }
        }

        Set<Set<DFANode> > mergedNodes = new HashSet<>();
        Map<DFANode, Set<DFANode>> mergedNodesMap = new HashMap<>();
        while (!nodes.isEmpty()) {
            DFANode node = nodes.iterator().next();
            Set<DFANode> partMergedNodes = new HashSet<>();
            partMergedNodes.add(node);
            dfsMakeMerged(node, linkedNodes, partMergedNodes);
            for (DFANode mergedNode: partMergedNodes) {
                mergedNodesMap.put(mergedNode, partMergedNodes);
            }
            mergedNodes.add(partMergedNodes);
            nodes.removeAll(partMergedNodes);
        }
        Map<Set<DFANode>, DFANode> finalNodesMap = new HashMap<>();
        for (Set<DFANode> partMergedNodes: mergedNodes) {
            finalNodesMap.put(partMergedNodes, new DFANode());
        }
        finalNodes.clear();
        for (DFANode node: linkedNodes.keySet()) {
            DFANode finalMapNode = finalNodesMap.get(mergedNodesMap.get(node));
            if (node.isFinal()) {
                finalMapNode.setFinal(true);
                finalMapNode.setFinalNames(node.getFinalNames());
                finalMapNode.setFinalNamesToBannedStrs(node.getFinalNamesToBannedStrs());
                finalNodes.add(finalMapNode);
            }
            for (String transformStr: node.getStateTransformTable().keySet()) {
                finalMapNode.addToTransformTable(transformStr, finalNodesMap.get(mergedNodesMap.get(node.getStateTransformTable().get(transformStr))));
            }
        }
        
        root = finalNodesMap.get(mergedNodesMap.get(root));
    }

    private void dfsMakeMerged(DFANode node, Map<DFANode, Set<DFANode>> linkedNodes, Set<DFANode> partMergedNodes) {
        Set<DFANode> anotherNodes = linkedNodes.get(node);
        for (DFANode anotherNode: anotherNodes) {
            if (!partMergedNodes.contains(anotherNode)) {
                partMergedNodes.add(anotherNode);
                dfsMakeMerged(anotherNode, linkedNodes, partMergedNodes);
            }
        }
    }

    public Map.Entry<String, Integer> analysis(String substring) {
        DFANode node = getRoot();
        Stack<Map.Entry<DFANode, Integer>> finalNodes = new Stack<>();
        int pointer = 0;
        while (pointer < substring.length()) {
            String next = String.valueOf(substring.charAt(pointer));
            if (node.isFinal()){
                finalNodes.push(new AbstractMap.SimpleEntry<>(node, pointer));
            }

            if (node.getStateTransformTable().containsKey(next)) {
                node = node.getStateTransformTable().get(next);
            }
            else {
                break;
            }
            ++pointer;
        }
        if (node.isFinal()){
            finalNodes.push(new AbstractMap.SimpleEntry<>(node, pointer));
        }
        while (!finalNodes.empty()){
            Map.Entry<DFANode, Integer> analyzeFinal = finalNodes.pop();
            for (String str: analyzeFinal.getKey().getFinalNames()){
                if (!analyzeFinal.getKey().getFinalNamesToBannedStrs().containsKey(str) ||
                    !analyzeFinal.getKey().getFinalNamesToBannedStrs().get(str).contains(String.valueOf(substring.charAt(analyzeFinal.getValue())))){
                    return new AbstractMap.SimpleEntry<>(str, analyzeFinal.getValue());
                }
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
