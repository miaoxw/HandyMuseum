package cn.edu.nju.miaoxw.handymuseum.mobile;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

public class MainActivity extends Activity
{
	private Handler handler;
	private BluetoothAdapter bluetoothAdapter;

	private ArrayList<iBeaconStatus> scanResults;

	private Runnable runnable=new Runnable()
	{
		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					Hashtable<String,iBeaconStatus> hashTable=new Hashtable<>();
					MyBLEScanCallback callback=new MyBLEScanCallback(hashTable);
					bluetoothAdapter.startLeScan(callback);
					Thread.sleep(3000);
					bluetoothAdapter.stopLeScan(callback);
					scanResults.clear();
					scanResults.addAll(hashTable.values());
					Collections.sort(scanResults,new Comparator<iBeaconStatus>()
					{
						//倒序排列，把信号最强的放在前面
						@Override
						public int compare(iBeaconStatus o1,iBeaconStatus o2)
						{
							return o2.rssi-o1.rssi;
						}
					});
					handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							Log.d("","refreshed");
						}
					});
					Thread.sleep(0);
				}
			}
			catch(InterruptedException e)
			{
				return;
			}
		}
	};

	private Thread currentThread;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		handler=new Handler();
		currentThread=null;
		scanResults=new ArrayList<>();

		//取消标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		BluetoothManager bluetoothManager=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter=bluetoothManager.getAdapter();
		if(bluetoothAdapter==null)
		{
			Toast.makeText(this,"Bluetooth not supported!",Toast.LENGTH_LONG);
			Process.killProcess(Process.myPid());
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if(!bluetoothAdapter.isEnabled())
		{
			Toast.makeText(this,R.string.BluetoothOnNotification,Toast.LENGTH_LONG);
			bluetoothAdapter.enable();
		}
		currentThread=new Thread(runnable);
		currentThread.start();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if(currentThread!=null)
		{
			currentThread.interrupt();
			currentThread=null;
		}

		if(bluetoothAdapter.isEnabled())
		{
			Toast.makeText(this,R.string.BluetoothOffNotification,Toast.LENGTH_LONG);
			bluetoothAdapter.disable();
		}
	}
}
