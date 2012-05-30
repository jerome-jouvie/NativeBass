// BASS plugin test, copyright (c) 2005-2006 Ian Luck.

package jouvieje.bass.examples;

import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_CTYPE.BASS_CTYPE_STREAM_AIFF;
import static jouvieje.bass.defines.BASS_CTYPE.BASS_CTYPE_STREAM_MP1;
import static jouvieje.bass.defines.BASS_CTYPE.BASS_CTYPE_STREAM_MP2;
import static jouvieje.bass.defines.BASS_CTYPE.BASS_CTYPE_STREAM_MP3;
import static jouvieje.bass.defines.BASS_CTYPE.BASS_CTYPE_STREAM_OGG;
import static jouvieje.bass.defines.BASS_CTYPE.BASS_CTYPE_STREAM_WAV;
import static jouvieje.bass.defines.BASS_CTYPE.BASS_CTYPE_STREAM_WAV_FLOAT;
import static jouvieje.bass.defines.BASS_CTYPE.BASS_CTYPE_STREAM_WAV_PCM;
import static jouvieje.bass.defines.BASS_POS.BASS_POS_BYTE;
import static jouvieje.bass.defines.BASS_SAMPLE.BASS_SAMPLE_LOOP;





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
import java.io.FilenameFilter;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import jouvieje.bass.BassInit;
import jouvieje.bass.examples.util.BassExampleFrame;
import jouvieje.bass.examples.util.FileFilters;
import jouvieje.bass.examples.util.GraphicalGui;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.structures.BASS_CHANNELINFO;
import jouvieje.bass.structures.BASS_PLUGININFO;
import jouvieje.bass.structures.HPLUGIN;
import jouvieje.bass.structures.HSTREAM;
import org.jouvieje.libloader.LibLoader;

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
public class Plugins extends GraphicalGui {
	private static final long serialVersionUID = 1L;
	
	/* display error messages */
	private final void error(String text) {
		JOptionPane.showMessageDialog(Plugins.this,
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

	public static void main(String[] args) {
		new BassExampleFrame(new Plugins());
	}
	
	HSTREAM stream;		// the channel

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
		
		//Initialize default output device
		if(!BASS_Init(forceNoSoundDevice(-1), forceFrequency(44100), 0, null, null)) {
			error("Can't initialize device");
			stop();
			return;
		}
		
		// look for plugins (in the executable's directory)
		{
			File[] plugins = new File(".").listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					name = name.toLowerCase();
					
					final int platform = LibLoader.getPlatform();
					if(platform == LibLoader.PLATFORM_WINDOWS) {
						return name.startsWith("bass") && name.endsWith(".dll");
					}
					else if(platform == LibLoader.PLATFORM_MAC) {
						return name.startsWith("libbass") && name.endsWith(".dylib");
					}
					else {
						return false;
					}
				}
			});
			if(plugins != null && plugins.length > 0) {
				//Browse plugins
				for(File plugin : plugins) {
					HPLUGIN plug = BASS_PluginLoad(plugin.getName(), 0);
					if(plug == null) {
						continue;
					}

					//plugin loaded...
					BASS_PLUGININFO pluginInfo = BASS_PluginGetInfo(plug); // get plugin info to add to the file selector filter...
					for(int f = 0; f < pluginInfo.getNumFormats(); f++) {
						//Format description
						final String format = String.format("%s (%s) - %s", 
								pluginInfo.getFormats()[f].getName(),
								pluginInfo.getFormats()[f].getExts(),
								plugin.getName());
						//Extension filter
						final String exts = String.format("%s", pluginInfo.getFormats()[f].getExts());

						getFileChooser().addChoosableFileFilter(new FileFilter(){
							@Override
							public String getDescription() {
								return format;
							}
							@Override
							public boolean accept(File f) {
								return f.isDirectory() || exts.contains(extensionOf(f.getName()));
							}
							private String extensionOf(String file) {
								int index = file.lastIndexOf(".");
								if(index != -1) return file.substring(index+1).toLowerCase();
								return "";
							}
						});

						// add plugin to the list
						pluginsListModel.addElement(plugin.getName());
					}
				}
			}
			else {
				// no plugins...
				pluginsListModel.addElement("no plugins - visit the BASS webpage to get some");
			}
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
		
