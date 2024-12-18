/* $Id: ElementalDagger2.java,v 1.58 2020/11/01 10:17:13 szyg edited by davvids Exp $ */
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

public class ElementalDagger2 extends AbstractQuest {
	private static final int REQUIRED_IRON = 20;

	private static final int REQUIRED_ELEMENTAL = 1;

	private static final int REQUIRED_WOOD = 30;

	private static final int REQUIRED_SAPPHIRE = 1;

	private static final int REQUIRED_MINUTES = 180;

	private static final String QUEST_SLOT = "upgelementalsword_quest";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Żywioł Wody");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Specjalizuje się w ulepszaniu sztyletów żywiołów. Jesteś zainteresowany?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Przecież już dostałeś swój miecz...");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Dlaczego zawracasz mi głowę skoro nie ukończyłeś swojego zadania?");
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
						+ " żelaza, "
						+ REQUIRED_WOOD
						+ " polan, "
						+ REQUIRED_ELEMENTAL
						+ " sztyletu żywiołów i jakiegoś odpowiedniego kamienia... niech bedzie "
						+ REQUIRED_SAPPHIRE
						+ " #szafir. Wróć, gdy będziesz je miał #dokładnie w tej kolejności! Jeżeli zapomnisz to powiedz #przypomnij");
					player.setQuest(QUEST_SLOT, "start;0;0;0;0");
					player.addKarma(10);

				}
			});

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Och, naprawdę nie chcesz ulepszyć sztyletu żywiołów?",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		npc.addReply(Arrays.asList("exact", "dokładnie"),
			"Żywioły nie lubią, gdy coś jest nieuporządkowane...");
		npc.addReply(Arrays.asList("szafirów", "szafiry"),
			"szafiry są mi potrzebne do przelania energii w ten sztylet.");
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Żywioł Wody");

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
					int neededElemental = REQUIRED_ELEMENTAL
							- Integer.parseInt(tokens[3]);
					int neededEmerald = REQUIRED_SAPPHIRE
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

					if (!missingSomething && (neededElemental > 0)) {
						if (player.isEquipped("sztylet żywiołów", neededElemental)) {
							player.drop("sztylet żywiołów", neededElemental);
							neededElemental = 0;
						} else {
							final int amount = player.getNumberOfEquipped("sztylet żywiołów");
							if (amount > 0) {
								player.drop("sztylet żywiołów", amount);
								neededElemental -= amount;
							}
							raiser.say("Muszę najpierw mieć podstawowy"
									+ Grammar.quantityplnoun(neededElemental, "sztylet żywiołów","") + " .");
							missingSomething = true;
						}
					}

					if (!missingSomething && (neededEmerald > 0)) {
						if (player.isEquipped("szafir", neededEmerald)) {
							player.drop("szafir", neededEmerald);
							neededEmerald = 0;
						} else {
							final int amount = player.getNumberOfEquipped("szafir");
							if (amount > 0) {
								player.drop("szafir", amount);
								neededEmerald -= amount;
							}
							raiser.say("Potrzebuje szafiru by przelać moc w sztylet.  "
								+ Grammar.quantityplnoun(neededEmerald, "","") + "");
							missingSomething = true;
						}
					}

					if (player.hasKilled("błękitny smok") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Biorę się do wykuwania Twojego sztyletu. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowy.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("błękitny smok") && !missingSomething) {
							raiser.say("Na pewno sam posiadłeś ten szafir? Żywioły nie wykonuja broni dla tchórzy! Idz i zabij błękitnego smoka, zanim oddam ci twój sztylet!");
						}

						player.setQuest(QUEST_SLOT,
							"start;"
							+ (REQUIRED_IRON - neededIron)
							+ ";"
							+ (REQUIRED_WOOD - neededWoodLogs)
							+ ";"
							+ (REQUIRED_ELEMENTAL - neededElemental)
							+ ";"
							+ (REQUIRED_SAPPHIRE - neededEmerald));
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
						raiser.say("Jeszcze nie skończyłem ulepszania twojego sztyletu. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					raiser.say("Skończyłem wykuwanie twojej broni. Oto ona!");
					player.addXP(100000);
					player.addKarma(20);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("ulepszony sztylet żywiołów");
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
					final int neededElemental = REQUIRED_ELEMENTAL
							- Integer.parseInt(tokens[3]);
					final int neededEmerald = REQUIRED_SAPPHIRE
							- Integer.parseInt(tokens[4]);

					raiser.say("Będę potrzebował " + neededIron + " #żelazo, "
							+ neededWoodLogs + " #polano, "
							+ neededElemental + " #sztylet żywołów i "
							+ neededEmerald + " #szafir");
				}
			});

		npc.add(
			ConversationStates.ANY,
			"żelazo",
			null,
			ConversationStates.ATTENDING,
			"Zbierz kilka rud żelaza, które są bogate w minerały a nastepnie przetop je u kowala w Semos na #sztabki #żelaza.",
			null);

		npc.add(ConversationStates.ANY, "polano", null,
				ConversationStates.ATTENDING,
				"W lesie jest pełno drewna.", null);
		npc.add(ConversationStates.ANY, "sztabki żelaza", null,
				ConversationStates.ATTENDING,
				"Kowal w Semos może dla Ciebie odlać rude żelaza w sztabki żelaza.",
				null);
		npc.add(
			ConversationStates.ANY,
			Arrays.asList("smoka","smok", "szafir"),
			null,
			ConversationStates.ATTENDING,
			"Istnieją starodawne legendy o smokach żyjącym w podziemiach Faumonii. W ich pobliżu powinny znajdować sie szafiry.",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Ulepszony Sztylet Żywiołów",
				"Żywioł Wody w Ados ulepszy dla ciebie miecz żywiołów. Musisz tylko przynieść mu pare minerałów",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "ElementalDagger2";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem Żywioł Wody w Ados.");
			if (questState.equals("rejected")) {
				res.add("Odmowiłem wykonania ulepszonego sztyletu żywiołów.");
				return res;
			} 
			res.add("Aby ulepszyć mój sztylet żywiołów, Żywioł Wody potrzebuje: " + REQUIRED_IRON
					+ " żelazo, "
					+ REQUIRED_WOOD
					+ " polana, "
					+ REQUIRED_ELEMENTAL
					+ " sztyletu żywiołów i "
					+ REQUIRED_SAPPHIRE
					+ " szafir, dokładnie w tej kolejności.");
			if(questState.startsWith("start") && !"start;15;26;12;6".equals(questState)){
				res.add("Nie zrobiłem wszystkiego. Żywioł Wody powie mi co jeszcze potrzebuje.");
			} else if ("start;15;26;12;6".equals(questState) || !questState.startsWith("start")) {
				res.add("Dostarczyłem wszystko co potrzebne dla Żywiołu Wody.");
			}
			if("start;15;26;12;6".equals(questState) && !player.hasKilled("błekitny smok")){
				res.add("Aby udowodnić, że sam zdobyłem szafir muszę zabić błękitnego smoka.");
			} 
			if (questState.startsWith("forging")) {
				res.add("Żywioł Wody ulepsza mi mój miecz.");
			} 
			if (isCompleted(player)) {
				res.add("Za szafir i pare innach drobiazgów Żywioł Wody ulepszył mój sztylet żywiołów.	W nagrodę za ukończenie zadania otrzymałem: 100.000 pkt doświadczenie, 20 pkt karmy i ulepszony sztylet żywiołów.");
			}
			return res;	
	}

	@Override
	public int getMinLevel() {
		return 75;
	}

	@Override
	public String getNPCName() {
		return "Żywioł Wody";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
