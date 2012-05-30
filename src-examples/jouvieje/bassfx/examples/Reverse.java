/*******************************************************************************
 reverse.c/h/rc - Copyright (c) 2002-2008 (: JOBnik! :) [Arthur Aminov, ISRAEL]
                                                        [http://www.jobnik.org]
                                                        [   bass_fx@jobnik.org]
 
 BASS_FX playing in reverse with tempo & dx8 fx
 * Imports: bass.lib, bass_fx.lib, kernel32.lib, user32.lib, comdlg32.lib
*******************************************************************************/

package jouvieje.bassfx.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_FX_RVS.BASS_FX_RVS_FORWARD;
import static jouvieje.bass.defines.BASS_FX_RVS.BASS_FX_RVS_REVERSE;
import static jouvieje.bass.defines.BASS_ATTRIB.BASS_ATTRIB_VOL;
import static jouvieje.bass.defines.BASS_STREAM.BASS_STREAM_DECODE;
import static jouvieje.bass.defines.BASS_STREAM.BASS_STREAM_PRESCAN;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_FX;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_LOOP;
import static jouvieje.bass.defines.BASS_FX.BASS_FX_FREESOURCE;
import static jouvieje.bass.defines.BASS_POS.BASS_POS_BYTE;


import static jouvieje.bass.defines.BASS_ATTRIB_REVERSE.BASS_ATTRIB_REVERSE_DIR;


import static jouvieje.bass.enumerations.BASS_ATTRIB.BASS_ATTRIB_TEMPO;

import static jouvieje.bass.examples.util.Device.*;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.nio.FloatBuffer;

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
import jouvieje.bass.structures.HSTREAM;
import jouvieje.bass.utils.BufferUtils;

import javax.swing.JScrollBar;

/**
 * @author Tom Zhou
 */
public class Reverse extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	private HSTREAM chan = null;
//	private HFX fx = null;
	
	private void initialize() {
        GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
        gridBagConstraints61.gridx = 1;
        gridBagConstraints61.gridy = 4;
        GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
        gridBagConstraints51.gridy = 3;
        gridBagConstraints51.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints51.gridx = 1;
        GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
        gridBagConstraints41.gridx = 1;
        gridBagConstraints41.gridy = 2;
        posLabel = new JLabel();
        posLabel.setText("Playing Position: 00:00 / 00:00");
        GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
        gridBagConstraints31.gridx = 1;
        gridBagConstraints31.gridy = 1;
        GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
        gridBagConstraints21.gridx = -1;
        gridBagConstraints21.gridy = -1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridy = 0;
        this.setLayout(new GridBagLayout());
        this.setSize(new Dimension(295, 348));
        this.add(getOpenButton(), gridBagConstraints);
        this.add(getMidSplitPanel(), gridBagConstraints31);
        this.add(posLabel, gridBagConstraints41);
        this.add(getPosSlider(), gridBagConstraints51);
        this.add(getReverseButton(), gridBagConstraints61);
			
	}

	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(Reverse.this,
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
	
	private JButton getOpenButton() {
		if (openButton == null) {
			openButton = new JButton();
			openButton.setText("click here to open a file & play it...");
			
			openButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					getFileChooser().resetChoosableFileFilters();
					getFileChooser().addChoosableFileFilter(FileFilters.allFiles);
					getFileChooser().addChoosableFileFilter(FileFilters.streamableFiles);
					
					if(getFileChooser().showOpenDialog(Reverse.this) == JFileChooser.APPROVE_OPTION) {
						File file = getFileChooser().getSelectedFile();
						
						//free the previous stream
						if (chan != null)
							BASS_StreamFree(chan);
						
						chan = BASS_StreamCreateFile(false,file.getPath(),0,0,BASS_STREAM_DECODE|BASS_STREAM_PRESCAN|BASS_SAMPLE_FX);
						
						if(chan == null)
						{
							// not a WAV/MP3
							openButton.setText("click here to open a file & play it...");
							error("Selected file couldn't be loaded!");
							return;
						}
						
						// create new stream - decoded & reversed
						// 2 seconds decoding block as a decoding channel
						chan=BASS_FX_ReverseCreate(chan.asInt(), 2, BASS_STREAM_DECODE|BASS_FX_FREESOURCE);
						
						if(chan == null)
						{
							openButton.setText("click here to open a file & play it...");
							error("Couldn't create a reversed stream!");
							return;
						}
						
						// create a new stream - decoded & resampled
						chan=BASS_FX_TempoCreate(chan.asInt(), BASS_SAMPLE_LOOP|BASS_FX_FREESOURCE);
						
						if(chan==null)
						{
							openButton.setText("click here to open a file & play it...");
							error("Couldn't create a resampled stream!");
						}
						
						// update the Button to show the loaded file name
						openButton.setText(file.getName());
						
						// update tempo view
						tempoSlider.setValue(0);
						tempoLabel.setText("Tempo = 0%");
						
						// set dx8 Reverb
//						fx=BASS_ChannelSetFX(chan.asInt(), BASS_FX_DX8_REVERB.asInt(), 0);
//						updateFX(reverbScrollBar.getValue());			
						
						// set Volume
						BASS_ChannelSetAttribute(chan.asInt(), BASS_ATTRIB_VOL, (float)volSlider.getValue()/100.0f);
							
						// setmax to position slider
						double p = BASS_ChannelBytes2Seconds(chan.asInt(), BASS_ChannelGetLength(chan.asInt(), BASS_POS_BYTE));
						posSlider.setValue(0);
						posSlider.setMaximum((int) p);
						posSlider.setValue((int)p);
						
						// update the approximate time in seconds view
						updatePositionLabel();
						
						// play the new stream
						BASS_ChannelPlay(chan.asInt(), false);
						
						
					}
				}
			});
		}
		return openButton;
	}
	
	
	public void updatePositionLabel(){
		
		if(BASS_FX_TempoGetRateRatio(chan)==0) return;
		{
			float totalsec = (float)posSlider.getMaximum()/BASS_FX_TempoGetRateRatio(chan);
			float posec = (float)posSlider.getValue()/BASS_FX_TempoGetRateRatio(chan);
			
			String positionText = "Playing position: " + (int)posec/60 + ":" + (int)posec%60 + " / " + (int)totalsec/60 + ":" + (int)totalsec%60;
			posLabel.setText(positionText);

		}
	}
	
	// update dx8 reverb
