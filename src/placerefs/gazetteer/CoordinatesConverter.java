package placerefs.gazetteer;

import java.text.DecimalFormat;

public class CoordinatesConverter {

    /**
     * Example input formats:
     * 37:55:15 122:20:59W
     * 37:55N 122:20E
     * -37:55 122:20:59
     * 
     * @param c
     * @return
     * @throws Exception
     */
    public static String deg2Dec(String c) {

        String[] latlng = c.split(" ");
        String lat = latlng[0];
        String lng = latlng[1];
        
        if (lat.charAt(lat.length()-1) == 'S') lat = "-" + lat.substring(0, lat.length() - 1);
        if (lat.charAt(lat.length()-1) == 'N') lat = lat.substring(0, lat.length() - 1);
        if (lng.charAt(lng.length()-1) == 'W') lng = "-" + lng.substring(0, lng.length() - 1);
        if (lng.charAt(lng.length()-1) == 'E') lng = lng.substring(0, lng.length() - 1);
        // some coordinates come with both the minus and the direction:
        lat = lat.replaceAll("--", "-");
        lng = lng.replaceAll("--", "-");
        
        String[] latParse = lat.split(":");
        String[] lngParse = lng.split(":");
        
        Double latH = Double.parseDouble(latParse[0]);
        Double latM = (latParse.length > 1) ? Double.parseDouble(latParse[1])/60 : 0.0;
        Double latS = (latParse.length > 2) ? Double.parseDouble(latParse[2])/3600 : 0.0;
        Double decLat = Math.signum(latH) * (Math.abs(latH) + latM + latS);
        
        Double lngH = Double.parseDouble(lngParse[0]);
        Double lngM = (lngParse.length > 1) ? Double.parseDouble(lngParse[1])/60 : 0.0;
        Double lngS = (lngParse.length > 2) ? Double.parseDouble(lngParse[2])/3600 : 0.0;
        Double decLng = Math.signum(lngH) * (Math.abs(lngH) + lngM + lngS);
        
        DecimalFormat dec = new DecimalFormat("0.0####");
        return  dec.format(decLat) + " " + dec.format(decLng);
    }
    
}
