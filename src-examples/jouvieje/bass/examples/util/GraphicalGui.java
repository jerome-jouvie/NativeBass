/* $LICENSE$ */
package jouvieje.bass.examples.util;

import java.awt.Dimension;

import javax.swing.JPanel;

public abstract class GraphicalGui extends JPanel implements BassExample {
	private static final long serialVersionUID = 1L;
	
	public GraphicalGui() {
		super();
		initialize();
	}
	private final void initialize() {
		this.setSize(new Dimension(550, 400));
	}
	
	@Override
	public void sendStopCommand() {
		
	}

	@Override
	public void setEnd(End end) {
		
	}
}
