package com.mercator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class TileUtils {
	
	
	public static String parseXyz2Bound(int x,int y,int z){
		
		StringBuilder sb = new StringBuilder("POLYGON ((");
		
		double lngLeft =
				MercatorProjection.tileXToLongitude(x, (byte)z);
		//-0.0015
		
		double latUp =
				MercatorProjection.tileYToLatitude(y, (byte)z);
		
		double lngRight =
				MercatorProjection.tileXToLongitude(x + 1, (byte)z);
		
		double latDown =
				MercatorProjection.tileYToLatitude(y + 1, (byte)z);
		
		sb.append(lngLeft +" "+latUp+", ");
		
		sb.append(lngRight +" "+latUp+", ");
		
		sb.append(lngRight +" "+latDown+", ");
		
		sb.append(lngLeft +" "+latDown+", ");
		
		sb.append(lngLeft +" "+latUp+")) ");
		
		return sb.toString();
		
	}
/**
　　* @description: 根据瓦片级别行列号构建瓦片范围，该范围含有一定的缓冲区，大于原始范围。
　　* @param
　　* @return
　　* @throws
　　* @author wwj
　　* @date 2019/7/26 9:26
　　*/
    public static Envelope parseXyz2Envelope(int x, int y, int z){

	    double lngLeft = MercatorProjection.tileXToLongitude(x, (byte)z)-0.00105;

        double latUp = MercatorProjection.tileYToLatitude(y, (byte)z)+0.00105;

        double lngRight = MercatorProjection.tileXToLongitude(x + 1, (byte)z)+0.00105;

        double latDown = MercatorProjection.tileYToLatitude(y + 1, (byte)z) -0.00105;

       Envelope env=new Envelope();
       env.init(lngLeft,lngRight,latDown,latUp);
         return env;

    }
	public static void convert2Piexl(int x,int y,int z,Geometry geom){
		
		double px = MercatorProjection.tileXToPixelX(x);
		
		double py = MercatorProjection.tileYToPixelY(y);
		
		Coordinate[] cs = geom.getCoordinates();
		
		byte zoom = (byte)z;
		
		for(Coordinate c : cs){
			c.x = (int)(((MercatorProjection.longitudeToPixelX(c.x, zoom)) - px) * 16);
			
			c.y = (int)(((MercatorProjection.latitudeToPixelY(c.y, zoom)) - py) * 16);
			
//			c.z = 218;
		}
		
	}
	/**
	 * tile Pixel to Geographic coordinate
	 * @param z
	 * @param geom
	 */
	public static void pixelConvert2Geo(int z,Geometry geom){
		Coordinate[] cs = geom.getCoordinates();

		byte zoom = (byte)z;

		for(Coordinate c : cs){
			c.x = MercatorProjection.pixelXToLongitude(c.x, zoom);

			c.y = MercatorProjection.pixelYToLatitude(c.y, zoom);

//			c.z = 218;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		WKTReader reader = new WKTReader();
		
		Geometry geom = reader.read("POLYGON ((101.25 48.92249926375824, 112.5 48.92249926375824, 112.5 40.97989806962013, 101.25 40.97989806962013, 101.25 48.92249926375824))");

//		System.out.println(MercatorProjection.longitudeToTileX((101.25 + 112.5)/2, (byte)5));
//		
//		System.out.println(MercatorProjection.latitudeToTileY((48.92249926375824 + 40.97989806962013)/2, (byte)5));
		
		convert2Piexl(25, 11, 5, geom);
		
	}

}
