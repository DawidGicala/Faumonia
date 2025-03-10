/* $Id: ActionAttributesExist.java,v 1.5 2013/01/06 16:24:04 nhnb Exp $ */
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
package games.stendhal.server.actions.validator;

import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import marauroa.common.game.RPAction;

import org.apache.log4j.Logger;

/**
 * checks the the required attribute are part of the action.
 *
 * @author hendrik
 */
public class ActionAttributesExist implements ActionValidator {
	private static Logger logger = Logger.getLogger(ActionAttributesExist.class);
	private Collection<String> attributes;

	/**
	 * creates a new ActionAttributesExist
	 *
	 * @param attributes list of required attributes
	 */
	public ActionAttributesExist(String... attributes) {
		this.attributes = new LinkedList<String>(Arrays.asList(attributes));
	}

	/**
	 * validates an RPAction.
	 *
	 * @param player Player
	 * @param action RPAction to validate
	 * @param data   data about this action
	 * @return <code>null</code> if the action is valid; an error message otherwise
	 */
	@Override
	public String validate(Player player, RPAction action, ActionData data) {
		for (String attribute : attributes) {
			if (!action.has(attribute)) {
				logger.warn("Internal Error: Action " + action.get("type") + " is missing required attribute " + attribute);
				return "Ta akcja wymaga więcej informacji.";
			}
		}
		return null;
	}

}
