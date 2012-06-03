package org.apache.lucene.search.spell;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import placerefs.gazetteer.AsciiConverter;
import placerefs.gazetteer.Configurator;



public class SpellChecker implements java.io.Closeable {


    private final int[] idCache;
    private static final String F_ID = "eid";

    /**
     * the spell index
     */
    Directory spellIndex;

    private IndexSearcher searcher;

    private volatile boolean closed = false;

    private String wField;
    private String idField;

    /**
     * 
     * @param spellIndex
     * @param wField
     * @param idField
     * @throws IOException
     */
    public SpellChecker(Directory spellIndex, String wField, String idField, boolean readOnly) throws IOException {
        setSpellIndex(spellIndex, readOnly);
        this.wField = wField;
        this.idField = idField;
        this.idCache = FieldCache.DEFAULT.getInts(obtainSearcher().getIndexReader(), idField);
    }

    /**
     * 
     * @param spellIndexDir
     * @throws IOException
     */
    private void setSpellIndex(Directory spellIndexDir, boolean readOnly) throws IOException {
        ensureOpen();
        if (!readOnly && !IndexReader.indexExists(spellIndexDir)) {
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, null);
            IndexWriter writer = new IndexWriter(spellIndexDir, conf);
            writer.close();
        }
        createSearcher(spellIndexDir);
        this.spellIndex = spellIndexDir;
    }


    /**
     * 
     * @param word
     * @param numSug
     * @return
     * @throws IOException
     */
    public Set<Integer> suggestSimilar(String text, int numSug) throws Exception {

        String words = nomalizeName(text);

        final int len = words.length();
        final IndexSearcher indexSearcher = obtainSearcher();
        Query query = new BooleanQuery();
        Set<Integer> idRes = new HashSet<Integer>();
        
        
        if (len < 2) return new HashSet<Integer>();

        //grams
        for (int ng = getMin(len); ng <= getMax(len); ng++) {

			String[] grams = formGrams(words, ng); // form word into ngrams (allow dups too)
			
			for (int i = 0; i < grams.length; i++) {
			    add(query, "gram", grams[i]);
			}
		}

        
        ScoreDoc[] hits = indexSearcher.search(query, numSug).scoreDocs;
        for (int i = 0; i < hits.length; i++) {
        	idRes.add(this.idCache[hits[i].doc]);
        }
        
        //term
        query = new QueryParser(Version.LUCENE_35, "term", new WhitespaceAnalyzer(Version.LUCENE_35)).parse(words);
        
        hits = indexSearcher.search(query, numSug).scoreDocs;
        for (int i = 0; i < hits.length; i++) {
        	if(!(idRes.contains(this.idCache[hits[i].doc])))
	        	idRes.add(this.idCache[hits[i].doc]);
        }

        return idRes;
    }

    
    /**
     * 
     * @param word
     * @param numSug
     * @return
     * @throws IOException
     */
    
	
