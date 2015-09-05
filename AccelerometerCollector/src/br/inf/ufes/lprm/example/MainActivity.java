package br.inf.ufes.lprm.example;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener  {

	private SensorManager sensorManager;
	private float ax,ay,az;
	private boolean connected = false;
	private EditText edtServerIp;
	private TextView txtX, txtY, txtZ, txtIp, txtMessage;
	private Button btnConnect, btnDisconnect;
	private DatagramSocket clientSocket;    
	private InetAddress serverIPAddress = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {       	   
			setContentView(R.layout.activity_main);
			sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
			sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

			View v = getLayoutInflater().inflate(R.layout.activity_main, null);
			v.setKeepScreenOn(true);
			setContentView(v);

			WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
			WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
			int ip = wifiInfo.getIpAddress();
			String ipAddress = Formatter.formatIpAddress(ip);

			txtIp = (TextView)findViewById(R.id.txtIp);
			edtServerIp = (EditText)findViewById(R.id.edtServerIp);
			txtX = (TextView)findViewById(R.id.txtXValue);
			txtY = (TextView)findViewById(R.id.txtYValue);
			txtZ = (TextView)findViewById(R.id.txtZValue);
			btnConnect = (Button) findViewById(R.id.btnConnect);
			btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
			txtMessage = (TextView)findViewById(R.id.txtMessage);

			txtIp.setText(String.valueOf("IP Address: " + ipAddress));

			btnConnect.setOnClickListener(new View.OnClickListener(){             
				public void onClick(View v) {                 
					try {
						sendMessage(edtServerIp.getText().toString(), "ping");
					} 
					catch (Exception e) {
						e.printStackTrace();
					}  
				}
			}); 

			btnDisconnect.setOnClickListener(new View.OnClickListener(){             
				public void onClick(View v) {                 
					try {
						closeConnection();
					} 
					catch (Exception e) {
						e.printStackTrace();
					}  
				}
			}); 

		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
			ax=event.values[0];
			ay=event.values[1];
			az=event.values[2];

			StringBuilder stbAcelerometerMetadata = new StringBuilder();
			stbAcelerometerMetadata.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			stbAcelerometerMetadata.append("<InteractionInfo xmlns:ns2=\"urn:mpeg:mpeg-v:2010:01-CT-NS\" xmlns:ns3=\"urn:mpeg:mpeg-v:2010:01-DCV-NS\" xmlns:ns4=\"urn:mpeg:mpeg7:schema:2004\" xmlns:ns5=\"urn:mpeg:mpeg-v:2010:01-SIV-NS\" xmlns:ns6=\"urn:mpeg:mpeg-v:2010:01-IIDL-NS\">");
			stbAcelerometerMetadata.append("    <ns6:SensedInfoList>");
			stbAcelerometerMetadata.append("        <ns6:SensedInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns5:AccelerationSensorType\" sensorIdRef=\"DEV_ID_001\" activate=\"true\">");
			stbAcelerometerMetadata.append("            <ns5:Acceleration>");
			stbAcelerometerMetadata.append("                <ns2:X>" + ax + "</ns2:X>");
			stbAcelerometerMetadata.append("                <ns2:Y>" + ay + "</ns2:Y>");
			stbAcelerometerMetadata.append("                <ns2:Z>" + az + "</ns2:Z>");
			stbAcelerometerMetadata.append("            </ns5:Acceleration>");
			stbAcelerometerMetadata.append("        </ns6:SensedInfo>");
			stbAcelerometerMetadata.append("    </ns6:SensedInfoList>");
			stbAcelerometerMetadata.append("</InteractionInfo>");

			if (connected){
				sendMessage(edtServerIp.getText().toString(), stbAcelerometerMetadata.toString());
			}

			txtX.setText(String.valueOf(ax));
			txtY.setText(String.valueOf(ay));
			txtZ.setText(String.valueOf(az));
		}		
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	private class NetUdp extends AsyncTask<String, String, Boolean> {        

		protected Boolean doInBackground(String... params) {
			try {
				if (!connected){
					clientSocket = new DatagramSocket();
					serverIPAddress = InetAddress.getByName(params[0]);
					runOnUiThread(new Runnable() {
						public void run() {
							txtMessage.setText(String.valueOf("Connected"));
						}
					});
					connected = true;
				}
				DatagramPacket packet = new DatagramPacket(params[1].getBytes(), params[1].length(), serverIPAddress, 12345);
				clientSocket.send(packet);                      

			} catch (Exception e) {
				connected = false;
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				final String error = errors.toString();
				runOnUiThread(new Runnable() {
					public void run() {
						txtMessage.setText(String.valueOf(error));
					}
				});
				e.printStackTrace();
			}
			return connected; 
		}

	}
	private void sendMessage(String ipDest, String message){
		new NetUdp().execute(ipDest, message);   
	}

	private void closeConnection(){
		if (connected){
			connected = false;
			clientSocket.close();
			txtMessage.setText(String.valueOf("Not connected"));
		}
	}

	@Override
	public void onBackPressed() {
		closeConnection();
		finish();
		return;
	}  

}