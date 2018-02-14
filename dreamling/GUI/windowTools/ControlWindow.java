package GUI.windowTools;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;


@SuppressWarnings("serial")
public class ControlWindow extends JFrame {
	
	public static final int STANDARD_TEXTAREA_ROWS = 5;
	public static final int STANDARD_TEXTAREA_COLUMNS = 30;
	public static final int STANDARD_SCROLLPANE_WIDTH = 1;
	public static final int STANDARD_SCROLLPANE_HEIGHT = 1;
	private Container content;
	GridBagConstraints c;
	private int currentRow;
	private int currentColumn;
	
	
	public ControlWindow(String title) {
		super(title);
		currentRow = 0;
		currentColumn = 0;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		content = getContentPane();
		content.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		pack();
		//addWindowListener( new ExitWindowListener(this) );
		setVisible(true);
	}
	
	public CustomScrollPane createScrollPane() {
		return new CustomScrollPane();
	}
	
	public CustomScrollPane addScrollPane(CustomScrollPane sp) {
		return addScrollPane(sp, STANDARD_SCROLLPANE_WIDTH, STANDARD_SCROLLPANE_HEIGHT);
	}
	
	public CustomScrollPane addScrollPane(CustomScrollPane sp, int gridWidth, int gridHeight) {
		return addScrollPane(sp, gridWidth, gridHeight, currentColumn, currentRow);
	}
	
	public CustomScrollPane addScrollPane(CustomScrollPane sp, int gridWidth, int gridHeight, int gridX, int gridY) {
		c.gridx = (currentColumn = gridX);
		c.gridy = (currentRow = gridY);
		c.gridwidth = gridWidth;
		c.gridheight = gridHeight;
		content.add(sp, c);
		pack();
		incrementPosition();
		return sp;
	}
	
	public JTextField createTextField(boolean editable) {
		JTextField tf = new JTextField(10);
		tf.setEditable(editable);
		return tf;
	}
	
	public JTextField addTextField(JTextField tf) {
		return addTextField(tf, currentColumn, currentRow);
	}
	
	public JTextField addTextField(JTextField tf, int gridX, int gridY) {
		c.gridx = (currentColumn = gridX);
		c.gridy = (currentRow = gridY);
		c.gridwidth = 1;
		c.gridheight = 1;
		content.add(tf, c);
		pack();
		incrementPosition();
		return tf;
	}
	
	public JTextArea createTextArea(boolean editable) {
		JTextArea ta = new JTextArea(STANDARD_TEXTAREA_ROWS, STANDARD_TEXTAREA_COLUMNS);
		ta.setEditable(editable);
		return ta;
	}
	
	public JTextArea createTextArea(boolean editable, int contentRows, int contentColumns) {
		JTextArea ta = new JTextArea(contentRows, contentColumns);
		ta.setEditable(editable);
		return ta;
	}
	
	public JTextArea addTextArea(JTextArea ta) {
		return addTextArea(ta, currentColumn, currentRow);
	}
	
	public JTextArea addTextArea(JTextArea ta, int gridX, int gridY) {
		return addTextArea(ta, gridX, gridY, 1, 1);
	}
	
	public JTextArea addTextArea(JTextArea ta, int gridX, int gridY, int gridWidth, int gridHeight) {
		JScrollPane sp = new JScrollPane(ta);
		c.gridx = (currentColumn = gridX);
		c.gridy = (currentRow = gridY);
		c.gridwidth = gridWidth;
		c.gridheight = gridHeight;
		content.add(sp, c);
		pack();
		incrementPosition();
		return ta;
	}
	
	public JLabel createLabel() {
		return createLabel("", false);
	}
	
	public JLabel createLabel(String text) {
		return createLabel(text, false);
	}
	
	public JLabel createLabel(String text, boolean rightBorder) {
		JLabel jl = new JLabel();
		jl.setText(text);
		if (rightBorder) jl.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.black));
		return jl;
	}
	
	public JLabel addLabel(JLabel l) {
		return addLabel(l, currentColumn, currentRow);
	}
	
	public JLabel addLabel(JLabel l, int gridX, int gridY) {
		return addLabel(l, gridX, gridY, 1, 1);
	}
	
	public JLabel addLabel(JLabel l, int gridX, int gridY, int gridWidth, int gridHeight) {
		c.gridx = (currentColumn = gridX);
		c.gridy = (currentRow = gridY);
		c.gridwidth = gridWidth;
		c.gridheight = gridHeight;
		content.add(l, c);
		pack();
		incrementPosition();
		return l;
	}
	
	public JButton createButton(String name) {
		JButton b = new JButton(name);
		return b;
	}
	
	public JButton createButton(String name, ActionListener al) {
		JButton b = new JButton(name);
		if (al != null) b.addActionListener(al);
		return b;
	}
	
	public JButton addButton(JButton b) {
		return addButton(b, currentColumn, currentRow);
	}
	
	public JButton addButton(JButton b, int gridX, int gridY) {
		return addButton(b, gridX, gridY, 1, 1);
	}
	
	public JButton addButton(JButton b, int gridX, int gridY, int gridWidth, int gridHeight) {
		c.gridx = (currentColumn = gridX);
		c.gridy = (currentRow = gridY);
		c.gridwidth = gridWidth;
		c.gridheight = gridHeight;
		content.add(b, c);
		pack();
		incrementPosition();
		return b;
	}
	
	public void incrementPosition() {
		currentColumn++;
	}
	
	public void nextRow() {
		currentColumn = 0;
		currentRow++;
	}
	
	public void setCursorAt(int gridX, int gridY) {
		currentColumn = gridX;
		currentRow = gridY;
	}
	
	public int getCurrentColumn() {
		return currentColumn;
	}
	
	public int getCurrentRow() {
		return currentRow;
	}
	
}
