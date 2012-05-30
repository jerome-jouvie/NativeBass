package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_CONFIG.BASS_CONFIG_NET_PLAYLIST;
import static jouvieje.bass.defines.BASS_CONFIG.BASS_CONFIG_NET_PREBUF;
import static jouvieje.bass.defines.BASS_CONFIG_NET.BASS_CONFIG_NET_PROXY;
import static jouvieje.bass.defines.BASS_FILEPOS.BASS_FILEPOS_BUFFER;
import static jouvieje.bass.defines.BASS_FILEPOS.BASS_FILEPOS_CONNECTED;
import static jouvieje.bass.defines.BASS_FILEPOS.BASS_FILEPOS_END;
import static jouvieje.bass.defines.BASS_STREAM.BASS_STREAM_AUTOFREE;
import static jouvieje.bass.defines.BASS_STREAM.BASS_STREAM_BLOCK;
import static jouvieje.bass.defines.BASS_STREAM.BASS_STREAM_STATUS;
import static jouvieje.bass.defines.BASS_SYNC.BASS_SYNC_END;
import static jouvieje.bass.defines.BASS_SYNC.BASS_SYNC_META;
import static jouvieje.bass.defines.BASS_SYNC.BASS_SYNC_OGG_CHANGE;
import static jouvieje.bass.defines.BASS_TAG.BASS_TAG_HTTP;
import static jouvieje.bass.defines.BASS_TAG.BASS_TAG_ICY;
import static jouvieje.bass.defines.BASS_TAG.BASS_TAG_META;
import static jouvieje.bass.defines.BASS_TAG.BASS_TAG_OGG;





