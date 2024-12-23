/* $Id: TradeCenterZoneConfigurator.java,v 1.7 2010/09/19 02:35:25 nhnb Exp $ */
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

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.trade.Market;

import java.util.Map;

import marauroa.common.game.RPObject;
/**
 * adds a market to a zone
 * 
 * @author madmetzger
 *
 */
public class TradeCenterZoneConfigurator implements ZoneConfigurator {

	private static final String TRADE_ADVISOR_NAME = "Harold";
	private static final int COORDINATE_Y = 13;
	private static final int COORDINATE_X = 10;

	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		Market market = addShopToZone(zone);
		// start checking for expired offers
		new OfferExpirer(market);
		
		buildTradeCenterAdvisor(zone);
	}

	private Market addShopToZone(StendhalRPZone zone) {
		Market market = getMarketFromZone(zone);
		if (market == null) {
			market = Market.createShop();
			market.setVisibility(0);
			zone.add(market, false);
		}
		
		return market;
	}

	private Market getMarketFromZone(StendhalRPZone zone) {
		for (RPObject rpObject : zone) {
			/*if (rpObject.getRPClass().getName().equals(Market.MARKET_RPCLASS_NAME)) {
				return (Market) rpObject;
			}
			*/
			if (rpObject instanceof Market) {
				return (Market) rpObject;
			}
		}
		return null;
	}

	private void buildTradeCenterAdvisor(StendhalRPZone zone) {
		SpeakerNPC speaker = new MarketManagerNPC(TRADE_ADVISOR_NAME);
		speaker.setPosition(COORDINATE_X,COORDINATE_Y);
		speaker.setEntityClass("tradecenteradvisornpc");
		speaker.setOutfit(new Outfit(5, 1, 34, 1));
		speaker.initHP(100);
		speaker.setDescription("Herold jest przyjaznym facetem, który czeka na utworzenie oferty od ciebie...");
		zone.add(speaker);
	}
	
	public static Market getShopFromZone(StendhalRPZone zone) {
		for (RPObject rpObject : zone) {
			if(rpObject.getRPClass().getName().equals(Market.MARKET_RPCLASS_NAME)) {
				return (Market) rpObject;
			}
		}
		return null;
	}

}