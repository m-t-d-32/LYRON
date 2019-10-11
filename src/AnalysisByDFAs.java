import java.util.List;
import java.util.Set;

public class AnalysisByDFAs {

	private List<DFA> dfas;

	AnalysisByDFAs(List<DFA> dfas){
		this.dfas = dfas;
	}
	
	void Analysis(String str, Set<Character> emptyChars) {
		int pointer = 0;
		while (pointer < str.length()) {
			if (!emptyChars.contains(str.charAt(pointer))) {
				int subIndex = 0, dfaSerial = -1;
				for (int i = 0; i < dfas.size(); ++i) {
					int analysisEnd = dfas.get(i).analysis(str.substring(pointer));
					if (analysisEnd != -1) {
						subIndex = analysisEnd;
						dfaSerial = i;
						break;
					}
				}
				if (dfaSerial != -1) {
					System.out.println("matched: " + str.substring(pointer, pointer + subIndex) + " by " + dfas.get(dfaSerial).getName());
					pointer += subIndex;
				}
				else {
					System.err.println("error: ");
					++pointer;
				}
			}
			else {
				++pointer;
			}
		}
	}
}
