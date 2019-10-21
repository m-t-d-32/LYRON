package translator;

import parser.AnalysisNode;
import parser.AnalysisTree;
import util.StringGenerator;

import java.util.ArrayList;
import java.util.List;

public class Translator {

    AnalysisTree tree = null;

    public Translator(AnalysisTree tree){
        this.tree = tree;
    }

    public void doTranslate(List<String> generations){
        rrTranslate(tree.getRoot(), generations);
    }

    private void rrTranslate(AnalysisNode root, List<String> generations) {
        if (root.getChildren() == null){
            return;
        }
        for (AnalysisNode node: root.getChildren()){
            rrTranslate(node, generations);
        }
        MovementProduction production = (MovementProduction) root.getProduction();
        for (Tuple5 tuple5: production.getMovements()){
            switch (tuple5.getElement(0)){
                case "gen":
                case "generate":
                case "g":
                    String []results = new String[4];
                    results[0] = tuple5.getElement(1).trim();
                    for (int i = 1; i < 4; ++i) {
                        results[i] = getObject(root, tuple5.getElement(i + 1).trim());
                    }
                    generations.add(String.join(",", results));
                    break;
                case "do":
                case "d":
                    doObject(root, tuple5.getElement(1), tuple5.getElement(2),
                        tuple5.getElement(3), tuple5.getElement(4));
                    break;
            }
        }
    }

    private String getObject(AnalysisNode root, String objName) {
        int begin = objName.indexOf("(") + 1, end = objName.indexOf(")");
        if (begin >= end){
            return objName;
        }
        String propertyName = objName.substring(begin, end);
        switch (objName.charAt(0)){
            case '$':
                int num = Integer.valueOf(objName.substring(1, objName.indexOf("(")));
                --num;
                return root.getChildren().get(num).getValue().getProperties().get(propertyName).toString();
            case 'R':
            case 'r':
                return root.getValue().getProperties().get(propertyName).toString();
            case 'P':
            case 'p':
                return root.getParent().getValue().getProperties().get(propertyName).toString();
        }
        return null;
    }

    private void doObject(AnalysisNode root, String s1, String s2, String s3, String s4) {
        switch (s1) {
            case "=":
                String from = null;
                String propertyName = null;
                switch (s2) {
                    case "tmp(next)":
                        from = StringGenerator.getNextCode();
                        break;
                    default:
                        propertyName = s2.substring(s2.indexOf("(") + 1, s2.indexOf(")"));
                        switch (s2.charAt(0)) {
                            case '$':
                                int num = Integer.valueOf(s2.substring(1, s2.indexOf("(")));
                                --num;
                                from = root.getChildren().get(num).getValue().getProperties().get(propertyName).toString();
                                break;
                            case 'R':
                            case 'r':
                                from = root.getValue().getProperties().get(propertyName).toString();
                                break;
                            case 'P':
                            case 'p':
                                from = root.getParent().getValue().getProperties().get(propertyName).toString();
                                break;
                        }
                        break;
                }
                propertyName = s3.substring(s3.indexOf("(") + 1, s3.indexOf(")"));
                switch (s3.charAt(0)) {
                    case '$':
                        int num = Integer.valueOf(s3.substring(1, s3.indexOf("(")));
                        --num;
                        root.getChildren().get(num).getValue().getProperties().put(propertyName, from);
                        break;
                    case 'R':
                    case 'r':
                        root.getValue().getProperties().put(propertyName, from);
                        break;
                    case 'P':
                    case 'p':
                        root.getParent().getValue().getProperties().put(propertyName, from);
                        break;
                }
        }
    }
}
