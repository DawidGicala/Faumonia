/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.update;

import java.text.NumberFormat;

import javax.swing.JOptionPane;

/**
 * Dialogboxes used during updating..
 * 
 * @author hendrik
 */
public class UpdateGUIDialogs {

	private static final String DIALOG_TITLE = "Aktualizacja " + ClientGameConfiguration.get("GAME_NAME");

	/**
	 * Asks the user to accept an update.
	 * 
	 * @param updateSize
	 *            size of the files to download
	 * @param update
	 *            true, if it is an update, false on first install
	 * @return true if the update should be performed, false otherwise
	 */
	public static boolean askForDownload(final int updateSize, final boolean update) {
		// format number, only provide decimal digits on very small sizes
		float size = (float) updateSize / 1024;
		if (size > 10) {
			size = (int) size;
		}
		final String sizeString = NumberFormat.getInstance().format(size);

		// ask user
		int resCode;
		if (update) {
			resCode = JOptionPane.showConfirmDialog(null,
					new SelectableLabel("Jest już nowa wersja, która zajmuje " + sizeString + " KB.\r\n"
							+ "Czy " + ClientGameConfiguration.get("GAME_NAME") + " powinna zostać zaktualizowana?"), DIALOG_TITLE,
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		} else {
			resCode = JOptionPane.showConfirmDialog(null,
					new SelectableLabel("Potrzebne jest pobranie dodatkowych plików, które zajmują "
							+ sizeString + " KB.\r\n"
							+ "Czy " + ClientGameConfiguration.get("GAME_NAME") + " powinna zostać zainstalowana?"),
					DIALOG_TITLE, JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		}

		return (resCode == JOptionPane.YES_OPTION);
	}

	/**
	 * Displays a message box.
	 * 
	 * @param message
	 *            message to display
	 */
	public static void messageBox(final String message) {
		JOptionPane.showMessageDialog(null, new SelectableLabel(message), DIALOG_TITLE,
				JOptionPane.INFORMATION_MESSAGE);
	}

}
