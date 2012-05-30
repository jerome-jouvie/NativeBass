// BASS custom looping example, copyright (c) 2004-2006 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_ACTIVE.BASS_ACTIVE_STOPPED;
import static jouvieje.bass.defines.BASS_MUSIC.BASS_MUSIC_DECODE;
import static jouvieje.bass.defines.BASS_MUSIC.BASS_MUSIC_POSRESET;
import static jouvieje.bass.defines.BASS_MUSIC.BASS_MUSIC_PRESCAN;
import static jouvieje.bass.defines.BASS_MUSIC.BASS_MUSIC_RAMPS;
import static jouvieje.bass.defines.BASS_POS.BASS_POS_BYTE;
import static jouvieje.bass.defines.BASS_STREAM.BASS_STREAM_DECODE;
import static jouvieje.bass.defines.BASS_SYNC.BASS_SYNC_END;
import static jouvieje.bass.defines.BASS_SYNC.BASS_SYNC_MIXTIME;
import static jouvieje.bass.defines.BASS_SYNC.BASS_SYNC_POS;





import static jouvieje.bass.examples.util.Device.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import jouvieje.bass.BassInit;
import jouvieje.bass.callbacks.SYNCPROC;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.FileFilters;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.utils.Pointer;
import jouvieje.bass.structures.HMUSIC;
import jouvieje.bass.structures.HSTREAM;
import jouvieje.bass.structures.HSYNC;

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
public class CustLoop extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(CustLoop.this,
				"<html><body>"+text+"<BR>(error code: "+BASS_ErrorGetCode()+")</body></html>");
	}

	private final void printfExit(String format, Object... args) {
		String s = String.format(format, args);
		JOptionPane.showMessageDialog(this, s);
		stop();
		try {
			System.exit(0);
		} catch(SecurityException e) {};
	}
	
	private boolean init   = false;
	private boolean deinit = false;

	private final int WIDTH  = 600;	//Display width
	private final int HEIGHT = 201;	//Height (odd number for centre line)

	private Thread scan = null;
	private boolean killScan = false;

	private int chan;
	private long bpp;					//Bytes per pixel
	private long[] loop = new long[2];	//Loop start & end
	private HSYNC lsync;				//Looping sync

	private BufferedImage wavebuf = null;
	
	private SYNCPROC loopSyncProc = new SYNCPROC(){
		@Override
		public void SYNCPROC(HSYNC handle, int channel, int data, Pointer user) {
			if(!BASS_ChannelSetPosition(channel, loop[0], BASS_POS_BYTE)) { //Try seeking to loop start
				BASS_ChannelSetPosition(channel, 0, BASS_POS_BYTE);		//Failed, go to start of file instead
			}
		}
	};
	
	public static void main(String[] args) {
		new BassExampleFrame(new CustLoop());
	}

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
		
		// check the correct BASS was loaded
		if(((BASS_GetVersion() & 0xFFFF0000) >> 16) != BassInit.BASSVERSION()) {
			printfExit("An incorrect version of BASS.DLL was loaded");
			return;
		}
		
		//Initialize BASS
		if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), 0, null, null)) {
			error("Can't initialize device");
			stop();
		}
		if(!playFile()) {
			//Start a file playing
			stop();
		}
		//Set update timer (10hz)
		timer.start();
	}
	
	private boolean playFile() {
		int result = getFileChooser().showOpenDialog(CustLoop.this);
		if(result != JFileChooser.APPROVE_OPTION) {
			return false;
		}

		HSTREAM stream = null;
		HMUSIC music = null;
		
		String file = getFileChooser().getSelectedFile().getPath();
		if (   (stream = BASS_StreamCreateFile(false, file, 0, 0, 0)) == null
			&& (music = BASS_MusicLoad(false, file, 0, 0, BASS_MUSIC_RAMPS | BASS_MUSIC_POSRESET | BASS_MUSIC_PRESCAN, 0)) == null) {
			error("Can't play file");
			return false; // Can't load the file
		}
		
		chan = (stream != null) ? stream.asInt() : ((music != null) ?  music.asInt() : 0);
		
		bpp = (int)(BASS_ChannelGetLength(chan, BASS_POS_BYTE) / WIDTH);			//Bytes per pixel
		if (bpp < BASS_ChannelSeconds2Bytes(chan, 0.02f)) {	//Minimum 20ms per pixel (BASS_ChannelGetLevel scans 20ms)
			bpp = (int)BASS_ChannelSeconds2Bytes(chan, 0.02f);
		}
		BASS_ChannelSetSync(chan, BASS_SYNC_END | BASS_SYNC_MIXTIME, 0, loopSyncProc, null); //Set sync to loop at end
		BASS_ChannelPlay(chan, false);						//Start playing
		
		/*
		 * Jouvieje note:
		 * Open a second time the music and scan it
		 */
		{
			final HSTREAM stream2;
			final HMUSIC music2;
			
			stream2 = BASS_StreamCreateFile(false, file, 0, 0, BASS_STREAM_DECODE);
			if(stream2 == null) {
				music2 = BASS_MusicLoad(false, file, 0, 0, BASS_MUSIC_DECODE, 0);
			}
			else {
				music2 = null;
			}
			
			final int chan2 = (stream2 != null) ? stream2.asInt() : ((music2 != null) ?  music2.asInt() : 0);
			
			scan = new Thread() {
				@Override
				public void run() {
					int cpos = 0;
					long[] peak = new long[2];
					wavebuf = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

					while(!killScan) {
						long level = BASS_ChannelGetLevel(chan2);	//Scan peaks
						
						int pos = (int)(BASS_ChannelGetPosition(chan2, BASS_POS_BYTE) / bpp);
						if(peak[0] < (level & 0x0000FFFF)) {
							peak[0] = (level & 0x0000FFFF);			//Set left peak
						}
						if(peak[1] < (level & 0xFFFF0000) >> 16) {
							peak[1] = (level & 0xFFFF0000) >> 16;	//Set right peak
						}
						if(BASS_ChannelIsActive(chan2) == BASS_ACTIVE_STOPPED) {
							pos = -1;		//Reached the end
						}
						/*else
							pos = (int)(BASS_ChannelGetPosition(chan2, BASS_POS_BYTE) / bpp);*/
						
						if(pos > cpos) {
							for(int a = 0; a < peak[0]*(HEIGHT/2)/32768; a++) {
								wavebuf.setRGB(cpos, HEIGHT/2-1-a, getIndexColor(1+a));		//Draw left peak
							}
							for(int a = 0; a < peak[1]*(HEIGHT/2)/32768; a++) {
								wavebuf.setRGB(cpos, HEIGHT/2+1+a, getIndexColor(1+a));		//Draw right peak
							}
							if(pos >= WIDTH) {
								break; //Gone off end of display
							}
							cpos = pos;
							peak[0] = peak[1] = 0;
						}
					}
					BASS_StreamFree(stream2);	//Free the decoder
					BASS_MusicFree(music2);	//Jouvieje note: Missing line from c example ?
				}
			};
			scan.start();
		}
		return true;
	}
	
	@Override
	public boolean isRunning() { return deinit; }
	@Override
	public void stop() {
		if(!init || deinit) {
			return;
		}
		deinit = true;
		
		timer.stop();
		if(scan != null && scan.isAlive()) {
			killScan = true;
			while(scan.isAlive()) {
				try {
					Thread.sleep(5);
				} catch(InterruptedException e){}
			}
		}
		BASS_Free();
	}

	private void setLoopStart(int pos) {
		loop[0] = pos * bpp;
	}

	private void setLoopEnd(int pos) {
		loop[1] = pos * bpp;
		BASS_ChannelRemoveSync(chan, lsync);		//Remove old sync
		lsync = BASS_ChannelSetSync(chan, BASS_SYNC_POS | BASS_SYNC_MIXTIME, loop[1], loopSyncProc, null); //Set new sync
	}
	
	private int getIndexColor(int index) {
		if(index == 0) {
			return 0;
		}
		int r = (255*index) / (HEIGHT/2);
		return (r << 16) + ((255-r) << 8);
	}
	
	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS custom looping example (left-click to set loop start, right-click to set end)"; }
	
			/* Graphical stuff */

	private JFileChooser fileChooser = null;
	private Timer timer = new Timer(100, new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			Graphics graphic = getGraphics(); if(graphic == null) return;
			graphic.drawImage(wavebuf, 0, 0, null); 		//Draw peak waveform
			drawTimeLine(graphic, loop[0], 0xffff00, 24);	//Loop start
			drawTimeLine(graphic, loop[1], 0x00ffff, 36);	//Loop end
			drawTimeLine(graphic, BASS_ChannelGetPosition(chan, BASS_POS_BYTE), 0xffffff, 12); //Current pos
		}
		
		private void drawTimeLine(Graphics graphic, long pos, int color, int y) {
			int x = (int)(pos / bpp);
			graphic.setColor(new Color(color));
			graphic.drawLine(x, 0, x, HEIGHT);
			
			int time = (int)BASS_ChannelBytes2Seconds(chan, pos);
			String text = String.format("%d:%02d", time/60, time%60);
			graphic.drawString(text, x, y);
		}
	});

	public CustLoop() {
		super();
		initialize();
	}

	private void initialize() {
		this.setDoubleBuffered(true);
		this.setSize(new Dimension(WIDTH, HEIGHT));
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		this.setMaximumSize(new Dimension(WIDTH, HEIGHT));
		this.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				if(e.getButton() == MouseEvent.BUTTON1) {		//Left
					setLoopStart(x);
				}
				else if(e.getButton() == MouseEvent.BUTTON3) {	//Right
					setLoopEnd(x);
				}
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter(){
			@Override
			public void mouseDragged(MouseEvent e) {
				int x = e.getX();
				if(e.getButton() == MouseEvent.BUTTON1) {		//Left
					setLoopStart(x);
				}
				else if(e.getButton() == MouseEvent.BUTTON3) {	//Right
					setLoopEnd(x);
				}
			}
		});
	}

	private JFileChooser getFileChooser() {
		if(fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.resetChoosableFileFilters();
			fileChooser.addChoosableFileFilter(FileFilters.allFiles);
			fileChooser.addChoosableFileFilter(FileFilters.playableFiles);
			fileChooser.setDialogTitle("Open a music");
		}
		return fileChooser;
	}
}
