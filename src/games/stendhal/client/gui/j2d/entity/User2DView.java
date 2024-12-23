/* $Id: User2DView.java,v 1.35 2012/09/01 20:17:54 kiheru Exp $ */
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
package games.stendhal.client.gui.j2d.entity;

import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.j2DClient;

import java.util.List;

/**
 * The 2D view of a user.
 * 
 * @param <T> user 
 */
class User2DView<T extends User> extends Player2DView<T> {
	@Override
	public void initialize(final T entity) {
		super.initialize(entity);
		j2DClient.get().setPosition(entity.getX(), entity.getY());
	}
	//
	// RPEntity2DView
	//

	/**
	 * Determine is the user can see this entity while in ghostmode.
	 * 
	 * @return <code>true</code> if the client user can see this entity while in
	 *         ghostmode.
	 */
	@Override
	protected boolean isVisibleGhost() {
		return true;
	}

	//
	// Entity2DView
	//

	/**
	 * Build a list of entity specific actions. <strong>NOTE: The first entry
	 * should be the default.</strong>
	 * 
	 * @param list
	 *            The list to populate.
	 */
	@Override
	protected void buildActions(final List<String> list) {
		super.buildActions(list);

		list.remove(ActionType.ATTACK.getRepresentation());
		list.remove(ActionType.ADD_BUDDY.getRepresentation());
		list.remove(ActionType.IGNORE.getRepresentation());
		list.remove(ActionType.UNIGNORE.getRepresentation());
		list.remove(ActionType.PUSH.getRepresentation());
		list.remove(ActionType.TRADE.getRepresentation());
		list.remove(ActionType.INVITE.getRepresentation());

		list.add(ActionType.SET_OUTFIT.getRepresentation());
		list.add(ActionType.WHERE.getRepresentation());

		User user = entity;
		if (user != null) {
			if (user.hasSheep()) {
				list.add(ActionType.LEAVE_SHEEP.getRepresentation());
			}

			if (user.hasPet()) {
				list.add(ActionType.LEAVE_PET.getRepresentation());
			}
		}
	}

	//
	// EntityChangeListener
	//

	/**
	 * An entity was changed.
	 * 
	 * @param entity
	 *            The entity that was changed.
	 * @param property
	 *            The property identifier.
	 */
	@Override
	public void entityChanged(final T entity, final Object property) {
		super.entityChanged(entity, property);

		if (property == IEntity.PROP_POSITION) {
			j2DClient.get().setPosition(entity.getX(), entity.getY());
		}
	}

	//
	// EntityView
	//

	/**
	 * Perform an action.
	 * 
	 * @param at
	 *            The action.
	 */
	@Override
	public void onAction(final ActionType at) {
		if (at == null) {
			super.onAction(null);
			return;
		}

		switch (at) {
		case SET_OUTFIT:
			j2DClient.get().chooseOutfit();
			break;
			
		case WHERE:
			at.send(at.fillTargetInfo(entity));
			break;

		case LEAVE_SHEEP:
			at.send(at.fillTargetInfo(entity));
			break;

		case LEAVE_PET:
			at.send(at.fillTargetInfo(entity));
			break;

		default:
			super.onAction(at);
			break;
		}
	}
}
