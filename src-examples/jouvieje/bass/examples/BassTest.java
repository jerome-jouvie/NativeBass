// BASS Simple Test, copyright (c) 1999-2006 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_ATTRIB.BASS_ATTRIB_PAN;
import static jouvieje.bass.defines.BASS_ATTRIB.BASS_ATTRIB_VOL;
import static jouvieje.bass.defines.BASS_CONFIG.BASS_CONFIG_GVOL_MUSIC;
import static jouvieje.bass.defines.BASS_CONFIG.BASS_CONFIG_GVOL_SAMPLE;
import static jouvieje.bass.defines.BASS_CONFIG.BASS_CONFIG_GVOL_STREAM;
import static jouvieje.bass.defines.BASS_CONFIG.BASS_CONFIG_UPDATETHREADS;
import static jouvieje.bass.defines.BASS_MUSIC.BASS_MUSIC_RAMPS;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_OVER_POS;





import static jouvieje.bass.examples.util.Device.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import jouvieje.bass.BassInit;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.FileFilters;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.structures.HCHANNEL;
import jouvieje.bass.structures.HMUSIC;
import jouvieje.bass.structures.HSAMPLE;
import jouvieje.bass.structures.HSTREAM;

/**
 * I've ported the C BASS example to NativeBass
 * 
 * @author Jérôme Jouvie (Jouvieje)
 * 
 * WANT TO CONTACT ME ?
 * @author Jérôme Jouvie (Jouvieje)
 * @site   http://jerome.jouvie.free.fr/
 * @mail   jerome.jouvie@gmail.com
 */
