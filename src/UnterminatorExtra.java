import java.util.HashMap;

public class UnterminatorExtra extends SymbolExtra{	
	
	public UnterminatorExtra(Unterminator u) {
		setSymbol(u);
		setProperties(new HashMap<>());
	}
	
	@Override
	public int getType() {
		return SymbolExtra.UNTERMINATOR;
	}

}
