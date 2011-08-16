package geo;

import static java.lang.Double.isNaN;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static java.lang.Math.round;
import java.text.NumberFormat;

/**
 * A geographic data calculator based on Vincenty's formulae.
 * 
 * <p>The geodetic operations in this class are based on an ellipsoidal
 * model of the Earth, using Vincenty's formulae; this is slow, but
 * very accurate.  Be aware, however, that accuracy is compromised in
 * extreme cases, such as nearly-antipodal points.
 * 
 * <p>The user needs to specify the ellipsoid to use for the geodetic
 * calculations; this is done in the constructor.  The Ellipsoid enum
 * defines a range of reference ellipsoids.  The default is the widely-used
 * WGS-84 standard.
 * 
 * <p>The code in this class was converted from the Fortran implementations
 * developed by the National Geodetic Survey.  No license is specified for
 * this code, but I believe that it is public domain, since it was created
 * by an agency of the US Government.
 * 
 * References:
 * 
 * <ul>
 * <li><a href="http://www.ngs.noaa.gov/PC_PROD/Inv_Fwd/">Fortran
 *     implementations of Vincenty's formulae,</a> from the National
 *     Geodetic Survey</li>
 * <li><a href="http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf">Vincenty's
 *     paper describing his formulae</a>, from NOAA</li>
 * <li><a href="http://www.movable-type.co.uk/scripts/latlong-vincenty.html">JS
 *     implementations of Vincenty's inverse formula</a>, from Movable Type
 *     Ltd</li>
 * <li><a href="http://www.movable-type.co.uk/scripts/latlong-vincenty-direct.html">JS
 *     implementations of Vincenty's direct formula</a>, from Movable Type
 *     Ltd</li>
 * </ul>
 *
 */
public class VincentyDistanceCalculator {

	// Returns the distance in kilometers
	public static double getDistance ( double lat1, double lon1, double lat2, double lon2 ) {
		VincentyDistanceCalculator dist = new VincentyDistanceCalculator();
		Position p1 = Position.fromDegrees(lat1, lon1);
		Position p2 = Position.fromDegrees(lat2, lon2);
                return dist.distance(p1,p2).getMetres() * 1000.0;
	}

	public VincentyDistanceCalculator() {
		this(Ellipsoid.WGS84);
	}

	public VincentyDistanceCalculator(Ellipsoid ellip) {
		ellipsoid = ellip;
	}

	Ellipsoid getEllipsoid() {
		return ellipsoid;
	}
	
	private Ellipsoid ellipsoid = null;

	public static final double MEAN_RADIUS = 6371000.0;
        
        /**
         * The equatorial radius of the Earth in metres.
         */
        public static final double EQUATORIAL_RADIUS = 6378137.0;
        
        /**
         * The polar radius of the Earth in metres.
         */
        public static final double POLAR_RADIUS = 6356755.0;

        /**
         * Selectable ellipsoids, for geodetic calculations.
         */
        public enum Ellipsoid {
                /** Pseudo-ellipsoid for an assumed spherical Earth. */
                SPHERE("Sphere",                                MEAN_RADIUS,    0.0),
                
                /** WGS 84 ellipsoid. */
                WGS84("GRS80 / WGS84 (NAD83)",  6378137,                1.0 / 298.25722210088),
                
                /** Clarke 1866 (NAD27) ellipsoid. */
                NAD27("Clarke 1866 (NAD27)",    6378206.4,              1.0 / 294.9786982138),
                
                /** Airy 1858 ellipsoid. */
                AIRY1858("Airy 1858",                   6377563.396,    1.0 / 299.3249646),
                
                /** Airy Modified ellipsoid. */
                AIRYM("Airy Modified",                  6377340.189,    1.0 / 299.3249646),
                
                /** NWL-9D (WGS 66) ellipsoid. */
                WGS66("NWL-9D (WGS 66)",                6378145,                1.0 / 298.25),
                
                /** WGS 72 ellipsoid. */
                WGS72("WGS 72",                                 6378135,                1.0 / 298.26);

                Ellipsoid(String n, double a, double f) {
                        name = n;
                        axis = a;
                        flat = f;
                }

                /** User-visible name of this ellipsoid. */
                public final String name;

                /** Equatorial semimajor axis of this ellipsoid (in metres). */
                public final double axis;

                /** Flattening of this ellipsoid. */
                public final double flat;

        }

	public Distance distance(Position p1, Position p2) {
		Ellipsoid ellipsoid = getEllipsoid();
		double[] ret = gpnhri(ellipsoid.axis, ellipsoid.flat,
							  p1.getLatRads(), p1.getLonRads(),
							  p2.getLatRads(), p2.getLonRads());		
		return new Distance(ret[0]);
	}
	
