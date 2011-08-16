package placerefs;

import java.util.HashSet;

import placerefs.gazetteer.KbEntity;

public class PLACEConstants {

	public static HashSet<KbEntity> candidatesPlaceSameDoc = new HashSet<KbEntity>();

	public static boolean isFirstPlace;

	public static void init(){			
			isFirstPlace = true;
	}

}