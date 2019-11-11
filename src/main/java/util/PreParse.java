package util;

import exception.PLDLParsingException;
import exception.PLDLParsingWarning;
import lexer.NFA;
import lexer.SimpleREApply;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import parser.CFG;
import parser.CFGProduction;
import symbol.AbstractTerminator;
import symbol.SymbolPool;
import symbol.Terminator;

import java.io.File;
import java.util.*;

public class PreParse {

    Set<String> terminators = new HashSet<>();
    Set<String> unterminators = new HashSet<>();
    Set<String> comments = new HashSet<>();
    List<Map.Entry<String, NFA>> terminatorsNFA = new ArrayList<>();
    Map<String, Set<String>> bannedStrMap = new HashMap<>();
    List<String> prods = new ArrayList<>();
    List<List<String> > movementsStrs = new ArrayList<>();
    String markinStr = null;
	
    public PreParse(String filename, String markinStr) throws Exception {
    	this.markinStr = markinStr;
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filename));
        Element root = document.getRootElement();
        if (root.getName().equals("pldl")) {
            Element el = root.element("terminators");
            List<Element> terminatorsEl = null;
            if (el != null){
                terminatorsEl = el.elements("item");
            }
            el = root.element("cfgproductions");
            List<Element> pdsEl = null;
            if (el != null){
                pdsEl = el.elements("item");
            }
            el = root.element("comments");
            List<Element> commentsEl = null;
            if (el != null){
                commentsEl = el.elements("item");
            }
            if (pdsEl != null) {
                for (Element e : pdsEl) {
                    String production = e.element("production").getText().trim();
                    prods.add(production);
                    unterminators.add(production.split("->")[0].trim());
//                List<Element> movements = e.element("movement").elements("item");
//                List<String> movementsStr = new ArrayList<>();
//                for (Element movement: movements){
//                    movementsStr.add(movement.getText().trim());
//                }
//                movementsStrs.add(movementsStr);
                }
            }
            if (terminatorsEl != null){
                for (Element e : terminatorsEl) {
                    String name = e.element("name").getText().trim();
                    terminators.add(name);
                }
            }
            if (commentsEl != null) {
                for (Element e : commentsEl) {
                    String name = e.element("name").getText().trim();
                    comments.add(name);
                }
            }
            if (pdsEl != null) {
                for (Element e : pdsEl) {
                    String production = e.element("production").getText().trim();
                    String[] afters = production.split("->")[1].trim().split(" +");
                    for (String after : afters) {
                        if (!after.equals("null") && !comments.contains(after.trim()) &&
                                !unterminators.contains(after.trim()) && !terminators.contains(after.trim())) {
                            terminatorsNFA.add(new AbstractMap.SimpleEntry<>(after.trim(), NFA.fastNFA(after.trim())));
                            terminators.add(after.trim());
                        }
                    }
                }
            }
            if (terminatorsEl != null) {
                for (Element e : terminatorsEl) {
                    String name = e.element("name").getText().trim();
                    String regex = e.element("regex").getText().trim();
                    terminatorsNFA.add(new AbstractMap.SimpleEntry<>(name, new SimpleREApply(regex).getNFA()));
                    if (e.element("ban") != null){
                        String bannedStrs = e.element("ban").getText().trim();
                        Set<String> bannedStrsSet = new HashSet<>();
                        for (char c : bannedStrs.toCharArray()){
                            bannedStrsSet.add(String.valueOf(c));
                        }
                        bannedStrMap.put(name, bannedStrsSet);
                    }
                    else if (e.element("allow") != null){
                        PLDLParsingWarning.setLog("There is likely to be a significant performance cost in making these calls.");
                        String bannedStrs = e.element("ban").getText().trim();
                        Set<String> allStrsSet = new HashSet<>();
                        for (char c = 0x0; c < 0xff; ++c){
                            allStrsSet.add(String.valueOf(c));
                        }
                        Set<String> bannedStrsSet = new HashSet<>();
                        for (char c : bannedStrs.toCharArray()){
                            bannedStrsSet.add(String.valueOf(c));
                        }
                        allStrsSet.removeAll(bannedStrsSet);
                        bannedStrMap.put(name, allStrsSet);
                    }
                }
            }
            if (commentsEl != null) {
                for (Element e : commentsEl) {
                    String name = e.element("name").getText().trim();
                    String regex = e.element("regex").getText().trim();
                    terminatorsNFA.add(new AbstractMap.SimpleEntry<>(name, new SimpleREApply(regex).getNFA()));
                }
            }
        }
    }
    
    public CFG getCFG() throws PLDLParsingException {
    	SymbolPool pool = new SymbolPool();
    	pool.initTerminatorString(terminators);
    	pool.initUnterminatorString(unterminators);
    	for (String comment : comments){
    	    pool.addCommentStr(comment);
        }
    	Set<CFGProduction> productions = new HashSet<>();
    	for (int i = 0; i < prods.size(); ++i){
    	    String s = prods.get(i);
//          List<String> movementsStr = movementsStrs.get(i);
//    	    productions.add(new MovementProduction(CFGProduction.getCFGProductionFromCFGString(s, pool), movementsStr));
            CFGProduction production = CFGProduction.getCFGProductionFromCFGString(s, pool);
            production.setSerialNumber(i + 1);
            productions.add(production);
        }
    	return new CFG(pool, productions, markinStr);
    }

    public List<Map.Entry<String, NFA>> getTerminatorRegexes() {
        return terminatorsNFA;
    }

    public Map<String, String> getBannedStrs() {
        return null;
    }
}
