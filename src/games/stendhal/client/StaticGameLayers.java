/* $Id: StaticGameLayers.java,v 1.111 2011/11/17 15:06:40 kiheru Exp $ */
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
package games.stendhal.client;

import games.stendhal.common.CollisionDetection;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;

/** This class stores the layers that make the floor and the buildings. */

public class StaticGameLayers {
	private static final Logger logger = Logger.getLogger(StaticGameLayers.class);

	/** Name of the layers set that we are rendering right now. */
	private String area;

	/** true when the area has been changed. */
	private boolean areaChanged;

	/** Global current zone information. */
	private final ZoneInfo zoneInfo = ZoneInfo.get();
	/** The current zone. */
	private Zone currentZone;

	public StaticGameLayers() {
		area = null;
		areaChanged = true;
	}

	/**
	 * Get the danger level of the current zone.
	 * 
	 * @return danger level
	 */
	public double getDangerLevel() {
		return currentZone.getDangerLevel();
	}

	/** @return width in world units. */
	public double getWidth() {
		if (currentZone != null) {
			return currentZone.getWidth();
		}
		return 0.0;
	}

	/** @return the height in world units */
	public double getHeight() {
		if (currentZone != null) {
			return currentZone.getHeight();
		}
		return 0.0;
	}

	/**
	 * Set the current zone.
	 * 
	 * @param zone
	 */
	public void setZone(Zone zone) {
		currentZone = zone;
		if (!zone.getName().equals(area)) {
			setAreaName(zone.getName());
			markAreaChanged();
		}
	}

	/**
	 * Check if a shape collides within the current zone.
	 * 
	 * @param shape
	 * @return <code>true</code>, if the shape overlaps the static zone
	 *	collision, <code>false</code> otherwise
	 */
	public boolean collides(final Rectangle2D shape) {
		if (currentZone != null) {
			return currentZone.collides(shape);
		}
		return false;
	}

	/** Prepare for zone change. */
	public void clear() {
		zoneInfo.zoneChanged();
	}

	/**
	 * Set the name of the area to be rendered.
	 * @param area the areas name
	 */
	private void setAreaName(final String area) {
		this.area = area;
	}

	/**
	 * Get the area name.
	 * 
	 * @return area name
		 */
	public String getAreaName() {
		return area;
	}

	/**
	 * Draw a set of layers.
	 * 
	 * @param g Graphics
	 * @param area Zone name
	 * @param compositeName A bundle name for the set of layers. It is used to
	 * 	cache the layer set
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param adjustLayer name of the adjustment layer
	 * @param layers names of the layer set, starting from the bottom
	 */
	public void drawLayers(Graphics g, final String area, final String compositeName,
			final int x, final int y, final int width, final int height,
			String adjustLayer, String ... layers) {
		LayerRenderer lr = getMerged(area, compositeName, adjustLayer, layers);
		if (lr != null) {
			lr.draw(g, x, y, width, height);
		}
	}

	/**
	 * Get a composite representation of multiple tile layers.
	 * 
	 * @param area area name
	 * @param compositeName name to be used for the composite for caching
	 * @param adjustLayer name of the adjustment layer
	 * @param layers names of the layers making up the composite starting from
	 * 	the bottom
	 * @return layer corresponding to all sub layers or <code>null</code> if
	 * 	they can not be merged
	 */
	private LayerRenderer getMerged(String area, String compositeName, 
			String adjustLayer, String ... layers) {
		if (currentZone != null) {
			if (currentZone.getName().equals(area)) {
				return currentZone.getMerged(compositeName, adjustLayer, layers);
				} else {
				logger.warn("Trying to draw zone: " + area + ", but the current zone is: " + currentZone.getName());
			}
		}
		return null;
	}

	/**
	 * 
	 * @return the CollisionDetection Layer for the current map
	 * 
	 */
	public CollisionDetection getCollisionDetection() {
		if (currentZone != null) {
			return currentZone.getCollision();
		}
		return null;
	}

	/**
	 * 
	 * @return the ProtectionDetection Layer for the current map
	 * 
	 */
	public CollisionDetection getProtectionDetection() {
		if (currentZone != null) {
			return currentZone.getProtection();
	}
		return null;
	}

	/**
	 * 
	 * @return the SecretDetection Layer for the current map
	 * 
	 */
	public CollisionDetection getSecretDetection() {
		if (currentZone != null) {
			return currentZone.getSecret();
		}
		return null;
	}

	/**
	 * @return true if the area has changed since the last
	 */
	public boolean isAreaChanged() {
		return areaChanged;
	}

	/**
	 * marks the area as changed
	 */
	private void markAreaChanged() {
		this.areaChanged = true;
	}

	/**
	 * resets the areaChanged flag.
	 */
	public void resetChangedArea() {
		areaChanged = false;
	}
}
