package cn.edu.nju.miaoxw.handymuseum.mobile;

public class BLEHelper
{
	private static final char[] hexArray="0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes)
	{
		char[] hexChars=new char[bytes.length*2];

		for(int i=0;i<bytes.length;i++)
		{
			int v=bytes[i]&0xFF;
			hexChars[i*2]=hexArray[v>>>4];
			hexChars[i*2+1]=hexArray[v&0xF];
		}

		return new String(hexChars);
	}
}
