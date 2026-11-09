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
public class sapmedal extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {
	private static final int REQUIRED_IRON = 1;

	private static final int REQUIRED_GOLD_BAR = 40;

	private static final int REQUIRED_WOOD = 1;

	private static final int REQUIRED_GIANT_HEART = 60;

	private static final int REQUIRED_MINUTES = 720;

	private static final String QUEST_SLOT = "sapphiremedal_quest";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Trener Łuczników");

		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            null,
            ConversationStates.QUEST_OFFERED,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
                    if (!dialogueRewardAvailable(raiser)) return;
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Łucznik potrzebuje cierpliwości przy ćwiczeniach i przy zaopatrzeniu oddziału. Wymienię twój szmaragdowy medal na obsydianowy, gdy dostarczysz materiały i wykażesz pokonanie czarnego smoczyska. Chcesz podjąć tę próbę?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Obsydianowy medal został już wydany. Zachowaj go, jeśli planujesz odwiedzić Trenera Magów i ubiegać się o następne odznaczenie.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Pamiętam twoje wcześniejsze dostawy. Sprawdźmy brakujące materiały i próbę walki, zanim przejdziemy do odznaczenia.");
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
                    if (!dialogueRewardAvailable(raiser)) return;
					raiser.say("Dziękuję. Przyjmuję materiały kolejno: " + games.stendhal.server.core.rp.quest.CollectionQuestProgress.itemText(sapmedal.this.getCollectionItems()) + ". Wcześniejsze wpłaty zachowam, a pełne warunki przypomnę na twoją prośbę.");
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
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Trener Łuczników");

		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
                    if (!dialogueRewardAvailable(raiser)) return;
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
						if (player.isEquipped("pazur czarnego smoka", neededIron)) {
							player.drop("pazur czarnego smoka", neededIron);
							neededIron = 0;
						} else {
							final int amount = player.getNumberOfEquipped("pazur czarnego smoka");
							if (amount > 0) {
								player.drop("pazur czarnego smoka", amount);
								neededIron -= amount;
							}

							raiser.say("Potrzebujemy "
								+ Grammar.quantityplnoun(
										neededIron, "pazur czarnego smoka", "")
								+ ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededWoodLogs > 0)) {
						if (player.isEquipped("szmaragdowy medal", neededWoodLogs)) {
							player.drop("szmaragdowy medal", neededWoodLogs);
							neededWoodLogs = 0;
						} else {
							final int amount = player.getNumberOfEquipped("szmaragdowy medal");
							if (amount > 0) {
								player.drop("szmaragdowy medal", amount);
								neededWoodLogs -= amount;
							}

							raiser.say("Nie mam jak cię awansować bez "
								+ Grammar.quantityplnoun(neededWoodLogs, "szmaragdowy medal","")
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
							raiser.say("Potrzebujemy materiały do zbroji. Przynieś "
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
							raiser.say("Potrzebujemy serc olbrzyma do wytwarzania mikstur. Nadal potrzebujemy  "
								+ Grammar.quantityplnoun(neededGiantHearts, "serce olbrzyma","one") + " wciąż.");
							missingSomething = true;
						}
					}

					if (player.hasKilled("czarne smoczysko") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Za niedługo zostaniesz odznaczony. Wróć za  "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowy.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("czarne smoczysko") && !missingSomething) {
							raiser.say("Zanim dostanisz medal zabij czarne smoczysko");
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
                    if (!dialogueRewardAvailable(raiser)) return;
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
						if (player.isEquipped("pazur czarnego smoka", neededIron)) {
							player.drop("pazur czarnego smoka", neededIron);
							neededIron = 0;
						} else {
							final int amount = player.getNumberOfEquipped("pazur czarnego smoka");
							if (amount > 0) {
								player.drop("pazur czarnego smoka", amount);
								neededIron -= amount;
							}

							raiser.say("Potrzebujemy "
								+ Grammar.quantityplnoun(
										neededIron, "pazur czarnego smoka", "")
								+ ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededWoodLogs > 0)) {
						if (player.isEquipped("szmaragdowy medal", neededWoodLogs)) {
							player.drop("szmaragdowy medal", neededWoodLogs);
							neededWoodLogs = 0;
						} else {
							final int amount = player.getNumberOfEquipped("szmaragdowy medal");
							if (amount > 0) {
								player.drop("szmaragdowy medal", amount);
								neededWoodLogs -= amount;
							}

							raiser.say("Nie mam jak cię awansować bez "
								+ Grammar.quantityplnoun(neededWoodLogs, "szmaragdowy medal","")
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
							raiser.say("Potrzebujemy materiały do zbroji. Przynieś "
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
							raiser.say("Potrzebujemy serc olbrzyma do wytwarzania mikstur. Nadal potrzebujemy  "
								+ Grammar.quantityplnoun(neededGiantHearts, "serce olbrzyma","one") + " wciąż.");
							missingSomething = true;
						}
					}

					if (player.hasKilled("czarne smoczysko") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Za niedługo zostaniesz odznaczony. Wróć za  "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowy.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("czarne smoczysko") && !missingSomething) {
							raiser.say("Zanim dostanisz medal zabij czarne smoczysko");
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
                    if (!dialogueRewardAvailable(raiser)) return;

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

					raiser.say("Oto obsydianowy medal za pomoc łucznikom. Zastępuje poprzednie odznaczenie i przypomina, że wytrwałość jest równie potrzebna jak celne oko. Z tym medalem możesz zwrócić się do Trenera Magów.");
					player.addXP(2500000);
					player.addatk_xp(30000);
					player.adddef_xp(40000);
					player.addKarma(200);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("obsydianowy medal");
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
                    if (!dialogueRewardAvailable(raiser)) return;

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

					raiser.say("Oto obsydianowy medal za pomoc łucznikom. Zastępuje poprzednie odznaczenie i przypomina, że wytrwałość jest równie potrzebna jak celne oko. Z tym medalem możesz zwrócić się do Trenera Magów.");
					player.addXP(2500000);
					player.addatk_xp(30000);
					player.adddef_xp(40000);
					player.addKarma(200);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("obsydianowy medal");
					magicSword.setBoundTo(player.getName());
					player.equipOrPutOnGround(magicSword);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});

		npc.add(ConversationStates.ATTENDING,
            Arrays.asList("odznaczenie", "missing", "medal", "brakuje", "lista", "przypomnij"),
            new QuestStateStartsWithCondition(QUEST_SLOT, "start;"),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
                    if (!dialogueRewardAvailable(raiser)) return;
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					final int neededIron = REQUIRED_IRON
							- Integer.parseInt(tokens[1]);
					final int neededWoodLogs = REQUIRED_WOOD
							- Integer.parseInt(tokens[2]);
					final int neededGoldBars = REQUIRED_GOLD_BAR
							- Integer.parseInt(tokens[3]);
					final int neededGiantHearts = REQUIRED_GIANT_HEART
							- Integer.parseInt(tokens[4]);

					raiser.say("Będę potrzebował " + neededIron + " pazura czarnego smoka, "
							+ neededWoodLogs + " szmaragdowego medalu "
							+ neededGoldBars + " sztabek złota i "
							+ neededGiantHearts + "  serc olbrzyma.");
				}
			});

		npc.add(ConversationStates.ANY,
            "szmaragdowy medal",
            null,
            ConversationStates.ATTENDING,
            "Dostaniesz go za pomoc Trenerowi Rycerzy.",
            null);

		npc.add(ConversationStates.ANY,
            "pazur czarnego smoka",
            null,
            ConversationStates.ATTENDING,
            "Wyrwij go z stopy czarnego smoka.",
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
				"Rzeczy dla Trenera Łuczników",
				"Trener Łuczników ma dla mnie zadanie.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "sapmedal";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }

 	// match to the min level of the immortal sword
	@Override
	public int getMinLevel() {
		return 150;
	}

	@Override
	public String getNPCName() {
		return "Trener Łuczników";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}


    public String getCollectionKind(){return "sequence";}
    public java.util.Map<String,Integer> getCollectionItems(){java.util.Map<String,Integer> m=new java.util.LinkedHashMap<String,Integer>(); m.put("pazur czarnego smoka",REQUIRED_IRON);m.put("szmaragdowy medal",REQUIRED_WOOD);m.put("sztabka złota",REQUIRED_GOLD_BAR);m.put("serce olbrzyma",REQUIRED_GIANT_HEART);return m;}
    public String getCollectionHint(){return "Trener Łuczników wymienia szmaragdowy medal na obsydianowy po dostawie pozostałych materiałów i pokonaniu czarnego smoczyska. Poprzedni medal jest oddawany, nie wystarczy go pokazać. Przygotowanie trwa dwanaście godzin.";}
    public String getCollectionKill(){return "czarne smoczysko";}
public boolean getCollectionSolo(){return false;}
public int getCollectionWait(){return REQUIRED_MINUTES;}


    // This legacy item is absent in the current world data. Never consume materials
    // or pay partial XP while the final reward cannot be created.
    private boolean dialogueRewardAvailable(EventRaiser raiser) {
        if (SingletonRepository.getEntityManager().getItem("obsydianowy medal") != null) return true;
        raiser.setCurrentState(ConversationStates.ATTENDING);
        raiser.say("Nie mogę wydać tego odznaczenia. Zachowaj pozostałe materiały.");
        return false;
    }

}
