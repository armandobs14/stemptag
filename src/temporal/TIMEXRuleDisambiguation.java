package temporal;


import java.util.ArrayList;
import java.util.Iterator;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

public class TIMEXRuleDisambiguation implements Chunker {

    private Chunker chunker;

    public TIMEXRuleDisambiguation ( Chunker chunker ) {
    	this.chunker = chunker;
    }
    
    public TIMEXRuleDisambiguation ( ) {
    	
    }
    
    public Chunking chunk(CharSequence cSeq) { 
 	   return chunk(cSeq.toString().toCharArray(),0,cSeq.length()); 
    }
    
    public Chunking chunk(char[] cs, int start, int end) { 
    	String s = new String(cs,start,end);
 	    ChunkingImpl chunks = new ChunkingImpl(s);
 	    Chunking chunking = chunker.chunk(cs,start,end);
 	    for ( Chunk chunk : chunking.chunkSet()) {
 	    	if (chunk.type().equals("TIMEX2") && !(chunk instanceof NormalizedChunk)) {
 	    		NormalizedChunk timex = new NormalizedChunk(chunk);
 	    		//timex.setNormalized(createCanonicalForm(s.substring(chunk.start(),chunk.end())));
 	    		chunks.add(timex);
 	    	} else chunks.add(chunk);
 	    }
 		return chunks;
    }
    
    //Estimate overlap between two intervals
    /**
     * @param interval1 -> The first interval
     * @param interval2 -> The second interval
     * @return -> The normalized overlap between the two intervals
     */
    public static double normalizedOverlap(Interval interval1, Interval interval2){
    	
    	if (!interval1.overlaps(interval2))
    		return 0;
    	
    	return interval1.overlap(interval2).toDurationMillis()/between(interval1, interval2);
    	
    //	return (float)(interval1.getEndMillis()-interval2.getStartMillis())/(interval2.getEndMillis()-interval1.getStartMillis());	 
    }
    
    
    /**
     * @param interval1 -> The first interval
     * @param interval2 -> The second interval
     * @return -> The length of the union of the two intervals
     */
    public static long between(Interval interval1, Interval interval2){
    	if(interval1.gap(interval2) != null){
    		return interval1.toDurationMillis()+interval2.toDurationMillis()+interval1.gap(interval2).toDurationMillis();
    	}
    	else{
    		return interval1.toDurationMillis()+interval2.toDurationMillis()-interval1.overlap(interval2).toDurationMillis();
    	}
    }
    
