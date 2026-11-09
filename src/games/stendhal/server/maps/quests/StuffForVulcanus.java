/* $Id: StuffForVulcanus.java,v 1.58 2012/04/24 17:01:18 kymara Exp $ */
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
public class StuffForVulcanus extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {
	private static final int REQUIRED_IRON = 400;

	private static final int REQUIRED_GOLD_BAR = 200;

	private static final int REQUIRED_WOOD = 100;

	private static final int REQUIRED_GIANT_HEART = 80;

	private static final int REQUIRED_MINUTES = 720;

	private static final String QUEST_SLOT = "immortalsword_quest";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Vulcanus");

		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            null,
            ConversationStates.QUEST_OFFERED,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Wykułem już kiedyś miecz nieśmiertelnych i wiem, ile wysiłku pochłania taka broń. Potrzebuję metalu, opału oraz serc olbrzymów, a jej przyszły właściciel musi dowieść, że potrafi pokonać olbrzyma. Podejmiesz się przygotowań?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Miecz nieśmiertelnych już ci służy. Nie mam sił na drugie takie zamówienie; pozwól ostygnąć palenisku.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Przyjąłem twoje zamówienie i pamiętam dostawy. Sprawdźmy, czego brakuje albo czy miecz jest już gotowy, zamiast wracać do pierwszej rozmowy.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.YES_MESSAGES,
            new games.stendhal.server.entity.npc.condition.QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Dziękuję. Przyjmuję materiały kolejno: " + games.stendhal.server.core.rp.quest.CollectionQuestProgress.itemText(StuffForVulcanus.this.getCollectionItems()) + ". Wcześniejsze wpłaty zachowam, a pełne warunki przypomnę na twoją prośbę.");
					player.setQuest(QUEST_SLOT, "start;0;0;0;0");
					player.addKarma(10);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            null,
            ConversationStates.ATTENDING,
            "Rozumiem. Nie przyjmuję zamówienia bez twojej zgody. Wróć, jeśli zdecydujesz się pomóc.",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		npc.addReply(Arrays.asList("exact", "dokładnie"),
			"Ta archaiczna magia potrzebuje tych składników w dokładnie podanej kolejności.");
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Vulcanus");

		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
            ConversationStates.ATTENDING,
            null,
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
							raiser.say("Muszę zapłacić rachunek duchom za włożenie uczuć w ten miecz. Potrzebuję "
									+ Grammar.quantityplnoun(neededGoldBars, "sztabka złota","one") + " więcej.");
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
							raiser.say("To główny składnik uczuć. Potrzebuję "
								+ Grammar.quantityplnoun(neededGiantHearts, "serce olbrzyma","one") + " wciąż.");
							missingSomething = true;
						}
					}

					if (player.hasKilled("olbrzym") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Muszę zrobić nieśmiertelny miecz. Poza tym jesteś wystarczająco silny, aby władać nim. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowy.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("olbrzym") && !missingSomething) {
							raiser.say("Naprawdę własnoręcznie zdobyłeś te serce olbrzyma? Nie sądzę! Ten potężny miecz może być dany tylko tym, którzy są wystarczająco silni, aby zabić #olbrzyma.");
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
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "dostawa",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
            ConversationStates.ATTENDING,
            null,
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
							raiser.say("Muszę zapłacić rachunek duchom za włożenie uczuć w ten miecz. Potrzebuję "
									+ Grammar.quantityplnoun(neededGoldBars, "sztabka złota","one") + " więcej.");
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
							raiser.say("To główny składnik uczuć. Potrzebuję "
								+ Grammar.quantityplnoun(neededGiantHearts, "serce olbrzyma","one") + " wciąż.");
							missingSomething = true;
						}
					}

					if (player.hasKilled("olbrzym") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Muszę zrobić nieśmiertelny miecz. Poza tym jesteś wystarczająco silny, aby władać nim. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowy.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("olbrzym") && !missingSomething) {
							raiser.say("Naprawdę własnoręcznie zdobyłeś te serce olbrzyma? Nie sądzę! Ten potężny miecz może być dany tylko tym, którzy są wystarczająco silni, aby zabić #olbrzyma.");
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

		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;")),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					
					final long delay = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE; 
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();

					if (timeRemaining > 0L) {
						raiser.say("Jeszcze nie skończyłem wykuwania miecza. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					if (!games.stendhal.server.maps.quests.logic.QuestGoldFees.requirePaid(player, QUEST_SLOT, raiser)) { return; }
                    raiser.say("Miecz nieśmiertelnych jest gotowy i należy do ciebie. Metal, ogień i serca olbrzymów dały broń, której nie wolno traktować lekkomyślnie. Moja praca się skończyła, teraz ty musisz dobrze władać ostrzem.");
					player.addXP(1000000);
					player.addatk_xp(100000);
					player.adddef_xp(125000);
					player.addKarma(100);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("miecz nieśmiertelnych");
					magicSword.setBoundTo(player.getName());
					player.equipOrPutOnGround(magicSword);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});
        // Resume the native stage without changing its guards or actions.
        npc.add(ConversationStates.ATTENDING, "dostawa",
            new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;")),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					
					final long delay = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE; 
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();

					if (timeRemaining > 0L) {
						raiser.say("Jeszcze nie skończyłem wykuwania miecza. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					if (!games.stendhal.server.maps.quests.logic.QuestGoldFees.requirePaid(player, QUEST_SLOT, raiser)) { return; }
                    raiser.say("Miecz nieśmiertelnych jest gotowy i należy do ciebie. Metal, ogień i serca olbrzymów dały broń, której nie wolno traktować lekkomyślnie. Moja praca się skończyła, teraz ty musisz dobrze władać ostrzem.");
					player.addXP(1000000);
					player.addatk_xp(100000);
					player.adddef_xp(125000);
					player.addKarma(100);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("miecz nieśmiertelnych");
					magicSword.setBoundTo(player.getName());
					player.equipOrPutOnGround(magicSword);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});

		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("forge", "missing", "wykuj", "brakuje", "lista", "przypomnij"),
            new QuestStateStartsWithCondition(QUEST_SLOT, "start;"),
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

					raiser.say("Będę potrzebował " + neededIron + " #żelazo, "
							+ neededWoodLogs + " #polano, "
							+ neededGoldBars + " #sztabka złota i "
							+ neededGiantHearts + " #serce olbrzyma.");
				}
			});

		npc.add(ConversationStates.ANY,
            "żelazo",
            null,
            ConversationStates.ATTENDING,
            "Do kuźni potrzebuję gotowego żelaza, a nie samej rudy. Najpierw zbierz rudę i zleć jej przetopienie kowalowi; potem przynieś sztabki zgodnie z kolejnością dostawy.",
            null);

		npc.add(ConversationStates.ANY,
            "polano",
            null,
            ConversationStates.ATTENDING,
            "Szukaj polan w lesie i zbieraj drewno podczas drogi do kuźni. Jest opałem do tego zamówienia, więc odłożę je dopiero po przyjęciu wymaganej ilości żelaza.",
            null);
		npc.add(ConversationStates.ANY,
            "złoto",
            null,
            ConversationStates.ATTENDING,
            "Kowal w Ados może dla Ciebie odlać bryłki złota w sztabki złota.",
            null);
		npc.add(ConversationStates.ANY,
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
				"Rzeczy dla Vulcanusa",
				"Vulcanus z Kotoch potrafi wykuć miecz nieśmiertelnych. Porozmawiaj z kowalem o wymaganych materiałach i próbie, której oczekuje od przyszłego właściciela.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "StuffForVulcanus";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }

 	// match to the min level of the immortal sword
	@Override
	public int getMinLevel() {
		return 250;
	}

	@Override
	public String getNPCName() {
		return "Vulcanus";
	}
	
	@Override
	public String getRegion() {
		return Region.KOTOCH;
	}



    public String getCollectionKind(){return "sequence";}
    public java.util.Map<String,Integer> getCollectionItems(){java.util.Map<String,Integer> m=new java.util.LinkedHashMap<String,Integer>(); m.put("żelazo",REQUIRED_IRON);m.put("polano",REQUIRED_WOOD);m.put("sztabka złota",REQUIRED_GOLD_BAR);m.put("serce olbrzyma",REQUIRED_GIANT_HEART);return m;}
    public String getCollectionHint(){return "Vulcanus potrzebuje kolejno żelaza, drewna, sztabek złota i serc olbrzymów. Wcześniejsze wpłaty zachowują ważność. Po dostawie i próbie walki wykucie miecza nieśmiertelnych potrwa dwanaście godzin.";}
    public String getCollectionKill(){return "olbrzym";}
public boolean getCollectionSolo(){return false;}
public int getCollectionWait(){return REQUIRED_MINUTES;}

}
