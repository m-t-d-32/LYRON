import java.util.HashMap;

public class Statement {
	
	HashMap<Character, Statement> statementTransformTable;
	
	boolean finished = false;
	
	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public void addTransform(Character str, Statement next) {
		statementTransformTable.put(str, next);
	}
	
	public boolean isFinished() {
		return finished;
	}
}
