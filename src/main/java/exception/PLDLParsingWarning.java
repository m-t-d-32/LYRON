package exception;

import java.util.ArrayList;
import java.util.List;

public class PLDLParsingWarning extends Throwable {

    private static final long serialVersionUID = -5121647494803023792L;

    private static ArrayList<String> loggings = new ArrayList<>();

    public static List<String> getLoggings() {
        return loggings;
    }

    public static void setLog(String warning) {
        System.err.println("Warning: " + warning);
        loggings.add(warning);
    }
}
