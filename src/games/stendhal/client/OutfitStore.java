/* $Id: OutfitStore.java,v 1.26 2011/10/02 16:17:11 kiheru Exp $ */
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
package games.stendhal.client;


import games.stendhal.client.gui.OutfitColor;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteCache;
import games.stendhal.client.sprite.SpriteStore;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;

import org.apache.log4j.Logger;

/**
 * An outfit store.
 */
public class OutfitStore {
	private Logger logger = Logger.getLogger(OutfitStore.class);

	/**
	 * The singleton.
	 */
	private static final OutfitStore sharedInstance = new OutfitStore(
			SpriteStore.get());

	/**
	 * The sprite store.
	 */
	protected SpriteStore store;

	/**
	 * Create an outfit store.
	 * 
	 * @param store
	 *            The sprite store to use.
	 */
	public OutfitStore(final SpriteStore store) {
		this.store = store;
	}

	//
	// OutfitStore
	//

	/**
	 * Build an outfit sprite.
	 * 
	 * The outfit is described by an "outfit code". It is an 8-digit integer of
	 * the form TTRRRHHDDDBB where TT is the number of the detail graphics (optional)
	 * RR is the number of the hair graphics (optional), HH for the
	 * head, DD for the dress, and BB for the base.
	 * 
	 * @param code
	 *            The outfit code.
	 * @param color coloring data
	 * 
	 * @return A walking state tileset.
	 */
	protected Sprite buildOutfit(int code, OutfitColor color) {
		int basecode = code % 100;
		code /= 100;
		
		int dresscode = code % 1000;
		code /= 1000;
		
		int headcode = code % 100;
		code /= 100;
		
		int haircode = code % 1000;
		code /= 1000;
		
		//int detailcode = code % 100;

		// Base (body) layer
		Sprite layer = getBaseSprite(basecode);
		if (layer == null) {
			throw new IllegalArgumentException("No base image found for outfit: " + basecode);
		}
		
		final ImageSprite sprite = new ImageSprite(layer);
		final Graphics g = sprite.getGraphics();

		// Dress layer
		layer = getDressSprite(dresscode, color);
		layer.draw(g, 0, 0);

		// Head layer
		layer = getHeadSprite(headcode);
		layer.draw(g, 0, 0);

		// Hair layer
		layer = getHairSprite(haircode, color);
		layer.draw(g, 0, 0);

		// Item layer
		/*layer = getDetailSprite(detailcode, color);
		layer.draw(g, 0, 0);*/

		return sprite;
	}

	/**
	 * Get the shared instance.
	 * 
	 * @return The shared [singleton] instance.
	 */
	public static OutfitStore get() {
		return sharedInstance;
	}

	/**
	 * Get the base sprite tileset.
	 * 
	 * @param index
	 *            The resource index.
	 * 
	 * @return The sprite, or <code>null</code>.
	 */
	public Sprite getBaseSprite(final int index) {
		final String ref = "data/sprites/outfit/player_base_" + index + ".png";

		if (!store.existsSprite(ref)) {
			return null;
		}

		return store.getSprite(ref);
	}

	/**
	 * Get the dress sprite tileset.
	 * 
	 * @param index
	 *            The resource index.
	 * @param color coloring data
	 * 
	 * @return The sprite, or <code>null</code>.
	 */
	public Sprite getDressSprite(final int index, OutfitColor color) {
		if (index == 0) {
			return getEmptySprite();
		}

		final String ref = "data/sprites/outfit/dress_" + index + ".png";
		return store.getColoredSprite(ref, color.getColor(OutfitColor.DRESS));
	}

	/**
	 * Get the empty sprite tileset.
	 * 
	 * @return The sprite.
	 */
	private Sprite getEmptySprite() {
		return store.getEmptySprite();
	}

	/**
	 * Get the failsafe outfit.
	 * 
	 * @return The failsafe outfit tileset.
	 */
	public Sprite getFailsafeOutfit() {
		try {
			return getOutfit(0, OutfitColor.PLAIN);
		} catch (RuntimeException e) {
			logger.warn("Cannot build failsafe outfit. Trying to use standard failsafe sprite.", e);
			return store.getFailsafe();
		}
	}

