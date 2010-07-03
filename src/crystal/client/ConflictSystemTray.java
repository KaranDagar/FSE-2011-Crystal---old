package crystal.client;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import crystal.server.TestHgStateChecker;

public class ConflictSystemTray {
	private static ConflictClient _client;
	private static ClientPreferences _prefs;

	public static void main(String[] args) {

		// UIManager.put("swing.boldMetal", Boolean.FALSE);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_prefs = loadPreferences();

				createAndShowGUI();
			}

			private ClientPreferences loadPreferences() {
				TestHgStateChecker thgsc = new TestHgStateChecker();
				ClientPreferences prefs = thgsc.getPreferences();

				// TODO: this should be from some persisted location

				return prefs;
			}
		});
	}

	private static void createAndShowGUI() {
		// Check the SystemTray support
		if (!SystemTray.isSupported()) {
			System.err.println("SystemTray is not supported");
			return;
		}

		final PopupMenu trayMenu = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon(createImage("images/bulb.gif", "tray icon"));
		final SystemTray tray = SystemTray.getSystemTray();

		trayIcon.setToolTip("ConflictClient");

		// Create a popup menu components
		MenuItem aboutItem = new MenuItem("About");
		MenuItem preferencesItem = new MenuItem("Preferences");
		CheckboxMenuItem enabledItem = new CheckboxMenuItem("Daemon Enabled");
		final MenuItem showClientItem = new MenuItem("Show Client");
		MenuItem exitItem = new MenuItem("Exit");

		// Add components to popup menu
		trayMenu.add(aboutItem);
		trayMenu.addSeparator();
		trayMenu.add(preferencesItem);
		trayMenu.add(enabledItem);
		trayMenu.addSeparator();
		trayMenu.add(showClientItem);
		trayMenu.addSeparator();
		trayMenu.add(exitItem);

		trayIcon.setPopupMenu(trayMenu);

		// make sure the client is enabled by default
		enabledItem.setState(true);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		}

		trayIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				System.out.println("Tray icon action: " + ae);
				// doesn't work on OS X; it doesn't register double clicks on the tray
				showClient();
			}

		});

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Built by Holmes, Brun, Ernst, and Notkin.");
			}
		});

		preferencesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (_client != null) {
					_client.close();
					_client = null;
				}

				showClientItem.setEnabled(false);
				ClientPreferencesUI cp = new ClientPreferencesUI(_prefs, new ClientPreferencesUI.IPreferencesListener() {
					@Override
					public void preferencesChanged(ClientPreferences preferences) {
						// when the preferences are updated, show the client
						_prefs = preferences;
					}

					@Override
					public void preferencesDialogClosed() {
						System.out.println("ConflictSystemTray::IPreferencesListener::preferencesDialogClosed()");
						showClientItem.setEnabled(true);
					}
				});
				cp.createAndShowGUI();
			}
		});

		showClientItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showClient();
			}
		});

		enabledItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int cb1Id = e.getStateChange();
				if (cb1Id == ItemEvent.SELECTED) {
					// daemon enabled
					System.out.println("ConflictClient - ConflictDaemon enabled");
				} else {
					// daemon disabled
					System.out.println("ConflictClient - ConflictDaemon disabled");
				}
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("ConflictClient - Client explicitly exited");
				tray.remove(trayIcon);
				System.exit(0);
			}
		});

	}

	private static void showClient() {
		if (_client != null) {
			_client.close();
			_client = null;
		}
		_client = new ConflictClient();
		_client.createAndShowGUI(_prefs);
		_client.calculateConflicts();

	}

	// Obtain the image URL
	protected static Image createImage(String path, String description) {
		URL imageURL = ConflictSystemTray.class.getResource(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}
}