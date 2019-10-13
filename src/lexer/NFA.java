package lexer;

import org.graph.Graphviz;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NFA {
	
	private NFANode root = null;
	
	private Set<NFANode> finalNodes = null;
	
	public Set<NFANode> getFinalNodes() {
		return finalNodes;
	}

	public void setFinalNodes(Set<NFANode> finalNodes) {
		this.finalNodes = finalNodes;
	}

	NFA(){
		root = new NFANode();
		root.setFinal(true);
		finalNodes = new HashSet<>();
		finalNodes.add(root);
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
	
	public DFA toDFA() {				
		Set<NFANode> initialState = new HashSet<>();		
		initialState.add(root);
		setClosure(initialState);
		Set<Set<NFANode> > states = new HashSet<>();
		List<Set<NFANode> > serials = new ArrayList<>(states);
		states.add(initialState);
		serials.add(initialState);
		Map<Set<NFANode>, DFANode> linkTable = new HashMap<>();
		DFANode root = new DFANode();
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
					newNode.setFinal(judgeFinalState(nextState));
					linkTable.put(nextState, newNode);
				}
				linkTable.get(nowState).addToTransformTable(str, linkTable.get(nextState));
			}
		}
		return result;
	}
	
	private boolean judgeFinalState(Set<NFANode> nextState) {
		for (NFANode node: nextState) {
			if (node.isFinal()) {
				return true;
			}
		}
		return false;
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

	public void draw(File file) {       
        Graphviz gv = new Graphviz();
        gv.addln(gv.start_graph());
        gv.addln("edge[fontname=\"DFKai-SB\" fontsize=15 fontcolor=\"black\" color=\"brown\" style=\"filled\"]");

        Set<NFANode> nodes = new HashSet<>();
        root.setLinkedNodes(nodes);
        for (NFANode node: nodes) {
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
}
