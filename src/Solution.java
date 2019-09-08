import jdk.nashorn.internal.codegen.CompilerConstants;
import org.dom4j.DocumentException;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class Solution {

    public static void main(String[] args) throws PLDLParsingException, DocumentException {
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

        CFG cfg = PreParse.autoRead("TEST.pldl", null);
        cfg.augmentCFG();
        cfg.getTable();
        System.out.println(PLDLParsingWarning.getLoggings());
    }
}