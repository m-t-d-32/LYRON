import java.util.*;

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

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        Set<Symbol> allSymbols = new HashSet<>();
        for (Integer i : table.keySet()) {
            allSymbols.addAll(table.get(i).keySet());
        }
        List<Integer> sortedStatementIndexes = new ArrayList<>(table.keySet());
        Collections.sort(sortedStatementIndexes);
        List<Symbol> certainAllSymbols = new ArrayList<>(allSymbols);
        //title
        for (Symbol s : certainAllSymbols) {
            result.append("\t");
            result.append(s.getName());
        }
        result.append("\n");
        //contents
        for (Integer i : sortedStatementIndexes) {
            result.append(i);
            for (Symbol s : certainAllSymbols) {
                result.append("\t");
                if (table.get(i).containsKey(s)) {
                    result.append(table.get(i).get(s).toString());
                }
            }
            result.append("\n");
        }
        return result.toString();
    }
}
