package cn.edu.nju.miaoxw.handymuseum.mobile;

public class iBeaconStatus
{
	public int major;
	public int minor;
	public int rssi;

	public iBeaconStatus(int major,int minor,int rssi)
	{
		this.major=major;
		this.minor=minor;
		this.rssi=rssi;
	}

	@Override
	public String toString()
	{
		return "major:"+major+" minor:"+minor+" rssi:"+rssi;
	}
}
