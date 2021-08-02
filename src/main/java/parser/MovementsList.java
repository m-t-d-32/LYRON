package parser;

import java.util.Iterator;
import java.util.Set;

public class MovementsList {
    Set<Movement> movements;
    Iterator<Movement> iterator;

    public MovementsList(Set<Movement> movements){
        this.movements = movements;
        iterator = movements.iterator();
    }

    public Movement nextMovement(){
        if (iterator.hasNext()) {
            return iterator.next();
        }
        else {
            return null;
        }
    }

    public boolean hasNextMovement() {
        return iterator.hasNext();
    }
}
