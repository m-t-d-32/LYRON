import java.util.List;

public class AnalysisByDFAs {

	private List<DFA> dfas;
	
	private List<String> todos;

	AnalysisByDFAs(List<DFA> dfas, List<String> todos) throws Exception{
		this.dfas = dfas;
		this.todos = todos;
		
		if (dfas.size() != todos.size()) {
			throw new Exception("DFA个数与输出个数不匹配", null);
		}
	}
	
	void Analysis(String str) {
		
	}
}
