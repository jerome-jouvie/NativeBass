/*****************************************************************************
 tempo.c/h/rc - Copyright (c) 2003-2008 (: JOBnik! :) [Arthur Aminov, ISRAEL]
                                                      [http://www.jobnik.org]
                                                      [   bass_fx@jobnik.org]

 BASS_FX tempo / rate / pitch with dsp fx
 * Imports: bass.lib, bass_fx.lib
            kernel32.lib, user32.lib, comdlg32.lib, gdi32.lib
 *****************************************************************************/

package jouvieje.bassfx.examples;

import static jouvieje.bass.Bass.BASS_ChannelBytes2Seconds;
import static jouvieje.bass.Bass.BASS_ChannelGetAttribute;
import static jouvieje.bass.Bass.BASS_ChannelGetLength;
import static jouvieje.bass.Bass.BASS_ChannelPlay;
import static jouvieje.bass.Bass.BASS_ChannelSeconds2Bytes;
import static jouvieje.bass.Bass.BASS_ChannelSetAttribute;
import static jouvieje.bass.Bass.BASS_ChannelSetFX;
import static jouvieje.bass.Bass.BASS_ChannelSetPosition;
import static jouvieje.bass.Bass.BASS_ErrorGetCode;
import static jouvieje.bass.Bass.BASS_FXGetParameters;
import static jouvieje.bass.Bass.BASS_FXSetParameters;
import static jouvieje.bass.Bass.BASS_FX_TempoCreate;
import static jouvieje.bass.Bass.BASS_FX_TempoGetRateRatio;
import static jouvieje.bass.Bass.BASS_Free;
import static jouvieje.bass.Bass.BASS_GetVersion;
import static jouvieje.bass.Bass.BASS_Init;
import static jouvieje.bass.Bass.BASS_StreamCreateFile;
import static jouvieje.bass.Bass.BASS_StreamFree;
import static jouvieje.bass.defines.BASS_ATTRIB.BASS_ATTRIB_FREQ;
import static jouvieje.bass.defines.BASS_ATTRIB.BASS_ATTRIB_VOL;
import static jouvieje.bass.defines.BASS_BFX_CHANNEL.BASS_BFX_CHANALL;
import static jouvieje.bass.defines.BASS_FX.BASS_FX_FREESOURCE;
import static jouvieje.bass.defines.BASS_POS.BASS_POS_BYTE;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_LOOP;
import static jouvieje.bass.defines.BASS_STREAM.BASS_STREAM_DECODE;



import static jouvieje.bass.enumerations.BASS_ATTRIB.BASS_ATTRIB_TEMPO;
import static jouvieje.bass.enumerations.BASS_ATTRIB.BASS_ATTRIB_TEMPO_FREQ;
import static jouvieje.bass.enumerations.BASS_ATTRIB.BASS_ATTRIB_TEMPO_PITCH;
import static jouvieje.bass.examples.util.Device.forceFrequency;
import static jouvieje.bass.examples.util.Device.forceNoSoundDevice;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jouvieje.bass.BassInit;
import jouvieje.bass.enumerations.BASS_FX_BFX;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.FileFilters;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.structures.BASS_BFX_PEAKEQ;
import jouvieje.bass.structures.HFX;
import jouvieje.bass.structures.HSTREAM;
import jouvieje.bass.utils.BufferUtils;

/**
 * @author Tom Zhou
 */
public class Tempo extends GraphicalGui {
	private static final long serialVersionUID = 1L;

