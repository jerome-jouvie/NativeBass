// BASS Full-duplex test, copyright (c) 2002-2007 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_DATA.BASS_DATA_AVAILABLE;
import static jouvieje.bass.defines.BASS_DEVICE.BASS_DEVICE_LATENCY;
import static jouvieje.bass.defines.BASS_INPUT.BASS_INPUT_OFF;
import static jouvieje.bass.defines.BASS_INPUT.BASS_INPUT_ON;

import static jouvieje.bass.examples.util.Device.*;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;

import jouvieje.bass.BassInit;
import jouvieje.bass.callbacks.RECORDPROC;
import jouvieje.bass.enumerations.STREAMPROC_SPECIAL;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.FileFilters;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.utils.BufferUtils;
import jouvieje.bass.utils.Pointer;
import jouvieje.bass.structures.BASS_INFO;
import jouvieje.bass.structures.HFX;
import jouvieje.bass.structures.HRECORD;
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
public class LiveFx extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(LiveFx.this,
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
	
	private HRECORD rchan = null;	//recording channel
	private HSTREAM chan = null;	//playback channel
//	private HFX[] fx = new HFX[4];	//FX handles
	private int input;				//current input source
	private int latency = 0;		//current latency

	public static void main(String[] args) {
		new BassExampleFrame(new LiveFx());
	}

	private RECORDPROC RecordinCallback = new RECORDPROC(){
		@Override
		public boolean RECORDPROC(HRECORD handle, ByteBuffer buffer, int length, Pointer user) {
			BASS_StreamPutData(chan, buffer, length); // feed recorded data to output stream
			return true; // continue recording
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

		// check the correct BASS was loaded
		if(((BASS_GetVersion() & 0xFFFF0000) >> 16) != BassInit.BASSVERSION()) {
			printfExit("An incorrect version of BASS.DLL was loaded");
			return;
		}
		
		//=======================================================
		JOptionPane.showMessageDialog(null,
				"<html><body>Do not set the input to 'WAVE' / 'What you hear' (etc...) with<br>"+
				"the level set high, as that is likely to result in nasty feedback.</body></html>",
				"Feedback warning", JOptionPane.WARNING_MESSAGE);
		//=======================================================
		
		// setup output - get device latency
		if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), BASS_DEVICE_LATENCY, null, null)) {
			error("Can't initialize output");
			System.exit(0);
			return;
		}

		//If no DX8, disable effect buttons
		BASS_INFO info = BASS_INFO.allocate();
		BASS_GetInfo(info);
//		int dxVersion = info.getDxVersion();
//		if(dxVersion < 8) {
//			getChorusCB().setEnabled(false);
//			getReverbCB().setEnabled(false);
//			getFlangerCB().setEnabled(false);
//			getGargleCB().setEnabled(false);
//		}

		// create a stream to play the recording
		chan = BASS_StreamCreate(44100, 2, 0, STREAMPROC_SPECIAL.STREAMPROC_PUSH, null);

		// start recording - default device, 44100hz, stereo, 16 bits, no callback function
		if(!BASS_RecordInit(-1) || (rchan = BASS_RecordStart(44100, 2, 10, RecordinCallback, null)) == null) {
			BASS_RecordFree();
			BASS_Free();
			error("Can't initialize recording device");
			System.exit(0);
		}
		// wait for recorded data to start arriving (calculate the latency)
		int chunk;
		while((chunk = BASS_ChannelGetData(rchan.asInt(), null, BASS_DATA_AVAILABLE)) == 0) {
		};

		{
			// get list of inputs
			FloatBuffer level = BufferUtils.newFloatBuffer(1);
			String i = null;
			for(int c = 0; (i = BASS_RecordGetInputName(c)) != null; c++) {
				getModel().addElement(i);
				if((BASS_RecordGetInput(c, level) & BASS_INPUT_OFF) != 0) { //this 1 is currently "on"
					input = c;
					getInputs().setSelectedIndex(input);
					getVolume().setValue((int)(level.get(0) * 100));	//Set level slider
				}
			}
			getInputs().setSelectedIndex(input);
		}
		
		{ // prebuffer at least "minbuf" amount of data before starting playback
			long prebuf = BASS_ChannelSeconds2Bytes(chan.asInt(), info.getMinBuffer() / 1000.f);
			while(BASS_ChannelGetData(chan.asInt(), null, BASS_DATA_AVAILABLE) < prebuf) {
				try {
					Thread.sleep(1);
				} catch(InterruptedException e) {}
			}
		}
		BASS_ChannelPlay(chan.asInt(), false);
		
		info.release();

		timer.start();
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
		// release it all
		BASS_RecordFree();
		BASS_Free();
	}
	
	private Timer timer = new Timer(250, new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			// display current latency (averaged)
			latency = (
				latency * 3 +
				BASS_ChannelGetData(chan.asInt(), null, BASS_DATA_AVAILABLE) +
				BASS_ChannelGetData(rchan.asInt(), null, BASS_DATA_AVAILABLE)
			) / 4;
			getLatencyValueLabel().setText(String.valueOf((int)BASS_ChannelBytes2Seconds(chan.asInt(), latency)*1000));
		}
	});
	
	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS full-duplex recording test with effects"; }
	
				/* Graphical stuff */
	
	private JComboBox inputs = null;
	private DefaultComboBoxModel model = null;
	private JSlider volume = null;
	private JFileChooser fileChooser = null;
	private JLabel latencyL = null;
	private JLabel latencyValueLabel = null;
