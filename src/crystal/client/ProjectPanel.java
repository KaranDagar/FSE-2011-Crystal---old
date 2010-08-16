package crystal.client;

import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import crystal.model.DataSource;

/**
 * @author brun
 *
 */
public class ProjectPanel extends JPanel {
	
	private static final long serialVersionUID = 5244512987255240473L;
	private String _name;

	/**
	 * 
	 */
	public ProjectPanel(final ProjectPreferences pref, final ClientPreferences prefs, final JFrame mainFrame, final JTabbedPane tabbedPane) {
		super();
		
		final JPanel panel = this;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		_name = pref.getEnvironment().getShortName();

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
		namePanel.add(new JLabel("Project Name: "));
		final JTextField shortName = new JTextField(pref.getEnvironment().getShortName());
		namePanel.add(shortName);
		shortName.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {				
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				pref.getEnvironment().setShortName(shortName.getText());
				prefs.setChanged(true);
				_name = shortName.getText();
				tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), _name);
				panel.validate();
				mainFrame.pack();
			}
		});
		add(namePanel);
		
		JPanel parentPanel = new JPanel();
		parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.X_AXIS));
		parentPanel.add(new JLabel("Parent Name (optional): "));
		final JTextField parentName = new JTextField(pref.getEnvironment().getParent());
		if (parentName.getText().equals(""))
			parentName.setText(" ");
		parentPanel.add(parentName);
		parentName.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {				
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				pref.getEnvironment().setParent(parentName.getText());
				prefs.setChanged(true);
				if (parentName.getText().equals(""))
					parentName.setText(" ");
				panel.validate();
				mainFrame.pack();
			}
		});
		add(parentPanel);

		
		JPanel typePanel = new JPanel();
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.X_AXIS));		
		typePanel.add(new JLabel("Repo Type: "));
		final JComboBox type = new JComboBox();
		type.addItem(DataSource.RepoKind.HG);
//		type.addItem(DataSource.RepoKind.GIT);
		type.setSelectedItem(pref.getEnvironment().getKind());
		typePanel.add(type);
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pref.getEnvironment().setKind((DataSource.RepoKind) type.getSelectedItem());
				prefs.setChanged(true);
			}
		});
		add(typePanel);

		JPanel addressPanel = new JPanel();
		addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.X_AXIS));		
		addressPanel.add(new JLabel("Clone Address: "));
		final JTextField address = new JTextField(pref.getEnvironment().getCloneString());
		addressPanel.add(address);
		address.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {				
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				pref.getEnvironment().setCloneString(address.getText());
				prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});
		add(addressPanel);
		
		final JButton newRepoButton = new JButton("Add New Repository");
		
		newRepoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				HashSet<String> shortNameLookup = new HashSet<String>();
				for (DataSource current : pref.getDataSources()) {
					shortNameLookup.add(current.getShortName());
				}
				int count = 1;
				while (shortNameLookup.contains("New Repo " + count++));
				
				DataSource newGuy = new DataSource("New Repo " + --count, "", DataSource.RepoKind.HG, false, null);
				pref.addDataSource(newGuy);
				add(repoPanel(newGuy, pref, prefs, panel, mainFrame));
				prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});
		add(newRepoButton);
		
		JPanel sourcesPanel = new JPanel();
		GridBagLayout grid = new GridBagLayout();
		sourcesPanel.setLayout(grid); 
//		sourcesPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.weightx = 1.0;
		JLabel pShortName = new JLabel("Short Name");
		JLabel pHide = new JLabel("Hide?");
		JLabel pParent = new JLabel("Parent");
		JLabel pClone = new JLabel("Clone Address");
		JLabel pDelete = new JLabel("");

		
		grid.setConstraints(pShortName, constraints);
		sourcesPanel.add(pShortName);
		grid.setConstraints(pHide, constraints);
		sourcesPanel.add(pHide);
		grid.setConstraints(pParent, constraints);
		sourcesPanel.add(pParent);
		grid.setConstraints(pClone, constraints);
		sourcesPanel.add(pClone);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(pDelete, constraints);
		sourcesPanel.add(pDelete);

		constraints.weightx = 0.0;
		for (DataSource source : pref.getDataSources()) {
			JPanel repoPanel = repoPanel(source, pref, prefs, panel, mainFrame);
			grid.setConstraints(repoPanel, constraints);
			sourcesPanel.add(repoPanel);
		}
		add(sourcesPanel);
	}

	public String getName() {
		return _name;
	}

	private JPanel repoPanel(final DataSource source, final ProjectPreferences pref, final ClientPreferences prefs, final JPanel panel, final JFrame mainFrame) {
		final JPanel repoPanel = new JPanel();		
		repoPanel.setLayout(new BoxLayout(repoPanel, BoxLayout.X_AXIS));

/*
		repoPanel.add(new JLabel("Repo Type"));
		final JComboBox type = new JComboBox();
		type.addItem(DataSource.RepoKind.HG);
//		type.addItem(DataSource.RepoKind.GIT);
		type.setSelectedItem(source.getKind());
		repoPanel.add(type);
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				source.setKind((DataSource.RepoKind) type.getSelectedItem());
				prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});
*/

//		repoPanel.add(new JLabel("Short Name"));
		final JTextField shortName = new JTextField(source.getShortName());
		repoPanel.add(shortName);
		shortName.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {				
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				source.setShortName(shortName.getText());
				prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});
		
//		repoPanel.add(new JLabel("Hide?"));
		final JCheckBox hideBox = new JCheckBox();
		if (source.isHidden())
			hideBox.setSelected(true);
		repoPanel.add(hideBox);
		hideBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				hideBox.setSelected(!(hideBox.isSelected()));
				source.hide(hideBox.isSelected());
				prefs.setChanged(true);
			}
		});
		
//		repoPanel.add(new JLabel("Parent"));
		final JTextField parent = new JTextField(source.getParent());
		if (parent.getText().equals(""))
			parent.setText(" ");
		repoPanel.add(parent);
		parent.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {				
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				source.setParent(parent.getText());
				prefs.setChanged(true);
				if (parent.getText().equals(""))
					parent.setText(" ");
				panel.validate();
				mainFrame.pack();
			}
		});

//		repoPanel.add(new JLabel("Clone Address"));
		final JTextField cloneAddress = new JTextField(source.getCloneString());
		repoPanel.add(cloneAddress);
		cloneAddress.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {				
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				source.setCloneString(cloneAddress.getText());
				prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});
		
		final JButton deleteRepoButton = new JButton("Delete");
		deleteRepoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pref.removeDataSource(source);
				prefs.setChanged(true);
				panel.remove(repoPanel);
				mainFrame.pack();
			}
		});
		repoPanel.add(deleteRepoButton);

		return repoPanel;
	}
}
