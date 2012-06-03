package com.aliasi.sentences;

import java.io.*;
import java.util.*;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 * An implementation of Marti Hearst's text tiling algorithm.
 */
public class TextTilingParagraphModel extends com.aliasi.sentences.AbstractSentenceModel {

	public TextTilingParagraphModel() { super(); }
	
	public void boundaryIndices(String[] tokens, String[] whitespaces, int start, int length, Collection<Integer> indices) {
		String stopwords[] = { "the" , "at" , "on", "a" };
		TextTilingParagraphModel t = new TextTilingParagraphModel(new RawText(tokens), stopwords);
		t.w = Math.max(1,Math.min((length / 2) - 1 , 100));
		t.s = 10;
		t.similarityDetermination();
		t.depthScore();
		t.boundaryIdentification();
		TreeSet<Integer> sindices = new TreeSet<Integer>();
		new IndoEuropeanSentenceModel().boundaryIndices(tokens,whitespaces,start,length,sindices);
		indices.add(t.segmentation.get(0));
		int previous = 0;
		for ( int i = 1 ; i<t.segmentation.size(); i++ ) {
			Integer in = t.segmentation.get(i);
			int max = sindices.higher(in);
			int min = sindices.lower(in);
			if (Math.abs(min-in) < Math.abs(max-in)) {				
				if (min==previous && max != (start + length - 1)) {
					previous = max;
					indices.add(max);
				} else if (min!=previous) {
					previous = min;
					indices.add(min);					
				}
			} else {
				if (min!=previous && max == (start + length - 1)) {
					previous = min;
					indices.add(min);
				} else if (max!=previous && max != (start + length - 1)) {
					previous = max;
					indices.add(max);					
				}
			}
		}
		indices.add(start + length - 1);
	}

	public static void main(String[] args) {
		String header = "";
		header += "##############################################################\n";
		header += "# This is JTextTile, a Java implementation of Marti Hearst's #\n";
		header += "# TextTiling algorithm. Free for educational, research and   #\n";
		header += "# other non-profit making uses only.                         #\n";
        header += "#                                                            #\n"; 
		header += "# Freddy Choi, Artificial Intelligence Group, Department of  #\n";
		header += "# Computer Science, University of Manchester.                #\n";
		header += "# Website : http://www.cs.man.ac.uk/~choif                   #\n";
		header += "# E:mail  : choif@cs.man.ac.uk                               #\n";
		header += "# Copyright 1999                                             #\n";
		header += "##############################################################";
		System.out.println(header);
		String aux = "";
		try {
			File input;
			try {
				input = new File(args[0]);
			} catch (Exception e) {
				input = new File("/text.txt");
			}
			System.out.println();
			BufferedReader r = new BufferedReader(new FileReader(input));
			StringBuffer text = new StringBuffer();
			while ( (aux=r.readLine()) !=null ) {
				text.append(aux);
				text.append('\n');
			}
			SentenceChunker mSentenceChunker = new SentenceChunker(IndoEuropeanTokenizerFactory.INSTANCE,new TextTilingParagraphModel());
			Chunking sentenceChunking = mSentenceChunker.chunk(text);
			genOutput(text.toString(), sentenceChunking);
		} catch (Exception e) {
			aux = "# Fatal error : " + e;
			System.err.println(aux);
			e.printStackTrace();
		}
	}

	protected int w = 100; // Size of the sliding window
	protected int s = 10; // Step size

	/* Token -> stem dictionary */
	protected Hashtable<String,String> stemOf = new Hashtable<String,String>(); // Token -> stem

	/* Similarity scores and the corresponding locations */
	protected float[] sim_score = new float[0];
	protected int[] site_loc = new int[0];

	/* Depth scores */
	protected float[] depth_score = new float[0];

	/* Segment boundaries */
	protected Vector<Integer> segmentation = new Vector<Integer>();

	Set<String> S;
	RawText C;
	
	/**
	 * Segment a collection
	 */
	private TextTilingParagraphModel( RawText text, String[] stopwords ) {
		C = text;
		S = new HashSet<String>();
		for ( String s : stopwords ) S.add(s.toLowerCase());
		preprocess();
	}

	/**
	 * Add a term to a block
	 */
	protected void blockAdd(final String term, Hashtable<String, Integer> B) {
		Integer freq = (Integer) B.get(term);
		if (freq == null) freq = new Integer(1);
		else freq = new Integer(freq.intValue() + 1);
		B.put(term, freq);
	}

