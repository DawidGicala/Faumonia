/* $Id: Outfit.java,v 1.34 2012/09/29 22:25:58 kymara Exp $ */
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
package games.stendhal.server.entity;

import games.stendhal.common.Outfits;
import games.stendhal.common.Rand;

import org.apache.log4j.Logger;
/**
 * A data structure that represents the outfit of an RPEntity. This RPEntity can
 * either be an NPC which uses the outfit sprite system, or of a player.
 *
 * You can use this data structure so that you don't have to deal with the way
 * outfits are stored internally.
 *
 * An outfit can contain up to five parts: hair, head, dress, and base.
 *
 * Note, however, that you can create outfit objects that consist of less than
 * five parts by setting the other parts to <code>null</code>. For example,
 * you can create a dress outfit that you can combine with the player's current
 * so that the player gets the dress, but keeps his hair, head, and base.
 *
 * Not all outfits can be chosen by players.
 *
 * @author daniel
 *
 */
public class Outfit {

	/** the logger instance. */
	private static final Logger LOGGER = Logger.getLogger(Outfit.class);

	/** The detail index, as a value between 0 and 99, or null. */
	//private Integer detail;
	
	/** The hair index, as a value between 0 and 99, or null. */
	private final Integer hair;

	/** The head index, as a value between 0 and 99, or null. */
	private final Integer head;

	/** The dress index, as a value between 0 and 99, or null. */
	private final Integer dress;

	/** The base index, as a value between 0 and 99, or null. */
	private final Integer base;

	/**
	 * Creates a new default outfit (naked person).
	 */
	public Outfit() {
		this(0, 0, 0, 0);
	}

	/**
	 * Creates a new outfit. Set some of the parameters to null if you want an
	 * entity that put on this outfit to keep on the corresponding parts of its
	 * current outfit.
	 *
	 * @param hair
	 *            The index of the hair style, or null
	 * @param head
	 *            The index of the head style, or null
	 * @param dress
	 *            The index of the dress style, or null
	 * @param base
	 *            The index of the base style, or null
	 */
	public Outfit(final Integer hair, final Integer head, final Integer dress, final Integer base) {
		//this.detail = detail;
		this.hair = hair;
		this.head = head;
		this.dress = dress;
		this.base = base;
	}

	/**
	 * Creates a new outfit based on a numeric code.
	 *
	 * @param code
	 *            A 10-digit decimal number where the last part (from the right) stands for base,
	 *            the next for dress, then head, then hair, then detail
	 */
	public Outfit(final int code) {

		this.base = code % 100;

		this.dress = code / 100 % 1000;

		this.head = code / 100000 % 100;

		this.hair = code / 10000000 % 1000;
	}

	/**
	 * Gets the index of this outfit's base style.
	 *
	 * @return The index, or null if this outfit doesn't contain a base.
	 */
	public Integer getBase() {
		return base;
	}

	/**
	 * Gets the index of this outfit's dress style.
	 *
	 * @return The index, or null if this outfit doesn't contain a dress.
	 */
	public Integer getDress() {
		return dress;
	}


	/**
	 * Gets the index of this outfit's hair style.
	 *
	 * @return The index, or null if this outfit doesn't contain hair.
	 */
	public Integer getHair() {
		return hair;
	}

	/**
	 * Gets the index of this outfit's head style.
	 *
	 * @return The index, or null if this outfit doesn't contain a head.
	 */
	public Integer getHead() {
		return head;
	}

	/**
	 * Gets the index of this outfit's detail style.
	 *
	 * @return The index, or null if this outfit doesn't contain a detail.
	 */
	/*public Integer getDetail() {
		return detail;
	}*/


	/**
	 * Represents this outfit in a numeric code.
	 *
	 * @return A 10-digit decimal number where the first pair of digits stand for
	 *         detail, the second pair for hair, the third pair for head, the
	 *         fourth pair for dress, and the fifth pair for base
	 */
	public int getCode() {
		//int de = 0;
		int ha = 0;
		int he = 0;
		int dr = 0;
		int ba = 0;
		/*if (detail != null) {
			de = detail.intValue();
		}*/
		if (hair != null) {
			ha = hair.intValue();
		}
		if (head != null) {
			he = head.intValue();
		}
		if (dress != null) {
			dr = dress.intValue();
		}
		if (base != null) {
			ba = base.intValue();
		}
		return ha * 10000000 + he * 100000 + dr * 100 + ba;
	}

