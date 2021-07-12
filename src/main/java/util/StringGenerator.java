package util;

import java.io.Serializable;

public class StringGenerator implements Serializable {
    private static final char BEGIN_CODE = 'A';
    private static final char END_CODE = 'Z';

    private static StringBuilder serialCode = new StringBuilder();

    static {
        serialCode.append(BEGIN_CODE);
    }


    public static String getNextCode() {
        String result = serialCode.toString();
        boolean end = false;
        for (int i = 0; i < serialCode.length(); ++i) {
            if (serialCode.charAt(i) != END_CODE) {
                serialCode.setCharAt(i, (char) (serialCode.charAt(i) + 1));
                end = true;
                break;
            }
            else {
                serialCode.setCharAt(i, BEGIN_CODE);
            }
        }
        if (!end) {
            serialCode.append(BEGIN_CODE);
        }
        return result;
    }

}