	public Distance latDistance(Position p1, double lat) {
		Position p2 = new Position(lat, p1.getLonRads());
		return distance(p1, p2);
	}

	public Azimuth azimuth(Position p1, Position p2) {
		Ellipsoid ellipsoid = getEllipsoid();
		double[] ret = gpnhri(ellipsoid.axis, ellipsoid.flat,
							  p1.getLatRads(), p1.getLonRads(),
							  p2.getLatRads(), p2.getLonRads());		
		return new Azimuth(ret[1]);
	}
	
	public Vector vector(Position p1, Position p2) {
		Ellipsoid ellipsoid = getEllipsoid();
		double[] ret = gpnhri(ellipsoid.axis, ellipsoid.flat,
							  p1.getLatRads(), p1.getLonRads(),
							  p2.getLatRads(), p2.getLonRads());
		return new Vector(new Distance(ret[0]), new Azimuth(ret[1]));
	}

	public Position offset(Position p1, Distance distance, Azimuth azimuth) {
		Ellipsoid ellipsoid = getEllipsoid();
		double[] res = dirct1(p1.getLatRads(), p1.getLonRads(),
							  azimuth.getRadians(),
							  distance.getMetres(),
							  ellipsoid.axis, ellipsoid.flat);
		if (isNaN(res[0]) || isNaN(res[1])) return null;
		return new Position(res[0], res[1]);
	}
	
	/**
	 * Solution of the geodetic direct problem after T. Vincenty.
	 * Modified Rainsford's method with Helmert's elliptical terms.
	 * Effective in any azimuth and at any distance short of antipodal.
	 *
	 * Programmed for the CDC-6600 by lcdr L. Pfeifer, NGS Rockville MD,
	 * 20 Feb 1975.
	 *
	 * @param	glat1		The latitude of the starting point, in radians,
	 * 						positive north.
	 * @param	glon1		The latitude of the starting point, in radians,
	 * 						positive east.
	 * @param	azimuth		The azimuth to the desired location, in radians
	 * 						clockwise from north.
	 * @param	dist		The distance to the desired location, in meters.
	 * @param	axis		The semi-major axis of the reference ellipsoid,
	 * 						in meters.
	 * @param	flat		The flattening of the reference ellipsoid.
	 * @return				An array containing the latitude and longitude
	 * 						of the desired point, in radians, and the
	 * 						azimuth back from that point to the starting
	 * 						point, in radians clockwise from north.
	 */
	private static double[] dirct1(double glat1, double glon1,
								   double azimuth, double dist,
								   double axis, double flat)
	{
		double r = 1.0 - flat;
		double tu = r * sin(glat1) / cos(glat1);
		double sf = sin(azimuth);
		double cf = cos(azimuth);
		double baz = 0.0;
		if (cf != 0.0) baz = atan2(tu, cf) * 2.0;
		double cu = 1.0 / sqrt(tu * tu + 1.0);
		double su = tu * cu;
		double sa = cu * sf;
		double c2a = -sa * sa + 1.0;
		double x = sqrt((1.0 / r / r - 1.0) * c2a + 1.0) + 1.0;
		x = (x - 2.0) / x;
		double c = 1.0 - x;
		c = (x * x / 4.0 + 1) / c;
		double d = (0.375 * x * x - 1.0) * x;
		tu = dist / r / axis / c;
		double y = tu;
		double sy, cy, cz, e;
		do {
			sy = sin(y);
			cy = cos(y);
			cz = cos(baz + y);
			e = cz * cz * 2.0 - 1.0;

			c = y;
			x = e * cy;
			y = e + e - 1.0;
			y = (((sy * sy * 4.0 - 3.0) * y * cz * d / 6.0 + x) * d / 4.0 - cz) * sy * d + tu;
		} while (abs(y - c) > PRECISION_LIMIT);
		baz = cu * cy * cf - su * sy;
		c = r * sqrt(sa * sa + baz * baz);
		d = su * cy + cu * sy * cf;
		double glat2 = atan2(d, c);
		c = cu * cy - su * sy * cf;
		x = atan2(sy * sf, c);
		c = ((-3.0 * c2a + 4.0) * flat + 4.0) * c2a * flat / 16.0;
		d = ((e * cy * c + cz) * sy * c + y) * sa;
		double glon2 = glon1 + x - (1.0 - c) * d * flat;
		baz = atan2(sa, baz) + PI;
		double[] ret = new double[3];
		ret[0] = glat2;
		ret[1] = glon2;
		ret[2] = baz;
		return ret;
	}

