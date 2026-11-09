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
						raiser.say("Ogień może zahartować stal albo obrócić ją w pył. Wykuję dla ciebie sztylet żywiołów, jeśli dostarczysz materiały i dowiedziesz odwagi w walce z koboldem olbrzymim. Chcesz złożyć takie zamówienie?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Sztylet żywiołów jest już w twoich rękach. Ogień zrobił swoje; teraz od ciebie zależy, jak wykorzystasz ostrze.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Pamiętam nasze ustalenia i wszystkie przekazane materiały. Możesz teraz sprawdzić braki albo oddać kolejną część dostawy. Gdy zbierzesz wszystko i spełnisz warunek walki, rozpocznę pracę.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Do wykucia sztyletu potrzebuję materiałów w tej kolejności: " + games.stendhal.server.core.rp.quest.JourneyQuestProgress.requirementsText(ElementalDagger1.this) + ". Możesz donosić je partiami; odłożę je do czasu zebrania kompletu. Pokonaj też kobolda olbrzymiego; sprawdzę to przed rozpoczęciem pracy.");
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

							raiser.say("Żelazo odłożę do pracy. Do kompletu brakuje: "
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

							raiser.say("Ogień potrzebuje opału. Do kompletu brakuje: "
								+ Grammar.quantityplnoun(neededWoodLogs, "polano","")
								+ ".");
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
							raiser.say("Złoto połączę ze stalą. Do kompletu brakuje: "
									+ Grammar.quantityplnoun(neededGoldBars, "sztabka złota","one") + ".");
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
							raiser.say("Kiełbasa wiejska to mój ulubiony przysmak. Brakuje mi jeszcze: "
								+ Grammar.quantityplnoun(neededKielbasa, "kiełbasa wiejska","one") + ".");
							missingSomething = true;
						}
					}

					if (player.hasKilled("kobold olbrzymi") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Biorę się do wykuwania twojego sztyletu. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowy.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("kobold olbrzymi") && !missingSomething) {
							raiser.say("Materiały są już zebrane i nie musisz przynosić ich ponownie. Pozostało ci pokonać kobolda olbrzymiego. Wróć po zwycięstwie, wtedy przejdziemy do następnego etapu.");
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
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "materiały ognia",
			new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
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

							raiser.say("Żelazo odłożę do pracy. Do kompletu brakuje: "
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

							raiser.say("Ogień potrzebuje opału. Do kompletu brakuje: "
								+ Grammar.quantityplnoun(neededWoodLogs, "polano","")
								+ ".");
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
							raiser.say("Złoto połączę ze stalą. Do kompletu brakuje: "
									+ Grammar.quantityplnoun(neededGoldBars, "sztabka złota","one") + ".");
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
							raiser.say("Kiełbasa wiejska to mój ulubiony przysmak. Brakuje mi jeszcze: "
								+ Grammar.quantityplnoun(neededKielbasa, "kiełbasa wiejska","one") + ".");
							missingSomething = true;
						}
					}

					if (player.hasKilled("kobold olbrzymi") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Biorę się do wykuwania twojego sztyletu. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowy.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("kobold olbrzymi") && !missingSomething) {
							raiser.say("Materiały są już zebrane i nie musisz przynosić ich ponownie. Pozostało ci pokonać kobolda olbrzymiego. Wróć po zwycięstwie, wtedy przejdziemy do następnego etapu.");
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
			ConversationStates.ATTENDING, null, new ChatAction() {
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

					if (!games.stendhal.server.maps.quests.logic.QuestGoldFees.requirePaid(player, QUEST_SLOT, raiser)) { return; }
                    raiser.say("Ostrze ostygło, ale zachowało moc ognia. Weź sztylet żywiołów; zdobyte materiały i pokonany kobold były potrzebne, by ukończyć tę pracę. Niech dobrze służy ci w drodze.");
					player.addXP(50000);
					player.addKarma(10);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("sztylet żywiołów");
					magicSword.setBoundTo(player.getName());
					player.equipOrPutOnGround(magicSword);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "materiały ognia",
			new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;")),
			ConversationStates.ATTENDING, null, new ChatAction() {
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

					if (!games.stendhal.server.maps.quests.logic.QuestGoldFees.requirePaid(player, QUEST_SLOT, raiser)) { return; }
                    raiser.say("Ostrze ostygło, ale zachowało moc ognia. Weź sztylet żywiołów; zdobyte materiały i pokonany kobold były potrzebne, by ukończyć tę pracę. Niech dobrze służy ci w drodze.");
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
			new games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition(QUEST_SLOT, "start;"),
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
				"Żywioł Ognia w Fado potrafi wykuć sztylet żywiołów. Dowiedz się, jakich materiałów potrzebuje, i przygotuj je do wykonania ostrza.",
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
        if (!player.hasQuest(QUEST_SLOT)) return res;
        res.add("Spotkałem Żywioł Ognia w Fado.");
        games.stendhal.common.QuestProgress progress = games.stendhal.server.core.rp.quest.JourneyQuestProgress.read(this,player);
        if (isCompleted(player)) { res.add("Otrzymałem sztylet żywiołów, 50000 XP postaci i 10 karmy."); return res; }
        res.add(progress.getNextStep());
        if (progress.getState()!=games.stendhal.common.QuestProgress.State.DECLINED) {
            res.add("Materiały w kolejności dostaw: " + games.stendhal.server.core.rp.quest.JourneyQuestProgress.requirementsText(this) + ".");
            for (games.stendhal.common.QuestProgress.Objective goal : progress.getObjectives())
                if (goal.getCurrent()<goal.getRequired()) res.add(goal.getLabel()+" - pozostało: "+(goal.getRequired()-goal.getCurrent())+".");
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

    public static java.util.Map<String,Integer> getRequiredItems() {
        java.util.Map<String,Integer> items=new java.util.LinkedHashMap<String,Integer>();
        items.put("żelazo", REQUIRED_IRON);
        items.put("polano", REQUIRED_WOOD);
        items.put("sztabka złota", REQUIRED_GOLD_BAR);
        items.put("kiełbasa wiejska", REQUIRED_KIELBASA);
        return java.util.Collections.unmodifiableMap(items);
    }

}
