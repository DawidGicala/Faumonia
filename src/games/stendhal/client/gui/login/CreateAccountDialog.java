/* $Id: CreateAccountDialog.java,v 1.15 2026/03/05 21:08:14 davvids Exp $ */
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
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
	private JCheckBox marketingConsentBox;
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

		marketingConsentBox = new JCheckBox("Zgoda marketingowa *");
		marketingConsentBox.setSelected(false);
		marketingConsentBox.setToolTipText(
				"Zgoda na wykorzystanie adresu e-mail w celach marketingowych Faumonii.");

		// createAccountButton
		createAccountButton = new JButton();
		createAccountButton.setText("Utwórz konto");
		createAccountButton.setMnemonic(KeyEvent.VK_U);
		this.rootPane.setDefaultButton(createAccountButton);
		createAccountButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				createAccountButton_actionPerformed(e, false);
			}
		});

		// contentPane
		int padding = SBoxLayout.COMMON_PADDING;
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new SBoxLayout(SBoxLayout.VERTICAL, padding));
		contentPane.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

		JComponent grid = new JComponent() {
			private static final long serialVersionUID = 1L;
		};
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

		// row 6
		grid.add(new JLabel(""));
		grid.add(marketingConsentBox);

		// Warning label
		JLabel logLabel = new JLabel(
				"<html><body><p><font size=\"-2\">"
				+ "<b>* Zgoda marketingowa</b>: e-mail może być użyty w celach"
				+ "<br>marketingowych Faumonii, m.in. do informacji o grze,"
				+ "<br>grup odbiorców i remarketingu, np. Google Ads i YouTube."
				+ "<br>Szczegóły: https://faumonia.pl/polityka-prywatnosci/"
				+ "<br>Rezygnacja: administrator w grze, administrator na Discordzie"
				+ "<br>albo e-mail: kontakt@faumonia.pl."
				+ "<br><br>"
				+ "Przy logowaniu zapisujemy dane techniczne i zdarzenia w grze"
				+ "<br>w celu bezpieczeństwa, ochrony kont i zapobiegania nadużyciom."
				+ "<br>Szczegóły opisuje polityka prywatności."
				+ "</font></p></body></html>");

		logLabel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		logLabel.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(logLabel, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));

		createAccountButton.setAlignmentX(RIGHT_ALIGNMENT);
		contentPane.add(createAccountButton);

		// CreateAccountDialog
		this.setTitle("Utwórz nowe konto");
		this.setResizable(false);
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
		final boolean marketingConsent = marketingConsentBox.isSelected();
		final String server = serverField.getText();
		int port = 32160;

		// standalone check
		if (client == null) {
			JOptionPane.showMessageDialog(this,
					"Konto nie zostało utworzone!");
			return;
		}

		final int finalPort;
		final ProgressBar progressBar = new ProgressBar(this);

		try {
			port = Integer.parseInt(serverPortField.getText());
		} catch (final Exception ex) {
			JOptionPane.showMessageDialog(getOwner(),
					"Nieprawidłowy numer portu. Sprawdź go i spróbuj ponownie.",
					"Nieprawidłowy port", JOptionPane.WARNING_MESSAGE);
			return;
		}
		finalPort = port;

		final Thread m_connectionThread = new Thread() {

			@Override
			public void run() {
				progressBar.start();
				setEnabled(false);

				try {
					client.connect(server, finalPort);
					progressBar.step();
				} catch (final Exception ex) {
					progressBar.cancel();
					setEnabled(true);
					JOptionPane.showMessageDialog(
							getOwner(),
							"Nie można połączyć się z serwerem, aby utworzyć konto. "
							+ "Sprawdź adres serwera, port i połączenie z internetem.");

					logger.error(ex, ex);
					return;
				}

				final Window owner = getOwner();
				try {
					if (server.length() > 0 && accountUsername.length() > 0
							&& password.length() > 0 && email.length() > 0) {

						final AccountResult result = client.createAccount(
								accountUsername, password, email, marketingConsent);

						if (result.failed()) {
							progressBar.cancel();
							setEnabled(true);
							JOptionPane.showMessageDialog(owner,
									result.getResult().getText(),
									"Nie powiodło się tworzenie konta",
									JOptionPane.ERROR_MESSAGE);
						} else {
							progressBar.step();
							progressBar.finish();

							client.setAccountUsername(accountUsername);
							client.setCharacter(accountUsername);

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
							"Nie można połączyć się z serwerem, aby utworzyć konto. Sprawdź adres serwera, port i połączenie z internetem.",
							"Błąd tworzenia konta", JOptionPane.ERROR_MESSAGE);
				} catch (final InvalidVersionException e) {
					progressBar.cancel();
					setEnabled(true);
					JOptionPane.showMessageDialog(
							owner,
							"Ta wersja klienta nie jest zgodna z serwerem. Pobierz aktualną wersję gry.",
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
		final String password = new String(passwordField.getPassword());
		final String passwordretype = new String(passwordretypeField.getPassword());
		final Window owner = getOwner();

		if (!password.equals(passwordretype)) {
			JOptionPane.showMessageDialog(owner,
					"Wpisane hasła różnią się. Wprowadź to samo hasło w obu polach.",
					"Hasła nie są zgodne", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		final boolean valPass = validatePassword(usernameField.getText(), password);
		if (!valPass) {
			if (badPasswordReason != null) {
				final int i = JOptionPane.showOptionDialog(owner, badPasswordReason,
						"Słabe hasło", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, null, 1);

				if (i == JOptionPane.NO_OPTION) {
					return false;
				}
			} else {
				return false;
			}
		}

		final String email = (emailField.getText()).trim();
		if (!validateEmail(email)) {
			final String warning = badEmailReason
					+ "Poprawny adres e-mail pomaga zweryfikować właściciela konta.\n"
					+ "Bez niego odzyskanie dostępu do konta może być trudniejsze, np. gdy:\n"
					+ "- zapomnisz hasła;\n"
					+ "- ktoś przejmie konto i zmieni hasło.\n"
					+ "Czy chcesz kontynuować?";

			final int i = JOptionPane.showOptionDialog(owner, warning, badEmailTitle,
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
					null, null, 1);

			if (i != 0) {
				return false;
			}
		}

		return true;
	}

	private boolean validateEmail(final String email) {
		if (email.length() == 0) {
			badEmailTitle = "Brak adresu e-mail";
			badEmailReason = "Nie podano adresu e-mail.\n";
			return false;
		} else {
			if (!email.contains("@") || !email.contains(".") || (email.length() <= 5)) {
				badEmailTitle = "Sprawdź adres e-mail";
				badEmailReason = "Wpisany adres e-mail wygląda na nieprawidłowy.\n";
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

			boolean allNumbers = true;
			try {
				Integer.parseInt(password);
			} catch (final Exception e) {
				allNumbers = false;
			}
			if (allNumbers) {
				badPasswordReason = "Hasło zawiera wyłącznie cyfry, przez co łatwiej je odgadnąć.\n"
						+ " Czy chcesz użyć tego hasła?";
			}

			boolean hasUsername = false;
			if (password.contains(username)) {
				hasUsername = true;
			}

			if (!hasUsername) {
				debug("Checking if password contains a derivative of the username, trimming from the back...");
				final int min_user_length = 3;
				for (int i = 1; i < username.length(); i++) {
					final String subuser = username.substring(0, username.length() - i);
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
				badPasswordReason = "Hasło zawiera login lub jego część, przez co łatwiej je odgadnąć.\n"
						+ " Jesteś pewien, że chcesz użyć tego hasła?";
				return false;
			}

		} else {
			final String text = "Hasło jest za krótkie. Wpisz co najmniej 6 znaków.";
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