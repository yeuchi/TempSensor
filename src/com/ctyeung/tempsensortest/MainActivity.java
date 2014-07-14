package com.ctyeung.tempsensortest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener{

	private SensorManager mSensorManager;
	private String strSensors;
	private TextView txtBatteryTemp;
	private TextView txtAmbientTemp;
	private TextView txtBatteryLevel;
	
	private String lastBatteryTemp;
	private String lastAmbientTemp;
	
	private Sensor tempSensor;
	private int numUpdates = 0;
	
	//battery change - http://www.tutorialforandroid.com/2009/01/getting-battery-information-on-android.html
	//Sensor overview - http://developer.android.com/guide/topics/sensors/sensors_overview.html
	//Crowd source temperature info. http://galaxy-note2.wonderhowto.com/how-to/get-ambient-weather-readings-instantly-using-your-samsung-galaxy-note-2s-built-sensors-0146896/
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		defineSensors();
		
		lastBatteryTemp = "";
		lastAmbientTemp = "";
		this.registerReceiver(this.mBatInfoReceiver, 
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void defineSensors()
	{
		//Context context = this.getBaseContext();
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		List<Sensor> tempList = mSensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);

		strSensors = "";
		if(tempList!=null && tempList.size()>0)
		{
			tempSensor = tempList.get(0);
			TextView txtAmbientType = (TextView)findViewById(R.id.ambient_type);
			String vendor = tempList.get(0).getVendor();
			int version = tempList.get(0).getVersion();
			String name = tempList.get(0).getName();
			
			txtAmbientType.setText("Ambient: name:" + name + 
									" vendor:" + vendor + 
									" version:" + String.valueOf(version));
		}
		/*
		if(mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)!=null)
		{
			strSensors += "presure ";
		}
		if(mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)!=null)
		{
			strSensors += "humidity ";
		} */
	}
	
	@Override
	  public final void onAccuracyChanged(Sensor sensor, int accuracy) {
	    // Do something here if sensor accuracy changes.
		
		if(sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
			TextView txtBatteryTemp = (TextView)findViewById(R.id.battery_temp);
			String str = "Battery temp accuracy changed:" + String.valueOf(accuracy);
			txtBatteryTemp.setText(str);
			// write to file
			write2file(str);
		}
	  }

	  @Override
	  public final void onSensorChanged(SensorEvent event) {
	    // The light sensor returns a single value.
	    // Many sensors return 3 values, one for each axis.
		TextView txtAmbientView = (TextView)findViewById(R.id.ambient_temp);
		String str = "Ambient temp:" + event.values[0];
		txtAmbientView.setText(str);
		// write to file
		
		if(!str.contentEquals(lastAmbientTemp)){
			lastAmbientTemp = str;
			write2file(str);
		}
	  }

	  @Override
	  protected void onResume() {
	    super.onResume();
	    
	    if(mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)!=null)
	    	mSensorManager.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_NORMAL);
	  }

	  	@Override
  		protected void onPause() {
	  		super.onPause();
	  		mSensorManager.unregisterListener(this);
	  	}

		private void write2file(String str)
		{
			DateFormat df = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
			String date = df.format(Calendar.getInstance().getTime());
			
			String filename = "sensorData.txt";
		    File myFile = new File(Environment
		            .getExternalStorageDirectory(), filename);
		    try {
			    if (!myFile.exists())
			        myFile.createNewFile();
			    
			    FileOutputStream fos;
			    String buf = date + " " + str + "\r\n";
			    byte[] data = buf.getBytes();
			        fos = new FileOutputStream(myFile, true);
			        fos.write(data);
			        fos.flush();
			        fos.close();
					
					TextView lastDateView = (TextView)findViewById(R.id.last_update);
			        lastDateView.setText(date);
			        
			        numUpdates ++;
			        TextView numUpdateView = (TextView)findViewById(R.id.num_updates);
			        numUpdateView.setText("Update count:" + String.valueOf(numUpdates));
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
		    @Override
		    public void onReceive(Context arg0, Intent intent) {
		      // TODO Auto-generated method stub
		      double temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0;
		      int level = intent.getIntExtra("level", 0);

		      txtBatteryTemp = (TextView)findViewById(R.id.battery_temp);
		      String str1 = "Battery temp:" + String.valueOf(temp)+ "C";
		      txtBatteryTemp.setText(str1);
		      txtBatteryLevel = (TextView)findViewById(R.id.battery_level);
		      String str2 = "Battery level:"+String.valueOf(level);
		      txtBatteryLevel.setText(str2);
		      
		      // write to file
		      if(!lastBatteryTemp.contentEquals(str1)){
		    	  lastBatteryTemp = str1;
		    	  write2file(lastBatteryTemp + " " + str2);
		      }
		    }
		};
}
