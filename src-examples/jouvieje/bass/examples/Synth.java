//BASS Simple Synth, copyright (c) 2001-2006 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static jouvieje.bass.defines.BASS_CONFIG.BASS_CONFIG_BUFFER;
import static jouvieje.bass.defines.BASS_CONFIG.BASS_CONFIG_UPDATEPERIOD;
import static jouvieje.bass.defines.BASS_DEVICE.BASS_DEVICE_LATENCY;

import static jouvieje.bass.examples.util.Device.*;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import javax.swing.JPanel;

import jouvieje.bass.BassInit;
import jouvieje.bass.callbacks.STREAMPROC;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.ConsoleGUI;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.utils.Pointer;
import jouvieje.bass.structures.BASS_INFO;
import jouvieje.bass.structures.HFX;
import jouvieje.bass.structures.HSTREAM;

/**
 * I've ported the C BASS example to NativeBass.
 * 
 * @author Jérôme Jouvie (Jouvieje)
 * 
 * WANT TO CONTACT ME ?
 * @author Jérôme Jouvie (Jouvieje)
 * @site   http://jerome.jouvie.free.fr/
 * @mail   jerome.jouvie@gmail.com
 */
public class Synth extends ConsoleGUI {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		new BassExampleFrame(new Synth());
	}

	public Synth() {
		super();
		initialize();
	}

	private boolean init   = false;
	private boolean deinit = false;
	
	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS Simple Synth"; }

	private final int TABLESIZE = 2048;
	private final int[] sinTable = new int[TABLESIZE];	//sine table
	private final int KEYS = 20;
	private final char[] keys = {
		'Q', '2', 'W', '3', 'E', 'R', '5', 'T', '6', 'Y', '7', 'U',
		'I', '9', 'O', '0', 'P', 219, 187, 221};
	private final int MAXVOL	= 4000;						//higher value = longer fadeout
	private final int[] vol = new int[KEYS];				//keys' volume & pos
	private final int[] pos = new int[KEYS];

	/* stream writer */
	private final STREAMPROC writeStream = new STREAMPROC(){
		@Override
		public int STREAMPROC(HSTREAM handle, ByteBuffer buffer, int length, Pointer user) {
			ShortBuffer shorts = buffer.asShortBuffer();
			for(int i = 0; i < shorts.capacity(); i++)
				shorts.put((short)0);
			shorts.rewind();

			for(int n = 0; n < KEYS; n++) {
				if(vol[n] == 0) {
					continue;
				}

				float f = (float)pow(2.0, (n+3)/12.0) * TABLESIZE*440.0f/44100.0f;
				for(int c = 0; c < length/4;c++) {
					int s = sinTable[(int)((pos[n]++)*f)&(TABLESIZE-1)]*vol[n]/MAXVOL;
					s += shorts.get(c*2);
					if(s > 32767)
						s = 32767;
					else if(s < -32768)
						s = -32768;

					//left and right channels are the same
					shorts.put(c*2, (short)s);
					shorts.put(c*2+1, (short)s);

					vol[n]--;
					if(vol[n] < 0) {
						vol[n] = 0;
					}
				}
			}
			return length;
		}
	};
	
	@Override
	public void init() {
		/*
		 * NativeBass Init
		 */
		try {
			BassInit.loadLibraries();
		} catch(BassException e) {
			printfExit("NativeBass error! %s\n", e.getMessage());
			return;
		}

		/*
		 * Checking NativeBass version
		 */
		if(BassInit.NATIVEBASS_LIBRARY_VERSION() != BassInit.NATIVEBASS_JAR_VERSION()) {
			printfExit("Error!  NativeBass library version (%08x) is different to jar version (%08x)\n",
					BassInit.NATIVEBASS_LIBRARY_VERSION(), BassInit.NATIVEBASS_JAR_VERSION());
			return;
		}

		/*==================================================*/
	    
		init = true;
	}

	@Override
	public void run() {
		if(!init) {
			return;
		}

//		HFX[] fx = new HFX[9]; // effect handles
//		String[] fxname= new String[]{"CHORUS","COMPRESSOR","DISTORTION","ECHO",
//				"FLANGER","GARGLE","I3DL2REVERB","PARAMEQ","REVERB"};

		printf("BASS Simple Sinewave Synth\n--------------------------\n");

		// check the correct BASS was loaded
		if(((BASS_GetVersion() & 0xFFFF0000) >> 16) != BassInit.BASSVERSION()) {
			printfExit("An incorrect version of BASS.DLL was loaded");
			return;
		}

		/* 10ms update period */
		BASS_SetConfig(BASS_CONFIG_UPDATEPERIOD, 10);

		/* setup output - get latency */
		if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), BASS_DEVICE_LATENCY, null, null)) {
			printExit("Can't initialize device");
			return;
		}

		/* build sine table */
		for(int r = 0; r < TABLESIZE; r++)
			sinTable[r] = (int)(sin(2.0*PI*r/TABLESIZE)*7000.0);

		BASS_INFO info = BASS_INFO.allocate();
		BASS_GetInfo(info);
		printf("device latency: %dms\n", info.getLatency());
		printf("device minbuf: %dms\n", info.getMinBuffer());
		printf("ds version: %d (effects %s)\n", info.getDxVersion(), info.getDxVersion()<8 ? "disabled" : "enabled");

		/* default buffer size = update period + 'minbuf' */
		BASS_SetConfig(BASS_CONFIG_BUFFER, 10+info.getMinBuffer());
		int buflen = BASS_GetConfig(BASS_CONFIG_BUFFER);

		/* create a stream, stereo so that effects sound nice */
		HSTREAM str = BASS_StreamCreate(44100, 2, 0, writeStream, null);
		printf("press these keys to play:\n\n"+
				"  2 3  5 6 7  9 0  =\n"+
				" Q W ER T Y UI O P[ ]\n\n"+
				"press -/+ to de/increase the buffer\n"+
		"press spacebar to quit\n\n");

		if(info.getDxVersion() >= 8) {	// DX8 effects available
			printf("press F1-F9 to toggle effects\n\n");
		}
		
		info.release();

		printf("using a %dms buffer\r", buflen);

		if(!BASS_ChannelPlay(str.asInt(), false)) {
			printExit("Can't play channel");
			return;
		}

		char key;
		while((key = readKey("")) != 'e' && key != 'E') {
			if(key == '-' || key == '+') {
				/* recreate stream with smaller/larger buffer */
				BASS_StreamFree(str);
				if(key == '-') {
					BASS_SetConfig(BASS_CONFIG_BUFFER, buflen-1);		//smaller buffer
				}
				else {
					BASS_SetConfig(BASS_CONFIG_BUFFER, buflen+1);		//larger buffer
				}
				printfr("using a %dms buffer\t\t",buflen);
				str = BASS_StreamCreate(44100, 2, 0, writeStream, null);
				/* set effects on the new stream */
//				for(int r = 0; r < 9; r++) {
//					if(fx[r] != null) {
//						fx[r] = BASS_ChannelSetFX(str.asInt(), BASS_FX_DX8_CHORUS.asInt()+r, 0);
//					}
//				}
				BASS_ChannelPlay(str.asInt(), false);
			}
//			else if(key >= '1' && key <= '2') {
//				int r = Integer.parseInt(""+key);
//				if(fx[r] != null) {
//					BASS_ChannelRemoveFX(str.asInt(), fx[r]);
//					fx[r] = null;
//					printfr("effect %s = OFF\t\t",fxname[r]);
//				}
//				else {
//					/* set the effect, not bothering with parameters (use defaults) */
//					fx[r] = BASS_ChannelSetFX(str.asInt(), BASS_FX_DX8_CHORUS.asInt()+r, 0);
//					if(fx[r] != null) {
//						printfr("effect %s = ON\t\t", fxname[r]);
//					}
//				}
//			}

			for(int i = 0; i < keys.length; i++) {
				if(key == keys[i]) {
					if(vol[i] != MAXVOL) {
						pos[i] = 0;
						vol[i] = MAXVOL; //start key
					}
					else if(vol[i] != 0) {
						vol[i]--;			//trigger key fadeout
					}
					break;
				}
			}
		}
		stop();
	}
	
	@Override
	public boolean isRunning() { return deinit; }
	@Override
	public void stop() {
		if(!init || deinit) {
			return;
		}
		deinit = true;
		
		print("\n");
		
		BASS_Free();
		
		printExit("Shutdown\n");
	}
}