package cn.egps.gps.main;

import java.util.Iterator;
import java.util.List;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Button.OnClickListener{
	
	private Button btnStart;
	private Button btnStop;
	private EditText editIp;
	private EditText editPort;
	private TextView txtImei;
	private TextView txtAddress;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        editIp=(EditText) findViewById(R.id.ip);
        editPort=(EditText) findViewById(R.id.port);     
        txtImei=(TextView) findViewById(R.id.txtimei);
        txtImei.setText(getIMEI());       
        txtAddress=(TextView) findViewById(R.id.txtAddress);       
        btnStart=(Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);
        btnStop=(Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(this);
        
        SharedPreferences shared=this.getSharedPreferences("gpsService", Context.MODE_PRIVATE);
        String ip=shared.getString("ip", "");
        String port= shared.getString("port", "");
        
        editIp.setText(ip);
        editPort.setText(port);
        
        if(serviceIsStart("cn.egps.gps.main.GpsService")){
        	btnStart.setEnabled(false);
        	btnStop.setEnabled(true);
        	btnStart.setBackgroundResource(R.drawable.btn_buy_disable);
        	btnStop.setBackgroundResource(R.drawable.button);
        }else{
        	btnStop.setEnabled(false);
        	btnStart.setEnabled(true);
        	btnStop.setBackgroundResource(R.drawable.btn_buy_disable);
        	btnStart.setBackgroundResource(R.drawable.button);
        }
        
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constant.RECEIVEADDRESS);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        this.registerReceiver(receiver, filter);
        
    }
    
    protected String getIMEI(){
		TelephonyManager manager=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getDeviceId();
	}

    protected boolean serviceIsStart(String className){
		ActivityManager mActivityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> mServiceList = mActivityManager.getRunningServices(30);
		Iterator<ActivityManager.RunningServiceInfo> it=mServiceList.iterator();
		while(it.hasNext()){
			ActivityManager.RunningServiceInfo info=it.next();
			if(info.service.getClassName().equals(className)){
				return  true;
			}
		}
		return false;
	}
   
    
    private BroadcastReceiver receiver=new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if(Constant.RECEIVEADDRESS.equals(intent.getAction())){
				Bundle bundle=intent.getExtras();
				LocationParams params=(LocationParams) bundle.getSerializable("location");
				txtAddress.setText("经度："+params.getLng()+"   纬度:"+params.getLat()+"   地址:"+params.getAddress()+"    时间:"+params.getTime());
			}
		}
    };
    
    
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
			case R.id.btnStart:{
				String ip=editIp.getText().toString().trim();
				String port=editPort.getText().toString().trim();
				if("".equals(ip)){
					Toast.makeText(this, "请输入IP", Toast.LENGTH_LONG).show();
				}else if("".equals(port)){
					Toast.makeText(this, "请输入端口", Toast.LENGTH_LONG).show();
				}
				SharedPreferences shared=this.getSharedPreferences("gpsService", Context.MODE_PRIVATE);
				Editor editor=shared.edit();
				editor.putString("ip", ip);
				editor.putString("port", port);
				editor.commit();
				
				Intent intent=new Intent(MainActivity.this,GpsService.class);
				startService(intent);
				btnStart.setEnabled(false);
	        	btnStop.setEnabled(true);
	        	btnStart.setBackgroundResource(R.drawable.btn_buy_disable);
	        	btnStop.setBackgroundResource(R.drawable.button);
			}break;
			case R.id.btnStop:{
				Intent intent=new Intent(MainActivity.this,GpsService.class);
				stopService(intent);
				btnStop.setEnabled(false);
	        	btnStart.setEnabled(true);
	         	btnStop.setBackgroundResource(R.drawable.btn_buy_disable);
	        	btnStart.setBackgroundResource(R.drawable.button);
			}break;
		}
	}
}