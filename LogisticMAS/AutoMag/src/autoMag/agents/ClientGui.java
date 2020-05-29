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

class ClientGui extends JFrame {	
	private Client myAgent;
	
	private JTextField warehouseNameField, wareCodeField, wareNameField, wareQuantityField;
	
	ClientGui(Client a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(4, 2));
		p.add(new JLabel("Warehouse:"));
		warehouseNameField = new JTextField(15);
		p.add(warehouseNameField);
		p.add(new JLabel("Ware brand:"));
		wareNameField = new JTextField(15);
		p.add(wareNameField);
		p.add(new JLabel("Ware code"));
		wareCodeField = new JTextField(15);
		p.add(wareCodeField);		
		p.add(new JLabel("Quantity:"));
		wareQuantityField = new JTextField(15);
		p.add(wareQuantityField);
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton requestButton = new JButton("Store request");
		requestButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String warehouseName = warehouseNameField.getText().trim();
					String wareCode = wareCodeField.getText().trim();
					String wareName = wareNameField.getText().trim();	
					String wareQuantity = wareQuantityField.getText().trim();
					myAgent.requestTransaction(warehouseName, wareCode, wareName, wareQuantity);
					warehouseNameField.setText("");
					wareCodeField.setText("");
					wareNameField.setText("");
					wareQuantityField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(ClientGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(requestButton);
		getContentPane().add(p, BorderLayout.WEST);
		
		JButton collectButton = new JButton("Collect request");
		collectButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String warehouseName = warehouseNameField.getText().trim();
					String wareCode = wareCodeField.getText().trim();
					String wareName = wareNameField.getText().trim();	
					String wareQuantity = wareQuantityField.getText().trim();
					myAgent.requestCollect(warehouseName, wareCode, wareName, wareQuantity);
					warehouseNameField.setText("");
					wareCodeField.setText("");
					wareNameField.setText("");
					wareQuantityField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(ClientGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(collectButton);
		getContentPane().add(p, BorderLayout.EAST);
		
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
