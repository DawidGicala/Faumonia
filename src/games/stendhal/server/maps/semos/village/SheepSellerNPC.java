/* $Id: SheepSellerNPC.java,v 1.30 2012/01/18 14:02:45 bluelads99 Exp $ */
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
package games.stendhal.server.maps.semos.village;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SheepSellerNPC implements ZoneConfigurator {

	public static final int BUYING_PRICE = 30;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosVillageArea(zone);
	}

	private void buildSemosVillageArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Nishiya") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(33, 44));
				nodes.add(new Node(33, 43));
				nodes.add(new Node(24, 43));
				nodes.add(new Node(24, 44));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				class SheepSellerBehaviour extends SellerBehaviour {
					SheepSellerBehaviour(final Map<String, Integer> items) {
						super(items);
					}

					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
						if (res.getAmount() > 1) {
							seller.say("Hmm... Nie sądzę, abyś mógł zaopiekować się więcej niż jedną owcą naraz.");
							return false;
						} else if (!player.hasSheep()) {
							if (!player.drop("money", getCharge(res,player))) {
								seller.say("Nie masz tyle pieniędzy.");
								return false;
							}
							seller.say("Proszę bardzo, oto miła, puszysta mała owieczka! Opiekuj się nią dobrze...");

							final Sheep sheep = new Sheep(player);
							StendhalRPAction.placeat(seller.getZone(), sheep, seller.getX(), seller.getY() + 1);

							player.notifyWorldAboutChanges();

							return true;
						} else {
							say("Dlaczego się nie upewnisz się i nie poszukasz owcy, którą już masz?");
							return false;
						}
					}
				}

				final Map<String, Integer> items = new HashMap<String, Integer>();
				items.put("sheep", BUYING_PRICE);

				addGreeting();
				addJob("Pracuję jako sprzedawca owiec.");
				addHelp("Sprzedaję owce. Aby kupić jedną wystarczy powiedzieć mi #buy #sheep. Jeżeli jesteś nowym w tym interesie to mogę Ci powiedzieć jak #podróżować z owcą, jak się nią #opiekować i powiem Ci, gdzie możesz ją #sprzedać. Jeżeli znajdziesz dziką owcę to możesz ją #przygarnąć.");
				addGoodbye();
				new SellerAdder().addSeller(this, new SheepSellerBehaviour(items));
				addReply(Arrays.asList("care", "opiekować"),
						"Moja owca kocha jeść brusznice - takie czerwone jagody, które rosną na małych krzaczkach. Stań obok jednego, a twoja owca podejdzie i zacznie jeść. Możesz sprawdzić jej wagę klikając na nią prawym przyciskiem i wybierając 'Zobacz'. Jej waga będzie rosła wraz ze zjedzeniem każdej jagody.");
				addReply(Arrays.asList("travel", "podróżować"),
						"Gdy zmieniasz miejsce pobytu twoja owca powinna być blisko Ciebie, aby nie zginęła. Jeżeli nie zwraca na Ciebie uwagi wystarczy powiedzieć #sheep aby ją wezwać. Jeżeli zdecydujesz się ją porzucić to kliknij na siebie prawym przyciskiem i wybierz 'Porzuć owcę', ale szczerze sądzę, że takie zachowanie jest odrażające.");
				addReply(Arrays.asList("sell", "sprzedać"),
						"Kiedy twoja owca osiągnie wagę 100 to możesz ja zabrać do Sato w Semos, a on kupi ją od Ciebie.");
				addReply(Arrays.asList("own", "przygarnąć"),
						"Jeżeli znajdziesz dziką lub porzuconą owcę to aby ją przygarnąć możesz kliknąć na nią prawym przyciskiem i wybrać 'Przygarnij'. Później musisz zobaczyć na owcę!");
			}
		};

		npc.setEntityClass("sellernpc");
		npc.setDescription("Nishiya patroluje ścieżki szukając owiec. Możesz kupić jedną od niego.");
		npc.setPosition(33, 44);
		npc.initHP(100);
		zone.add(npc);
	}
}
