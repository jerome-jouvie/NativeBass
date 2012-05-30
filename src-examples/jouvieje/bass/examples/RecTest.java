// BASS Recording example, copyright (c) 2002-2007 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_ACTIVE.BASS_ACTIVE_STOPPED;
import static jouvieje.bass.defines.BASS_INPUT.BASS_INPUT_OFF;
import static jouvieje.bass.defines.BASS_INPUT.BASS_INPUT_ON;
import static jouvieje.bass.defines.BASS_INPUT_TYPE.BASS_INPUT_TYPE_ANALOG;
import static jouvieje.bass.defines.BASS_INPUT_TYPE.BASS_INPUT_TYPE_AUX;
import static jouvieje.bass.defines.BASS_INPUT_TYPE.BASS_INPUT_TYPE_CD;
import static jouvieje.bass.defines.BASS_INPUT_TYPE.BASS_INPUT_TYPE_DIGITAL;
import static jouvieje.bass.defines.BASS_INPUT_TYPE.BASS_INPUT_TYPE_LINE;
import static jouvieje.bass.defines.BASS_INPUT_TYPE.BASS_INPUT_TYPE_MASK;
import static jouvieje.bass.defines.BASS_INPUT_TYPE.BASS_INPUT_TYPE_MIC;
import static jouvieje.bass.defines.BASS_INPUT_TYPE.BASS_INPUT_TYPE_PHONE;
import static jouvieje.bass.defines.BASS_INPUT_TYPE.BASS_INPUT_TYPE_SPEAKER;
import static jouvieje.bass.defines.BASS_INPUT_TYPE.BASS_INPUT_TYPE_SYNTH;
import static jouvieje.bass.defines.BASS_INPUT_TYPE.BASS_INPUT_TYPE_WAVE;
import static jouvieje.bass.defines.BASS_POS.BASS_POS_BYTE;




import static jouvieje.bass.utils.BufferUtils.newByteBuffer;

import static jouvieje.bass.examples.util.Device.*;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;

