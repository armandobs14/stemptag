package temporal;

import org.joda.time.*;

import java.util.ArrayList;
import java.util.HashMap;

public class CandidateCreation {
	
	private static HashMap<String,Integer> months = new HashMap<String,Integer>();
	private static HashMap<String,Integer> weekDays = new HashMap<String,Integer>();
	private static HashMap<String,Integer> days = new HashMap<String,Integer>();
	private static HashMap<String,Integer> ordinals = new HashMap<String,Integer>();
	public static DateTime docCreationTime;
	public static ArrayList<Long> candidatesMillisecondsSameDoc = new ArrayList<Long>();
	public static ArrayList<Interval> candidatesIntervalsSameDoc = new ArrayList<Interval>();
	public static ArrayList<Long> candidatesMillisecondsSameSentence = new ArrayList<Long>();
	public static ArrayList<Interval> candidatesIntervalsSameSentence = new ArrayList<Interval>();
	public static boolean isXMLflag;
	//V se a frase tem um ponto final no fim
	public static boolean flag;
	public static boolean isFirstTimex;
	public static boolean isNull;
	public static int numberCandidatesTimex;
	public static boolean docCreationTimePlainText;
	public static int granularityDuration;//9->Vague
	public static int past_future_present_null_ref;//4->past 5->present  6->future 7->null 1->Recurrences
	
