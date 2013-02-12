package cn.egps.gps.main;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class GpsService extends Service {
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return myBinder;
	}

	private final MyBinder myBinder=new MyBinder();
		
		class MyBinder extends Binder{
			GpsService getMyService(){
				return GpsService.this;
			}
		}
		
		@Override
		public void onCreate() {
			// TODO Auto-generated method stub
			super.onCreate();
			Log.e(Constant.TAG, "监听服务已创建");
		}

		@Override
		public void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			stopBaidu();
			stopSelf();
			Log.e(Constant.TAG, "监听服务已销毁");
		}
		
		
		@Override
		public void onStart(Intent intent, int startId) {
			// TODO Auto-generated method stub
			super.onStart(intent, startId);
			//注册广播监听网络状态
			try{
				Context context=getApplicationContext();
				IntentFilter filter = new IntentFilter();
				filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
				context.registerReceiver(reciver, filter);
				Log.e(Constant.TAG, "监听服务已启动");
				
				//TelephonyManager manager1=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				//Log.e(Constant.TAG, "SSSSSSSSSSSSSSSSSSSSSSSSSSSS:"+manager1.getSubscriberId());
				
				getBaiduLocation();
			}catch(Exception ex){
				writeLog(Utils.printException(ex));
			}
	
			//getGoogleLocation();
		}
		
		public static void writeLog(String msg){
			final String state=Environment.getExternalStorageState();
			if(state.equals(Environment.MEDIA_MOUNTED)){
				String path=Environment.getExternalStorageDirectory().getPath();
				try {
					File file=new File(path+"/gpsService");
					if(!file.exists()){
						file.mkdirs();
					}
					file=new File(path+"/gpsService/log.txt");
					if(!file.exists()){
						file.createNewFile();
					}
					FileWriter writer=new FileWriter(path+"/gpsService/log.txt",true);
					writer.write(msg);
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	
		protected String getIMEI(){
			TelephonyManager manager=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String imei=manager.getDeviceId();
			if(imei.length()>11){
				imei=imei.substring(imei.length()-11);
			}else{
				imei=imei+"00000000000".substring(0,11-imei.length());
			}
			return imei;
		}
		
		private String format(int num){
			if(num<10){
				return "0"+num;
			}else
				return String.valueOf(num);
		}
		
		private String format(float num){
			return String.valueOf(num).replace(".", "").substring(0,2);
		}
		
		//*HQ20,IMEI,经度,纬度,速度,方向,时间,日期#
		//*HQ20013500000000,BA&A1953032232000011404000060803251003&B0300000000#
		private void sendData(LocationParams params){
				try{
					Socket socket=new Socket();
					InetSocketAddress address=new InetSocketAddress(InetAddress.getByName(getIp()),Integer.valueOf(getPort()));
					socket.connect(address,15000);
					OutputStream outStream=socket.getOutputStream();
					outStream.write("*HQ200".getBytes());
					outStream.write(getIMEI().getBytes());
					outStream.write(",BA&A".getBytes());
					Date date=new Date();
					outStream.write(format(date.getHours()).getBytes());
					outStream.write(format(date.getMinutes()).getBytes());
					outStream.write(format(date.getSeconds()).getBytes());
					String lat=String.valueOf(params.getLat()).replace(".", "");
					if(lat.length()<8){
						lat=lat+"00000000".substring(0,8-lat.length());
					}
					outStream.write(lat.getBytes());
					String lng=String.valueOf(params.getLng()).replace(".", "");
					if(lng.length()<9){
						lng=lng+"000000000".substring(0,9-lng.length());
					}
					outStream.write(lng.getBytes());
					outStream.write("6".getBytes());
				
					outStream.write(format(params.getSpeed()).getBytes());
					outStream.write(format(params.getDirection()).getBytes());
					outStream.write(format(date.getDate()).getBytes());
					outStream.write(format(date.getMonth()+1).getBytes());
					
					SimpleDateFormat format=new SimpleDateFormat("yy");
					
					outStream.write(format.format(date).getBytes());
					outStream.write("&B0300000000#".getBytes());
					outStream.flush();
					outStream.close();
					socket.close();
				}catch(Exception ex){
					writeLog(Utils.printException(ex));
					Log.e(Constant.TAG, Utils.printException(ex));
				}
		}
		
		
		private String getIp(){
			SharedPreferences shared=GpsService.this.getSharedPreferences("gpsService", Context.MODE_PRIVATE);
			return shared.getString("ip", "justcall.cn");
		}
		
		private String getPort(){
			SharedPreferences shared=GpsService.this.getSharedPreferences("gpsService", Context.MODE_PRIVATE);
			return shared.getString("port", "8091");
		}
		
		BroadcastReceiver reciver=new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				ConnectivityManager manager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			 	NetworkInfo gprs=manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			 	NetworkInfo wifi=manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			 	if(!gprs.isConnected() && !wifi.isConnected()){
			 	}else{
			 		if(null!=tempParams){
			 			sendData(tempParams);
			 			tempParams=null;
			 		}
			 	}
			}
		};
		
		/**
		 * 停止百度
		 */
		private void stopBaidu(){
			if(mLocationClient.isStarted()){
				mLocationClient.stop();
				mLocationClient.unRegisterLocationListener(baiduListener);
			}
		}
		
		
		private void openGps(){
			boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled( getContentResolver(), LocationManager.GPS_PROVIDER );
		    if(gpsEnabled)
		    {
		    //关闭GPS
		    // Settings.Secure.setLocationProviderEnabled( getContentResolver(), LocationManager.GPS_PROVIDER, false );
		    }
		    else
		    {
		     //打开GPS  www.2cto.com
		     Settings.Secure.setLocationProviderEnabled( getContentResolver(), LocationManager.GPS_PROVIDER, true);

		    }


		}
		
		/**
		 * 百度
		 */
		private LocationClient mLocationClient=null;
		private BDLocationListener baiduListener=null;
		private LocationParams tempParams=null;
		private void getBaiduLocation(){
			try{
				mLocationClient = new LocationClient(this);
		        LocationClientOption option = new LocationClientOption();
		        option.setOpenGps(true);								//打开gps
		        option.setCoorType("bd09ll");//bd09ll							//设置坐标类型为bd09ll
		        option.setPriority(LocationClientOption.GpsFirst);	//设置网络优先
		        option.setProdName("android_map");						//设置产品线名称
		        option.setAddrType("detail");
		        
		        option.setScanSpan(15000);//5*60000
		        option.setTimeOut(30000);
		        option.disableCache(false);
		        
		        //定时定位，每隔5秒钟定位一次。
		        mLocationClient.setLocOption(option);
		        
		        baiduListener=new BDLocationListener() {
					@Override
					public void onReceiveLocation(BDLocation location) {
						//openGps();
						if (location == null){
							return ;
						}
						else{
							LocationParams params=new LocationParams();
							params.setLat(location.getLatitude());
							params.setLng(location.getLongitude());
							params.setErrorCode(location.getLocType());
							params.setRadius(location.getRadius());
							
							if(location.getLocType() == BDLocation.TypeGpsLocation){
								params.setSpeed(location.getSpeed());
								//Log.e(Constant.TAG, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABBBBBBBBBBBBBBBBBBBBBBB");
								params.setAltitude(location.getAltitude());
								params.setDirection(location.getDerect());
								String latlng=location.getLatitude()+","+location.getLongitude();
								String address=MapHelper.getAddressByGeoPoint(latlng);
								params.setAddress(address);
							} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
								params.setAddress(location.getAddrStr());
								params.setSpeed(0);
							}
							params.setTime(location.getTime());
							GsmCellLocation cell=MapHelper.getCellId(GpsService.this);
							params.setCid(cell.getCid());
							params.setLac(cell.getLac());
							Log.e(Constant.TAG, "baidu:  "+params.getLat()+"  "+params.getLng()+"   "+params.getSpeed()+"   "+params.getAltitude()
									+"     "+params.getDirection());
							//writeLog("baidu:  "+params.getLat()+"  "+params.getLng()+"   "+params.getSpeed()+"   "+params.getAltitude()
							//		+"     "+params.getDirection());
							if(checkNerWork()){
								sendData(params);
							}else{
								openNetwork();
								tempParams=params;
							}
							Intent intent=new Intent(Constant.RECEIVEADDRESS);
							Bundle bundle=new Bundle();
							bundle.putSerializable("location", params);
							intent.putExtras(bundle);
							GpsService.this.sendBroadcast(intent);
						}
					}
					
			        public void onReceivePoi(BDLocation location){
			        	//return ;
			        }
				};
		        mLocationClient.registerLocationListener(baiduListener);
				
		        mLocationClient.start();
		        mLocationClient.requestLocation();
		        BDLocation location= mLocationClient.getLastKnownLocation();
		        if(null!=location){
		        	
		        }
			}catch(Exception ex){
				writeLog(Utils.printException(ex));
			}
			
		}
		
		
		
		/**
		 * 调用google定位接口
		 * @param info
		 */
		private LocationManager manager=null;
		private void getGoogleLocation(){
			manager = (LocationManager)getSystemService(LOCATION_SERVICE);
			Criteria criteria = new Criteria();
		    criteria.setAccuracy(Criteria.ACCURACY_FINE);//获取精准位置
		    criteria.setCostAllowed(true);//允许产生开销
		    criteria.setPowerRequirement(Criteria.POWER_LOW);//消耗大的话，获取的频率高
		    criteria.setSpeedRequired(true);//手机位置移动
		    criteria.setAltitudeRequired(true);//海拔
		    criteria.setBearingRequired(true);
		    String bestProvider = manager.getBestProvider(criteria, true);//使用GPS卫星
		    
		    if(Utils.isNullOrEmpty(bestProvider)){
		    	
		     }else{
		    	 //Location location = manager.getLastKnownLocation(bestProvider);
		 	     //return location;
		    	 manager.requestLocationUpdates(bestProvider, 5*6000, 8, ll);    //绑定事件监听  
		     }
		     //manager.requestLocationUpdates(bestProvider,5000,20, new MyLocationListener());
		}
		
		   LocationListener ll = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				//manager.getLastKnownLocation(provider)
				String latlng=location.getLatitude()+","+location.getLongitude();
				String address=MapHelper.getAddressByGeoPoint(latlng);
				GsmCellLocation cell=MapHelper.getCellId(GpsService.this);
				
				LocationParams params=new LocationParams();
				params.setAddress(address);
				params.setLat(location.getLatitude());
				params.setLng(location.getLongitude());
				params.setAltitude(location.getAltitude());
				Date date=new Date();
				date.setTime(location.getTime());
				params.setTime(Utils.formatChinaDate(date));
				params.setSpeed(location.getSpeed());
				params.setCid(cell.getCid());
				params.setLac(cell.getLac());
				params.setDirection(location.getBearing());
				Log.e(Constant.TAG, "google:  "+params.getLat()+"  "+params.getLng()+"   "+params.getSpeed()+"   "+params.getAltitude()
						+"     "+params.getDirection());
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				manager.getLastKnownLocation(provider);
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}   
	           
		      
		    }; 
		    
		    
			protected boolean checkNerWork(){
				ConnectivityManager manager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info=manager.getActiveNetworkInfo();
				if(null==info || !info.isAvailable()){
					return false;
				}else{
					return true;
				}
			}
		    
		    
			/**
			 * 打开网络
			 */
			 protected void openNetwork(){
				 //ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				  Object[] arg = null;
				  try {
					  
					  Log.e(Constant.TAG, "KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK开启网络...");
					   boolean isMobileDataEnable = invokeMethod("getMobileDataEnabled", arg);
					   if(!isMobileDataEnable){
						   invokeBooleanArgMethod("setMobileDataEnabled", true);
					   }
				  }catch (Exception e) {
					  // TODO Auto-generated catch block
					  e.printStackTrace();
				  }
			 }
			 
			 
			 @SuppressWarnings({ "rawtypes", "unchecked" })
			private boolean invokeMethod(String methodName,
			            Object[]  arg) throws Exception {
			     ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			     Class ownerClass = mConnectivityManager.getClass();

			     Class[]  argsClass = null;
			     if (arg != null) {
			    	 argsClass = new Class[1];
			         argsClass[0] = arg.getClass();
			     }

				Method method = ownerClass.getMethod(methodName, argsClass);
			     Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);
			     return isOpen;
			 }
			    
			     @SuppressWarnings({ "rawtypes", "unchecked" })
				private Object invokeBooleanArgMethod(String methodName,
			                boolean value) throws Exception {
			     
			      ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

			            Class ownerClass = mConnectivityManager.getClass();

			            Class[]  argsClass = new Class[1];
			                argsClass[0] = boolean.class;

			            Method method = ownerClass.getMethod(methodName,argsClass);

			            return method.invoke(mConnectivityManager, value);
			    }
			     
			     
			     
			     
			     
			     /************************************/
			     
			     
			     
			     
		    
		    
}
