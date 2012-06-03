package temporal.gp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DataPoint {

	private static Map<String,Long> months;
	
	private static Map<String,Long> weekDays;
	
	private static Map<String,Long> units;

	private static void init () {
		months = new HashMap<String,Long>();
		weekDays = new HashMap<String,Long>();
		units = new HashMap<String,Long>();

		months.put("january",1l);
		months.put("february",2l);
		months.put("march",3l);
		months.put("april",4l);
		months.put("may",5l);
		months.put("june",6l);
		months.put("july",7l);
		months.put("august",8l);
		months.put("september",9l);
		months.put("october",10l);
		months.put("november",11l);
		months.put("december",12l);

		weekDays.put("monday", 1l);
		weekDays.put("tuesday", 2l);
		weekDays.put("wednesday", 3l);
		weekDays.put("thursday", 4l);
		weekDays.put("friday", 5l);
		weekDays.put("saturday", 6l);
		weekDays.put("sunday", 7l);
		
		units.put("second", 1000l);
		units.put("minute", 60 * 1000l);
		units.put("hour", 60 * 60 * 1000l);
		units.put("day", 24 * 60 * 60 * 1000l);
		units.put("week", 8 * 24 * 60 * 60 * 1000l);
		units.put("month", 31 * 24 * 60 * 60 * 1000l);
		units.put("year", 364 * 24 * 60 * 60 * 1000l);
		units.put("decade", 10 * 364 * 24 * 60 * 60 * 1000l);
		units.put("century", 100 * 364 * 60 * 60 * 1000l);
	};

	public DataPoint ( String timex , long result ) {
		init();
		ArrayList<Long> values = new ArrayList<Long>();
		long monthNames = 0;
		long weekDayNames = 0;
		long unitNames = 0;
		timex = timex.trim().toLowerCase();
		for ( String val : timex.split(" +") ) {
			if (months.containsKey(val)) { 
				monthNames++; 
				values.add(months.get(val));
			} else if (weekDays.containsKey(val)) { 
				weekDayNames++; 
				values.add(weekDays.get(val)); 
			} else if (units.containsKey(val)) { 
				unitNames++; 
				values.add(units.get(val)); 
			} else try { 
				if (val.matches("^[0-9]+((st)|(ns)|(rd)|(th))$")) val = val.substring(0,val.length()-2);
				values.add(new Long(val));
			} catch ( Exception ex ) { }
		}
		values.add(0, monthNames);
		values.add(0, weekDayNames);
	    values.add(0, unitNames);
		values.add(0, (long)values.size());
		Long[] aux = values.toArray(new Long[0]);
		this.result = result;
		this.data = new long[aux.length];
		for ( int i = 0; i < data.length; i++ ) this.data[i] = aux[i];
	}

	public DataPoint(Long data[], long result) {
		this.result = result;
		this.data = new long[data.length];
		for ( int i = 0; i < data.length; i++ ) this.data[i] = data[i];
	}
	
	public DataPoint(long data[], long result) {
		this.result = result;
		this.data = Arrays.copyOf(data, data.length);
	}
	
	public long data[];
	
	public long result;

}