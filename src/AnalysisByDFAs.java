import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnalysisByDFAs {

	private List<DFA> dfas;

	AnalysisByDFAs(List<DFA> dfas){
		this.dfas = dfas;
	}
	
	List<Symbol> Analysis(String str, Set<Character> emptyChars) throws PLDLAnalysisException {
		int pointer = 0;
		List<Symbol> result = new ArrayList<>();
		while (pointer < str.length()) {
			if (!emptyChars.contains(str.charAt(pointer))) {
				int subIndex = 0, dfaSerial = -1;
				String substring = str.substring(pointer);
				for (int i = 0; i < dfas.size(); ++i) {
					int analysisEnd = dfas.get(i).analysis(substring);
					if (analysisEnd != -1) {
						subIndex = analysisEnd;
						dfaSerial = i;
						break;
					}
				}
				if (dfaSerial != -1) {
					Terminator simpleResult = new Terminator(new AbstractTerminator(dfas.get(dfaSerial).getName()));
					simpleResult.addProperty("name", str.substring(pointer, pointer + subIndex));
					result.add(simpleResult);
					System.out.println("matched: " + str.substring(pointer, pointer + subIndex) + " by " + dfas.get(dfaSerial).getName());
					pointer += subIndex;
				}
				else {
					throw new PLDLAnalysisException("词法分析错误出现在第  " + getRow(pointer, str) + " 行，第 " + getColumn(pointer, str) + " 列", null);
				}
			}
			else {
				++pointer;
			}
		}
		return result;
	}

	private int getColumn(int pointer, String str) {
		int result = 0;
		for (int i = pointer; i >= 0; --i) {
			if (str.charAt(i) == '\n') {
				return result;
			}
			else {
				++result;
			}
		}
		return result;
	}

	private int getRow(int pointer, String str) {
		int result = 0;
		for (int i = 0; i < pointer; ++i) {
			char temp = str.charAt(i);
			if (temp == '\n') {
				++result;
			}
		}
		return result + 1;
	}
}
