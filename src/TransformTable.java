import java.util.HashMap;
import java.util.Map;

public class TransformTable {
    final Map<Integer, Map<Symbol, Movement>> table;

    public TransformTable() {
        this.table = new HashMap<>();
    }

    public void add(int statementIndex, Symbol nextSymbol, int nextStatementIndex) {
        Movement movement;
        if (nextSymbol.getType() == Symbol.UNTERMINATOR) {
            movement = new Movement(Movement.GOTO, nextStatementIndex);
        } else {
            movement = new Movement(Movement.SHIFT, nextStatementIndex);
        }
        if (!table.containsKey(statementIndex)) {
            table.put(statementIndex, new HashMap<>());
        }
        table.get(statementIndex).put(nextSymbol, movement);
    }

    public void add(int statementIndex, Symbol nextSymbol, CFGProduction production) {
        Movement movement = new Movement(production);
        if (!table.containsKey(statementIndex)) {
            table.put(statementIndex, new HashMap<>());
        }
        table.get(statementIndex).put(nextSymbol, movement);
    }
}
