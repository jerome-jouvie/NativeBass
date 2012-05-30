// BASS spectrum analyser example, copyright (c) 2002-2006 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_MUSIC.BASS_MUSIC_RAMP;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_LOOP;





import static jouvieje.bass.examples.util.Device.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import jouvieje.bass.BassInit;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.FileFilters;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
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
public class Spectrum extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(Spectrum.this,
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
	
	private static final int SPECTRUM_WIDTH = 368;	//Display width
	private static final int SPECTRUM_HEIGHT = 127;	//Height (changing requires palette adjustments too)

	private int chan;
	private SpectrumPanel spectrumPanel;

	public static void main(String[] args) {
		new BassExampleFrame(new Spectrum());
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
		
		// initialize BASS
		if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), 0, null, null)) {
			error("Can't initialize device");
			stop();
			return;
		}
		if(!playFile()) {
			// start a file playing
			BASS_Free();
			stop();
			return;
		}
		// setup update timer (40hz)
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

	private boolean playFile() {
		int result = getFileChooser().showOpenDialog(Spectrum.this);
		if(result != JFileChooser.APPROVE_OPTION) return false;

		String file = getFileChooser().getSelectedFile().getPath();
		HSTREAM stream = null; HMUSIC music = null;
		if(  (stream = BASS_StreamCreateFile(false, file, 0, 0, BASS_SAMPLE_LOOP)) == null
		  && (music = BASS_MusicLoad(false, file, 0, 0, BASS_MUSIC_RAMP | BASS_SAMPLE_LOOP, 0)) == null) {
			error("Can't play file");
			return false; // Can't load the file
		}

		chan = (stream != null) ? stream.asInt() : ((music != null) ?  music.asInt() : 0);

		BASS_ChannelPlay(chan, false);
		return true;
	}
	
	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS spectrum example (click to toggle mode)"; }

	/* Graphical stuff */

	private JFileChooser fileChooser = null;
	private Timer timer = new Timer(25, new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			/* update the spectrum display - the interesting bit :) */
			getSpectrumPanel().update(chan);
		}
	});

	public Spectrum() {
		super();
		initialize();
	}
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getSpectrumPanel(), BorderLayout.CENTER);
		this.setSize(getSpectrumPanel().getSize());
	}
	private SpectrumPanel getSpectrumPanel() {
		if(spectrumPanel == null) {
			spectrumPanel = new SpectrumPanel(SPECTRUM_WIDTH, SPECTRUM_HEIGHT);
		}
		return spectrumPanel;
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
