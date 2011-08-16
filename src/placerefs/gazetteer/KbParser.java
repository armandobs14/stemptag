package placerefs.gazetteer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.aliasi.spell.EditDistance;

/**
 * Reads the XML file containing the TAC-KBP knowledge base.
 * 
 * @author ivo
 *
 */
public class KbParser {

    private IndexWriter namesWriter = null;
    private IndexWriter completeWriter = null;
    private SpellChecker spellchecker = null;
    private Map<String, Set<String>> wikiAlternativeNames = new HashMap<String, Set<String>>();

    public void indexKb(){
        try {
            setWikiAlternatives(Configurator.DBPEDIA_REDIRECTS);
            setWikiAlternatives(Configurator.DBPEDIA_DISAMBIGUATIONS);


            File workspace = new File(Configurator.LUCENE_KB);
            if (!workspace.exists() && !workspace.mkdirs()) {
                System.out.println("Problem creating directory.");
            }


            Directory luceneNamesIdx = FSDirectory.open(new File(Configurator.LUCENE_KB_NAMES));            
            namesWriter = new IndexWriter(luceneNamesIdx, new KeywordAnalyzer(), true, 
                    IndexWriter.MaxFieldLength.UNLIMITED);

            Directory luceneTextIdx = FSDirectory.open(new File(Configurator.LUCENE_KB_COMPLETE));     
            completeWriter = new IndexWriter(luceneTextIdx, new StandardAnalyzer(Version.LUCENE_29), 
                    true, IndexWriter.MaxFieldLength.UNLIMITED);


            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            File kbFolder = new File(Configurator.KB_FOLDER);
            for (File f : kbFolder.listFiles()){
                System.out.println("file: " + f.getName());
                parser.parse(f, new KbHandler());
            }

            completeWriter.optimize();
            completeWriter.close();
            namesWriter.optimize();
            namesWriter.close();
            luceneTextIdx.close();
            luceneNamesIdx.close();

            /** SpellCheck index. **/
            Directory namesDirectory = FSDirectory.open(new File(Configurator.LUCENE_KB_NAMES));
            IndexReader iReader = IndexReader.open(namesDirectory, true);
            File luceneSpellIndex = new File(Configurator.LUCENE_KB_SPELLCHECK);
            Directory luceneSpellDirectory = FSDirectory.open(luceneSpellIndex);
            spellchecker = new SpellChecker(luceneSpellDirectory, "name", "eid");
            spellchecker.indexDictionary(new LuceneDictionary(iReader));
            spellchecker.close();
            luceneSpellDirectory.close();
            iReader.close();
            namesDirectory.close();
        } catch(Exception e) {
            e.printStackTrace();
        } 
    }



