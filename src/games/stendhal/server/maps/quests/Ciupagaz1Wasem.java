/* $Id: Ciupagaz1Wasem.java,v 1.58 2025/06/21 00:51:07 davvids Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2020 - Stendhal                    *
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


public class Ciupagaz1Wasem extends AbstractQuest {
	private static final int REQUIRED_CIUPAGA = 1;
	private static final int REQUIRED_MONEY = 1000000;
	private static final int REQUIRED_GOLD_BAR = 200;
	private static final int REQUIRED_NIEBIANSKIE_PIORO = 3;
	private static final int REQUIRED_TYTANOWA_SKALA = 1;
	private static final int REQUIRED_GREEN_CLAW = 1;
	private static final int REQUIRED_GOLDEN_CLAW = 1;
	private static final int REQUIRED_BLACK_CLAW = 1;
	private static final int REQUIRED_KIEL_KOSTUCHY = 1;

	private static final int REQUIRED_MINUTES = 1440;

	private static final String QUEST_SLOT = "ciupaga_1_was";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Ludwik");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Specjalizuje się w ulepszaniu złotych ciupag o dodatkowy wąs. Jesteś zainteresowany?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Przecież już dostałeś swoją ciupagę...");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Dlaczego zawracasz mi głowę skoro nie ukończyłeś zadania?");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Do ulepszenia złotej ciupagi o dodatkowego wąsa, będę potrzebował kilku rzeczy: "
						+ REQUIRED_CIUPAGA
						+ " złotą ciupagę, "
						+ REQUIRED_MONEY
						+ " złota, "
						+ REQUIRED_GOLD_BAR
						+ " sztabek złota, "
						+ REQUIRED_NIEBIANSKIE_PIORO
						+ " niebiańskich piór, "
						+ REQUIRED_TYTANOWA_SKALA
						+ " tytanową skałę, "
						+ REQUIRED_GREEN_CLAW
						+ " pazur zielonego smoka, "
						+ REQUIRED_GOLDEN_CLAW
						+ " pazur złotego smoka, "
						+ REQUIRED_BLACK_CLAW
						+ " pazur czarnego smoka. Potrzebuje również kieł starożytnej bestii, przynieś mi "
						+ REQUIRED_KIEL_KOSTUCHY
						+ " kieł złotej kostuchy. Wróć, gdy będziesz je miał #dokładnie w tej kolejności! Jeżeli zapomnisz to powiedz #przypomnij");
					player.setQuest(QUEST_SLOT, "start;0;0;0;0;0;0;0;0;0");
					player.addKarma(10);

				}
			});

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Och, naprawdę nie chcesz wzmocnić swojej złotej ciupagi wąsem...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		npc.addReply(Arrays.asList("exact", "dokładnie"),
			"Wszystko musi być uporządkowane...");
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Ludwik");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					int neededCiupaga = REQUIRED_CIUPAGA
							- Integer.parseInt(tokens[1]);
					int neededMoney = REQUIRED_MONEY
							- Integer.parseInt(tokens[2]);
					int neededGoldBar = REQUIRED_GOLD_BAR
							- Integer.parseInt(tokens[3]);
					int neededNiebianskiePioro = REQUIRED_NIEBIANSKIE_PIORO
							- Integer.parseInt(tokens[4]);
					int neededTytanowaSkala = REQUIRED_TYTANOWA_SKALA
							- Integer.parseInt(tokens[5]);
					int neededGreenClaw = REQUIRED_GREEN_CLAW
							- Integer.parseInt(tokens[6]);
					int neededGoldenClaw = REQUIRED_GOLDEN_CLAW
							- Integer.parseInt(tokens[7]);
					int neededBlackClaw = REQUIRED_BLACK_CLAW
							- Integer.parseInt(tokens[8]);
					int neededKielKostuchy = REQUIRED_KIEL_KOSTUCHY
							- Integer.parseInt(tokens[9]);		
					boolean missingSomething = false;

					if (!missingSomething && (neededCiupaga > 0)) {
						if (player.isEquipped("złota ciupaga", neededCiupaga)) {
							player.drop("złota ciupaga", neededCiupaga);
							neededCiupaga = 0;
						} else {
							final int amount = player.getNumberOfEquipped("złota ciupaga");
							if (amount > 0) {
								player.drop("złota ciupaga", amount);
								neededCiupaga -= amount;
							}

							raiser.say("Nie mogę wykuć bez "
								+ Grammar.quantityplnoun(
										neededCiupaga, "złota ciupaga", "")
								+ ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededMoney > 0)) {
						if (player.isEquipped("money", neededMoney)) {
							player.drop("money", neededMoney);
							neededMoney = 0;
						} else {
							final int amount = player.getNumberOfEquipped("money");
							if (amount > 0) {
								player.drop("money", amount);
								neededMoney -= amount;
							}

							raiser.say("Jak możesz wymagać wykucia skoro nie masz "
								+ Grammar.quantityplnoun(neededMoney, "money","")
								+ " ?");
							missingSomething = true;
						}
					}
					
					if (!missingSomething && (neededGoldBar > 0)) {
						if (player.isEquipped("sztabka złota", neededGoldBar)) {
							player.drop("sztabka złota", neededGoldBar);
							neededGoldBar = 0;
						} else {
							final int amount = player.getNumberOfEquipped("sztabka złota");
							if (amount > 0) {
								player.drop("sztabka złota", amount);
								neededGoldBar -= amount;
							}

							raiser.say("Jak możesz wymagać wykucia skoro nie masz "
								+ Grammar.quantityplnoun(neededGoldBar, "sztabka złota","")
								+ " ?");
							missingSomething = true;
						}
					}
					
					if (!missingSomething && (neededNiebianskiePioro > 0)) {
						if (player.isEquipped("niebiańskie pióro", neededNiebianskiePioro)) {
							player.drop("niebiańskie pióro", neededNiebianskiePioro);
							neededNiebianskiePioro = 0;
						} else {
							final int amount = player.getNumberOfEquipped("niebiańskie pióro");
							if (amount > 0) {
								player.drop("niebiańskie pióro", amount);
								neededNiebianskiePioro -= amount;
							}

							raiser.say("Jak możesz wymagać wykucia skoro nie masz "
								+ Grammar.quantityplnoun(neededNiebianskiePioro, "niebiańskie pióro","")
								+ " ?");
							missingSomething = true;
						}
					}
					
					if (!missingSomething && (neededTytanowaSkala > 0)) {
						if (player.isEquipped("tytanowa skała", neededTytanowaSkala)) {
							player.drop("tytanowa skała", neededTytanowaSkala);
							neededTytanowaSkala = 0;
						} else {
							final int amount = player.getNumberOfEquipped("tytanowa skała");
							if (amount > 0) {
								player.drop("tytanowa skała", amount);
								neededTytanowaSkala -= amount;
							}

							raiser.say("Jak możesz wymagać wykucia skoro nie masz "
								+ Grammar.quantityplnoun(neededTytanowaSkala, "tytanowa skała","")
								+ " ?");
							missingSomething = true;
						}
					}
					
					if (!missingSomething && (neededGreenClaw > 0)) {
						if (player.isEquipped("pazur zielonego smoka", neededGreenClaw)) {
							player.drop("pazur zielonego smoka", neededGreenClaw);
							neededGreenClaw = 0;
						} else {
							final int amount = player.getNumberOfEquipped("pazur zielonego smoka");
							if (amount > 0) {
								player.drop("pazur zielonego smoka", amount);
								neededGreenClaw -= amount;
							}

							raiser.say("Jak możesz wymagać wykucia skoro nie masz "
								+ Grammar.quantityplnoun(neededGreenClaw, "pazur zielonego smoka","")
								+ " ?");
							missingSomething = true;
						}
					}
					
					if (!missingSomething && (neededGoldenClaw > 0)) {
						if (player.isEquipped("pazur złotego smoka", neededGoldenClaw)) {
							player.drop("pazur złotego smoka", neededGoldenClaw);
							neededGoldenClaw = 0;
						} else {
							final int amount = player.getNumberOfEquipped("pazur złotego smoka");
							if (amount > 0) {
								player.drop("pazur złotego smoka", amount);
								neededGoldenClaw -= amount;
							}

							raiser.say("Jak możesz wymagać wykucia skoro nie masz "
								+ Grammar.quantityplnoun(neededGoldenClaw, "pazur złotego smoka","")
								+ " ?");
							missingSomething = true;
						}
					}
					
					if (!missingSomething && (neededBlackClaw > 0)) {
						if (player.isEquipped("pazur czarnego smoka", neededBlackClaw)) {
							player.drop("pazur czarnego smoka", neededBlackClaw);
							neededBlackClaw = 0;
						} else {
							final int amount = player.getNumberOfEquipped("pazur czarnego smoka");
							if (amount > 0) {
								player.drop("pazur czarnego smoka", amount);
								neededBlackClaw -= amount;
							}

							raiser.say("Jak możesz wymagać wykucia skoro nie masz "
								+ Grammar.quantityplnoun(neededBlackClaw, "pazur czarnego smoka","")
								+ " ?");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededKielKostuchy > 0)) {
						if (player.isEquipped("kieł złotej kostuchy", neededKielKostuchy)) {
							player.drop("kieł złotej kostuchy", neededKielKostuchy);
							neededKielKostuchy = 0;
						} else {
							final int amount = player.getNumberOfEquipped("kieł złotej kostuchy");
							if (amount > 0) {
								player.drop("kieł złotej kostuchy", amount);
								neededKielKostuchy -= amount;
							}
							raiser.say("Kieł złotej kostuchy zdobędziesz ze starożytnego potwora. Bez niego nie bede pracować. Potrzebuję  "
								+ Grammar.quantityplnoun(neededKielKostuchy, "kieł złotej kostuchy","one") + " wciąż.");
							missingSomething = true;
						}
					}

					if (player.hasKilled("serafin") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Biorę się do wykuwania złotej ciupagi z wąsem. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowa.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("serafin") && !missingSomething) {
							raiser.say("Złota ciupaga z wąsem jest już gotowa.... lecz zanim ją dostaniesz, musisz udowodnić, że na nią zasługujesz. Idź i zabij serafina!");
						}

						player.setQuest(QUEST_SLOT,
							"start;"
							+ (REQUIRED_CIUPAGA - neededCiupaga)
							+ ";"
							+ (REQUIRED_MONEY - neededMoney)
							+ ";"
							+ (REQUIRED_GOLD_BAR - neededGoldBar)
							+ ";"
							+ (REQUIRED_NIEBIANSKIE_PIORO - neededNiebianskiePioro)
							+ ";"
							+ (REQUIRED_TYTANOWA_SKALA - neededTytanowaSkala)
							+ ";"
							+ (REQUIRED_GREEN_CLAW - neededGreenClaw)
							+ ";"
							+ (REQUIRED_GOLDEN_CLAW - neededGoldenClaw)
							+ ";"
							+ (REQUIRED_BLACK_CLAW - neededBlackClaw)
							+ ";"
							+ (REQUIRED_KIEL_KOSTUCHY - neededKielKostuchy));
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
						raiser.say("Jeszcze nie skończyłem wykuwania ciupagi. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					raiser.say("Udowodniłeś, że zasługujesz na złotą ciupagę z wąsem. Oto ona!");
					player.addXP(1000000);
					player.addKarma(100);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("złota ciupaga z wąsem");
					magicSword.setBoundTo(player.getName());
					player.equipOrPutOnGround(magicSword);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("forge", "missing", "wykuj", "brakuje", "lista", "przypomnij"), 
			new QuestStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					final int neededCiupaga = REQUIRED_CIUPAGA
							- Integer.parseInt(tokens[1]);
					final int neededMoney = REQUIRED_MONEY
							- Integer.parseInt(tokens[2]);
					final int neededGoldBar = REQUIRED_GOLD_BAR
							- Integer.parseInt(tokens[3]);
					final int neededNiebianskiePioro = REQUIRED_NIEBIANSKIE_PIORO
							- Integer.parseInt(tokens[4]);
					final int neededTytanowaSkala = REQUIRED_TYTANOWA_SKALA
							- Integer.parseInt(tokens[5]);
					final int neededGreenClaw = REQUIRED_GREEN_CLAW
							- Integer.parseInt(tokens[6]);
					final int neededGoldenClaw = REQUIRED_GOLDEN_CLAW
							- Integer.parseInt(tokens[7]);
					final int neededBlackClaw = REQUIRED_BLACK_CLAW
							- Integer.parseInt(tokens[8]);
					final int neededKielKostuchy = REQUIRED_KIEL_KOSTUCHY
							- Integer.parseInt(tokens[9]);

					raiser.say("Będę potrzebował " + neededCiupaga + " złota ciupaga, "
							+ neededMoney + " złota, "
							+ neededGoldBar + " złotych sztabek, "
							+ neededNiebianskiePioro + " niebiańskich piór, "
							+ neededTytanowaSkala + " tytanową skałę, "
							+ neededGreenClaw + " pazur zielonego smoka, "
							+ neededGoldenClaw + " pazur złotego smoka, "
							+ neededBlackClaw + " pazur czarnego smoka i "
							+ neededKielKostuchy + " kieł złotej kostuchyy");
				}
			});

		npc.add(
			ConversationStates.ANY,
			"złota ciupaga z wąsem",
			null,
			ConversationStates.ATTENDING,
			"Kowal Ludwik wykuje dla Ciebie legendarną ciupagę - złotą ciupage z wąsem.",
			null);

		npc.add(ConversationStates.ANY, "polano", null,
				ConversationStates.ATTENDING,
				"W lesie jest pełno drewna.", null);
		npc.add(ConversationStates.ANY, "złoto", null,
				ConversationStates.ATTENDING,
				"Zbierz kilka grudek złota a następnie przepal je na sztabki złota u kowala w stolicy Ados - Joshua.",
				null);
		npc.add(
			ConversationStates.ANY,
			Arrays.asList("kobold","olbrzym", "serafin"),
			null,
			ConversationStates.ATTENDING,
			"Są starodawne legendy o olbrzymach żyjących w podziemnym mieście w górach na północ od Semos i Ados.",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Złota ciupaga z wąsem",
				"Kowal Ludwik wykuje dla Ciebie legendarną ciupagę - złotą ciupage z wąsem.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "Ciupagaz1Wasem";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem Ludwika w zbójeckiej wiosce.");
			if (questState.equals("rejected")) {
				res.add("Odmówiłem wykonania złotej ciupagi z wąsem.");
				return res;
			} 
			res.add("Aby wykuć złotą ciupage z wąsem, Ludwik potrzebuje: " + REQUIRED_CIUPAGA
					+ " złotą ciupagę, "
					+ REQUIRED_MONEY
					+ " złota, "
					+ REQUIRED_GOLD_BAR
					+ " sztabek zlota"
					+ REQUIRED_NIEBIANSKIE_PIORO
					+ " niebiańskie pióra "
					+ REQUIRED_TYTANOWA_SKALA
					+ " tytanową skałę "
					+ REQUIRED_GREEN_CLAW
					+ " pazur zielonego smoka "
					+ REQUIRED_GOLDEN_CLAW
					+ " pazur złotego smoka "
					+ REQUIRED_BLACK_CLAW
					+ " pazur czarnego smoka i "
					+ REQUIRED_KIEL_KOSTUCHY
					+ " kieł złotej kostuchyy, dokładnie w tej kolejności.");
			if(questState.startsWith("start") && !"start;15;26;12;6".equals(questState)){
				res.add("Nie dostarczyłem wszystkiego. Ludwik powie mi co jeszcze potrzebuje.");
			} else if ("start;15;26;12;6".equals(questState) || !questState.startsWith("start")) {
				res.add("Dostarczyłem wszystko co potrzebne.");
			}
			if("start;15;26;12;6".equals(questState) && !player.hasKilled("szczur")){
				res.add("Aby zasłużyć na złotą ciupagę z wąsem musze zabić serafina. Powinienem szukać go wysoko w górach.");
			} 
			if (questState.startsWith("forging")) {
				res.add("Ludwik wykuwa moją złotą ciupagę z wąsem.");
			} 
			if (isCompleted(player)) {
				res.add("Za trochę surowców otrzymałem legendarną broń - złotą ciupagę z wąsem.	W nagrodę za ukończenie zadania otrzymałem: 1.000.000 pkt doświadczenia, 100 pkt karmy i złotą ciupagę z wąsem.");
			}
			return res;	
	}

 	// match to the min level of the elemental dagger
	@Override
	public int getMinLevel() {
		return 250;
	}

	@Override
	public String getNPCName() {
		return "Ludwik";
	}
	
	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}
}