	/**
	 * Compute the cosine similarity measure for two blocks
	 */
	protected float blockCosine(final Hashtable<String,Integer> B1, final Hashtable<String,Integer> B2) {
		/* 1. Declare variables */
		int W; // Weight of a term (temporary variable)
		int sq_b1 = 0; // Sum of squared weights for B1
		int sq_b2 = 0; // Sum of squared weights for B2
		int sum_b = 0; // Sum of product of weights for common terms in B1 and B2
		/* 2. Compute the squared sum of term weights for B1 */
		for (Enumeration<Integer> e = B1.elements(); e.hasMoreElements();) {
			W = e.nextElement().intValue();
			sq_b1 += (W * W);
		}
		/* 3. Compute the squared sum of term weights for B2 */
		for (Enumeration<Integer> e = B2.elements(); e.hasMoreElements();) {
			W = e.nextElement().intValue();
			sq_b2 += (W * W);
		}
		/* 4. Compute sum of term weights for common terms in B1 and B2 */
		/* 4.1. Union of terms in B1 and B2 */
		Hashtable<String,Boolean> union = new Hashtable<String,Boolean>(B1.size() + B2.size());
		for (Enumeration<String> e = B1.keys(); e.hasMoreElements();)
			union.put(e.nextElement(), new Boolean(true));
		for (Enumeration<String> e = B2.keys(); e.hasMoreElements();)
			union.put(e.nextElement(), new Boolean(true));
		/* 4.2. Compute sum */
		Integer W1; // Weight of a term in B1 (temporary variable)
		Integer W2; // Weight of a term in B2 (temporary variable)
		String term; // A term (temporary variable)
		for (Enumeration<String> e = union.keys(); e.hasMoreElements();) {
			term = e.nextElement();
			W1 = B1.get(term);
			W2 = B2.get(term);
			if (W1 != null && W2 != null)
				sum_b += (W1.intValue() * W2.intValue());
		}
		/* 5. Compute similarity */
		float sim;
		sim = (float) sum_b / (float) Math.sqrt(sq_b1 * sq_b2);
		return sim;
	}

	/**
	 * Remove a term from the block
	 */
	protected void blockRemove(final String term, Hashtable<String,Integer> B) {
		Integer freq = (Integer) B.get(term);
		if (freq != null) {
			if (freq.intValue() == 1) B.remove(term);
			else B.put(term, new Integer(freq.intValue() - 1));
		}
	}

	/**
	 * Identify the boundaries
	 */
	protected void boundaryIdentification() {
		/* Declare variables */
		float mean = 0; // Mean depth score
		float sd = 0; // S.D. of depth score
		float threshold; // Threshold to use for determining boundaries
		int neighbours = 3; // The area to check before assigning boundary
		/* Compute mean and s.d. from depth scores */
		for (int i = depth_score.length; i-- > 0;) mean += depth_score[i];
		mean = mean / depth_score.length;
		for (int i = depth_score.length; i-- > 0;) sd += Math.pow(depth_score[i] - mean, 2);
		sd = sd / depth_score.length;
		/* Compute threshold */
		threshold = mean - sd / 2;
		/* Identify segments in pseudo-sentence terms */
		Vector<Integer> pseudo_boundaries = new Vector<Integer>();
		boolean largest = true; // Is the potential boundary the largest in the local area?
		for (int i = depth_score.length; i-- > 0;) {
			/* Found a potential boundary */
			if (depth_score[i] >= threshold) {
				/* Check if the nearby area has anything better */
				largest = true;
				for (int j = neighbours; largest && j > 0 && (i - j) > 0; j--) {
					if (depth_score[i - j] > depth_score[i]) largest = false;
				}
				for (int j = neighbours; largest && j > 0 && (i + j) < depth_score.length; j--) {
					if (depth_score[i + j] > depth_score[i]) largest = false;
				}
				/* Lets make the decision */
				if (largest) pseudo_boundaries.addElement(new Integer(site_loc[i]));
			}
		}
		/* Convert pseudo boundaries into real boundaries. We use the nearest true boundary. */
		/* Convert real boundaries into array for faster access */
		int[] true_boundaries = new int[C.boundaries.size()];
		for (int i = true_boundaries.length; i-- > 0;) true_boundaries[i] = ((Integer) C.boundaries.elementAt(i)).intValue();
		int pseudo_boundary;
		int distance; // Distance between pseudo and true boundary
		int smallest_distance; // Shortest distance
		int closest_boundary; // Nearest real boundary
		for (int i = pseudo_boundaries.size(); i-- > 0;) {
			pseudo_boundary = ((Integer) pseudo_boundaries.elementAt(i)).intValue();
			smallest_distance = Integer.MAX_VALUE;
			closest_boundary = true_boundaries[0];
			for (int j = true_boundaries.length; j-- > 0;) {
				distance = Math.abs(true_boundaries[j] - pseudo_boundary);
				if (distance <= smallest_distance) {
					smallest_distance = distance;
					closest_boundary = true_boundaries[j];
				}
			}
			segmentation.addElement(new Integer(closest_boundary));
		}
	}

