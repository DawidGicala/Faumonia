/* $Id: BuddyLabelPopMenu.java,v 1.12 2011/09/05 20:07:01 madmetzger Exp $ */
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


import games.stendhal.client.entity.User;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

class BuddyLabelPopMenu extends JPopupMenu {
	/**
	 * serial version uid
	 */
	private static final long serialVersionUID = -8258597079393606179L;

	protected BuddyLabelPopMenu(final String buddyName, final boolean online) {
		super(buddyName);
		if (online) {
			createOnlineMenu(buddyName);
		} else {
			createOfflineMenu(buddyName);
		}
		
		JMenuItem removeBuddyMenuItem = new JMenuItem("Usuń");
		this.add(removeBuddyMenuItem);
		removeBuddyMenuItem.addActionListener(new RemovebuddyAction(buddyName));
	}

	// this one will fill into the chatline : /tell postman tell buddyName 
	// and then you type the message
	private void createOfflineMenu(final String buddyName) {
		JMenuItem leaveMessageBuddyMenuItem = new JMenuItem("Zostaw wiadomość");
		this.add(leaveMessageBuddyMenuItem);
		leaveMessageBuddyMenuItem.addActionListener(new LeaveBuddyMessageAction(buddyName));
	}

	private void createOnlineMenu(final String buddyName) {
		
		// this one will fill into the chatline : /tell buddyName 
		// and then you type the message
		JMenuItem talkBuddyMenuItem = new JMenuItem("Porozmawiaj");
		this.add(talkBuddyMenuItem);
		talkBuddyMenuItem.addActionListener(new TalkBuddyAction(buddyName));
		
		
		JMenuItem whereBuddyMenuItem = new JMenuItem("Gdzie jest");
		this.add(whereBuddyMenuItem);
		whereBuddyMenuItem.addActionListener(new WhereBuddyAction(buddyName));
		
		JMenuItem inviteBuddyMenuItem = new JMenuItem("Zaproś");
		this.add(inviteBuddyMenuItem);
		inviteBuddyMenuItem.addActionListener(new InviteBuddyAction(buddyName));
		
		if (User.isAdmin()) {
			JMenuItem teleportToBuddyMenuItem = new JMenuItem("(*)Teleportuj do");
			this.add(teleportToBuddyMenuItem);
			teleportToBuddyMenuItem.addActionListener(new TeleportToBuddyAction(buddyName));
		}
	}
}
