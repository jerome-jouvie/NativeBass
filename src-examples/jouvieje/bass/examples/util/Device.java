/* $LICENSE$ */
package jouvieje.bass.examples.util;

public class Device {
	private final static boolean forceNoSoundDevice = false;

	public static int forceNoSoundDevice(int device) {
		if(forceNoSoundDevice) {
			return 0;
		}
		return device;
	}

//	private final static boolean forceFrequency = true;
	public static int forceFrequency(int freq) {
//		if(forceFrequency) {
//			return ;
//		}
		return freq;
	}
}
