import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SelectionDialog extends JDialog {
	public String answer;
	
	public SelectionDialog(JFrame parent, String title, String[] items){
		super(parent, title, true);
		setSize(400, items.length * 70);

		answer = null;

		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		
		for(int i = 0; i < items.length; i++) {
			JButton button = new JButton(items[i]);
			button.setPreferredSize(new Dimension(300, 40));
			button.addActionListener(e -> {
				answer = button.getText();
				dispose();
			});
			
			GridBagConstraints gbc1 = new GridBagConstraints();
			gbc1.gridx = 0;
			gbc1.gridy = i * 2;
			layout.setConstraints(button, gbc1);
			getContentPane().add(button);
			
			JPanel spacer = new JPanel();
			spacer.setSize(10, 10);
			
			GridBagConstraints gbc2 = new GridBagConstraints();
			gbc2.gridx = 0;
			gbc2.gridy = i * 2 + 1;
			layout.setConstraints(spacer, gbc2);
			getContentPane().add(spacer);
		}
		setVisible(true);	
	}
}