	public static void init(){
		
		docCreationTime = null;
		isXMLflag = false;
		isFirstTimex = true;
		isNull = false;
		flag = true;
		numberCandidatesTimex = 0;
		docCreationTimePlainText = false;
		granularityDuration = 0;
		past_future_present_null_ref = 0;
		
		months.put("january",1);
    	months.put("february",2);
    	months.put("march",3);
    	months.put("april",4);
    	months.put("may",5);
    	months.put("june",6);
    	months.put("july",7);
    	months.put("august",8);
    	months.put("september",9);
		months.put("october",10);
		months.put("november",11);
		months.put("december",12);
		months.put("jan",1);
		months.put("feb",2);
		months.put("mar",3);
		months.put("apr",4);
		months.put("may",5);
		months.put("jun",6);
		months.put("jul",7);
		months.put("aug",8);
		months.put("sep",9);
		months.put("sept",9);
		months.put("oct",10);
		months.put("nov",11);
		months.put("dec",12);

    	weekDays.put("monday", 1);
		weekDays.put("tuesday", 2);
		weekDays.put("wednesday", 3);
		weekDays.put("thursday", 4);
		weekDays.put("friday", 5);
		weekDays.put("saturday", 6);
		weekDays.put("sunday", 7);
		weekDays.put("mon", 1);
		weekDays.put("tue", 2);
		weekDays.put("wed", 3);
		weekDays.put("thu", 4);
		weekDays.put("fri", 5);
		weekDays.put("sat", 6);
		weekDays.put("sun", 7);
		weekDays.put("tomorrow", 1);
		weekDays.put("yester", -1);
		weekDays.put("yesterday", -1);
		
		days.put("a", 1);
		days.put("one", 1);
		days.put("two", 2);
		days.put("three", 3);
		days.put("four", 4);
		days.put("five", 5);
		days.put("six", 6);
		days.put("seven", 7);
		days.put("eight", 8);
		days.put("nine", 9);
		days.put("ten", 10);
		days.put("eleven", 11);
		days.put("twelve", 12);
		days.put("thirteen", 13);
		days.put("fourteen", 14);
		days.put("fifteen", 15);
		days.put("sixteen", 16);
		days.put("seventeen", 17);
		days.put("eighteen", 18);
		days.put("nineteen", 19);
		days.put("twenty", 20);
		days.put("twenty one", 21);
		days.put("twenty two", 22);
		days.put("twenty three", 23);
		days.put("twenty four", 24);
		days.put("hundred", 100);
		
		ordinals.put("first", 1);
		ordinals.put("second", 2);
		ordinals.put("third", 3);
		ordinals.put("fourth", 4);
		ordinals.put("fifth", 5);
		ordinals.put("sixth", 6);
		ordinals.put("seventh", 7);
		ordinals.put("eighth", 8);
		ordinals.put("ninth", 9);
		ordinals.put("tenth", 10);		
		
    }
    
	    
	// Strings to compose complex dates
	private static String fullyear = "1[0-9]{3}|20[0-5][0-9]";
	private static String shortyear = "'?[0-9]{2}";
	private static String calendar_granularity = "(second|minute|hour|day|weekend|week|month|quarter|year|decade|centuries|century)s?";
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
	private static String times = "([012][0-9]([\\:\\.][0-5][0-9])? ?(pm|am|hr)?)";
	
	
	public static Interval normalizeTimex ( String timeEx, DateTime contextDate ) {
		DateTime date = new DateTime(contextDate);
		int diaMes = date.getDayOfMonth();
		int diaSemana = date.getDayOfWeek();
		int numeroAno = date.getYear();
		int mesAno = date.getMonthOfYear();
		

		if (timeEx.toLowerCase().matches(exp1)){//"(((early|late|earlier|later|the) )?((this|next|last|following|in) )?("+calendar_granularity+"|"+longdays+"|"+monthspec+"))";
			String[] split = timeEx.split(" ");

			if (timeEx.toLowerCase().equals("today"))
				return date.dayOfMonth().toInterval();
			if (timeEx.toLowerCase().matches(longdays)){
				if (timeEx.toLowerCase().matches("yesterday")){
					date = date.minusDays(1);
					return date.dayOfMonth().toInterval();
				}
				
				date = date.minusDays(diaSemana);
				date = date.plusDays(weekDays.get(timeEx.toLowerCase()));

				return date.dayOfMonth().toInterval();
			}
			if (timeEx.toLowerCase().matches(monthspec)){
				date = date.monthOfYear().setCopy(months.get(timeEx.toLowerCase()));
				return date.monthOfYear().toInterval();
			}
			
				if (timeEx.matches(calendar_granularity)){
					if (split[0].toLowerCase().matches("seconds?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 91;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[0].toLowerCase().matches("minutes?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 92;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[0].toLowerCase().matches("hours?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 93;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[0].toLowerCase().matches("days?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 94;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[0].toLowerCase().matches("weeks?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 95;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[0].toLowerCase().matches("months?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 96;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[0].toLowerCase().matches("years?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 97;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[0].toLowerCase().matches("decades?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 98;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[2].toLowerCase().matches("centuries")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 100;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[2].toLowerCase().matches("century")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 101;
						return date.millisOfSecond().toInterval(); 
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					} 
				}
				
			if (split.length == 3){
				if(split[1].equals("following") || split[1].equals("next")){
					if (split[2].toLowerCase().matches(longdays)){						
						if (diaSemana < weekDays.get(split[2].toLowerCase())){
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[2].toLowerCase()));
						}
						else{
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[2].toLowerCase()));
							date = date.plusDays(7);
						}
						return date.dayOfMonth().toInterval();
					}
					else if (split[2].toLowerCase().matches(calendar_granularity)){
						if (split[2].toLowerCase().matches("day")){
							date = date.plusDays(1);
							date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0, 0, 0);
							return date.dayOfMonth().toInterval();
						}
						else if (split[2].toLowerCase().matches("hour")){
							date = date.plusHours(1);
							date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), 0, 0, 0);
							return date.hourOfDay().toInterval();
						}
						else if (split[2].toLowerCase().matches("week")){	
							date = date.plusDays(diaMes-diaSemana+7);
							return date.weekOfWeekyear().toInterval();
						}
						else if (split[2].toLowerCase().matches("weekend")){
							if (diaSemana == 6 || diaSemana == 7){
								date = date.minusDays(diaSemana);
								date = date.plusDays(13);
							}
							else{
								date = date.minusDays(diaSemana);
								date = date.plusDays(6);
							}
							int dia = date.getDayOfMonth()+2;
							return new Interval(new DateTime(date.toString("YYYY-MM-dd'T'00:00:00.000")), new DateTime(date.toString("YYYY-MM-"+dia+"'T'00:00:00.000")));
						}
						else if (split[2].toLowerCase().matches("month")){
							date = date.plusMonths(1);
							return date.monthOfYear().toInterval();
						}
						else if (split[2].toLowerCase().matches("year")){
							date = date.plusYears(1);
							return date.year().toInterval();
						}
						else if (split[2].toLowerCase().matches("minute")){
							date = date.plusMinutes(1);
							return date.minuteOfHour().toInterval();
						}
						else if (split[2].toLowerCase().matches("seconds?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 91;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("minutes?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 92;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("hours?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 93;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("days?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 94;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("weeks?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 95;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("months?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 96;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("years?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 97;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("decades?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 98;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("centuries")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 100;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("century")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 101;
							return date.millisOfSecond().toInterval();
						}
						else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						}
	
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					}
				}
				else if (split[1].equals("last") || split[1].equals("previous")){
					if (split[2].toLowerCase().matches(longdays)){
						if (diaSemana > weekDays.get(split[2].toLowerCase())){
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[2].toLowerCase()));
						}
						else{
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[2].toLowerCase()));
							date = date.minusDays(7);
						}
						return date.dayOfMonth().toInterval();
					}
					else if (split[2].toLowerCase().matches(calendar_granularity)){
						if (split[2].toLowerCase().matches("day")){
							date = date.minusDays(1);
							return date.dayOfMonth().toInterval();
						}
						else if (split[2].toLowerCase().matches("hour")){
							date = date.minusHours(1);
							return date.hourOfDay().toInterval();
						}
						else if (split[2].toLowerCase().matches("week")){
							if (diaSemana == 6 || diaSemana == 7){
								date = date.minusDays(diaSemana-1);
							}
							else{
								date = date.minusDays(diaSemana-1);
								date = date.minusDays(7);
							}
							return new Interval(new DateTime(date.toString("YYYY-MM-dd'T'00:00:00.000")), new DateTime(date.plusDays(5).toString("YYYY-MM-dd'T'00:00:00.000")));
						}
						else if (split[2].toLowerCase().matches("weekend")){
							date = date.minusDays(diaSemana+1);
							return new Interval(new DateTime(date.toString("YYYY-MM-dd'T'00:00:00.000")), new DateTime(date.plusDays(2).toString("YYYY-MM-dd'T'00:00:00.000")));
						}
						else if (split[2].toLowerCase().matches("month")){
							date = date.minusMonths(1);
							return date.monthOfYear().toInterval();
						}
						else if (split[2].toLowerCase().matches("year")){
							date = date.minusYears(1);
							return date.year().toInterval();
						}
						else if (split[2].toLowerCase().matches("minute")){
							date = date.minusMinutes(1);
							return date.minuteOfHour().toInterval();
						}
						else if (split[2].toLowerCase().matches("seconds?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 91;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("minutes?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 92;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("hours?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 93;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("days?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 94;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("weeks?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 95;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("months?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 96;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("years?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 97;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("decades?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 98;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("centuries")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 100;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("century")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 101;
							return date.millisOfSecond().toInterval();
						}
						else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						}
					}
					
				}
				else if (split[1].toLowerCase().equals("this") || split[0].toLowerCase().equals("early") || split[0].toLowerCase().equals("late") || split[0].toLowerCase().equals("earlier") || split[0].toLowerCase().equals("later")){
	    			if (split[2].toLowerCase().matches(longdays)){
	    				if (diaSemana > weekDays.get(split[2].toLowerCase())){
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[2].toLowerCase()));
						}
						else{
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[2].toLowerCase()));
							date = date.minusDays(7);
						}
						return date.dayOfMonth().toInterval();
	    			}
	    			else if (split[2].toLowerCase().matches(calendar_granularity)){
						if (split[2].toLowerCase().matches("day")){
							return date.dayOfMonth().toInterval();
						}
						else if (split[2].toLowerCase().matches("hour")){
							return date.hourOfDay().toInterval();
						}
						else if (split[2].toLowerCase().matches("week")){
							date = date.minusDays(diaSemana-1);
							return new Interval(new DateTime(date.toString("YYYY-MM-dd'T'00:00:00.000")), new DateTime(date.plusDays(5).toString("YYYY-MM-dd'T'00:00:00.000")));
						}
						else if (split[2].toLowerCase().matches("weekend")){
							date = date.minusDays(diaSemana+1);
							return new Interval(new DateTime(date.toString("YYYY-MM-dd'T'00:00:00.000")), new DateTime(date.plusDays(2).toString("YYYY-MM-dd'T'00:00:00.000")));
						}
						else if (split[2].toLowerCase().matches("month")){
							return date.monthOfYear().toInterval();
						}
						else if (split[2].toLowerCase().matches("year")){
							return date.year().toInterval();
						}
						else if (split[2].toLowerCase().matches("minute")){
							return date.minuteOfHour().toInterval();
						}
						else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						}
					}
				}
				else {
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				}
			}
			else if (split.length == 2){
				if(split[0].equals("following") || split[0].equals("next")){
					if (split[1].toLowerCase().matches(longdays)){
						if (diaSemana < weekDays.get(split[1].toLowerCase())){
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[1].toLowerCase()));
						}
						else{
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[1].toLowerCase())+7);
						}
						
						return date.dayOfMonth().toInterval();

					}
					else if (split[1].toLowerCase().matches(calendar_granularity)){
						if (split[1].toLowerCase().matches("second")){
							date = date.plusSeconds(1);
							return date.secondOfMinute().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("minute")){
							date = date.plusMinutes(1);
							return date.minuteOfHour().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("hour")){
							date = date.plusHours(1);
							return date.hourOfDay().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("day")){
							date = date.plusDays(1);
							return date.dayOfMonth().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("month")){
							date = date.plusMonths(1);
							return date.monthOfYear().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("year")){
							date = date.plusYears(1);
							return date.year().toInterval(); 
						}
						if (split[1].toLowerCase().matches("seconds")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 91;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("minutes")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 92;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("hours")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 93;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("days")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 94;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("weeks")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 95;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("months")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 96;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("years")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 97;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("decades")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 98;
							return date.millisOfSecond().toInterval(); 
						}
						else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						}						
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					}
				}
				else if (split[0].equals("last")){
					if (split[1].toLowerCase().matches(longdays)){
						if (diaSemana > weekDays.get(split[1].toLowerCase())){
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[1].toLowerCase()));
						}
						else{
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[1].toLowerCase()));
							date = date.minusDays(7);
						}
						
						return date.dayOfMonth().toInterval();
					}
					else if(split[1].toLowerCase().matches(monthspec)){
						int mes = months.get(split[1].toLowerCase());
						if (mes >= date.getMonthOfYear()){
							date = date.minusYears(1);
							date = date.monthOfYear().setCopy(mes);
							return date.monthOfYear().toInterval();
						}
						else{
							date = date.monthOfYear().setCopy(mes);
							return date.monthOfYear().toInterval();
						}
					}
					else if(split[1].toLowerCase().matches("month")){
						date = date.minusMonths(1);
						return date.monthOfYear().toInterval();
					}
					else if(split[1].toLowerCase().matches("year")){
						date = date.minusYears(1);
						return date.year().toInterval();
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					}
				}
				else if (split[0].equals("this") || split[0].equals("early") || split[0].equals("late") || split[0].equals("earlier") || split[0].equals("later")){
	    			if (split[1].toLowerCase().matches(longdays)){
	    				if (split[1].toLowerCase().matches("yesterday")){
	    					date = date.minusDays(1);
	    					return date.dayOfMonth().toInterval();
	    				}
	    				if (diaSemana > weekDays.get(split[2].toLowerCase())){
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[2].toLowerCase()));
						}
						else{
							date = date.minusDays(diaSemana);
							date = date.plusDays(weekDays.get(split[2].toLowerCase()));
							date = date.minusDays(7);
						}
	    				
	    				return date.dayOfMonth().toInterval();
	    			}
	    			else if (split[1].toLowerCase().matches(calendar_granularity)){
	    				if (split[1].toLowerCase().matches("minutes?")){
	    					return date.minuteOfHour().toInterval();
	    				}
	    				else if (split[1].toLowerCase().matches("hours?")){
	    					return date.hourOfDay().toInterval();
	    				}
	    				else if (split[1].toLowerCase().matches("days?")){
	    					return date.dayOfMonth().toInterval(); 
	    				}
	    				else if (split[1].toLowerCase().matches("months?")){
	    					return date.monthOfYear().toInterval();
	    				}
	    				else if (split[1].toLowerCase().matches("years?")){
	    					return date.year().toInterval();
	    				}
	    				else if (split[1].toLowerCase().matches("weekend")){
	    					date = date.minusDays(diaSemana);
	    					date = date.plusDays(6);
							return new Interval(new DateTime(date.toString("YYYY-MM-dd'T'00:00:00.000")), new DateTime(date.plusDays(2).toString("YYYY-MM-dd'T'00:00:00.000")));
	    				}
	    				else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						}
	    			}
	    			else if (split[1].toLowerCase().matches(monthspec)){
	    				date = date.monthOfYear().setCopy(months.get(split[1].toLowerCase()));
	    				return date.monthOfYear().toInterval();
	    			}
	    			else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					}
	    				
	    			}
					else if(split[0].toLowerCase().matches("the")){
						if (split[1].toLowerCase().matches(calendar_granularity)){
							if (split[1].toLowerCase().matches("minute"))
								return date.minuteOfHour().toInterval();
							else if (split[1].toLowerCase().matches("hour"))
								return date.hourOfDay().toInterval();
							else if (split[1].toLowerCase().matches("day"))
								return date.dayOfMonth().toInterval();
							else if (split[1].toLowerCase().matches("month"))
								return date.monthOfYear().toInterval();
							else if (split[1].toLowerCase().matches("year"))
								return date.year().toInterval();
							else {
								System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
								return null;
							}
						}
						else{
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						}
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
				}
			}
				
		}
		else if (timeEx.toLowerCase().matches(exp2)){
			String[] split = timeEx.split(" ");

			int numeroMes;
			int numeroAnoAux;
			if (split.length == 4){
				if(split[2].matches("[0-9]{1,2}")) 
					numeroMes = Integer.parseInt(split[2]);
				else 
					numeroMes = months.get(split[2].toLowerCase());
				
				if(split[3].matches("[0-9]{2}"))
					numeroAnoAux = Integer.parseInt(split[3])+1900;
				else
					numeroAnoAux = Integer.parseInt(split[3]);
				
				date = new DateTime(numeroAnoAux, numeroMes, Integer.parseInt(split[1]), 0, 0, 0, 0);
				
				return date.dayOfMonth().toInterval();
			}
			else if (split.length == 3){
				if(split[1].matches("[0-9]{1,2}")) 
					numeroMes = Integer.parseInt(split[1]);
				else 
					numeroMes = months.get(split[1].toLowerCase());
				
				if(split[2].matches("[0-9]{2}"))
					numeroAnoAux = Integer.parseInt(split[2])+1900;
				else
					numeroAnoAux = Integer.parseInt(split[2]);
				
				date = new DateTime(numeroAnoAux, numeroMes, Integer.parseInt(split[0]), 0, 0, 0, 0);
				
				return date.dayOfMonth().toInterval();
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}
				
		}
		else if (timeEx.toLowerCase().matches(exp3)){//"((under )|(over )|(near )|(next )|(previous )|(last )|(following )|(a ))?((few)|(many)|("+textual_number+")|("+numeric_days+"))[\\-\\ ]"+calendar_granularity+"([\\-\\ ](long|old))?";
			String[] split = timeEx.split(" ");

			if (split.length == 1){
				String[] split2 = timeEx.split("-");
				if (split2[0].toLowerCase().matches(calendar_granularity)){
					if (split2[0].toLowerCase().matches("seconds?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 1;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[0].toLowerCase().matches("minutes?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 2;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[0].toLowerCase().matches("hours?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 3;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[0].toLowerCase().matches("days?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 4;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[0].toLowerCase().matches("weeks?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 5;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[0].toLowerCase().matches("months?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 6;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[0].toLowerCase().matches("years?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 7;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[0].toLowerCase().matches("decades?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 8;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[0].toLowerCase().matches("centuries")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[0].toLowerCase().matches("century")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					}
				}
				else if (split2[0].toLowerCase().matches(textual_number) && split2[1].toLowerCase().matches(calendar_granularity)){
					if (split2[1].toLowerCase().matches("seconds?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split2[0].toLowerCase()));
						CandidateCreation.granularityDuration = 1;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[1].toLowerCase().matches("minutes?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split2[0].toLowerCase()));
						CandidateCreation.granularityDuration = 2;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[1].toLowerCase().matches("hours?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split2[0].toLowerCase()));
						CandidateCreation.granularityDuration = 3;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[1].toLowerCase().matches("days?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split2[0].toLowerCase()));
						CandidateCreation.granularityDuration = 4;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[1].toLowerCase().matches("weeks?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split2[0].toLowerCase()));
						CandidateCreation.granularityDuration = 5;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[1].toLowerCase().matches("months?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split2[0].toLowerCase()));
						CandidateCreation.granularityDuration = 6;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[1].toLowerCase().matches("years?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split2[0].toLowerCase()));
						CandidateCreation.granularityDuration = 7;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[1].toLowerCase().matches("decades?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split2[0].toLowerCase()));
						CandidateCreation.granularityDuration = 8;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("centuries")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split2[0].toLowerCase()));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split2[1].toLowerCase().matches("century")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split2[0].toLowerCase()));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					} 
				}
			}
				
			if (split.length == 2){
				if (split[0].toLowerCase().matches(textual_number)){
					if (split[1].toLowerCase().matches("seconds?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[0].toLowerCase()));
						CandidateCreation.granularityDuration = 1;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("minutes?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[0].toLowerCase()));
						CandidateCreation.granularityDuration = 2;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("hours?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[0].toLowerCase()));
						CandidateCreation.granularityDuration = 3;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("days?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[0].toLowerCase()));
						CandidateCreation.granularityDuration = 4;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("weeks?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[0].toLowerCase()));
						CandidateCreation.granularityDuration = 5;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("months?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[0].toLowerCase()));
						CandidateCreation.granularityDuration = 6;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("years?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[0].toLowerCase()));
						CandidateCreation.granularityDuration = 7;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("decades?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[0].toLowerCase()));
						CandidateCreation.granularityDuration = 8;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("centuries")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[0].toLowerCase()));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("century")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[0].toLowerCase()));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					} 
				}
				else if (split[0].toLowerCase().matches(numeric_days)){
					if (split[1].toLowerCase().matches("seconds?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 1;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("minutes?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 2;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("hours?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 3;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("days?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 4;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("weeks?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 5;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("months?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 6;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("years?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 7;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("decades?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 8;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("centuries")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("century")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					} 
				}
				else if (split[0].toLowerCase().matches("(few|many)")){

					if (split[1].toLowerCase().matches(calendar_granularity)){
						if (split[1].toLowerCase().matches("seconds?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 91;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("minutes?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 92;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("hours?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 93;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("days?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 94;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("weeks?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 95;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("months?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 96;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("years?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 97;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("decades?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 98;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("centuries")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 100;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("century")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 101;
							return date.millisOfSecond().toInterval(); 
						}
						else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						} 
					}
				}
				else if (split[0].toLowerCase().matches("(next|previous|following|per)")){
					if (split[1].toLowerCase().matches(calendar_granularity)){
						if (split[1].toLowerCase().matches("seconds?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 1;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("minutes?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 2;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("hours?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 3;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("days?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 4;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("weeks?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 5;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("months?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 6;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("years?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 7;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("decades?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 8;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("centuries")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 100;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("century")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
							CandidateCreation.granularityDuration = 101;
							return date.millisOfSecond().toInterval(); 
						}
						else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						} 
					}
				}
			}
			else if (split.length == 3){
				if (split[0].toLowerCase().matches("(under|over|near|a|next|previous|following)")){
					if (split[1].toLowerCase().matches(textual_number)){
						if (split[2].toLowerCase().matches("seconds?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
							CandidateCreation.granularityDuration = 1;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("minutes?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
							CandidateCreation.granularityDuration = 2;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("hours?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
							CandidateCreation.granularityDuration = 3;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("days?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
							CandidateCreation.granularityDuration = 4;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("weeks?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
							CandidateCreation.granularityDuration = 5;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("months?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
							CandidateCreation.granularityDuration = 6;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("years?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
							CandidateCreation.granularityDuration = 7;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("decades?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
							CandidateCreation.granularityDuration = 8;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("centuries")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
							CandidateCreation.granularityDuration = 10;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("century")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
							CandidateCreation.granularityDuration = 10;
							return date.millisOfSecond().toInterval(); 
						}
						else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						} 
					}
					else if (split[1].toLowerCase().matches(numeric_days)){
						if (split[2].toLowerCase().matches("seconds?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
							CandidateCreation.granularityDuration = 1;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("minutes?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
							CandidateCreation.granularityDuration = 2;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("hours?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
							CandidateCreation.granularityDuration = 3;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("days?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
							CandidateCreation.granularityDuration = 4;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("weeks?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
							CandidateCreation.granularityDuration = 5;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("months?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
							CandidateCreation.granularityDuration = 6;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("years?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
							CandidateCreation.granularityDuration = 7;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[2].toLowerCase().matches("decades?")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
							CandidateCreation.granularityDuration = 8;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("centuries")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
							CandidateCreation.granularityDuration = 10;
							return date.millisOfSecond().toInterval(); 
						}
						else if (split[1].toLowerCase().matches("century")){
							date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
							CandidateCreation.granularityDuration = 10;
							return date.millisOfSecond().toInterval(); 
						}
						else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						} 
					}
					else if(split[1].toLowerCase().matches("(few|many)")){//((over )|(near )|(next )|(previous )|(last )|(following )|(a ))
						if (split[2].toLowerCase().matches(calendar_granularity)){
							if (split[2].toLowerCase().matches("seconds?")){
								date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
								CandidateCreation.granularityDuration = 91;
								return date.millisOfSecond().toInterval(); 
							}
							else if (split[2].toLowerCase().matches("minutes?")){
								date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
								CandidateCreation.granularityDuration = 92;
								return date.millisOfSecond().toInterval(); 
							}
							else if (split[2].toLowerCase().matches("hours?")){
								date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
								CandidateCreation.granularityDuration = 93;
								return date.millisOfSecond().toInterval(); 
							}
							else if (split[2].toLowerCase().matches("days?")){
								date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
								CandidateCreation.granularityDuration = 94;
								return date.millisOfSecond().toInterval(); 
							}
							else if (split[2].toLowerCase().matches("weeks?")){
								date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
								CandidateCreation.granularityDuration = 95;
								return date.millisOfSecond().toInterval(); 
							}
							else if (split[2].toLowerCase().matches("months?")){
								date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
								CandidateCreation.granularityDuration = 96;
								return date.millisOfSecond().toInterval(); 
							}
							else if (split[2].toLowerCase().matches("years?")){
								date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
								CandidateCreation.granularityDuration = 97;
								return date.millisOfSecond().toInterval(); 
							}
							else if (split[2].toLowerCase().matches("decades?")){
								date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
								CandidateCreation.granularityDuration = 98;
								return date.millisOfSecond().toInterval(); 
							}
							else if (split[2].toLowerCase().matches("centuries")){
								date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
								CandidateCreation.granularityDuration = 100;
								return date.millisOfSecond().toInterval(); 
							}
							else if (split[2].toLowerCase().matches("century")){
								date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
								CandidateCreation.granularityDuration = 101;
								return date.millisOfSecond().toInterval(); 
							}
							else {
								System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
								return null;
							} 
						}
						else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						} 
					}
				}
				else if (split[0].toLowerCase().matches(numeric_days)){
					if (split[1].toLowerCase().matches("seconds?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 1;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("minutes?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 2;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("hours?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 3;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("days?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 4;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("weeks?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 5;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("months?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 6;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("years?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 7;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("decades?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 8;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("centuries")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("century")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					}
				}
				else if (split[0].toLowerCase().matches(textual_number)){
					if (split[1].toLowerCase().matches("seconds?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 1;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("minutes?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 2;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("hours?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 3;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("days?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 4;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("weeks?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 5;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("months?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 6;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("years?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 7;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("decades?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 8;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("centuries")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("century")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[0]));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					}
				}
			}
			else if (split.length == 4){
				if (split[2].toLowerCase().matches(textual_number)){
					if (split[3].toLowerCase().matches("seconds?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[2].toLowerCase()));
						CandidateCreation.granularityDuration = 1;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("minutes?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[2].toLowerCase()));
						CandidateCreation.granularityDuration = 2;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("hours?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[2].toLowerCase()));
						CandidateCreation.granularityDuration = 3;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("days?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[2].toLowerCase()));
						CandidateCreation.granularityDuration = 4;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("weeks?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[2].toLowerCase()));
						CandidateCreation.granularityDuration = 5;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("months?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[2].toLowerCase()));
						CandidateCreation.granularityDuration = 6;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("years?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[2].toLowerCase()));
						CandidateCreation.granularityDuration = 7;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("decades?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[2].toLowerCase()));
						CandidateCreation.granularityDuration = 8;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("centuries")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[2].toLowerCase()));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("century")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[2].toLowerCase()));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					} 
				}
				else if (split[2].toLowerCase().matches(numeric_days)){
					if (split[3].toLowerCase().matches("seconds?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[2]));
						CandidateCreation.granularityDuration = 1;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("minutes?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[2]));
						CandidateCreation.granularityDuration = 2;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("hours?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[2]));
						CandidateCreation.granularityDuration = 3;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("days?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[2]));
						CandidateCreation.granularityDuration = 4;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("weeks?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[2]));
						CandidateCreation.granularityDuration = 5;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("months?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[2]));
						CandidateCreation.granularityDuration = 6;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("years?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[2]));
						CandidateCreation.granularityDuration = 7;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("decades?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[2]));
						CandidateCreation.granularityDuration = 8;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("centuries")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[2]));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[3].toLowerCase().matches("century")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[2]));
						CandidateCreation.granularityDuration = 10;
						return date.millisOfSecond().toInterval(); 
					}
					else {
						System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
						return null;
					} 
				}
			}
			else if (split.length == 5){
				if (split[1].toLowerCase().matches(numeric_days) && split[4].toLowerCase().matches(calendar_granularity)){
					if (split[4].toLowerCase().matches("month")){
						date = date.dayOfMonth().setCopy(Integer.parseInt(split[1].substring(0, split[1].length()-2)));
						return date.dayOfMonth().toInterval();
					}
				}
				else {
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				} 
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}
		}
		else if (timeEx.toLowerCase().matches(exp4)){
			String[] split = timeEx.split(" ");
			System.out.println("4");
			return null;
		}
		else if (timeEx.toLowerCase().matches(exp5)){
			String[] split = timeEx.split(" ");
			
			if (split.length == 3){
				if (split[2].matches("day"))
					return date.dayOfMonth().toInterval();
				else if (split[2].matches("month"))
					return date.monthOfYear().toInterval();
				else if (split[2].matches("year"))
					return date.year().toInterval();
				else if (split[2].matches("minute"))
					return date.minuteOfHour().toInterval();
				else if (split[2].matches("time"))
					return date.year().toInterval();
				else {
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				} 
			}
			else if (split.length == 2){
				if (split[1].toLowerCase().matches(calendar_granularity)){
					if (split[1].toLowerCase().matches("seconds?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 91;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("minutes?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 92;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("hours?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 93;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("days?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 94;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("weeks?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 95;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("months?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 96;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("years?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 97;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("decades?")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 98;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("centuries")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 100;
						return date.millisOfSecond().toInterval(); 
					}
					else if (split[1].toLowerCase().matches("century")){
						date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(1);
						CandidateCreation.granularityDuration = 101;
						return date.millisOfSecond().toInterval(); 
					}
				}
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}
			
			return null;
		}
		else if (timeEx.toLowerCase().matches(exp6)){//"(early|mid|end|late)[\\-\\ ](in )?("+monthspec+"|"+calendar_granularity+") ?"+fullyear+"?";
			String[] split = timeEx.split(" ");
			
			if (split.length == 3){
				if (split[1].toLowerCase().matches(monthspec) && split[2].matches(fullyear)){
					date = new DateTime(Integer.parseInt(split[2]), months.get(split[1].toLowerCase()), 1, 0, 0, 0, 0);
					return date.monthOfYear().toInterval();
				}
				else if (split[2].toLowerCase().matches(monthspec)){
					date = new DateTime(numeroAno, months.get(split[2].toLowerCase()), 1, 0, 0, 0, 0);
					return date.monthOfYear().toInterval();
				}
				else if (split[2].toLowerCase().matches(fullyear)){
					date = new DateTime(Integer.parseInt(split[2]), 1, 1, 0, 0, 0, 0);
					return date.year().toInterval();
				}
				else {
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				}
			}
			else if (split.length == 2){
				if (split[1].toLowerCase().matches(fullyear))
					date = new DateTime(Integer.parseInt(split[1]), 1, 1, 0, 0, 0, 0);
					return date.year().toInterval();
			}
			
			if (split.length < 2)
				split = timeEx.split("-");
			
			if (split[1].toLowerCase().matches(monthspec)){
				date = new DateTime(numeroAno, months.get(split[1].toLowerCase()), 1, 0, 0, 0, 0);
				return date.monthOfYear().toInterval();
			}
			else if (split[1].toLowerCase().matches(calendar_granularity)){
				if (split[1].toLowerCase().matches("months?")){
					date = new DateTime(numeroAno, mesAno, 0, 0, 0, 0, 0);
					return date.monthOfYear().toInterval();
				}
				else if (split[1].toLowerCase().matches("years?")){
					date = new DateTime(numeroAno, 0, 0, 0, 0, 0, 0);
					return date.year().toInterval();
				}
				else {
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				}
			}
			else if (split[1].toLowerCase().matches(year)){
				date = new DateTime(Integer.parseInt(split[1]), 1, 1, 0, 0, 0, 0);
				return date.year().toInterval();
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}
			
		}
		else if (timeEx.toLowerCase().matches(exp7)){//"((the )?"+simple_ordinals+" "+calendar_granularity+")";
			String[] split = timeEx.split(" ");
			
			if(!split[0].toLowerCase().contains("the")){
				if(split[1].toLowerCase().matches("days?")){
					int ordinal = ordinals.get(split[0].toLowerCase());
					date = new DateTime(numeroAno, mesAno, ordinal, 0, 0, 0, 0);
					return date.dayOfMonth().toInterval();
				}
				else if(split[1].toLowerCase().matches("months?")){
					int ordinal = ordinals.get(split[0].toLowerCase());
					date = new DateTime(numeroAno, ordinal, 0, 0, 0, 0, 0);
					return date.monthOfYear().toInterval();
				}
				else {
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				}
			}
			else if (split.length == 3){
				if (split[2].toLowerCase().matches("day")){
					date = date.plusDays(ordinals.get(split[1].toLowerCase()));
					return date.dayOfMonth().toInterval();
				}
				else if(split[2].toLowerCase().matches("week")){
					date = date.minusDays(diaSemana);
					date = date.plusDays(ordinals.get(split[1].toLowerCase())*7+1);
					return new Interval(new DateTime(date.toString("YYYY-MM-dd'T'00:00:00.000")), new DateTime(date.plusDays(5).toString("YYYY-MM-dd'T'00:00:00.000")));
				}
				else if(split[2].toLowerCase().matches("weekend")){
					date = date.minusDays(diaSemana);
					date = date.plusDays(ordinals.get(split[1].toLowerCase())*7-1);
					return new Interval(new DateTime(date.toString("YYYY-MM-dd'T'00:00:00.000")), new DateTime(date.plusDays(2).toString("YYYY-MM-dd'T'00:00:00.000")));
				}
				else if(split[2].toLowerCase().matches("month")){
					date = date.plusMonths(ordinals.get(split[1].toLowerCase()));
					return date.monthOfYear().toInterval();
				}
				else if(split[2].toLowerCase().matches("year")){
					date = date.plusYears(ordinals.get(split[1].toLowerCase()));
					return date.year().toInterval();
				}
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}
		}
		else if (timeEx.toLowerCase().matches(exp8)){
			String[] split = timeEx.split(" ");

				if(split[0].toLowerCase().matches(textual_number)){
					if(split[2].toLowerCase().matches("earlier") || split[2].toLowerCase().matches("ago")){
						if(split[1].toLowerCase().matches("minutes?")){
							date = date.minusMinutes(days.get(split[0].toLowerCase()));
							return date.minuteOfHour().toInterval();
						}
						else if(split[1].toLowerCase().matches("hours?")){
							date = date.minusHours(days.get(split[0].toLowerCase()));
							return date.hourOfDay().toInterval();
						}
						else if(split[1].toLowerCase().matches("years?")){
							date = date.minusYears(days.get(split[0].toLowerCase()));
							return date.year().toInterval();
						}
						else if(split[1].toLowerCase().matches("months?")){
							date = date.minusMonths(days.get(split[0].toLowerCase()));
							return date.monthOfYear().toInterval();
						}
						else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						}
					}
					else if(split[2].toLowerCase().matches("later")){
						if(split[1].toLowerCase().matches("minutes?")){
							date.plusMinutes(days.get(split[0].toLowerCase()));
							return date.minuteOfHour().toInterval();
						}
						else if(split[1].toLowerCase().matches("hours?")){
							date = date.plusHours(days.get(split[0].toLowerCase()));
							return date.hourOfDay().toInterval();
						}
						else if(split[1].toLowerCase().matches("years?")){
							date = date.plusYears(days.get(split[0].toLowerCase()));
							return date.year().toInterval();
						}
						else if(split[1].toLowerCase().matches("months?")){
							date = date.plusMonths(days.get(split[0].toLowerCase()));
							return date.monthOfYear().toInterval();
						}
						else {
							System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
							return null;
						}
					}
				}
				else {
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				}
		}
		else if (timeEx.toLowerCase().matches(exp9)){//"([0-9]{1,2}-)?"+year+"(-[0-9]{1,2})?(-[0-9]{1,2})?(t[0-2][0-9]:[0-6][0-9])?";
			String[] split = timeEx.split("-");
			
			if (timeEx.toLowerCase().contains("t") && timeEx.toLowerCase().contains(":")){
				String[] split2 = split[2].split("T");
				String[] split3 = split2[1].split(":");
				date = new DateTime(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split2[0]), Integer.parseInt(split3[0]), Integer.parseInt(split3[1]), 0, 0);
				return date.minuteOfHour().toInterval();
			}
			
			if (split.length == 2){
				if (split[1].matches(fullyear)){
					date = new DateTime(Integer.parseInt(split[1]), Integer.parseInt(split[0]), 1, 0, 0, 0, 0);
					return date.monthOfYear().toInterval();
				}
				else if (split[0].matches(fullyear)){
					date = new DateTime(Integer.parseInt(split[0]), Integer.parseInt(split[1]), 1, 0, 0, 0, 0);
					return date.monthOfYear().toInterval();
				}
			}
			else if (split.length == 1){
				date = date.year().setCopy(Integer.parseInt(split[0].replaceAll("'", "")));
				return date.year().toInterval();
			}
			else if (split.length == 3){
				date = new DateTime(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), 0, 0, 0, 0);
				return date.dayOfMonth().toInterval();
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}
				
		}
		else if (timeEx.toLowerCase().matches(exp10)){//((the )(end|start|beginning|middle) of ("+year+"|"+monthspec+"|(the "+calendar_granularity+"))( ?"+year+")?)";
			String[] split = timeEx.split(" ");
			
			if (split.length == 4){
				if (split[3].matches(year)){
					date = date.year().setCopy(Integer.parseInt(split[3]));
					return date.year().toInterval();
				}
				else if (split[3].toLowerCase().matches(monthspec)){
					date = date.monthOfYear().setCopy(months.get(split[3].toLowerCase()));
					return date.monthOfYear().toInterval();
				}
				else {
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				}
			}
			else if (split.length == 5){
				if (split[4].toLowerCase().matches("month")){
					return date.monthOfYear().toInterval();
				}
				else if (split[4].toLowerCase().matches("year")){
					return date.year().toInterval();
				}
				else if (split[4].toLowerCase().matches("day")){
					return date.dayOfMonth().toInterval();
				}
				else if(split[3].toLowerCase().matches(monthspec) && split[4].toLowerCase().matches(year)){
					date = new DateTime(Integer.parseInt(split[4]), months.get(split[3].toLowerCase()), 1, 0, 0, 0, 0);
					return date.monthOfYear().toInterval();
				}
				else{
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				}
					
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}

		}
		else if (timeEx.toLowerCase().matches(exp11)){
			String[] split = timeEx.split(" ");
			System.out.println("express‹o 11");
			return null;
		}
		else if (timeEx.toLowerCase().matches(exp12)){
			String[] split = timeEx.split(" ");
			System.out.println("express‹o 12");
			return null;
		}
		else if (timeEx.toLowerCase().matches(exp13)){//"((next|previous|last|following|a) (few|many|"+textual_number+"|"+numeric_days+") "+calendar_granularity+"s?)";
			String[] split = timeEx.split(" ");	
			System.out.println("express‹o 13");			
		}
		else if (timeEx.toLowerCase().matches(exp14)){//"("+vague+" "+textual_number+" "+calendar_granularity+"s?)";
			String[] split = timeEx.split(" ");
			
			if (split[1].toLowerCase().matches(textual_number)){
				if (split[2].toLowerCase().matches("seconds?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
					CandidateCreation.granularityDuration = 1;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("minutes?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
					CandidateCreation.granularityDuration = 2;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("hours?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
					CandidateCreation.granularityDuration = 3;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("days?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
					CandidateCreation.granularityDuration = 4;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("weeks?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
					CandidateCreation.granularityDuration = 5;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("months?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
					CandidateCreation.granularityDuration = 6;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("years?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
					CandidateCreation.granularityDuration = 7;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("decades?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
					CandidateCreation.granularityDuration = 8;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("centuries")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
					CandidateCreation.granularityDuration = 10;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[1].toLowerCase().matches("century")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(days.get(split[1].toLowerCase()));
					CandidateCreation.granularityDuration = 10;
					return date.millisOfSecond().toInterval(); 
				}
				else {
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				} 
			}
			else if (split[1].toLowerCase().matches(numeric_days)){
				if (split[2].toLowerCase().matches("seconds?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
					CandidateCreation.granularityDuration = 1;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("minutes?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
					CandidateCreation.granularityDuration = 2;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("hours?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
					CandidateCreation.granularityDuration = 3;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("days?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
					CandidateCreation.granularityDuration = 4;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("weeks?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
					CandidateCreation.granularityDuration = 5;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("months?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
					CandidateCreation.granularityDuration = 6;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("years?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
					CandidateCreation.granularityDuration = 7;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("decades?")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
					CandidateCreation.granularityDuration = 8;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("centuries")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
					CandidateCreation.granularityDuration = 10;
					return date.millisOfSecond().toInterval(); 
				}
				else if (split[2].toLowerCase().matches("century")){
					date = new DateTime(1970, 1, 1, 0, 0, 0, 0).plusMillis(Integer.parseInt(split[1]));
					CandidateCreation.granularityDuration = 10;
					return date.millisOfSecond().toInterval(); 
				}
				else {
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				}
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}
		}
		else if (timeEx.toLowerCase().matches(exp15)){
			String[] split = timeEx.split(":");
			if (split.length == 1 && timeEx.toLowerCase().contains("hr")){
				date = new DateTime(numeroAno, mesAno, diaMes, Integer.parseInt(split[0].substring(0, split[0].length()-2)), 0, 0, 0);
				return date.hourOfDay().toInterval();
			}
			if(timeEx.toLowerCase().matches(times)){
				if (timeEx.toLowerCase().contains("pm")){
					if (timeEx.toLowerCase().contains(" pm")){
						date = new DateTime(numeroAno, mesAno, diaMes, Integer.parseInt(split[0])+12, Integer.parseInt(split[1].substring(0, split[1].length()-3)), 0, 0);
						return date.minuteOfHour().toInterval();
					}
					else{
						date = new DateTime(numeroAno, mesAno, diaMes, Integer.parseInt(split[0])+12, Integer.parseInt(split[1].substring(0, split[1].length()-2)), 0, 0);
						return date.minuteOfHour().toInterval();
					}
				}
				else if (timeEx.toLowerCase().contains("am") || timeEx.toLowerCase().contains("hr")){
					if (timeEx.toLowerCase().contains(" am") || timeEx.toLowerCase().contains(" hr")){
						date = new DateTime(numeroAno, mesAno, diaMes, Integer.parseInt(split[0]), Integer.parseInt(split[1].substring(0, split[1].length()-3)), 0, 0);
						return date.minuteOfHour().toInterval();
					}
					else{
						date = new DateTime(numeroAno, mesAno, diaMes, Integer.parseInt(split[0]), Integer.parseInt(split[1].substring(0, split[1].length()-2)), 0, 0);
						return date.minuteOfHour().toInterval();
					}
				}
				else{
					date = new DateTime(numeroAno, mesAno, diaMes, Integer.parseInt(split[0]), Integer.parseInt(split[1]), 0, 0);
					return date.minuteOfHour().toInterval();
				}
					
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}

		}
		else if (timeEx.toLowerCase().matches(exp16)){//"("+monthspec+"\\.? [0-3]?[0-9](st|nd|rd|th)?)";
			timeEx = timeEx.replaceAll("\\.", "");
			String[] split = timeEx.split(" ");
			
			if(split[1].matches("[0-9]{1,2}(st|nd|rd|th)"))
				date = new DateTime(numeroAno, months.get(split[0].toLowerCase()), Integer.parseInt(split[1].substring(0, split[1].length()-2)), 0, 0, 0, 0);
			else
				date = new DateTime(numeroAno, months.get(split[0].toLowerCase()), Integer.parseInt(split[1]), 0, 0, 0, 0);
			
			return date.dayOfMonth().toInterval();
			
		}//Expression like "1st October 2010"
		else if (timeEx.toLowerCase().matches(exp17)){
			String[] split = timeEx.split(" ");
			
			if (split.length == 3){
				if (split[0].toLowerCase().matches("the")){
					if(split[1].matches("[0-9]{1,2}(st|nd|rd|th)"))
						date = new DateTime(numeroAno, months.get(split[2].toLowerCase()), Integer.parseInt(split[1].substring(0, split[1].length()-2)), 0, 0, 0, 0);
					else
						date = new DateTime(numeroAno, months.get(split[2].toLowerCase()), Integer.parseInt(split[1]), 0, 0, 0, 0);
					
					return date.dayOfMonth().toInterval();
				}
				if(split[0].matches("[0-9]{1,2}(st|nd|rd|th)"))
					date = new DateTime(Integer.parseInt(split[2]), months.get(split[1].toLowerCase()), Integer.parseInt(split[0].substring(0, split[0].length()-2)), 0, 0, 0, 0);
				else
					date = new DateTime(Integer.parseInt(split[2]), months.get(split[1].toLowerCase().replaceAll(",", "")), Integer.parseInt(split[0]), 0, 0, 0, 0);
				
				return date.dayOfMonth().toInterval();
			}
			else if(split.length == 2){
				if(split[0].matches("[0-9]{1,2}(st|nd|rd|th)"))
					date = new DateTime(numeroAno, months.get(split[1].toLowerCase()), Integer.parseInt(split[0].substring(0, split[0].length()-2)), 0, 0, 0, 0);
				else
					date = new DateTime(numeroAno, months.get(split[1].toLowerCase()), Integer.parseInt(split[0]), 0, 0, 0, 0);
				
				return date.dayOfMonth().toInterval();
			}
			else if (split.length == 4){
				if(split[1].matches("[0-9]{1,2}(st|nd|rd|th)"))
					date = new DateTime(Integer.parseInt(split[3]), months.get(split[2].toLowerCase()), Integer.parseInt(split[1].substring(0, split[1].length()-2)), 0, 0, 0, 0);
				else
					date = new DateTime(Integer.parseInt(split[3]), months.get(split[2].toLowerCase().replaceAll(",", "")), Integer.parseInt(split[1]), 0, 0, 0, 0);
				
				return date.dayOfMonth().toInterval();
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}
			
		}		
		else if (timeEx.toLowerCase().matches(exp18)){//"((around )?"+monthspec+"?( "+numeric_days+")?(, | )?"+year+"?)"; //April 14, 1979
			String[] split = timeEx.split(" ");
			if (split.length == 2 && split[0].toLowerCase().matches("around")){
				if (split[1].toLowerCase().matches(monthspec)){
					date = new DateTime(numeroAno, months.get(split[1].toLowerCase()), 1, 0, 0, 0, 0);
					return date.monthOfYear().toInterval();
				}
				else if (split[1].toLowerCase().matches(fullyear)){
					date = new DateTime(Integer.parseInt(split[1]), 1, 1, 0, 0, 0, 0);
					return date.year().toInterval();
				}
				else {
					System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
					return null;
				}
			}
			else if(split.length == 2 && split[1].toLowerCase().matches(year)){
				if (split[0].contains(",") && !split[0].toLowerCase().replaceAll(",", "").matches(monthspec)){
					date = new DateTime(Integer.parseInt(split[1]),mesAno, Integer.parseInt(split[0].toLowerCase().replace(",", "")), 0, 0, 0, 0);
					return date.dayOfMonth().toInterval();
				}
				date = new DateTime(Integer.parseInt(split[1]), months.get(split[0].toLowerCase().replace(",", "")), 1, 0, 0, 0, 0);
				return date.monthOfYear().toInterval();
			}
			else if (split.length == 1){
				if (split[0].contains(",")){
					date = new DateTime(numeroAno, months.get(split[0].toLowerCase().replace(",", "")), 1, 0, 0, 0, 0);
					return date.monthOfYear().toInterval();
				}
				else{
					date = new DateTime(numeroAno, mesAno, Integer.parseInt(split[0]), 0, 0, 0, 0);
					return date.dayOfMonth().toInterval();
				}
			}
			else if (split.length == 3 && !split[0].toLowerCase().matches("around")){
				split[0] = split[0].replaceAll("\\.", "");
				date = new DateTime(Integer.parseInt(split[2]), months.get(split[0].toLowerCase().replace(",", "")), Integer.parseInt(split[1].toLowerCase().replace(",", "")), 0, 0, 0, 0);
				return date.dayOfMonth().toInterval();
			}
			else if (split.length == 3 && split[0].toLowerCase().matches("around")){
				if (split[2].matches("[0-9]{1,2}")){
					date = new DateTime(numeroAno, months.get(split[1].toLowerCase()), Integer.parseInt(split[2]), 0, 0, 0, 0);
					return date.dayOfMonth().toInterval();
				}
				else if (split[2].matches(fullyear)){
					date = new DateTime(Integer.parseInt(split[2]), months.get(split[1].toLowerCase()), 1, 0, 0, 0, 0);
					return date.monthOfYear().toInterval();
				}
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}
				
			}
		else if (timeEx.toLowerCase().matches(exp19)){
			String[] split = timeEx.split(" ");
			System.out.println("express‹o 10");
			return null;
		}
		else if (timeEx.toLowerCase().matches(exp20)){
			String[] split = timeEx.split(" ");
			
			if (split.length == 2){
				int ano = Integer.parseInt(split[1].replace("s", ""));
				date = new DateTime(ano, 1, 1, 0, 0, 0, 0);
				return new Interval(new DateTime(date.toString("YYYY-01-01'T'00:00:00.000")), new DateTime(date.toString(ano+10+"-01-01'T'00:00:00.000")));
			}
			else if	(split.length == 3){
				int ano = Integer.parseInt(split[2].replace("s", ""));
				date = new DateTime(ano, 1, 1, 0, 0, 0, 0);
				return new Interval(new DateTime(date.toString("YYYY-01-01'T'00:00:00.000")), new DateTime(date.toString(ano+10+"-01-01'T'00:00:00.000")));
			}
			else if	(split.length == 1){
				int ano = Integer.parseInt(split[0].replace("s", ""));
				date = new DateTime(ano, 1, 1, 0, 0, 0, 0);
				return new Interval(new DateTime(date.toString("YYYY-01-01'T'00:00:00.000")), new DateTime(date.toString(ano+10+"-01-01'T'00:00:00.000")));
			}
			else {
				System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
				return null;
			}
		}
		else if (timeEx.toLowerCase().matches(exp21)){
			String[] split = timeEx.split(" ");
			
			if (split[3].toLowerCase().matches("year")){
				date = new DateTime(numeroAno, 1, 1, 0, 0, 0, 0);
				return date.year().toInterval();
			}
			else if (split[3].toLowerCase().matches("month")){
				date = new DateTime(numeroAno, mesAno, 1, 0, 0, 0, 0);
				return date.monthOfYear().toInterval();
			}		
			
		}
		else if (timeEx.toLowerCase().matches(exp23)){
			String[] split = timeEx.split(" ");
			
			if (timeEx.toLowerCase().matches("now") || timeEx.toLowerCase().matches("current") || timeEx.toLowerCase().matches("currently")){
				past_future_present_null_ref = 3;
				return null;
			}
			else if (timeEx.toLowerCase().matches("christmas")){
				date = date.monthOfYear().setCopy(12);
				return date.monthOfYear().toInterval();
			}
			else if (timeEx.toLowerCase().matches("future") || timeEx.toLowerCase().matches("the future") || timeEx.toLowerCase().matches("soon")){
				past_future_present_null_ref = 2;
				return null;
			}
			else if (timeEx.toLowerCase().matches("recently")){
				past_future_present_null_ref = 4;
				return null;
			}
			else if (split.length == 2){
				if (split[0].toLowerCase().equals("recent")){
					past_future_present_null_ref = 4;
					return null;
				}
				else if (split[0].toLowerCase().equals("later")){
					past_future_present_null_ref = 2;
					return null;
				}
			}
			else if (split.length == 3){
				if (split[0].toLowerCase().equals("a") && split[1].toLowerCase().equals("later")){
					past_future_present_null_ref = 6;
					return null;
				}
			}
		}
		else System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida: "+timeEx);
		//Expressions like "Later in the year"

		System.out.println("ERRO!! Expressï¿½o nï¿½o reconhecida antes de sair: "+timeEx);
		return null;
	}

	
	// Expressions like "early this friday"
	private static String exp1 = "(((early|late|earlier|later|the) )?((this|next|last|following|previous|in) )?("+calendar_granularity+"|"+longdays+"|"+monthspec+"))";
	// Expressions like "mon 10 jan 2010"
	private static String exp2 = "(("+dayspec+" )?[0-9]{1,2} ([0-9]{1,2}|" + monthspec + ") (19|20)?[0-9]{2})";
	// Expressions like "twenty hour long"
	private static String exp3 = "(the )?((latest )|(per )|(past )|(under )|(over )|(near )|(next )|(previous )|(last )|(following )|(a ))?((few)|(many)|("+textual_number+")|("+numeric_days+"))?[\\-\\ ]?(of that )?"+calendar_granularity+"([\\-\\ ](long|old))?";
	// Expressions like "last couple of months"
	private static String exp4 = "";//(((recent|previous|past|first|last) )?(([0-9]+|"+textual_number+"|couple of|few) )?"+calendar_granularity+"s?( (ago|later|earlier))?)";
	// Expressions like "the weekend"
	private static String exp5 = "(((the )|(that )|(some ))"+"(same )?(("+calendar_granularity+")|(time)))";
	// Expressions like "mid february"
	private static String exp6 = "((early)|(mid)|(end)|(late))(-| )(in )?("+monthspec+"|"+calendar_granularity+")?( ?"+fullyear+")?";
	// Expressions like "the second month"
	private static String exp7 = "((the )?"+simple_ordinals+" "+calendar_granularity+")";
	// Expression like "two hours later"
	private static String exp8 = textual_number+" "+calendar_granularity+" (or so )?(earlier|later|previous|ago|since)";
	//Expressions like "1989"
	private static String exp9 = "([0-9]{1,2}-)?"+year+"(-[0-9]{1,2})?(-[0-9]{1,2})?(t[0-2][0-9]:[0-6][0-9])?";
	//Expression like "the end of the month"
	private static String exp10 = "((the )(end|start|beginning|middle) of ("+year+"|"+monthspec+"|(the "+calendar_granularity+"))( ?"+year+")?)";
	//Expression like "monday morning"
	private static String exp11 = "(("+longdays+"|this) (morning|afternoon|evening|night)|tonight)";
	//Expression like "in more than sixteen hours"
	private static String exp12 = "((within|in ((more|less) than )("+vague+" )?)"+textual_number+" "+calendar_granularity+"s?)";
	//Expression like "following two hours"
	private static String exp13 = "((next|previous|last|following|a) (few|many|"+textual_number+"|"+numeric_days+") "+calendar_granularity+"s?)";
	//Expression like "around four hours"  
	private static String exp14 = "("+vague+" ("+textual_number+"|"+numeric_days+") "+calendar_granularity+"s?)";
	//Expression like "tomorrow 20:59pm"
	private static String exp15 = "(("+times+"( "+longdays+")?)|("+longdays+" "+times+"))";
	//Expression like "jan 1st"
	private static String exp16 = "("+monthspec+"\\.? [0-3]?[0-9](st|nd|rd|th)?)";
	//Expression like "1st October 2010"
	private static String exp17 = "((the )?"+numeric_days+"? "+monthspec+",?\\.? ?(of)?"+year+"?)";
	//Expressions like "September 17, 1939"
	private static String exp18 = "((around )?"+monthspec+"?.?( "+numeric_days+")?(, | )?"+year+"?)";
	//Expressions like "the first two months of 1939"
	private static String exp19 = "the ((first)|(last)) "+textual_number+" "+calendar_granularity+" of ("+year+"|"+monthspec+")";
	//Expressions like "the 1920s"
	private static String exp20 = "(the )?(early |mid |end |late )?"+year+"s?";
	//Expressions like "Later in the year"
	private static String exp21 = "((later)|(earlier)|(sooner)|(previously)|(recently)) in the "+calendar_granularity;
	//Expression like "the early 21st century"
	//private static String exp22 = 
	private static String exp23 = "(later "+calendar_granularity+")|(soon)|(now)|(christmas)|(the future)|(future)|(current)|(currently)|(recently)|(recent "+calendar_granularity+")|(a later date)";
}