	/**
	 * Solution of the geodetic inverse problem after T. Vincenty.
	 * Modified rainsford's method with helmert's elliptical terms.
	 * Effective in any azimuth and at any distance short of antipodal;
	 * from/to stations must not be the geographic pole.
	 *               
	 * Programmed by Robert (Sid) Safford; released for field use 5 Jul 1975.
	 * 
	 * @param	a			Semi-major axis of reference ellipsoid in meters.
	 * @param	f			Flattening (0.0033528...).
	 * @param	p1			Lat station 1, in radians, positive north.
	 * @param	e1			Lon station 1, in radians, positive east.
	 * @param	p2			Lat station 2, in radians, positive north.
	 * @param	e2			Lon station 2, in radians, positive east.
	 * @return				An array of doubles, containing: the geodetic
	 * 						distance between the stations, in meters; the
	 * 						azimuth at station 1 to station 2; and the
	 * 						azimuth at station 2 to station 1.  Azimuths are
	 * 						in radians, clockwise from north, and may not
	 * 						be normalized.
	 */
	private static double[] gpnhri(double a, double f,
							       double p1, double e1, double p2, double e2)
	{
        //  aa               constant from subroutine gpnloa                    
        //  alimit           equatorial arc distance along the equator   (radians)
        //  arc              meridional arc distance latitude p1 to p2 (in meters)      
        //  az1              azimuth forward                          (in radians)
        //  az2              azimuth back                             (in radians)
        //  bb               constant from subroutine gpnloa                    
        //  dlon             temporary value for difference in longitude (radians)   
        //  equ              equatorial distance                       (in meters)
        //  r1,r2            temporary variables    
        //  s                ellipsoid distance                        (in meters)
        //  sms              equatorial - geodesic distance (s - s) "sms"       
        //  ss               temporary variable     

		// Calculate the eccentricity squared.
		double esq = f * (2.0 - f);
		// Normalize the longitudes to be positive.
		if (e1 < 0.0)
			e1 += TWO_PI;
		if (e2 < 0.0)
			e2 += TWO_PI;
		// Test the longitude difference; if it's next to zero, then we
		// have to calculate this as a meridional arc.
		double dlon = e2 - e1;
		if (abs(dlon) < GEO_TOLERANCE) return gpnarc(a, f, esq, p1, p2);
		// Normalize the longitude difference to -PI .. PI.
		if (dlon >= PI  && dlon < TWO_PI)
			dlon = dlon - TWO_PI;
		if (dlon <= -PI && dlon > -TWO_PI)
			dlon = dlon + TWO_PI;
		// If the longitude difference is over 180 degrees, turn it around.
		double absDlon = abs(dlon);
		if (absDlon > PI)
			absDlon = TWO_PI - absDlon;
		// Compute the limit in longitude (alimit): it is equal 
		// to twice the distance from the equator to the pole,
		// as measured along the equator (east/west).
		double alimit = PI * (1.0 - f);
		// If the longitude difference is beyond the lift-off point, see if
		// our points are anti-nodal.  If so, we need to use the lift-off
		// algorithm.
		if (absDlon >= alimit && abs(p1) < NODAL_LIMIT && abs(p2) < NODAL_LIMIT)
			return gpnloa(a, f, esq, dlon);
		double f0   = 1.0 - f;
		double b    = a * f0;
		double epsq = esq / (1.0 - esq);
		double f2   = f * f;
		double f3   = f * f2;
		double f4   = f * f3;
//		the longitude difference 
		dlon  = e2 - e1;
		double ab    = dlon;
//		the reduced latitudes    
		double u1    = f0 * sin(p1) / cos(p1);
		double u2    = f0 * sin(p2) / cos(p2);
		u1    = atan(u1);
		u2    = atan(u2);
		double su1   = sin(u1);
		double cu1   = cos(u1);
		double su2   = sin(u2);
		double cu2   = cos(u2);
//		counter for the iteration operation
		double clon = 0, slon = 0, sinalf = 0;
                double sig = 0, csig = 0, ssig = 0, w = 0;
		double q2 = 0, q4 = 0, q6 = 0, r2 = 0, r3 = 0;
		for (int i = 0; i < 8; ++i) {
			clon  = cos(ab);
			slon  = sin(ab);
			csig  = su1 * su2 + cu1 * cu2 * clon;
			double k1 = slon * cu2;
			double k2 = su2 * cu1 - su1 * cu2 * clon;
			ssig  = sqrt(k1 * k1 + k2 * k2);
			sig   = atan2(ssig, csig);
			sinalf = cu1 * cu2 * slon / ssig;
			w   = 1.0 - sinalf * sinalf;
			double t4  = w * w;
			double t6  = w * t4;
//			the coefficients of type a      
			double ao  = f - f2 * (1.0 + f + f2) * w / 4.0 + 3.0 * f3 *
			(1.0 + 9.0 * f / 4.0) * t4 / 16.0 - 25.0 * f4 * t6 / 128.0;
			double a2  = f2 * (1 + f + f2) * w / 4.0 - f3 * (1 + 9.0 * f / 4.0) * t4 / 4.0 +
			75.0 * f4 * t6 / 256.0;
			double a4  = f3 * (1.0 + 9.0 * f / 4.0) * t4 / 32.0 - 15.0 * f4 * t6 / 256.0;
			double a6  = 5.0 * f4 * t6 / 768.0;
//			the multiple angle functions    
			double qo  = 0.0;
			if (w > FP_TOLERANCE) qo = -2.0 * su1 * su2 / w;
			q2  = csig + qo;
			q4  = 2.0 * q2 * q2 - 1.0;
			q6  = q2 * (4.0 * q2 * q2 - 3.0);
			r2  = 2.0 * ssig * csig;
			r3  = ssig * (3.0 - 4.0 * ssig * ssig);
//			the longitude difference 
			double s   = sinalf * (ao * sig + a2 * ssig * q2 + a4 * r2 * q4 + a6 * r3 * q6);
			double xz  = dlon + s;
			double xy  = abs(xz - ab);
			ab  = dlon + s;
			if (xy < PRECISION_LIMIT) break;
		}
//		the coefficients of type b      
		double z   = epsq * w;
		double bo  = 1.0 + z * (1.0 / 4.0 + z * (-3.0 / 64.0 + z * (5.0 / 256.0 - z * 175.0 / 16384.0)));
		double b2  = z * (-1.0 / 4.0 + z * (1.0 / 16.0 + z * (-15.0 / 512.0 + z * 35.0 / 2048.0)));
		double b4  = z * z * (-1.0 / 128.0 + z * (3.0 / 512.0 - z * 35.0 / 8192.0));
		double b6  = z * z * z * (-1.0 / 1536.0 + z * 5.0 / 6144.0);
//		the distance in meters   
		double s = b * (bo * sig + b2 * ssig * q2 + b4 * r2 * q4 + b6 * r3 * q6);		
		// Check for a non-distance ... p1,e1 & p2,e2 equal zero?  If so,
		// set the azimuths to zero.  Otherwise calculate them.
		double az1, az2;
		if (s < 0.00005) {
			az1 = 0.0;
			az2 = 0.0;
		} else {
			// First compute the az1 & az2 for along the equator.
			if (dlon > PI)
				dlon -= TWO_PI;
			else if (dlon < -PI)
				dlon += TWO_PI;
			az1 = dlon < 0 ? PI * 3.0 / 2.0 : PI / 2.0;
			az2 = dlon < 0 ? PI / 2.0 : PI * 3.0 / 2.0;
			// Now compute the az1 & az2 for latitudes not on the equator.
			if (!(abs(su1) < FP_TOLERANCE && abs(su2) < FP_TOLERANCE)) {
				double tana1 =  slon * cu2 / (su2 * cu1 - clon * su1 * cu2);
				double tana2 =  slon * cu1 / (su1 * cu2 - clon * su2 * cu1);
				double sina1 =  sinalf / cu1;
				double sina2 = -sinalf / cu2;
				az1 = atan2(sina1, sina1 / tana1);
				az2 = PI - atan2(sina2, sina2 / tana2);
			}
		}
		return new double[] { s, az1, az2 };
	}

