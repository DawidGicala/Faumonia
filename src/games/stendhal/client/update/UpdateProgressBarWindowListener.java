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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;

/**
 * Ask for confirmation and exists the JVM if the user closes the progress bar window.
 */
public class UpdateProgressBarWindowListener implements WindowListener {

	public void windowOpened(WindowEvent e) {
		// do nothing
	}

	/**
	 * Ask for confirmation and closes the JVM
	 */
	public void windowClosing(WindowEvent e) {
		int result = JOptionPane.showConfirmDialog(e.getWindow(),
				"Czy chcesz przerwać pobieranie?", "Potwierdzenie",
				JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION) {
			e.getWindow().dispose();
			System.exit(1);
		}
	}

	public void windowClosed(WindowEvent e) {
		// do nothing
	}

	public void windowIconified(WindowEvent e) {
		// do nothing
	}

	public void windowDeiconified(WindowEvent e) {
		// do nothing
	}

	public void windowActivated(WindowEvent e) {
		// do nothing
	}

	public void windowDeactivated(WindowEvent e) {
		// do nothing
	}
}
