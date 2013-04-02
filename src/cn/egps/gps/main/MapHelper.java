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
			get.addHeader("Accept-Language", "zh-cn");
			get.addHeader("Accept","*/*");
			
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