import jouvieje.bass.BassInit;
import jouvieje.bass.callbacks.RECORDPROC;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.FileFilters;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.utils.BufferUtils;
import jouvieje.bass.utils.Pointer;
import jouvieje.bass.structures.HRECORD;
import jouvieje.bass.structures.HSTREAM;
import jouvieje.bass.utils.wav.DataChunk;
import jouvieje.bass.utils.wav.FmtChunk;
import jouvieje.bass.utils.wav.RiffChunk;
import jouvieje.bass.utils.wav.WavHeader;

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
public class RecTest extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(RecTest.this,
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
	
	private final int BUFSTEP = 200000;		//memory allocation unit

	private int input = 0;				//current input source
	private ByteBuffer recbuf = null;	//recording buffer
	private int reclen;					//recording length

	private HRECORD rchan = null;	//recording channel
	private HSTREAM chan = null;	//playback channel

	// buffer the recorded data
	RECORDPROC recordingCallback = new RECORDPROC(){
		@Override
		public boolean RECORDPROC(HRECORD handle, ByteBuffer buffer, int length, Pointer user) {
			// increase buffer size if needed
			if((reclen % BUFSTEP) + length >= BUFSTEP) {
				try {
					ByteBuffer realloc = newByteBuffer(((reclen+length)/BUFSTEP+1)*BUFSTEP);
					recbuf.rewind();
					recbuf.limit(reclen);
					realloc.put(recbuf);

					recbuf = realloc;
				} catch(OutOfMemoryError e) {
					rchan = null;
					error("Out of memory!");
					getRecord().setText("Record");
					return false;	//Stop recording
				}
			}
			//Buffer the data
			recbuf.put(buffer);
			reclen += buffer.capacity();
			return true;	//Continue recording
		}
	};

	public static void main(String[] args) {
		new BassExampleFrame(new RecTest());
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
		
		// setup recording device (using default device)
		if(!BASS_RecordInit(-1)) {
			error("Can't initialize recording device");
			stop();
		}
		else {
			// get list of inputs
			String i = null;
			for(int c = 0; (i = BASS_RecordGetInputName(c)) != null; c++) {
				getModel().addElement(i);
				if((BASS_RecordGetInput(c, null) & BASS_INPUT_OFF) != 0) { //this 1 is currently "on"
					input = c;
					updateInputInfo();	//display info
				}
			}
			getInputs().setSelectedIndex(input);

			timer.start();	//timer to update the position display
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

		BASS_RecordFree();
		BASS_Free();
	}

	private void startRecording() {
		if(recbuf != null) {
			// free old recording
			BASS_StreamFree(chan);
			chan = null;
			recbuf = null;
			getPlay().setEnabled(false);
			getSave().setEnabled(false);
			//close output device before recording incase of half-duplex device
			BASS_Free();
		}

		//Allocate initial buffer and make space for WAVE header
		recbuf = newByteBuffer(BUFSTEP);

		//Fill the WAVE header
		int lenbytes = 0;	//Length unknown
		int channels = 2;
		int rate = 44100;
		int bits = 16;
		WavHeader wavHeader = new WavHeader(
				new RiffChunk(new byte[]{'R','I','F','F'},
						FmtChunk.SIZEOF_FMT_CHUNK + RiffChunk.SIZEOF_RIFF_CHUNK+lenbytes),
						new byte[]{'W','A','V','E'});
		FmtChunk fmtChunk = new FmtChunk(
				new RiffChunk(new byte[]{'f','m','t',' '},
						FmtChunk.SIZEOF_FMT_CHUNK - RiffChunk.SIZEOF_RIFF_CHUNK),
						(short)1, (short)channels,
						rate, rate*channels*bits/8,
						(short)(1*channels*bits/8), (short)bits);
		DataChunk dataChunk = new DataChunk(new RiffChunk(new byte[]{'d','a','t','a'}, lenbytes));
		wavHeader.put(recbuf);
		fmtChunk.put(recbuf);
		dataChunk.put(recbuf);
		reclen = recbuf.position();

		//Start recording @ 44100hz 16-bit stereo
		rchan = BASS_RecordStart(44100, 2, 0, recordingCallback, null);
		if(rchan == null) {
			error("Couldn't start recording");
			recbuf = null;
			return;
		}
		getRecord().setText("Stop");
	}

	private void stopRecording() {
		BASS_ChannelStop(rchan.asInt());
		rchan = null;
		getRecord().setText("Record");
		//complete the WAVE header
		recbuf.rewind();
		recbuf.position(4);
		recbuf.putInt(reclen-8);
		recbuf.position(40);
		recbuf.putInt(reclen-44);
		recbuf.rewind();
		//enable "save" button
		getSave().setEnabled(true);
		// setup output device (using default device)
		if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), 0, null, null)) {
			error("Can't initialize output device");
			return;
		}
		//create a stream from the recording
		chan = BASS_StreamCreateFile(true, recbuf, 0, reclen, 0);
		if(chan != null) {
			getPlay().setEnabled(true);
		}
		else {
			BASS_Free();
		}
	}

	// write the recorded data to disk
	private void writeToDisk() {
		int result = getFileChooser().showSaveDialog(RecTest.this);
		if(result == JFileChooser.APPROVE_OPTION) {
			try {
				File file = getFileChooser().getSelectedFile();
				RandomAccessFile os = new RandomAccessFile(file, "rw");
				FileChannel fileChannel = os.getChannel();
				fileChannel.write(recbuf);
				fileChannel.close();
			} catch(Exception e) {
				error("Can't create the file");
			}
		}
	}

	private void updateInputInfo() {
		FloatBuffer level = BufferUtils.newFloatBuffer(1);
		int it = BASS_RecordGetInput(input, level); //get info on the input
		getVolume().setValue((int)(level.get(0) * 100));

		String type = "";
		switch(it & BASS_INPUT_TYPE_MASK) {
			case BASS_INPUT_TYPE_DIGITAL:
				type="digital";
				break;
			case BASS_INPUT_TYPE_LINE:
				type="line-in";
				break;
			case BASS_INPUT_TYPE_MIC:
				type="microphone";
				break;
			case BASS_INPUT_TYPE_SYNTH:
				type="midi synth";
				break;
			case BASS_INPUT_TYPE_CD:
				type="analog cd";
				break;
			case BASS_INPUT_TYPE_PHONE:
				type="telephone";
				break;
			case BASS_INPUT_TYPE_SPEAKER:
				type="pc speaker";
				break;
			case BASS_INPUT_TYPE_WAVE:
				type="wave/pcm";
				break;
			case BASS_INPUT_TYPE_AUX:
				type="aux";
				break;
			case BASS_INPUT_TYPE_ANALOG:
				type="analog";
				break;
			default:
				type="undefined";
		}
		getInputName().setText(type);
	}
	
	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS Recording test"; }
	
			/* Graphical stuff */

	private JComboBox inputs = null;
	private DefaultComboBoxModel model = null;
	private JButton record = null;
	private JButton play = null;
	private JButton save = null;
	private JTextField progress = null;
	private JLabel inputName = null;
	private JSlider volume = null;
	private JFileChooser fileChooser = null;
	private Timer timer = new Timer(200, new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			String text = "";
			if(rchan != null) { //Recording
				text = String.valueOf((int)BASS_ChannelGetPosition(rchan.asInt(), BASS_POS_BYTE));
			}
			else if(chan != null) {
				if(BASS_ChannelIsActive(chan.asInt()) == BASS_ACTIVE_STOPPED) {
					// playing ?
					text = String.valueOf((int)BASS_ChannelGetLength(chan.asInt(), BASS_POS_BYTE));
				}
				else {
					text  = (int)BASS_ChannelGetPosition(chan.asInt(), BASS_POS_BYTE) + " / " + 
							(int)BASS_ChannelGetLength(chan.asInt(), BASS_POS_BYTE);
				}
			}
			getProgress().setText(text);
		}
	});

	public RecTest() {
		super();
		initialize();
	}

	private void initialize() {
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints6.gridy = 2;
		gridBagConstraints6.weightx = 1.0;
		gridBagConstraints6.gridx = 0;
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridx = 0;
		gridBagConstraints5.gridy = 1;
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 1;
		gridBagConstraints4.gridwidth = 3;
		gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints4.gridheight = 2;
		gridBagConstraints4.gridy = 1;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 3;
		gridBagConstraints3.gridy = 0;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 2;
		gridBagConstraints2.gridy = 0;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		this.setSize(new Dimension(340, 75));
		this.setPreferredSize(new Dimension(340, 75));
		this.setLayout(new GridBagLayout());
		this.add(getRecord(), gridBagConstraints);
		this.add(getInputs(), gridBagConstraints1);
		this.add(getPlay(), gridBagConstraints2);
		this.add(getSave(), gridBagConstraints3);
		this.add(getProgress(), gridBagConstraints4);
		this.add(getInputName(), gridBagConstraints5);
		this.add(getVolume(), gridBagConstraints6);
	}

	private JComboBox getInputs() {
		if(inputs == null) {
			inputs = new JComboBox();
			inputs.setModel(getModel());
			inputs.addItemListener(new java.awt.event.ItemListener(){
				@Override
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					input = inputs.getSelectedIndex(); // get the selection
					if(input == -1) {
						return;
					}

					//1st disable all inputs, then...
					for(int i = 0; BASS_RecordSetInput(i, BASS_INPUT_OFF, -1);i++) {};
					//enable the selected
					BASS_RecordSetInput(input, BASS_INPUT_ON, -1);
					updateInputInfo();		// update info
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

	private JButton getRecord() {
		if(record == null) {
			record = new JButton();
			record.setText("Record");
			record.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(rchan == null) {
						startRecording();
					}
					else {
						stopRecording();
					}
				}
			});
		}
		return record;
	}

	private JButton getPlay() {
		if(play == null) {
			play = new JButton();
			play.setText("Play");
			play.setEnabled(false);
			play.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					BASS_ChannelPlay(chan.asInt(), true);	//play the recorded data
				}
			});
		}
		return play;
	}

	private JButton getSave() {
		if(save == null) {
			save = new JButton();
			save.setText("Save");
			save.setEnabled(false);
			save.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					writeToDisk();
				}
			});
		}
		return save;
	}

	private JTextField getProgress() {
		if(progress == null) {
			progress = new JTextField();
			progress.setEditable(false);
			progress.setHorizontalAlignment(JTextField.CENTER);
		}
		return progress;
	}

	public JLabel getInputName() {
		if(inputName == null) {
			inputName = new JLabel();
			inputName.setText("Undefined");
		}
		return inputName;
	}

	private JSlider getVolume() {
		if(volume == null) {
			volume = new JSlider();
			volume.setMinimum(0);
			volume.setMaximum(100);
			volume.addChangeListener(new javax.swing.event.ChangeListener(){
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					float level = volume.getValue() / 100.0f;
					if(!BASS_RecordSetInput(input, 0, level)) {
						BASS_RecordSetInput(-1, 0, level);
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
}  //  @jve:decl-index=0:visual-constraint="10,10"