	/**
	 * Compute the length of a meridional arc between two latitudes.
	 *               
	 * Programmed by Robert (Sid) Safford; released for field use 5 Jul 1975.
	 * 
	 * @param	amax		The semi-major axis of the reference ellipsoid,
	 * @param	flat		The flattening (0.0033528 ... ).
	 * @param	esq			Eccentricity squared for reference ellipsoid.
	 * @param	p1			The latitude of station 1.
	 * @param	p2			The latitude of station 2.
	 * @return				An array of doubles, containing: the geodesic
	 * 						distance between the stations, in meters;
	 * 						the azimuth at station 1 to station 2;
	 * 						and the azimuth at station 2 to station 1.
	 */
	private static double[] gpnarc(double amax, double flat, double esq,
							       double p1, double p2)
	{
		// Check for a 90 degree lookup.
		boolean ninety = abs(p1) < FP_TOLERANCE && abs(abs(p2) - PI / 2) < FP_TOLERANCE;
		double da = p2 - p1;
		double s1 = 0.0;
		double s2 = 0.0;
		// Compute the length of a meridional arc between two latitudes.
		double e2 = esq;
		double e4 = e2 * e2;
		double e6 = e4 * e2;
		double e8 = e6 * e2;
		double ex = e8 * e2;
		double t1 = e2 * (003.0 / 4.0);
		double t2 = e4 * (015.0 / 64.0);
		double t3 = e6 * (035.0 / 512.0);
		double t4 = e8 * (315.0 / 16384.0);
		double t5 = ex * (693.0 / 131072.0);
		double a = 1.0 + t1 + 3.0 * t2 + 10.0 * t3 + 35.0 * t4 + 126.0 * t5;
		if (!ninety) {
			double b  = t1 + 4.0 * t2 + 15.0 * t3 + 56.0 * t4 + 210.0 * t5;
			double c  = t2 + 06.0 * t3 + 28.0 * t4 + 120.0 * t5;
			double d  = t3 + 08.0 * t4 + 045.0 * t5;
			double e  = t4 + 010.0 * t5;
			double f  = t5;
			double db = sin(p2 *  2.0) - sin(p1 *  2.0);
			double dc = sin(p2 *  4.0) - sin(p1 *  4.0);
			double dd = sin(p2 *  6.0) - sin(p1 *  6.0);
			double de = sin(p2 *  8.0) - sin(p1 *  8.0);
			double df = sin(p2 * 10.0) - sin(p1 * 10.0);
//			compute the s2 part of the series expansion
			s2 = -db * b / 2 + dc * c / 4 - dd * d / 6 + de * e / 8 - df * f / 10;
		}
//		compute the s1 part of the series expansion
		s1 = da * a;
//		compute the arc length
		double arc = amax * (1.0 - esq) * (s1 + s2);
		// Make the return array.
		double[] ret = new double[3];
		ret[0] = abs(arc);		
		// Calculate the forward and back azimuths, which will be
		// north and south or vice versa.
		if (p2 > p1) {
			ret[1] = 0.0;
			ret[2] = PI;
		} else {
			ret[1] = PI;
			ret[2] = 0.0;
		}
		return ret;
	}