		//"free" the output device and all plugins
		BASS_Free();
		BASS_PluginFree(null);		//Free all plugins
	}
	
	// translate a CTYPE value to text
	private String getChannelTypeString(long channelType, HPLUGIN plugin) {
		if(plugin != null) {
			// using a plugin
			BASS_PLUGININFO pinfo = BASS_PluginGetInfo(plugin);		//get plugin info
			for(int a = 0; a < pinfo.getNumFormats(); a++) {
				if(pinfo.getFormats()[a].getChannelType() == channelType) {	//found a "ctype" match...
					return pinfo.getFormats()[a].getName(); 				//return it's name
				}
			}
		}
		// check built-in stream formats...
		if(channelType == BASS_CTYPE_STREAM_OGG) return "Ogg Vorbis";
		if(channelType == BASS_CTYPE_STREAM_MP1) return "MPEG layer 1";
		if(channelType == BASS_CTYPE_STREAM_MP2) return "MPEG layer 2";
		if(channelType == BASS_CTYPE_STREAM_MP3) return "MPEG layer 3";
		if(channelType == BASS_CTYPE_STREAM_AIFF) return "Audio IFF";
		if(channelType == BASS_CTYPE_STREAM_WAV_PCM) return "PCM WAVE";
		if(channelType == BASS_CTYPE_STREAM_WAV_FLOAT) return "Floating-point WAVE";
		if((channelType & BASS_CTYPE_STREAM_WAV) != 0) { // other WAVE codec, could use acmFormatTagDetails to get its name, but...
			return "WAVE";
		}
		return "?";
	}

	private Timer timer = new Timer(250, new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			if(stream != null) {
				int time = (int)BASS_ChannelBytes2Seconds(stream.asInt(), BASS_ChannelGetPosition(stream.asInt(), BASS_POS_BYTE));		//update position
				getPosition().setValue(time);
			}
		}
	});
	
	@Override
	public JPanel getPanel() { return this; }
	@Override
	public String getTitle() { return "BASS plugin test"; }
	
			/* Graphical stuff */
	
	private JFileChooser fileChooser = null;
	private JPanel pluginP = null;
	private JScrollPane pluginsSP = null;
	private JButton open = null;
	private JLabel infos = null;
	private JSlider position = null;
	private JList plugins = null;
	private DefaultListModel pluginsListModel = new DefaultListModel();
	
	public Plugins() {
		super();
		initialize();
	}

	private void initialize() {
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints4.gridy = 3;
		gridBagConstraints4.weightx = 1.0;
		gridBagConstraints4.insets = new Insets(0, 30, 0, 30);
		gridBagConstraints4.gridx = 0;
		gridBagConstraints4.insets = new Insets(0, 5, 5, 5);
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.fill = GridBagConstraints.BOTH;
		gridBagConstraints3.weightx = 1.0D;
		gridBagConstraints3.gridy = 2;
		infos = new JLabel();
		infos.setText("");
		infos.setHorizontalTextPosition(SwingConstants.CENTER);
		infos.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.weightx = 1.0D;
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints2.weighty = 1.0D;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints4.insets = new Insets(0, 5, 5, 5);
		this.setSize(new Dimension(306, 220));
		this.setPreferredSize(new Dimension(306, 220));
		this.setLayout(new GridBagLayout());
		this.add(getPluginP(), gridBagConstraints2);
		this.add(getOpen(), gridBagConstraints1);
		this.add(infos, gridBagConstraints3);
		this.add(getPosition(), gridBagConstraints4);
	}

	private JFileChooser getFileChooser() {
		if(fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.resetChoosableFileFilters();
			fileChooser.addChoosableFileFilter(FileFilters.allFiles);
			fileChooser.addChoosableFileFilter(new FileFilter(){
				private final static String extensions = "mp3;mp2;mp1;ogg;wav;aif";

				@Override
				public String getDescription() {
					return "BASS built-in (*.mp3;*.mp2;*.mp1;*.ogg;*.wav;*.aif)";
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

	private JPanel getPluginP() {
		if(pluginP == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.weightx = 1.0;
			pluginP = new JPanel();
			pluginP.setLayout(new GridBagLayout());
			pluginP.setBorder(BorderFactory.createTitledBorder(null, "Loaded plugins", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			pluginP.add(getPluginsSP(), gridBagConstraints);
		}
		return pluginP;
	}

	private JScrollPane getPluginsSP() {
		if(pluginsSP == null) {
			pluginsSP = new JScrollPane();
			pluginsSP.setViewportView(getPlugins());
		}
		return pluginsSP;
	}

	private JButton getOpen() {
		if(open == null) {
			open = new JButton();
			open.setText("Click here to open a file ...");
			open.addActionListener(new java.awt.event.ActionListener(){
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(getFileChooser().showOpenDialog(Plugins.this) == JFileChooser.APPROVE_OPTION) {
						File file = getFileChooser().getSelectedFile();
						
						BASS_StreamFree(stream); 	// free the old stream
						stream = BASS_StreamCreateFile(false, file.getPath(), 0, 0, BASS_SAMPLE_LOOP);
						if(stream == null) {
							// it ain't playable
							open.setText("Click here to open a file ...");
							infos.setText("");
							error("Can't play the file");
							return;
						}
						open.setText(file.getName());
						open.setToolTipText(file.getPath());
						{
							// display the file type and length
							long bytes = BASS_ChannelGetLength(stream.asInt(), BASS_POS_BYTE);
							int time = (int)BASS_ChannelBytes2Seconds(stream.asInt(), bytes);
							BASS_CHANNELINFO info = BASS_CHANNELINFO.allocate();
							BASS_ChannelGetInfo(stream.asInt(), info);
							String s = String.format("<html><body>channel type = %x (%s)<br>length = %d (%d:%02d)</body></html>",
								info.getChannelType(),
								getChannelTypeString(info.getChannelType(), info.getPlugin()),
								bytes, time/60, time%60);
							info.release();
							infos.setText(s);
							position.setMaximum(time);
							position.setValue(0);	// update scroller range
						}
						BASS_ChannelPlay(stream.asInt(), false);
					}
				}
			});
		}
		return open;
	}

	private JSlider getPosition() {
		if(position == null) {
			position = new JSlider();
			position.setValue(0);
			position.addChangeListener(new javax.swing.event.ChangeListener(){
				@Override
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					if(position.getValueIsAdjusting()) {
						int pos = position.getValue();
						BASS_ChannelSetPosition(stream.asInt(), BASS_ChannelSeconds2Bytes(stream.asInt(), pos), BASS_POS_BYTE);
					}
				}
			});
		}
		return position;
	}

	private JList getPlugins() {
		if(plugins == null) {
			plugins = new JList(pluginsListModel);
			plugins.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return plugins;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
