// BASS DX8 Effects Test, copyright (c) 2001-2006 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_MUSIC.BASS_MUSIC_RAMP;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_FX;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_LOOP;

import static jouvieje.bass.examples.util.Device.*;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

import jouvieje.bass.BassInit;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.FileFilters;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.structures.BASS_INFO;
import jouvieje.bass.structures.HFX;
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
public class FxTest extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(FxTest.this,
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

	private HSTREAM stream;		//Channel handle
	private HMUSIC music;		//Channel handle
//	private HFX[] fx = new HFX[4];		//3 eq bands + reverb

	public static void main(String[] args) {
		new BassExampleFrame(new FxTest());
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
		
		// setup output - default device
		if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), 0, null, null)) {
			error("Can't initialize device");
			stop();
			return;
		}
		{
			// check that DX8 features are available
			BASS_INFO bi = BASS_INFO.allocate();
			BASS_GetInfo(bi);
			int dxVersion = bi.getDxVersion();
			bi.release();
			if(dxVersion < 8) {
				BASS_Free();
				error("DirectX 8 is not installed");
				stop();
			}
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

//	private void updateFX(int index, int value) {
//		if(index < 3) {
//			BASS_DX8_PARAMEQ p = BASS_DX8_PARAMEQ.allocate();
//			BASS_FXGetParameters(fx[index], p);
//			p.setGain(value);
//			BASS_FXSetParameters(fx[index], p);
//			p.release();
//		}
//		else {
//			BASS_DX8_REVERB p = BASS_DX8_REVERB.allocate();
//			BASS_FXGetParameters(fx[3], p);
//			value = 20-value;
//			p.setReverbMix(-0.012f*value*value*value);
//			BASS_FXSetParameters(fx[3], p);
//			p.release();
//		}
//	}
	
	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS DX8 effects test"; }

	/* Graphical stuff */

	private JButton open = null;
//	private JSlider freq1 = null;
//	private JSlider freq2 = null;
//	private JSlider freq3 = null;
//	private JSlider reverb = null;
	private JLabel freq1Label = null;
	private JLabel freq2Label = null;
	private JLabel freq3Label = null;
	private JLabel reverbLabel = null;
	private JFileChooser fileChooser = null;

	public FxTest() {
		super();
		initialize();
	}

	private void initialize() {
		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
		gridBagConstraints8.gridx = 3;
		gridBagConstraints8.insets = new Insets(0, 0, 10, 0);
		gridBagConstraints8.gridy = 2;
		reverbLabel = new JLabel();
		reverbLabel.setText("Reverb");
		GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
		gridBagConstraints7.gridx = 2;
		gridBagConstraints7.insets = new Insets(0, 0, 10, 0);
		gridBagConstraints7.gridy = 2;
		freq3Label = new JLabel();
		freq3Label.setText("8 khz");
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.gridx = 1;
		gridBagConstraints6.insets = new Insets(0, 0, 10, 0);
		gridBagConstraints6.gridy = 2;
		freq2Label = new JLabel();
		freq2Label.setText("1 khz");
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridx = 0;
		gridBagConstraints5.insets = new Insets(0, 0, 10, 0);
		gridBagConstraints5.gridy = 2;
		freq1Label = new JLabel();
		freq1Label.setText("125 hz");
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints4.gridy = 1;
		gridBagConstraints4.weightx = 2.0D;
		gridBagConstraints4.gridx = 3;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints3.gridy = 1;
		gridBagConstraints3.weightx = 1.0;
		gridBagConstraints3.gridx = 2;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints2.gridy = 1;
		gridBagConstraints2.weightx = 1.0;
		gridBagConstraints2.gridx = 1;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.weighty = 1.0D;
		gridBagConstraints1.gridx = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(10, 10, 5, 10);
		gridBagConstraints.gridy = 0;
		this.setSize(new Dimension(306, 160));
		this.setPreferredSize(new Dimension(306, 160));
		this.setLayout(new GridBagLayout());
		this.add(getOpen(), gridBagConstraints);
//		this.add(getFreq1(), gridBagConstraints1);
//		this.add(getFreq2(), gridBagConstraints2);
//		this.add(getFreq3(), gridBagConstraints3);
//		this.add(getReverb(), gridBagConstraints4);
		this.add(freq1Label, gridBagConstraints5);
		this.add(freq2Label, gridBagConstraints6);
		this.add(freq3Label, gridBagConstraints7);
		this.add(reverbLabel, gridBagConstraints8);
	}

	private JButton getOpen() {
		if(open == null) {
			open = new JButton();
			open.setText("Click here to open a file...");
			open.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int result = getFileChooser().showOpenDialog(FxTest.this);
					if(result == JFileChooser.APPROVE_OPTION) {
						File file = getFileChooser().getSelectedFile();

						// free both MOD and stream, it must be one of them! :)
						BASS_MusicFree(music);		music = null;
						BASS_StreamFree(stream);	stream = null;
						if(true) { //With FX flag
							//FIXME Don't work with BASS_SAMPLE_FX flag
							stream = BASS_StreamCreateFile(false, file.getPath(), 0, 0, BASS_SAMPLE_LOOP | BASS_SAMPLE_FX);
							if(stream == null) {
								music = BASS_MusicLoad(false, file.getPath(), 0, 0, BASS_SAMPLE_LOOP | BASS_MUSIC_RAMP | BASS_SAMPLE_FX, 0);
							}
						}
						else { //Without FX flag
							stream = BASS_StreamCreateFile(false, file.getPath(), 0, 0, BASS_SAMPLE_LOOP);
							if(stream == null) {
								music = BASS_MusicLoad(false, file.getPath(), 0, 0, BASS_SAMPLE_LOOP | BASS_MUSIC_RAMP, 0);
							}
						}

						int chan = (stream != null) ? stream.asInt() : ((music != null) ?  music.asInt() : 0);
						if(chan == 0) {
							// whatever it is, it ain't playable
							open.setText("click here to open a file...");
							error("Can't play the file");
							return;
						}
						open.setText(file.getName());
						{
							//Setup the effects
//							fx[0] = BASS_ChannelSetFX(chan, BASS_FX_DX8_PARAMEQ.asInt(), 0);
//							fx[1] = BASS_ChannelSetFX(chan, BASS_FX_DX8_PARAMEQ.asInt(), 0);
//							fx[2] = BASS_ChannelSetFX(chan, BASS_FX_DX8_PARAMEQ.asInt(), 0);
//							fx[3] = BASS_ChannelSetFX(chan, BASS_FX_DX8_REVERB.asInt(),  0);
//							BASS_DX8_PARAMEQ p = BASS_DX8_PARAMEQ.allocate();
//							p.setGain(0);
//							p.setBandwidth(18);
//							p.setCenter(125);
//							BASS_FXSetParameters(fx[0], p);
//							p.setCenter(1000);
//							BASS_FXSetParameters(fx[1], p);
//							p.setCenter(8000);
//							BASS_FXSetParameters(fx[2], p);
//							p.release();		//Free memory allocated with BASS_FXPARAMEQ.create()
//							
//							updateFX(0, getFreq1().getValue());
//							updateFX(1, getFreq2().getValue());
//							updateFX(2, getFreq3().getValue());
//							updateFX(3, getReverb().getValue());
						}
						BASS_ChannelPlay(chan, false);
					}
				}
			});
		}
		return open;
	}

