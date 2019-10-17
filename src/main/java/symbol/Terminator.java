package symbol;

import java.util.HashMap;

public class Terminator extends Symbol {

	public Terminator(AbstractTerminator t) {
		setAbstractSymbol(t);
		setProperties(new HashMap<>());
	}
	
	@Override
	public int getType() {
		return Symbol.UNTERMINATOR;
	}
}
