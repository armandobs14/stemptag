package temporal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

public class TIMEXRuleDisambiguation implements Chunker {

    private Chunker chunker;

    public TIMEXRuleDisambiguation ( Chunker chunker ) {
    	this.chunker = chunker;
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
 	    		timex.setNormalized(createCanonicalForm(s.substring(chunk.start(),chunk.end())));
 	    		chunks.add(timex);
 	    	} else chunks.add(chunk);
 	    }
 		return chunks;
    }
        
	/** a date format for ISO 8601 dates */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat ("yyyy-MM-dd");

	/** a date format for ISO 8601 dates with time */
	private static final DateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

	/**
	 * an array of {@link TimeExMapping}s which is used to find and annotate a
	 * few simple temporal expressions
	 */
	private static final TimeExMapping[] TIME_EX_MAPPING;
	static {
		String[][] mappings = new String[][] {
				new String[] { "(this morning)", "[DATE]TEV" },
				new String[] { "(this afternoon)", "[DATE]TAF" },
				new String[] { "(this evening)", "[DATE]TNI" },
				new String[] { "(tonight)", "[DATE]TNI" },
				new String[] { "(from (\\d\\d)(?: ?(?:h|hours)))( to (\\d\\d) ?(?:h|hours))", "[DATE]T$2:00" + "[DATE]T$4:00" },
				new String[] { "(at (\\d\\d) ?(?:h|hours))", "[DATE]T$2:00" },
				new String[] { "(at (\\d) ?(?:h|hour))", "[DATE]T0$2:00" },
				new String[] { "(at (\\d\\d:\\d\\d)(?: ?(?:h|Uhr))?)", "[DATE]T$2" },
				new String[] { "(today)", "[DATE]" },
				new String[] { "(now)", "[DATETIME]" } };
		TIME_EX_MAPPING = new TimeExMapping[mappings.length];
		for (int i = 0; i < mappings.length; i++) TIME_EX_MAPPING[i] = new TimeExMapping(mappings[i][0], mappings[i][1]);
	}
	
    private static SimpleDateFormat[] formats = {
            new SimpleDateFormat("yyyy"),
        	new SimpleDateFormat("yyyy-MM"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"),
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yy-MM-dd'T'HH:mm:ss"),
            new SimpleDateFormat("yy-MM-dd'T'HH:mm"),
            new SimpleDateFormat("dd-MM-yyyy"),
            new SimpleDateFormat("dd/MM/yyyy"),
            new SimpleDateFormat("dd.MM.yyyy"),
            new SimpleDateFormat("yy-MM-dd"),
            new SimpleDateFormat("yyyy G"),
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z"),
            new SimpleDateFormat("yyyyy.MMMMM.dd GGG hh:mm aaa"),
            new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
    };
    
    private static String[] regularExpressions = {
    	//This RE validate Dates in the MMM dd, yyyy format from Jan 1, 1600 to Dec 31, 9999 including leap years
    	new String ("^(?:(((Jan(uary)?|Ma(r(ch)?|y)|Jul(y)?|Aug(ust)?|Oct(ober)?|Dec(ember)?)\\ 31)|((Jan(uary)?|Ma(r(ch)?|y)|Apr(il)?|Ju((ly?)|(ne?))|Aug(ust)?|Oct(ober)?|(Sept|Nov|Dec)(ember)?)\\ (0?[1-9]|([12][0-9])|30))|(Feb(ruary)?\\ (0?[1-9]|1[0-9]|2[0-8]|(29(?=,\\ ((1[6-9]|[2-9][0-9])(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00)))))))\\,\\ ((1[6-9]|[2-9][0-9])[0-9]{2}))"),
    	//ISO format YYYY-MM-DD with leap years
    	new String ("^((((19|20)(([02468][048])|([13579][26]))([/.-])02([/.-])29))|((20[0-9][0-9])|(19[0-9][0-9]))([/.-])((((0[1-9])|(1[0-2]))([/.-])((0[1-9])|(1[0-9])|(2[0-8])))|((((0[13578])|(1[02]))([/.-])31)|(((0[1,3-9])|(1[0-2]))([/.-])(29|30)))))$"),
    	//Validates Gregorian dates of the form (DD)? MONTH YYYY, taking days/month and leap years into account.Ex: 10 July 2010 or July 2010 or Nov 2010
    	new String ("(0[1-9]|[12][0-9]|3[01])? ?([Jj](anuary|uly|an)|[Mm]a(rch|y|r)|[Aa](ug|ugust)|([Oo](cto|ct)|[Dd](ec|ecem))(ber)?) [1-9][0-9]{3}| ?(0[1-9]|[12][0-9]|30)? ?([Aa](pr|pril)|[Jj](un|une)|([Ss](ep|ept)|[Nn]ov)(ember)?) [1-9][0-9]{3}| ?(0[1-9]|1[0-9]|2[0-8])? ?[Ff](eb|ebruary) [1-9][0-9]{3}| 29 [Ff](eb|ebruary) ((0[48]|[2468][048]|[13579][26])00|[0-9]{2}(0[48]|[2468][048]|[13579][26]))"),
    	//Validates MM/.-YYYY from 1900 to 2999
    	new String ("(((0[123456789]|10|11|12)([/.-])(([1][9][0-9][0-9])|([2][0-9][0-9][0-9]))))"),
    	//Validates DD/.-MM/.-YYYY with leap years
    	new String ("(((0[1-9]|[12][0-9]|3[01])([/.-])(0[13578]|10|12)([/.-])([1-2][0,9][0-9][0-9]))|(([0][1-9]|[12][0-9]|30)([/.-])(0[469]|11)([/.-])([1-2][0,9][0-9][0-9]))|((0[1-9]|1[0-9]|2[0-8])([/.-])(02)([/.-])([1-2][0,9][0-9][0-9]))|((29)(\\.|-|\\/)(02)([/.-])([02468][048]00))|((29)([/.-])(02)([/.-])([13579][26]00))|((29)([/.-])(02)([/.-])([0-9][0-9][0][48]))|((29)([/.-])(02)([/.-])([0-9][0-9][2468][048]))|((29)([/.-])(02)([/.-])([0-9][0-9][13579][26])))"),
    	//Validates MM/.-YY
    	new String ("^((0[1-9])|(1[0-2]))([/.-])([0-9]{2})$")
    };
    
    // Strings to compose complex dates
    private static String fullyear = "1[0-9]{3}|20[0-5][0-9]";
    private static String shortyear = "'?[0-9]{2}";
    private static String calendar_granularity = "(minute|hour|day|weekend|week|month|quarter|year)";
    private static String longdays = "((mon|tues|wednes|thurs|fri|satur|sun|to|yester)day|tomorrow)";
    private static String dayspec = "("+longdays+"|(mon|tue|wed|thu|fri|sat|sun))";
    private static String fullmonth = "(january|february|march|april|may|june|july|august|september|october|november|december)";
    private static String monthspec = "(jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec|"+fullmonth+")";
    private static String simple_ordinals = "(first|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth)";
    private static String numeric_days = "([0-9]{1,2}(st|nd|rd|th)?)";
    private static String digits = "(one|two|three|four|five|six|seven|eight|nine)";
    private static String teen = "(ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen)";
    private static String textual_number = "(((twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety)[\\ \\-])?"+digits+"|"+digits+"|a|an|"+teen+")";
    private static String vague = "(around|about|roughly|nearly|over)";
    private static String year = "("+fullyear+"|"+shortyear+")";
    private static String times = "([012][0-9][\\:\\.][0-5][0-9] ?(pm|am)?";
    
    // Expressions like "early this friday"
    private static String exp1 = "(((early|late|earlier|later) )?((this|next|last) )?("+calendar_granularity+"|"+longdays+"))";
    // Expressions like "mon 10 jan 2010"
    private static String exp2 = "(("+dayspec+" )?[0-9]{1,2}([\\.\\/\\-\\ ])([0-9]{1,2}|" + monthspec + ")\\7(19|20)?[0-9]{2})";
    // Expressions like "twenty hour long"
    private static String exp3 = "("+textual_number + "[\\-\\ ]" + calendar_granularity + "([\\-\\ ](long|old))?)";
    // Expressions like "last couple of months"
    private static String exp4 = "(((recent|previous|past|first|last) )?(([0-9]+|"+textual_number+"|couple of|few) )?"+calendar_granularity+"s?( (ago|later|earlier))?)";
    // Expressions like "the weekend"
    private static String exp5 = "(the "+calendar_granularity+"s?)";
    // Expressions like "mid february"
    private static String exp6 = "((early|mid|end)[\\-\\ ]("+fullmonth+"|"+calendar_granularity+"))";
    // Expressions like "the second month"
    private static String exp7 = "((the )?"+simple_ordinals+" "+calendar_granularity+")";
    // Expression like "two hours later"
    private static String exp8 = "((a|"+textual_number+") "+calendar_granularity+"s? (or so )?(earlier|later|previous|ago|since))";
    // Expression like "jan 2010"
    private static String exp9 = "("+monthspec+"\\.? ?"+year+")";
    //Expression like "the end of the month"
    private static String exp10 = "((the )(end|start|beginning|middle) of the "+calendar_granularity+")";
    //Expression like "monday morning"
    private static String exp11 = "(("+longdays+"|this) (morning|afternoon|evening|night)|tonight)";
    //Expression like "in more than sixteen hours"
    private static String exp12 = "((within|in ((more|less) than )("+vague+" )?)"+textual_number+" "+calendar_granularity+"s?)";
    //Expression like "following two hours"
    private static String exp13 = "((next|previous|last|following) (few|many|"+textual_number+") "+calendar_granularity+"s?)";
    //Expression like "around four hours"
    private static String exp14 = "("+vague+" "+textual_number+" "+calendar_granularity+"s?)";
    //Expression like "tomorrow 20:59pm"
    private static String exp15 = "(("+times+"( "+longdays+")?)|("+longdays+" "+times+"))";
    //Expression like "jan 1st"
    private static String exp16 = "("+monthspec+"\\.? [0-3]?[0-9](st|nd|rd|th)?)";
    //Expression like "1st October 2010"
    private static String exp17 = "("+numeric_days+"? "+monthspec+" "+year+"?)";
    
	/**
	 * Creates the canonical form value for a new {@link Annotation} element for
	 * the given temporal expression.
	 * 
	 * @param timeEx
	 *            the temporal expression for which the canonical form shall be created
	 * @return the canonical form value for a new {@link Annotation} element for
	 *         the given temporal expression
	 */
	public static String createCanonicalForm ( String timeEx ) {
		GregorianCalendar today = new GregorianCalendar(); 
		return createCanonicalForm( timeEx, today );
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
	public static String createCanonicalForm( String timeEx, GregorianCalendar temporalContext ) {
		String result = "";
		// Attempt RegExp-based maching
		for (TimeExMapping timeExMapping : TIME_EX_MAPPING) {	
			Matcher matcher = timeExMapping.nlPattern.matcher(timeEx.toLowerCase());
			if (matcher.matches()) {
				result = matcher.replaceAll(timeExMapping.timex2Pattern);
				result = result.replaceAll("\\[DATE\\]", DATE_FORMAT.format(temporalContext.getTime()));
				result = result.replaceAll("\\[DATETIME\\]", DATETIME_FORMAT.format(temporalContext.getTime()));
				return result;
			}
		}
		// Attempt Temporal pattern matching
        Date time = null;
        timeEx = timeEx.replace("a.c.","AD").replace("b.c.","BC");
        for ( SimpleDateFormat format : formats ) try {
                if(temporalContext!=null) format.set2DigitYearStart(temporalContext.getTime());
                if(temporalContext!=null) format.setCalendar(temporalContext);
        	    time = format.parse(timeEx);
                break;
        } catch (ParseException pE) { }
        if (time != null) result = DATE_FORMAT.format(time);
		return result;
	}
	
	/**
	 * A mapping between the NL pattern of a temporal expression and the
	 * corresponding TIMEX2 pattern.
	 */
	private static class TimeExMapping {

		/** the pattern of a NL temporal expression */
		public final Pattern nlPattern;

		/**
		 * a replacement string according to the specification of
		 * {@link String#replaceAll(String, String)} which is used to create
		 * canonical form values for {@link Annotation} elements for temporal
		 * expressions that are found with the {@link #nlPattern}
		 */
		public final String timex2Pattern;

		/**
		 * Constructs a {@link TimeExMapping} for the two given values.
		 * 
		 * @param nlPattern
		 *            the pattern of a NL temporal expression
		 * @param timex2Pattern
		 *            a replacement string according to the specification of
		 *            {@link String#replaceAll(String, String)} which is used to
		 *            create canonical form values for {@link Annotation}
		 *            elements for temporal expressions that are found with the
		 *            given NL pattern
		 */
		public TimeExMapping(String nlPattern, String timex2Pattern) {
			this.nlPattern = Pattern.compile(nlPattern);
			this.timex2Pattern = timex2Pattern;
		}

	}
	
}