import static jouvieje.bass.examples.util.Device.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.nio.ByteBuffer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import jouvieje.bass.BassInit;
import jouvieje.bass.callbacks.DOWNLOADPROC;
import jouvieje.bass.callbacks.SYNCPROC;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.utils.BufferUtils;
import jouvieje.bass.utils.Pointer;
import jouvieje.bass.structures.HSTREAM;
import jouvieje.bass.structures.HSYNC;

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
public class NetRadio extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(NetRadio.this,
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
	
	private HSTREAM chan = null;

	private String userUrl = "http://www.sky.fm/mp3/altrock.pls";
	private String[] urls = new String[] {	//stream URLs
		"http://www.radioparadise.com/musiclinks/rp_128-9.m3u",
		"http://www.radioparadise.com/musiclinks/rp_32.m3u",
		"http://www.sky.fm/mp3/classical.pls",
		"http://www.sky.fm/mp3/classical_low.pls",
		"http://www.sky.fm/mp3/altrock.pls",
		"http://www.sky.fm/mp3/altrock_low.pls",
		"http://bassdrive.com/v2/streams/BassDrive.m3u",
		"http://bassdrive.com/v2/streams/BassDrive3.m3u",
		"http://somafm.com/secretagent.pls",
		"http://somafm.com/secretagent24.pls"
	};
	
	private Timer timer = new Timer(50, new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			//Monitor prebuffering progress
			int progress = (int)(BASS_StreamGetFilePosition(chan, BASS_FILEPOS_BUFFER)
				         * 100 / BASS_StreamGetFilePosition(chan, BASS_FILEPOS_END));	// percentage of buffer filled
			
			if(progress > 75 || BASS_StreamGetFilePosition(chan, BASS_FILEPOS_CONNECTED) != 0) { // over 75% full (or end of download)
				timer.stop(); //Finished prebuffering, stop monitoring
				{														//Get the broadcast name and bitrate
					Pointer tags = BASS_ChannelGetTags(chan.asInt(), BASS_TAG_ICY);
					if(tags == null) {
						tags = BASS_ChannelGetTags(chan.asInt(), BASS_TAG_HTTP);		// no ICY tags, try HTTP
					}
					if(tags != null) {
						int length;
						while((length = tags.asString().length()) > 0) {
							final String ICY_NAME = "icy-name:";
							final String ICY_URL  = "icy-br:";

							String tag = tags.asString();
							if(tag.toLowerCase().startsWith(ICY_NAME)) {
								getLabel2().setText(tag);
							}
							if(tag.toLowerCase().startsWith(ICY_URL)) {
								getLabel3().setText(tag);
							}
							tags = tags.asPointer(length+1);
						}
					}
					else {
						getLabel3().setText("");
					}
				}
				//Get the stream title and set sync for subsequent titles
				DoMeta();
				BASS_ChannelSetSync(chan.asInt(), BASS_SYNC_META, 0, metaSync, null);
				BASS_ChannelSetSync(chan.asInt(), BASS_SYNC_OGG_CHANGE, 0, metaSync, null);
				//Set sync for end of stream
				BASS_ChannelSetSync(chan.asInt(), BASS_SYNC_END, 0, endSync, null);
				//Play it!
				BASS_ChannelPlay(chan.asInt(), false);
			}
			else {
				getLabel2().setText("buffering... "+progress+"%");
			}
		}
	});
	
	private Thread thread = null;
	
	//Update stream title from metadata
	private void DoMeta() {
		Pointer metaBuff = BASS_ChannelGetTags(chan.asInt(), BASS_TAG_META);
		if(metaBuff != null) {
			String meta = metaBuff.asString();
			
			// got Shoutcast metadata
			final String STREAM_TITLE = "StreamTitle='";
			int index = meta.indexOf(STREAM_TITLE);
			if(index != -1) {
				String p = meta.substring(index+STREAM_TITLE.length());
				if(p.contains(";")) {
					p = p.substring(0, p.indexOf(";"));
				}
				getLabel1().setText(p);
			}
		}
		else
		{
			metaBuff = BASS_ChannelGetTags(chan.asInt(), BASS_TAG_OGG);
			if(metaBuff != null) {
				String artist=null, title=null;
				
				// got Icecast/OGG tags
				int length;
				while((length = metaBuff.asString().length()) > 0) {
					String s = metaBuff.asString();
					if(s.startsWith("artist=")) {
						// found the artist
						artist = s.substring(7);
					}
					if(s.startsWith("title=")) {
						// found the title
						title = s.substring(6);
					}
					metaBuff = metaBuff.asPointer(length+1);
				}
				
				if(artist != null && title != null) {
					getLabel1().setText(artist+" - "+title);
				}
				else if(title != null) {
					getLabel1().setText(title);
				}
			}
		}
	}

	private SYNCPROC metaSync = new SYNCPROC(){
		@Override
		public void SYNCPROC(HSYNC handle, int channel, int data, Pointer user) {
//			DoMeta(data);	//FIXME
		}
	};
	private SYNCPROC endSync = new SYNCPROC(){
		@Override
		public void SYNCPROC(HSYNC handle, int channel, int data, Pointer user) {
			getLabel1().setText("");
			getLabel2().setText("not playing");
			getLabel3().setText("");
		}
	};

	private DOWNLOADPROC statusProc = new DOWNLOADPROC(){
		@Override
		public void DOWNLOADPROC(ByteBuffer buffer, int length, Pointer user) {
			if(buffer != null && length == 0) {
				getLabel3().setText(BufferUtils.toString(buffer));	// display connection status
			}
		}
	};
	
	private void openURL(final String url) {
		if(thread != null) {
			return;
		}
		thread = new Thread(){
			@Override
			public synchronized void start() {
				timer.stop();				//Stop prebuffer monitoring
				BASS_StreamFree(chan);	//Close old stream
				getLabel1().setText("");
				getLabel2().setText("connecting...");
				getLabel3().setText("");
				chan = BASS_StreamCreateURL(url, 0, BASS_STREAM_BLOCK | BASS_STREAM_STATUS | BASS_STREAM_AUTOFREE, statusProc, null);
				if(chan == null) {
					getLabel2().setText("not playing");
					error("Can't play the stream");
				}
				else {
					timer.start();			//Start prebuffer monitoring
				}
				
				thread = null;
			}
		};
		thread.start();
	}

	public static void main(String[] args) {
		new BassExampleFrame(new NetRadio());
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
		
		// setup output device
		if (!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), 0, null, null)) {
			error("Can't initialize device");
			stop();
		}
		BASS_SetConfig(BASS_CONFIG_NET_PLAYLIST, 1);	//Enable playlist processing
		BASS_SetConfig(BASS_CONFIG_NET_PREBUF, 0);		//Minimize automatic pre-buffering, so we can do it (and display it) instead
		BASS_SetConfigPtr(BASS_CONFIG_NET_PROXY, null);
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
	public String getTitle() { return "BASS internet radio tuner"; }
	
			/* Graphical stuff */
	
	private JPanel presetsP = null;
	private JPanel playingP = null;
	private JPanel proxyP = null;
	private JTextField serverTF = null;
	private JCheckBox directConnectionCB = null;
	private JLabel proxyLabel = null;
	private JLabel broadbandLabel = null;
	private JLabel modemLabel = null;
	private JButton presetB1 = null;
	private JButton presetB2 = null;
	private JButton presetB3 = null;
	private JButton presetB4 = null;
	private JButton presetB5 = null;
	private JButton presetM1 = null;
	private JButton presetM2 = null;
	private JButton presetM3 = null;
	private JButton presetM4 = null;
	private JButton presetM5 = null;
	private JLabel label1 = null;
	private JLabel label2 = null;
	private JLabel label3 = null;
	private JTextField radioPlaylistTF = null;
	private JButton presetUserB = null;
	private JLabel userRadioLabel = null;
	
	public NetRadio() {
		super();
		initialize();
	}

	private void initialize() {
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.weightx = 1.0D;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.gridx = 0;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.weightx = 1.0D;
		gridBagConstraints1.weighty = 1.0D;
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.gridy = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0D;
		this.setSize(new Dimension(350, 275));
		this.setPreferredSize(new Dimension(350, 275));
		this.setLayout(new GridBagLayout());
		this.add(getPresetsP(), gridBagConstraints);
		this.add(getPlayingP(), gridBagConstraints1);
		this.add(getProxyP(), gridBagConstraints2);
	}

	private JPanel getPresetsP() {
		if(presetsP == null) {
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 0;
			gridBagConstraints20.anchor = GridBagConstraints.WEST;
			gridBagConstraints20.gridy = 2;
			userRadioLabel = new JLabel();
			userRadioLabel.setText("User");
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 5;
			gridBagConstraints19.gridy = 2;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints18.gridy = 2;
			gridBagConstraints18.weightx = 1.0;
			gridBagConstraints18.gridwidth = 4;
			gridBagConstraints18.insets = new Insets(0, 5, 0, 5);
			gridBagConstraints18.gridx = 1;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 5;
			gridBagConstraints17.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints17.gridy = 1;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 4;
			gridBagConstraints16.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints16.gridy = 1;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 3;
			gridBagConstraints15.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints15.gridy = 1;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 2;
			gridBagConstraints14.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints14.gridy = 1;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints13.gridy = 1;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 5;
			gridBagConstraints12.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints12.gridy = 0;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 4;
			gridBagConstraints11.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints11.gridy = 0;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 3;
			gridBagConstraints10.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints10.gridy = 0;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 2;
			gridBagConstraints9.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints9.gridy = 0;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints8.gridy = 0;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.gridy = 1;
			modemLabel = new JLabel();
			modemLabel.setText("Modem");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.gridy = 0;
			broadbandLabel = new JLabel();
			broadbandLabel.setText("Broadband");
			presetsP = new JPanel();
			presetsP.setLayout(new GridBagLayout());
			presetsP.setBorder(BorderFactory.createTitledBorder(null, "Presets", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			presetsP.add(broadbandLabel, gridBagConstraints6);
			presetsP.add(modemLabel, gridBagConstraints7);
			presetsP.add(getPresetB1(), gridBagConstraints8);
			presetsP.add(getPresetB2(), gridBagConstraints9);
			presetsP.add(getPresetB3(), gridBagConstraints10);
			presetsP.add(getPresetB4(), gridBagConstraints11);
			presetsP.add(getPresetB5(), gridBagConstraints12);
			presetsP.add(getPresetM1(), gridBagConstraints13);
			presetsP.add(getPresetM2(), gridBagConstraints14);
			presetsP.add(getPresetM3(), gridBagConstraints15);
			presetsP.add(getPresetM4(), gridBagConstraints16);
			presetsP.add(getPresetM5(), gridBagConstraints17);
			presetsP.add(getRadioPlaylistTF(), gridBagConstraints18);
			presetsP.add(getPresetUserB(), gridBagConstraints19);
			presetsP.add(userRadioLabel, gridBagConstraints20);
		}
		return presetsP;
	}

	private JPanel getPlayingP() {
		if(playingP == null) {
			playingP = new JPanel();
			playingP.setLayout(new BorderLayout());
			playingP.setBorder(BorderFactory.createTitledBorder(null, "Current playing", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			playingP.add(getLabel1(), BorderLayout.NORTH);
			playingP.add(getLabel2(), BorderLayout.CENTER);
			playingP.add(getLabel3(), BorderLayout.SOUTH);
		}
		return playingP;
	}

	private JLabel getLabel1() {
		if(label1 == null) {
			label1 = new JLabel();
			label1.setText("");
			label1.setHorizontalTextPosition(SwingConstants.CENTER);
			label1.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return label1;
	}

	private JLabel getLabel2() {
		if(label2 == null) {
			label2 = new JLabel();
			label2.setText("not playing");
			label2.setHorizontalTextPosition(SwingConstants.CENTER);
			label2.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return label2;
	}

	private JLabel getLabel3() {
		if(label3 == null) {
			label3 = new JLabel();
			label3.setText("");
			label3.setHorizontalTextPosition(SwingConstants.CENTER);
			label3.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return label3;
	}

	private JPanel getProxyP() {
		if(proxyP == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.gridy = 1;
			proxyLabel = new JLabel();
			proxyLabel.setText("[user:pass@]server:port");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.gridwidth = 2;
			gridBagConstraints3.gridx = 0;
			proxyP = new JPanel();
			proxyP.setLayout(new GridBagLayout());
			proxyP.setBorder(BorderFactory.createTitledBorder(null, "Proxy settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			proxyP.add(getServerTF(), gridBagConstraints3);
			proxyP.add(getDirectConnectionCB(), gridBagConstraints4);
			proxyP.add(proxyLabel, gridBagConstraints5);
		}
		return proxyP;
	}

	private JTextField getServerTF() {
		if(serverTF == null) {
			serverTF = new JTextField();
		}
		return serverTF;
	}

	private JCheckBox getDirectConnectionCB() {
		if(directConnectionCB == null) {
			directConnectionCB = new JCheckBox();
			directConnectionCB.setText("Use direct connection");
			directConnectionCB.setSelected(true);
			directConnectionCB.addItemListener(new ItemListener(){
				private ByteBuffer proxyPtr;

				@Override
				public void itemStateChanged(ItemEvent e) {
					proxyPtr = null;
					if(directConnectionCB.isSelected()) {
						BASS_SetConfigPtr(BASS_CONFIG_NET_PROXY, null); // disable proxy
					}
					else {
						byte[] bytes = getServerTF().getText().getBytes();
						proxyPtr = BufferUtils.newByteBuffer(bytes.length+1);
						proxyPtr.put(bytes);
						BufferUtils.putNullTerminal(proxyPtr);
						proxyPtr.rewind();
						
						BASS_SetConfigPtr(BASS_CONFIG_NET_PROXY, BufferUtils.asPointer(proxyPtr));	// enable proxy		//FIXME
					}
				}
			});
		}
		return directConnectionCB;
	}
	private JButton getPresetB1() {
		if(presetB1 == null) {
			presetB1 = new JButton();
			presetB1.setText("1");
			presetB1.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					openURL(urls[0]);
				}
			});
		}
		return presetB1;
	}
	private JButton getPresetB2() {
		if(presetB2 == null) {
			presetB2 = new JButton();
			presetB2.setText("2");
			presetB2.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					openURL(urls[2]);
				}
			});
		}
		return presetB2;
	}
	private JButton getPresetB3() {
		if(presetB3 == null) {
			presetB3 = new JButton();
			presetB3.setText("3");
			presetB3.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					openURL(urls[4]);
				}
			});
		}
		return presetB3;
	}
	private JButton getPresetB4() {
		if(presetB4 == null) {
			presetB4 = new JButton();
			presetB4.setText("4");
			presetB4.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					openURL(urls[6]);
				}
			});
		}
		return presetB4;
	}
	private JButton getPresetB5() {
		if(presetB5 == null) {
			presetB5 = new JButton();
			presetB5.setText("5");
			presetB5.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					openURL(urls[8]);
				}
			});
		}
		return presetB5;
	}
	private JButton getPresetM1() {
		if(presetM1 == null) {
			presetM1 = new JButton();
			presetM1.setText("1");
			presetM1.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					openURL(urls[1]);
				}
			});
		}
		return presetM1;
	}
	private JButton getPresetM2() {
		if(presetM2 == null) {
			presetM2 = new JButton();
			presetM2.setText("2");
			presetM2.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					openURL(urls[3]);
				}
			});
		}
		return presetM2;
	}
	private JButton getPresetM3() {
		if(presetM3 == null) {
			presetM3 = new JButton();
			presetM3.setText("3");
			presetM3.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					openURL(urls[5]);
				}
			});
		}
		return presetM3;
	}
	private JButton getPresetM4() {
		if(presetM4 == null) {
			presetM4 = new JButton();
			presetM4.setText("4");
			presetM4.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					openURL(urls[7]);
				}
			});
		}
		return presetM4;
	}
	private JButton getPresetM5() {
		if(presetM5 == null) {
			presetM5 = new JButton();
			presetM5.setText("5");
			presetM5.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					openURL(urls[9]);
				}
			});
		}
		return presetM5;
	}
	private JTextField getRadioPlaylistTF() {
		if(radioPlaylistTF == null) {
			radioPlaylistTF = new JTextField();
			radioPlaylistTF.setText(userUrl);
		}
		return radioPlaylistTF;
	}
	private JButton getPresetUserB() {
		if(presetUserB == null) {
			presetUserB = new JButton();
			presetUserB.setText("Play");
			presetUserB.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					openURL(getRadioPlaylistTF().getText());
				}
			});
		}
		return presetUserB;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"