//	private void updateFX(int a) {
//		BASS_DX8_REVERB p;
//		
//		p = BASS_DX8_REVERB.allocate();
//
//		BASS_FXGetParameters(fx, p);
//			p.setReverbMix(-0.012f * (float)(a * a * a));
//		BASS_FXSetParameters(fx, p);
//		
//		p.release();
//	}
	

	private JPanel getMidLPanel() {
		if (midLPanel == null) {
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.BOTH;
			gridBagConstraints4.gridy = 3;
			gridBagConstraints4.weighty = 1.0;
			gridBagConstraints4.gridwidth = 1;
			gridBagConstraints4.gridx = 0;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 2;
//			dx8Label = new JLabel();
//			dx8Label.setText("DX8 Reverb");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 0;
			volLabel = new JLabel();
			volLabel.setText("Volume");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.gridx = 0;
			midLPanel = new JPanel();
			midLPanel.setLayout(new GridBagLayout());
			midLPanel.add(getVolSlider(), gridBagConstraints1);
			midLPanel.add(volLabel, gridBagConstraints2);
//			midLPanel.add(dx8Label, gridBagConstraints3);
//			midLPanel.add(getReverbScrollBar(), gridBagConstraints4);
		}
		return midLPanel;
	}

	private JSlider getVolSlider() {
		if (volSlider == null) {
			volSlider = new JSlider();
			volSlider.setMinimum(0);
			volSlider.setMaximum(100);
			volSlider.setValue(50);
			volSlider.addChangeListener(new javax.swing.event.ChangeListener() {
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					BASS_ChannelSetAttribute(chan.asInt(), BASS_ATTRIB_VOL, (float)volSlider.getValue()/100.0f);
				}
			});
		}
		return volSlider;
	}

