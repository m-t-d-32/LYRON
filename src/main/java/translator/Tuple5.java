package translator;

import java.util.List;

public class Tuple5 {
    private String []elements = new String[5];

    public Tuple5(String []s4){
        for (int i = 0; i < 5; ++i){
            elements[i] = s4[i];
        }
    }

    public Tuple5(List<String> s4){
        for (int i = 0; i < 5; ++i){
            elements[i] = s4.get(i);
        }
    }

    public void setElement(int index, String s){
        elements[index] = s;
    }

    public String getElement(int index){
        return elements[index];
    }

}
