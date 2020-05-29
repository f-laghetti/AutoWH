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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

class WarehouseAddTransporterGui extends JFrame {	
	private Warehouse myAgent;
	
	private JTextField macAddressField;
	private JTextField sectorField;
	private JTextField capacityField;
	private JCheckBox directionField;
	
	WarehouseAddTransporterGui(Warehouse a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(4, 2));
		p.add(new JLabel("MAC Address:"));
		macAddressField = new JTextField(15);
		p.add(macAddressField);
		p.add(new JLabel("Sector:"));
		sectorField = new JTextField(15);
		p.add(sectorField);
		p.add(new JLabel("Capacity:"));
		capacityField = new JTextField(15);
		p.add(capacityField);
		directionField = new JCheckBox("Anti-clockwise");
        p.add(directionField);
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton requestButton = new JButton("Add Transporter");
		requestButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String macAddressCode = macAddressField.getText().trim();
					String sectorCoord = sectorField.getText().trim();
					String capacity = capacityField.getText().trim();
					Boolean direction = directionField.isSelected();
					myAgent.AddNewTransporter(macAddressCode, sectorCoord, capacity, direction);
					macAddressField.setText("");
					sectorField.setText("");
					capacityField.setText("");
					directionField.setSelected(false);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(WarehouseAddTransporterGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
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
