import java.io.File;
import java.util.HashSet;
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
	}
	
	public NFANode getRoot() {
		return root;
	}
	
	public void draw() {       
        Graphviz gv = new Graphviz();
        String nodesty = "[shape = polygon, sides = 6, peripheries = 2, color = lightblue, style = filled]";        
        gv.addln(gv.start_graph());//SATRT
        gv.addln("edge[fontname=\"DFKai-SB\" fontsize=15 fontcolor=\"black\" color=\"brown\" style=\"filled\"]");
        gv.addln("size =\"8,8\";");

        Set<NFANode> nodes = new HashSet<>();
        root.setLinkedNodes(nodes);
        for (NFANode node: nodes) {
        	gv.addln(node.getSerial());
        }
        Set<String> links = new HashSet<>();
        root.setTransformTableByText(links);
        for (String link: links) {
        	gv.addln(link + " " + " [dir=\"forward\"]");
        }
        gv.addln(gv.end_graph());//END

        System.out.println(gv.getDotSource());

        String type = "png";

        File out = new File("images/test." + type);   // Linux
        gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
    }
}