//	private JCheckBox chorusCB = null;
//	private JCheckBox gargleCB = null;
//	private JCheckBox reverbCB = null;
//	private JCheckBox flangerCB = null;

	public LiveFx() {
		super();
		initialize();
	}

	private void initialize() {
		GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
		gridBagConstraints61.gridx = 3;
		gridBagConstraints61.insets = new Insets(0, 5, 0, 5);
		gridBagConstraints61.gridy = 1;
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridx = 3;
		gridBagConstraints5.insets = new Insets(0, 5, 0, 5);
		gridBagConstraints5.gridy = 0;
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 2;
		gridBagConstraints4.gridy = 1;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 2;
		gridBagConstraints3.gridy = 0;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.insets = new Insets(0, 0, 5, 10);
		gridBagConstraints2.gridy = 1;
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.gridx = 1;
		gridBagConstraints11.insets = new Insets(0, 0, 0, 10);
		gridBagConstraints11.gridy = 0;
		latencyL = new JLabel();
		latencyL.setText("Latency");
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints6.gridy = 1;
		gridBagConstraints6.weightx = 1.0;
		gridBagConstraints6.insets = new Insets(0, 5, 5, 15);
		gridBagConstraints6.gridx = 0;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.insets = new Insets(5, 5, 5, 15);
		gridBagConstraints1.gridy = 0;
		this.setSize(new Dimension(332, 75));
		this.setPreferredSize(new Dimension(332, 75));
		this.setLayout(new GridBagLayout());
		this.add(getInputs(), gridBagConstraints1);
		this.add(getVolume(), gridBagConstraints6);
		this.add(latencyL, gridBagConstraints11);
		this.add(getLatencyValueLabel(), gridBagConstraints2);
//		this.add(getChorusCB(), gridBagConstraints3);
//		this.add(getGargleCB(), gridBagConstraints4);
//		this.add(getReverbCB(), gridBagConstraints5);
//		this.add(getFlangerCB(), gridBagConstraints61);
	}

	private JLabel getLatencyValueLabel() {
		if(latencyValueLabel == null) {
			latencyValueLabel = new JLabel();
			latencyValueLabel.setText("");
			latencyValueLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		}
		return latencyValueLabel;
	}

	private JComboBox getInputs() {
		if(inputs == null) {
			inputs = new JComboBox();
			inputs.setModel(getModel());
			inputs.addItemListener(new java.awt.event.ItemListener(){
				@Override
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					FloatBuffer level = BufferUtils.newFloatBuffer(1);
					input = inputs.getSelectedIndex();									//get the selection
					for(int i = 0; BASS_RecordSetInput(i,BASS_INPUT_OFF, -1); i++) {};	//1st disable all inputs, then...
					BASS_RecordSetInput(input,BASS_INPUT_ON, -1);						//enable the selected input
					BASS_RecordGetInput(input, level);
					getVolume().setValue((int)(level.get(0) * 100));					//get the level
				}
			});
		}
		return inputs;
	}

	private DefaultComboBoxModel getModel() {
		if(model == null) {
			model = new DefaultComboBoxModel();
		}
		return model;
	}

	private JSlider getVolume() {
		if(volume == null) {
			volume = new JSlider(0, 100);
			volume.addChangeListener(new javax.swing.event.ChangeListener(){
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					float level = volume.getValue() / 100.0f;
					if(!BASS_RecordSetInput(input, 0, level)) { //failed to set input level
						BASS_RecordSetInput(-1, 0, level); //try master level instead
					}
				}
			});
		}
		return volume;
	}

	public JFileChooser getFileChooser() {
		if(fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.resetChoosableFileFilters();
			fileChooser.addChoosableFileFilter(FileFilters.allFiles);
			fileChooser.addChoosableFileFilter(FileFilters.wavFile);
			fileChooser.setDialogTitle("Open a music");
		}
		return fileChooser;
	}

