/* $Id: ItemPanel.java,v 1.25 2013/01/27 17:37:57 kiheru Exp $ */
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

import games.stendhal.client.GameLoop;
import games.stendhal.client.IGameScreen;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Inspector;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.j2d.entity.EntityView;
import games.stendhal.client.gui.j2d.entity.EntityViewFactory;
import games.stendhal.client.gui.styled.cursor.CursorRepository;
import games.stendhal.client.gui.styled.cursor.StendhalCursor;
import games.stendhal.client.gui.wt.EntityViewCommandList;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.EquipActionConsts;
import games.stendhal.common.constants.Actions;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * A component representing space in a slot. 
 */
public class ItemPanel extends JComponent implements DropTarget, Inspectable {
	/** serial version uid. */
	private static final long serialVersionUID = 3409932623156446910L;

	/** 
	 * Amount in pixels to shift the popup menu under the mouse, compared to
	 * the left up corner.
	 */
	private static final int POPUP_MENU_OFFSET = 10;
	private static final CursorRepository cursorRepository = new CursorRepository();
	
	/**
	 * The background surface sprite.
	 */
	private static final Sprite background = SpriteStore.get().getSprite("data/gui/slot.png");
	
	/** The placeholder sprite, or <code>null</code>. */
	private final Sprite placeholder;
	
	/**
	 * The entity view being held.
	 */
	private EntityView<?> view;
	/** The entity to whom the displayed slot belongs to. */
	private IEntity parent;
	/**
	 * Current associated popup menu. Using
	 * {@link #setComponentPopupMenu(JPopupMenu)} is
	 * <b>not</b> safe because of a swing bug that can under some conditions
	 * display the menu at the location of the mouse click and prevent the
	 * correct menu displaying code (which does the offset adjustments) from
	 * being called.<p>
	 * Fix for bug #3069835.
	 */
	private JPopupMenu popupMenu;
	/** The inspector the included entity should use. */
	private Inspector inspector;
	private int itemNumber;
	
	/** Object types the panel can accept. */
	private List<Class> acceptedTypes = new ArrayList<Class>();
	
	/**
	 * Create a new ItemPanel.
	 * 
	 * @param slotName name of the slot this refers to 
	 * @param placeholder image used in an empty panel, or <code>null</code>
	 */
	public ItemPanel(final String slotName, final Sprite placeholder) {
		this.placeholder = placeholder;
		setName(slotName);
		
		Dimension size = new Dimension(background.getWidth(), background.getHeight()); 
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setOpaque(false);
		
		// DnD handling
		ItemPanelMouseHandler drag = new ItemPanelMouseHandler();
		addMouseMotionListener(drag);
		addMouseListener(drag);
	}
	
	/**
	 * Set item number for purposes of reordering slot contents.
	 * 
	 * @param itemNumber the index, corresponding to this panel, of the space
	 * in the slot
	 */
	void setItemNumber(int itemNumber) {
		this.itemNumber = itemNumber;
	}
	
	/**
	 * Set the inspector the contained entity should use.
	 * 
	 * @param inspector
	 */
	@Override
	public void setInspector(Inspector inspector) {
		this.inspector = inspector;
	}
	
	/**
	 * Set the slot entity.
	 * 
	 * @param entity The new entity, or <code>null</code>.
	 */
	protected void setEntity(final IEntity entity) {
		if (view != null) {
			/*
			 * Don't replace the same object
			 */
			if (view.getEntity() == entity) {
				return;
			}

			view.release();
		}

		if (entity != null) {
			setEntityView(EntityViewFactory.create(entity));
		} else {
			setEntityView(null);
		}
		
		// The old popup menu is no longer valid
		popupMenu = null;
		repaint();
	}

	/**
	 * Set the entity view.
	 * 
	 * @param view new view, or <code>null</code>
	 */
	private void setEntityView(EntityView<?> view) {
		this.view = view;
			if (view != null) {
				view.setContained(true);
				view.setInspector(inspector);
				if (parent.isUser()) {
					setCursor(cursorRepository.get(view.getCursor()));
				} else {
					setCursor(cursorRepository.get(StendhalCursor.ITEM_PICK_UP_FROM_SLOT));
				}
			} else {
			setCursor(null);
		}
		popupMenu = null;
	}
	
