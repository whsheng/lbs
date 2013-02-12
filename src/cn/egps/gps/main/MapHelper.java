package cn.egps.gps.main;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;


public class MapHelper {

	
	private LocationManager manager=null;
	
	public Context context=null;
	
	public static String mapKey="0jclL_qJEDHqws1PJxYR94BFhQQvHvIjnoxzHQw";
	
	public MapHelper(LocationManager _manager){
		manager=_manager;
	}

	public MapHelper(LocationManager _manager,Context _context,String _mapKey){
		manager=_manager;
		context=_context;
		mapKey=_mapKey;
	}
	
	/**
	 * gps定位
	 * @return
	 */
	public Location getLocation(){
		//声明一个标准
		Criteria cri=new Criteria();
		//精度
		cri.setAccuracy(Criteria.ACCURACY_FINE);
		//海拔
		cri.setAltitudeRequired(true);
		//气压
		cri.setBearingRequired(false);
		//是否产生费用
		cri.setCostAllowed(false);
		cri.setPowerRequirement(Criteria.POWER_LOW);
		String provider=manager.getBestProvider(cri, true);
		return manager.getLastKnownLocation(provider);
	}
	
	/*
	public GeoPoint getGeoPointByLocation(Location location){
		GeoPoint point=null;
		if(null!=location){
			int lat=(int) ((int)location.getLatitude()*1E6);
			int lng=(int) (location.getLongitude()*1E6);
			point=new GeoPoint(lat,lng);
		}
		return point;
	}
	
	public GeoPoint getLatLngFromAddr(String address){
		Geocoder coder=new Geocoder(context, Locale.CHINA);
		GeoPoint point=null;
		try {
			List<Address> list=coder.getFromLocationName(address, 1);
			if(!list.isEmpty()){
				Address add=list.get(0);
				int lat=(int) (add.getLatitude()*1E6);
				int lng=(int)(add.getLongitude()*1E6);
				point=new GeoPoint(lat,lng);
			}
		} catch (IOException e) {
			Log.e(Common.TAG,e.getMessage());
		}
		return point;
	}
	
	public static GeoPoint getLatLngFromAddress(String address) {
		String param=URLEncoder.encode(address);
		String strUrl="http://maps.google.com/maps/geo?q="+param+"&output=xml&oe=utf8&sensor=true&key="+mapKey;
		GeoPoint point=null;
		try{
			HttpClient client=new DefaultHttpClient();
			HttpResponse response=client.execute(new HttpGet(strUrl));
			StatusLine status=response.getStatusLine();
			if(HttpStatus.SC_OK==status.getStatusCode()){
				String result=EntityUtils.toString(response.getEntity(),Common.ENCODING);
				XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
				XmlPullParser parser=factory.newPullParser();
				StringReader reader=new StringReader(result);
				parser.setInput(reader);
				int eventType=parser.getEventType();
				while(eventType!=XmlPullParser.END_DOCUMENT){
					if(eventType==XmlPullParser.START_TAG){
						if("coordinates".equalsIgnoreCase(parser.getName())){
							String latLng=parser.nextText().trim();
							String[] tempLatLng=latLng.split(",");
							int lat=(int) (Double.valueOf(tempLatLng[1])*1E6);
							int lng=(int) (Double.valueOf(tempLatLng[0])*1E6);
							point=new GeoPoint(lat,lng);
							break;
						}
					}
					eventType=parser.next();
				}
				reader.close();
			}
		}catch(Exception ex){
			Log.e(Common.TAG, ex.getMessage());
		}
		return point;
	}*/
	
	public static String getAddressByGeoPoint(String latlng){
		//String latlng=location.getLatitude()+","+location.getLongitude();
		String url="http://maps.google.com/maps/geo?q="+latlng+"&output=xml&oe=utf8&sensor=false&ion=cn&key="+mapKey;
		InputStream inStream=null;
		String result=null;
		HttpClient client=new DefaultHttpClient();
		client.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT,
                  15000); // 超时设置
		client.getParams().setIntParameter(
                  HttpConnectionParams.CONNECTION_TIMEOUT, 15000);// 连接超时
		try {
			HttpGet get=new HttpGet(url);
			//get.addHeader("User-Agent", " Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; InfoPath.3; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			get.addHeader("Accept-Language", "zh-cn");
			//get.addHeader("Content-Type","text/xml;charset=UTF-8");
			get.addHeader("Accept","*/*");
			//get.addHeader("Accept-Encoding","gzip, deflate");
			
			HttpResponse response=client.execute(get);
			StatusLine status=response.getStatusLine();
			if(status.getStatusCode()==HttpStatus.SC_OK){
				inStream=response.getEntity().getContent();
				XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
				XmlPullParser parser=factory.newPullParser();
				parser.setInput(inStream, Constant.ENCODING);
				int eventType=parser.getEventType();
				while(eventType!=XmlPullParser.END_DOCUMENT){
					if(eventType==XmlPullParser.START_TAG){
						if(parser.getName().equalsIgnoreCase("address")){
							result=parser.nextText().trim();
							break;
						}
					}
					eventType=parser.next();
				}
			}
			inStream.close();
		}catch (Exception e) {
			Log.e(Constant.TAG, e.getMessage());
		}
		return result;
	}
	
	public static String getAddrByGeoPoint(Context context,Location location){
		StringBuilder sb=new StringBuilder();
		try{
			Geocoder coder=new Geocoder(context, Locale.CHINA);
			List<Address> list=coder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
			if(null!=list && 0!=list.size()){
				Address address=list.get(0);
				for(int i=0;i<address.getMaxAddressLineIndex();i++){
					sb.append(address.getAddressLine(i));
				}
			}
		}catch(Exception ex){
			Log.e(Constant.TAG, ex.getMessage());
		}
		return sb.toString();
	}
	
	
	/**
	 * 获取基站信息
	 * @param context
	 * @return
	 */
	public static GsmCellLocation getCellId(Context context){
		TelephonyManager  tm=(TelephonyManager )context.getSystemService(Context.TELEPHONY_SERVICE);
		GsmCellLocation location = (GsmCellLocation)tm.getCellLocation();
		return location;
	}
	
}
