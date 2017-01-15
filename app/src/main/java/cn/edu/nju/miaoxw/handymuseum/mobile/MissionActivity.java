package cn.edu.nju.miaoxw.handymuseum.mobile;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MissionActivity extends Activity
{
	private TextView textViewProblemTitle;
	private TextView textViewProblemDescription;
	private Button buttonConnect;
	private Handler handler;

	private ProgressDialog progressDialog;

	private iBeaconStatus currentBeacon;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mission);
		ActionBar actionBar=getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		handler=new Handler();

		//交互资源的创建
		progressDialog=new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("请在交互展板上寻找答案");
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);

		textViewProblemTitle=(TextView)findViewById(R.id.textViewProblemTitle);
		textViewProblemDescription=(TextView)findViewById(R.id.textViewProblemDescription);
		buttonConnect=(Button)findViewById(R.id.buttonConnect);

		currentBeacon=(iBeaconStatus)getIntent().getExtras().getSerializable("place");
		if(currentBeacon==null)
		{
			setResult(Activity.RESULT_CANCELED);
			finish();
		}

		buttonConnect.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new Thread(new Runnable()
				{
					Socket socket;

					@Override
					public void run()
					{
						try
						{
							socket=new Socket("192.168.0.198",22222);
						}
						catch(IOException e)
						{
							e.printStackTrace();
							handler.post(new Runnable()
							{
								@Override
								public void run()
								{
									Toast.makeText(MissionActivity.this,"与交互展板连接失败！",Toast.LENGTH_LONG).show();
								}
							});
							return;
						}

						handler.post(new Runnable()
						{
							@Override
							public void run()
							{
								progressDialog.show();
							}
						});

						ObjectInputStream objectInputStream=null;
						boolean result=false;

						try
						{
							objectInputStream=new ObjectInputStream(socket.getInputStream());
							result=(Boolean)objectInputStream.readObject();
						}
						catch(IOException|ClassNotFoundException e)
						{
							handler.post(new Runnable()
							{
								@Override
								public void run()
								{
									progressDialog.dismiss();
									Toast.makeText(MissionActivity.this,"与交互展板的连接异常断开",Toast.LENGTH_LONG).show();
								}
							});
							return;
						}
						finally
						{
							if(objectInputStream!=null)
								try
								{
									objectInputStream.close();
								}
								catch(IOException e)
								{
								}
							try
							{
								socket.close();
							}
							catch(IOException e)
							{
							}
						}

						handler.post(new Runnable()
						{
							@Override
							public void run()
							{
								progressDialog.dismiss();
							}
						});

						Intent intent=new Intent();
						intent.putExtra("result",result);
						setResult(Activity.RESULT_OK,intent);
						finish();
					}
				}).start();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
				setResult(Activity.RESULT_CANCELED);
				finish();
				break;
		}
		return true;
	}
}
