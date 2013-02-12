package cn.egps.gps.main;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import android.app.Activity;

public class Utils {

	public static List<Activity> activityStake=new ArrayList<Activity>();
	
	public static boolean isNullOrEmpty(String input){
		return (null==input || "".equals(input)) ? true : false;
	}
	
	public static String isNull(String input){
		return null==input ? "" : input;
	}
	
	public static String now(){
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date());
	}
	
	public static String formatChinaDate(Date date){
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}
	
	public static String formatDate(Date date){
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
		return format.format(date);
	}
	
	public static String printException(Exception ex){
		  StringWriter sw = new StringWriter();
	      PrintWriter pw = new PrintWriter(sw, true);
	      ex.printStackTrace(pw);
	      pw.flush();
	      sw.flush();
	      return sw.toString();
	}
	
	public static long dateToSpan(String date){
		long time=-1;
		try{
			//2012-07-03 10:54:46
			Calendar cal=Calendar.getInstance();
			cal.clear();
			String year=date.substring(0,4);
			cal.set(Calendar.YEAR, Integer.parseInt(year));
		
			String month=date.substring(5,7);
			cal.set(Calendar.MONTH, Integer.parseInt(month.startsWith("0") ? month.substring(1) : month));
			String day=date.substring(8, 10);
			cal.set(Calendar.DATE, Integer.parseInt(day.startsWith("0") ? day.substring(1) : day));
			
			String hour=date.substring(11,13);
			cal.set(Calendar.HOUR, Integer.parseInt(hour.startsWith("0") ? hour.substring(1) : hour));
			String minute=date.substring(14,16);
			cal.set(Calendar.MINUTE, Integer.parseInt(minute.startsWith("0") ? minute.substring(1) : minute));
			String second=date.substring(17,19);
			cal.set(Calendar.SECOND, Integer.parseInt(second.startsWith("0") ? second.substring(1) : second));
			time=cal.getTime().getTime();
			
			System.out.println(year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return time;
	}
	
	
	public static byte[] getBytes(String str,int len){
		byte[] buff=new byte[len];
		if(null!=str){
			int length=str.trim().getBytes().length;
			if(length<=len)
				System.arraycopy(str.getBytes(), 0, buff, 0,length);
			else
				System.arraycopy(str.getBytes(), 0, buff, 0,len);
		}
		//不够不用补0，自动会用0填充
		//for(int i=length;i<len;i++){
		//	buff[i]=0;
		//}
		return buff;
	}
	
	
	
	public static Date parseDate(String times){
	    Date date=new Date();
	    date.setTime(Long.parseLong(times));
	    return date;
	}
	

	
	
}
