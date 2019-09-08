public class PointedCFGProduction {

    private int pointer;

    private final CFGProduction cfgproduction;

    private Terminator outlookTerminator;

    public PointedCFGProduction(CFGProduction cfgproduction, Terminator outlookTerminator) {
        this.cfgproduction = cfgproduction;
        this.pointer = 0;
        this.outlookTerminator = outlookTerminator;
    }

    public Symbol getNextSymbol() {
        return cfgproduction.getAfterSymbols().get(pointer);
    }

    public boolean finished() {
        if (pointer >= cfgproduction.getAfterSymbols().size()) {
            return true;
        } else if (cfgproduction.getAfterSymbols().get(0).getName().equals("null")) {
            return true;
        }
        return false;
    }

    public PointedCFGProduction next() {
        PointedCFGProduction pointedProduction = new PointedCFGProduction(cfgproduction, outlookTerminator);
        pointedProduction.pointer = pointer + 1;
        return pointedProduction;
    }

    @Override
    public boolean equals(Object obj) {
        PointedCFGProduction argument = (PointedCFGProduction) obj;
        return argument.pointer == pointer
                && argument.cfgproduction.equals(cfgproduction)
                && argument.outlookTerminator.equals(outlookTerminator);
    }

    @Override
    public int hashCode() {
        return cfgproduction.hashCode() ^ pointer;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(cfgproduction.getBeforeSymbol().toString());
        result.append(" ->");
        for (int i = 0; i < pointer; ++i) {
            result.append(" ");
            result.append(cfgproduction.getAfterSymbols().get(i).toString());
        }
        result.append(" ·");
        for (int i = pointer; i < cfgproduction.getAfterSymbols().size(); ++i) {
            result.append(" ");
            result.append(cfgproduction.getAfterSymbols().get(i).toString());
        }
        result.append("（展望符：");
        result.append(outlookTerminator);
        result.append("）");
        return result.toString();
    }

    public Terminator getOutlookTerminator() {
        return outlookTerminator;
    }

    public void setOutlookTerminator(Terminator outlookTerminator) {
        this.outlookTerminator = outlookTerminator;
    }

    public int getPointer() {
        return pointer;
    }

    public CFGProduction getProduction() {
        return cfgproduction;
    }
}