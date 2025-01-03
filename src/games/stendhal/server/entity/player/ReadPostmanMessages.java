/* $Id: ReadPostmanMessages.java,v 1.3 2010/12/11 12:16:45 kymara Exp $ */
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
package games.stendhal.server.entity.player;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.ChatMessage;
import games.stendhal.server.core.engine.dbcommand.GetPostmanMessagesCommand;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnListenerDecorator;
import games.stendhal.server.core.events.TurnNotifier;

import java.util.List;

import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.db.command.ResultHandle;

import org.apache.log4j.Logger;

/**
 * Retrieves postman messages for the logging in player from the database
 *
 * @author kymara
 */
public class ReadPostmanMessages implements LoginListener, TurnListener {
	
	private static final Logger LOGGER = Logger.getLogger(ReadPostmanMessages.class);
	
	private ResultHandle handle = new ResultHandle();
	
	public void readMessages(final Player player) {
		DBCommand command = new GetPostmanMessagesCommand(player);
		DBCommandQueue.get().enqueueAndAwaitResult(command, handle);
		// wait one turn so that the messages come after any login messages
		TurnNotifier.get().notifyInTurns(1, new TurnListenerDecorator(this));
	}
	
	/** 
	 * Execute command to get messages for the player when they log in
	 * 
	 * @param player the player who logged in
	 */
	public void onLoggedIn(final Player player) {
		readMessages(player);
	}
	
	/**
	 * Completes handling the get messages action.
	 * 
	 * @param currentTurn ignored
	 */
	public void onTurnReached(int currentTurn) {
		GetPostmanMessagesCommand command = DBCommandQueue.get().getOneResult(GetPostmanMessagesCommand.class, handle);
		
		if (command == null) {
			TurnNotifier.get().notifyInTurns(0, new TurnListenerDecorator(this));
			return;
		}

		List<ChatMessage> messages = command.getMessages();
		Player player = command.getPlayer();
		LOGGER.debug(messages.size()+ " messages left for " + player.getName());
		for (ChatMessage chatmessage : messages) {
			LOGGER.debug(player.getName() + " got message: " + chatmessage.toString());
			// set the date to 'unknown' if the message was sent on a specific hard coded date which we will use for the old messages from xml
			player.sendPrivateText("postman powiedział Tobie: " + chatmessage.getTimestamp().substring(0,16).replace("2010-07-20 00:00", "nie znana data") + " " + chatmessage.getSource() + " poprosił mnie o dostarczenie tej wiadomości dla Ciebie: \n" + chatmessage.getMessage());
			// we faked that postman sent a message, better set the last private chatter incase the player now uses /answer
			player.setLastPrivateChatter("postman");
		}
		
	}
	
	private NotificationType getNotificationType(final String messagetype) {
		if ("S".equals(messagetype)) {
			return NotificationType.SUPPORT;
		} else {
			return NotificationType.PRIVMSG;
		}
		
	}
	
}