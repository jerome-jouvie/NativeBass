/*
 * Created on 12 janv. 08
 */
package jouvieje.bass.examples.util;

import javax.swing.JPanel;

import jouvieje.bass.examples.util.End;

public interface BassExample extends Runnable {
	public void setEnd(End end);
	public String getTitle();
	public JPanel getPanel();
	public void init();
	public void stop();
	public boolean isRunning();
	public void sendStopCommand();
}