	/**
	 * Subroutine to compute the lift-off-azimuth constants.
	 *               
	 * Programmed by Robert (Sid) Safford; released for field use 10 Jun 1985.
	 * 
	 * @param	a			The semi-major axis of the reference ellipsoid,
	 * @param	f			The flattening (0.0033528 ... ).
	 * @param	esq			Eccentricity squared for reference ellipsoid.
	 * @param	dlon		The longitude difference.
	 * @return				An array of doubles, containing: the geodesic
	 * 						distance between the stations, in meters;
	 * 						the azimuth at station 1 to station 2;
	 * 						and the azimuth at station 2 to station 1.
	 */
	private static double[] gpnloa(double a, double f, double esq, double dlon) {
		double absDlon = abs(dlon);
		double cons = (PI - absDlon) / (PI * f);
//		compute an approximate az
		double az = asin(cons);
		double t1   =    1.0;
		double t2   =  (-1.0 / 4.0) * f * (1.0 + f + f * f);
		double t4   =    3.0 / 16.0 * f * f * (1.0 + (9.0 / 4.0) * f);
		double t6   = (-25.0 / 128.0) * f * f * f;
		double ao = 0;
		double s = 0;
		for (int iter = 0; iter < 7; ++iter) {
			s    = cos(az);
			double c2   = s * s;
//			compute new ao
			ao = t1 + t2 * c2 + t4 * c2 * c2 + t6 * c2 * c2 * c2;
			double cs = cons / ao;
			s = asin(cs);
			if (abs(s - az) < PRECISION_LIMIT) break;
			az = s;
		}
		double az1 = s;
		if (dlon < 0.0) az1 = 2.0 * PI - az1;
		double az2 = 2.0 * PI - az1;
//		equatorial - geodesic  (s - s)   "sms"
		double esqp = esq / (1.0 - esq);
		s = cos(az1);
		double u2   = esqp * s * s;
		double u4   = u2 * u2;
		double u6   = u4 * u2;
		double u8   = u6 * u2;
		t1   =     1.0;
		t2   =    (1.0 / 4.0) * u2;
		t4   =   (-3.0 / 64.0) * u4;
		t6   =    (5.0 / 256.0) * u6;
		double t8   = (-175.0 / 16384.0) * u8;
		double bo   = t1 + t2 + t4 + t6 + t8;
		s    = sin(az1);		
		// Compute s - s: the equatorial - geodesic distance between the
		// stations, in meters.
		double sms  = a * PI * (1.0 - f * abs(s) * ao - bo * (1.0 - f));
		// And now compute the geodesic distance, which is the equatorial
		// distance (calculated from the axis) minus sms.
		double equDist = a * absDlon;
		double geoDist = equDist - sms;
		return new double[] { geoDist, az1, az2 };
	}
	