//    public List<SuggestWord> suggestSimilarWords(String text, int numSug) throws Exception {
//    	Directory indexLC = new SimpleFSDirectory(new File(Configurator.LUCENE_KB_COMPLETE));
//    	IndexReader reader = IndexReader.open(indexLC);
//    	IndexSearcher searcherLC = new IndexSearcher(reader);
//    	final IndexSearcher indexSearcher = obtainSearcher();
//        
//    	Set<Integer> suggestedWordsIds = suggestSimilar(text, numSug);
//        
//        List<SuggestWord> suggestions = new ArrayList<SuggestWord>();
//        
//        //TODO: COMPLETAR LISTA DE SUGGEST WORDS
//        
//		
//		TopScoreDocCollector collector;
//		
//		for (Integer i : suggestedWordsIds) {
////			String escaped = QueryParser.escape(Integer.toString(i));
////			Query q = new QueryParser(Version.LUCENE_35, "eid", new KeywordAnalyzer()).parse("\""+escaped+"\"");
//			TopDocs top = searcherLC.search(new TermQuery(new Term("eid", i.toString())), 1);
//			ScoreDoc[] hits = top.scoreDocs;
//			if(hits.length>0){
//				ScoreDoc hit = hits[0];
//				Document doc = indexSearcher.doc(hit.doc);
//				SuggestWord sugWord = new SuggestWord();
//				//TODO: Verificar se e term
//				sugWord.string = doc.get("name"); // get orig word
//				
////				System.out.println("doc get name: " + doc.get("name"));
////				System.out.println("doc get eid: " + doc.get("eid"));
////				System.out.println("doc get score: " + hit.score);
//				
//				sugWord.eid = doc.get("eid"); // get the orig id
//				sugWord.score = -1;//sd.getDistance(word, sugWord.string);
//				suggestions.add(sugWord);
//			}
//		}
//		
//		
//        return suggestions;
//    }
    
    
    /**
     * @param text
     * @return
     */
    private String nomalizeName(String text) {
        return AsciiConverter.convert(text).toLowerCase();
    }

    /**
     * Add a clause to a boolean query.
     */
    private static void add(Query q, String name, String value) {
        ((BooleanQuery)q).add(new BooleanClause(new TermQuery(new Term(name, value)), BooleanClause.Occur.SHOULD));
    }

    /**
     * Form all ngrams for a given word.
     * @param text the word to parse
     * @param ng the ngram length e.g. 3
     * @return an array of all ngrams in the word and note that duplicates are not removed
     */
    private static String[] formGrams(String text, int ng) {
        int len = text.length();
        String[] res = new String[len - ng + 1];
        for (int i = 0; i < len - ng + 1; i++) {
            res[i] = text.substring(i, i + ng);
        }
        return res;
    }



    /**
     * Indexes the data from the given {@link Dictionary}.
     * @param dict Dictionary to index
     * @param ramMB the max amount or memory in MB to use
     * @throws AlreadyClosedException if the Spellchecker is already closed
     * @throws IOException
     */
    public void indexDictionary(Dictionary dict, int ramMB) throws IOException {
        ensureOpen();
        final Directory dir = this.spellIndex;
        final IndexWriterConfig conf = new IndexWriterConfig(
                Version.LUCENE_35, 
                new WhitespaceAnalyzer(Version.LUCENE_35));
        conf.setRAMBufferSizeMB(ramMB);
        final IndexWriter writer = new IndexWriter(dir, conf);

        Iterator<Document> iter = dict.getWordsIterator();
        while (iter.hasNext()) {
            Document baseDoc = iter.next();
            String words = nomalizeName(baseDoc.get(wField));
            String id = baseDoc.get(idField);

            int len = words.length();
            if (len < 2) {
                continue; // too short we bail but "too long" is fine...
            }

            // ok index the word
            Document doc = createDocument(words, id, getMin(len), getMax(len));
            writer.addDocument(doc);
        }
        // close writer
        writer.optimize();
        writer.close();
        // also re-open the spell index to see our own changes when the next suggestion
        // is fetched:
        createSearcher(dir);
    }


    /**
     * 
     * @param dict
     * @throws IOException
     */
    public void indexDictionary(Dictionary dict) throws IOException {
        indexDictionary(dict, 200);
    }

    private static int getMin(int l) {
        return 2;
    }

    private static int getMax(int l) {
        return Math.min(l, 4);
    }

    private static Document createDocument(String text, String id, int ng1, int ng2) {
        Document doc = new Document();
        doc.add(new Field(F_ID, id, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS)); // orig id
        addGram(text, doc, ng1, ng2);
        addTerms(text, doc);
        return doc;
    }

    private static void addTerms(String text, Document doc) {

        doc.add(new Field("term", text, Field.Store.NO, Field.Index.ANALYZED));
    }

    private static void addGram(String text, Document doc, int ng1, int ng2) {
        int len = text.length();
        for (int ng = ng1; ng <= ng2; ng++) {
            for (int i = 0; i < len - ng + 1; i++) {
                String gram = text.substring(i, i + ng);
                doc.add(new Field("gram", gram, Field.Store.NO, Field.Index.NOT_ANALYZED));
            }
        }
    }

    private IndexSearcher obtainSearcher() {
        ensureOpen();
        return searcher;
    }

    private void ensureOpen() {
        if (closed) {
            throw new AlreadyClosedException("Spellchecker has been closed");
        }
    }

    /**
     * Close the IndexSearcher used by this SpellChecker
     * @throws IOException if the close operation causes an {@link IOException}
     * @throws AlreadyClosedException if the {@link SpellChecker} is already closed
     */
    public void close() throws IOException {
        ensureOpen();
        closed = true;
        if (searcher != null) {
            searcher.close();
        }
        searcher = null;
    }

    /**
     * Creates a new read-only IndexSearcher 
     * @param dir the directory used to open the searcher
     * @return a new read-only IndexSearcher
     * @throws IOException f there is a low-level IO error
     */
    IndexSearcher createSearcher(final Directory dir) throws IOException{
        if(searcher == null)
            searcher = new IndexSearcher(dir, true);

        return searcher;
    }

    /**
     * Returns <code>true</code> if and only if the {@link SpellChecker} is
     * closed, otherwise <code>false</code>.
     * 
     * @return <code>true</code> if and only if the {@link SpellChecker} is
     *         closed, otherwise <code>false</code>.
     */
    boolean isClosed(){
        return closed;
    }

}