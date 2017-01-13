package cn.edu.nju.miaoxw.handymuseum.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;

import cn.edu.nju.miaoxw.handymuseum.mobile.utility.DensityHelper;

public class MainActivity extends Activity
{
	private Handler handler;
	private BluetoothAdapter bluetoothAdapter;

	private iBeaconStatus nearestBeacon;
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

					//清理过期元素
					for(Iterator<iBeaconStatus> it=scanResults.iterator();it.hasNext();)
					{
						iBeaconStatus item=it.next();
						item.age++;
						if(item.age>=3)
							it.remove();
					}

					scanResults.addAll(hashTable.values());
					Collections.sort(scanResults,new Comparator<iBeaconStatus>()
					{
						//倒序排列，把信号最强的放在前面
						@Override
						public int compare(iBeaconStatus o1,iBeaconStatus o2)
						{
							if(o1.rssi!=o2.rssi)
								return o2.rssi-o1.rssi;
							else
								return o1.age-o2.age;
						}
					});

					if(!scanResults.isEmpty())
					{
						//所有不过期数据中最可信的那个
						nearestBeacon=scanResults.get(0);
					}

					handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							if(nearestBeacon==null)
							{
								imageButtonPositionMarker.setVisibility(View.INVISIBLE);
								imageButtonPositionMarker.setClickable(false);
								linearLayoutDescription.setVisibility(View.GONE);
								return;
							}
							if(nearestBeacon.major==0)
							{
								FrameLayout.LayoutParams layoutParams=(FrameLayout.LayoutParams)imageButtonPositionMarker.getLayoutParams();

								switch(nearestBeacon.minor)
								{
									case 1:
										layoutParams.setMargins(DensityHelper.dip2px(getApplicationContext(),240),DensityHelper.dip2px(getApplicationContext(),90),0,0);
										imageButtonPositionMarker.setLayoutParams(layoutParams);
										imageButtonPositionMarker.setImageDrawable(getResources().getDrawable(R.drawable.position_dot));
										imageButtonPositionMarker.setVisibility(View.VISIBLE);
										imageButtonPositionMarker.setClickable(false);
										textViewZoneName.setText(R.string.zone1_name);
										textViewZoneDescription.setText(R.string.zone1_description);
										linearLayoutDescription.setVisibility(View.VISIBLE);
										break;
									case 2:
										layoutParams.setMargins(DensityHelper.dip2px(getApplicationContext(),240),DensityHelper.dip2px(getApplicationContext(),190),0,0);
										imageButtonPositionMarker.setLayoutParams(layoutParams);
										imageButtonPositionMarker.setImageDrawable(getResources().getDrawable(R.drawable.bulb));
										imageButtonPositionMarker.setVisibility(View.VISIBLE);
										imageButtonPositionMarker.setClickable(true);
										textViewZoneName.setText(R.string.inter1_name);
										textViewZoneDescription.setText(R.string.inter1_description);
										linearLayoutDescription.setVisibility(View.VISIBLE);
										break;
									case 3:
										layoutParams.setMargins(DensityHelper.dip2px(getApplicationContext(),240),DensityHelper.dip2px(getApplicationContext(),320),0,0);
										imageButtonPositionMarker.setLayoutParams(layoutParams);
										imageButtonPositionMarker.setImageDrawable(getResources().getDrawable(R.drawable.position_dot));
										imageButtonPositionMarker.setVisibility(View.VISIBLE);
										imageButtonPositionMarker.setClickable(false);
										textViewZoneName.setText(R.string.zone2_name);
										textViewZoneDescription.setText(R.string.zone2_description);
										linearLayoutDescription.setVisibility(View.VISIBLE);
										break;
									default:
										imageButtonPositionMarker.setVisibility(View.INVISIBLE);
										imageButtonPositionMarker.setClickable(false);
										linearLayoutDescription.setVisibility(View.GONE);
										break;
								}
							}
							else
							{
								imageButtonPositionMarker.setVisibility(View.INVISIBLE);
								imageButtonPositionMarker.setClickable(false);
								textViewZoneName.setVisibility(View.GONE);
								textViewZoneDescription.setVisibility(View.GONE);
							}
						}
					});
					Thread.sleep(1000);
				}
			}
			catch(InterruptedException e)
			{
				return;
			}
		}
	};

	private Thread currentThread;

	private LinearLayout linearLayoutDescription;
	private ImageButton imageButtonPositionMarker;
	private TextView textViewZoneName;
	private TextView textViewZoneDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		handler=new Handler();
		currentThread=null;
		scanResults=new ArrayList<>();
		nearestBeacon=null;

		//取消标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		linearLayoutDescription=(LinearLayout)findViewById(R.id.linearLayoutDescription);
		imageButtonPositionMarker=(ImageButton)findViewById(R.id.imageButtonPositionMarker);
		textViewZoneName=(TextView)findViewById(R.id.textViewZoneName);
		textViewZoneDescription=(TextView)findViewById(R.id.textViewZoneDescription);

		imageButtonPositionMarker.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				iBeaconStatus currentBeacon=nearestBeacon;

				//Demo用，因为仅一个交互展台
				if(currentBeacon.major!=0||currentBeacon.minor!=2)
				{
					Bundle bundleToSend=new Bundle();
					bundleToSend.putSerializable("place",currentBeacon);

					Intent intent=new Intent(MainActivity.this,MissionActivity.class);
					intent.putExtras(bundleToSend);
					startActivityForResult(intent,0);
				}
			}
		});

		BluetoothManager bluetoothManager=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter=bluetoothManager.getAdapter();
		if(bluetoothAdapter==null)
		{
			Toast.makeText(this,"Bluetooth not supported!",Toast.LENGTH_LONG).show();
			Process.killProcess(Process.myPid());
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if(!bluetoothAdapter.isEnabled())
		{
			Toast.makeText(this,R.string.BluetoothOnNotification,Toast.LENGTH_LONG).show();
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
			Toast.makeText(this,R.string.BluetoothOffNotification,Toast.LENGTH_LONG).show();
			bluetoothAdapter.disable();
		}
	}

	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data)
	{
		if(resultCode==Activity.RESULT_OK)
		{
			if(requestCode==0)
			{
				boolean result=data.getBooleanExtra("result",false);
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				builder.setTitle("提示");
				builder.setPositiveButton("确定",new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog,int which)
					{
						dialog.dismiss();
					}
				});
				if(result)
					builder.setMessage("回答正确！");
				else
					builder.setMessage("回答错误。");
				builder.create().show();
			}
		}
		else
		{
			Toast.makeText(this,"回答取消。",Toast.LENGTH_LONG).show();
		}
	}
}