    //Generate features for each temporal expression
    /**
     * @param timex -> The words composing the temporal expression
     * @param candidate -> The candidate who's features are being generated
     * @return -> ArrayList composed of features for the candidate
     */
    public static ArrayList<String> featuresGenerator(Interval candidate){
		ArrayList<String> _array = new ArrayList<String>();
		long centerPointCandidate = 0;
		long centerPointDocTimeStamp = 0;
		Long aux;
		Long minVal = Long.MAX_VALUE;
		Long value;
		Long valueDuration;
		Long maxValDuration = Long.MIN_VALUE;
		int size = 0;
		boolean flag = false;
		try{
		//Feature 1: distance between center point of candidate and center point of document timestamp
		centerPointCandidate = (long)(candidate.getEndMillis()+candidate.getStartMillis())/2;
		centerPointDocTimeStamp = (long)(CandidateCreation.docCreationTime.getMillis());
		
		System.out.println("Features for candidate: "+candidate.toString());
		
		System.out.println(centerPointCandidate+" CPC");
		System.out.println(centerPointDocTimeStamp+" CPDTS");
		
		aux = Math.abs(centerPointCandidate-centerPointDocTimeStamp);
		_array.add(aux.toString());
		
		//Feature 2: the number of milliseconds between the center point of the candidate and the closest candidate disambiguation recognized in the same document
		Iterator<Long> it = CandidateCreation.candidatesMillisecondsSameDoc.iterator();
		size = CandidateCreation.candidatesMillisecondsSameDoc.size()-CandidateCreation.numberCandidatesTimex;
		System.out.println(CandidateCreation.numberCandidatesTimex+" teste");
		System.out.println(size+" teste2");
		while (it.hasNext() && size > 0){
			flag = true;
			value = Math.abs(centerPointCandidate - it.next());
			if (value < minVal){
				minVal = value;
			}
			size--;
		}
		if (flag)
			_array.add(minVal.toString());
		else
			_array.add("-1");
		flag = false;
		
		//Feature 3: the number of milliseconds between the center point of the candidate and the closest candidate disambiguation recognized in the same sentence
		minVal = Long.MAX_VALUE;
		it = CandidateCreation.candidatesMillisecondsSameSentence.iterator();
		size = CandidateCreation.candidatesMillisecondsSameSentence.size()-CandidateCreation.numberCandidatesTimex;
		while (it.hasNext() && size > 0){
			flag = true;
			value = Math.abs(centerPointCandidate - it.next());
			if (value < minVal){
				minVal = value;
			}
			size--;
		}
		if (flag)
			_array.add(minVal.toString());
		else
			_array.add("-1");
		flag = false;
		
		//Feature 4: the maximum duration of the period of temporal overlap between the candidate disambiguation and and some other candidate disambiguation in the same document
		Iterator<Interval> it2 = CandidateCreation.candidatesIntervalsSameDoc.iterator();
		size = CandidateCreation.candidatesIntervalsSameDoc.size()-CandidateCreation.numberCandidatesTimex;
		
		while (it2.hasNext() && size > 0){
			flag = true;
			Interval t = candidate.overlap(it2.next());
			if (t != null){
				valueDuration = t.toDurationMillis();
				if (valueDuration > maxValDuration){
					maxValDuration = valueDuration;
				}
			}
			else{
				if (0 > maxValDuration){
					maxValDuration = new Long(0);
				}
			}
			size--;
		}
		if (flag)
			_array.add(maxValDuration.toString());
		else
			_array.add("-1");
		flag = false;
		
		//Feature 5: the maximum duration of the period of temporal overlap between the candidate disambiguation and and some other candidate disambiguation in the same sentence
		maxValDuration = Long.MIN_VALUE;
		it2 = CandidateCreation.candidatesIntervalsSameSentence.iterator();
		size = CandidateCreation.candidatesIntervalsSameSentence.size()-CandidateCreation.numberCandidatesTimex;
		
		while (it2.hasNext() && size > 0){
			flag = true;
			Interval t = candidate.overlap(it2.next());
			if (t != null){
				valueDuration = t.toDurationMillis();
				if (valueDuration > maxValDuration){
					maxValDuration = valueDuration;
				}
			}
			else{
				if (0 > maxValDuration){
					maxValDuration = new Long(0);
				}
			}
			size--;
		}
		if (flag)
			_array.add(maxValDuration.toString());
		else
			_array.add("-1");
		flag = false;
		
		//Feature 6: numeric value if temporal period corresponds to (1) less than a minute, (2) hour, (3) day, (4) week, (5) month, (6) year, (7) decade, (8) century or (9) millennium
		
		if (candidate.toDuration().getMillis() <= 60000)
			_array.add("1");
		else if (candidate.toDuration().getMillis() <= 3600000)
			_array.add("2");
		else if (candidate.toDuration().getMillis() <= 86400000)
			_array.add("3");
		else if (candidate.toDuration().getStandardSeconds() <= 604800)
			_array.add("4");
		else if (candidate.toDuration().getStandardSeconds() <= 2688400)
			_array.add("5");
		else if (candidate.toDuration().getStandardSeconds() <= 31656926)
			_array.add("6");
		else if (candidate.toDuration().getStandardSeconds() <= 10*31656926)
			_array.add("7");
		else if (candidate.toDuration().getStandardSeconds() <= 100*31656926)
			_array.add("8");
		else 
			_array.add("9");
		
		//Feature 7: The milliseconds from 1970 to the beginning of the interval

		_array.add(""+Math.abs(candidate.getStartMillis()));
		
		//Feature 8: The milliseconds from 1970 to the end of the interval
		
		if (candidate.getStartMillis() < 0){
			long auxLong = Math.abs(candidate.getStartMillis())+candidate.toDurationMillis();
			_array.add(""+auxLong);
		}
			
		else
			_array.add(""+candidate.getEndMillis());
		
		//Feature 9: The duration in milliseconds of the interval
		
		_array.add(""+candidate.toDurationMillis());
		
		
    	return _array;
		}catch(Exception e){
			e.printStackTrace();
		}
		return _array;
    }
    
    
    
