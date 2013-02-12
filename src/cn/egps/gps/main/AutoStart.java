package cn.egps.gps.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		try{
			Intent myIntent = new Intent(context,GpsService.class);  
			//myIntent.setAction("cn.egps.gps.main.GpsService");  
			context.startService(myIntent); 
		}catch(Exception ex){
			GpsService.writeLog(Utils.printException(ex));
		}
		 
	}

}
