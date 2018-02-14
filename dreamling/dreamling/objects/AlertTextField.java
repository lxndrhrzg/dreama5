package dreamling.objects;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dreamling.listMaker.DreamlistMaker.Account;

public class AlertTextField extends JTextField {
	
	private static final long serialVersionUID = -5475388799633730644L;
	
	//becomes yellow if value is handchanged. goes back to default color if programmatically set value
	
	public AlertTextField(String text, Account acc, int i) {
		super(text, 8);
		getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				acc.set(i, AlertTextField.this.getText());
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				acc.set(i, AlertTextField.this.getText());
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				acc.set(i, AlertTextField.this.getText());
			}
		});
	}
	
}