	/**
	 * Get the hair sprite tileset.
	 * 
	 * @param index
	 *            The resource index.
	 * @param color coloring data
	 * 
	 * @return The sprite, or <code>null</code>.
	 */
	public Sprite getHairSprite(final int index, OutfitColor color) {
		if (index == 0) {
			return getEmptySprite();
		}

		final String ref = "data/sprites/outfit/hair_" + index + ".png";

		return store.getColoredSprite(ref, color.getColor(OutfitColor.HAIR));
	}

	/**
	 * Get the head sprite tileset.
	 * 
	 * @param index
	 *            The resource index.
	 * 
	 * @return The sprite, or <code>null</code>.
	 */
	public Sprite getHeadSprite(final int index) {
		final String ref = "data/sprites/outfit/head_" + index + ".png";
		if (!store.existsSprite(ref)) {
			return null;
		}

		return store.getSprite(ref);
	}

	/**
	 * Get the item sprite tileset.
	 * 
	 * @param index
	 *            The resource index.
	 * @param color coloring data
	 * 
	 * @return The sprite, or <code>null</code>.
	 */
	/*public Sprite getDetailSprite(final int index, OutfitColor color) {
		if (index == 0) {
			return getEmptySprite();
		}

		final String ref = "data/sprites/outfit/detail_" + index + ".png";

		return store.getColoredSprite(ref, color.getColor(OutfitColor.DETAIL));
	}*/
	
	/**
	 * Get an outfit sprite.
	 * 
	 * The outfit is described by an "outfit code". It is an 8-digit integer of
	 * the form TTRRRHHDDDBB where where TT is the number of the detail graphics (optional)
	 * RR is the number of the hair graphics, HH for the
	 * head, DD for the dress, and BB for the base.
	 * 
	 * @param code
	 *            The outfit code.
	 * @param color Colors for coloring some outfit parts
	 * 
	 * @return An walking state tileset.
	 */
	public Sprite getOutfit(final int code, OutfitColor color) {
		// Use the normalized string for the reference
		final String reference = buildReference(code, color.toString());
		return getOutfit(code, color, reference);
	}

	/**
	 * Get outfit for a known outfit reference.
	 * 
	 * @param code outfit code
	 * @param color Color information for outfit parts
	 * @param reference outfit reference
	 * @return outfit
	 */
	private Sprite getOutfit(final int code, OutfitColor color, String reference) {
		final SpriteCache cache = SpriteCache.get();
		Sprite sprite = cache.get(reference);

		if (sprite == null) {
			sprite = buildOutfit(code, color);
			cache.add(reference, sprite);
		}

		return sprite;
	}

		/**
	 * Get an outfit with color adjustment, such as a player in colored light.
		 * 
	 * @param code outfit code
	 * @param color Color information for outfit parts
	 * @param adjColor adjustment color for the entire outfit
	 * @param blend blend mode for applying the adjustment color
	 * @return color adjusted outfit
	 */
	public Sprite getAdjustedOutfit(final int code, OutfitColor color, 
			Color adjColor, Composite blend) {
		if ((adjColor == null) || (blend == null)) {
			return getOutfit(code, color);
		} else {
			final SpriteCache cache = SpriteCache.get();
			// Use the normalized string for the reference
			final String reference = buildReference(code, color.toString());
			String fullRef = reference + ":" + adjColor.getRGB() + blend.toString();
			Sprite sprite = cache.get(fullRef);
			if (sprite == null) {
				Sprite plain = getOutfit(code, color);
				SpriteStore store = SpriteStore.get();
				sprite = store.modifySprite(plain, adjColor, blend, fullRef);

			}
			return sprite;
		}
	}

	/**
	 * Create an unique reference for an outfit.
	 * 
	 * @param code outfit code
	 * @param colorCode color information for outfit parts
	 * @return outfit reference
	 */
	private String buildReference(final int code, final String colorCode) {
		return "OUTFIT:" + code + "@" + colorCode;
	}
}
