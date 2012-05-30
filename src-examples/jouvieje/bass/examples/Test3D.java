// BASS 3D Test, copyright (c) 1999-2006 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_ACTIVE.BASS_ACTIVE_PLAYING;
import static jouvieje.bass.defines.BASS_DEVICE.BASS_DEVICE_3D;
import static jouvieje.bass.defines.BASS_MUSIC.BASS_MUSIC_RAMP;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_3D;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_LOOP;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_MONO;





import static jouvieje.bass.examples.util.Device.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import jouvieje.bass.BassInit;
import jouvieje.bass.enumerations.EAX_ENVIRONMENT;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.FileFilters;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.structures.BASS_3DVECTOR;
import jouvieje.bass.structures.HMUSIC;
import jouvieje.bass.structures.HSAMPLE;

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
public class Test3D extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(Test3D.this,
				"<html><body>" + text + "<BR>(error code: " + BASS_ErrorGetCode() + ")</body></html>");
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
		new BassExampleFrame(new Test3D());
	}
	
	private boolean init   = false;
	private boolean deinit = false;
	
	class Channel {
		String name;
		//The channel
		HSAMPLE sample;
		HMUSIC  music;
		int channel;
		//Position,velocity
		BASS_3DVECTOR pos = BASS_3DVECTOR.create(0, 0, 0);		//Note: BASS_3DVECTOR.create() does not initialize the x,y & z values
		BASS_3DVECTOR vel = BASS_3DVECTOR.create(0, 0, 0);

		public void release() {
			pos.release();
			vel.release();
		}
		@Override
		public String toString() { return name; }
	}
	int currentChannel = -1;		//number of channels, current channel

	private final static int TIMERPERIOD = 50;	//timer period (ms)
	private final static int MAXDIST     = 50;	//maximum distance of the channels (m)
	
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
		
		/* If there's more than 1 device, let the user choose */
		int device = 1;
//		if(BASS_GetDeviceDescription(2) != null)
		{
			OutputDevice outputDevice = new OutputDevice(BASS_DEVICE_3D);
			outputDevice.setVisible(true);
			device = outputDevice.getSelectedDevice();
		}

		/* Initialize the output device with 3D support */
		if(!BASS_Init(forceNoSoundDevice(device), forceFrequency(44100), BASS_DEVICE_3D, null, null)) {
			error("Can't initialize output device");
			return;
		}

		/* Use meters as distance unit, real world rolloff, real doppler effect */
		BASS_Set3DFactors(1,1,1);
		/* Turn EAX off (volume=0), if error then EAX is not supported */
