package cn.edu.nju.miaoxw.handymuseum.mobile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Hashtable;

import cn.edu.nju.miaoxw.handymuseum.mobile.utility.BLEHelper;


class MyBLEScanCallback implements BluetoothAdapter.LeScanCallback
{
	private static final String UUID_PATTERN="1D22DBEB-76D4-4D8A-9B18-818AAE991A99";

	private Hashtable<String,iBeaconStatus> dest;

	public MyBLEScanCallback(Hashtable<String,iBeaconStatus> dest)
	{
		this.dest=dest;
	}

	@Override
	public void onLeScan(BluetoothDevice device,int rssi,byte[] scanRecord)
	{
		int startByte=2;
		boolean patternFound=false;

		//寻找iBeacon相关信息
		while(startByte<=5)
		{
			if(((int)scanRecord[startByte+2]&0xFF)==0x02&&
					   ((int)scanRecord[startByte+3]&0xFF)==0x15)
			{
				patternFound=true;
				break;
			}
			startByte++;
		}

		if(patternFound)
		{
			byte[] uuidBytes=new byte[16];
			System.arraycopy(scanRecord,startByte+4,uuidBytes,0,16);
			String hexString=BLEHelper.bytesToHex(uuidBytes);

			StringBuilder stringBuilder=new StringBuilder();
			stringBuilder.append(hexString.substring(0,8));
			stringBuilder.append('-');
			stringBuilder.append(hexString.substring(8,12));
			stringBuilder.append('-');
			stringBuilder.append(hexString.substring(12,16));
			stringBuilder.append('-');
			stringBuilder.append(hexString.substring(16,20));
			stringBuilder.append('-');
			stringBuilder.append(hexString.substring(20,32));
			String UUID=stringBuilder.toString();

			int major=(scanRecord[startByte+20]&0xFF)*0x100+(scanRecord[startByte+21]&0xFF);
			int minor=(scanRecord[startByte+22]&0xFF)*0x100+(scanRecord[startByte+23]&0xFF);

			if(UUID.equals(UUID_PATTERN))
			{
				iBeaconStatus newStatus=new iBeaconStatus(major,minor,rssi);
				dest.put(device.getAddress(),newStatus);
			}
		}
	}


}
