/* $Id: ElementalDagger1.java,v 1.58 2012/04/24 17:01:18 kymara/ edited by szyg Exp $ */
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
package games.stendhal.server.maps.quests;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QUEST: The immortal sword forging.
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Vulcanus, son of Zeus itself, will forge for you the god's sword.
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Vulcanus tells you about the sword.
 * <li> He offers to forge a immortal sword for you if you bring him what it
 * needs.
 * <li> You give him all what he ask you.
 * <li> He tells you you must have killed a giant to get the shield
 * <li> Vulcanus forges the immortal sword for you
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> immortal sword
 * <li>15000 XP
 * </ul>
 * 
 * 
 * REPETITIONS:
 * <ul>
 * <li> None.
 * </ul>
 */
public class obsmedal extends AbstractQuest {
	private static final int REQUIRED_IRON = 1;

	private static final int REQUIRED_GOLD_BAR = 100;

	private static final int REQUIRED_WOOD = 1;

	private static final int REQUIRED_GIANT_HEART = 500;

	private static final int REQUIRED_MINUTES = 1440;

	private static final String QUEST_SLOT = "obsemedal_quest";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Major Klykh");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Nasze wojsko potrzebuję zapasów. Chcesz nam pomóc?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Już przyniosłeś nam to co potrzebowaliśmy.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Dlaczego zawracasz mi głowę?");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Będę potrzebował kilku rzeczy: "
						+ REQUIRED_IRON
						+ " pazura arktycznego smoka "
						+ REQUIRED_WOOD
						+ " szafirowego medalu "
						+ REQUIRED_GOLD_BAR
						+ " sztabek mithrilu"
						+ REQUIRED_GIANT_HEART
						+ "  serc giganta. Wróć, gdy będziesz je miał w tej kolejności! Jeżeli zapomnisz to powiedz #przypomnij");
					player.setQuest(QUEST_SLOT, "start;0;0;0;0");
					player.addKarma(10);

				}
			});

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Och, naprawdę nie chcesz nam pomóc?",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Major Klykh");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					int neededIron = REQUIRED_IRON
							- Integer.parseInt(tokens[1]);
					int neededWoodLogs = REQUIRED_WOOD
							- Integer.parseInt(tokens[2]);
					int neededGoldBars = REQUIRED_GOLD_BAR
							- Integer.parseInt(tokens[3]);
					int neededGiantHearts = REQUIRED_GIANT_HEART
							- Integer.parseInt(tokens[4]);
					boolean missingSomething = false;

					if (!missingSomething && (neededIron > 0)) {
						if (player.isEquipped("pazur arktycznego smoka", neededIron)) {
							player.drop("pazur arktycznego smoka", neededIron);
							neededIron = 0;
						} else {
							final int amount = player.getNumberOfEquipped("pazur arktycznego smoka");
							if (amount > 0) {
								player.drop("pazur arktycznego smoka", amount);
								neededIron -= amount;
							}

							raiser.say("Potrzebujemy "
								+ Grammar.quantityplnoun(
										neededIron, "pazur arktycznego smoka", "")
								+ ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededWoodLogs > 0)) {
						if (player.isEquipped("szafirowy medal", neededWoodLogs)) {
							player.drop("szafirowy medal", neededWoodLogs);
							neededWoodLogs = 0;
						} else {
							final int amount = player.getNumberOfEquipped("szafirowy medal");
							if (amount > 0) {
								player.drop("szafirowy medal", amount);
								neededWoodLogs -= amount;
							}

							raiser.say("Nie mam jak cię awansować bez "
								+ Grammar.quantityplnoun(neededWoodLogs, "szafirowy medal","")
								+ ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededGoldBars > 0)) {
						if (player.isEquipped("sztabka mithrilu", neededGoldBars)) {
							player.drop("sztabka mithrilu", neededGoldBars);
							neededGoldBars = 0;
						} else {
							final int amount = player.getNumberOfEquipped("sztabka mithrilu");
							if (amount > 0) {
								player.drop("sztabka mithrilu", amount);
								neededGoldBars -= amount;
							}
							raiser.say("Potrzebujemy materiały do zbroji. Przynieś "
									+ Grammar.quantityplnoun(neededGoldBars, "sztabka mithrilu","one") + " więcej.");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededGiantHearts > 0)) {
						if (player.isEquipped("serce olbrzyma", neededGiantHearts)) {
							player.drop("serce olbrzyma", neededGiantHearts);
							neededGiantHearts = 0;
						} else {
							final int amount = player.getNumberOfEquipped("serce olbrzyma");
							if (amount > 0) {
								player.drop("serce olbrzyma", amount);
								neededGiantHearts -= amount;
							}
							raiser.say("Potrzebujemy serc olbrzyma do wytwarzania mikstur. Nadal potrzebujemy  "
								+ Grammar.quantityplnoun(neededGiantHearts, "serce olbrzyma","one") + " wciąż.");
							missingSomething = true;
						}
					}

					if (player.hasKilled("Smok Wawelski") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Za niedługo zostaniesz odznaczony. Wróć za  "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowy.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("Smok Wawelski") && !missingSomething) {
							raiser.say("Zanim dostanisz medal zabij Smoka Wawelskiego !");
						}

						player.setQuest(QUEST_SLOT,
							"start;"
							+ (REQUIRED_IRON - neededIron)
							+ ";"
							+ (REQUIRED_WOOD - neededWoodLogs)
							+ ";"
							+ (REQUIRED_GOLD_BAR - neededGoldBars)
							+ ";"
							+ (REQUIRED_GIANT_HEART - neededGiantHearts));
					}
				}
			});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;")),
			ConversationStates.IDLE, null, new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					
					final long delay = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE; 
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();

					if (timeRemaining > 0L) {
						raiser.say("Musisz jeszcze poczekać. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					raiser.say("Proszę bardzo, oto Twoje odznaczenie.");
					player.addXP(10000000);
					player.addatk_xp(100000);
					player.adddef_xp(150000);
					player.addKarma(500);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("diamentowy medal");
					magicSword.setBoundTo(player.getName());
					player.equipOrPutOnGround(magicSword);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("odznaczenie", "missing", "medal", "brakuje", "lista", "przypomnij"), 
			new QuestStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					final int neededIron = REQUIRED_IRON
							- Integer.parseInt(tokens[1]);
					final int neededWoodLogs = REQUIRED_WOOD
							- Integer.parseInt(tokens[2]);
					final int neededGoldBars = REQUIRED_GOLD_BAR
							- Integer.parseInt(tokens[3]);
					final int neededGiantHearts = REQUIRED_GIANT_HEART
							- Integer.parseInt(tokens[4]);

					raiser.say("Będę potrzebował " + neededIron + " pazura arktycznego smoka, "
							+ neededWoodLogs + " szafirowy medal "
							+ neededGoldBars + " sztabka mithrilu i "
							+ neededGiantHearts + " serc giganta.");
				}
			});

		npc.add(
			ConversationStates.ANY,
			"pazur arktycznego smoka",
			null,
			ConversationStates.ATTENDING,
			"Przynieś pazur arktycznego smoka aby udowodnić swoją potężną siłę.",
			null);

		npc.add(ConversationStates.ANY, "szafirowy medal", null,
				ConversationStates.ATTENDING,
				"Przynieś szafirowy medal aby udowodnić mi, że pomogłeś Trenerowi Magów.", null);
		npc.add(ConversationStates.ANY, "mithril", null,
				ConversationStates.ATTENDING,
				"Kowal w Ados może dla Ciebie odlać bryłki mithrilu w sztabki mithrilu.",
				null);
		npc.add(
			ConversationStates.ANY,
			Arrays.asList("giant","olbrzyma", "olbrzym"),
			null,
			ConversationStates.ATTENDING,
			"Są starodawne legendy o olbrzymach żyjących w górach na północ od Semos i Ados.",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Rzeczy dla Majora Klykha",
				"Major Klykh stacjonujący w twierdzy Ados potrzebuje zapasów.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "obsmedal";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem Majora Khylka w twierdzy Ados.");
			if (questState.equals("rejected")) {
				res.add("Nie pomogę Majorowi Klykhowi.");
				return res;
			} 
			res.add("Major Klykh poprosił mnie o przyniesienie: " + REQUIRED_IRON
					+ " pazur arktycznego smoka, "
					+ REQUIRED_WOOD
					+ " szafirowy medal, "
					+ REQUIRED_GOLD_BAR
					+ " sztabki mithrilu i "
					+ REQUIRED_GIANT_HEART
					+ " serc olbrzyma, dokładnie w tej kolejności.");
			// yes, yes. this is the most horrible quest code and so you get a horrible quest history. 
			if(questState.startsWith("start") && !"start;15;26;12;6".equals(questState)){
				res.add("Nie dostarczyłem wszystkiego. Major Klykh powie mi co jeszcze potrzebuje.");
			} else if ("start;15;26;12;6".equals(questState) || !questState.startsWith("start")) {
				res.add("Dostarczyłem wszystko co potrzebne dla Majora Klykha.");
			}
			if("start;15;26;12;6".equals(questState) && !player.hasKilled("Smok Wawelski")){
				res.add("Aby pokazać swoją siłę muszę pokonać Smoka Wawelskiego !");
			} 
			if (questState.startsWith("forging")) {
				res.add("Major Klykh kazał mi poczekać na moją odznakę.");
			} 
			if (isCompleted(player)) {
				res.add("Pomogłem Majorowi Klykhowi i trenerom w twierdzy Ados i dostałem diamentowe odznaczenie.");
			}
			return res;	
	}

 	// match to the min level of the immortal sword
	@Override
	public int getMinLevel() {
		return 150;
	}

	@Override
	public String getNPCName() {
		return "Major Klykh";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
