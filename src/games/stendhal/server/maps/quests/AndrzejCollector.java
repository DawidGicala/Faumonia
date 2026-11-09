/* $Id: AndrzejCollector.java,v 1.58 2012/04/24 17:01:18 kymara/ edited by dav Exp $ */
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
public class AndrzejCollector extends AbstractQuest implements games.stendhal.server.core.rp.quest.NativeCollectionDetails {
	private static final int REQURED_KIEL = 1;

	private static final int REQUIRED_PIORO_SERAFINA = 8;

	private static final int REQUIRED_ROG_DEMONA = 18;

	private static final int REQUIRED_ZAB_POTWORA = 36;

	private static final int REQUIRED_MINUTES = 60;

	private static final String QUEST_SLOT = "kolekcjoner_andrzej_quest";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Kolekcjoner Andrzej");

		npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            null,
            ConversationStates.QUEST_OFFERED,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Kiedyś liczyłem zysk z każdej sprzedanej rzeczy. Dziś wolę zbierać trofea, za którymi stoi prawdziwa wyprawa: od kła złotej kostuchy po rzadkie pióra i zęby. Pomożesz mi uzupełnić kolekcję?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Kolekcja jest już na swoim miejscu, a twoja pomoc nie poszła w zapomnienie. Dbaj o siły, które dzięki niej zyskałeś - przydadzą się w następnej wyprawie.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Zachowałem wszystkie trofea, które już mi przekazano. Dokończmy tę kolekcję, zanim zabierzemy się za następną sprawę.");
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
					raiser.say("Dziękuję. Trofea ustawiam w takiej kolejności: " + games.stendhal.server.core.rp.quest.CollectionQuestProgress.itemText(AndrzejCollector.this.getCollectionItems()) + ". To, co już przyniesiesz, zostanie w kolekcji. Nie musisz zdobywać wszystkiego na jedną wyprawę.");
					player.setQuest(QUEST_SLOT, "start;0;0;0;0");
					player.addKarma(10);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            null,
            ConversationStates.ATTENDING,
            "Rozumiem. Jeśli kiedyś trafisz na te trofea i zechcesz mi je oddać, wróć do mnie.",
            new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		npc.addReply(Arrays.asList("exact", "dokładnie"),
			"Musisz zachować pewną kolejność, przynosząc mi te przedmioty..");
		npc.addReply(Arrays.asList("specjalny", "specjalnego"),
			"Potrzebuję kła złotej kostuchy. Podobno można go zdobyć w bardzo odległych krainach. Niestety nie wiem gdzie dokładnie.");
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Kolekcjoner Andrzej");

		npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
            ConversationStates.ATTENDING,
            null,
            new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					int neededKiel = REQURED_KIEL
							- Integer.parseInt(tokens[1]);
					int neededRogDemona = REQUIRED_ROG_DEMONA
							- Integer.parseInt(tokens[2]);
					int neededPioroSerafina = REQUIRED_PIORO_SERAFINA
							- Integer.parseInt(tokens[3]);
					int neededZabPotwora = REQUIRED_ZAB_POTWORA
							- Integer.parseInt(tokens[4]);
					boolean missingSomething = false;