	// Two times PI (handy sometimes).
	private static final double TWO_PI = 2 * PI;

	// Floating-point tolerance.  Two values closer than this can be considered to be the same.
	private static final double FP_TOLERANCE = 5.0e-15;
	
	// Tolerance used when comparing values against meridians or the equator.
	private static final double GEO_TOLERANCE = 5.0e-14;
	
	// A looser version of FP_TOLERANCE.
	private static final double PRECISION_LIMIT = 0.5e-13;
	
	// Points closer to the equator than this are candidates to be consider anti-nodal.
	private static final double NODAL_LIMIT = 7.0e-03;
	
}

class Position {
    
    public static final Position UNKNOWN = new Position(Double.NaN, Double.NaN, true);
    
    public Position(double latRadians, double lonRadians) { this(latRadians, lonRadians, false); }

    private Position(double latRadians, double lonRadians, boolean allowNan) {
        if (!allowNan) {
            if (Double.isNaN(latRadians) || Double.isInfinite(latRadians) ||
                    Double.isNaN(lonRadians) || Double.isInfinite(lonRadians))
                throw new IllegalArgumentException("Components of a Position must be finite");
        }        
        init(latRadians, lonRadians);
    }

	
	public Position(Position pos) {
		init(pos.latitudeR, pos.longitudeR);
	}


	private void init(double latRadians, double lonRadians) {
	    if (!Double.isNaN(latRadians) && !Double.isNaN(lonRadians)) {
	        if (latRadians < -PI / 2)
	            latRadians = -PI / 2;
	        else if (latRadians > PI / 2)
	            latRadians = PI / 2;
	        while (lonRadians < 0)
	            lonRadians += 2 * PI;
	        lonRadians = (lonRadians + PI) % (2 * PI) - PI;
	    }	    
        latitudeR = latRadians;
        longitudeR = lonRadians;
	}

	public static Position fromDegrees(double latDegrees, double lonDegrees) {
        if (Double.isNaN(latDegrees) || Double.isInfinite(latDegrees) ||
                Double.isNaN(lonDegrees) || Double.isInfinite(lonDegrees))
            throw new IllegalArgumentException("Components of a Position" +
                                               " must be finite");
        
		return new Position(toRadians(latDegrees), toRadians(lonDegrees));
	}

	
	public double getLatRads() {
		return latitudeR;
	}

	public double getGeocentricLat() {
	    if (Double.isNaN(latitudeR))
	        return Double.NaN;	    
		final double f1 = toRadians(692.73 / 3600.0);
		final double f2 = toRadians(1.16 / 3600.0);
		double Δφ = f1 * sin(2 * latitudeR) - f2 * sin(4 * latitudeR);
		return latitudeR - Δφ;
	}

	public double getLonRads() {
		return longitudeR;
	}

	public double getLatDegs() {
        if (Double.isNaN(latitudeR))
            return Double.NaN;        
		return toDegrees(latitudeR);
	}

	public double getLonDegs() {
        if (Double.isNaN(longitudeR))
            return Double.NaN;        
		return toDegrees(longitudeR);
	}

    public double getCentreDistance() {
        if (Double.isNaN(latitudeR))
            return Double.NaN;        
        double ρ = 0.9983271 +
                   0.0016764 * cos(2 * latitudeR) -
                   0.0000035 * cos(4 * latitudeR);
        return ρ;
    }

    public String formatDegMin() {
        return Angle.formatDegMin(toDegrees(latitudeR), 'N', 'S') + ' ' +
        	   Angle.formatDegMin(toDegrees(longitudeR), 'E', 'W');
    }

    public String formatDegMinSec() {
        return Angle.formatDegMinSec(toDegrees(latitudeR), 'N', 'S') + ' ' +
        	   Angle.formatDegMinSec(toDegrees(longitudeR), 'E', 'W');
    }

    public String toString() {
        return formatDegMin();
    }
    
	private double latitudeR;

	private double longitudeR;

}

class Angle {

	public static final double HALFPI = PI / 2;

	public static final double TWOPI = PI * 2;

	public Angle(double radians) { angleR = radians; }

	public static Angle fromDegrees(double degrees) { return new Angle(toRadians(degrees)); }

