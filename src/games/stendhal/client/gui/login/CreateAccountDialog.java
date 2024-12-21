/* $Id: CreateAccountDialog.java,v 1.15 2012/06/30 18:56:14 kiheru Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.login;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.stendhal;
import games.stendhal.client.gui.ProgressBar;
import games.stendhal.client.gui.WindowUtils;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.update.ClientGameConfiguration;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import marauroa.client.BannedAddressException;
import marauroa.client.LoginFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.game.AccountResult;
import marauroa.common.net.InvalidVersionException;

import org.apache.log4j.Logger;


public class CreateAccountDialog extends JDialog {

	private static final long serialVersionUID = 4436228792112530975L;

	private static final Logger logger = Logger.getLogger(CreateAccountDialog.class);

	// Variables declaration
	private JLabel usernameLabel;
	private JLabel serverLabel;
	private JLabel serverPortLabel;
	private JLabel passwordLabel;
	private JLabel passwordretypeLabel;
	private JLabel emailLabel;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JPasswordField passwordretypeField;
	private JTextField emailField;
	private JTextField serverField;
	private JTextField serverPortField;
	private JButton createAccountButton;
	private JPanel contentPane;

	// End of variables declaration
	private StendhalClient client;
	private String badEmailTitle, badEmailReason, badPasswordReason;

	public CreateAccountDialog(final Frame owner, final StendhalClient client) {
		super(owner, true);
		this.client = client;
		initializeComponent(owner);

		WindowUtils.closeOnEscape(this);
		this.setVisible(true);
	}

	CreateAccountDialog() {
		super();
		initializeComponent(null);
	}

	private void initializeComponent(final Frame owner) {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (owner == null) {
					System.exit(0);
				}
				owner.setEnabled(true);
				dispose();
			}
		});

		serverLabel = new JLabel("Nazwa serwera");
		serverField = new JTextField(
				ClientGameConfiguration.get("DEFAULT_SERVER"));
		serverField.setEditable(true);
		serverPortLabel = new JLabel("Port serwera");
		serverPortField = new JTextField(
				ClientGameConfiguration.get("DEFAULT_PORT"));

		usernameLabel = new JLabel("Login");
		usernameField = new JTextField();
		//usernameField.setDocument(new LowerCaseLetterDocument());

		passwordLabel = new JLabel("Hasło");
		passwordField = new JPasswordField();

		passwordretypeLabel = new JLabel("Powtórz hasło");
		passwordretypeField = new JPasswordField();

		emailLabel = new JLabel("E-mail");
		emailField = new JTextField();

		// createAccountButton
		//
		createAccountButton = new JButton();
		createAccountButton.setText("Utwórz Konto");
		createAccountButton.setMnemonic(KeyEvent.VK_U);
		this.rootPane.setDefaultButton(createAccountButton);
		createAccountButton.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				createAccountButton_actionPerformed(e, false);
			}
		});

		//
		// contentPane
		//
		int padding = SBoxLayout.COMMON_PADDING;
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new SBoxLayout(SBoxLayout.VERTICAL, padding));
		contentPane.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		
		JComponent grid = new JComponent() {private static final long serialVersionUID = 1L;};
		grid.setLayout(new GridLayout(7, 2, padding, padding));
		contentPane.add(grid, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));

		// row 0
		grid.add(serverLabel);
		grid.add(serverField);

		// row 1
		grid.add(serverPortLabel);
		grid.add(serverPortField);

		// row 2
		grid.add(usernameLabel);
		grid.add(usernameField);
	
		// row 3
		grid.add(passwordLabel);
		grid.add(passwordField);

		// row 4
		grid.add(passwordretypeLabel);
		grid.add(passwordretypeField);

		// row 5
		grid.add(emailLabel);
		grid.add(emailField);

		// Warning label
		JLabel logLabel = new JLabel("<html><body><p><font size=\"-2\"> Przy logowaniu będą zapisywane informacje, które będą identyfikować <br>Twój komputer w internecie w celu zapobiegania nadużyciom <br>(np. przy próbie odgadnięcia hasła w celu włamania się na konto <br>lub tworzeniu wielu kont w celu sprawiania problemów). <br>Ponadto wszystkie zdarzenia i akcje, które wydarzą się w grze <br>(np. rozwiązywanie zadań, atakowanie potworów) <br>są umieszczane w dzienniku. Te informacje będą wykorzystywane <br>w celu analizy luk i czasami w sprawach nadużyć.</font></p></body></html>");
		// Add a bit more empty space around it
		logLabel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		logLabel.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(logLabel, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));
		
		createAccountButton.setAlignmentX(RIGHT_ALIGNMENT);
		contentPane.add(createAccountButton);

		// CreateAccountDialog
		this.setTitle("Utwórz Nowe Konto");
		this.setResizable(false);
		// required on Compiz
		this.pack(); 

		usernameField.requestFocusInWindow();
		if (owner != null) {
			owner.setEnabled(false);
			this.setLocationRelativeTo(owner);
		}
	}

	private void createAccountButton_actionPerformed(final ActionEvent e,
			final boolean saveLoginBoxStatus) {
		final String accountUsername = usernameField.getText();
		final String password = new String(passwordField.getPassword());

		// If this window isn't enabled, we shouldn't act.
		if (!this.isEnabled()) {
			return;
		}

		final boolean ok = checkFields();

		if (!ok) {
			return;
		}

		final String email = emailField.getText();
		final String server = serverField.getText();
		int port = 32160;

		// standalone check
		if (client == null) {
			JOptionPane.showMessageDialog(this,
					"Konto nie zostało utworzone!");
			return;
		}
		// port couldn't be accessed from inner class
		final int finalPort; 
		final ProgressBar progressBar = new ProgressBar(this);

		try {
			port = Integer.parseInt(serverPortField.getText());
		} catch (final Exception ex) {
			JOptionPane.showMessageDialog(getOwner(),
					"Niewłaściwy numer portu. Spróbuj ponownie.",
					"Niewłaściwy Port", JOptionPane.WARNING_MESSAGE);
			return;
		}
		finalPort = port;

		/* separate thread for connection process added by TheGeneral */
		// run the connection process in separate thread
		final Thread m_connectionThread = new Thread() {

			@Override
			public void run() {
				// initialize progress bar
				progressBar.start(); 
				// disable this screen when attempting to connect
				setEnabled(false); 
	

				try {
					client.connect(server, finalPort);
					// for each major connection milestone call step()
					progressBar.step(); 
				} catch (final Exception ex) {
					// if something goes horribly just cancel the progress bar
					progressBar.cancel(); 
					setEnabled(true);
					JOptionPane.showMessageDialog(
							getOwner(),
							"Nie można się połączyć z serwerem w celu utworzenia konta. Serwer może nie działać, a jeśli korzystasz z innego serwera " +
							"to sprawdź czy poprawnie wpisałeś nazwę i numer portu.");

					logger.error(ex, ex);

					return;
				}
				final Window owner = getOwner();
				try {
					if (server.length() > 0 && accountUsername.length() > 0
					&& password.length() > 0 && email.length() > 0) {

						final AccountResult result = client.createAccount(
								accountUsername, password, email);
						if (result.failed()) {
							/*
							 * If the account can't be created, show an error
							 * message and don't continue.
							 */
							progressBar.cancel();
							setEnabled(true);
							JOptionPane.showMessageDialog(owner,
									result.getResult().getText(),
									"Nie powiodło się tworzenie konta",
									JOptionPane.ERROR_MESSAGE);
						} else {

							/*
							 * Print username returned by server, as server can
							 * modify it at will to match account names rules.
							 */

							progressBar.step();
							progressBar.finish();

							client.setAccountUsername(accountUsername);
							client.setCharacter(accountUsername);

							/*
							 * Once the account is created, login into server.
							 */
							client.login(accountUsername, password);
							progressBar.step();
							progressBar.finish();

							setEnabled(false);
							if (owner != null) {
								owner.setVisible(false);
								owner.dispose();
							}

							stendhal.setDoLogin();
						}
					} else {
						progressBar.cancel(); 
						setEnabled(true);
						JOptionPane.showMessageDialog(
								getOwner(),
								"Nie wypełniłeś wymaganych pól.");

					}
				} catch (final TimeoutException e) {
					progressBar.cancel();
					setEnabled(true);
					JOptionPane.showMessageDialog(
							owner,
							"Nie można się połączyć z serwerem w celu utworzenia konta. Serwer może nie działać lub mogłeś wprowadzić niewłaściwą nazwę serwera i numer portu.",
							"Błąd Tworzenia Konta", JOptionPane.ERROR_MESSAGE);
				} catch (final InvalidVersionException e) {
					progressBar.cancel();
					setEnabled(true);
					JOptionPane.showMessageDialog(
							owner,
							"Uruchomiłeś starszą wersję FaumoniaOnline. Proszę zaktualizuj swoją wersje",
							"Starsza wersja", JOptionPane.ERROR_MESSAGE);
				} catch (final BannedAddressException e) {
					progressBar.cancel();
					setEnabled(true);
					JOptionPane.showMessageDialog(
							owner,
							"Twoje IP zostało zablokowane. Jeżeli nie zgadzasz się z decyzją to skontaktuj się z nami na https://www.facebook.com/faumoniaonline",
							"Zablokowane IP", JOptionPane.ERROR_MESSAGE);
				} catch (final LoginFailedException e) {
					progressBar.cancel();
					setEnabled(true);
					JOptionPane.showMessageDialog(owner, e.getMessage(),
							"Nieudane logowanie", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		};
		m_connectionThread.start();
	}

	/**
	 * Runs field checks, to, ex. confirm the passwords correct, etc.
	 * @return if no error found
	 */
	private boolean checkFields() {
		//
		// Check the password
		//
		final String password = new String(passwordField.getPassword());
		final String passwordretype = new String(
				passwordretypeField.getPassword());
		final Window owner = getOwner();
		if (!password.equals(passwordretype)) {
			JOptionPane.showMessageDialog(owner,
					"Hasła nie pasują do siebie. Powtórz oba hasła.",
					"Hasło nie pasuje", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		//
		// Password strength
		//
		final boolean valPass = validatePassword(usernameField.getText(), password);
		if (!valPass) {
			if (badPasswordReason != null) {
				// didn't like the password for some reason, show a dialog and
				// try again
				final int i = JOptionPane.showOptionDialog(owner, badPasswordReason,
						"Niewałaściwe Hasło", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, null, 1);

				if (i == JOptionPane.NO_OPTION) {
					return false;
				}
			} else {
				return false;
			}
		}

		//
		// Check the email
		//
		final String email = (emailField.getText()).trim();
		if  (!validateEmail(email)){
	        final String warning = badEmailReason + "Adres email jest tylko dla administratorów, aby mogli sprawdzić własciciela konta.\nJeżeli nie chcesz podać to nie będziesz mógł zmienić hasła do konta. Na przykład:\n- Zapomniałeś hasło.\n- Inny gracz w jakiś sposób zdobył twoje hasło i zmienił je.\nCzy chcesz kontynuować?";
           	final int i = JOptionPane.showOptionDialog(owner, warning, badEmailTitle,
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, null, 1);
            if (i != 0) {
			    // no, let me type a valid email
                return false;
			} 
			// yes, continue anyway		
		}
		return true;
	}

	private boolean validateEmail(final String email) {
		if  (email.length() == 0){
		    badEmailTitle = "Adres email jest pusty";
			badEmailReason = "Nie wprowadziłeś adresu email.\n";
		    return false;
		} else {
   		    if (!email.contains("@") || !email.contains(".") || (email.length() <= 5)) {
		        badEmailTitle =  "Błędny adres email?";
		        badEmailReason = "Adres email, który wpisałeś jest prawdopodobnie błędny.\n";
		        return false;
			}
		}
		return true;
	}

	/**
	 * Used to preview the CreateAccountDialog.
	 * @param args 
	 */
	public static void main(final String[] args) {
		new CreateAccountDialog(null, null);
	}

	private static class LowerCaseLetterDocument extends PlainDocument {
		private static final long serialVersionUID = -5123268875802709841L;

		@Override
		public void insertString(final int offs, final String str, final AttributeSet a)
				throws BadLocationException {
			final String lower = str.toLowerCase(Locale.ENGLISH);
			boolean ok = true;
			for (int i = lower.length() - 1; i >= 0; i--) {
				final char chr = lower.charAt(i);
			}
			if (ok) {
				super.insertString(offs, lower, a);
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}

	public boolean validatePassword(final String username, final String password) {
		if (password.length() > 5) {

			// check for all numbers
			boolean allNumbers = true;
			try {
				Integer.parseInt(password);
			} catch (final Exception e) {
				allNumbers = false;
			}
			if (allNumbers) {
				badPasswordReason = "W swoim haśle użyłeś tylko liczb. To nie jest bezpieczna metoda.\n"
						+ " Czy chcesz użyć tego hasła?";
			}

			// check for username
			boolean hasUsername = false;
			if (password.contains(username)) {
				hasUsername = true;
			}

			if (!hasUsername) {
				// now we'll do some more checks to see if the password
				// contains more than three letters of the username
				debug("Checking if password contains a derivative of the username, trimming from the back...");
				final int min_user_length = 3;
				for (int i = 1; i < username.length(); i++) {
					final String subuser = username.substring(0, username.length()
							- i);
					debug("\tsprawdzam \"" + subuser + "\"...");
					if (subuser.length() <= min_user_length) {
						break;
					}

					if (password.contains(subuser)) {
						hasUsername = true;
						debug("Hasło zawiera imię wojownika!");
						break;
					}
				}

				if (!hasUsername) {
					// now from the end of the password..
					debug("Checking if password contains a derivative of the username, trimming from the front...");
					for (int i = 0; i < username.length(); i++) {
						final String subuser = username.substring(i);
						debug("\tsprawdzam \"" + subuser + "\"...");
						if (subuser.length() <= min_user_length) {
							break;
						}
						if (password.contains(subuser)) {
							hasUsername = true;
							debug("Hasło zawiera imię wojownika!");
							break;
						}
					}
				}
			}

			if (hasUsername) {
				badPasswordReason = "W haśle użyłeś imienia wojownika lub jest do niej podobna. To jest kiepskie zabezpieczenie konta.\n"
						+ " Jesteś pewien, że chcesz użyć tego hasła?";
				return false;
			}

		} else {
			final String text = "Hasło, które wprowadziłeś jest za krótkie. Musi się składać z minimum 6 znaków.";
			if (isVisible()) {
				JOptionPane.showMessageDialog(getOwner(), text);
			} else {
				logger.warn(text);
			}
			return false;
		}

		return true;
	}

	/**
	 * Prints text only when running stand-alone.
	 * @param text 
	 */
	public void debug(final String text) {

		if (client == null) {
			logger.debug(text);
		}
	}
}
