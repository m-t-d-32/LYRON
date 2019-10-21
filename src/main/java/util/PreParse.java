package util;

import java.io.File;
import java.util.*;

import exception.PLDLParsingException;
import lexer.DFA;
import lexer.NFA;
import lexer.SimpleREApply;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import parser.CFG;
import parser.CFGProduction;
import symbol.SymbolPool;
import translator.MovementProduction;

public class PreParse {

    Set<String> terminators = new HashSet<>();
    Set<String> unterminators = new HashSet<>();
    List<Map.Entry<String, NFA>> terminatorsNFA = new ArrayList<>();
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
            List<Element> terminatorsEl = el.elements("item");
            for (Element e : terminatorsEl) {
                String name = e.element("name").getText().trim();
                String regex = e.element("regex").getText().trim();
                terminatorsNFA.add(new AbstractMap.SimpleEntry<>(name, new SimpleREApply(regex).getNFA()));
                terminators.add(name);
            }
            el = root.element("cfgproductions");
            List<Element> pdsEl = el.elements("item");
            for (Element e : pdsEl) {
                String production =  e.element("production").getText().trim();
                prods.add(production);
                unterminators.add(production.split("->")[0].trim());
                List<Element> movements = e.element("movement").elements("item");
                List<String> movementsStr = new ArrayList<>();
                for (Element movement: movements){
                    movementsStr.add(movement.getText().trim());
                }
                movementsStrs.add(movementsStr);
            }
            for (Element e : pdsEl){
                String production = e.element("production").getText().trim();
                String[] afters = production.split("->")[1].trim().split(" +");
                for (String after : afters) {
                    if (!unterminators.contains(after.trim()) && !terminators.contains(after.trim())) {
                        terminatorsNFA.add(new AbstractMap.SimpleEntry<>(after.trim(), NFA.fastNFA(after.trim())));
                        terminators.add(after.trim());
                    }
                }
            }
        }
    }
    
    public CFG getCFG() throws PLDLParsingException {
    	SymbolPool pool = new SymbolPool();
    	pool.initTerminatorString(terminators);
    	pool.initUnterminatorString(unterminators);
    	Set<MovementProduction> productions = new HashSet<>();
    	for (int i = 0; i < prods.size(); ++i){
    	    String s = prods.get(i);
            List<String> movementsStr = movementsStrs.get(i);
    	    productions.add(new MovementProduction(CFGProduction.getCFGProductionFromCFGString(s, pool), movementsStr));
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
