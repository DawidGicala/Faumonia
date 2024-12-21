/* $Id: ElementalDagger1.java,v 1.58 2020/11/01 9:11:07 szyg edited by davvids Exp $ */
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


public class ElementalDagger1 extends AbstractQuest {
	private static final int REQUIRED_IRON = 10;

	private static final int REQUIRED_GOLD_BAR = 3;

	private static final int REQUIRED_WOOD = 15;

	private static final int REQUIRED_KIELBASA = 60;

	private static final int REQUIRED_MINUTES = 90;

	private static final String QUEST_SLOT = "elementalsword_quest";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Żywioł Ognia");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Specjalizuje się w wykuwaniu magicznych mieczy wzmocnionych przez magie żywiołów. Jesteś zainteresowany?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Przecież już dostałeś swój miecz...");
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
					raiser.say("Do wykucia miecza będę potrzebował kilku rzeczy: "
						+ REQUIRED_IRON
						+ " #żelaza, "
						+ REQUIRED_WOOD
						+ " #polan i "
						+ REQUIRED_GOLD_BAR
						+ " #sztabek #złota. Przyda mi sie też coś do jedzenia, przynieś mi "
						+ REQUIRED_KIELBASA
						+ " #kiełbasa #wiejska. Wróć, gdy będziesz je miał #dokładnie w tej kolejności! Jeżeli zapomnisz to powiedz #przypomnij");
					player.setQuest(QUEST_SLOT, "start;0;0;0;0");
					player.addKarma(10);

				}
			});

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Och, naprawdę nie chcesz broni potężnych żywiołów...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		npc.addReply(Arrays.asList("exact", "dokładnie"),
			"Żywioły nie lubią, gdy coś jest nieuporządkowane...");
		npc.addReply(Arrays.asList("kiełbasa", "kiełbasa wiejska"),
			"Pomimo tego że nie jestem człowiekiem, jeść też muszę. Kiełbasa wiejska to mój ulubiony przysmak na tym świecie. Znajdziesz je na farmie koło Semos.");
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Żywioł Ognia");

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
					int neededKielbasa = REQUIRED_KIELBASA
							- Integer.parseInt(tokens[4]);
					boolean missingSomething = false;

					if (!missingSomething && (neededIron > 0)) {
						if (player.isEquipped("żelazo", neededIron)) {
							player.drop("żelazo", neededIron);
							neededIron = 0;
						} else {
							final int amount = player.getNumberOfEquipped("żelazo");
							if (amount > 0) {
								player.drop("żelazo", amount);
								neededIron -= amount;
							}

							raiser.say("Nie mogę wykuć bez "
								+ Grammar.quantityplnoun(
										neededIron, "żelazo", "")
								+ ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededWoodLogs > 0)) {
						if (player.isEquipped("polano", neededWoodLogs)) {
							player.drop("polano", neededWoodLogs);
							neededWoodLogs = 0;
						} else {
							final int amount = player.getNumberOfEquipped("polano");
							if (amount > 0) {
								player.drop("polano", amount);
								neededWoodLogs -= amount;
							}

							raiser.say("Jak możesz wymagać wykucia skoro nie masz "
								+ Grammar.quantityplnoun(neededWoodLogs, "polano","")
								+ " do ognia?");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededGoldBars > 0)) {
						if (player.isEquipped("sztabka złota", neededGoldBars)) {
							player.drop("sztabka złota", neededGoldBars);
							neededGoldBars = 0;
						} else {
							final int amount = player.getNumberOfEquipped("sztabka złota");
							if (amount > 0) {
								player.drop("sztabka złota", amount);
								neededGoldBars -= amount;
							}
							raiser.say("Muszę wzbogacić broń żywiołow złotym kruszcem. Potrzebuję "
									+ Grammar.quantityplnoun(neededGoldBars, "sztabka złota","one") + " więcej.");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededKielbasa > 0)) {
						if (player.isEquipped("kiełbasa wiejska", neededKielbasa)) {
							player.drop("kiełbasa wiejska", neededKielbasa);
							neededKielbasa = 0;
						} else {
							final int amount = player.getNumberOfEquipped("kiełbasa wiejska");
							if (amount > 0) {
								player.drop("kiełbasa wiejska", amount);
								neededKielbasa -= amount;
							}
							raiser.say("kiełbasa wiejska to mój ulubiony smakołyk. Bez niego nie bede pracować. Potrzebuję  "
								+ Grammar.quantityplnoun(neededKielbasa, "kiełbasa wiejska","one") + " wciąż.");
							missingSomething = true;
						}
					}

					if (player.hasKilled("kobold olbrzymi") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Biorę się do wykuwania Twojego sztyletu. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowy.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("kobold olbrzymi") && !missingSomething) {
							raiser.say("Sztylet jest już gotowy.... lecz zanim dostaniesz swój sztylet, musisz udowodnić, że na niego zasługujesz. Pójdź zabić kobolda olbrzymiego!");
						}

						player.setQuest(QUEST_SLOT,
							"start;"
							+ (REQUIRED_IRON - neededIron)
							+ ";"
							+ (REQUIRED_WOOD - neededWoodLogs)
							+ ";"
							+ (REQUIRED_GOLD_BAR - neededGoldBars)
							+ ";"
							+ (REQUIRED_KIELBASA - neededKielbasa));
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
						raiser.say("Jeszcze nie skończyłem wykuwania sztyletu. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					raiser.say("Udowodniłeś, że zasługujesz na ten sztylet. Oto on!");
					player.addXP(50000);
					player.addKarma(10);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("sztylet żywiołów");
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

					final int neededIron = REQUIRED_IRON
							- Integer.parseInt(tokens[1]);
					final int neededWoodLogs = REQUIRED_WOOD
							- Integer.parseInt(tokens[2]);
					final int neededGoldBars = REQUIRED_GOLD_BAR
							- Integer.parseInt(tokens[3]);
					final int neededKielbasa = REQUIRED_KIELBASA
							- Integer.parseInt(tokens[4]);

					raiser.say("Będę potrzebował " + neededIron + " #żelazo, "
							+ neededWoodLogs + " #polano, "
							+ neededGoldBars + " #sztabka złota i "
							+ neededKielbasa + " #kiełbasa wiejskay");
				}
			});

		npc.add(
			ConversationStates.ANY,
			"żelazo",
			null,
			ConversationStates.ATTENDING,
			"Zbierz kilka rud żelaza, które są bogate w minerały a następnie przepal je na sztabkę żelaza u kowala w mieście Semos - Xoderosa.",
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
			Arrays.asList("kobold","olbrzym", "kobold olbrzymi"),
			null,
			ConversationStates.ATTENDING,
			"Są starodawne legendy o olbrzymach żyjących w podziemnym mieście w górach na północ od Semos i Ados. Musisz go pokonać!",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Podstawowy Sztylet Żywiołów",
				"Żywioł Ognia w mieście Fado wykuje dla ciebie miecz żywiołów",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "ElementalDagger1";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem Żywioł Ognia w Fado.");
			if (questState.equals("rejected")) {
				res.add("Odmówiłem wykonania sztyletu żywiołów.");
				return res;
			} 
			res.add("Aby wykuć miecz żywiołów, Żywioł Ognia potrzebuje: " + REQUIRED_IRON
					+ " żelazo, "
					+ REQUIRED_WOOD
					+ " polana, "
					+ REQUIRED_GOLD_BAR
					+ " sztabki zlota i "
					+ REQUIRED_KIELBASA
					+ " kiełbasa wiejskay, dokładnie w tej kolejności.");
			if(questState.startsWith("start") && !"start;15;26;12;6".equals(questState)){
				res.add("Nie dostarczyłem wszystkiego. Żywioł Ognia powie mi co jeszcze potrzebuje.");
			} else if ("start;15;26;12;6".equals(questState) || !questState.startsWith("start")) {
				res.add("Dostarczyłem wszystko co potrzebne dla Żywioła Ognia.");
			}
			if("start;15;26;12;6".equals(questState) && !player.hasKilled("szczur")){
				res.add("Aby zasłużyć na sztylet muszę zabić kobolda olbrzymiego. Powinienem go szukać w jaskiniach Semos-Ados oraz w podziemnym mieście Wofol.");
			} 
			if (questState.startsWith("forging")) {
				res.add("Żywiołak wykuwa mi sztylet żywiołów.");
			} 
			if (isCompleted(player)) {
				res.add("Za sztabki złota, kiełbasa wiejskay i pare innach drobiazgów zostałem nagrodzony sztyletem żywiołów.	W nagrodę za ukończenie zadania otrzymałem: 50.000 pkt doświadczenia, 10 pkt karmy i sztylet żywiołów.");
			}
			return res;	
	}

 	// match to the min level of the elemental dagger
	@Override
	public int getMinLevel() {
		return 30;
	}

	@Override
	public String getNPCName() {
		return "Żywioł Ognia";
	}
	
	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}
}