public class BassTest extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(BassTest.this,
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
	
	public static void main(String[] args) {
		new BassExampleFrame(new BassTest());
	}
	
	private boolean init   = false;
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
		
		BASS_Free();
	}
	
	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS - Simple Playback Test"; }

	private Timer timer = new Timer(250, new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			/* update the CPU usage % display */
			String s = String.format("CPU%%  %.2f", BASS_GetCPU());
			getCpuLabel().setText(s);
		}
	});
	
			/* Graphical stuff */
	
	private DefaultListModel streamListModel = new DefaultListModel();
	private DefaultListModel musicListModel  = new DefaultListModel();
	private DefaultListModel sampleListModel = new DefaultListModel();

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
	private JPanel streamP = null;
	private JPanel musicP = null;
	private JPanel sampleP = null;
	private JPanel buttonP = null;  //  @jve:decl-index=0:visual-constraint="30,10"
	private JButton stopAllB = null;
	private JButton resumeB = null;
	private JLabel cpuLabel = null;
	private JScrollPane streamLSP = null;
	private JList streamL = null;
	private JButton streamPlayB = null;
	private JButton streamStopB = null;
	private JButton streamRestartB = null;
	private JButton streamAddB = null;
	private JButton streamRemoveB = null;
	private JScrollPane musicLSP = null;
	private JScrollPane sampleLSP = null;
	private JList musicL = null;
	private JList sampleL = null;
	private JButton musicPlayB = null;
	private JButton musicStopB = null;
	private JButton musicRestartB = null;
	private JButton musicAddB = null;
	private JButton musicRemoveB = null;
	private JButton samplePlayB = null;
	private JButton sampleAddB = null;
	private JButton sampleRemoveB = null;
	private JLabel sampleLabel = null;
	private JLabel streamLabel = null;
	private JLabel musicLabel = null;
	private JSlider sampleSlider = null;
	private JSlider musicSLider = null;
	private JSlider streamSlider = null;
	private JSlider volumeSlider = null;
	private JLabel volumeLabel = null;
	private JCheckBox updateThreadCB = null;
	private JPanel subPanel = null;
	
	public BassTest() {
		super();
		inititalize();
	}

	private void inititalize() {
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.weightx = 1.0D;
		gridBagConstraints3.fill = GridBagConstraints.BOTH;
		gridBagConstraints3.gridwidth = 3;
		gridBagConstraints3.gridy = 1;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 2;
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.weightx = 1.0D;
		gridBagConstraints2.weighty = 1.0D;
		gridBagConstraints2.gridy = 0;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.weightx = 1.0D;
		gridBagConstraints1.weighty = 1.0D;
		gridBagConstraints1.gridy = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0D;
		gridBagConstraints.weighty = 1.0D;
		gridBagConstraints.gridy = 0;
		this.setSize(new Dimension(650, 250));
		this.setLayout(new GridBagLayout());
		this.add(getStreamP(), gridBagConstraints);
		this.add(getMusicP(), gridBagConstraints1);
		this.add(getSampleP(), gridBagConstraints2);
		this.add(getButtonP(), gridBagConstraints3);
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

	private JPanel getStreamP() {
		if(streamP == null) {
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints31.gridy = 4;
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.gridwidth = 6;
			gridBagConstraints31.insets = new Insets(0, 20, 0, 20);
			gridBagConstraints31.gridx = 0;
			GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
			gridBagConstraints28.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints28.gridy = 4;
			gridBagConstraints28.weightx = 1.0;
			gridBagConstraints28.gridx = 1;
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints24.gridy = 4;
			gridBagConstraints24.weightx = 1.0;
			gridBagConstraints24.gridwidth = 6;
			gridBagConstraints24.insets = new Insets(0, 20, 0, 20);
			gridBagConstraints24.gridx = 0;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.gridwidth = 6;
			gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints21.anchor = GridBagConstraints.WEST;
			gridBagConstraints21.gridy = 3;
			streamLabel = new JLabel();
			streamLabel.setText("Global volume:");
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 3;
			gridBagConstraints9.gridwidth = 3;
			gridBagConstraints9.weightx = 1.0D;
			gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.gridy = 2;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridwidth = 3;
			gridBagConstraints8.weightx = 1.0D;
			gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints8.gridy = 2;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 4;
			gridBagConstraints7.gridwidth = 2;
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.weightx = 1.0D;
			gridBagConstraints7.gridy = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 2;
			gridBagConstraints6.gridwidth = 2;
			gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.weightx = 1.0D;
			gridBagConstraints6.gridy = 1;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.weightx = 1.0D;
			gridBagConstraints5.gridy = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.BOTH;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.weighty = 1.0;
			gridBagConstraints4.gridwidth = 6;
			gridBagConstraints4.gridx = 0;
			streamP = new JPanel();
			streamP.setLayout(new GridBagLayout());
			streamP.setBorder(BorderFactory.createTitledBorder(null, "Stream", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			streamP.add(getStreamLSP(), gridBagConstraints4);
			streamP.add(getStreamPlayB(), gridBagConstraints5);
			streamP.add(getStreamStopB(), gridBagConstraints6);
			streamP.add(getStreamRestartB(), gridBagConstraints7);
			streamP.add(getStreamAddB(), gridBagConstraints8);
			streamP.add(getStreamRemoveB(), gridBagConstraints9);
			streamP.add(streamLabel, gridBagConstraints21);
			streamP.add(getStreamSlider(), gridBagConstraints31);
		}
		return streamP;
	}

	private JPanel getMusicP() {
		if(musicP == null) {
			GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
			gridBagConstraints30.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints30.gridy = 4;
			gridBagConstraints30.weightx = 1.0;
			gridBagConstraints30.gridwidth = 6;
			gridBagConstraints30.insets = new Insets(0, 20, 0, 20);
			gridBagConstraints30.gridx = 0;
			GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
			gridBagConstraints27.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints27.gridy = 4;
			gridBagConstraints27.weightx = 1.0;
			gridBagConstraints27.gridx = 1;
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints25.gridy = 4;
			gridBagConstraints25.weightx = 1.0;
			gridBagConstraints25.gridwidth = 6;
			gridBagConstraints25.insets = new Insets(0, 20, 0, 20);
			gridBagConstraints25.gridx = 1;
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 0;
			gridBagConstraints22.gridwidth = 6;
			gridBagConstraints22.anchor = GridBagConstraints.WEST;
			gridBagConstraints22.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints22.gridy = 3;
			musicLabel = new JLabel();
			musicLabel.setText("Global volume:");
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 3;
			gridBagConstraints16.gridwidth = 3;
			gridBagConstraints16.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints16.gridy = 2;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.gridwidth = 3;
			gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.gridy = 2;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 4;
			gridBagConstraints14.gridwidth = 2;
			gridBagConstraints14.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints14.weightx = 1.0D;
			gridBagConstraints14.gridy = 1;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 2;
			gridBagConstraints13.gridwidth = 2;
			gridBagConstraints13.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints13.weightx = 1.0D;
			gridBagConstraints13.gridy = 1;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridwidth = 2;
			gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints12.weightx = 1.0D;
			gridBagConstraints12.gridy = 1;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.fill = GridBagConstraints.BOTH;
			gridBagConstraints10.gridy = 0;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.weighty = 1.0;
			gridBagConstraints10.gridwidth = 6;
			gridBagConstraints10.gridx = 0;
			musicP = new JPanel();
			musicP.setLayout(new GridBagLayout());
			musicP.setBorder(BorderFactory.createTitledBorder(null, "Music", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			musicP.add(getMusicLSP(), gridBagConstraints10);
			musicP.add(getMusicPlayB(), gridBagConstraints12);
			musicP.add(getMusicStopB(), gridBagConstraints13);
			musicP.add(getMusicRestartB(), gridBagConstraints14);
			musicP.add(getMusicAddB(), gridBagConstraints15);
			musicP.add(getMusicRemoveB(), gridBagConstraints16);
			musicP.add(musicLabel, gridBagConstraints22);
			musicP.add(getMusicSLider(), gridBagConstraints30);
		}
		return musicP;
	}

	private JPanel getSampleP() {
		if(sampleP == null) {
			GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
			gridBagConstraints29.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints29.gridy = 4;
			gridBagConstraints29.weightx = 1.0;
			gridBagConstraints29.gridwidth = 6;
			gridBagConstraints29.insets = new Insets(0, 20, 0, 20);
			gridBagConstraints29.gridx = 0;
			GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
			gridBagConstraints26.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints26.gridy = 4;
			gridBagConstraints26.weightx = 1.0;
			gridBagConstraints26.gridx = 2;
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints23.gridy = 4;
			gridBagConstraints23.weightx = 1.0;
			gridBagConstraints23.gridwidth = 6;
			gridBagConstraints23.insets = new Insets(0, 20, 0, 20);
			gridBagConstraints23.gridx = 0;
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 0;
			gridBagConstraints20.anchor = GridBagConstraints.WEST;
			gridBagConstraints20.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints20.gridwidth = 6;
			gridBagConstraints20.gridy = 3;
			sampleLabel = new JLabel();
			sampleLabel.setText("Global volume:");
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 3;
			gridBagConstraints19.weightx = 1.0D;
			gridBagConstraints19.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints19.gridwidth = 3;
			gridBagConstraints19.gridy = 2;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 0;
			gridBagConstraints18.weightx = 1.0D;
			gridBagConstraints18.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints18.gridwidth = 3;
			gridBagConstraints18.gridy = 2;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 0;
			gridBagConstraints17.gridwidth = 6;
			gridBagConstraints17.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints17.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.BOTH;
			gridBagConstraints11.gridy = 0;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.weighty = 1.0;
			gridBagConstraints11.gridwidth = 6;
			gridBagConstraints11.gridx = 0;
			sampleP = new JPanel();
			sampleP.setLayout(new GridBagLayout());
			sampleP.setBorder(BorderFactory.createTitledBorder(null, "Sample", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			sampleP.add(getSampleLSP(), gridBagConstraints11);
			sampleP.add(getSamplePlayB(), gridBagConstraints17);
			sampleP.add(getSampleAddB(), gridBagConstraints18);
			sampleP.add(getSampleRemoveB(), gridBagConstraints19);
			sampleP.add(sampleLabel, gridBagConstraints20);
			sampleP.add(getSampleSlider(), gridBagConstraints29);
		}
		return sampleP;
	}

	private JPanel getButtonP() {
		if(buttonP == null) {
			GridBagConstraints gridBagConstraints38 = new GridBagConstraints();
			gridBagConstraints38.gridy = 0;
			gridBagConstraints38.weightx = 1.0D;
			gridBagConstraints38.fill = GridBagConstraints.BOTH;
			gridBagConstraints38.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints38.gridx = 2;
			GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
			gridBagConstraints33.gridy = 0;
			gridBagConstraints33.insets = new Insets(0, 5, 0, 5);
			gridBagConstraints33.gridx = 1;
			GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
			gridBagConstraints32.gridy = 0;
			gridBagConstraints32.insets = new Insets(0, 5, 0, 5);
			gridBagConstraints32.gridx = 0;
			volumeLabel = new JLabel();
			volumeLabel.setText("Volume :");
			buttonP = new JPanel();
			buttonP.setLayout(new GridBagLayout());
			buttonP.setSize(new Dimension(500, 60));
			buttonP.setPreferredSize(new Dimension(500, 60));
			buttonP.add(getStopAllB(), gridBagConstraints32);
			buttonP.add(getResumeB(), gridBagConstraints33);
			buttonP.add(getSubPanel(), gridBagConstraints38);
		}
		return buttonP;
	}

	private JLabel getCpuLabel() {
		if(cpuLabel == null) {
			cpuLabel = new JLabel();
			cpuLabel.setText("CPU");
			cpuLabel.setName("cpuL");
		}
		return cpuLabel;
	}

	private JButton getStopAllB() {
		if(stopAllB == null) {
			stopAllB = new JButton();
			stopAllB.setText("Stop output");
			stopAllB.setName("stopAllB");
			stopAllB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					/* Pause output */
					BASS_Pause();
				}
			});
		}
		return stopAllB;
	}

	private JButton getResumeB() {
		if(resumeB == null) {
			resumeB = new JButton();
			resumeB.setText("Resume");
			resumeB.setName("resumeB");
			resumeB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					/* Resume output */
					BASS_Start();
				}
			});
		}
		return resumeB;
	}

	private JScrollPane getStreamLSP() {
		if(streamLSP == null) {
			streamLSP = new JScrollPane();
			streamLSP.setViewportView(getStreamL());
		}
		return streamLSP;
	}

	private JList getStreamL() {
		if(streamL == null) {
			streamL = new JList(streamListModel);
		}
		return streamL;
	}

	private JButton getStreamPlayB() {
		if(streamPlayB == null) {
			streamPlayB = new JButton();
			streamPlayB.setText("Play");
			streamPlayB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					/* Play the stream (continue from current position) */
					int index = getStreamL().getSelectedIndex();
					if(index != -1) {
						ListElement item = (ListElement)streamListModel.get(index);
						if(!BASS_ChannelPlay(((HSTREAM)item.element).asInt(), false)) {
							error("Can't play stream");
						}
					}
				}
			});
		}
		return streamPlayB;
	}

	private JButton getStreamStopB() {
		if(streamStopB == null) {
			streamStopB = new JButton();
			streamStopB.setText("Stop");
			streamStopB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = getStreamL().getSelectedIndex();
					if(index != -1) {
						/* Stop the stream */
						ListElement item = (ListElement)streamListModel.get(index);
						BASS_ChannelStop(((HSTREAM)item.element).asInt());
					}
				}
			});
		}
		return streamStopB;
	}

	private JButton getStreamRestartB() {
		if(streamRestartB == null) {
			streamRestartB = new JButton();
			streamRestartB.setText("Restart");
			streamRestartB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = getStreamL().getSelectedIndex();
					if(index != -1) {
						/* Stop the stream */
						ListElement item = (ListElement)streamListModel.get(index);
						BASS_ChannelPlay(((HSTREAM)item.element).asInt(), true);
					}
				}
			});
		}
		return streamRestartB;
	}

	private JButton getStreamAddB() {
		if(streamAddB == null) {
			streamAddB = new JButton();
			streamAddB.setText("Add");
			streamAddB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getFileChooser().resetChoosableFileFilters();
					getFileChooser().addChoosableFileFilter(FileFilters.allFiles);
					getFileChooser().addChoosableFileFilter(FileFilters.streamableFiles);

					if(getFileChooser().showOpenDialog(BassTest.this) == JFileChooser.APPROVE_OPTION) {
						File file = getFileChooser().getSelectedFile();

						HSTREAM str = BASS_StreamCreateFile(false, file.getPath(), 0, 0, 0);
						if(str != null) {
							streamListModel.addElement(new ListElement(file.getName(), str));
						}
						else {
							error("Can't open stream");
						}
					}
				}
			});
		}
		return streamAddB;
	}

	private JButton getStreamRemoveB() {
		if(streamRemoveB == null) {
			streamRemoveB = new JButton();
			streamRemoveB.setText("Remove");
			streamRemoveB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = getStreamL().getSelectedIndex();
					if(index != -1) {
						ListElement item = (ListElement)streamListModel.get(index);
						/* Free the music from memory */
						BASS_StreamFree((HSTREAM)item.element);
						streamListModel.remove(index);
					}
				}
			});
		}
		return streamRemoveB;
	}

	private JScrollPane getMusicLSP() {
		if(musicLSP == null) {
			musicLSP = new JScrollPane();
			musicLSP.setViewportView(getMusicL());
		}
		return musicLSP;
	}

	private JScrollPane getSampleLSP() {
		if(sampleLSP == null) {
			sampleLSP = new JScrollPane();
			sampleLSP.setViewportView(getSampleL());
		}
		return sampleLSP;
	}

	private JList getMusicL() {
		if(musicL == null) {
			musicL = new JList(musicListModel);
		}
		return musicL;
	}

	private JList getSampleL() {
		if(sampleL == null) {
			sampleL = new JList(sampleListModel);
		}
		return sampleL;
	}

	private JButton getMusicPlayB() {
		if(musicPlayB == null) {
			musicPlayB = new JButton();
			musicPlayB.setText("Play");
			musicPlayB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					/* Play the music (continue from current position) */
					int index = getMusicL().getSelectedIndex();
					if(index != -1) {
						ListElement item = (ListElement)musicListModel.get(index);
						if (!BASS_ChannelPlay(((HMUSIC)item.element).asInt(), false)) {
							error("Can't play music");
						}
					}
				}
			});
		}
		return musicPlayB;
	}

	private JButton getMusicStopB() {
		if(musicStopB == null) {
			musicStopB = new JButton();
			musicStopB.setText("Stop");
			musicStopB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = getMusicL().getSelectedIndex();
					if(index != -1) {
						/* Stop the music */
						ListElement item = (ListElement)musicListModel.get(index);
						BASS_ChannelStop(((HMUSIC)item.element).asInt());
					}
				}
			});
		}
		return musicStopB;
	}

	private JButton getMusicRestartB() {
		if(musicRestartB == null) {
			musicRestartB = new JButton();
			musicRestartB.setText("Restart");
			musicRestartB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = getMusicL().getSelectedIndex();
					if(index != -1) {
						/* Play the music from the start */
						ListElement item = (ListElement)musicListModel.get(index);
						BASS_ChannelPlay(((HMUSIC)item.element).asInt(), true);
					}
				}
			});
		}
		return musicRestartB;
	}

	private JButton getMusicAddB() {
		if(musicAddB == null) {
			musicAddB = new JButton();
			musicAddB.setText("Add");
			musicAddB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getFileChooser().resetChoosableFileFilters();
					getFileChooser().addChoosableFileFilter(FileFilters.allFiles);
					getFileChooser().addChoosableFileFilter(FileFilters.modMusicFiles);

					if(getFileChooser().showOpenDialog(BassTest.this) == JFileChooser.APPROVE_OPTION) {
						File file = getFileChooser().getSelectedFile();

						HMUSIC mod = BASS_MusicLoad(false, file.getPath(), 0, 0, BASS_MUSIC_RAMPS, 0);
						if(mod != null) {
							musicListModel.addElement(new ListElement(file.getName(), mod));
						}
						else {
							error("Can't open music");
						}
					}
				}
			});
		}
		return musicAddB;
	}

	private JButton getMusicRemoveB() {
		if(musicRemoveB == null) {
			musicRemoveB = new JButton();
			musicRemoveB.setText("Remove");
			musicRemoveB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = getMusicL().getSelectedIndex();
					if(index != -1) {
						ListElement item = (ListElement)musicListModel.get(index);
						/* Free the music from memory */
						BASS_MusicFree((HMUSIC)item.element);
						musicListModel.remove(index);
					}
				}
			});
		}
		return musicRemoveB;
	}

	private JButton getSamplePlayB() {
		if(samplePlayB == null) {
			samplePlayB = new JButton();
			samplePlayB.setText("Play");
			samplePlayB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//Play the sample at default rate, volume=50, random pan position
					int index = getMusicL().getSelectedIndex();
					if(index != -1) {
						ListElement item = (ListElement)sampleListModel.get(index);
						HCHANNEL channel = BASS_SampleGetChannel((HSAMPLE)item.element, false);
						
						BASS_ChannelSetAttribute(channel.asInt(), BASS_ATTRIB_VOL, 0.5f);
						BASS_ChannelSetAttribute(channel.asInt(), BASS_ATTRIB_PAN, (float)Math.random()*2.0f-1.0f);
						if(!BASS_ChannelPlay(channel.asInt(), false)) {
							error("Can't play sample");
						}
					}
				}
			});
		}
		return samplePlayB;
	}

	private JButton getSampleAddB() {
		if(sampleAddB == null) {
			sampleAddB = new JButton();
			sampleAddB.setText("Add");
			sampleAddB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getFileChooser().resetChoosableFileFilters();
					getFileChooser().addChoosableFileFilter(FileFilters.allFiles);
					getFileChooser().addChoosableFileFilter(FileFilters.sampleFiles);

					if(getFileChooser().showOpenDialog(BassTest.this) == JFileChooser.APPROVE_OPTION) {
						File file = getFileChooser().getSelectedFile();
						
						HSAMPLE sam = BASS_SampleLoad(false, file.getPath(), 0, 0, 3, BASS_SAMPLE_OVER_POS);
						if(sam != null) {
							sampleListModel.addElement(new ListElement(file.getName(), sam));
						}
						else {
							error("Can't load sample");
						}
					}
				}
			});
		}
		return sampleAddB;
	}

	private JButton getSampleRemoveB() {
		if(sampleRemoveB == null) {
			sampleRemoveB = new JButton();
			sampleRemoveB.setText("Remove");
			sampleRemoveB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = getSampleL().getSelectedIndex();
					if(index != -1) {
						ListElement item = (ListElement)sampleListModel.get(index);
						/* Free the music from memory */
						BASS_SampleFree((HSAMPLE)item.element);
						sampleListModel.remove(index);
					}
				}
			});
		}
		return sampleRemoveB;
	}

	private JSlider getSampleSlider() {
		if(sampleSlider == null) {
			sampleSlider = new JSlider(0, 10000);
			sampleSlider.setValue(10000);
			sampleSlider.addChangeListener(new javax.swing.event.ChangeListener(){
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					BASS_SetConfig(BASS_CONFIG_GVOL_SAMPLE, sampleSlider.getValue());
				}
			});
		}
		return sampleSlider;
	}

	private JSlider getMusicSLider() {
		if(musicSLider == null) {
			musicSLider = new JSlider(0, 10000);
			musicSLider.setValue(10000);
			musicSLider.addChangeListener(new javax.swing.event.ChangeListener(){
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					BASS_SetConfig(BASS_CONFIG_GVOL_MUSIC, musicSLider.getValue());
				}
			});
		}
		return musicSLider;
	}

	private JSlider getStreamSlider() {
		if(streamSlider == null) {
			streamSlider = new JSlider(0, 10000);
			streamSlider.setValue(10000);
			streamSlider.addChangeListener(new javax.swing.event.ChangeListener(){
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					BASS_SetConfig(BASS_CONFIG_GVOL_STREAM, streamSlider.getValue());
				}
			});
		}
		return streamSlider;
	}

	private JSlider getVolumeSlider() {
		if(volumeSlider == null) {
			volumeSlider = new JSlider();
			volumeSlider.setValue(100);
			volumeSlider.addChangeListener(new javax.swing.event.ChangeListener(){
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					BASS_SetVolume(volumeSlider.getValue() / 100.f);
				}
			});
		}
		return volumeSlider;
	}

	private JCheckBox getUpdateThreadCB() {
		if(updateThreadCB == null) {
			updateThreadCB = new JCheckBox();
			updateThreadCB.setText("2 update threads");
			updateThreadCB.addItemListener(new java.awt.event.ItemListener(){
				@Override
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					BASS_SetConfig(BASS_CONFIG_UPDATETHREADS, updateThreadCB.isSelected() ? 2 : 1);
				}
			});
		}
		return updateThreadCB;
	}

	private JPanel getSubPanel() {
		if(subPanel == null) {
			GridBagConstraints gridBagConstraints37 = new GridBagConstraints();
			gridBagConstraints37.gridx = 1;
			gridBagConstraints37.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints37.gridy = 0;
			GridBagConstraints gridBagConstraints36 = new GridBagConstraints();
			gridBagConstraints36.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints36.gridy = 0;
			gridBagConstraints36.gridx = 0;
			GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
			gridBagConstraints35.insets = new Insets(10, 3, 10, 3);
			gridBagConstraints35.gridy = 1;
			gridBagConstraints35.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints35.gridx = -1;
			GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
			gridBagConstraints34.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints34.gridy = 1;
			gridBagConstraints34.weightx = 1.0;
			gridBagConstraints34.gridx = -1;
			subPanel = new JPanel();
			subPanel.setLayout(new GridBagLayout());
			subPanel.add(getVolumeSlider(), gridBagConstraints34);
			subPanel.add(getCpuLabel(), gridBagConstraints35);
			subPanel.add(volumeLabel, gridBagConstraints36);
			subPanel.add(getUpdateThreadCB(), gridBagConstraints37);
		}
		return subPanel;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