	/**
	 * Creates the canonical form values for a new {@link Annotation} element for
	 * the given temporal expression.
	 * 
	 * @param timeEx
	 *            the temporal expression for which the canonical form shall be created
	 * @return the canonical form values for a new {@link Annotation} element for
	 *         the given temporal expression
	 * @throws Exception 
	 */
	public static ArrayList<Interval> createCanonicalForm ( String timeEx ) {
		ArrayList<Interval> _array = new ArrayList<Interval>();
		ArrayList<Interval> auxIntervalListDoc = new ArrayList<Interval>();
		DateTime today = new DateTime();
		Interval aux = null;
		ArrayList<Interval> toPreviousAnchor = new ArrayList<Interval>();
		
		try{
		
		//Verifica se o ficheiro e XML, se for usa como ancora a data de criacao do documento
		if (CandidateCreation.isXMLflag){
			aux = createCanonicalForm( timeEx, CandidateCreation.docCreationTime );

			if (aux == null){
				CandidateCreation.isNull = true;
				return null;
			}

			if(!_array.contains(aux)){
				_array.add(aux);
				if (CandidateCreation.granularityDuration == 0){
					toPreviousAnchor.add(aux);
					if (!CandidateCreation.candidatesIntervalsSameDoc.contains(aux)){
						CandidateCreation.candidatesMillisecondsSameDoc.add((long)(aux.getEndMillis()+aux.getStartMillis())/2);
						CandidateCreation.candidatesIntervalsSameDoc.add(aux);
					}
					if (!CandidateCreation.candidatesIntervalsSameSentence.contains(aux)){
						CandidateCreation.candidatesMillisecondsSameSentence.add((long)(aux.getEndMillis()+aux.getStartMillis())/2);
						CandidateCreation.candidatesIntervalsSameSentence.add(aux);
					}
					CandidateCreation.numberCandidatesTimex ++;
				}
			}
		}
		
		//Usa como ancora o presente dia
		aux = createCanonicalForm( timeEx, today );

		if (aux == null){
			CandidateCreation.isNull = true;
			return null;
		}

		if(!_array.contains(aux)){
			_array.add(aux);
			if (CandidateCreation.granularityDuration == 0){
				toPreviousAnchor.add(aux);
				if (!CandidateCreation.candidatesIntervalsSameDoc.contains(aux)){
					CandidateCreation.candidatesMillisecondsSameDoc.add((long)(aux.getEndMillis()+aux.getStartMillis())/2);
					CandidateCreation.candidatesIntervalsSameDoc.add(aux);
				}
				if (!CandidateCreation.candidatesIntervalsSameSentence.contains(aux)){
					CandidateCreation.candidatesMillisecondsSameSentence.add((long)(aux.getEndMillis()+aux.getStartMillis())/2);
					CandidateCreation.candidatesIntervalsSameSentence.add(aux);
				}
			CandidateCreation.numberCandidatesTimex ++;
			}
		}
		
		//Verifica se e o primeiro timex, se nao for, usa tambem como ancora todos os candidatos anteriores do documento
		if (!CandidateCreation.isFirstTimex){
			Iterator<Interval> it2 = CandidateCreation.candidatesIntervalsSameDoc.iterator();
			while (it2.hasNext())
				auxIntervalListDoc.add(it2.next());
			
			Iterator<Interval> it3 = auxIntervalListDoc.iterator();
			int size = CandidateCreation.candidatesIntervalsSameDoc.size()-toPreviousAnchor.size();
			System.out.println("Tamanho previousAnchor: "+ CandidateCreation.candidatesIntervalsSameDoc.size());
			
			while (it3.hasNext() && size > 0){
				aux = createCanonicalForm( timeEx, it3.next().getStart() );
				
				if (aux != null)
					if(!_array.contains(aux)){
						_array.add(aux);
						if (CandidateCreation.granularityDuration == 0){
							if (!auxIntervalListDoc.contains(aux)){
								CandidateCreation.candidatesMillisecondsSameDoc.add((long)(aux.getEndMillis()+aux.getStartMillis())/2);
								CandidateCreation.candidatesIntervalsSameDoc.add(aux);
							}
							if (!CandidateCreation.candidatesIntervalsSameSentence.contains(aux)){
								CandidateCreation.candidatesMillisecondsSameSentence.add((long)(aux.getEndMillis()+aux.getStartMillis())/2);
								CandidateCreation.candidatesIntervalsSameSentence.add(aux);
							}
						CandidateCreation.numberCandidatesTimex ++;
						}
					}
			}
			size--;
		}
		else CandidateCreation.isFirstTimex = false;
		
		return _array;
		}catch(Exception e){
			e.printStackTrace();
		}
		return _array;
	}

	/**
	 * Creates the canonical form value for a new {@link Annotation} element for
	 * the given temporal expression.
	 * 
	 * @param timeEx
	 *            the temporal expression for which the canonical form shall be
	 *            created
	 * @param temporalContext
	 *            the temporal context to use for replacing {@code [DATE]} and
	 *            {@code [DATETIME]} placeholders appropriately
	 * @return the canonical form value for a new {@link Annotation} element for
	 *         the given temporal expression
	 */
	public static Interval createCanonicalForm( String timeEx, DateTime temporalContext ) {
		Interval result;
        try{
        	result = CandidateCreation.normalizeTimex(timeEx,temporalContext);
        }catch(Exception e){
        	e.printStackTrace();
        	return null;
        }
				
		return result;
	}
	
}