	private HSTREAM str = null;
	private float oldfreq;
	private FloatBuffer freq = BufferUtils.newFloatBuffer(10);  //  @jve:decl-index=0:
	private HFX fxEQ = null;  

	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(Tempo.this,
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

	public void updatePositionLabel(){

		if(BASS_FX_TempoGetRateRatio(str)==0) return;
		{
			float totalsec = (float)posSlider.getMaximum()/BASS_FX_TempoGetRateRatio(str);
			float posec = (float)posSlider.getValue()/BASS_FX_TempoGetRateRatio(str);

			String positionText = "Playing position: " + (int)posec/60 + ":" + (int)posec%60 + " / " + (int)totalsec/60 + ":" + (int)totalsec%60;
			positionLabel.setText(positionText);

		}
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

					if(getFileChooser().showOpenDialog(Tempo.this) == JFileChooser.APPROVE_OPTION) {
						File file = getFileChooser().getSelectedFile();

						if (str != null)
							BASS_StreamFree(str);

						str = BASS_StreamCreateFile(false,file.getPath(),0,0,BASS_STREAM_DECODE);

						if(str == null)
						{
							error("Selected file couldn't be loaded!");
							return;
						}

						//update the position slider
						posSlider.setMaximum((int)BASS_ChannelBytes2Seconds(str.asInt(), BASS_ChannelGetLength(str.asInt(), BASS_POS_BYTE)));

						//get the current sample rate

						BASS_ChannelGetAttribute(str.asInt(), BASS_ATTRIB_FREQ, freq);


						//create a new stream - decoded & resampled
						if ((str = BASS_FX_TempoCreate(str.asInt(), BASS_SAMPLE_LOOP|BASS_FX_FREESOURCE)) == null)
						{
							error("Couldn't create a resampled stream!");
							BASS_StreamFree(str);
						}


						//set dsp eq to channel
						SetDSP_EQ(0.0f, 2.5f, 0.0f, 125.0f, 1000.0f, 8000.0f);

						//update the button text to show the filename
						openButton.setText(file.getName());

						//set Volume
						BASS_ChannelSetAttribute(str.asInt(), BASS_ATTRIB_VOL,  (float)volSlider.getValue()/100.0f);

						//update tempo sliders
						float freqf = freq.get();
						oldfreq = freqf;
						freq.rewind();

						sampleRateSlider.setMaximum((int) (freqf * 1.3f));
						sampleRateSlider.setMinimum((int)(freqf * 0.7f));
						sampleRateSlider.setValue((int)freqf);

						sampleRateLabel.setText("Samplerate = " + Integer.toString((int)freqf) + "Hz");

						tempoSlider.setValue(0);
						tempoLabel.setText("Tempo = 0%");

						pitchSlider.setValue(0);
						pitchLabel.setText("Pitch Scaling = 0 semitones");

						hz125Slider.setValue(10);
						Khz1Slider.setValue(10);
						Khz8Slider.setValue(10);
						volSlider.setValue(50);


						updatePositionLabel();

						if(str != null){
							if(!BASS_ChannelPlay(str.asInt(), false)) {
								error("Can't play stream");
							}
						}else{
							error("Failed to play the file.");
						}
					}
				}
			});
		}
		return openButton;
	}

	// Update DSP EQ
	private void UpdateFX(int b)
	{
		int v = 0;
		BASS_BFX_PEAKEQ eq = BASS_BFX_PEAKEQ.allocate();

		switch (b)
		{
			case 0:
				v = hz125Slider.getValue();
				break;
			case 1:
				v = Khz8Slider.getValue();
				break;
			case 2:
				v = Khz1Slider.getValue();
				break;
		}

		eq.setBand(b);
		BASS_FXGetParameters(fxEQ, eq);
		eq.setGain((float)(10 - v));
		BASS_FXSetParameters(fxEQ, eq);

		eq.release();
	}

	private void SetDSP_EQ(float fGain, float fBandwidth, float fQ, float fCenter_Bass, float fCenter_Mid, float fCenter_Treble)
	{
		BASS_BFX_PEAKEQ eq = BASS_BFX_PEAKEQ.allocate();

		// set peaking equalizer effect with no bands
		fxEQ=BASS_ChannelSetFX(str.asInt(), BASS_FX_BFX.BASS_FX_BFX_PEAKEQ.asInt(),0);

		eq.setGain(fGain);
		eq.setQ(fQ);
		eq.setBandwidth(fBandwidth);
		eq.setChannel(BASS_BFX_CHANALL);

		// create 1st band for bass
		eq.setBand(0);
		eq.setCenter(fCenter_Bass);
		BASS_FXSetParameters(fxEQ, eq);

		// create 2nd band for mid
		eq.setBand(1);
		eq.setCenter(fCenter_Mid);
		BASS_FXSetParameters(fxEQ, eq);

		// create 3rd band for treble
		eq.setBand(2);
		eq.setCenter(fCenter_Treble);
		BASS_FXSetParameters(fxEQ, eq);

		// update dsp eq
		UpdateFX(0);
		UpdateFX(1);
		UpdateFX(2);

		eq.release();
	}

	private JSlider getVolSlider() {
		if (volSlider == null) {
			volSlider = new JSlider();
			volSlider.setMinimum(0);
			volSlider.setMaximum(100);
			volSlider.setValue(50);
			volSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {

					if(str==null){return;}

					BASS_ChannelSetAttribute(str.asInt(), BASS_ATTRIB_VOL, (float)volSlider.getValue()/100.0f);
				}
			});
		}
		return volSlider;
	}

	private JSlider getHz125Slider() {
		if (hz125Slider == null) {
			hz125Slider = new JSlider();
			hz125Slider.setOrientation(JSlider.VERTICAL);
			hz125Slider.setMinimum(0);
			hz125Slider.setMaximum(20);
			hz125Slider.setValue(10);
			hz125Slider.setInverted(true);
			hz125Slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					UpdateFX(0);
				}
			});

		}
		return hz125Slider;
	}

	private JSlider getKhz1Slider() {
		if (Khz1Slider == null) {
			Khz1Slider = new JSlider();
			Khz1Slider.setOrientation(JSlider.VERTICAL);

			Khz1Slider.setMinimum(0);
			Khz1Slider.setMaximum(20);
			Khz1Slider.setValue(10);
			Khz1Slider.setInverted(true);
			Khz1Slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					UpdateFX(2);
				}
			});
		}
		return Khz1Slider;
	}

	private JSlider getKhz8Slider() {
		if (Khz8Slider == null) {
			Khz8Slider = new JSlider();
			Khz8Slider.setOrientation(JSlider.VERTICAL);
			Khz8Slider.setMinimum(0);
			Khz8Slider.setMaximum(20);
			Khz8Slider.setValue(10);
			Khz8Slider.setInverted(true);
			Khz8Slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					UpdateFX(1);
				}
			});
		}
		return Khz8Slider;
	}

	private JSlider getTempoSlider() {
		if (tempoSlider == null) {
			tempoSlider = new JSlider();
			tempoSlider.setMinimum(-30);
			tempoSlider.setMaximum(30);
			tempoSlider.setValue(0);
			tempoSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {

					if(str==null){return;}

					// set new tempo
					BASS_ChannelSetAttribute(str.asInt(), BASS_ATTRIB_TEMPO.asInt(), (float)tempoSlider.getValue());

					// update tempo static text
					tempoLabel.setText("Tempo = " + tempoSlider.getValue()+ "%");
					updatePositionLabel();
				}
			});

		}
		return tempoSlider;
	}

	private JSlider getSampleRateSlider() {
		if (sampleRateSlider == null) {
			sampleRateSlider = new JSlider();
			sampleRateSlider.setMinimum((int)(44100.0f * 0.7f));
			sampleRateSlider.setMaximum((int)(44100.0f * 1.3f));
			sampleRateSlider.setValue(44100);
			sampleRateSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {

					if(str==null){return;}

					// set new samplerate
					BASS_ChannelSetAttribute(str.asInt(), BASS_ATTRIB_TEMPO_FREQ.asInt(), (float)sampleRateSlider.getValue());

					// update samplerate static text
					sampleRateLabel.setText("Samplerate = " + sampleRateSlider.getValue() + "Hz");


					// update all bands fCenters after changing samplerate
					{
						BASS_BFX_PEAKEQ eq = BASS_BFX_PEAKEQ.allocate();

						int i;
						for(i=0;i<3;i++){
							eq.setBand(i);
							BASS_FXGetParameters(fxEQ, eq);
							eq.setCenter(eq.getCenter() * (float)sampleRateSlider.getValue() / oldfreq);
							BASS_FXSetParameters(fxEQ, eq);
						}
						oldfreq = (float)sampleRateSlider.getValue();

						eq.release();
					}

					updatePositionLabel();
				}
			});

		}
		return sampleRateSlider;
	}
	
	private JSlider getPitchSlider() {
		if (pitchSlider == null) {
			pitchSlider = new JSlider();
			pitchSlider.setMinimum(-30);
			pitchSlider.setMaximum(30);
			pitchSlider.setValue(0);
			pitchSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {

					if(str==null){return;}

					// set new pitch scale
					BASS_ChannelSetAttribute(str.asInt(), BASS_ATTRIB_TEMPO_PITCH.asInt(), (float)pitchSlider.getValue());

					// update pitch static text
					pitchLabel.setText("Pitch Scaling = " + pitchSlider.getValue()+ " semitones");

					// update the approximate time in seconds view
					updatePositionLabel();
				}
			});
		}
		return pitchSlider;
	}

	private JSlider getPosSlider() {
		if (posSlider == null) {
			posSlider = new JSlider();
			posSlider.setValue(0);
			posSlider.addChangeListener(new ChangeListener(){
				@Override
				public void stateChanged(ChangeEvent e) {

					if(str==null){return;}

					BASS_ChannelSetPosition(str.asInt(), BASS_ChannelSeconds2Bytes(str.asInt(), (double)posSlider.getValue()), BASS_POS_BYTE);
					updatePositionLabel();
				}
			});
		}
		return posSlider;
	}


	public static void main(String[] args) {
		new BassExampleFrame(new Tempo());
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
	public String getTitle() { return "BASS Fx - Tempo /rate /pitch with dsp fx"; }

	/* Graphical stuff */

	/**
	 * This method initializes the GUI
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints191 = new GridBagConstraints();
		gridBagConstraints191.gridx = 3;
		gridBagConstraints191.gridy = 3;
		GridBagConstraints gridBagConstraints181 = new GridBagConstraints();
		gridBagConstraints181.gridx = 3;
		gridBagConstraints181.gridy = 1;
		jLabel8 = new JLabel();
		jLabel8.setText("Volume");
		GridBagConstraints gridBagConstraints171 = new GridBagConstraints();
		gridBagConstraints171.fill = GridBagConstraints.BOTH;
		gridBagConstraints171.gridy = 5;
		gridBagConstraints171.weightx = 1.0;
		gridBagConstraints171.gridx = 3;
		GridBagConstraints gridBagConstraints161 = new GridBagConstraints();
		gridBagConstraints161.gridx = 3;
		gridBagConstraints161.gridy = 4;
		positionLabel = new JLabel();
		positionLabel.setText("Playing position: 00:00 / 00:00");
		jLabel3 = new JLabel();
		jLabel3.setText(" 8 khz ");
		jLabel2 = new JLabel();
		jLabel2.setText(" 1 khz ");
		jLabel1 = new JLabel();
		jLabel1.setText(" 125 hz ");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.gridx = 1;
		jLabel = new JLabel();
		jLabel.setText("-= DSP Peaking Equalizer =-");
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 3;
		gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.gridy = 0;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridy = 2;
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.gridx = 3;
		this.setSize(new Dimension(470, 387));
		this.setLayout(new GridBagLayout());
		this.add(getVolSlider(), gridBagConstraints1);
		this.add(getOpenButton(), gridBagConstraints2);
		this.add(positionLabel, gridBagConstraints161);
		this.add(getPosSlider(), gridBagConstraints171);
		this.add(jLabel8, gridBagConstraints181);
		this.add(getMidPanel(), gridBagConstraints191);

	}

	private JPanel getEqPanel() {
		if (eqPanel == null) {
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 2;
			gridBagConstraints9.gridy = 3;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.gridy = 3;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridy = 3;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints6.gridy = 2;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.gridx = 1;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints5.gridy = 2;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.gridx = 2;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints4.gridy = 2;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.gridx = 0;
			eqPanel = new JPanel();
			eqPanel.setLayout(new GridBagLayout());
			eqPanel.add(getHz125Slider(), gridBagConstraints4);
			eqPanel.add(getKhz1Slider(), gridBagConstraints5);
			eqPanel.add(getKhz8Slider(), gridBagConstraints6);
			eqPanel.add(jLabel1, gridBagConstraints7);
			eqPanel.add(jLabel2, gridBagConstraints8);
			eqPanel.add(jLabel3, gridBagConstraints9);
		}
		return eqPanel;
	}

	private JPanel getEqPanel2() {
		if (eqPanel2 == null) {
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.gridwidth = 3;
			gridBagConstraints13.gridy = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 0;
			eqPanel2 = new JPanel();
			eqPanel2.setLayout(new GridBagLayout());
			eqPanel2.add(jLabel, gridBagConstraints3);
			eqPanel2.add(getEqPanel(), gridBagConstraints13);
		}
		return eqPanel2;
	}

	private JPanel getMidPanel() {
		if (midPanel == null) {
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 1;
			gridBagConstraints19.gridy = 0;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 0;
			gridBagConstraints18.gridy = 0;
			midPanel = new JPanel();
			midPanel.setLayout(new GridBagLayout());
			midPanel.add(getEqPanel2(), gridBagConstraints18);
			midPanel.add(getTspPanel(), gridBagConstraints19);
		}
		return midPanel;
	}

	private JPanel getTspPanel() {
		if (tspPanel == null) {
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints16.gridy = 5;
			gridBagConstraints16.weightx = 1.0;
			gridBagConstraints16.gridx = 0;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.gridy = 4;
			pitchLabel = new JLabel();
			pitchLabel.setText("Pitch Scaling = 0 semitones");
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints14.gridy = 3;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.gridx = 0;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridy = 2;
			sampleRateLabel = new JLabel();
			sampleRateLabel.setText("Samplerate = 44100Hz");
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.gridx = 0;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.gridy = 0;
			tempoLabel = new JLabel();
			tempoLabel.setText("Tempo = 0%");
			tspPanel = new JPanel();
			tspPanel.setLayout(new GridBagLayout());
			tspPanel.add(tempoLabel, gridBagConstraints10);
			tspPanel.add(getTempoSlider(), gridBagConstraints11);
			tspPanel.add(sampleRateLabel, gridBagConstraints12);
			tspPanel.add(getSampleRateSlider(), gridBagConstraints14);
			tspPanel.add(pitchLabel, gridBagConstraints15);
			tspPanel.add(getPitchSlider(), gridBagConstraints16);
		}
		return tspPanel;
	}

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
	private JSlider volSlider = null;
	private JLabel jLabel = null;
	private JSlider hz125Slider = null;
	private JSlider Khz1Slider = null;
	private JSlider Khz8Slider = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JPanel eqPanel = null;
	private JPanel tspPanel = null;
	private JLabel tempoLabel = null;
	private JSlider tempoSlider = null;
	private JLabel sampleRateLabel = null;
	private JSlider sampleRateSlider = null;
	private JLabel pitchLabel = null;
	private JSlider pitchSlider = null;
	private JLabel positionLabel = null;
	private JSlider posSlider = null;
	private JLabel jLabel8 = null;
	private JPanel eqPanel2 = null;
	private JPanel midPanel = null;
	public Tempo() {
		super();
		initialize();

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

}  //  @jve:decl-index=0:visual-constraint="20,-54"