//		if(BASS_SetEAXParameters(-1, 0, -1, -1)) {
//			getEaxP().setEnabled(true);
//		}
		
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
		for(int i = 0; i < channelListModel.size(); i++) {
			Channel c = (Channel)channelListModel.getElementAt(i);
			c.release();
		}
		BASS_Free();
	}
	
	private Channel getCurrentChannel() {
		if(currentChannel != -1) {
			return (Channel)channelListModel.getElementAt(currentChannel);
		}
		return null;
	}
	
	/* Update the button states */
	private void updateButtons() {
		Channel c = getCurrentChannel();
		boolean enabled = c != null;
		
		//Enable/Disable components
		getRemoveB().setEnabled(enabled);
		getPlayB().setEnabled(enabled);
		getStopB().setEnabled(enabled);
		xL.setEnabled(enabled);
		getXCoord().setEnabled(enabled);
		yL.setEnabled(enabled);
		getYCoord().setEnabled(enabled);
		getReset().setEnabled(enabled);
		
//		if(getEaxP().isEnabled()) {
//			getEnvironments().setEnabled(enabled);
//		}
		
		if(c != null) {
			//Update GUI
			getXCoord().setText(String.valueOf((int)c.vel.getX()));
			getYCoord().setText(String.valueOf((int)c.vel.getZ()));
		}
	}
	
	private Timer timer = new Timer(TIMERPERIOD, new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			final int SIZE = 8;
			final int HALF_SIZE = SIZE/2;
			
			//Graphic
			Graphics g = getDisplay().getGraphics(); if(g == null) return;
			Dimension gSize = getDisplay().getSize();
			
			/* clear the display */
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, gSize.width, gSize.height);

			/* Draw the listener */
			int cx = gSize.width/2;
			int cy = gSize.height/2;
			g.setColor(Color.GRAY);
			g.fillOval(cx-HALF_SIZE, cy-HALF_SIZE, SIZE, SIZE);
			g.setColor(Color.BLACK);
			g.drawOval(cx-HALF_SIZE, cy-HALF_SIZE, SIZE, SIZE);

			for(int i = 0; i < channelListModel.size(); i++) {
				Channel c = (Channel)channelListModel.getElementAt(i);
				
				/* If the channel's playing then update it's position */
				if(BASS_ChannelIsActive(c.channel) == BASS_ACTIVE_PLAYING) {
					/* Check if channel has reached the max distance */
					if(c.pos.getZ() >= MAXDIST || c.pos.getZ() <= -MAXDIST)
						c.vel.setZ(-c.vel.getZ());
					if(c.pos.getX() >= MAXDIST || c.pos.getX() <= -MAXDIST)
						c.vel.setX(-c.vel.getX());
					/* Update channel position */
					c.pos.setZ(c.pos.getZ() + c.vel.getZ()*TIMERPERIOD/1000);
					c.pos.setX(c.pos.getX() + c.vel.getX()*TIMERPERIOD/1000);
					BASS_ChannelSet3DPosition(c.channel, c.pos, null, c.vel);
				}
				/* Draw the channel position indicator */
				int x = cx+(int)((cx-10)*c.pos.getX()/MAXDIST);
				int y = cy-(int)((cy-10)*c.pos.getZ()/MAXDIST);
				g.setColor(currentChannel == i ? Color.GRAY : Color.WHITE);
				g.fillOval(x-HALF_SIZE, y-HALF_SIZE, SIZE, SIZE);
				g.setColor(Color.BLACK);
				g.drawOval(x-HALF_SIZE, y-HALF_SIZE, SIZE, SIZE);
			}
			
			/* Apply the 3D changes */
			BASS_Apply3D();
		}
	});

	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS - 3D Test"; }
	
			/* Graphical stuff */
	
	private JFileChooser fileChooser = null;
	private JPanel channelsP = null;
	private JList channelL = null;
	private DefaultListModel channelListModel = new DefaultListModel();
	private JPanel displayP = null;
	private JPanel rolloffP = null;
	private JPanel dopplerP = null;
	private JPanel movementP = null;
//	private JPanel eaxP = null;
	private JPanel display = null;