    public void indexEntity (KbEntity entity){
        
        
        
        if (entity == null) return;
        
        try {

            Document doc = new Document();
            doc.add(new Field("eid", entity.id, Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field("name", entity.name, Field.Store.YES, Field.Index.NO));
            doc.add(new Field("wiki_title", entity.wiki_title, Field.Store.YES, Field.Index.NO));
            doc.add(new Field("text", entity.wiki_text, Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("coord", entity.coordinates, Field.Store.YES, Field.Index.NOT_ANALYZED));
            if (entity.population != null)
                doc.add(new Field("pop", entity.population, Field.Store.YES, Field.Index.NOT_ANALYZED));
            if (entity.area != null)
                doc.add(new Field("area", entity.area, Field.Store.YES, Field.Index.NOT_ANALYZED));

            Set<String> altNames = getWikiAlternativesTo(entity.wiki_title);
            if (altNames != null) {
                for (String name : altNames) {
                    doc.add(new Field("altname", getNormalizedName(name), 
                            Field.Store.YES, Field.Index.NO));
                }
            }
            completeWriter.addDocument(doc);

            doc = new Document();
            doc.add(new Field("eid", entity.id, Field.Store.YES, Field.Index.NO));
            doc.add(new Field("name", entity.name, Field.Store.YES, Field.Index.ANALYZED));
            namesWriter.addDocument(doc);

            //useful for cases like "Cleveland, Ohio", where we want an entry for "Cleveland"
            int idx = entity.name.indexOf(',');
            if (idx > 0) {
                if (altNames == null) { altNames = new HashSet<String>(); }
                altNames.add(entity.name.substring(0, idx));
            }


            if (altNames != null) {
                for (String altName : altNames) {
                    doc = new Document();
                    doc.add(new Field("eid", entity.id, Field.Store.YES,  Field.Index.NO));
                    doc.add(new Field("name", getNormalizedName(altName), 
                            Field.Store.YES, Field.Index.ANALYZED));
                    namesWriter.addDocument(doc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 
     * @param Wikipedia title
     * @return The normalized title
     */
    public static String getNormalizedName(String name) {
        //String newName = URLDecoder.decode(name, "UTF-8"); //it is already being done
        String newName = name.replaceFirst("_\\(.+\\)", "");
        newName = newName.replace('_', ' ');
        return  newName;
    }

    /**
     * 
     * @param wikiTitle The name of a Wikipedia page.
     * @return A list containing all Wikipedia names redirecting/disambiguating to target page, or null if none.
     */
    public Set<String> getWikiAlternativesTo(String wikiTitle) {
        return wikiAlternativeNames.get(wikiTitle);
    }



    private void setWikiAlternatives(String source) throws Exception {
        if (wikiAlternativeNames == null) {
            wikiAlternativeNames = new HashMap<String, Set<String>>();
        }
        BufferedReader reader = new BufferedReader(new FileReader(source));
        String namePre = "<http://dbpedia.org/resource/";
        String line;
        while ((line = reader.readLine()) != null) {                
            int nameEnd = line.indexOf('>');                
            String redFrom = new String(line.substring(namePre.length(), nameEnd));
            redFrom = URLDecoder.decode(redFrom, "UTF-8");

            int redPreStart = line.indexOf(namePre, nameEnd);
            int redSufStart = line.indexOf('>', redPreStart + 1);
            String redTo = new String(line.substring(redPreStart + namePre.length(), redSufStart));
            redTo = URLDecoder.decode(redTo, "UTF-8");

            Set<String> froms = wikiAlternativeNames.get(redTo);
            if (froms == null) {
                froms = new HashSet<String>();
            }
            froms.add(redFrom);
            wikiAlternativeNames.put(redTo, froms);
        }
        reader.close();
    }

    /************
     * 
     * SAX Handler.
     * 
     * Note: only considers the link text in a fact, not the link itself.
     * 
     ************/
    private class KbHandler extends DefaultHandler {
        private String currentFact = null;
        private StringBuffer accumulator = new StringBuffer();
        private KbEntity entity = null;

        private String c = "\\-?\\d{1,3}[^\\s\\w=]\\d+[^\\s\\w=]?(\\d+(\\.\\d+)?[^\\s\\w=]?)?[NSns]?" +
                           "((\\s+|\\s*,)\\s*)" +
        		           "\\-?\\d{1,3}[^\\s\\w=]\\d+[^\\s\\w=]?(\\d+(\\.\\d+)?[^\\s\\w=]?)?[EWew]?";
        private Pattern cp = Pattern.compile(c);
        
        private String p = "\\d+(.\\d+)*";
        private Pattern pp = Pattern.compile(p);
        
        private String a = "\\d+(.\\d+)*";
        private Pattern ap = Pattern.compile(a);
        
        private String coordinate;

        private String latd;
        private String latm;
        private String lats;
        private String latns;

        private String lond;
        private String lonm;
        private String lons;
        private String lonew;

        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {

            accumulator.setLength(0);

            if (qName.equals("entity")){
                latd = null;
                latm = null;
                lats = null;
                latns = null;
                lond = null;
                lonm = null;
                lons = null;
                lonew = null;
                coordinate = null;

                entity = new KbEntity();
                entity.wiki_title = attributes.getValue("wiki_title");
                entity.id = attributes.getValue("id");
                entity.name = attributes.getValue("name");
            } else if (qName.equals("fact")) {
                currentFact = attributes.getValue("name");
            }
        }

        public void characters(char[] buffer, int start, int length) {
            accumulator.append(buffer, start, length);
        }

        public void endElement(String uri, String localName, String qName) {

            if (qName.equals("entity")){

                if (entity.coordinates == null && latd != null && lond != null) {
                    String latitude = latd;
                    String longitude = lond;
                    if (latm != null) latitude += ":" + latm;
                    if (lonm != null) longitude += ":" + lonm;
                    if (lats != null) latitude += ":" + lats;
                    if (lons != null) longitude += ":" + lons;
                    if (latns != null) latitude += latns.toUpperCase();
                    if (lonew != null) longitude += lonew.toUpperCase();

                    entity.coordinates = latitude + " " + longitude;
                    entity.coordinates = normalizeCoordinate(entity.coordinates);
                }
                
                if (entity.coordinates == null || !cp.matcher(entity.coordinates).matches()) return;

                if (entity.coordinates.indexOf(':') > 0) 
                    entity.coordinates = CoordinatesConverter.deg2Dec(entity.coordinates);
                
                indexEntity(entity);

            } else if (qName.equals("fact")){
                if (accumulator.length() > 0) {
                    String value = accumulator.toString().trim();

                    /****
                     * Area
                     */
                    if (currentFact.equalsIgnoreCase("area_total_km2")
                            || currentFact.equalsIgnoreCase("area_km2")){

                        Matcher matcher = ap.matcher(value);
                        boolean found = matcher.find();
                        if (found) {
                            value = matcher.group();
                            value = value.replaceAll(",", "");
                            
                            entity.area = value;
                        } else {
                            return;
                        }
                    } else if (currentFact.equalsIgnoreCase("area_total_sq_mi")
                            ||currentFact.equalsIgnoreCase("area_sq_mi")) {
                        Matcher matcher = ap.matcher(value);
                        boolean found = matcher.find();
                        if (found  && entity.area == null) { //give preference to km2
                            value = matcher.group();
                            value = value.replaceAll(",", "");
                            Double sqmi = Double.parseDouble(value);
                            entity.area = "" + (sqmi * 2.58998811);
                        } else {
                            return;
                        }
                    } else
                    /****
                     * Population
                     */
                    if (currentFact.equalsIgnoreCase("population")
                            || currentFact.equalsIgnoreCase("population_total")
                            || currentFact.equalsIgnoreCase("pop")){
                        
                        value = value.toLowerCase();
                        value = value.replaceAll("\\s*million(s)?", "000000");
                        value = value.replaceAll("0+\\d+0+", "");
                        
                        Matcher matcher = pp.matcher(value);
                        boolean found = matcher.find();
                        if (found) {
                            value = matcher.group();
                            value = value.replaceAll("[^\\d]", "");
                            
                            entity.population = value;
                        } else {
                            return;
                        }
                    } else 
                    /****
                     * Coordinates
                     */
                    if (currentFact.equalsIgnoreCase("coor")
                            || currentFact.equalsIgnoreCase("coord")
                            || currentFact.equalsIgnoreCase("coords")
                            || currentFact.equalsIgnoreCase("coordinates")) {
                        
                        Matcher matcher = cp.matcher(value);
                        boolean found = matcher.find();
                        if (found) {
                            value = matcher.group();
                        } else {
                            return;
                        }
                        
                        value = normalizeCoordinate(value);
                        entity.coordinates = value;
                        
                    } else if (currentFact.equalsIgnoreCase("latitude")) {
                        value = normalizeCoordinate(value);
                        if (coordinate == null) { coordinate = value; } 
                        else { coordinate = value + " " + coordinate; }
                        
                        if (entity.coordinates == null) entity.coordinates = coordinate;
                        
                    } else if (currentFact.equalsIgnoreCase("longitude")) {
                        value = normalizeCoordinate(value);
                        if (coordinate == null) { coordinate = value; } 
                        else { coordinate += " " + value; }
                        
                        if (entity.coordinates == null) entity.coordinates = coordinate;
                        
                    } else if (currentFact.equalsIgnoreCase("latd")
                            || currentFact.equalsIgnoreCase("lat_d")
                            || currentFact.equalsIgnoreCase("lat_degrees")
                            || currentFact.equalsIgnoreCase("lat_deg")) {
                        latd = value;
                    } else if (currentFact.equalsIgnoreCase("latm")
                            || currentFact.equalsIgnoreCase("lat_m")
                            || currentFact.equalsIgnoreCase("lat_minutes")
                            || currentFact.equalsIgnoreCase("lat_min")) {
                        latm = value;
                    } else if (currentFact.equalsIgnoreCase("lats")
                            || currentFact.equalsIgnoreCase("lat_s")
                            || currentFact.equalsIgnoreCase("lat_seconds")
                            || currentFact.equalsIgnoreCase("lat_sec")) {
                        lats = value;
                    } else if (currentFact.equalsIgnoreCase("latns")
                            || currentFact.equalsIgnoreCase("lat_ns")
                            || currentFact.equalsIgnoreCase("lat_direction")) {
                        latns = value;
                    } else if (currentFact.equalsIgnoreCase("longd")
                            || currentFact.equalsIgnoreCase("long_d")
                            || currentFact.equalsIgnoreCase("long_degrees")
                            || currentFact.equalsIgnoreCase("lon_deg")) {
                        lond = value;
                    } else if (currentFact.equalsIgnoreCase("longm")
                            || currentFact.equalsIgnoreCase("long_m")
                            || currentFact.equalsIgnoreCase("long_minutes")
                            || currentFact.equalsIgnoreCase("lon_min")) {
                        lonm = value;
                    } else if (currentFact.equalsIgnoreCase("longs")
                            || currentFact.equalsIgnoreCase("long_s")
                            || currentFact.equalsIgnoreCase("long_seconds")
                            || currentFact.equalsIgnoreCase("lon_sec")) {
                        lons = value;
                    } else if (currentFact.equalsIgnoreCase("longew")
                            || currentFact.equalsIgnoreCase("long_ew")
                            || currentFact.equalsIgnoreCase("long_direction")) {
                        lonew = value;
                    }
                }
            } else if (qName.equals("wiki_text")){
                entity.wiki_text = AsciiConverter.convert(accumulator.toString().trim());
            }

            accumulator.setLength(0);
        }

        /**
         * @param c
         * @return
         */
        private String normalizeCoordinate(String c) {
            c = c.toUpperCase();
            c = c.replaceAll("[^\\dNEWS\\-\\+\\.]+(?=[NSEW])", "");
            c = c.replaceAll("\\s*,\\s*", " ");
            c = c.replaceAll("[^\\dNEWS\\-\\+\\. ]", ":");
            return c;
        }
    }
    /*****
     *
     * END SAX Handler.
     * 
     *******/
    
    /**
     * Controls the application workflow.
     * 
     * @param args not used
     * @throws Exception pray
     */
    public static void main(String[] args) throws Exception {


        if (Configurator.INDEX) {
            KbParser kbp = new KbParser();
            kbp.indexKb();
        }
        String toponym = "Lisbon";
        CandidateGenerator cg = new CandidateGenerator();
        List<KbEntity> candidates = cg.getCandidates(toponym);
        for (KbEntity c : candidates) {
            System.out.println(c.name);
        }        
        EditDistance levenshtein = new EditDistance(true);
        System.out.println(levenshtein.proximity(toponym, "Lisabonne"));
    }

}

