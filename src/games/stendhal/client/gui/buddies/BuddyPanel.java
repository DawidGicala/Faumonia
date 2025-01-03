/* $Id: BuddyPanel.java,v 1.19 2012/12/02 21:38:46 kiheru Exp $ */
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
package games.stendhal.client.gui.buddies;

import games.stendhal.client.gui.MousePopupAdapter;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;

/**
 * JList that can show popup menues for buddies. Use <code>BuddyListModel</code>
 * with this.
 */
class BuddyPanel extends JList {
	/**
	 * serial version uid.
	 */
	private static final long serialVersionUID = -1728697267036233233L;

	/**
	 * The amount of pixels that popup menus will be shifted up and left from
	 * the clicking point.
	 */
	private static final int POPUP_OFFSET = 10;

	/**
	 * Create a new BuddyList.
	 * 
	 * @param model associated list model
	 */
	protected BuddyPanel(final BuddyListModel model) {
		super(model);
		setCellRenderer(new BuddyLabel());
		setOpaque(false);
		this.setFocusable(false);
		this.addMouseListener(new BuddyPanelMouseListener());
	}
	
	@Override
	public Font getFont() {
		// The only real for is that of the cell renderer
		ListCellRenderer renderer = getCellRenderer();
		if (renderer instanceof Component) {
			return ((Component) renderer).getFont();
		}
		
		return super.getFont();
	}

	@Override
	public void setFont(Font font) {
		// Pass the font change to the cell renderer
		ListCellRenderer renderer = getCellRenderer();
		if (renderer instanceof Component) {
			Component comp = (Component) renderer;
			comp.setFont(font);
		}
	}
	
	/**
	 * MouseListener for triggering the buddy list popup menus.
	 */
	private class BuddyPanelMouseListener extends MousePopupAdapter {
		@Override
		protected void showPopup(final MouseEvent e) {
				int index = BuddyPanel.this.locationToIndex(e.getPoint());
				Object obj = BuddyPanel.this.getModel().getElementAt(index);
				if (obj instanceof Buddy) {
					Buddy buddy = (Buddy) obj;
					final JPopupMenu popup = new BuddyLabelPopMenu(buddy.getName(), buddy.isOnline());
					popup.show(e.getComponent(), e.getX() - POPUP_OFFSET, e.getY() - POPUP_OFFSET);
			}
		}
	}
}
