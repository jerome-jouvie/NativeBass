// BASS multi-speaker example, copyright (c) 2003-2006 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_DEVICE.BASS_DEVICE_SPEAKERS;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_LOOP;
import static jouvieje.bass.defines.BASS_SPEAKER.BASS_SPEAKER_CENLFE;
import static jouvieje.bass.defines.BASS_SPEAKER.BASS_SPEAKER_FRONT;
import static jouvieje.bass.defines.BASS_SPEAKER.BASS_SPEAKER_REAR;
import static jouvieje.bass.defines.BASS_SPEAKER.BASS_SPEAKER_REAR2;





import static jouvieje.bass.examples.util.Device.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import jouvieje.bass.BassInit;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.FileFilters;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.structures.BASS_INFO;
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
public class Speakers extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(Speakers.this,
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
		new BassExampleFrame(new Speakers());
	}
	
	private boolean init   = false;
	private boolean deinit = false;

	private int[] flags = new int[]{BASS_SPEAKER_FRONT, BASS_SPEAKER_REAR, BASS_SPEAKER_CENLFE, BASS_SPEAKER_REAR2};
	private HSTREAM[] chan = new HSTREAM[4];

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
		
		//Initialize BASS - default device
		if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), 0, null, null)) {
			error("Can't initialize device");
			stop();
			return;
		}

		{
			//Check how many speakers the device supports
			BASS_INFO info = BASS_INFO.allocate();
			BASS_GetInfo(info);

			//No extra speakers detected, enable them anyway?
			if(info.getSpeakers() < 4) {
				int result = JOptionPane.showConfirmDialog(this, "Do you wish to enable \"speaker assignment\" anyway?",
						"No extra speakers detected", JOptionPane.YES_NO_OPTION);
				if(result == JOptionPane.YES_OPTION) {
					//Reinitialize BASS - forcing speaker assignment
					BASS_Free();
					if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), BASS_DEVICE_SPEAKERS, null, null)) {
						error("Can't initialize device");
						stop();
						info.release();
						return;
					}
					BASS_GetInfo(info); // get info again
				}
			}
			if(info.getSpeakers() < 8) {
				getOpen4().setEnabled(false);
				getSwap34().setEnabled(false);
			}
			if(info.getSpeakers() < 6) {
				getOpen3().setEnabled(false);
				getSwap23().setEnabled(false);
			}
			if(info.getSpeakers() < 4) {
				getOpen2().setEnabled(false);
				getSwap12().setEnabled(false);
				flags[0]=0; // no multi-speaker support, so remove speaker flag for normal stereo output
			}
			info.release();
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

	private void openFile(int speaker, JButton button) {
		int result = getFileChooser().showOpenDialog(this);
		if(result == JFileChooser.APPROVE_OPTION) {
			File file = getFileChooser().getSelectedFile();
			BASS_StreamFree(chan[speaker]);		// free old stream before opening new
			chan[speaker] = BASS_StreamCreateFile(false, file.getAbsolutePath(), 0, 0, flags[speaker] | BASS_SAMPLE_LOOP);
			if(chan[speaker] == null) {
				button.setText("click here to open a file...");
				error("Can't play the file");
				return;
			}
			button.setText(file.getName());
			BASS_ChannelPlay(chan[speaker].asInt(), false);
		}
	}

	private void swap(int speaker, JButton b0, JButton b1) {
		//Swap handles
		HSTREAM stream  = chan[speaker];
		chan[speaker]   = chan[speaker+1];
		chan[speaker+1] = stream;

		//Swap text
		String text0 = b0.getText();
		String text1 = b1.getText();
		b0.setText(text1);
		b1.setText(text0);

		//Update speaker flags
		BASS_ChannelFlags(chan[speaker].asInt(),   flags[speaker],BASS_SPEAKER_FRONT);
		BASS_ChannelFlags(chan[speaker+1].asInt(), flags[speaker+1],BASS_SPEAKER_FRONT);
	}
	
	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS multi-speaker example"; }

	/* Graphical code*/

	private JPanel front = null;
	private JPanel rear = null;
	private JPanel center = null;
	private JPanel readCenter = null;
	private JButton swap12 = null;
	private JButton swap23 = null;
	private JButton swap34 = null;
	private JButton open1 = null;
	private JButton open2 = null;
	private JButton open3 = null;
	private JButton open4 = null;
	private JFileChooser fileChooser = null;

	public Speakers() {
		super();
		initialize();
	}

	private void initialize() {
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.gridx = 0;
		gridBagConstraints6.weightx = 1.0D;
		gridBagConstraints6.fill = GridBagConstraints.BOTH;
		gridBagConstraints6.weighty = 1.0D;
		gridBagConstraints6.gridheight = 2;
		gridBagConstraints6.gridy = 6;
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridx = 0;
		gridBagConstraints5.weightx = 1.0D;
		gridBagConstraints5.fill = GridBagConstraints.BOTH;
		gridBagConstraints5.weighty = 1.0D;
		gridBagConstraints5.gridheight = 2;
		gridBagConstraints5.gridy = 4;
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 0;
		gridBagConstraints4.weightx = 1.0D;
		gridBagConstraints4.gridheight = 2;
		gridBagConstraints4.fill = GridBagConstraints.BOTH;
		gridBagConstraints4.weighty = 1.0D;
		gridBagConstraints4.gridy = 2;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.weightx = 1.0D;
		gridBagConstraints3.gridheight = 2;
		gridBagConstraints3.fill = GridBagConstraints.BOTH;
		gridBagConstraints3.weighty = 1.0D;
		gridBagConstraints3.gridy = 0;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.gridheight = 3;
		gridBagConstraints2.gridy = 5;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.gridheight = 3;
		gridBagConstraints1.gridy = 3;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridheight = 3;
		gridBagConstraints.gridy = 1;
		this.setSize(new Dimension(351, 245));
		this.setPreferredSize(new Dimension(351, 245));
		this.setLayout(new GridBagLayout());
		this.add(getFront(), gridBagConstraints3);
		this.add(getRear(), gridBagConstraints4);
		this.add(getCenter(), gridBagConstraints5);
		this.add(getReadCenter(), gridBagConstraints6);
		this.add(getSwap12(), gridBagConstraints);
		this.add(getSwap23(), gridBagConstraints1);
		this.add(getSwap34(), gridBagConstraints2);
	}

	private JPanel getFront() {
		if(front == null) {
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints8.insets = new Insets(0, 20, 0, 20);
			gridBagConstraints8.weightx = 1.0D;
			front = new JPanel();
			front.setLayout(new GridBagLayout());
			front.setBorder(BorderFactory.createTitledBorder(null, "1 - Front", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			front.add(getOpen1(), gridBagConstraints8);
		}
		return front;
	}

	private JPanel getRear() {
		if(rear == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.insets = new Insets(0, 20, 0, 20);
			gridBagConstraints7.weightx = 1.0D;
			rear = new JPanel();
			rear.setLayout(new GridBagLayout());
			rear.setBorder(BorderFactory.createTitledBorder(null, "2 - Rear", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			rear.add(getOpen2(), gridBagConstraints7);
		}
		return rear;
	}

	private JPanel getCenter() {
		if(center == null) {
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.insets = new Insets(0, 20, 0, 20);
			gridBagConstraints9.weightx = 1.0D;
			center = new JPanel();
			center.setLayout(new GridBagLayout());
			center.setBorder(BorderFactory.createTitledBorder(null, "3 - Center/LFE", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			center.add(getOpen3(), gridBagConstraints9);
		}
		return center;
	}

	private JPanel getReadCenter() {
		if(readCenter == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints10.insets = new Insets(0, 20, 0, 20);
			gridBagConstraints10.weightx = 1.0D;
			readCenter = new JPanel();
			readCenter.setLayout(new GridBagLayout());
			readCenter.setBorder(BorderFactory.createTitledBorder(null, "4 - Rear Center", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			readCenter.add(getOpen4(), gridBagConstraints10);
		}
		return readCenter;
	}

	private JButton getSwap12() {
		if(swap12 == null) {
			swap12 = new JButton();
			swap12.setText("Swap");
			swap12.setName("swap1");
			swap12.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					swap(0, open1, open2);
				}
			});
		}
		return swap12;
	}

	private JButton getSwap23() {
		if(swap23 == null) {
			swap23 = new JButton();
			swap23.setText("Swap");
			swap23.setName("swap2");
			swap23.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					swap(1, open2, open3);
				}
			});
		}
		return swap23;
	}

	private JButton getSwap34() {
		if(swap34 == null) {
			swap34 = new JButton();
			swap34.setText("Swap");
			swap34.setName("swap3");
			swap34.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					swap(2, open3, open4);
				}
			});
		}
		return swap34;
	}

	private JButton getOpen1() {
		if(open1 == null) {
			open1 = new JButton();
			open1.setText("click here to open a file...");
			open1.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					openFile(0, open1);
				}
			});
		}
		return open1;
	}

	private JButton getOpen2() {
		if(open2 == null) {
			open2 = new JButton();
			open2.setText("click here to open a file...");
			open2.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					openFile(1, open2);
				}
			});
		}
		return open2;
	}

	private JButton getOpen3() {
		if(open3 == null) {
			open3 = new JButton();
			open3.setText("click here to open a file...");
			open3.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					openFile(2, open3);
				}
			});
		}
		return open3;
	}

	private JButton getOpen4() {
		if(open4 == null) {
			open4 = new JButton();
			open4.setText("click here to open a file...");
			open4.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					openFile(3, open4);
				}
			});
		}
		return open4;
	}

	private JFileChooser getFileChooser() {
		if(fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.resetChoosableFileFilters();
			fileChooser.addChoosableFileFilter(FileFilters.allFiles);
			fileChooser.addChoosableFileFilter(FileFilters.streamableFiles);
			fileChooser.setDialogTitle("Open a music");
		}
		return fileChooser;
	}
}  //  @jve:decl-index=0:visual-constraint="8,3"
