// BASS Simple Console Test, copyright (c) 1999-2006 Ian Luck.

package jouvieje.bassmix.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_ACTIVE.BASS_ACTIVE_STALLED;
import static jouvieje.bass.defines.BASS_ACTIVE.BASS_ACTIVE_STOPPED;
import static jouvieje.bass.defines.BASS_FILEPOS.BASS_FILEPOS_BUFFER;
import static jouvieje.bass.defines.BASS_POS.BASS_POS_BYTE;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_LOOP;




import static jouvieje.bass.examples.util.Device.*;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jouvieje.bass.BassInit;
import jouvieje.bass.examples.OutputDevice;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.ConsoleGUI;
import jouvieje.bass.exceptions.BassException;
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
public class console_test extends ConsoleGUI {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		new BassExampleFrame(new console_test());
	}
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(console_test.this,
				"<html><body>"+text+"<BR>(error code: "+BASS_ErrorGetCode()+")</body></html>");
	}
	private final void checkError() {
		int error = BASS_ErrorGetCode();
		if(error != 0) {
			error("Unknown error");
		}
	}
	
	private boolean init   = false;
	private boolean deinit = false;

	public console_test() {
		super();
		initialize();
	}
	
	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS Simple Console Test"; }
	
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
		
		HSTREAM stream = null;
		HSTREAM[] streams = new HSTREAM[2];
		int chan;
		long act,time,level;
		long pos;
		
		printf( "Simple console mode BASS example : MOD/MPx/OGG/WAV player\n"+
				"---------------------------------------------------------\n"+
				"Usage:    ConTest <file>\n"+
				"Examples: ConTest music.mp3");
		
		resetInput();
		setInput("music.mp3");
		while(!keyHit()) {
			Thread.yield();
		}
		String path = getInput();

		// check the correct BASS was loaded
		if(((BASS_GetVersion() & 0xFFFF0000) >> 16) != BassInit.BASSVERSION()) {
			printfExit("An incorrect version of BASS.DLL was loaded");
		}
		
		// set the device to create 1st splitter stream on, and then create it
		/* If there's more than 1 device, let the user choose */
		OutputDevice outputDevice = new OutputDevice(0);
		outputDevice.setVisible(true);
		int device1 = outputDevice.getSelectedDevice();
		
		// setup output - default device
		if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), 0, null, null)) {
			printExit("Can't initialize device");
			return;
		}
		checkError();
		
		// try streaming the file/url
		stream = BASS_StreamCreateFile(false, path, 0, 0, BASS_SAMPLE_LOOP);
		chan = stream.asInt();
		System.out.println("HCHANNEL: 0x"+Integer.toHexString(chan));
		checkError();
		
		if(!BASS_SetDevice(device1)) {
			printExit("Can't set device");
			return;
		}
		checkError();
		
		streams[0] = BASS_Split_StreamCreate(chan, 0, null);
		checkError();
		if (streams[0] == null) {
			error("Can't create splitter");
			printExit("Can't create splitter");
			return;
		}
		
		BASS_ChannelPlay(chan, false);

		while(!keyHit() && (act = BASS_ChannelIsActive(chan)) != BASS_ACTIVE_STOPPED) {
			StringBuilder sb = new StringBuilder(80);
			
			// display some stuff and wait a bit
			level = BASS_ChannelGetLevel(chan);
			pos = BASS_ChannelGetPosition(chan, BASS_POS_BYTE);
			time = (int)BASS_ChannelBytes2Seconds(chan, pos);
			sb.append(String.format("pos %09d", pos));
			sb.append(String.format(" - %d:%02d - L ", time/60, time%60));
			if(act == BASS_ACTIVE_STALLED) {
				// playback has stalled
				sb.append(String.format("-- buffering : %05d --", BASS_StreamGetFilePosition(stream, BASS_FILEPOS_BUFFER)));
			}
			else {
				for(int a = 27204; a > 200; a = a*2/3) {
					sb.append((level & 0x0000FFFF) >= a ? '*' : '-');
				}
				sb.append(' ');
				for(int a = 210; a < 32768; a = a*3/2) {
					sb.append(((level & 0xFFFF0000) >> 16) >= a ? '*' : '-');
				}
			}
			sb.append(String.format(" R - cpu %.2f%%", BASS_GetCPU()));
			
			printr(sb.toString());
			sleep(50);
		}
		
		print("\n");

		BASS_ChannelStop(chan);
		
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
	
	private static void sleep(int t) {
		try {
			Thread.sleep(t);
		} catch(InterruptedException e) {}
	}
}