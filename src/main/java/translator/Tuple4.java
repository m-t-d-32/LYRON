package translator;

public class Tuple4 {
    private String[] elements = new String[5];

    public Tuple4(String s1, String s2, String s3, String s4) {
        elements[0] = s1;
        elements[1] = s2;
        elements[2] = s3;
        elements[3] = s4;
    }

    public Tuple4(Tuple4 other) {
        elements[0] = other.get(0);
        elements[1] = other.get(1);
        elements[2] = other.get(2);
        elements[3] = other.get(3);
    }

    @Override
    public String toString() {
        return "(" + elements[0] + "," + elements[1] + "," + elements[2] + "," + elements[3] + ")";
    }

    public String get(int index) {
        return elements[index];
    }

    public void set(int index, String s) {
        elements[index] = s;
    }
}
