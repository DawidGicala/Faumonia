/* $Id: StoreMessageOnBehalfOfPlayerAction.java,v 1.6 2013/01/06 16:23:55 nhnb Exp $ */
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
package games.stendhal.server.actions.admin;

import static games.stendhal.common.constants.Actions.TARGET;
import static games.stendhal.common.constants.Actions.TEXT;
import games.stendhal.common.NotificationType;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.dbcommand.StoreMessageCommand;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnListenerDecorator;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.db.command.ResultHandle;

/**
 * Stores a message to another player on behalf of any player
 * for admins >= level 2000 (e.g. postman)
 */
public class StoreMessageOnBehalfOfPlayerAction extends AdministrationAction implements TurnListener  {

	private ResultHandle handle = new ResultHandle();
	
	public static void register() {
		CommandCenter.register("storemessageonbehalfofplayer", new StoreMessageOnBehalfOfPlayerAction(), 21);
	}

	@Override
	public void perform(final Player player, final RPAction action) {

		if (action.has("source") && action.has(TARGET) && action.has(TEXT)) {
			
			String message = action.get(TEXT);
			
			DBCommand command = new StoreMessageCommand(action.get("source"), action.get(TARGET), message, "P");
			DBCommandQueue.get().enqueueAndAwaitResult(command, handle);
			TurnNotifier.get().notifyInTurns(0, new TurnListenerDecorator(this));
		}
	}
	
	/**
	 * Completes handling the store message action
	 * 
	 * @param currentTurn ignored
	 */
	@Override
	public void onTurnReached(int currentTurn) {
		StoreMessageCommand checkcommand = DBCommandQueue.get().getOneResult(StoreMessageCommand.class, handle);
		
		if (checkcommand == null) {
			TurnNotifier.get().notifyInTurns(0, new TurnListenerDecorator(this));
			return;
		}

		boolean characterExists = checkcommand.targetCharacterExists();
		String source = checkcommand.getSource();
		String target = checkcommand.getTarget();
		
		final Player sourceplayer = SingletonRepository.getRuleProcessor().getPlayer(source);


		if (sourceplayer != null) {
			// incase source player logged out while waiting we want to avoid NPE
			if(characterExists) {
				sourceplayer.sendPrivateText("postman powiedział Tobie: Wiadomość zaakceptowana do dostarczenia");
			} else {
				sourceplayer.sendPrivateText(NotificationType.ERROR, "postman powiedział Tobie: Nie można znaleść wojownika " + target + " z związku z tym wiadomość nie może zostać przyjęta.");
			}
			sourceplayer.setLastPrivateChatter("postman");
		}
		
		return;
	} 
		
}
