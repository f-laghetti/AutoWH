package autoMag.agents;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

class TransactorGui extends JFrame {	
	private Transactor myAgent;
	
	private JTextField transactionCodeField;
	private JTextField wareQuantityField;
	
	TransactorGui(Transactor a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 2));
		p.add(new JLabel("Transaction code:"));
		transactionCodeField = new JTextField(15);
		p.add(transactionCodeField);
		p.add(new JLabel("Ware quantity:"));
		wareQuantityField = new JTextField(15);
		p.add(wareQuantityField);
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton requestButton = new JButton("Transaction request");
		requestButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				//se il transactor è occupato non accetta la transazione
				if(myAgent.info.isOccupied()) {
					System.out.println("Transactor occupied");
					transactionCodeField.setText("");
					wareQuantityField.setText("");
				}
				//se il transactor è libero accetta la transazione
				else {
					try {
						String transactionCode = transactionCodeField.getText().trim();
						String wareQuantity = wareQuantityField.getText().trim();
						myAgent.deliveryRequest(Integer.parseInt(transactionCode),Integer.parseInt(wareQuantity));
						transactionCodeField.setText("");
						wareQuantityField.setText("");
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(TransactorGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
					}
				}
			}
		} );
		p = new JPanel();
		p.add(requestButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}	
}