package translator;

import java.util.List;

public class Tuple4 {
    private String[] elements = new String[5];

    Tuple4(String s1, String s2, String s3, String s4) {
        elements[0] = s1;
        elements[1] = s2;
        elements[2] = s3;
        elements[3] = s4;
    }

    @Override
    public String toString() {
        return "(" + elements[0] + "," + elements[1] + "," + elements[2] + "," + elements[3] + ")";
    }
}