	/**
	 * Compute depth score after applying similarityDetermination()
	 */
	protected void depthScore() {
		/* Declare variables */
		float maxima = 0; // Local maxima
		float dleft = 0; // Difference for the left side
		float dright = 0; // Difference for the right side
		/* For each position, compute depth score */
		depth_score = new float[sim_score.length];
		for (int i = sim_score.length; i-- > 0;) {
			maxima = sim_score[i];
			for (int j = i; j > 0 && sim_score[j] >= maxima; j--) maxima = sim_score[j];
			dleft = maxima - sim_score[i];
			maxima = sim_score[i];
			for (int j = i; j < sim_score.length && sim_score[j] >= maxima; j++) maxima = sim_score[j];
			dright = maxima - sim_score[i];
			depth_score[i] = dleft + dright;
		}
	}

	protected static void genOutput( String str , Chunking chunking ) throws IOException {
		for ( Chunk chunk : chunking.chunkSet() ) {
			String aux = "";
			aux = "==========";
			System.out.println(aux);
			int start = chunk.start();
			int end = chunk.end();
			System.out.println(str.substring(start,end));			
		}
	}
	
	/**
	 * Decide whether word i is worth using as feature for segmentation.
	 */
	protected boolean include(int i) {
		/*
		 * Noise reduction by filtering out everything but nouns and verbs -
		 * Best but requires POS tagging String pos = (String)
		 * C.pos.elementAt(i); return (pos.startsWith("N") ||
		 * pos.startsWith("V"));
		 */
		/* Noise reduction by stopword removal - OK */
		String token = (String) C.text.elementAt(i);
		return !S.contains(token.toLowerCase());
	}

	/**
	 * Perform some preprocessing to save execution time
	 */
	protected void preprocess() {
		PorterStemmer stemmer = new PorterStemmer();
		Vector<String> text = C.text;
		String token;
		for (int i = text.size(); i-- > 0;) {
			token = (String) text.elementAt(i);
			stemOf.put(token, token);
		}
		for (Enumeration<String> e = stemOf.keys(); e.hasMoreElements();) {
			token = (String) e.nextElement();
			stemmer.setCurrent(token);
			stemmer.stem();
			stemOf.put(token, stemmer.getCurrent());
		}
	}

	/**
	 * Compute the similarity score.
	 */
	protected void similarityDetermination() {
		/* Declare variables */
		Vector<String> text = C.text; // The source text
		Hashtable<String,Integer> left = new Hashtable<String,Integer>(); // Left sliding window
		Hashtable<String,Integer> right = new Hashtable<String,Integer>(); // Right sliding window
		Vector<Float> score = new Vector<Float>(); // Scores
		Vector<Integer> site = new Vector<Integer>(); // Locations
		/* Initialise windows */
		for (int i = w; i-- > 0;) blockAdd((String) stemOf.get((String) text.elementAt(i)), left);
		for (int i = w * 2; i-- > w;) blockAdd((String) stemOf.get((String) text.elementAt(i)), right);
		/* Slide window and compute score */
		final int end = text.size() - w; // Last index to check
		String token; // A stem
		int step = 0; // Step counter
		int i; // Counter
		for (i = w; i < end; i++) {
			/* Compute score for a step */
			if (step == 0) {
				score.addElement(new Float(blockCosine(left, right)));
				site.addElement(new Integer(i));
				step = s;
			}
			/* Remove word which is at the very left of the left window */
			if (include(i - w)) {
				blockRemove((String) stemOf.get((String) text.elementAt(i - w)), left);
			}
			/* Add current word to the left window and remove it from the right window */
			if (include(i)) {
				token = (String) text.elementAt(i);
				blockAdd((String) stemOf.get(token), left);
				blockRemove((String) stemOf.get(token), right);
			}
			/* Add the first word after the very right of the right window */
			if (include(i + w)) {
				blockAdd((String) stemOf.get((String) text.elementAt(i + w)), right);
			}
			step--;
		}
		/* Compute score for the last step */
		if (step == 0) {
			score.addElement(new Float(blockCosine(left, right)));
			site.addElement(new Integer(i));
			step = s;
		}
		/* Smoothing with a window size of 3 */
		sim_score = new float[score.size() - 2];
		site_loc = new int[site.size() - 2];
		for (int j = 0; j < sim_score.length; j++) {
			sim_score[j] = (((Float) score.elementAt(j)).floatValue() + ((Float) score.elementAt(j + 1)).floatValue() + ((Float) score.elementAt(j + 2)).floatValue()) / 3;
			site_loc[j] = ((Integer) site.elementAt(j + 1)).intValue();
		}
	}

}

class RawText {
	public Vector<String> text = new Vector<String>();
	public Vector<Integer> boundaries = new Vector<Integer>();
	public RawText(String tokens[] ) {
		for ( int i = 0; i<tokens.length ; i++ ) {
			text.add(tokens[i]);
			boundaries.add(i);
		}
	}
}