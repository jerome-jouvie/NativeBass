//BASS simple DSP test, copyright (c) 2000-2006 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.sin;
import static jouvieje.bass.defines.BASS_CONFIG.BASS_CONFIG_FLOATDSP;
import static jouvieje.bass.defines.BASS_MUSIC.BASS_MUSIC_RAMPS;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_FLOAT;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_LOOP;





import static jouvieje.bass.examples.util.Device.*;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jouvieje.bass.BassInit;
import jouvieje.bass.callbacks.DSPPROC;
import jouvieje.bass.callbacks.STREAMPROC;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.FileFilters;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.utils.Pointer;
import jouvieje.bass.structures.BASS_CHANNELINFO;
import jouvieje.bass.structures.HDSP;
import jouvieje.bass.structures.HMUSIC;
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
public class DspTest extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(DspTest.this,
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
	
	private int floatable = 0;	//floating-point channel support?
	private int chan = 0;		//the channel...

	/* "rotate" */
	private HDSP rotdsp = null;		//DSP handle
	private float rotpos;			//cur.pos
	private DSPPROC Rotate = new DSPPROC(){
		@Override
		public void DSPPROC(HDSP handle, int channel, ByteBuffer buffer, int length, Pointer user) {
			FloatBuffer d = buffer.asFloatBuffer();

			for(int a = 0; a < length / 4; a += 2) {
				d.put(a,   d.get(a  ) * (float)abs(sin(rotpos)));
				d.put(a+1, d.get(a+1) * (float)abs(cos(rotpos)));
				rotpos = (float)fmod(rotpos+0.00003, PI);
			}
		}

		/**
		 * The fmod function calculates the floating-point remainder f of x / y such that x = i * y + f, where i is an integer,
		 * f has the same sign as x, and the absolute value of f is less than the absolute value of y.
		 */
		public double fmod(double x, double y) {
			return x - (int)floor(x / y) * y;
		}
	};

	/* "echo" */
	private HDSP echdsp = null;		//DSP handle
	private int ECHBUFLEN = 1200;		//buffer length
	private float[][] echbuf = null;	//buffer
	private int echpos;				//cur.pos
	private DSPPROC Echo = new DSPPROC(){
		@Override
		public void DSPPROC(HDSP handle, int channel, ByteBuffer buffer, int length, Pointer user) {
			FloatBuffer d = buffer.asFloatBuffer();

			for(int a = 0; a < length / 4; a += 2) {
				float da0 = d.get(a  );
				float da1 = d.get(a+1);

				float l = da0+(echbuf[echpos][1]/2);
				float r = da1+(echbuf[echpos][0]/2);
				if(true) {	//basic "bathroom" reverb
					echbuf[echpos][0] = l;
					echbuf[echpos][1] = r;
					d.put(a  , l);
					d.put(a+1, r);
				}
				else { //echo
					echbuf[echpos][0] = da0;
					echbuf[echpos][1] = da1;
					d.put(a  , l);
					d.put(a+1, r);
				}
				echpos++;
				if(echpos == ECHBUFLEN) {
					echpos = 0;
				}
			}
		}
	};

	/* "flanger" */
	private HDSP fladsp = null;		//DSP handle
	private int FLABUFLEN = 350;	//buffer length
	private float[][] flabuf = null;//buffer
	private int flapos;				//cur.pos
	private float flas,flasinc;		//sweep pos/increment
	private DSPPROC Flange = new DSPPROC(){
		@Override
		public void DSPPROC(HDSP handle, int channel, ByteBuffer buffer, int length, Pointer user) {
			FloatBuffer d = buffer.asFloatBuffer();

			for(int a = 0; a < length / 4; a += 2) {
				int p1 = (flapos+(int)flas)%FLABUFLEN;
				int p2 = (p1+1)%FLABUFLEN;
				float f = flas-(int)flas;
				float s;

				float da0 = d.get(a  );
				s = (da0+((flabuf[p1][0]*(1-f))+(flabuf[p2][0]*f)))*0.7f;
				flabuf[flapos][0] = da0;
				d.put(a, s);

				float da1 = d.get(a+1);
				s = (da1+((flabuf[p1][1]*(1-f))+(flabuf[p2][1]*f)))*0.7f;
				flabuf[flapos][1] = da1;
				d.put(a+1, s);

				flapos++;
				if(flapos == FLABUFLEN)
					flapos=0;
				flas += flasinc;
				if(flas < 0 || flas > FLABUFLEN - 1) {
					flasinc = -flasinc;
					flas += flasinc;
				}
			}
		}
	};

	public static void main(String[] args) {
		new BassExampleFrame(new DspTest());
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
		
		//enable floating-point DSP
		BASS_SetConfig(BASS_CONFIG_FLOATDSP, 1);	//true => 1
		//initialize - default device
		if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), 0, null, null)) {
			error("Can't initialize device");
			stop();
			return;
		}
		// check for floating-point capability
		HSTREAM stream = BASS_StreamCreate(44100, 2, BASS_SAMPLE_FLOAT, (STREAMPROC)null, null);
		if(stream != null) {
			// woohoo!
			BASS_StreamFree(stream);
			floatable = BASS_SAMPLE_FLOAT;
		}
	}

	@Override
	public boolean isRunning() { return deinit; }
	@Override
	public void stop() {
		if(!init || deinit) {
			return;
		}
		deinit = true;
		
		BASS_Free();
	}
	
	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS simple DSP test"; }

	/* Graphical stuff */

	private JButton open = null;
	private JFileChooser fileChooser = null;
	private JCheckBox rotate = null;
	private JCheckBox echo = null;
	private JCheckBox flanger = null;

	public DspTest() {
		super();
		initialize();
	}

	private void initialize() {
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 2;
		gridBagConstraints3.weightx = 1.0D;
		gridBagConstraints3.gridy = 1;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.weightx = 1.0D;
		gridBagConstraints2.gridy = 1;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.weightx = 1.0D;
		gridBagConstraints1.gridy = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.weightx = 3.0D;
		gridBagConstraints.gridy = 0;
		this.setSize(new Dimension(260, 70));
		this.setPreferredSize(new Dimension(260, 70));
		this.setLayout(new GridBagLayout());
		this.add(getOpen(), gridBagConstraints);
		this.add(getRotate(), gridBagConstraints1);
		this.add(getEcho(), gridBagConstraints2);
		this.add(getFlanger(), gridBagConstraints3);
	}

	private JButton getOpen() {
		if(open == null) {
			open = new JButton();
			open.setText("Click here to open a file ...");
			open.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int result = getFileChooser().showOpenDialog(DspTest.this);
					if(result == JFileChooser.APPROVE_OPTION) {
						String filePath = getFileChooser().getSelectedFile().getAbsolutePath();
						String fileName = getFileChooser().getSelectedFile().getName();

						HMUSIC music = null;
						HSTREAM stream = null;

						// free both MOD and stream, it must be one of them! :)
						BASS_MusicFree(music);
						BASS_StreamFree(stream);
						if ((stream = BASS_StreamCreateFile(false, filePath, 0, 0, BASS_SAMPLE_LOOP | floatable)) == null
						 && (music = BASS_MusicLoad(false, filePath, 0, 0, BASS_SAMPLE_LOOP | BASS_MUSIC_RAMPS | floatable, 0)) == null) {
							//whatever it is, it ain't playable
							open.setText("click here to open a file...");
							open.setToolTipText("");
							error("Can't play the file");
							return;
						}

						chan = (stream != null) ? stream.asInt() : ((music != null) ? music.asInt() : 0);

						BASS_CHANNELINFO info = BASS_CHANNELINFO.allocate();
						BASS_ChannelGetInfo(chan, info);
						long channels = info.getChannels();
						info.release();
						if(channels != 2) {
							//only stereo is allowed
							open.setText("click here to open a file...");
							open.setToolTipText("");
							BASS_MusicFree(music);
							BASS_StreamFree(stream);
							error("only stereo sources are supported");
							return;
						}

						open.setText(fileName);
						open.setToolTipText(filePath);
//						// setup DSPs on new channel and play it
//						SendMessage(win,WM_COMMAND,11,0);
//						SendMessage(win,WM_COMMAND,12,0);
//						SendMessage(win,WM_COMMAND,13,0);
						BASS_ChannelPlay(chan, false);
					}
				}
			});
		}
		return open;
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

	private JCheckBox getRotate() {
		if(rotate == null) {
			rotate = new JCheckBox();
			rotate.setText("Rotate");
			rotate.addItemListener(new java.awt.event.ItemListener(){
				@Override
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					// toggle "rotate"
					if(rotate.isSelected()) {
						rotpos = 0.7853981f;
						rotdsp = BASS_ChannelSetDSP(chan, Rotate, null, 2);
					}
					else {
						BASS_ChannelRemoveDSP(chan, rotdsp);
					}
				}
			});
		}
		return rotate;
	}

	private JCheckBox getEcho() {
		if(echo == null) {
			echo = new JCheckBox();
			echo.setText("Echo");
			echo.addItemListener(new java.awt.event.ItemListener(){
				@Override
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					// toggle "echo"
					if(echo.isSelected()) {
						echbuf = new float[ECHBUFLEN][2];
						echpos = 0;
						echdsp = BASS_ChannelSetDSP(chan, Echo, null, 1);
					}
					else {
						BASS_ChannelRemoveDSP(chan, echdsp);
					}
				}
			});
		}
		return echo;
	}

	private JCheckBox getFlanger() {
		if(flanger == null) {
			flanger = new JCheckBox();
			flanger.setText("Flanger");
			flanger.addItemListener(new java.awt.event.ItemListener(){
				@Override
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					// toggle "flanger"
					if(flanger.isSelected()) {
						flabuf = new float[FLABUFLEN][2];
						flapos = 0;
						flas = FLABUFLEN / 2;
						flasinc = 0.002f;
						fladsp = BASS_ChannelSetDSP(chan, Flange, null, 0);
					}
					else {
						BASS_ChannelRemoveDSP(chan, fladsp);
					}
				}
			});
		}
		return flanger;
	}
} //  @jve:decl-index=0:visual-constraint="10,10"