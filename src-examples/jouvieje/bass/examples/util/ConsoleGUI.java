package jouvieje.bass.examples.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * @author Jérôme Jouvie (Jouvieje)
 * @site   http://jerome.jouvie.free.fr/
 * @mail   jerome.jouvie@gmail.com
 */
public abstract class ConsoleGUI extends JPanel implements BassExample {
	private static final long serialVersionUID = 1L;
	
	public End end;
	private boolean inputChanged = false;
	private String input = null;
	private char key = 0;
	
	@Override
	public void setEnd(End end) {
		this.end = end;
	}
	
	public synchronized String getInput() {
		String localInput = input;
		resetInput();
		return localInput;
	}

	public synchronized char getKey() {
		char localKey = key;
		resetInput();
		return localKey;
	}

	public synchronized boolean keyHit() {
		return inputChanged;
	}

	public void setInput(String input) {
		getInputTF().setText(input);
		getInputTF().setSelectionStart(0);
		getInputTF().setSelectionEnd(getInputTF().getText().length() - 1);
	}

	//====================== GUI Interaction ============================

	@Override
	public void sendStopCommand() {
		//Force stop
		inputChanged = true;
		input = "e";
		key = 'e';
	}
	
	protected void resetInput() {
		inputChanged = false;
		input = null;
		key = 0;
	}

	protected String readInput(String message) {
		print(message + "\n");
		inputChanged = false;
		while(!inputChanged) {
			Thread.yield();
		}
		return input;
	}

	protected char readKey(String message) {
		print(message + "\n");
		inputChanged = false;
		while(!inputChanged) {
			Thread.yield();
		}
		return key;
	}

	protected void print(String message) {
		getOutputTA().append(message);
		getOutputTA().setCaretPosition(getOutputTA().getText().length());
	}

	protected void printf(String format, Object... args) {
		print(String.format(format, args));
	}

	protected void printr(String message) {
		String text = getOutputTA().getText();
		int index = text.lastIndexOf("\n");
		if(index >= 0) text = text.substring(0, index + 1);
		getOutputTA().setText(text + message);
		getOutputTA().setCaretPosition(getOutputTA().getText().length());
	}

	protected void printfr(String format, Object... args) {
		printr(String.format(format, args));
	}

	protected void printExit(String message) {
		print(message);
		JOptionPane.showMessageDialog(this, message);
		stop();
		end.end();
	}

	protected void printfExit(String format, Object... args) {
		printExit(String.format(format, args));
	}
	
	private final void readEnter() {
		synchronized(ConsoleGUI.this) {
			input = getInputTF().getText();
			if(input != null && input.length() > 0) {
				key = (input == null) ? 0 : input.charAt(0);
			}
			else {
				key = 0;
			}
			inputChanged = true;
			getInputTF().setText("");
			getInputTF().requestFocus();
		}
	}

	//====================== GUI ============================

	private JTextArea outputTA = null;
	private JTextField inputTF = null;
	private JScrollPane outputSP = null;

	protected void initialize() {
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 1;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.gridx = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.gridx = 0;

		this.setLayout(new GridBagLayout());
		this.setSize(new Dimension(550, 400));
		this.setPreferredSize(new Dimension(550, 400));
		this.add(getOutputSP(), gridBagConstraints);
		this.add(getInputTF(), gridBagConstraints1);
		this.add(new JLabel("Input"), gridBagConstraints2);
	}

	private JScrollPane getOutputSP() {
		if(outputSP == null) {
			outputSP = new JScrollPane();
			outputSP.setViewportView(getOutputTA());
		}
		return outputSP;
	}

	private JTextArea getOutputTA() {
		if(outputTA == null) {
			outputTA = new JTextArea();
			outputTA.setEditable(false);
			outputTA.setBackground(new Color(220, 220, 220));

			Font font = Font.decode("Courier New");
			if(font != null) {
				Font oldFont = outputTA.getFont();
				outputTA.setFont(font.deriveFont(oldFont.getStyle(), oldFont.getSize()));
			}
		}
		return outputTA;
	}

	private JTextField getInputTF() {
		if(inputTF == null) {
			inputTF = new JTextField();
			inputTF.addKeyListener(new KeyAdapter(){
				@Override
				public void keyReleased(KeyEvent e) {
					switch(e.getKeyCode()) {
						case KeyEvent.VK_ENTER: readEnter(); break;
					}
				}
			});
		}
		return inputTF;
	}
}
