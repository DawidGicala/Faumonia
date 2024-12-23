/* $Id: ActionType.java,v 1.47 2012/09/02 11:40:50 kiheru Exp $ */
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
package games.stendhal.client.entity;

import games.stendhal.client.StendhalClient;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

/**
 * translates the visual representation into server side commands.
 * 
 * @author astridemma
 */
public enum ActionType {
	LOOK("look", "Zobacz") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			return fillTargetPath(super.fillTargetInfo(entity), entity);
		}
	},
	READ("look", "Przeczytaj"),
	LOOK_CLOSELY("use", "Przyjżyj się dokładnie"),
	INSPECT("inspect", "Przeszukaj") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			return fillTargetPath(super.fillTargetInfo(entity), entity);
		}
	},
	ATTACK("attack", "Atakuj"),
	STOP_ATTACK("stop", "Przerwij atak"),
	PUSH("push", "Popchnij"),
	CLOSE("use", "Zamknij"),
	OPEN("use", "Otwórz") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			return fillTargetPath(super.fillTargetInfo(entity), entity);
		}
	},
	OWN("own", "Przygarnij"),
	USE("use", "Użyj") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			return fillTargetPath(super.fillTargetInfo(entity), entity);
		}
	},
	HARVEST("use", "Zbierz"),
	PICK("use", "Podnieś"),
	PROSPECT("use", "Poszukaj złota"),
	FISH("use", "Złów rybę"),
	STONE("use", "Wydobądź"),
	WISH("use", "Pomyśl życzenie"),
	WOOD("use", "Zetnij"),
	LEAVE_SHEEP("forsake", "Zostaw owcę") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			RPAction rpaction = super.fillTargetInfo(entity);
			rpaction.put("species", "sheep");
			return rpaction;
		}
	},
	LEAVE_PET("forsake", "Zostaw zwierzątko") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			RPAction rpaction = super.fillTargetInfo(entity);
			rpaction.put("species", "pet");
			return rpaction;
		}
	},
	ADD_BUDDY("addbuddy", "Dodaj do Przyjaciół") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			RPAction rpaction = super.fillTargetInfo(entity);
			rpaction.put("target", entity.getName());
			return rpaction;
		}
	},
	IGNORE("ignore", "Ignoruj") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			RPAction rpaction = super.fillTargetInfo(entity);
			rpaction.put("target", entity.getName());
			return rpaction;
		}
	},
	UNIGNORE("unignore", "Usuń ignorowanie") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			RPAction rpaction = super.fillTargetInfo(entity);
			rpaction.put("target", entity.getName());
			return rpaction;
		}
	},
	TRADE("trade", "Handluj") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			RPAction rpaction = super.fillTargetInfo(entity);
			rpaction.put("action", "offer_trade");
			return rpaction;
		}
	},
	ADMIN_INSPECT("inspect", "(*)Zbadaj (inspect)") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			return fillTargetPath(super.fillTargetInfo(entity), entity);
		}
	},
	ADMIN_GAG("gag", "(*)Ucisz (gag)"),
	ADMIN_JAIL("jail", "(*)Aresztuj (jail)"),
	ADMIN_DESTROY("destroy", "(*)Zniszcz (destroy)") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			return fillTargetPath(super.fillTargetInfo(entity), entity);
		}
	},
	ADMIN_ALTER("alter", "(*)Zmień (alter)") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			return fillTargetPath(super.fillTargetInfo(entity), entity);
		}
	},
	SET_OUTFIT("outfit", "Ustaw wygląd"),
	WHERE("where", "Gdzie") {
		@Override
		public RPAction fillTargetInfo(final IEntity entity) {
			RPAction rpaction = super.fillTargetInfo(entity);
			rpaction.put("target", entity.getName());
			return rpaction;
		}
	},
	ADMIN_VIEW_NPC_TRANSITIONS("npctransitions", "(*)Pokaż przejścia"),
	KNOCK("knock", "Zapukaj"),
	INVITE("group_management", "Zaproś") {

		@Override
		public RPAction fillTargetInfo(IEntity entity) {
			// invite action needs to add additional parameters to the RPAction
			RPAction a = super.fillTargetInfo(entity);
			a.put("action", "invite");
			a.put("params", entity.getName());
			return a;
		}
		
	};

	/**
	 * the String send to the server, if so.
	 */
	private final String actionCode;

	/**
	 * the String which is shown to the user.
	 */
	private final String actionRepresentation;

	/**
	 * Constructor.
	 * 
	 * @param actCode
	 *            the code to be sent to the server
	 * @param actionRep
	 *            the String to be shown to the user
	 */
	private ActionType(final String actCode, final String actionRep) {
		actionCode = actCode;
		actionRepresentation = actionRep;
	}

	/**
	 * finds the ActionType that belongs to a visual String representation.
	 * 
	 * @param representation
	 *            the menu String
	 * @return the Action Element or null if not found
	 */
	public static ActionType getbyRep(final String representation) {
		for (final ActionType at : ActionType.values()) {
			if (at.actionRepresentation.equals(representation)) {
				return at;
			}

		}
		return null;
	}

	/**
	 * @return the command code for usage on server side
	 */
	@Override
	public String toString() {
		return actionCode;
	}

	/**
	 * @return the String the user should see on the menu
	 */
	public String getRepresentation() {
		return actionRepresentation;
	}

	/**
	 * sends the requested action to the server.
	 * 
	 * @param rpaction
	 *            action to be sent
	 */
	public void send(final RPAction rpaction) {
		StendhalClient.get().send(rpaction);
	}
	
	/**
	 * Create an RPAction with target information pointing to an entity.
	 * 
	 * @param entity target entity
	 * @return action with entity as the target
	 */
	public RPAction fillTargetInfo(final IEntity entity) {
		RPAction rpaction = new RPAction();
		
		rpaction.put("type", toString());
		
		RPObject rpObject = entity.getRPObject(); 
		final int id = rpObject.getID().getObjectID();

		if (rpObject.isContained()) {
			/*
			 * Compatibility for old servers. Cannot handle nested objects.
			 * The actions that need to cope with contained objects should call
			 * fillTargetPath().
			 */
			rpaction.put("baseobject",
					rpObject.getContainer().getID().getObjectID());
			rpaction.put("baseslot", rpObject.getContainerSlot().getName());
			rpaction.put("baseitem", id);
		} else {
			StringBuilder target;
			target = new StringBuilder("#");
			target.append(Integer.toString(id));
			rpaction.put("target", target.toString());
		}
		
		return rpaction;
	}
	
	/**
	 * Add target information for a contained target object.
	 * 
	 * @param action
	 * @param entity target entity
	 * @return the action
	 */
	RPAction fillTargetPath(RPAction action, IEntity entity) {
		action.put(Actions.TARGET_PATH, entity.getPath());
		return action;
	}

	/**
	 * gets the action code
	 *
	 * @return actioncode
	 */
	public String getActionCode() {
		return actionCode;
	}
}