//	private JSlider getFreq1() {
//		if(freq1 == null) {
//			freq1 = new JSlider(-10, 10);
//			freq1.setValue(0);
//			freq1.setOrientation(JSlider.VERTICAL);
//			freq1.addChangeListener(new javax.swing.event.ChangeListener(){
//				@Override
//				public void stateChanged(javax.swing.event.ChangeEvent e) {
//					updateFX(0, freq1.getValue());
//				}
//			});
//		}
//		return freq1;
//	}
//
//	private JSlider getFreq2() {
//		if(freq2 == null) {
//			freq2 = new JSlider(-10, 10);
//			freq2.setValue(0);
//			freq2.setOrientation(JSlider.VERTICAL);
//			freq2.addChangeListener(new javax.swing.event.ChangeListener(){
//				@Override
//				public void stateChanged(javax.swing.event.ChangeEvent e) {
//					updateFX(1, freq2.getValue());
//				}
//			});
//		}
//		return freq2;
//	}
//
//	private JSlider getFreq3() {
//		if(freq3 == null) {
//			freq3 = new JSlider(-10, 10);
//			freq3.setValue(0);
//			freq3.setOrientation(JSlider.VERTICAL);
//			freq3.addChangeListener(new javax.swing.event.ChangeListener(){
//				@Override
//				public void stateChanged(javax.swing.event.ChangeEvent e) {
//					updateFX(2, freq3.getValue());
//				}
//			});
//		}
//		return freq3;
//	}
//
//	private JSlider getReverb() {
//		if(reverb == null) {
//			reverb = new JSlider(0, 20);
//			reverb.setValue(0);
//			reverb.setOrientation(JSlider.VERTICAL);
//			reverb.addChangeListener(new javax.swing.event.ChangeListener(){
//				@Override
//				public void stateChanged(javax.swing.event.ChangeEvent e) {
//					updateFX(3, reverb.getValue());
//				}
//			});
//		}
//		return reverb;
//	}

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
}  //  @jve:decl-index=0:visual-constraint="8,10"