	public static Angle fromDegrees(int d, int m, double s) {
		boolean neg = d < 0 || m < 0 || s < 0;
		double df = ((abs(s) / 60.0 + abs(m)) / 60.0 + abs(d));
		return new Angle(toRadians(neg ? -df : df));
	}
	
	public static Angle fromRightAscension(int rh, int rm, double rs) {
		boolean neg = rh < 0 || rm < 0 || rs < 0;
		double ra = ((abs(rs) / 60.0 + abs(rm)) / 60.0 + abs(rh)) * 15.0;
		return new Angle(toRadians(neg ? -ra : ra));
	}
	
	public final double getRadians() {
		return angleR;
	}

	public final double getDegrees() {
		return toDegrees(angleR);
	}

	public Angle add(double radians) {
		return new Angle(angleR + radians);
	}

	public static final double modPi(double v) {
		v %= PI;
		return v < 0 ? v + PI : v;
	}
	
	public static final double modTwoPi(double v) {
		v %= TWOPI;
		return v < 0 ? v + TWOPI : v;
	}
	
    public String formatDeg() {
        return String.format("%d°", round(toDegrees(angleR)));
    }


    public String formatDegMin() {
        return Angle.formatDegMin(toDegrees(angleR)) + '°';
    }

    public String formatDegMinSec() {
        return Angle.formatDegMinSec(toDegrees(angleR)) + '°';
    }

    public String toString() {
        return formatDeg();
    }
    
	public static String formatFloat(double val, int frac) {
		floatFormat.setMaximumFractionDigits(frac);
		return floatFormat.format(val);
	}


	public static String formatBearing(double val) {
		return Math.round(val) + "°";
	}

	public static String formatDegMin(double angle) {
		return formatDegMin(angle, ' ', '-');
	}


	public static String formatDegMin(double angle, char pos, char neg) {
		StringBuilder sb = new StringBuilder(12);
		formatDegMin(angle, pos, neg, sb);
		return sb.toString();
	}


	public static void formatDegMin(double angle, char pos, char neg, StringBuilder sb) {
		if (sb.length() != 12)
			sb.setLength(12);		
		if (angle < 0) {
			sb.setCharAt(0, neg);
			angle = -angle;
		} else
			sb.setCharAt(0, pos);
		int deg = (int) angle;
		int min = (int) (angle * 60.0 % 60.0);
		int frac = (int) (angle * 60000.0 % 1000.0);
		sb.setCharAt( 1, deg < 100 ? ' ' : (char) ('0' + deg / 100));
		sb.setCharAt( 2, deg < 10 ? ' ' : (char) ('0' + deg / 10 % 10));
		sb.setCharAt( 3, (char) ('0' + deg % 10));
		sb.setCharAt( 4, '°');
		sb.setCharAt( 5, (char) ('0' + min / 10));
		sb.setCharAt( 6, (char) ('0' + min % 10));
		sb.setCharAt( 7, '.');
		sb.setCharAt( 8, (char) ('0' + frac / 100));
		sb.setCharAt( 9, (char) ('0' + frac / 10 % 10));
		sb.setCharAt(10, (char) ('0' + frac % 10));
		sb.setCharAt(11, '\'');
	}
	
	public static String formatDegMinSec(double angle) {
		return formatDegMinSec(angle, ' ', '-');
	}
	
	public static String formatDegMinSec(double angle, char posSign, char negSign) {
		char sign = angle >= 0 ? posSign : negSign;
		angle = Math.abs(angle);
		int deg = (int) angle;
		angle = (angle - deg) * 60.0;
		int min = (int) angle;
		angle = (angle - min) * 60.0;
		double sec = angle;
		if (sec >= 60.0) {
			sec = 0;
			++min;
		}
		if (min >= 60) {
			min -= 60;
			++deg;
		}		
		return String.format("%s%3d° %2d' %8.5f\"", sign, deg, min, sec);
	}

	public static String formatLatLon(double lat, double lon) {
		return formatDegMin(lat, 'N', 'S') + " " + formatDegMin(lon, 'E', 'W');
	}

	public static String formatRightAsc(double angle) {
		if (angle < 0) angle += 360.0;
		double hours = angle / 15.0;
		int h = (int) hours;
		hours = (hours - h) * 60.0;
		int m = (int) hours;
		hours = (hours - m) * 60.0;
		double s = hours;
		if (s >= 60.0) {
			s = 0;
			++m;
		}
		if (m >= 60) {
			m -= 60;
			++h;
		}
		if (h >= 24) {
			h -= 24;
		}		
		return String.format("%02dh %02d' %08.5f\"", h, m, s);
	}

