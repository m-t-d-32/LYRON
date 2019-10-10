import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DFA {
	
	private DFANode root = null;
	
	private Set<DFANode> finalNodes = null;
	
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
	}
	
	DFA(DFANode root){
		this.root = root;
		finalNodes = new HashSet<>();
	}
	
	public DFANode getRoot() {
		return root;
	}
	
	public void draw() {       
        Graphviz gv = new Graphviz();
        gv.addln(gv.start_graph());//SATRT
        gv.addln("edge[fontname=\"DFKai-SB\" fontsize=15 fontcolor=\"black\" color=\"brown\" style=\"filled\"]");
        gv.addln("size =\"8,8\";");

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

        File out = new File("images/test." + type);   // Linux
        gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
    }
	
	public void simplify() {
		
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
}
