/* $Id: GroundContainer.java,v 1.15 2012/05/28 14:34:44 kiheru Exp $ */
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
package games.stendhal.client.gui;

import games.stendhal.client.IGameScreen;
import games.stendhal.client.StaticGameLayers;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Inspector;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.factory.EntityMap;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.spellcasting.DefaultGroundContainerMouseState;
import games.stendhal.client.gui.spellcasting.GroundContainerMouseState;
import games.stendhal.client.gui.styled.cursor.CursorRepository;
import games.stendhal.client.gui.styled.cursor.StendhalCursor;
import games.stendhal.common.Direction;
import games.stendhal.common.EquipActionConsts;
import games.stendhal.common.NotificationType;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import javax.swing.JComponent;

import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

import org.apache.log4j.Logger;

/**
 * Mouse handler for the game screen floor.
 */
public class GroundContainer extends MouseHandler implements Inspector, 
	MouseWheelListener {

	private static final Logger logger = Logger.getLogger(GroundContainer.class);

	private CursorRepository cursorRepository = new CursorRepository(); 
	
	/** The game screen this handler is providing mouse processing */
	private final IGameScreen screen;
	/** Client for sending actions */
	private final StendhalClient client;
	/** Component to place popup menus */
	private final JComponent canvas;

	private GroundContainerMouseState state;

	/**
	 * Create a new GroundContainer.
	 * 
	 * @param client
	 * @param gameScreen
	 * @param canvas The component to place popup menus
	 */
	public GroundContainer(final StendhalClient client, final IGameScreen gameScreen, 
			final JComponent canvas) {
		this.client = client;
		this.screen = gameScreen;
		this.canvas = canvas;
		this.state = new DefaultGroundContainerMouseState(this);
	}

	@Override
	protected void onDragStart(Point point) {
		state.onDragStart(point);
	}

	@Override
	public synchronized void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		/*
		 * Get cursor from entity below the mouse.
		 */
		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
			return;
		}

		StendhalCursor cursor = getCursor(e.getPoint());
		canvas.setCursor(cursorRepository.get(cursor));
	}

	/**
	 * Get cursor for a point.
	 * 
	 * @param point
	 * @return cursor
	 */
	public StendhalCursor getCursor(Point point) {
		return state.getCursor(point);
	}

	/**
	 * Send a move to command to the server.
	 * 
	 * @param point destination
	 * @param doubleClick <code>true</code> if the action was created with a
	 * 	double click, <code>false</code> otherwise
	 */
	public void createAndSendMoveToAction(final Point2D point, boolean doubleClick) {
		final RPAction action = new RPAction();
		action.put("type", "moveto");
		action.put("x", (int) point.getX());
		action.put("y", (int) point.getY());
		if (doubleClick) {
			action.put("double_click", "");
		}
		
		Direction dir = calculateZoneChangeDirection(point);
		if (dir != null) {
			action.put("extend", dir.ordinal());
		}
		
		client.send(action);
	}

	/**
	 * calculates whether the click was close enough to a zone border to trigger
	 * a zone change
	 *
	 * @param point click point in world coordinates
	 * @return Direction of the zone to change to, <code>null</code> if no zone change should happen
	 */
	public Direction calculateZoneChangeDirection(Point2D point) {
		StaticGameLayers layers = StendhalClient.get().getStaticGameLayers();
		double x = point.getX();
		double y = point.getY();
		double width = layers.getWidth();
		double height = layers.getHeight();
		if (x < 0.333) {
			return Direction.LEFT;
		}
		if (x > width - 0.333) {
			return Direction.RIGHT;
		}
		if (y < 0.333) {
			return Direction.UP;
		}
		if (y > height - 0.4) {
			return Direction.DOWN;
		}
		return null;
	}

	@Override
	protected boolean onMouseClick(Point point) {
		return this.state.onMouseClick(point);
	}

	@Override
	protected boolean onMouseDoubleClick(Point point) {
		return this.state.onMouseDoubleClick(point);
	}

	@Override
	protected void onMouseRightClick(Point point) {
		this.state.onMouseRightClick(point);
	}
	
	/**
	 * Remembers whether the client was active on last mouse down
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		state.mousePressed(e);
	}

	

	/*
	 * (non-Javadoc)
	 * @see games.stendhal.client.gui.DropTarget#dropEntity(games.stendhal.client.entity.IEntity, java.awt.Point)
	 */
	public void dropEntity(IEntity entity, int amount, Point point) {
		final RPAction action = new RPAction();

		RPObject item = entity.getRPObject();
		if (item == null) {
				return;
			}

		RPObject parent = item.getContainer();
		action.put(EquipActionConsts.SOURCE_PATH, entity.getPath());
		if (parent != null) {
			// looks like an drop
			action.put("type", "drop");
			
			// Compatibility object addressing
			action.put(EquipActionConsts.BASE_OBJECT, parent.getID().getObjectID());
			action.put(EquipActionConsts.BASE_SLOT, item.getContainerSlot().getName());
		} else {
			// it is a displace
			action.put("type", "displace");
		}
		// Compatibility object addressing
		action.put(EquipActionConsts.BASE_ITEM, item.getID().getObjectID());

		if (amount >= 1) {
			action.put("quantity", amount);
		}

		// 'move to'
		final Point2D location = screen.convertScreenViewToWorld(point);
		action.put("x", (int) location.getX());
		action.put("y", (int) location.getY());

		client.send(action);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (User.isNull()) {
			return;
		}

		/*
		 * Turning with mouse wheel. Ignore all but the first to avoid flooding
		 * the server with turn commands.
		 */
		logger.debug(e.getClickCount() + " click count and " + e.getScrollType() + " scroll type and wheel rotation " + e.getWheelRotation());
		if (e.getClickCount() <= 1) {
			Direction current = User.get().getDirection();
			if (e.getUnitsToScroll() > 0) {
				// Turn right
				client.addDirection(current.nextDirection(), true);
			} else {
				// Turn left
				client.addDirection(current.nextDirection().oppositeDirection(), true);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see games.stendhal.client.entity.Inspector#inspectMe(games.stendhal.client.entity.IEntity, marauroa.common.game.RPSlot, games.stendhal.client.gui.SlotWindow, int, int)
	 */
	public SlotWindow inspectMe(final IEntity suspect, final RPSlot content,
			final SlotWindow container, final int width, final int height) {
		if ((container != null) && container.isVisible()) {
			container.raise();
			return container;
		} else {
			SlotWindow window = new SlotWindow(suspect.getType(), width, height);
			window.setAcceptedTypes(EntityMap.getClass("item", null, null));
			window.setSlot(suspect, content.getName());
			// Only display the window if it's actually going to stay open
			if (window.isCloseEnough()) {
			j2DClient.get().addWindow(window);
			window.raise();
			window.setVisible(true);
			return window;
			} else {
				// Otherwise just give a message to the user and let the window
				// be collected as garbage
				j2DClient.get().addEventLine(new EventLine("", "" + suspect.getType() + " jest zbyt daleko.", NotificationType.CLIENT));
				return null;
			}
		}
	}
	

	public IGameScreen getScreen() {
		return screen;
	}

	public JComponent getCanvas() {
		return canvas;
	}

	public StendhalClient getClient() {
		return client;
	}

	public void setNewMouseHandlerState(
			GroundContainerMouseState newState) {
		this.state = newState;
	}
}