					if (!missingSomething && (neededKiel > 0)) {
						if (player.isEquipped("kieł złotej kostuchy", neededKiel)) {
							player.drop("kieł złotej kostuchy", neededKiel);
							neededKiel = 0;
						} else {
							final int amount = player.getNumberOfEquipped("kieł złotej kostuchy");
							if (amount > 0) {
								player.drop("kieł złotej kostuchy", amount);
								neededKiel -= amount;
							}

							raiser.say("Przyniesione trofea zachowam. Brakuje mi jeszcze: "
								+ Grammar.quantityplnoun(
										neededKiel, "kieł złotej kostuchy", "")
								+ ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededRogDemona > 0)) {
						if (player.isEquipped("róg demona", neededRogDemona)) {
							player.drop("róg demona", neededRogDemona);
							neededRogDemona = 0;
						} else {
							final int amount = player.getNumberOfEquipped("róg demona");
							if (amount > 0) {
								player.drop("róg demona", amount);
								neededRogDemona -= amount;
							}

							raiser.say("Przyniesione trofea zachowam. Brakuje mi jeszcze: "
								+ Grammar.quantityplnoun(neededRogDemona, "róg demona","")
								+ ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededPioroSerafina > 0)) {
						if (player.isEquipped("pióro serafina", neededPioroSerafina)) {
							player.drop("pióro serafina", neededPioroSerafina);
							neededPioroSerafina = 0;
						} else {
							final int amount = player.getNumberOfEquipped("pióro serafina");
							if (amount > 0) {
								player.drop("pióro serafina", amount);
								neededPioroSerafina -= amount;
							}
							raiser.say("Przyniesione trofea zachowam. Brakuje mi jeszcze: "
									+ Grammar.quantityplnoun(neededPioroSerafina, "pióro serafina","one") + ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededZabPotwora > 0)) {
						if (player.isEquipped("ząb potwora", neededZabPotwora)) {
							player.drop("ząb potwora", neededZabPotwora);
							neededZabPotwora = 0;
						} else {
							final int amount = player.getNumberOfEquipped("ząb potwora");
							if (amount > 0) {
								player.drop("ząb potwora", amount);
								neededZabPotwora -= amount;
							}
							raiser.say("Przyniesione trofea zachowam. Brakuje mi jeszcze: "
								+ Grammar.quantityplnoun(neededZabPotwora, "ząb potwora","one") + ".");
							missingSomething = true;
						}
					}

					if (player.hasKilled("gashadokuro") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Muszę to odpowiednio poukładać. Daj mi "
							+ REQUIRED_MINUTES
							+ " minut" + ", a będzie ułożone.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("gashadokuro") && !missingSomething) {
							raiser.say("Zanim ukończysz zadanie idź i zabij wielkiego szkieleta gashadokuro. Znajdziesz go w tunelach chaosu.");
						}

						player.setQuest(QUEST_SLOT,
							"start;"
							+ (REQURED_KIEL - neededKiel)
							+ ";"
							+ (REQUIRED_ROG_DEMONA - neededRogDemona)
							+ ";"
							+ (REQUIRED_PIORO_SERAFINA - neededPioroSerafina)
							+ ";"
							+ (REQUIRED_ZAB_POTWORA - neededZabPotwora));
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

					int neededKiel = REQURED_KIEL
							- Integer.parseInt(tokens[1]);
					int neededRogDemona = REQUIRED_ROG_DEMONA
							- Integer.parseInt(tokens[2]);
					int neededPioroSerafina = REQUIRED_PIORO_SERAFINA
							- Integer.parseInt(tokens[3]);
					int neededZabPotwora = REQUIRED_ZAB_POTWORA
							- Integer.parseInt(tokens[4]);
					boolean missingSomething = false;

					if (!missingSomething && (neededKiel > 0)) {
						if (player.isEquipped("kieł złotej kostuchy", neededKiel)) {
							player.drop("kieł złotej kostuchy", neededKiel);
							neededKiel = 0;
						} else {
							final int amount = player.getNumberOfEquipped("kieł złotej kostuchy");
							if (amount > 0) {
								player.drop("kieł złotej kostuchy", amount);
								neededKiel -= amount;
							}

							raiser.say("Przyniesione trofea zachowam. Brakuje mi jeszcze: "
								+ Grammar.quantityplnoun(
										neededKiel, "kieł złotej kostuchy", "")
								+ ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededRogDemona > 0)) {
						if (player.isEquipped("róg demona", neededRogDemona)) {
							player.drop("róg demona", neededRogDemona);
							neededRogDemona = 0;
						} else {
							final int amount = player.getNumberOfEquipped("róg demona");
							if (amount > 0) {
								player.drop("róg demona", amount);
								neededRogDemona -= amount;
							}

							raiser.say("Przyniesione trofea zachowam. Brakuje mi jeszcze: "
								+ Grammar.quantityplnoun(neededRogDemona, "róg demona","")
								+ ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededPioroSerafina > 0)) {
						if (player.isEquipped("pióro serafina", neededPioroSerafina)) {
							player.drop("pióro serafina", neededPioroSerafina);
							neededPioroSerafina = 0;
						} else {
							final int amount = player.getNumberOfEquipped("pióro serafina");
							if (amount > 0) {
								player.drop("pióro serafina", amount);
								neededPioroSerafina -= amount;
							}
							raiser.say("Przyniesione trofea zachowam. Brakuje mi jeszcze: "
									+ Grammar.quantityplnoun(neededPioroSerafina, "pióro serafina","one") + ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededZabPotwora > 0)) {
						if (player.isEquipped("ząb potwora", neededZabPotwora)) {
							player.drop("ząb potwora", neededZabPotwora);
							neededZabPotwora = 0;
						} else {
							final int amount = player.getNumberOfEquipped("ząb potwora");
							if (amount > 0) {
								player.drop("ząb potwora", amount);
								neededZabPotwora -= amount;
							}
							raiser.say("Przyniesione trofea zachowam. Brakuje mi jeszcze: "
								+ Grammar.quantityplnoun(neededZabPotwora, "ząb potwora","one") + ".");
							missingSomething = true;
						}
					}

					if (player.hasKilled("gashadokuro") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Muszę to odpowiednio poukładać. Daj mi "
							+ REQUIRED_MINUTES
							+ " minut" + ", a będzie ułożone.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("gashadokuro") && !missingSomething) {
							raiser.say("Zanim ukończysz zadanie idź i zabij wielkiego szkieleta gashadokuro. Znajdziesz go w tunelach chaosu.");
						}

						player.setQuest(QUEST_SLOT,
							"start;"
							+ (REQURED_KIEL - neededKiel)
							+ ";"
							+ (REQUIRED_ROG_DEMONA - neededRogDemona)
							+ ";"
							+ (REQUIRED_PIORO_SERAFINA - neededPioroSerafina)
							+ ";"
							+ (REQUIRED_ZAB_POTWORA - neededZabPotwora));
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
						raiser.say("Wciąż układam swoją nową kolekcje. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					if (!games.stendhal.server.maps.quests.logic.QuestGoldFees.requirePaid(player, QUEST_SLOT, raiser)) { return; }
                    raiser.say("Trofea leżą już obok siebie i wreszcie widać, ile różnych wypraw za nimi stoi. W podziękowaniu zwiększam twoje maksymalne zdrowie o 100 oraz umiejętności walki. Korzystaj z tych sił rozsądnie, bo żaden zbiór nie jest wart życia zbieracza.");
					player.addXP(1000000);
					player.addatk_xp(50000);
					player.adddef_xp(50000);
					player.setBaseHP(100 + player.getBaseHP());
                    games.stendhal.server.entity.npc.dialogue.NpcDialogueService.reward((SpeakerNPC)raiser.getEntity(),player,new games.stendhal.common.NpcDialogueSnapshot.Reward("Maksymalne zdrowie","gui/dialogue/health",100,false));
					player.heal(100, true);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("money");
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
						raiser.say("Wciąż układam swoją nową kolekcje. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					if (!games.stendhal.server.maps.quests.logic.QuestGoldFees.requirePaid(player, QUEST_SLOT, raiser)) { return; }
                    raiser.say("Trofea leżą już obok siebie i wreszcie widać, ile różnych wypraw za nimi stoi. W podziękowaniu zwiększam twoje maksymalne zdrowie o 100 oraz umiejętności walki. Korzystaj z tych sił rozsądnie, bo żaden zbiór nie jest wart życia zbieracza.");
					player.addXP(1000000);
					player.addatk_xp(50000);
					player.adddef_xp(50000);
					player.setBaseHP(100 + player.getBaseHP());
                    games.stendhal.server.entity.npc.dialogue.NpcDialogueService.reward((SpeakerNPC)raiser.getEntity(),player,new games.stendhal.common.NpcDialogueSnapshot.Reward("Maksymalne zdrowie","gui/dialogue/health",100,false));
					player.heal(100, true);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("money");
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

					final int neededKiel = REQURED_KIEL
							- Integer.parseInt(tokens[1]);
					final int neededRogDemona = REQUIRED_ROG_DEMONA
							- Integer.parseInt(tokens[2]);
					final int neededPioroSerafina = REQUIRED_PIORO_SERAFINA
							- Integer.parseInt(tokens[3]);
					final int neededZabPotwora = REQUIRED_ZAB_POTWORA
							- Integer.parseInt(tokens[4]);

					raiser.say("Będę potrzebował " + neededKiel + " #kieł #złotej #kostuchy, "
							+ neededRogDemona + " #róg #demona, "
							+ neededPioroSerafina + " #pióro #serafina i "
							+ neededZabPotwora + " #ząb #potwora");
				}
			});

		npc.add(ConversationStates.ANY,
            "kieł złotej kostuchy",
            null,
            ConversationStates.ATTENDING,
            "Rozmawiałem kiedyś z podróżnikiem który mówił, że w dalekiej piaszczystej krainie widział kły złotej kostuchy leżące na targu.",
            null);

		npc.add(ConversationStates.ANY,
            "róg demona",
            null,
            ConversationStates.ATTENDING,
            "Musisz wybrać się daleko w podziemia i odszukać ogromnego Balroga. Pokonaj go, a na pewno zdobędziesz róg demona.",
            null);
		npc.add(ConversationStates.ANY,
            "pióro serafina",
            null,
            ConversationStates.ATTENDING,
            "Musisz wybrać się wysoko w chmury i pokonać znajdującego się tam Serafina. Podobno można dostać się tam przy pomocy biletu do niebios.",
            null);
		npc.add(ConversationStates.ANY,
            Arrays.asList("ząb","ząb potwora", "potwora"),
            null,
            ConversationStates.ATTENDING,
            "Nie wiesz skąd zdobyć zęby potwora? Po prostu wyrwij je ogromnym bestiom! Najlepsze zęby do kolekcji mają Jožiny, Cosie oraz Gigantyczne Monstra.",
            null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Rzeczy dla Kolekcjonera Andrzeja",
				"Kolekcjoner Andrzej z hotelu Fado poszukuje rzadkich trofeów. Poznaj jego listę i przynieś brakujące okazy, aby uzupełnić kolekcję.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "AndrzejCollector";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }

	@Override
	public int getMinLevel() {
		return 300;
	}

	@Override
	public String getNPCName() {
		return "Kolekcjoner Andrzej";
	}
	
	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}



    public String getCollectionKind(){return "sequence";}
    public java.util.Map<String,Integer> getCollectionItems(){java.util.Map<String,Integer> m=new java.util.LinkedHashMap<String,Integer>(); m.put("kieł złotej kostuchy",REQURED_KIEL);m.put("róg demona",REQUIRED_ROG_DEMONA);m.put("pióro serafina",REQUIRED_PIORO_SERAFINA);m.put("ząb potwora",REQUIRED_ZAB_POTWORA);return m;}
    public String getCollectionHint(){return "Kolekcjoner Andrzej w hotelu Fado przyjmuje trofea kolejno: kieł złotej kostuchy, rogi demonów, pióra serafinów i zęby potwora. Wpłaty są zapamiętywane. Wymaga także pokonania gashadokuro; po spełnieniu warunków przygotowanie nagrody trwa godzinę.";}
    public String getCollectionKill(){return "gashadokuro";}
public boolean getCollectionSolo(){return false;}
public int getCollectionWait(){return REQUIRED_MINUTES;}

}
