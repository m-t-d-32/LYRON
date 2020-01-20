package app;
import util.Graphics;

public class GUIApplication {

    public static void main(String[] args) throws InterruptedException {
        if (args.length > 0 || System.getProperty("java.awt.headless") == null ||
                !System.getProperty("java.awt.headless").equals("true")){
            Graphics.main();
        }
        else {
            ConsoleApplication.main(args);
        }
    }
}
