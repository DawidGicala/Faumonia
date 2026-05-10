/* $Id: StendhalQuestSystem.java,v 1.107 2026/05/03 18:29:30 davvids Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;

public class FindDragons extends AbstractQuest {
	private static final String QUEST_SLOT = "find_dragons";
	private static final int DRAGONS_TO_FIND = 11;

	private final HashMap<String, String> dragonHistory = new HashMap<String,String>();

	private void fillHistoryMap() {
		dragonHistory.put("Gorath",     "Gorath przebywał przy Superczłowiekach w Ados.");
		dragonHistory.put("Velmord",    "Velmord przebywał w smoczej jaskini.");
		dragonHistory.put("Saphiron",   "Saphiron przebywał w górach Semos.");
		dragonHistory.put("Ardavyr",    "Ardavyr przebywał na dzikiej wyspie.");
		dragonHistory.put("Kharion",    "Kharion przebywał w jaskini Kikareukin.");
		dragonHistory.put("Morthax",    "Morthax przebywał w tunelu amazonek.");
		dragonHistory.put("Dravok",     "Dravok przebywał na smoczej wyspie.");
		dragonHistory.put("Ignathar",   "Ignathar przebywał w piekle.");
		dragonHistory.put("Faldurion",  "Faldurion przebywał w wielkiej jaskini Fado.");
		dragonHistory.put("Nalthar",    "Nalthar przebywał w tunelach mrocznych elfów Nalwor.");
		dragonHistory.put("Atherion",   "Atherion przebywał na zachodniej części wyspy Athor.");
	}

	@Override
	public boolean isCompleted(final Player player) {
		if (!player.hasQuest(QUEST_SLOT)) {
			return false;
		}
		final String npcDoneText = player.getQuest(QUEST_SLOT);
		final String[] done = npcDoneText.split(";");
		final int left = DRAGONS_TO_FIND - done.length;
		return left < 0;
	}

	static class DragonNPC extends SpeakerNPC {
		public DragonNPC(final String name, final int x, final int y) {
			super(name);

			setEntityClass("dragonNPC");
			setPosition(x, y);
			initHP(100);

			final List<Node> nodes = new LinkedList<Node>();
			nodes.add(new Node(x, y));
			nodes.add(new Node(x - 4, y));
			nodes.add(new Node(x - 4, y - 2));
			nodes.add(new Node(x, y - 2));
			setPath(new FixedPath(nodes, true));
		}

		@Override
		protected void createPath() {
			// do nothing
		}

		@Override
		protected void createDialog() {
			add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new GreetingMatchesNameCondition(getName()), true,
				ConversationStates.IDLE, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (!player.hasQuest(QUEST_SLOT)) {
							player.setQuest(QUEST_SLOT, "");
						}

						final String npcDoneText = player.getQuest(QUEST_SLOT);
						final String[] done = npcDoneText.split(";");
						final List<String> list = Arrays.asList(done);
						final int left = DRAGONS_TO_FIND - list.size();

						if (list.contains(raiser.getName())) {
							if (left > -1) {
								raiser.say("Tego smoka już odnalazłeś. Poszukaj pozostałych, a zostaniesz wynagrodzony!");
							} else {
								raiser.say("Znalazłeś wszystkie smoki. Nagroda jest już w Twoich rękach.");
							}
						} else {
							player.setQuest(QUEST_SLOT, npcDoneText + ";"
									+ raiser.getName());
							player.heal();

							if (left > 0) {
								raiser.say("Witaj! Mnie już odnalazłeś. Poszukaj jeszcze "
												+ (DRAGONS_TO_FIND - list.size())
												+ " z moich braci. Żegnaj!");
								player.addXP((DRAGONS_TO_FIND - left + 1) * 500);
							} else {
								raiser.say("Udowodniłeś, że jesteś godny smoczej nagrody!");

								final Item item = SingletonRepository.getEntityManager()
									.getItem("płaszcz ze smoczych łusek");
								item.setBoundTo(player.getName());
								player.equipOrPutOnGround(item);
								player.addXP(100000);
							}
						}
						player.notifyWorldAboutChanges();
					}
				});
			addGoodbye();
		}
	}

	@Override
	public void addToWorld() {
		final StendhalRPWorld world = SingletonRepository.getRPWorld();
		fillHistoryMap();
		fillQuestInfo(
			"Poszukiwanie Smoków",
			"Odnajdź wszystkie 11 smoków rozsianych po krainie Faumonii.",
			false);

		StendhalRPZone zone;
		SpeakerNPC npc;

		zone = world.getZone("-1_ados_wall");
		npc = new DragonNPC("Gorath", 58, 87);
		zone.add(npc);

		zone = world.getZone("-1_dragon_land_cave");
		npc = new DragonNPC("Velmord", 137, 86);
		zone.add(npc);

		zone = world.getZone("0_semos_mountain_n2_e");
		npc = new DragonNPC("Saphiron", 16, 9);
		zone.add(npc);

		zone = world.getZone("0_wild_island_e");
		npc = new DragonNPC("Ardavyr", 97, 35);
		zone.add(npc);

		zone = world.getZone("2_kikareukin_cave");
		npc = new DragonNPC("Kharion", 14, 79);
		zone.add(npc);

		zone = world.getZone("-1_amazon_tunnel_e");
		npc = new DragonNPC("Morthax", 19, 94);
		zone.add(npc);

		zone = world.getZone("0_dragon_island");
		npc = new DragonNPC("Dravok", 63, 27);
		zone.add(npc);

		zone = world.getZone("hell");
		npc = new DragonNPC("Ignathar", 53, 38);
		zone.add(npc);

		zone = world.getZone("-1_fado_great_cave");
		npc = new DragonNPC("Faldurion", 9, 45);
		zone.add(npc);

		zone = world.getZone("-1_nalwor_drows_tunnel_nw");
		npc = new DragonNPC("Nalthar", 43, 55);
		zone.add(npc);

		zone = world.getZone("0_athor_island_w");
		npc = new DragonNPC("Atherion", 90, 59);
		zone.add(npc);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			final String npcDoneText = player.getQuest(QUEST_SLOT);
			final String[] done = npcDoneText.split(";");
			boolean first = true;
			for (final String dragon : done) {
				if (!dragon.trim().equals("")) {
					res.add(dragon.toUpperCase());
					if (first) {
						first = false;
						res.add("Podjąłem się znalezienia wszystkich smoków.");
					}
					res.add(dragonHistory.get(dragon));
				}
			}
			if (isCompleted(player)) {
				res.add("Znalazłem Smoki i dostałem nagrodę.");
			}
		}
		return res;
	}

	@Override
	public int getMinLevel() {
		return 300;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Poszukiwanie Smoków";
	}
}