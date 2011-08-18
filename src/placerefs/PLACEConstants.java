package placerefs;

import java.util.HashSet;

import placerefs.gazetteer.GazetteerEntry;

public class PLACEConstants {

	public static HashSet<GazetteerEntry> candidatesPlaceSameDoc = new HashSet<GazetteerEntry>();

	public static boolean isFirstPlace;

	public static void init(){			
			isFirstPlace = true;
	}

}