//	private JComboBox environments = null;
	private JSlider doppler = null;
	private JSlider rolloff = null;
	private JLabel xL = null;
	private JTextField xCoord = null;
	private JLabel yL = null;
	private JTextField yCoord = null;
	private JButton reset = null;
	private JScrollPane channelLSP = null;
	private JButton addB = null;
	private JButton removeB = null;
	private JButton playB = null;
	private JButton stopB = null;
	
	public Test3D() {
		super();
		initialize();
	}

	private void initialize() {
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridx = 0;
		gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints5.weightx = 1.0D;
		gridBagConstraints5.gridy = 2;
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 0;
		gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints4.weightx = 1.0D;
		gridBagConstraints4.gridy = 1;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 1;
		gridBagConstraints3.fill = GridBagConstraints.BOTH;
		gridBagConstraints3.gridy = 2;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.gridy = 1;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.gridy = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.weighty = 1.0D;
		gridBagConstraints.weightx = 1.0D;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		this.setSize(new Dimension(378, 318));
		this.setPreferredSize(new Dimension(378, 318));
		this.setLayout(new GridBagLayout());
		this.add(getChannelsP(), gridBagConstraints);
		this.add(getDisplayP(), gridBagConstraints1);
		this.add(getRolloffP(), gridBagConstraints2);
		this.add(getDopplerP(), gridBagConstraints3);
		this.add(getMovementP(), gridBagConstraints4);
//		this.add(getEaxP(), gridBagConstraints5);
	}

	private JFileChooser getFileChooser() {
		if(fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.resetChoosableFileFilters();
			fileChooser.addChoosableFileFilter(FileFilters.allFiles);
			fileChooser.addChoosableFileFilter(new FileFilter(){
				private final static String extensions = "wav;aif;mo3;xm;mod;s3m;it;mtm;umx";

				@Override
				public String getDescription() {
					return "wav/aif/mo3/xm/mod/s3m/it/mtm/umx";
				}
				@Override
				public boolean accept(File f) {
					return f.isDirectory() || extensions.contains(extensionOf(f.getName()));
				}
				private String extensionOf(String file) {
					int index = file.lastIndexOf(".");
					if(index != -1) return file.substring(index+1).toLowerCase();
					return "";
				}
			});
			fileChooser.setDialogTitle("Open a music");
		}
		return fileChooser;
	}

	private JPanel getChannelsP() {
		if(channelsP == null) {
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 1;
			gridBagConstraints16.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints16.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints16.gridy = 2;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.gridy = 2;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.insets = new Insets(5, 0, 5, 5);
			gridBagConstraints14.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints14.gridy = 1;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints13.ipadx = 20;
			gridBagConstraints13.gridy = 1;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.fill = GridBagConstraints.BOTH;
			gridBagConstraints12.gridy = 0;
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.weighty = 1.0;
			gridBagConstraints12.gridwidth = 2;
			gridBagConstraints12.gridx = 0;
			channelsP = new JPanel();
			channelsP.setLayout(new GridBagLayout());
			channelsP.setBorder(BorderFactory.createTitledBorder(null, "Channels (sample/music)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			channelsP.add(getChannelLSP(), gridBagConstraints12);
			channelsP.add(getAddB(), gridBagConstraints13);
			channelsP.add(getRemoveB(), gridBagConstraints14);
			channelsP.add(getPlayB(), gridBagConstraints15);
			channelsP.add(getStopB(), gridBagConstraints16);
		}
		return channelsP;
	}

	private JPanel getDisplayP() {
		if(displayP == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.insets = new Insets(0, 10, 10, 10);
			gridBagConstraints11.gridy = 0;
			displayP = new JPanel();
			displayP.setLayout(new GridBagLayout());
			displayP.setBorder(BorderFactory.createTitledBorder(null, " ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			displayP.add(getDisplay(), gridBagConstraints11);
		}
		return displayP;
	}

	private JPanel getRolloffP() {
		if(rolloffP == null) {
			rolloffP = new JPanel();
			rolloffP.setLayout(new BorderLayout());
			rolloffP.setBorder(BorderFactory.createTitledBorder(null, "Rolloff factor", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			rolloffP.add(getRolloff(), BorderLayout.CENTER);
		}
		return rolloffP;
	}

	private JPanel getDopplerP() {
		if(dopplerP == null) {
			dopplerP = new JPanel();
			dopplerP.setLayout(new BorderLayout());
			dopplerP.setBorder(BorderFactory.createTitledBorder(null, "Doppler factor", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			dopplerP.add(getDoppler(), BorderLayout.CENTER);
		}
		return dopplerP;
	}

	private JPanel getMovementP() {
		if(movementP == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 4;
			gridBagConstraints10.insets = new Insets(0, 0, 0, 5);
			gridBagConstraints10.gridy = 0;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 2;
			gridBagConstraints9.gridy = 0;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridy = 0;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.weightx = 1.0D;
			gridBagConstraints7.gridx = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.gridx = 3;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.weightx = 1.0;
			yL = new JLabel();
			yL.setText("y: ");
			yL.setEnabled(false);
			xL = new JLabel();
			xL.setText("x: ");
			xL.setEnabled(false);
			movementP = new JPanel();
			movementP.setLayout(new GridBagLayout());
			movementP.setBorder(BorderFactory.createTitledBorder(null, "Movement", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			movementP.add(xL, gridBagConstraints8);
			movementP.add(getXCoord(), gridBagConstraints7);
			movementP.add(yL, gridBagConstraints9);
			movementP.add(getYCoord(), gridBagConstraints6);
			movementP.add(getReset(), gridBagConstraints10);
		}
		return movementP;
	}

//	private JPanel getEaxP() {
//		if(eaxP == null) {
//			eaxP = new JPanel();
//			eaxP.setEnabled(false);
//			eaxP.setLayout(new BorderLayout());
//			eaxP.setBorder(BorderFactory.createTitledBorder(null, "EAX environment", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
//			eaxP.add(getEnvironments(), BorderLayout.CENTER);
//		}
//		return eaxP;
//	}

	private JPanel getDisplay() {
		if(display == null) {
			display = new JPanel();
			display.setBackground(Color.WHITE);
			display.setLayout(new GridBagLayout());
			display.setSize(new Dimension(160, 160));
			display.setPreferredSize(new Dimension(160, 160));
			display.setMinimumSize(new Dimension(160, 160));
			display.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			display.setMaximumSize(new Dimension(160, 160));
		}
		return display;
	}

//	private JComboBox getEnvironments() {
//		if(environments == null) {
//			DefaultComboBoxModel model = new DefaultComboBoxModel();
//			Iterator<EAX_ENVIRONMENT> i = EAX_ENVIRONMENT.iterator();
//			 model.addElement("Off");
//			for(; i.hasNext();) {
//				model.addElement(i.next());
//			}
//			
//			environments = new JComboBox();
//			environments.setModel(model);
//			environments.setEnabled(false);
//			environments.addItemListener(new java.awt.event.ItemListener(){
//				@Override
//				public void itemStateChanged(java.awt.event.ItemEvent e) {
//					Object item = environments.getSelectedItem();
//					if(item instanceof EAX_ENVIRONMENT) {
//						BASS_SetEAXParameters(((EAX_ENVIRONMENT)item).asInt(), -1, -1, -1);
//					}
//					else {
//						BASS_SetEAXParameters(-1, 0, -1, -1);		//off (volume=0)
//					}
//				}
//			});
//		}
//		return environments;
//	}

	private JSlider getDoppler() {
		if(doppler == null) {
			doppler = new JSlider(0, 20, 10);
			doppler.addChangeListener(new javax.swing.event.ChangeListener(){
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					if(doppler.getValueIsAdjusting()) {
						//change the doppler factor
						int pos = doppler.getValue();
						BASS_Set3DFactors(-1, -1, (float)Math.pow(2, (pos-10)/5.0));
					}
				}
			});
		}
		return doppler;
	}

	private JSlider getRolloff() {
		if(rolloff == null) {
			rolloff = new JSlider(0, 20, 10);
			rolloff.addChangeListener(new javax.swing.event.ChangeListener(){
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					if(rolloff.getValueIsAdjusting()) {
						//change the rolloff factor
						int pos = rolloff.getValue();
						BASS_Set3DFactors(-1, (float)Math.pow(2, (pos-10)/5.0), -1);
					}
				}
			});
		}
		return rolloff;
	}

	private JTextField getXCoord() {
		if(xCoord == null) {
			xCoord = new JTextField();
			xCoord.setEnabled(false);
			xCoord.addKeyListener(new java.awt.event.KeyAdapter(){
				@Override
				public void keyReleased(java.awt.event.KeyEvent e) {
					Channel c = getCurrentChannel();
					if(c == null) {
						return;
					}
					
					//TODO Small troubles when editing the values here
					
					//X velocity
					try {
						int x = (int)Float.parseFloat(xCoord.getText());
						c.vel.setX(x);
					} catch(NumberFormatException e1){ }
				}
			});
		}
		return xCoord;
	}

	private JTextField getYCoord() {
		if(yCoord == null) {
			yCoord = new JTextField();
			yCoord.setEnabled(false);
			yCoord.addKeyListener(new java.awt.event.KeyAdapter(){
				@Override
				public void keyTyped(java.awt.event.KeyEvent e) {
					Channel c = getCurrentChannel();
					if(c == null) {
						return;
					}
					
					//TODO Small troubles when editing the values here
					
					//Z velocity
					try {
						int z = (int)Float.parseFloat(yCoord.getText());
						c.vel.setZ(z);
					} catch(NumberFormatException e1){ }
				}
			});
		}
		return yCoord;
	}

	private JButton getReset() {
		if(reset == null) {
			reset = new JButton();
			reset.setText("Reset");
			reset.setEnabled(false);
			reset.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Channel c = getCurrentChannel();
					if(c == null) {
						return;
					}
					
					// reset the position and velocity to 0
					c.pos.setX(0); c.pos.setY(0); c.pos.setZ(0);
					c.vel.setX(0); c.vel.setY(0); c.vel.setZ(0);
					updateButtons();
				}
			});
		}
		return reset;
	}

	private JScrollPane getChannelLSP() {
		if(channelLSP == null) {
			channelLSP = new JScrollPane();
			channelLSP.setViewportView(getChannelL());
		}
		return channelLSP;
	}

	private JList getChannelL() {
		if(channelL == null) {
			channelL = new JList(channelListModel);
			channelL.addListSelectionListener(new javax.swing.event.ListSelectionListener(){
				@Override
				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
					//change the selected channel
					currentChannel = channelL.getSelectedIndex();
					updateButtons();
				}
			});
		}
		return channelL;
	}

	private JButton getAddB() {
		if(addB == null) {
			addB = new JButton();
			addB.setText("Add");
			addB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// add a channel
					if(getFileChooser().showOpenDialog(Test3D.this) == JFileChooser.APPROVE_OPTION) {
						File file = getFileChooser().getSelectedFile();
						/* Load a music or sample from "file" */
						HMUSIC music = null; HSAMPLE sample = null;
						if((music = BASS_MusicLoad(false, file.getPath(), 0, 0, BASS_MUSIC_RAMP | BASS_SAMPLE_LOOP| BASS_SAMPLE_3D, 0)) != null 
							|| (sample = BASS_SampleLoad(false, file.getPath(), 0, 0, 1, BASS_SAMPLE_LOOP| BASS_SAMPLE_3D| BASS_SAMPLE_MONO))  != null) {
							Channel c = new Channel();
							c.name = file.getName();
							c.music = music;
							c.sample = sample;
							c.channel = (music != null) ? music.asInt() : sample.asInt();
							
							BASS_SampleGetChannel(c.sample, false); // initialize sample channel
							channelListModel.addElement(c);
						}
						else {
							error("Can't load file (note samples must be mono)");
						}
					}
				}
			});
		}
		return addB;
	}

	private JButton getRemoveB() {
		if(removeB == null) {
			removeB = new JButton();
			removeB.setText("Remove");
			removeB.setEnabled(false);
			removeB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Channel c = getCurrentChannel();
					if(c == null) {
						return;
					}
					
					BASS_SampleFree(c.sample);
					BASS_MusicFree(c.music);
					c.release();		//Free memory allocated with BASS_VECTOR3D.create()
					channelListModel.removeElementAt(currentChannel);
					
					currentChannel = -1;
					updateButtons();
				}
			});
		}
		return removeB;
	}

	private JButton getPlayB() {
		if(playB == null) {
			playB = new JButton();
			playB.setText("Play");
			playB.setEnabled(false);
			playB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Channel c = getCurrentChannel();
					if(c == null) {
						return;
					}
					BASS_ChannelPlay(c.channel, false);
				}
			});
		}
		return playB;
	}

	private JButton getStopB() {
		if(stopB == null) {
			stopB = new JButton();
			stopB.setText("Stop");
			stopB.setEnabled(false);
			stopB.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Channel c = getCurrentChannel();
					if(c == null) {
						return;
					}
					BASS_ChannelPause(c.channel);
				}
			});
		}
		return stopB;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