	/**
	 * Get the shown entity.
	 * 
	 * @return entity, or <code>null</code> if the panel contains no entity
	 */
	IEntity getEntity() {
		if (view != null) {
			return view.getEntity();
		}
		return null;
	}
	
	/**
	 * Move the contents of the panel to another ItemPanel.
	 * 
	 * @param other the panel to move the contents
	 */
	void moveViewTo(ItemPanel other) {
		other.setEntityView(view);
		setEntityView(null);
	}
	
	/**
	 * Set the containing entity.
	 * 
	 * @param parent
	 */
	protected void setParent(IEntity parent) {
		this.parent = parent;
	}

	@Override
	public void paintComponent(Graphics g) {
		// draw the background image
		background.draw(g, 0, 0);
		
		// Take a temporary copy in case the game loop destroys the view under
		// us.
		EntityView<?> entityView = view;
		if (entityView != null) {
			// Center the entity view (assume 1x1 tile)
			final int x = (getWidth() - IGameScreen.SIZE_UNIT_PIXELS) / 2;
			final int y = (getHeight() - IGameScreen.SIZE_UNIT_PIXELS) / 2;

			final Graphics2D vg = (Graphics2D) g.create(0, 0, getWidth(),
					getHeight());
			vg.translate(x, y);
			entityView.draw(vg);
			vg.dispose();
		} else if (placeholder != null) {
			placeholder.draw(g, (getWidth() - placeholder.getWidth()) / 2, 
					(getHeight() - placeholder.getHeight()) / 2);
		}
	}

	@Override
	public void dropEntity(IEntity entity, int amount, Point point) {
		// Don't drag an item into the same slot
		if ((view != null) && (entity == view.getEntity())) {
			return;
		}
		
		// Reorder, instead of a move
		if (entity.getRPObject().getContainerSlot() == parent.getSlot(getName())) {
			// Don't reorder, if the user has chosen a non-standard amount
			if (amount != -1) {
				return;
			}
			reorder(entity);
			return;
		}
		
		// Fill in appropriate action data
		RPAction action = new RPAction();
		action.put(EquipActionConsts.TYPE, "equip");
		action.put(EquipActionConsts.SOURCE_PATH, entity.getPath());
		List<String> targetPath = parent.getPath();
		targetPath.add(getName());
		action.put(Actions.TARGET_PATH, targetPath);
		
		if (amount >= 1) {
			action.put(EquipActionConsts.QUANTITY, amount);
		}

		// ** Compatibility. Fill old style object address data **
		// fill 'moved from' parameters
		final RPObject rpObject = entity.getRPObject();
		if (rpObject.isContained()) {
			// the item is inside a container
			action.put(EquipActionConsts.BASE_OBJECT, rpObject.getContainer().getID().getObjectID());
			action.put(EquipActionConsts.BASE_SLOT, rpObject.getContainerSlot().getName());
		}
		action.put(EquipActionConsts.BASE_ITEM, rpObject.getID().getObjectID());

		// 'move to'
		action.put(EquipActionConsts.TARGET_OBJECT, parent.getID().getObjectID());
		action.put(EquipActionConsts.TARGET_SLOT, getName());

		StendhalClient.get().send(action);
	}
	
	/**
	 * Generate a reordering action for an entity in the slot.
	 * 
	 * @param entity
	 */
	private void reorder(final IEntity entity) {
		// Don't needlessly send reordering commands to servers that do not
		// understand them
		if (User.getServerRelease().compareTo("0.41") < 0) {
			return;
		}
		// GameLoop may modify slot contents, so we need to scan the contents in
		// the same thread.
		GameLoop.get().runOnce(new Runnable() {
			@Override
			public void run() {
				RPObject rpobject = entity.getRPObject();
				RPSlot slot = rpobject.getContainerSlot();
				int i = 0;
				for (RPObject content : slot) {
					if (content == rpobject) {
						if (itemNumber != i) {
							RPAction action = new RPAction();
							action.put(EquipActionConsts.TYPE, "reorder");
							action.put(EquipActionConsts.SOURCE_PATH, entity.getPath());
							action.put("new_position", itemNumber);
							
							StendhalClient.get().send(action);
							return;
						}
					}
					i++;
				}
			}
		});
	}
	
