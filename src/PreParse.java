import java.io.File;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class PreParse {
    public static CFG readFromFile(String filename, String markinStr) throws DocumentException, PLDLParsingException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filename));
        Element root = document.getRootElement();
        Set<String> terminators = null, unterminators = null;
        List<String> prods = null;
        if (root.getName().equals("pldl")) {
            for (Iterator<Element> i = root.elementIterator(); i.hasNext(); ) {
                Element el = i.next();
                switch (el.attribute("name").getValue()) {
                    case "unterminators":
                        List<Element> unterminatorsEl = el.elements("item");
                        unterminators = new HashSet<>();
                        for (Element e : unterminatorsEl) {
                            unterminators.add("_" + e.getText().trim());
                        }
                        break;
                    case "terminators":
                        List<Element> terminatorsEl = el.elements("item");
                        terminators = new HashSet<>();
                        for (Element e : terminatorsEl) {
                            terminators.add("_" + e.getText().trim());
                        }
                        break;
                    case "cfgproductions":
                        List<Element> pdsEl = el.elements("item");
                        prods = new ArrayList<>();
                        for (Element e : pdsEl) {
                            prods.add(e.getText().trim());
                        }
                        break;
                }
            }
        }
        CFG cfg = null;
        if (prods != null && terminators != null && unterminators != null) {
            cfg = new CFG(prods, terminators, unterminators, markinStr);
        }
        return cfg;
    }

    public static CFG autoRead(String filename, String markinStr) throws DocumentException, PLDLParsingException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filename));
        Element root = document.getRootElement();
        Set<String> terminators = null, unterminators = null;
        List<String> prods = null;
        if (root.getName().equals("pldl")) {
            for (Iterator<Element> i = root.elementIterator(); i.hasNext(); ) {
                Element el = i.next();
                if (el.attribute("name").getValue().equals("cfgproductions")) {
                    List<Element> pdsEl = el.elements("item");
                    unterminators = new HashSet<>();
                    prods = new ArrayList<>();
                    for (Element e : pdsEl) {
                        String production = e.getText().trim();
                        prods.add(production);
                        unterminators.add("_" + production.split("->")[0].trim());
                    }
                    terminators = new HashSet<>();
                    for (Element e : pdsEl) {
                        String[] afters = e.getText().trim().split("->")[1].trim().split(" +");
                        for (String after : afters) {
                            if (!unterminators.contains(after.trim())) {
                                terminators.add("_" + after.trim());
                            }
                        }
                    }
                }
            }
        }
        CFG cfg = null;
        if (prods != null && terminators != null && unterminators != null) {
            cfg = new CFG(prods, terminators, unterminators, markinStr);
        }
        return cfg;
    }
}
