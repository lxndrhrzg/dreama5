package GUI.windowTools;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CustomScrollPane extends JScrollPane {
	
	private static final long serialVersionUID = 101987903016697403L;
	
	private JPanel content;
	private GridBagConstraints c;
	private int currentRow;
	private int currentColumn;
	private JPanel row;
	
	public CustomScrollPane() {
		this(false);
	}
	
	public CustomScrollPane(boolean flowLayout) {
		super();
		c = new GridBagConstraints();
		currentRow = 0;
		currentColumn = 1;
		if (flowLayout) {
			content = new JPanel(new GridBagLayout());
			row = new JPanel(new FlowLayout());
			c.gridx = currentColumn;
			c.gridy = currentRow;
			c.gridwidth = 1;
			c.gridheight = 1;
			content.add(row, c);
		} else {
			content = new JPanel(new GridBagLayout());
			row = null;
		}
		getViewport().add(content);
	}
	
	public Component addComponent(Component comp) {
		return addComponent(comp, 1, 1);
	}
	
	public Component addComponent(Component comp, int width, int height) {
		if (row != null) { //if flow
			row.add(comp);
		} else {
			c.gridx = currentColumn;
			c.gridy = currentRow;
			c.gridwidth = width;
			c.gridheight = height;
			content.add(comp, c);
		}
		incrementPosition();
		revalidate();
		repaint();
		return comp;
	}
	
	public Component addLeftSidebar(Component comp, int y, int height) {
		c.gridx = 0;
		c.gridy = y;
		c.gridwidth = 1;
		c.gridheight = height;
		content.add(comp, c);
		revalidate();
		repaint();
		return comp;
	}
	
	public void removeComponent(Component comp) {
		if (row != null) {
			row.remove(comp);
		} else {
			content.remove(comp);
		}
		revalidate();
		repaint();
	}
	
	public Component[] getContent() {
		return content.getComponents();
	}
	
	public void incrementPosition() {
		currentColumn++;
	}
	
	public void newLine() {
		currentColumn = 1;
		currentRow++;
		if (row != null) {
			row = new JPanel(new FlowLayout());
			c.gridx = currentColumn;
			c.gridy = currentRow;
			c.gridwidth = 1;
			c.gridheight = 1;
			content.add(row, c);
		}
	}
	
}
