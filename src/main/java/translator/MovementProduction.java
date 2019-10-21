package translator;

import parser.CFGProduction;

import java.util.ArrayList;
import java.util.List;

public class MovementProduction extends CFGProduction {

    public List<Tuple5> getMovements() {
        return movements;
    }

    private List<Tuple5> movements;

    public MovementProduction(CFGProduction production, List<String> movementsStr){
        super(production);
        movements = new ArrayList<>();
        for (String s: movementsStr){
            int index = 0;
            for (; index < s.length(); ++index){
                if (s.charAt(index) == '('){
                    break;
                }
            }
            List<String> tuple = new ArrayList<>();
            tuple.add(s.substring(0, index).trim());
            for (int i = 0; i < 3; ++i) {
                int newIndex = s.indexOf(",", index + 1);
                tuple.add(s.substring(index + 1, newIndex).trim());
                index = newIndex;
            }
            int newIndex = s.length() - 1;
            for (; newIndex >= index; --newIndex){
                if (s.charAt(newIndex) == ')'){
                    break;
                }
            }
            tuple.add(s.substring(index + 1, newIndex));
            movements.add(new Tuple5(tuple));
        }
    }
}
