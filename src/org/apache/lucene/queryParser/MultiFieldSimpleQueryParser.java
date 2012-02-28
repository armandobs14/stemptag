package org.apache.lucene.queryParser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class MultiFieldSimpleQueryParser extends SimpleQueryParser {

    private String[] fields;

    private Map boosts;

    public MultiFieldSimpleQueryParser(String[] fields, Analyzer a, Map boosts) {
        super("", a);
        this.fields = fields;
        this.boosts = boosts;        
       	setValidFields(fields);
    }

    /** Parses a query string, returning a {@link org.apache.lucene.search.Query}.
     *  @param query  the query string to be parsed.
     *  @throws ParseException if the parsing fails
     */
    public Query parse(String query) throws ParseException {
      ReInit(new FastCharStream(new StringReader(query)));
      try {
        return Query("");
      }
      catch (ParseException tme) {
        throw new ParseException("Cannot parse '" +query+ "': " + tme.getMessage());
      }
      catch (TokenMgrError tme) {
        throw new ParseException("Cannot parse '" +query+ "': " + tme.getMessage());
      }
      catch (BooleanQuery.TooManyClauses tmc) {
        throw new ParseException("Cannot parse '" +query+ "': too many boolean clauses");
      }
    }

    /**
     * @exception ParseException throw in overridden method to disallow
     */
    protected Query getFieldQuery(String field, String queryText, boolean isPhrase)  throws ParseException {
      if ((!"".equals(field) && validFieldSet.contains(field)) || this.fields.length == 1) {
        if ("".equals(field) || !(validFieldSet.contains(field))) {
          field = this.fields[0];
        }
        return super.getFieldQuery(field, queryText, isPhrase);
      }
      Vector v = new Vector();
      org.apache.lucene.analysis.Token t;
      int positionCount = 0;
      boolean severalTokensAtSamePosition = false;
      if(!"".equals(field) && ! validFieldSet.contains(field)) {
        TokenStream source = analyzer.tokenStream(this.field, new StringReader(field));
        while (true) {
          try {
            t = source.next();
          }
          catch (IOException e) {
            t = null;
          }
          if (t == null)
            break;
          if(! t.termText().equals("")) {
            v.addElement(t);
          }
        }
        try {
          source.close();
        }
        catch (IOException e) { }
        field = "";
      }
      TokenStream source = analyzer.tokenStream(field, new StringReader(queryText));
      while (true) {
        try {
          t = source.next();
        }
        catch (IOException e) {
          t = null;
        }
        if (t == null)
          break;
        v.addElement(t);
        if (t.getPositionIncrement() != 0)
          positionCount += t.getPositionIncrement();
        else
          severalTokensAtSamePosition = true;
      }
      try {
        source.close();
      }
      catch (IOException e) { }
      if (v.size() == 0) {
        return null;
      }
      if (v.size() == 1) {
        BooleanQuery q = new BooleanQuery(true);
        for (int m = 0; m < this.fields.length; m++) {
          t = (org.apache.lucene.analysis.Token) v.elementAt(0);
          TermQuery tq = new TermQuery(new Term(this.fields[m], t.termText()));
          if (this.boosts.get(this.fields[m]) != null) {
            float boost = ((Float) this.boosts.get(this.fields[m])).floatValue();
            tq.setBoost(boost);
          }
          q.add(tq, BooleanClause.Occur.SHOULD);
        }
        return q;
      }
      if (severalTokensAtSamePosition) {
        if (positionCount == 1) {
          BooleanQuery q = new BooleanQuery(true);
          for (int i = 0; i < v.size(); i++) {
            t = (org.apache.lucene.analysis.Token) v.elementAt(i);
            BooleanQuery sq = new BooleanQuery(true);
            for (int m=0; m<this.fields.length; m++) {
              TermQuery tq = new TermQuery(
                new Term(this.fields[m], t.termText()));
              float boost = this.boosts.get(this.fields[m]) == null ? 1.0f : ((Float) this.boosts.get(this.fields[m])).floatValue();
              tq.setBoost(boost);
              sq.add(tq, BooleanClause.Occur.SHOULD);
            }
            q.add(sq, BooleanClause.Occur.SHOULD);
          }
          return q;
        }
        else {
          if(isPhrase) {
            BooleanQuery q = new BooleanQuery(true);
            for (int m=0; m<this.fields.length; m++) {
              MultiPhraseQuery mpq = new MultiPhraseQuery();
              mpq.setSlop(phraseSlop);
              List multiTerms = new ArrayList();
              for (int i = 0; i < v.size(); i++) {
                t = (org.apache.lucene.analysis.Token) v.elementAt(i);
                if (t.getPositionIncrement() == 1 && multiTerms.size() > 0) {
                  mpq.add((Term[])multiTerms.toArray(new Term[0]));
                  multiTerms.clear();
                }
                multiTerms.add(new Term(field, t.termText()));
              }
              mpq.add((Term[])multiTerms.toArray(new Term[0]));
              q.add(mpq, BooleanClause.Occur.SHOULD);
            }
            return q;
          } else {
            BooleanQuery q = new BooleanQuery(true);
            List multiTerms = new ArrayList();
            for (int i = 0; i < v.size(); i++) {
              t = (org.apache.lucene.analysis.Token) v.elementAt(i);
              if (t.getPositionIncrement() == 1 && multiTerms.size() > 0) {
                BooleanQuery sq = new BooleanQuery(true);
                for (int m=0; m<this.fields.length; m++) {
                BooleanQuery ssq = new BooleanQuery(true);
                  for (int n=0; n<multiTerms.size(); n++) {
                    org.apache.lucene.analysis.Token oneToken = 
                     (org.apache.lucene.analysis.Token) multiTerms.get(n);
                    TermQuery tq = new TermQuery(
                      new Term(this.fields[m], oneToken.termText()));
                    float boost = this.boosts.get(this.fields[m]) == null ? 1.0f : ((Float) this.boosts.get(this.fields[m])).floatValue();
                    tq.setBoost(boost);
                    ssq.add(tq, BooleanClause.Occur.SHOULD);
                  }
                  sq.add(ssq, BooleanClause.Occur.SHOULD);
                }
                q.add(sq, BooleanClause.Occur.MUST);
                multiTerms.clear();
              }
              multiTerms.add(new Term(field, t.termText()));
            }
            if (multiTerms.size() > 0) {
              BooleanQuery sq = new BooleanQuery(true);
              for (int m=0; m<this.fields.length; m++) {
                BooleanQuery ssq = new BooleanQuery(true);
                for (int n=0; n<multiTerms.size(); n++) {
                  org.apache.lucene.analysis.Token oneToken = 
                   (org.apache.lucene.analysis.Token) multiTerms.get(n);
                  TermQuery tq = new TermQuery(
                    new Term(this.fields[m], oneToken.termText()));
                  float boost = this.boosts.get(this.fields[m]) == null ? 1.0f : ((Float) this.boosts.get(this.fields[m])).floatValue();
                  tq.setBoost(boost);
                  ssq.add(tq, BooleanClause.Occur.SHOULD);
                }
                sq.add(ssq, BooleanClause.Occur.SHOULD);
              }
              q.add(sq, BooleanClause.Occur.MUST);
            }
            return q;
          }
        }
      } else {
   	    if(isPhrase) {
          BooleanQuery q = new BooleanQuery(true);
          for (int m=0; m<this.fields.length; m++) {
            PhraseQuery pq = new PhraseQuery();
            pq.setSlop(phraseSlop);
            for (int i = 0; i < v.size(); i++) {
              pq.add(new Term(this.fields[m], ((org.apache.lucene.analysis.Token) 
                v.elementAt(i)).termText()));
            }
            q.add(pq, BooleanClause.Occur.SHOULD);
          }
          return q;
        }
        else {
          BooleanQuery q = new BooleanQuery(true);
          for (int i = 0; i < v.size(); i++) {
            t = (org.apache.lucene.analysis.Token) v.elementAt(i);
            BooleanQuery sq = new BooleanQuery(true);
            for (int m=0; m<this.fields.length; m++) {
              TermQuery tq = new TermQuery(
                new Term(this.fields[m], t.termText()));
              float boost = this.boosts.get(this.fields[m]) == null ? 1.0f : ((Float) this.boosts.get(this.fields[m])).floatValue();
              tq.setBoost(boost);
              sq.add(tq, BooleanClause.Occur.SHOULD);
            }
            q.add(sq, BooleanClause.Occur.MUST);
          }
          return q;
        }
      }
    }

}