	/**
	 * Handler for mouse use. Takes care of both drags and clicks. 
	 */
	private class ItemPanelMouseHandler extends MouseHandler {
		@Override
		protected void onDragStart(Point point) {
			if (view != null) {
				if (view.isMovable()) {
					DragLayer.get().startDrag(view.getEntity());
				}
			}
		}

		@Override
		protected boolean onMouseClick(Point point) {
			// ignore empty slots
			if (view == null) {
				return true;
			}
			
			boolean doubleClick = Boolean.parseBoolean(WtWindowManager.getInstance().getProperty("ui.doubleclick", "false"));
			if (doubleClick) {
				return false;
			}

			// Click on entity. Decide on action
			if (isUserSlot()) {
				return view.onHarmlessAction();
			} else {
				moveItemToBag();
				return true;
			}
		}

		@Override
		protected boolean onMouseDoubleClick(Point point) {
			// ignore empty slots
			if (view == null) {
				return false;
			}

			/*
			 * moveto events are not the default for items the player is
			 * carrying along
			 */
			if (isUserSlot()) {
				view.onAction();
				return true;
			}

			// otherwise try to grab the item
			moveItemToBag();
			return true;
		}

		/**
		 * Check if the slot is carried by the user.
		 * 
		 * @return <code>true</code> if the slot belongs to the user, or to an
		 * 	item carried by the user, otherwise <code>false</code>
		 */
		private boolean isUserSlot() {
			return parent.getRPObject().getContainerBaseOwner().equals(User.get().getRPObject());
		}

		@Override
		@SuppressWarnings("serial")
		protected void onMouseRightClick(Point point) {
			if (view != null) {
				if (popupMenu == null) {
					// create the context menu
					popupMenu = new EntityViewCommandList(getName(), view.getActions(), view) {
						@Override
						protected void doAction(final String command) {
							super.doAction(command);
							setVisible(false);
						}
					};
				}
				/*
				 * Relocate under the cursor. Offset a bit so that the first
				 * entry is under the mouse.
				 */
				popupMenu.show(ItemPanel.this, point.x - POPUP_MENU_OFFSET,
						point.y - POPUP_MENU_OFFSET);
			}
		}
		
		/**
		 * Send an action for grabbing the item to the bag.
		 */
		private void moveItemToBag() {
			final RPAction action = new RPAction();
			
			// Views and entities can be destroyed by game loop. Grab copies
			EntityView<?> entityView = view;
			IEntity parentEntity = parent;
			if ((entityView == null) || (parentEntity == null)) {
				return;
			}
			
			action.put(EquipActionConsts.TYPE, "equip");
			action.put(EquipActionConsts.SOURCE_PATH, entityView.getEntity().getPath());
			action.put(Actions.TARGET_PATH, 
					Arrays.asList(Integer.toString(User.get().getID().getObjectID()), "bag"));
			
			// Compatibility item identification data
			// source object and content from THIS container
			final RPObject content = entityView.getEntity().getRPObject();
			action.put(EquipActionConsts.BASE_OBJECT, parentEntity.getID().getObjectID());
			action.put(EquipActionConsts.BASE_SLOT, getName());
			action.put(EquipActionConsts.BASE_ITEM, content.getID().getObjectID());
			// target is player's bag
			action.put(EquipActionConsts.TARGET_OBJECT, User.get().getID().getObjectID());
			action.put(EquipActionConsts.TARGET_SLOT, "bag");
			
			StendhalClient.get().send(action);
		}
	}
	
	/**
	 * Set the types the panel can accept.
	 * 
	 * @param types
	 */
	void setAcceptedTypes(Class ... types) {
		acceptedTypes = Arrays.asList(types);
	}
	
	/**
	 * Set the types the panel can accept.
	 * 
	 * @param types
	 */
	void setAcceptedTypes(List<Class> types) {
		acceptedTypes = types;
	}

	@Override
	public boolean canAccept(IEntity entity) {
		for (Class<?> c : acceptedTypes) {
			if (c.isAssignableFrom(entity.getClass())) {
				return true;
			}
		}
		return false;
	}
}
