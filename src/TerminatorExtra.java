import java.util.HashMap;

public class TerminatorExtra extends SymbolExtra{

	public TerminatorExtra(Terminator t) {
		setSymbol(t);
		setProperties(new HashMap<>());
	}
	
	@Override
	public int getType() {
		return SymbolExtra.UNTERMINATOR;
	}
}
