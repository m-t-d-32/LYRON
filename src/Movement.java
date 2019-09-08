public class Movement {
    public static final int SHIFT = 0x01, GOTO = 0xff, REGRESSION = 0xbe;
    private int movement;
    private int shiftToStatement;
    private CFGProduction regressionProduction;

    public Movement(int movement, int shiftToStatement) {
        this.movement = movement;
        this.shiftToStatement = shiftToStatement;
        this.regressionProduction = null;
    }

    public Movement(CFGProduction production) {
        this.movement = REGRESSION;
        this.shiftToStatement = -1;
        this.regressionProduction = production;
    }

    @Override
    public String toString() {
        if (movement == REGRESSION) {
            return "r[" + regressionProduction.toString() + "]";
        } else {
            return "s[" + String.valueOf(shiftToStatement) + "]";
        }
    }
}
