import java.io.File;
import java.io.StringBufferInputStream;
import java.util.*;
import java.util.regex.*;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class PreParse {
	
    Set<String> unterminators = new HashSet<>();
    Map<String, Pattern> terminators = new HashMap<>();
    List<String> prods = new ArrayList<>();
    String markinStr = null;
	
    public PreParse(String filename, String markinStr) throws DocumentException {
    	this.markinStr = markinStr;
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filename));
        Element root = document.getRootElement();
        if (root.getName().equals("pldl")) {
            for (Iterator<Element> i = root.elementIterator(); i.hasNext(); ) {
                Element el = i.next();
                if (el.attribute("name").getValue().equals("cfgproductions")) {
                    List<Element> pdsEl = el.elements("item");
                    for (Element e : pdsEl) {
                        String production = e.getText().trim();
                        prods.add(production);
                        unterminators.add(production.split("->")[0].trim());
                    }
                    for (Element e : pdsEl) {
                        String[] afters = e.getText().trim().split("->")[1].trim().split(" +");
                        for (String after : afters) {
                            if (!unterminators.contains(after.trim()) && !terminators.containsKey(after.trim())) {
                                terminators.put(after.trim(), Pattern.compile(Pattern.quote(after.trim())));
                            }
                        }
                    }
                }
                else if (el.attribute("name").getValue().equals("terminators")){
                	List<Element> terminatorsEl = el.elements("item");
                    for (Element e : terminatorsEl) {
                    	String name = e.element("name").getText().trim();                            
                        Pattern pattern = Pattern.compile(e.element("regex").getText().trim());
                        terminators.put(name, pattern);
                    }
                }
            }
        }
    }
    
    public CFG getCFG() throws PLDLParsingException {
    	CFG cfg = null;
    	if (prods != null && terminators != null && unterminators != null) {
            cfg = new CFG(prods, terminators.keySet(), unterminators, markinStr);
        }
    	return cfg;
    }

	public List<SymbolExtra> getSymbols(String str, CFG cfg) throws PLDLAnalysisException, PLDLParsingException{
		List<String> rawStrings = new ArrayList<>();
		List<SymbolExtra> resultTokens = new ArrayList<>();
		Scanner fileScanner = new Scanner(new StringBufferInputStream(str));
		while (fileScanner.hasNext()){
			String nowString = fileScanner.next();
			int matchIndex = 0;
			while (true){
				if (matchIndex >= nowString.length()){
					break;
				}
				else {
					nowString = nowString.substring(matchIndex);
				}
				boolean find = false;
				System.out.println(nowString);
				for (String terminatorStr: terminators.keySet()) {
					Matcher matcher = terminators.get(terminatorStr).matcher(nowString);
					if (matcher.find() && nowString.indexOf(matcher.group(0)) == 0){
						Terminator terminator = cfg.getSymbolPool().getTerminator(terminatorStr);
						String resultString = matcher.group(0);
						TerminatorExtra te = new TerminatorExtra(terminator);
						te.addProperty("name", resultString);
						resultTokens.add(te);
						matchIndex = resultString.length();
						find = true;
						break;
					}
				}
				if (find == false){
					throw new PLDLAnalysisException("表达式无效", null);
				}
			}
		}
		return resultTokens;
    }
}
