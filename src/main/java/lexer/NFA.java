package lexer;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.*;

public class NFA {
	
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
		NFA result = new NFA(root);
		result.getFinalNodes().add(pointer);
		return result;
	}

	public void draw(File file) throws IOException {
		Graphviz.useEngine(new GraphvizV8Engine());
		MutableGraph graph = mutGraph()
				.setDirected(true)
				.graphAttrs()
				.add(Rank.dir(LEFT_TO_RIGHT));
		Set<NFANode> nodes = new LinkedHashSet<>();
		root.setLinkedNodes(nodes);
		for (NFANode state : nodes) {
			MutableNode node = mutNode(state.getSerial());
			if (state.isFinal()) {
				node.add(Shape.DOUBLE_CIRCLE);
			} else {
				node.add(Shape.CIRCLE);
			}
			if (state == root) {
				MutableNode entryNode = mutNode("0")
						.add(Shape.NONE)
						.add(Label.of(""))
						.addLink(to(node).with(Label.of("start")));
				graph.add(entryNode);
			}
			for (String s: state.getStateTransformTable().keySet()) {
				Set<NFANode> nextStates = state.getStateTransformTable().get(s);
				for (NFANode nextState : nextStates) {
					node.addLink(
							to(mutNode(String.valueOf(nextState.getSerial())))
									.with(Label.of(s)));
				}
			}
			graph.add(node);
		}
		Graphviz.fromGraph(graph).render(Format.PNG).toFile(file);
	}
}
