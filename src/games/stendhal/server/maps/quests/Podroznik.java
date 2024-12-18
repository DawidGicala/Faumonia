/* $Id: Podroznik.java,v 1.58 2012/04/24 17:01:18 kymara/edited by szyg Exp $ */
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

public class Podroznik extends AbstractQuest {
	private static final int REQUIRED_KIEL = 10;

	private static final int REQUIRED_PAZUR = 10;

	private static final int REQUIRED_ARANDULA = 20;

	private static final int REQUIRED_BOROWIK = 10;

	private static final int REQUIRED_MINUTES = 15;

	private static final String QUEST_SLOT = "Podroznik_quest";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Tajemniczy Wędrowiec");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Potrzebuje kogoś kto mi pomoże uzupełnić zapasy. Jeżeli przyniesiesz mi potrzebne przedmioty, oddam ci mój naszyjnik. Co ty na to?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Przecież już dostałeś naszyjnik...");
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
					raiser.say("Będę potrzebował kilku rzeczy: "
						+ REQUIRED_ARANDULA
						+ " arandul aby móc wykonać nowe maści lecznicze, "
						+ REQUIRED_KIEL
						+ " kłów niedźwiedzia, "
						+ REQUIRED_PAZUR
						+ " pazurów niedźwiedzia. Przyda mi sie też coś do jedzenia, wiec przynieś mi "
						+ REQUIRED_BOROWIK
						+ " borówikow. Wróć, gdy będziesz je miał #dokładnie w tej kolejności! Jeżeli zapomnisz to powiedz #przypomnij");
					player.setQuest(QUEST_SLOT, "start;0;0;0;0");
					player.addKarma(10);

				}
			});

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Och, naprawdę nie chcesz magicznego naszyjnika?",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		npc.addReply(Arrays.asList("exact", "dokładnie"),
			"Nie lubię, gdy coś jest nieuporządkowane...");
		npc.addReply(Arrays.asList("arandula", "arandule"),
			"Znajdziesz je na północ od miasta Semos. Można z nich przyrządzić mikstury lecznicze.");
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Tajemniczy Wędrowiec");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					int neededKiel = REQUIRED_KIEL
							- Integer.parseInt(tokens[1]);
					int neededArandula = REQUIRED_ARANDULA
							- Integer.parseInt(tokens[2]);
					int neededPazury = REQUIRED_PAZUR
							- Integer.parseInt(tokens[3]);
					int neededBorowik = REQUIRED_BOROWIK
							- Integer.parseInt(tokens[4]);
					boolean missingSomething = false;

					if (!missingSomething && (neededKiel > 0)) {
						if (player.isEquipped("kieł niedźwiedzi", neededKiel)) {
							player.drop("kieł niedźwiedzi", neededKiel);
							neededKiel = 0;
						} else {
							final int amount = player.getNumberOfEquipped("kieł niedźwiedzi");
							if (amount > 0) {
								player.drop("kieł niedźwiedzi", amount);
								neededKiel -= amount;
							}

							raiser.say("Moja łowiecka kolekcja jest wybrakowana. Przynieś mi "
								+ Grammar.quantityplnoun(
										neededKiel, "kieł niedźwiedzi", "")
								+ ".");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededArandula > 0)) {
						if (player.isEquipped("arandula", neededArandula)) {
							player.drop("arandula", neededArandula);
							neededArandula = 0;
						} else {
							final int amount = player.getNumberOfEquipped("arandula");
							if (amount > 0) {
								player.drop("arandula", amount);
								neededArandula -= amount;
							}

							raiser.say("Kończa mi sie zapasy mikstur leczniczych. Potrzebuje czegoś do produkcji nowych, przynieś mi "
								+ Grammar.quantityplnoun(neededArandula, "arandula","")
								+ "");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededPazury > 0)) {
						if (player.isEquipped("niedźwiedzie pazury", neededPazury)) {
							player.drop("niedźwiedzie pazury", neededPazury);
							neededPazury = 0;
						} else {
							final int amount = player.getNumberOfEquipped("niedźwiedzie pazury");
							if (amount > 0) {
								player.drop("niedźwiedzie pazury", amount);
								neededPazury -= amount;
							}
							raiser.say("Bez niedźwiedzich pazurów moja łowiecka kolekcja jest wybrakowana. Potrzebuję "
									+ Grammar.quantityplnoun(neededPazury, "niedźwiedzie pazury","") + "");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededBorowik > 0)) {
						if (player.isEquipped("borowik", neededBorowik)) {
							player.drop("borowik", neededBorowik);
							neededBorowik = 0;
						} else {
							final int amount = player.getNumberOfEquipped("borowik");
							if (amount > 0) {
								player.drop("borowik", amount);
								neededBorowik -= amount;
							}
							raiser.say("Kończa mi sie zapasy żywności a coś musze jeść. Przynieś mi "
								+ Grammar.quantityplnoun(neededBorowik, "borowik","") + "");
							missingSomething = true;
						}
					}

					if (player.hasKilled("szczur olbrzymi") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko i zabiłes tego wstretnego szczura. Daj mi teraz chwile na uporzadkowanie zapasów. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a dostaniesz naszyjnik.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("szczur olbrzymi") && !missingSomething) {
							raiser.say("Zanim dostaniesz naszyjnik, zabij olbrzymiego szczura który kraży tu w okolicy. Strasznie mnie denerwuje!");
						}

						player.setQuest(QUEST_SLOT,
							"start;"
							+ (REQUIRED_KIEL - neededKiel)
							+ ";"
							+ (REQUIRED_ARANDULA - neededArandula)
							+ ";"
							+ (REQUIRED_PAZUR - neededPazury)
							+ ";"
							+ (REQUIRED_BOROWIK - neededBorowik));
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
						raiser.say("Jeszcze nie skończyłem układania. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					raiser.say("Skończyłem układanie. W zamian za twoja pomoc, przyjmij ten magiczny naszyjnik. Na pewno ochroni cie przed różnymi klatwami, albo i nie...");
					player.addXP(25000);
					player.addatk_xp(5000);
					player.adddef_xp(5000);
					player.addKarma(15);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("naszyjnik podróżnika");
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

					final int neededKiel = REQUIRED_KIEL
							- Integer.parseInt(tokens[1]);
					final int neededArandula = REQUIRED_ARANDULA
							- Integer.parseInt(tokens[2]);
					final int neededPazury = REQUIRED_PAZUR
							- Integer.parseInt(tokens[3]);
					final int neededBorowik = REQUIRED_BOROWIK
							- Integer.parseInt(tokens[4]);

					raiser.say("Będę potrzebował " + neededKiel + " #kieł #niedźwiedzi, "
							+ neededArandula + " #arandula, "
							+ neededPazury + " #niedźwiedzie #pazury i "
							+ neededBorowik + " #borowik");
				}
			});

		npc.add(
			ConversationStates.ANY,
			"kieł niedźwiedzi",
			null,
			ConversationStates.ATTENDING,
			"Kieł wyrwany z paszczy niedźwiedzia robi wrażenie.",
			null);

		npc.add(ConversationStates.ANY, "arandula", null,
				ConversationStates.ATTENDING,
				"Arandula bedzie idealna do produkcji nowych mikstur leczniczych. Znajdziesz ja na północ od miasta Semos.", null);
		npc.add(ConversationStates.ANY, "niedźwiedzie pazury", null,
				ConversationStates.ATTENDING,
				"Pazury wyrwane z łapy niedźwiedzia robia wrażenie.",
				null);
		npc.add(
			ConversationStates.ANY,
			Arrays.asList("szczur olbrzymi","olbrzymiego szczura", "szczur"),
			null,
			ConversationStates.ATTENDING,
			"Wielkie szczury olbrzymie lubia chodzić po głebokich jaskiniach.",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Tajemniczy Podróżnik",
				"Podróżnik w jaskini pod Ados podaruje ci magiczny naszyjnik, jeżeli mu pomożesz..",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "NaszyjnikPodroznika";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem Tajemniczego Podróżnika w jaskini w górach Ados.");
			if (questState.equals("rejected")) {
				res.add("Stwierdziłem, że nie potrzebny mi naszyjnik podróżnika.");
				return res;
			} 
			res.add("Podróżnik powiedział mi, że dostane naszyjnik jeżeli dostarcze mu:" + REQUIRED_KIEL
					+ " kłów niedźwiedzich, "
					+ REQUIRED_ARANDULA
					+ " aranduli, "
					+ REQUIRED_PAZUR
					+ " niedźwiedzich pazurów i "
					+ REQUIRED_BOROWIK
					+ " borowików, dokładnie w tej kolejności.");
					
			if(questState.startsWith("start") && !"start;15;26;12;6".equals(questState)){
				res.add("Nie zrobiłem wszystkiego. Podróżnik powie mi czego jeszcze potrzebuje.");
			} else if ("start;15;26;12;6".equals(questState) || !questState.startsWith("start")) {
				res.add("Dostarczyłem wszystko czego chciał Podróżnik.");
			}
			if("start;15;26;12;6".equals(questState) && !player.hasKilled("szczur olbrzymi")){
				res.add("Aby dostać naszyjnik, muszę zabić olbrzymiego szczura. Powinienem go szukać w jaskiniach.");
			} 
			if (questState.startsWith("forging")) {
				res.add("Musze poczekać aż podróżnik uporządkuję swoje rzeczy.");
			} 
			if (isCompleted(player)) {
				res.add("Za niedźwiedzie pazury, kły niedźwiedzia, arandule i borowiki dostałem naszyjnik podróżnika.	W nagrodę za ukończenie zadania otrzymałem: 25.000 pkt doświadczenia, 5.000 pkt ataku, 5.000 pkt obrony, 15 pkt karmy oraz naszyjnik podróżnika.");
			}
			return res;	
	}

	@Override
	public int getMinLevel() {
		return 20;
	}

	@Override
	public String getNPCName() {
		return "Tajemniczy Wędrowiec";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
