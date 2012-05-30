/*
 * Created on 8 janv. 08
 */
package jouvieje.bass.examples.util;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JApplet;

import jouvieje.bass.examples.util.End;
import jouvieje.bass.examples.util.BassExample;

public class BassExampleApplet  extends JApplet implements End {
	private static final long serialVersionUID = 1L;
	
	private BassExample example;
	private Thread thread = null;

	@Override
	public void init() {
		//Instanciate the fmod example choosed
		try {
			this.example = (BassExample)Class.forName(getParameter("bassExample")).newInstance();
		} catch(Exception e) {
			e.printStackTrace();
		}
		example.setEnd(this);

		this.setSize(example.getPanel().getSize());
		this.setContentPane(example.getPanel());
		this.addKeyListener(new KeyAdapter(){
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE) example.stop();
			}
		});
	}

	@Override
	public void start() {
		thread = new Thread(example);
		example.init();
		thread.start();
	}

	@Override
	public void stop() {
		if(example != null) {
			//Attempt to stop the thread if running
			if(thread != null) {
				while(thread.isAlive()) {
					example.sendStopCommand();
				}
				thread = null;
			}
			example.stop();
		}
	}
	
	@Override
	public void end() {
		
	}
}