//	private JScrollBar getReverbScrollBar() {
//		if (reverbScrollBar == null) {
//			reverbScrollBar = new JScrollBar();
//			reverbScrollBar.setMinimum(0);
//			reverbScrollBar.setMaximum(20);
//			reverbScrollBar.setValue(20);
//			reverbScrollBar.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
//				@Override
//				public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
//					updateFX(reverbScrollBar.getValue());
//				}
//			});
//		}
//		return reverbScrollBar;
//	}

	private JPanel getMidRPanel() {
		if (midRPanel == null) {
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints6.gridy = 1;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.gridx = 1;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridy = 0;
			tempoLabel = new JLabel();
			tempoLabel.setText("Tempo = 0%");
			midRPanel = new JPanel();
			midRPanel.setLayout(new GridBagLayout());
			midRPanel.add(tempoLabel, gridBagConstraints5);
			midRPanel.add(getTempoSlider(), gridBagConstraints6);
		}
		return midRPanel;
	}

	private JSlider getTempoSlider() {
		if (tempoSlider == null) {
			tempoSlider = new JSlider();
			tempoSlider.setOrientation(JSlider.VERTICAL);
			tempoSlider.setInverted(true);
			tempoSlider.setMinimum(-30);
			tempoSlider.setMaximum(30);
			tempoSlider.setValue(0);
			tempoSlider.addChangeListener(new javax.swing.event.ChangeListener() {
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					
					if (chan != null && BASS_ChannelIsActive(chan.asInt()) != 0){
						BASS_ChannelSetAttribute(chan.asInt(), BASS_ATTRIB_TEMPO.asInt(), (float)tempoSlider.getValue() * -1.0f);
						tempoLabel.setText(String.format("Tempo = %2d%%",tempoSlider.getValue()*-1));
						// update the approximate time in seconds view
						updatePositionLabel();
					}
				}
			});
		}
		return tempoSlider;
	}

	private JPanel getMidSplitPanel() {
		if (midSplitPanel == null) {
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.gridwidth = 2;
			gridBagConstraints8.gridy = 0;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints7.gridy = 0;
			midSplitPanel = new JPanel();
			midSplitPanel.setLayout(new GridBagLayout());
			midSplitPanel.add(getMidLPanel(), gridBagConstraints7);
			midSplitPanel.add(getMidRPanel(), gridBagConstraints8);
		}
		return midSplitPanel;
	}

	private JSlider getPosSlider() {
		if (posSlider == null) {
			posSlider = new JSlider();
			posSlider.setValue(100);
			posSlider.addChangeListener(new javax.swing.event.ChangeListener() {
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					if(chan==null){return;}
					
					// change the position
					BASS_ChannelSetPosition(chan.asInt(), BASS_ChannelSeconds2Bytes(chan.asInt(), (double)posSlider.getValue()), BASS_POS_BYTE);
					
					updatePositionLabel();
				}
			});
		}
		return posSlider;
	}

	private JButton getReverseButton() {
		if (reverseButton == null) {
			reverseButton = new JButton();
			reverseButton.setText("Playing Direction - Reverse");
			reverseButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					if (chan == null)
						return;
					
					int srcChan = BASS_FX_TempoGetSource(chan);
				
					FloatBuffer dir = BufferUtils.newFloatBuffer(5);
					BASS_ChannelGetAttribute(srcChan, BASS_ATTRIB_REVERSE_DIR, dir);
					
					float dirf = dir.get();
					if (dirf<0) {
						BASS_ChannelSetAttribute(srcChan, BASS_ATTRIB_REVERSE_DIR, BASS_FX_RVS_FORWARD);
						reverseButton.setText("Playing Direction - Forward");
					} else {
						BASS_ChannelSetAttribute(srcChan, BASS_ATTRIB_REVERSE_DIR, BASS_FX_RVS_REVERSE);
						reverseButton.setText("Playing Direction - Reverse");
					}
				}
			});
		}
		return reverseButton;
	}

	public static void main(String[] args) {
		new BassExampleFrame(new Reverse());
	}
	
	private boolean init = false;
	private boolean deinit = false;
	
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
		
		/* Initialize default output device */
		if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), 0, null, null)) {
			error("Can't initialize device");
		}
		
		/*check if DX8 features are available */
		BASS_INFO bi;
		
		bi = BASS_INFO.allocate();
		
		BASS_GetInfo(bi);
		if(bi.getDxVersion()<8){
			error("DirectX version 8 is not Installed!!!<BR>You won't be able to use any DX8 Effects!");
		}
		
		bi.release();
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
	public String getTitle() { return "BASS Fx - Reverse"; }

	/* Graphical stuff */
	
	class ListElement {
		final String display;
		final Object element;

		ListElement(String s, Object o) {
			this.display = s;
			this.element = o;
		}
		@Override
		public String toString() {
			return display;
		}
	}
	
	private JFileChooser fileChooser = null;
	private JButton openButton = null;
	private JPanel midLPanel = null;
	private JSlider volSlider = null;
	private JLabel volLabel = null;
//	private JLabel dx8Label = null;
//	private JScrollBar reverbScrollBar = null;
	private JPanel midRPanel = null;
	private JLabel tempoLabel = null;
	private JSlider tempoSlider = null;
	private JPanel midSplitPanel = null;
	private JLabel posLabel = null;
	private JSlider posSlider = null;
	private JButton reverseButton = null;
	public Reverse() {
		super();
		initialize();
		inititalize();
	}

	private void inititalize() {
	
	}

	private JFileChooser getFileChooser() {
		if(fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setDialogTitle("Open a music");
		}
		return fileChooser;
	}
}  //  @jve:decl-index=0:visual-constraint="6,14"
