/* $Id: ShowOfferItemsChatAction.java,v 1.7 2011/05/01 19:50:07 martinfuchs Exp $ */
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
package games.stendhal.server.maps.semos.tavern.market;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.trade.Market;
import games.stendhal.server.entity.trade.Offer;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * show a list of all items for which offers exist.
 */
public class ShowOfferItemsChatAction implements ChatAction {

	/**
	 * show a list of all items for which offers exist.
	 */
	public void fire(Player player, Sentence sentence, EventRaiser npc) {
		Market market = TradeCenterZoneConfigurator.getShopFromZone(player.getZone());
		RPSlot offersSlot = market.getSlot(Market.OFFERS_SLOT_NAME);
		List<Offer> offers = getOffers(offersSlot);
		if (offers.isEmpty()) {
			npc.say("Przykro mi, ale obecnie brak ofert:");
		} else {
			String text = buildItemListText(buildItemList(offers));
			npc.say(text);
		}
	}

	/**
	 * gets a list of all offers
	 *
	 * @param slot slot to get the offers from
	 * @return list of offers
	 */
	private List<Offer> getOffers(RPSlot slot) {
		LinkedList<Offer> offers = new LinkedList<Offer>();
		for (RPObject rpObject : slot) {
			offers.add((Offer) rpObject);
		}
		return offers;
	}

	/**
	 * creates an alphabetically sorted set of items for which offers exist
	 *
	 * @param offers list of offers
	 * @return set of items
	 */
	private Set<String> buildItemList(List<Offer> offers) {
		Set<String> items = new TreeSet<String>();
		for (Offer offer : offers) {
			items.add(offer.getItemName());
		}
		return items;
	}

	/**
	 * creates the response text based on the item set
	 *
	 * @param items items to list
	 * @return text for the NPC to say
	 */
	private String buildItemListText(Set<String> items) {
		StringBuilder sb = new StringBuilder();
		sb.append("Mam w ofercie następujące przedmioty: ");
		boolean first = true;
		for (String item : items) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append("#'" + item + "'");
		}
		sb.append("Powiedz #'pokaż <nazwa przedmiotu>', aby poznać cenę oraz ilość danego towaru.");
		return sb.toString();
	}
}
