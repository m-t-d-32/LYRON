import org.dom4j.DocumentException;

public class Solution {

    public static void main(String[] args) throws PLDLParsingException, DocumentException, PLDLAnalysisException {
        new Thread() {
            @Override
            public void run() {
                try {
                    Graphics.main();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        PreParse parser = new PreParse("TEST.pldl", null);
        CFG cfg = parser.getCFG();
        cfg.augmentCFG();
        TransformTable table = cfg.getTable();
        System.out.println(table);
        AnalysisTree tree = table.getAnalysisTree(parser.getSymbols("1 + 3 * ( 5 + 6 )"));
        System.out.println(tree);
        System.out.println(PLDLParsingWarning.getLoggings());
    }
}