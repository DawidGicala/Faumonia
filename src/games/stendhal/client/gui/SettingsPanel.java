/* $Id: SettingsPanel.java,v 1.18 2012/12/02 18:14:18 kiheru Exp $ */
/***************************************************************************
 *                  (C) Copyright 2005-2012 - Stendhal                     *
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

import games.stendhal.client.actions.SlashAction;
import games.stendhal.client.actions.SlashActionRepository;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * Main client menu.
 */
class SettingsPanel extends JButton {

	/** Empty space above and below the separators in the popup menu. */
	private static final int SEPARATOR_MARGIN = 8;

	SettingsPanel() {
		setText("Menu");
		setIcon(createArrowIcon(true));
		setSelectedIcon(createArrowIcon(false));
		setFocusable(false);

		final JPopupMenu menu = new JPopupMenu();
		final GridBagLayout layout = new GridBagLayout();
		menu.setLayout(layout);
		final GridBagConstraints c = new GridBagConstraints();

		Column column = new Column(menu, c);
		createFaumoniaMenu(column);

		addSeparator(menu, c);

		column = new Column(menu, c);
		createGameDatabaseMenu(column);

		addSeparator(menu, c);

		column = new Column(menu, c);
		createLevelingMenu(column);

		addSeparator(menu, c);

		column = new Column(menu, c);
		createQuestsMenu(column);

		addSeparator(menu, c);

		column = new Column(menu, c);
		createToolsMenu(column);

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				menu.show(SettingsPanel.this,
						getWidth() - menu.getPreferredSize().width,
						getHeight());
			}
		});

		WindowUtils.watchFontSize(menu);
	}

	private void addSeparator(final JComponent container, final GridBagConstraints c) {
		c.gridy = 0;
		c.gridx++;
		c.gridheight = GridBagConstraints.REMAINDER;

		final JComponent separator = new JSeparator(SwingConstants.VERTICAL);
		final Insets tmpInsets = c.insets;
		c.insets = new Insets(SEPARATOR_MARGIN, 0, SEPARATOR_MARGIN, 0);
		container.add(separator, c);

		c.gridheight = 1;
		c.insets = tmpInsets;
	}

	private Icon createArrowIcon(final boolean etched) {
		final BufferedImage image =
				new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = image.createGraphics();

		final Color highLight;
		final Color shadow;
		final Style style = StyleUtil.getStyle();

		if (style != null) {
			highLight = style.getHighLightColor();
			shadow = style.getShadowColor();
		} else {
			highLight = Color.WHITE;
			shadow = Color.BLACK;
		}

		final Color nwColor;
		final Color seColor;
		if (etched) {
			nwColor = shadow;
			seColor = highLight;
		} else {
			nwColor = highLight;
			seColor = shadow;
		}

		g.setColor(nwColor);
		g.drawPolyline(new int[] { 7, 0, 13 }, new int[] { 11, 4, 4 }, 3);
		g.setColor(seColor);
		g.drawLine(7, 11, 14, 4);
		g.dispose();

		return new ImageIcon(image);
	}

	/**
	 * Official Faumonia links.
	 */
	private void createFaumoniaMenu(final Column column) {
		column.addComponent(createMenuTitle("Faumonia"));

		column.addComponent(createMenuItem("Strona główna", "faumonia_www"));
		column.addComponent(createMenuItem("Aktualności", "aktualnosci"));
		column.addComponent(createMenuItem("Poradniki", "poradniki"));
		column.addComponent(createMenuItem("Discord", "faumonia_discord"));
		column.addComponent(createMenuItem("Hall of Fame", "hall_of_fame"));
		column.addComponent(createMenuItem("Zasłużeni", "zasluzeni"));
	}

	/**
	 * Main game database pages.
	 */
	private void createGameDatabaseMenu(final Column column) {
		column.addComponent(createMenuTitle("Baza gry"));

		column.addComponent(createMenuItem("Potwory", "potwory"));
		column.addComponent(createMenuItem("Przedmioty", "przedmioty"));
		column.addComponent(createMenuItem("Sklepy", "sklepy"));
		column.addComponent(createMenuItem("Atlas map", "mapy"));
		column.addComponent(createMenuItem("Wszystkie expowiska", "expowiska"));
		column.addComponent(createMenuItem("Wszystkie zadania", "zadania"));
	}

	/**
	 * Direct links to leveling guides.
	 */
	private void createLevelingMenu(final Column column) {
		column.addComponent(createMenuTitle("Expowiska"));

		column.addComponent(createMenuItem("Poziomy 0-20", "exp_0_20"));
		column.addComponent(createMenuItem("Poziomy 20-60", "exp_20_60"));
		column.addComponent(createMenuItem("Poziomy 60-120", "exp_60_120"));
		column.addComponent(createMenuItem("Poziomy 120-200", "exp_120_200"));
		column.addComponent(createMenuItem("Poziomy 200-300", "exp_200_300"));
		column.addComponent(createMenuItem("Poziomy 300+", "exp_300_plus"));
	}

	/**
	 * Direct links to the most useful quest lists.
	 */
	private void createQuestsMenu(final Column column) {
		column.addComponent(createMenuTitle("Zadania"));

		column.addComponent(createMenuItem("Mistrzowie Gry", "quest_masters"));
		column.addComponent(createMenuItem("Miasto Semos", "quest_semos"));
		column.addComponent(createMenuItem("Stolica Ados", "quest_ados"));
		column.addComponent(createMenuItem("Południowe miasta", "quest_south"));
		column.addComponent(createMenuItem("Odnawialne - potwory", "quest_repeatable"));
		column.addComponent(createMenuItem("Najsilniejsze przedmioty", "quest_best_items"));
	}

	/**
	 * Internal client tools.
	 */
	private void createToolsMenu(final Column column) {
		column.addComponent(createMenuTitle("Narzędzia"));

		column.addComponent(createMenuItem("Dziennik zadań", "travellog"));
		column.addComponent(createMenuItem("Ustawienia", "settings"));
		column.addComponent(createMenuItem("Wojownicy Online", "who"));
		column.addComponent(createMenuItem("Komendy", "help"));
	}

	private JComponent createMenuTitle(final String title) {
		final JLabel label = new JLabel(title);
		label.setBorder(BorderFactory.createEmptyBorder(5, 4, 10, 0));
		return label;
	}

	private JMenuItem createMenuItem(final String name, final String action) {
		final JMenuItem item = new VariableWidthMenuItem(name);
		item.addActionListener(new CommandActionListener(action));
		item.setBorder(null);
		return item;
	}

	private static class CommandActionListener implements ActionListener {
		private final SlashAction action;

		CommandActionListener(final String identifier) {
			action = SlashActionRepository.get(identifier);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (action != null) {
				action.execute(null, null);
			}
		}
	}

	private static class VariableWidthMenuItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		private final Dimension pref;

		VariableWidthMenuItem(final String name) {
			super(name);
			pref = super.getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			return pref;
		}
	}

	private static class Column {
		private final JComponent container;
		private final GridBagConstraints c;

		Column(final JComponent container, final GridBagConstraints c) {
			this.container = container;
			this.c = c;
			c.gridy = 0;
			c.gridx++;
			c.fill = GridBagConstraints.BOTH;
		}

		void addComponent(final JComponent component) {
			container.add(component, c);
			c.gridy++;
		}
	}
}
