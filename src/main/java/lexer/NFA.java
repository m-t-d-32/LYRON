package lexer;

import java.io.Serializable;
import java.util.*;

public class NFA implements Serializable {

    private NFANode root = null;

    private Set<NFANode> finalNodes = null;

    public Set<NFANode> getFinalNodes() {
        return finalNodes;
    }

    public void setFinalNodes(Set<NFANode> finalNodes) {
        this.finalNodes = finalNodes;
    }

    NFA(NFANode root){
        this.root = root;
        finalNodes = new HashSet<>();
        if (root.isFinal()) {
            finalNodes.add(root);
        }
    }

    public NFANode getRoot() {
        return root;
    }

    public DFA toDFA(Map<String, Integer> ordersMap, Map<String, Set<String>> bannedStrMap) {
        Set<Set<NFANode> > states = new HashSet<>();
        List<Set<NFANode> > serials = new ArrayList<>(states);
        Map<Set<NFANode>, DFANode> linkTable = new HashMap<>();

        Set<NFANode> initialState = new HashSet<>();
        initialState.add(root);
        setClosure(initialState);
        states.add(initialState);
        serials.add(initialState);
        DFANode root = new DFANode();
        List<String> rootFinalNames = new ArrayList<>();
        for (NFANode node: initialState){
            if (node.isFinal()){
                rootFinalNames.add(node.getFinalName());
            }
        }
        if (rootFinalNames.size() > 0){
            rootFinalNames.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return ordersMap.get(o1) - ordersMap.get(o2);
                }
            });
            root.setFinal(true);
            root.setFinalNames(rootFinalNames);
            /* Lazy Initialize */
            if (bannedStrMap != null) {
                root.setFinalNamesToBannedStrs(bannedStrMap);
            }
        }
        linkTable.put(initialState, root);
        DFA result = new DFA(root);
        for (int i = 0; i < serials.size(); ++i) {
            Set<NFANode> nowState = serials.get(i);
            Map<String, Set<NFANode>> transforms = new HashMap<>();
            for (NFANode node: nowState) {
                Map<String, Set<NFANode>> singleTransform = node.getStateTransformTable();
                for (String str:singleTransform.keySet()) {
                    if (!str.equals("null")) {
                        if (!transforms.containsKey(str)) {
                            transforms.put(str, new HashSet<>());
                        }
                        transforms.get(str).addAll(singleTransform.get(str));
                    }
                }
            }
            for (String str:transforms.keySet()) {
                Set<NFANode> nextState = transforms.get(str);
                setClosure(nextState);
                if (!states.contains(nextState)) {
                    states.add(nextState);
                    serials.add(nextState);
                    DFANode newNode = new DFANode();
                    List<String> finalNames = new ArrayList<>();
                    for (NFANode node: nextState){
                        if (node.isFinal()){
                            finalNames.add(node.getFinalName());
                        }
                    }
                    if (finalNames.size() > 0){
                        finalNames.sort(new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return ordersMap.get(o1) - ordersMap.get(o2);
                            }
                        });
                        newNode.setFinal(true);
                        newNode.setFinalNames(finalNames);
                        /* Lazy Initialize */
                        if (bannedStrMap != null) {
                            newNode.setFinalNamesToBannedStrs(bannedStrMap);
                        }
                    }
                    linkTable.put(nextState, newNode);
                }
                linkTable.get(nowState).addToTransformTable(str, linkTable.get(nextState));
            }
        }
        return result;
    }

    private void setClosure(Set<NFANode> nodes) {
        List<NFANode> serials = new ArrayList<>(nodes);
        for (int i = 0; i < serials.size(); ++i) {
            Set<NFANode> nextsByNull = serials.get(i).getStateTransformTable().get("null");
            if (nextsByNull != null) {
                for (NFANode nextByNull: nextsByNull) {
                    if (!nodes.contains(nextByNull)) {
                        nodes.add(nextByNull);
                        serials.add(nextByNull);
                    }
                }
            }
        }
    }

    public static NFA getJoinedNFA(Map<String, NFA> NFAMap) {
        NFANode newNode = new NFANode();
        NFA result = new NFA(newNode);
        for (String name: NFAMap.keySet()){
            for (NFANode node: NFAMap.get(name).getFinalNodes()){
                node.setFinalName(name);
                result.getFinalNodes().add(node);
            }
            newNode.addToTransformTable("null", NFAMap.get(name).getRoot());
        }
        return result;
    }

    public static NFA fastNFA(String str) {
        NFANode root = new NFANode();
        NFANode pointer = root;
        for (int i = 0; i < str.length(); ++i) {
            NFANode next = new NFANode();
            String c = String.valueOf(str.charAt(i));
            pointer.addToTransformTable(c, next);
            pointer = next;
        }
        pointer.setFinal(true);
        pointer.setFinalName(str);
        NFA result = new NFA(root);
        result.getFinalNodes().add(pointer);
        return result;
    }
}
