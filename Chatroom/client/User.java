package client;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JEditorPane;

public class User extends JPanel {
    private String name;
    private JEditorPane nameField;

    public User(String name) {
		this.name = name;
		nameField = new JEditorPane();
		nameField.setContentType("text/html");
		nameField.setText(name);
		nameField.setEditable(false);
		this.setLayout(new BorderLayout());
		this.add(nameField);
    }

    public String getName() {
    	return name;
    }
    
    public void setName(String clientName) {
    	nameField.setText(clientName);
    }
}