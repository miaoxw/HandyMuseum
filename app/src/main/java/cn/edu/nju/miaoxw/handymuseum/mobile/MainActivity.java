package cn.edu.nju.miaoxw.handymuseum.mobile;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Hashtable;

public class MainActivity extends Activity
{
	private Handler handler;
	private BluetoothAdapter bluetoothAdapter;
	private ListView listView;

	private ArrayList<String> scanResults;
	private ArrayAdapter<String> listDataAdapter;

	private Runnable runnable=new Runnable()
	{
		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					Hashtable<Long,String> hashtable=new Hashtable<>();
					MyBLEScanCallback callback=new MyBLEScanCallback(hashtable);
					bluetoothAdapter.startLeScan(callback);
					Thread.sleep(3000);
					bluetoothAdapter.stopLeScan(callback);
					scanResults.clear();
					scanResults.addAll(hashtable.values());
					handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							listDataAdapter.notifyDataSetChanged();
						}
					});
					Thread.sleep(2000);
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
		listDataAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,scanResults);

		setContentView(R.layout.activity_main);
		listView=(ListView)findViewById(R.id.listviewMain);
		listView.setAdapter(listDataAdapter);

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
			bluetoothAdapter.enable();
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
			bluetoothAdapter.disable();
	}
}
