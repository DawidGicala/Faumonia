/* $Id: StendhalFirstScreen.java,v 1.84 2012/09/13 19:05:28 kiheru Exp $ */
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
package games.stendhal.client.gui;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.stendhal;
import games.stendhal.client.gui.login.CreateAccountDialog;
import games.stendhal.client.gui.login.LoginDialog;
import games.stendhal.client.sprite.DataLoader;
import games.stendhal.client.update.ClientGameConfiguration;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Summary description for LoginGUI.
 *
 */
@SuppressWarnings("serial")
public class StendhalFirstScreen extends JFrame {
	private static final long serialVersionUID = -7825572598938892220L;
	private static final int BUTTON_WIDTH = 220;
	private static final int BUTTON_HEIGHT = 32;

	/** Name of the font used for the html areas. Should match the file name without .ttf */
	private static final String FONT_NAME = "AntykwaTorunska";

	private final StendhalClient client;

	private final Image background;

	private JButton loginButton;
	private JButton createAccountButton;
	private JButton helpButton;
	private JButton wwwButton;
	private JButton creditButton;
	private JButton ruleButton;
	
	static {
		// This is the initial window, when loaded at all.
		Initializer.init();
	}

	/**
	 * Creates the first screen.
	 *
	 * @param client
	 *            StendhalClient
	 */
	public StendhalFirstScreen(final StendhalClient client) {
		super(detectScreen());
		this.client = client;
		client.setSplashScreen(this);

		final URL url = DataLoader.getResource(ClientGameConfiguration.get("GAME_SPLASH_BACKGROUND"));
		final ImageIcon imageIcon = new ImageIcon(url);
		background = imageIcon.getImage();

		initializeComponent();

		this.setVisible(true);
	}

	/**
	 * Detect the preferred screen by where the mouse is the moment the method
	 * is called. This is for multi-monitor support.
	 *
	 * @return GraphicsEnvironment of the current screen
	 */
	private static GraphicsConfiguration detectScreen() {
		PointerInfo pointer = MouseInfo.getPointerInfo();
		if (pointer != null) {
			return pointer.getDevice().getDefaultConfiguration();
		}
		return null;
	}

	/**
	 * Setup the window contents.
	 */
	private void initializeComponent() {
		setContentPane(new JComponent() {
			{
				setOpaque(true);
				setPreferredSize(new Dimension(640, 480));
			}

			@Override
			public void paintComponent(final Graphics g) {
				g.drawImage(background, 0, 0, this);
			}
		});

		Font font = new Font(FONT_NAME, Font.PLAIN, 16);

		//
		// loginButton
		//
		loginButton = new JButton();
		loginButton.setFont(font);
		loginButton.setText("Zaloguj do "
				+ ClientGameConfiguration.get("GAME_NAME"));
		loginButton.setMnemonic(KeyEvent.VK_L);
		loginButton.setToolTipText("Naciśnij ten przycisk, aby zalogować sie do serwera "
				+ ClientGameConfiguration.get("GAME_NAME") + ".");
		loginButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				login();
			}
		});
		//
		// createAccountButton
		//
		createAccountButton = new JButton();
		createAccountButton.setFont(font);
		createAccountButton.setText("Utwórz konto");
		createAccountButton.setMnemonic(KeyEvent.VK_U);
		createAccountButton.setToolTipText("Naciśnij ten przycisk, aby utworzyć konto na serwerze "
				+ ClientGameConfiguration.get("GAME_NAME") + ".");
		createAccountButton.setEnabled(true);
		createAccountButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				createAccount();
			}
		});
		//
		// helpButton
		//
		helpButton = new JButton();
		helpButton.setFont(font);
		helpButton.setText("Pomoc");
		helpButton.setMnemonic(KeyEvent.VK_P);
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				showHelp();
			}
		});
		//
		// creaditButton
		//
		creditButton = new JButton();
		creditButton.setFont(font);
		creditButton.setText("Wyróżnieni");
		creditButton.setMnemonic(KeyEvent.VK_W);
		creditButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				showCredits();
			}
		});
		//
		// ruleButton
		//
		ruleButton = new JButton();
		ruleButton.setFont(font);
		ruleButton.setText("Podstawowe zasady gry");
		ruleButton.setMnemonic(KeyEvent.VK_Z);
		ruleButton.setToolTipText("Naciśnij ten przycisk, aby poznać podstawowe zasady gry "
				+ ClientGameConfiguration.get("GAME_NAME") + ".");
		ruleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				showRules();
			}
		});
		//
		// wwwButton
		//
		wwwButton = new JButton();
		wwwButton.setFont(font);
		wwwButton.setText("Odwiedź stronę");
		wwwButton.setMnemonic(KeyEvent.VK_O);
		wwwButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				showWww();
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				System.exit(0);
			}
		});

		//
		// contentPane
		//
		final Container contentPane = this.getContentPane();
		contentPane.setLayout(null);

		int x = (background.getWidth(null) - BUTTON_WIDTH) / 2; 
		addComponent(contentPane, loginButton, x, 220, BUTTON_WIDTH, BUTTON_HEIGHT);
		addComponent(contentPane, createAccountButton, x, 270, BUTTON_WIDTH, BUTTON_HEIGHT);
		addComponent(contentPane, ruleButton, x, 310, BUTTON_WIDTH, BUTTON_HEIGHT);
		addComponent(contentPane, helpButton, x, 350, BUTTON_WIDTH, BUTTON_HEIGHT);
		addComponent(contentPane, wwwButton, x, 390, BUTTON_WIDTH, BUTTON_HEIGHT);
		addComponent(contentPane, creditButton, x, 430, BUTTON_WIDTH, BUTTON_HEIGHT);

		getRootPane().setDefaultButton(loginButton);

		//
		// LoginGUI
		//
		setTitle(ClientGameConfiguration.get("GAME_NAME") + " "
				+ stendhal.VERSION
				+ " - darmowa gra MMORPG - www.faumoniaonline.pl");
		this.setResizable(false);

		final URL url = DataLoader.getResource(ClientGameConfiguration.get("GAME_ICON"));
		this.setIconImage(new ImageIcon(url).getImage());
		pack();
	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		loginButton.setEnabled(b);
		createAccountButton.setEnabled(b);
		helpButton.setEnabled(b);
		creditButton.setEnabled(b);
	}

	private void login() {
		new LoginDialog(StendhalFirstScreen.this, client).setVisible(true);
	}

	private void showCredits() {
		new CreditsDialog(StendhalFirstScreen.this);
	}

	private void showRules() {
		BareBonesBrowserLaunch.openURL("http://www.faumoniaonline.pl/regulamin");
	}

	private void showHelp() {
		BareBonesBrowserLaunch.openURL("http://www.faumoniaonline.pl/");
	}

	private void showWww() {
		BareBonesBrowserLaunch.openURL("http://www.faumoniaonline.pl/");
	}

	/**
	 * Opens the create account dialog after checking the server version.
	 */
	public void createAccount() {
		new CreateAccountDialog(StendhalFirstScreen.this, client);
	}

	/** Adds Component Without a Layout Manager (Absolute Positioning).
	 * @param container
	 * @param c
	 * @param x
	 * @param y
	 * @param width
	 * @param height */
	private void addComponent(final Container container, final Component c, final int x, final int y,
			final int width, final int height) {
		c.setBounds(x, y, width, height);
		container.add(c);
	}

}
