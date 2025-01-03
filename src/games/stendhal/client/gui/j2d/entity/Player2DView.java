/* $Id: Player2DView.java,v 1.53 2012/10/10 14:35:22 kiheru Exp $ */
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


import games.stendhal.client.IGameScreen;
import games.stendhal.client.OutfitStore;
import games.stendhal.client.ZoneInfo;
import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.Player;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.OutfitColor;
import games.stendhal.client.gui.j2d.entity.helpers.HorizontalAlignment;
import games.stendhal.client.gui.j2d.entity.helpers.VerticalAlignment;
import games.stendhal.client.gui.styled.cursor.StendhalCursor;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.Version;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The 2D view of a player.
 * 
 * @param <T> player type
 */
class Player2DView<T extends Player> extends RPEntity2DView<T> {
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(Player2DView.class);
	
	/**
	 * Sprite representing grumpy.
	 */
	private static Sprite grumpySprite;
	
	private boolean ignored = false;

	/**
	 * Sprite representing recently killing of other player.
	 */
	private static Sprite skullSprite;

	static {
		final SpriteStore store = SpriteStore.get();
		final Sprite gotGrumpySprite = store.getSprite("data/sprites/ideas/grumpy.png");
		final Sprite gotPkSprite = store.getSprite("data/sprites/ideas/pk.png");
		skullSprite = store.getAnimatedSprite(gotPkSprite , 0, 0, 12, 16, 24, 200);
		grumpySprite = store.getAnimatedSprite(gotGrumpySprite, 0, 0, 4,
				IGameScreen.SIZE_UNIT_PIXELS, IGameScreen.SIZE_UNIT_PIXELS,
				2000);
	}

	/**
	 * Create a new Player2DView.
	 */
	public Player2DView() {
		addIconManager(new StatusIconManager(Player.PROP_GRUMPY, grumpySprite,
				HorizontalAlignment.LEFT, VerticalAlignment.TOP, -8, -6) {
					@Override
					boolean show(T player) {
						return player.isGrumpy();
					}} );
		addIconManager(new StatusIconManager(Player.PROP_PLAYER_KILLER, skullSprite,
				HorizontalAlignment.LEFT, VerticalAlignment.TOP, 0, 0) {
					@Override
					boolean show(T player) {
						return player.isBadBoy();
					}} );
	}

	//
	// RPEntity2DView
	//

	/**
	 * Draw the entity status bar.
	 * 
	 * @param g2d
	 *            The graphics context.
	 * @param x
	 *            The drawn X coordinate.
	 * @param y
	 *            The drawn Y coordinate.
	 * @param width
	 *            The drawn width.
	 */
	@Override
	protected void drawStatusBar(final Graphics2D g2d, final int x,
			final int y, final int width) {
		/*
		 * Shift bar slightly to avoid overlap with smaller entities
		 */
		drawTitle(g2d, x, y + 6, width);
		Composite comp = g2d.getComposite();
		// Draw in full color for ignored players. Avoid making ghosts visible
		if (ignored && !entity.isGhostMode()) {
			g2d.setComposite(AlphaComposite.SrcAtop);
			drawHPbar(g2d, x, y + 6, width);
			g2d.setComposite(comp);
		} else {
			drawHPbar(g2d, x, y + 6, width);
		}
	}

	/**
	 * Get the full directional animation tile set for this entity.
	 * 
	 * @return A tile sprite containing all animation images.
	 */
	@Override
	protected Sprite getAnimationSprite() {
		final OutfitStore store = OutfitStore.get();

		try {
			OutfitColor color = OutfitColor.get(entity.getRPObject());
			ZoneInfo info = ZoneInfo.get();
			return store.getAdjustedOutfit(entity.getOutfit(), color,
					info.getZoneColor(), info.getColorMethod());
		} catch (final RuntimeException e) {
			logger.warn("Cannot build outfit. Setting failsafe outfit.", e);
			return store.getFailsafeOutfit();
		}
	}
	
	@Override
	protected AlphaComposite getComposite() {
		// Check for ghostmode to avoid ignored ghostmode admins becoming visible
		if (User.isIgnoring(entity.getName()) && !entity.isGhostMode()) {
			return AlphaComposite.DstOut;
		}
		return super.getComposite();
	}

	/**
	 * Determine is the user can see this entity while in ghostmode.
	 * 
	 * @return <code>true</code> if the client user can see this entity while in
	 *         ghostmode.
	 */
	@Override
	protected boolean isVisibleGhost() {
		/*
		 * Admins see all
		 */
		if (User.isAdmin()) {
			return true;
		}

		return false;
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
		if (!entity.isGhostMode()) {
			super.buildActions(list);
			
			boolean hasBuddy = User.hasBuddy(entity.getName());
			if (!hasBuddy) {
				list.add(ActionType.ADD_BUDDY.getRepresentation());
			}
			
			if (User.isIgnoring(entity.getName())) {
				list.add(ActionType.UNIGNORE.getRepresentation());
			} else if (!hasBuddy)  {
				list.add(ActionType.IGNORE.getRepresentation());
			}
			String serverVersion = User.getServerRelease();
			if ((serverVersion == null) || (Version.compare(serverVersion, "0.26.1") > 0)) {
				list.add(ActionType.TRADE.getRepresentation());
			}
			if ((serverVersion == null) || (Version.compare(serverVersion, "0.28") > 0)) {
				list.add(ActionType.INVITE.getRepresentation());
			}
		}
	}

	/**
	 * Draw the entity.
	 * 
	 * @param g2d
	 *            The graphics to drawn on.
	 */
	@Override
	protected void draw(final Graphics2D g2d, final int x, final int y, final int width, final int height) {
		boolean newIgnoreStatus = User.isIgnoring(entity.getName());
		if (newIgnoreStatus != ignored) {
			visibilityChanged = true;
			ignored = newIgnoreStatus;
			markChanged();
		}
		
		super.draw(g2d, x, y, width, height);
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
		if (isReleased()) {
			return;
		}
		if (at == null) {
			super.onAction(null);
			return;
		}
		switch (at) {
		case ADD_BUDDY:
			at.send(at.fillTargetInfo(entity));
			break;
			
		case IGNORE:
			at.send(at.fillTargetInfo(entity));
			break;

		case INVITE:
			at.send(at.fillTargetInfo(entity));
			break;

		case UNIGNORE:
			at.send(at.fillTargetInfo(entity));
			break;
			
		case TRADE:
			at.send(at.fillTargetInfo(entity));
			break;
		default:
			super.onAction(at);
			break;
		}
	}

	@Override
	public boolean isInteractive() {
		if (entity.isGhostMode() && !isVisibleGhost()) {
			return false;
		}
		return super.isInteractive();
	}

	@Override
	public StendhalCursor getCursor() {
		if (isInteractive()) {
			return StendhalCursor.LOOK;
		} else {
			return null;
		}
	}
}
