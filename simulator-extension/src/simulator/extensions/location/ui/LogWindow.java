package simulator.extensions.location.ui;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogWindow extends JFrame {
	private static final long serialVersionUID = -5610807955258781126L;
	private JTextArea textArea = new JTextArea();

	public LogWindow() {
		super("");
		setSize(300, 300);
		add(new JScrollPane(textArea));
		setVisible(true);
	}

	public void showInfo(String data) {
		textArea.append(data+"\r\n");
		textArea.setCaretPosition(textArea.getText().length()-1);
		this.validate();
	}

	public void showByte(char b) {
		textArea.append(b+"");
		textArea.setCaretPosition(textArea.getText().length()-1);
		this.validate();
	}
}