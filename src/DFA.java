import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javafx.util.Pair;

public class DFA {
	
	private DFANode root = null;
	
	private String name = null;
	
	private Set<DFANode> finalNodes = null;
	
	private Set<String> bannedLookingForwardStrs = null;
	
	public Set<DFANode> getFinalNodes() {
		return finalNodes;
	}

	public void setFinalNodes(Set<DFANode> finalNodes) {
		this.finalNodes = finalNodes;
	}

	DFA(){
		root = new DFANode();
		root.setFinal(true);
		finalNodes = new HashSet<>();
		finalNodes.add(root);
		bannedLookingForwardStrs = new HashSet<>();
	}
	
	DFA(DFANode root){
		this.root = root;
		finalNodes = new HashSet<>();
		bannedLookingForwardStrs = new HashSet<>();
	}
	
	public DFANode getRoot() {
		return root;
	}
	
	public void draw(File file) {       
        Graphviz gv = new Graphviz();
        gv.addln(gv.start_graph());//SATRT
        gv.addln("edge[fontname=\"DFKai-SB\" fontsize=15 fontcolor=\"black\" color=\"brown\" style=\"filled\"]");

        Set<DFANode> nodes = new HashSet<>();
        root.setLinkedNodes(nodes);
        for (DFANode node: nodes) {
        	if (node.isFinal()) {
        		gv.addln(node.getSerial() + " [color=\"red\"]");
        	}
        	else {
        		gv.addln(node.getSerial() + " [color=\"blue\"]");
        	}
        }
        Set<String> links = new HashSet<>();
        root.setTransformTableByText(links);
        for (String link: links) {
        	gv.addln(link + " " + " [dir=\"forward\"]");
        }
        gv.addln(gv.end_graph());

        System.out.println(gv.getDotSource());

        String type = "png";

        //File out = new File("images/test." + type);   // Linux
        gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), file );
    }
	
	public void simplify() {
		HashMap<Pair<DFANode, DFANode>, DFALinkState> map = new HashMap<>();
		Set<DFANode> nodes = new HashSet<>();
        root.setLinkedNodes(nodes);
        
        for (DFANode node: nodes) {
        	for (DFANode anotherNode: nodes) {
        		if (node != anotherNode) {
        			map.put(new Pair<>(node, anotherNode), new DFALinkState());
        		}
        	}
        }
        Stack<Pair<DFANode, DFANode>> willProceed = new Stack<>();
        
        for (DFANode node: nodes) {
        	for (DFANode anotherNode: nodes) {
        		if (node != anotherNode) {
        			int willState = DFALinkState.STATE_UNDEFINED;
        			if (node.getStateTransformTable().keySet().containsAll(anotherNode.getStateTransformTable().keySet())
        					&& node.getStateTransformTable().size() == anotherNode.getStateTransformTable().size()) {
	        			for (String s: node.getStateTransformTable().keySet()) {
	        				DFANode nodeNext = node.getStateTransformTable().get(s);
	    					DFANode nodeAnotherNode = anotherNode.getStateTransformTable().get(s);
	    					if (nodeNext.isFinal() ^ nodeAnotherNode.isFinal()) {
	    						willState = DFALinkState.STATE_DIFF;
	    						break;
	    					}
	    					else if (nodeNext != nodeAnotherNode) {
	    						map.get(new Pair<>(node, anotherNode)).addSlot(nodeNext, nodeAnotherNode);
	    						map.get(new Pair<>(nodeNext, nodeAnotherNode)).addSignal(node, anotherNode);
	    					}
	        			}
        			}
        			else {
        				willState = DFALinkState.STATE_DIFF;
        			}
        			if (willState == DFALinkState.STATE_DIFF) {
        				map.get(new Pair<>(node, anotherNode)).setState(DFALinkState.STATE_DIFF);
        				willProceed.add(new Pair<>(node, anotherNode));
        			}
        			else if (map.get(new Pair<>(node, anotherNode)).getSlot().size() <= 0) {
        				map.get(new Pair<>(node, anotherNode)).setState(DFALinkState.STATE_SAME);
        				willProceed.add(new Pair<>(node, anotherNode));
        			}
	        	}
        	}
        }
        
        while (!willProceed.empty()) {
        	Pair<DFANode, DFANode> pair = willProceed.pop();
        	if (map.get(pair).getState() == DFALinkState.STATE_DIFF) {
        		for (Pair<DFANode, DFANode> signalPair: map.get(pair).getSignal()) {
        			map.get(signalPair).setState(DFALinkState.STATE_DIFF);
        			willProceed.add(signalPair);
        		}
        	}
        	else if (map.get(pair).getState() == DFALinkState.STATE_SAME) {
        		for (Pair<DFANode, DFANode> signalPair: map.get(pair).getSignal()) {
        			map.get(signalPair).removeSlot(pair);
        			if (map.get(signalPair).getSlot().size() <= 0) {
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
        		if (node != anotherNode && map.get(new Pair<>(node, anotherNode)).getState() != DFALinkState.STATE_DIFF) {
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
        for (Set<DFANode> partMergedNodes: mergedNodes) {
        	for (DFANode node: partMergedNodes) {
        		DFANode finalMapNode = finalNodesMap.get(mergedNodesMap.get(node));
        		if (node.isFinal()) {
        			finalMapNode.setFinal(true);
        			finalNodes.add(finalMapNode);
        		}
        		for (String transformStr: node.getStateTransformTable().keySet()) {
        			finalMapNode.addToTransformTable(transformStr, finalNodesMap.get(mergedNodesMap.get(node.getStateTransformTable().get(transformStr))));
        		}
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

	public static DFA fastDFA(String str) {
		DFANode root = new DFANode();
		DFANode pointer = root;
		for (int i = 0; i < str.length(); ++i) {
			DFANode next = new DFANode();
			String c = String.valueOf(str.charAt(i));
			pointer.addToTransformTable(c, next);
			pointer = next;
		}
		pointer.setFinal(true);
		return new DFA(root);
	}

	public Set<String> getBannedLookingForwardStrs() {
		return bannedLookingForwardStrs;
	}

	public void setBannedLookingForwardStrs(Set<String> bannedLookingForwardStrs) {
		this.bannedLookingForwardStrs = bannedLookingForwardStrs;
	}
	
	public void setAllowedLookingForwardStrs(Set<String> allowedLookingForwardStrs) {
		bannedLookingForwardStrs.clear();
		for (char c = 32; c < 127; ++c) {
			if (!allowedLookingForwardStrs.contains(String.valueOf(c))) {
				bannedLookingForwardStrs.add(String.valueOf(c));
			}
		}
	}
	
	public void addBannedLookingForwardStrs(String str) {
		bannedLookingForwardStrs.add(str);
	}

	public Integer analysis(String substring) {
		DFANode node = getRoot();
		int pointer = 0;
		while (pointer < substring.length() && (
				!node.isFinal() || 
				bannedLookingForwardStrs.contains(String.valueOf(substring.charAt(pointer))))) {
			char c = substring.charAt(pointer);
			if (node.getStateTransformTable().containsKey(String.valueOf(c))) {
				node = node.getStateTransformTable().get(String.valueOf(c));
			}
			else {
				return -1;
			}
			++pointer;
		}
		return pointer;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
