import java.util.Map;

public abstract class SymbolExtra {
	
	public static final int TERMINATOR = 0x01, UNTERMINATOR = 0xff;

	protected Map<String, Object> properties;
	
	private Symbol symbol;

    @Override
    public boolean equals(Object obj) {
    	SymbolExtra argument = (SymbolExtra) (obj);
        return getSymbol().equals(argument.getSymbol()) && getProperties().equals(argument.getProperties());
    }
    
    public abstract int getType();

	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol.toString() + ":" + properties.toString();
	}

	public Symbol getSymbol() {
		return symbol;
	}

    @Override
    public int hashCode() {
        return getSymbol().hashCode() ^ getProperties().hashCode();
    }
    
    public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	public void addProperty(String key, Object value) {
		this.properties.put(key, value);
	}
}
