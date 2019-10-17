package symbol;

import java.util.Map;

public abstract class Symbol {
	
	public static final int TERMINATOR = 0x01, UNTERMINATOR = 0xff;

	protected Map<String, Object> properties;
	
	private AbstractSymbol abstractSymbol;

    @Override
    public boolean equals(Object obj) {
    	Symbol argument = (Symbol) (obj);
        return getAbstractSymbol().equals(argument.getAbstractSymbol()) && getProperties().equals(argument.getProperties());
    }
    
    public abstract int getType();

	public void setAbstractSymbol(AbstractSymbol abstractSymbol) {
		this.abstractSymbol = abstractSymbol;
	}

	@Override
	public String toString() {
		return abstractSymbol.toString() + ":" + properties.toString();
	}

	public AbstractSymbol getAbstractSymbol() {
		return abstractSymbol;
	}

    @Override
    public int hashCode() {
        return getAbstractSymbol().hashCode() ^ getProperties().hashCode();
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
