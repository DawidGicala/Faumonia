/* $Id: ClickModeAction.java,v 1.3 2010/09/24 20:52:18 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.actions;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.gui.chatlog.StandardEventLine;
import games.stendhal.client.gui.wt.core.WtWindowManager;

/**
 * switches between single click and double click
 *
 * @author hendrik
 */
public class ClickModeAction implements SlashAction {

	/**
	 * Execute a chat command.
	 * 
	 * @param params
	 *            The formal parameters.
	 * @param remainder
	 *            Line content after parameters.
	 * 
	 * @return <code>true</code> if was handled.
	 */
	public boolean execute(String[] params, String remainder) {
		boolean doubleClick = Boolean.parseBoolean(WtWindowManager.getInstance().getProperty("ui.doubleclick", "false"));
		doubleClick = !doubleClick;
		WtWindowManager.getInstance().setProperty("ui.doubleclick", Boolean.toString(doubleClick));
		if (doubleClick) {
			ClientSingletonRepository.getUserInterface().addEventLine(new StandardEventLine("Tryb klikania jest teraz ustawiony na podwójne klikanie."));
		} else {
			ClientSingletonRepository.getUserInterface().addEventLine(new StandardEventLine("Tryb klikania jest teraz ustawiony na pojedyńcze klikanie."));
		}
		return true;
	}

	/**
	 * Get the maximum number of formal parameters.
	 * 
	 * @return The parameter count.
	 */
	public int getMaximumParameters() {
		return 0;
	}

	/**
	 * Get the minimum number of formal parameters.
	 * 
	 * @return The parameter count.
	 */
	public int getMinimumParameters() {
		return 0;
	}

}