//	private JCheckBox getChorusCB() {
//		if(chorusCB == null) {
//			chorusCB = new JCheckBox();
//			chorusCB.setText("Chorus");
//			chorusCB.addItemListener(new java.awt.event.ItemListener(){
//				@Override
//				public void itemStateChanged(java.awt.event.ItemEvent e) {
//					// toggle chorus
//					if(fx[0] != null) {
//						BASS_ChannelRemoveFX(chan.asInt(), fx[0]);
//						fx[0] = null;
//					}
//					else {
//						fx[0] = BASS_ChannelSetFX(chan.asInt(), BASS_FX_DX8_CHORUS.asInt(), 0);
//					}
//				}
//			});
//		}
//		return chorusCB;
//	}
//
//	private JCheckBox getGargleCB() {
//		if(gargleCB == null) {
//			gargleCB = new JCheckBox();
//			gargleCB.setText("Gargle");
//			gargleCB.addItemListener(new java.awt.event.ItemListener(){
//				@Override
//				public void itemStateChanged(java.awt.event.ItemEvent e) {
//					// toggle gargle
//					if(fx[1] != null) {
//						BASS_ChannelRemoveFX(chan.asInt(), fx[1]);
//						fx[1] = null;
//					}
//					else {
//						fx[1] = BASS_ChannelSetFX(chan.asInt(), BASS_FX_DX8_GARGLE.asInt(), 0);
//					}
//				}
//			});
//		}
//		return gargleCB;
//	}
//
//	private JCheckBox getReverbCB() {
//		if(reverbCB == null) {
//			reverbCB = new JCheckBox();
//			reverbCB.setText("Reverb");
//			reverbCB.addItemListener(new java.awt.event.ItemListener(){
//				@Override
//				public void itemStateChanged(java.awt.event.ItemEvent e) {
//					// toggle reverb
//					if(fx[2] != null) {
//						BASS_ChannelRemoveFX(chan.asInt(), fx[2]);
//						fx[2] = null;
//					}
//					else {
//						fx[2] = BASS_ChannelSetFX(chan.asInt(), BASS_FX_DX8_REVERB.asInt(), 0);
//					}
//				}
//			});
//		}
//		return reverbCB;
//	}
//
//	private JCheckBox getFlangerCB() {
//		if(flangerCB == null) {
//			flangerCB = new JCheckBox();
//			flangerCB.setText("Flanger");
//			flangerCB.addItemListener(new java.awt.event.ItemListener(){
//				@Override
//				public void itemStateChanged(java.awt.event.ItemEvent e) {
//					// toggle gargle
//					if(fx[3] != null) {
//						BASS_ChannelRemoveFX(chan.asInt(), fx[3]);
//						fx[3] = null;
//					}
//					else {
//						fx[3] = BASS_ChannelSetFX(chan.asInt(), BASS_FX_DX8_FLANGER.asInt(), 0);
//					}
//				}
//			});
//		}
//		return flangerCB;
//	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