	/**
	 * Gets the result that you get when you wear this outfit over another
	 * outfit. Note that this new outfit can contain parts that are marked as
	 * NONE; in this case, the parts from the other outfit will be used.
	 *
	 * @param other
	 *            the outfit that should be worn 'under' the current one
	 * @return the combined outfit
	 */
	public Outfit putOver(final Outfit other) {
		//int newDetail;
		int newHair;
		int newHead;
		int newDress;
		int newBase;
		// wear the this outfit 'over' the other outfit;
		// use the other outfit for parts that are not defined for this outfit.
		/*if (this.detail == null) {
			newDetail = other.detail;
		} else {
			newDetail = this.detail;
		}*/
		if (this.hair == null) {
			newHair = other.hair;
		} else {
			newHair = this.hair;
		}
		if (this.head == null) {
			newHead = other.head;
		} else {
			newHead = this.head;
		}
		if (this.dress == null) {
			newDress = other.dress;
		} else {
			newDress = this.dress;
		}
		if (this.base == null) {
			newBase = other.base;
		} else {
			newBase = this.base;
		}
		return new Outfit(newHair, newHead, newDress, newBase);
	}

	/**
	 * Gets the result that you get when you remove (parts of) an outfit.
	 * Removes the parts in the parameter, from the current outfit.
	 * NOTE: If a part does not match, the current outfit part will remain the same.
	 *
	 * @param other
	 *            the outfit that should be removed from the current one
	 * @return the new outfit, with the parameter-outfit removed
	 */
	public Outfit removeOutfit(final Outfit other) {
		//int newDetail;
		int newHair;
		int newHead;
		int newDress;
		int newBase;
		// wear the this outfit 'over' the other outfit;
		// use the other outfit for parts that are not defined for this outfit.
		/*if ((detail == null) || detail.equals(other.detail)) {
			newDetail = 0;
		} else {
			newDetail = detail;
		}*/
		if ((hair == null) || hair.equals(other.hair)) {
			newHair = 0;
		} else {
			newHair = hair;
		}
		if ((head == null) || head.equals(other.head)) {
			newHead = 0;
		} else {
			newHead = head;
		}
		if ((dress == null) || dress.equals(other.dress)) {
			newDress = 0;
		} else {
			newDress = dress;
		}
		if ((base == null) || base.equals(other.base)) {
			newBase = 0;
		} else {
			newBase = base;
		}
		return new Outfit(/*newDetail, */newHair, newHead, newDress, newBase);
	}

	/**
	 * removes the details
	 */
	/*public void removeDetail() {
		detail = 0;
	}*/

	/**
	 * Checks whether this outfit is equal to or part of another outfit.
	 *
	 * @param other
	 *            Another outfit.
	 * @return true iff this outfit is part of the given outfit.
	 */
	public boolean isPartOf(final Outfit other) {
		return ((hair == null) || hair.equals(other.hair))
				&& ((head == null) || head.equals(other.head))
				&& ((dress == null) || dress.equals(other.dress))
				&& ((base == null) || base.equals(other.base));
	}

	/**
	 * Checks whether this outfit may be selected by a normal player as normal
	 * outfit. It returns false for special event and GM outfits.
	 *
	 * @return true if it is a normal outfit
	 */
	public boolean isChoosableByPlayers() {
		return (hair < Outfits.HAIR_OUTFITS) && (hair >= 0) 
 			&& (head < Outfits.HEAD_OUTFITS) && (head >= 0)
			&& (dress < Outfits.CLOTHES_OUTFITS) && (dress >= 0)
			&& (base < Outfits.BODY_OUTFITS) && (base >= 0);
	}

	/**
	 * Is outfit missing a dress?
	 *
	 * @return true if naked, false if dressed
	 */
	public boolean isNaked() {
		if (isCompatibleWithClothes()) {
			return (dress == null) || dress.equals(0);
		} else {
			return false;
		} 
		
	}

	/**
	 * Create a random unisex outfit, with a 'normal' face and unisex base
	 *
	 * <ul>
	 * <li>hair number (1 to 26) selection of hairs which look ok with both goblin
	 *     face and cute face (later hairs only look right with cute face)</li>
	 * <li>head numbers (1 to 15) to avoid the cut eye, pink eyes, weird green eyeshadow etc</li>
	 * <li>dress numbers (1 to 16) from the early outfits before lady player base got introduced i.e. they are all unisex</li>
	 * <li>base numbers ( 1 to 15), these are the early bodies which were unisex</li>
	 * </ul>
	 * @return the new random outfit
	 */

	public static Outfit getRandomOutfit() {
		final int newHair = Rand.randUniform(1, 26);
		final int newHead = Rand.randUniform(1, 15);
		final int newDress = Rand.randUniform(1, 16);
		final int newBase = Rand.randUniform(1, 5);
		LOGGER.debug("chose random outfit: "  + newHair + " " + newHead + " " + newDress + " " + newBase);
		return new Outfit(newHair, newHead, newDress, newBase);
	}
	
	/**
	 * Can this outfit be worn with normal clothes
	 *
	 * @return true if the outfit is compatible with clothes, false otherwise
	 */
	public boolean isCompatibleWithClothes() {
		if (base > 26 && base < 37) {
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object other) {

		if (!(other instanceof Outfit)) {
			return false;
		}
		else {
			Outfit outfit = (Outfit)other;
			return this.getCode() == outfit.getCode();
		}

	}

	@Override
	public int hashCode() {
		return this.getCode();
	}

}
