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
public class AndrzejCollector extends AbstractQuest {
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
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Kiedyś handlowałem przedmiotami i dorobiłem się fortuny. Teraz jestem kolekcjonerem i chce zebrać wszystko co się da w tej krainie ! Potrzebuje kogoś kto przyniesie mi troche gratów i jeden specjalny przedmiot. Podejmiesz się tego wyzwania?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Już mi pomogłeś. Dziękuje");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Dlaczego zawracasz mi głowę skoro nie masz potrzebnych przedmiotów?");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Potrzebuje paru podstawowych przedmiotów i jeden #specjalny: "
						+ REQURED_KIEL
						+ " kieł złotej kostuchy, "
						+ REQUIRED_ROG_DEMONA
						+ " rogów demona, "
						+ REQUIRED_PIORO_SERAFINA
						+ " piór serafina i "
						+ REQUIRED_ZAB_POTWORA
						+ " zębów potwora. Wróć, gdy będziesz je miał #dokładnie w tej kolejności! Jeżeli zapomnisz to powiedz #przypomnij");
					player.setQuest(QUEST_SLOT, "start;0;0;0;0");
					player.addKarma(10);

				}
			});

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Och, naprawdę mi nie pomożesz?",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		npc.addReply(Arrays.asList("exact", "dokładnie"),
			"Musisz zachować pewną kolejność, przynosząc mi te przedmioty..");
		npc.addReply(Arrays.asList("specjalny", "specjalnego"),
			"Potrzebuje kieł złotej kostuchy. Podobno można go zdobyć w bardzo odległych krainach. Niestety nie wiem gdzie dokładnie.");
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Kolekcjoner Andrzej");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING, null,
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

							raiser.say("Porozmawiamy jak przyniesiesz mi: "
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

							raiser.say("Porozmawiamy jak przyniesiesz mi:"
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
							raiser.say("Porozmawiamy jak przyniesiesz mi:"
									+ Grammar.quantityplnoun(neededPioroSerafina, "pióro serafina","one") + " więcej.");
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
							raiser.say("Porozmawiamy jak przyniesiesz mi: "
								+ Grammar.quantityplnoun(neededZabPotwora, "ząb potwora","one") + " wciąż.");
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
						raiser.say("Wciąż układam swoją nową kolekcje. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					raiser.say("Skończyłem układanie. Moja kolekcja wygląda przepięknie ! Muszę ci się jakoś odwdzięczyć. Zamknij oczy a zwiększe twoje maksymalne życie... O już! Zamknij jeszcze na chwile... Zwiększyłem również twoje umiejętności walki i obrony. Powinieneś dłużej przeżyć gdy będziesz atakowany. Jeszcze raz dziękuje za pomoc !");
					player.addXP(1000000);
					player.addatk_xp(50000);
					player.adddef_xp(50000);
					player.setBaseHP(100 + player.getBaseHP());
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
			new QuestStartedCondition(QUEST_SLOT),
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

		npc.add(
			ConversationStates.ANY,
			"kieł złotej kostuchy",
			null,
			ConversationStates.ATTENDING,
			"Rozmawiałem kiedyś z podróżnikiem który mówił, że w dalekiej piaszczystej krainie widział kły złotej kostuchy leżące na targu.",
			null);

		npc.add(ConversationStates.ANY, "róg demona", null,
				ConversationStates.ATTENDING,
				"Musisz wybrać się daleko w podziemia i odszukać ogromnego Balroga. Pokonaj go, a na pewno zdobędziesz róg demona.", null);
		npc.add(ConversationStates.ANY, "pióro serafina", null,
				ConversationStates.ATTENDING,
				"Musisz wybrać się wysoko w chmury i pokonać znajdującego się tam Serafina. Podobno można dostać się tam przy pomocy biletu do niebios.",
				null);
		npc.add(
			ConversationStates.ANY,
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
				"Kolekcjoner Andrzej potrzebuje kilku rzeczy do swojej kolekcji.",
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
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem Kolekcjonera Andrzeja w hotelu Fado.");
			if (questState.equals("rejected")) {
				res.add("Nie pomoge kolekcjonerowi.");
				return res;
			} 
			res.add("Kolekcjoner Andrzej prosił mnie abym przyniósł mu: " + REQURED_KIEL
					+ " kieł złotej kostuchy, "
					+ REQUIRED_ROG_DEMONA
					+ " piór anioła, "
					+ REQUIRED_PIORO_SERAFINA
					+ " piór serafina i "
					+ REQUIRED_ZAB_POTWORA
					+ "  zębów potwora, dokładnie w tej kolejności.");
			if(questState.startsWith("start") && !"start;15;26;12;6".equals(questState)){
				res.add("Nie dostarczyłem wszystkiego. Kolekcjoner Andrzej powie mi co jeszcze potrzebuje.");
			} else if ("start;15;26;12;6".equals(questState) || !questState.startsWith("start")) {
				res.add("Dostarczyłem wszystko o co prosił mnie Kolekcjoner Andrzej.");
			}
			if("start;15;26;12;6".equals(questState) && !player.hasKilled("gashadokuro")){
				res.add("Kolekcjoner poprosił mnie abym zabił... gashadokuro... Podobno znajde go w podziemiach chaosu.");
			} 
			if (questState.startsWith("forging")) {
				res.add("Kolekcjoner Andrzej układa swoje nowe zabawki.");
			} 
			if (isCompleted(player)) {
				res.add("Za przyniesienie kła złotej kostuchy i paru innych rzeczy Kolekcjoner Andrzej zwiększył moje umiejętności obrony, ataku oraz maksymalne życie.	W nagrodę za ukończenie zadania otrzymałem: 1 000.000 pkt doświadczenia, 20.000 pkt ataku, 20.000 pkt obrony i 100 pkt maksymalnego życia więcej.");
			}
			return res;	
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
}