	private static NumberFormat intFormat = null; 
        static {
		intFormat = NumberFormat.getInstance();
		intFormat.setMinimumIntegerDigits(3);
		intFormat.setMaximumIntegerDigits(3);
		intFormat.setMaximumFractionDigits(0);
	}

	private static NumberFormat floatFormat = null;
	static {
		floatFormat = NumberFormat.getInstance();
		floatFormat.setMinimumFractionDigits(0);
		floatFormat.setMaximumFractionDigits(7);
	}
	
	private static NumberFormat angleFormat = null;
	static {
		angleFormat = NumberFormat.getInstance();
		angleFormat.setMinimumFractionDigits(0);
		angleFormat.setMaximumFractionDigits(3);
	}
    
	private double angleR;

}

class Azimuth extends Angle {

	public Azimuth(double radians) {
		super(modTwoPi(radians));
	}

	public static Azimuth fromDegrees(double degrees) {
		return new Azimuth(toRadians(degrees));
	}

	@Override
	public Azimuth add(double radians) {
		return new Azimuth(getRadians() + radians);
	}

}

final class Distance {

	public static final Distance ZERO = new Distance(0);
	
	
	public Distance(double metres) {
		distanceM = metres;
	}

	public static Distance fromFeet(double feet) {
		return new Distance(feet * FOOT);
	}

	public static Distance fromNm(double nmiles) {
		return new Distance(nmiles * NAUTICAL_MILE);
	}

	public final double getMetres() {
		return distanceM;
	}

	public final double getFeet() {
		return distanceM / FOOT;
	}


	public final double getNm() {
		return distanceM / NAUTICAL_MILE;
	}

	public Distance add(Distance d) {
		if (d == null || d == ZERO)
			return this;
		if (this == ZERO)
			return d;
		return new Distance(distanceM + d.distanceM);
	}

        public static final String formatM(double m) {
		floatFormat.setMaximumFractionDigits(1);
		return floatFormat.format(m) + " m";
        }

        public final String formatM() {
		return formatM(distanceM);
        }

        public static final String formatNm(double m) {
		floatFormat.setMaximumFractionDigits(1);
		return floatFormat.format(m / NAUTICAL_MILE) + " nm";
        }

        public final String formatNm() {
		return formatNm(distanceM);
        }

        public static final String describeNautical(double m) {
    	        final double feet = m / FOOT;
		if (feet < 1000) return "" + (int) Math.round(feet) + " feet";
		final double nm = m / NAUTICAL_MILE;
		if (nm < 10) {
			floatFormat.setMaximumFractionDigits(1);
			return floatFormat.format(nm) + " nm";
		}
		return "" + (int) Math.round(nm) + " nm";
	}
	
        public final String describeNautical() {
		return describeNautical(distanceM);
	}
	
        @Override
        public String toString() {
           return formatNm();
        }
    
	// The length of an international standard foot, in metres.
	private static final double FOOT = 0.3048;

	// The length of an international standard nautical mile, in metres.
	private static final double NAUTICAL_MILE = 1852;
	
	// Number formatter for floating-point values.
	private static NumberFormat floatFormat = null;
	static {
		floatFormat = NumberFormat.getInstance();
		floatFormat.setMinimumFractionDigits(0);
		floatFormat.setMaximumFractionDigits(1);
	}
	
	/**
	 * The distance in metres.
	 */
	private double distanceM;

}

final class Vector {

	public Vector(Distance distance, Azimuth azimuth) {
		this.distance = distance;
		this.azimuth = azimuth;
	}

	public static Vector fromMetresRadians(double metres, double radians) {
		return new Vector(new Distance(metres), new Azimuth(radians));
	}

	public static Vector fromNmRadians(double nmiles, double radians) {
		return new Vector(Distance.fromNm(nmiles), new Azimuth(radians));
	}

	public final Azimuth getAzimuth() {
		return azimuth;
	}

	public final double getAzimuthRadians() {
		return azimuth.getRadians();
	}

	public final double getAzimuthDegrees() {
		return azimuth.getDegrees();
	}

	public final Distance getDistance() {
		return distance;
	}

	public final double getDistanceMetres() {
		return distance.getMetres();
	}

	public final double getDistanceNm() {
		return distance.getNm();
	}

        public String formatDegMin() {
                return distance.formatM() + ' ' + azimuth.formatDegMin();
        }

        public String formatDegMinSec() {
                return distance.formatM() + ' ' + azimuth.formatDegMinSec();
        }

        @Override
        public String toString() {
                return formatDegMin();
        }
    
	private Distance distance;

	private Azimuth azimuth;

}
