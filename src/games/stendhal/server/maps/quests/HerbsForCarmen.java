/* $Id: HerbsForCarmen.java,v 1.21 2011/11/13 17:13:16 kymara Exp $ */
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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.DialogueHintContextAction;
import games.stendhal.server.entity.npc.action.DialogueNarrationAction;
import games.stendhal.server.entity.npc.action.DialogueRewardAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Herbs For Carmen
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Carmen (the healer in Semos)</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Carmen introduces herself and asks for some items to help her heal people.</li>
 * <li>You collect the items.</li>
 * <li>Carmen sees yours items, asks for them then thanks you.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>2 antidote</li>
 * <li>Karma: 10</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class HerbsForCarmen extends AbstractQuest {

	public static final String QUEST_SLOT = "herbs_for_carmen";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "arandula=5;borowik=1;jabłko=3;polano=2;pieczarka=1";

	/** A fresh copy of the requirements used by both the quest and its progress view. */
	public static Map<String, Integer> getRequiredItems() {
		ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		return items;
	}

	/** Local display format; the stored item names and remaining counts stay unchanged. */
	private static String describeIngredients(final String state, final boolean highlight) {
		final ItemCollection remaining = new ItemCollection();
		remaining.addFromQuestStateString(state);
		final StringBuilder text = new StringBuilder();
		for (final Map.Entry<String, Integer> item : remaining.entrySet()) {
			if (text.length() > 0) {
				text.append(", ");
			}
			if (highlight) {
				text.append('#');
			}
			text.append(item.getKey()).append(" - ").append(item.getValue());
		}
		return text.toString();
	}

	/** Carmen uses Polish item names without changing the shared English grammar rules. */
	private static ChatAction sayIngredients(final String message) {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (player.hasQuest(QUEST_SLOT)) {
					raiser.say(message.replace("[items]", describeIngredients(player.getQuest(QUEST_SLOT), true)));
				}
			}
		};
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Carmen poprosiła mnie o składniki potrzebne do leczenia rannych w Semos.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Odmówiłem Carmen pomocy. Jeśli zmienię zdanie, mogę do niej wrócić.");
		} else if (!"done".equals(questState)) {
			res.add("Do uzupełnienia zapasów Carmen brakuje mi: " + describeIngredients(questState, false) + ".");
		} else {
			res.add("Uzupełniłem zapasy Carmen, by mogła dalej leczyć rannych. W nagrodę za ukończenie zadania otrzymałem: 500 pkt doświadczenia, 500 pkt ataku, 500 pkt obrony, 10 pkt karmy i 50 małych eliksirów.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Carmen");
		npc.add(ConversationStates.ATTENDING, "wskazówki", null, ConversationStates.ATTENDING,
				"Którego składnika szukasz? Mogę podpowiedzieć, gdzie go znaleźć.",
				new DialogueHintContextAction("carmen_general"));

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new LevelGreaterThanCondition(2),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
			ConversationStates.QUESTION_1, 
			"Przydałaby mi się pomoc przy uzupełnieniu zapasów. Rannych przybywa, a przygotowanie lekarstw wymaga składników, po które nie zawsze mogę sama wyruszyć. Znasz już moją pracę?", null);

		npc.add(ConversationStates.ATTENDING, 
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT,"rejected"),
			ConversationStates.QUEST_OFFERED, 
			"Nadal brakuje mi składników do leczenia rannych. Pomożesz mi uzupełnić zapasy? W podzięce podzielę się z tobą eliksirami.", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"W takim razie wiesz, jak wielu rannych do mnie trafia. Kiedy ktoś potrzebuje pomocy, nie chcę zostawiać go samego, by ruszyć po składniki. Niestety, zapasy ziół #leczniczych szybko się kończą.",
			new DialogueHintContextAction("carmen_intro"));

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Jestem Carmen i pomagam rannym. Jeśli pozwolą na to moje umiejętności, uleczę cię bez opłat. Potrzebuję jednak uzupełnić zapasy ziół #leczniczych.",
			new DialogueHintContextAction("carmen_intro"));

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("ingredients", "leczniczych", "lecznicze"),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Chcę mieć pod ręką wszystko, czego potrzebuję do przygotowania lekarstw, zanim zjawi się kolejny ranny. Pomożesz mi zebrać kilka składników? W podzięce podzielę się z tobą eliksirami, które zabierzesz w drogę.",
			new DialogueNarrationAction("Carmen odkłada pęk ziół i zwraca się do ciebie."));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5.0),
							sayIngredients("Dziękuję. Potrzebuję: [items]. Nie musisz przynosić wszystkiego naraz. Gdy wrócisz, zapytaj mnie o #zapasy.")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Szkoda, przydałaby mi się pomoc. Jeśli zmienisz zdanie, wróć do mnie.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.ATTENDING,
			"jabłko",
			null,
			ConversationStates.ATTENDING,
			"Jabłek szukaj na drzewach na wschód od Semos. Bywają też na talerzu w tutejszej tawernie. Potrzebuję trzech sztuk, ale możesz przynosić je osobno.",
			new DialogueHintContextAction("carmen_apples"));

		npc.add(
			ConversationStates.ATTENDING,
			"polano",
			null,
			ConversationStates.ATTENDING,
			"Przy przygotowaniu lekarstw potrzebuję także opału. Rozejrzyj się za zwykłymi polanami z leśnych drzew i przynieś dwie sztuki. Możesz oddawać je osobno, razem z pozostałymi składnikami.",
			new DialogueHintContextAction("carmen_wood"));

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("pieczarka", "borowik"),
			null,
			ConversationStates.ATTENDING,
			"Na południe od Semos znajdziesz polany przy lesie, na których rosną grzyby. Potrzebuję jednego borowika i jednej pieczarki, więc zwróć uwagę, co zbierasz. Jeśli znajdziesz najpierw tylko jeden z nich, możesz od razu mi go przynieść.",
			new DialogueHintContextAction("carmen_mushrooms"));

		npc.add(
			ConversationStates.ATTENDING,
			"arandula",
			null,
			ConversationStates.ATTENDING,
			"Aranduli szukaj na północ od Semos, niedaleko wilków. Potrzebuję pięciu sztuk, ale możesz przynosić je stopniowo. Rozglądaj się uważnie i pilnuj drogi powrotnej - nie chcę, żeby przez moje zapasy przybył mi kolejny ranny.",
			new DialogueHintContextAction("carmen_arandula"));

		npc.add(ConversationStates.ATTENDING, "rycina", null, ConversationStates.ATTENDING,
			"Rozejrzyj się niedaleko wilków na północ od miasta. Na tej rycinie zobaczysz, jak wygląda arandula.",
			new MultipleActions(new DialogueNarrationAction("Carmen rozkłada rycinę zioła."),
					new ExamineChatAction("arandula.png", "Rycina Carmen", "Arandula")));
		npc.add(ConversationStates.ATTENDING, "grzyby", null, ConversationStates.ATTENDING,
			"Na południe od Semos widziałam polany pełne grzybów. Potrzebuję jednego borowika i jednej pieczarki.", null);
		npc.add(ConversationStates.ATTENDING, "sad", null, ConversationStates.ATTENDING,
			"Rozejrzyj się za jabłkami na wschód od Semos. Bywają też na talerzu w tutejszej tawernie.", null);
		npc.add(ConversationStates.ATTENDING, "drewno", null, ConversationStates.ATTENDING,
			"Chodzi o zwykłe polana z leśnych drzew. Potrzebuję dwóch sztuk, ale możesz przynosić je osobno.", null);

	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Carmen");
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()), new QuestInStateCondition(QUEST_SLOT, "done")),
				ConversationStates.ATTENDING,
				"Witaj ponownie. Dzięki tobie mam już pełne zapasy. Jeśli potrzebujesz leczenia, powiedz #ulecz. Możesz też sprawdzić moją #ofertę.", null);
	
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Dobrze cię znowu widzieć. Wciąż przygotowuję lekarstwa dla rannych, więc każdy przyniesiony składnik się przyda. Udało ci się zebrać coś na moje #zapasy?",
				null);

		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.ATTENDING, "zapasy", null,
				ConversationStates.QUESTION_2, null,
				sayIngredients("Sprawdźmy, czego brakuje w moich zapasach: [items]. Nie musisz mieć wszystkiego naraz, przyjmę to, co udało ci się zebrać. Masz coś ze sobą?"));

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, 
			null, sayIngredients("Sprawdźmy, czego brakuje w moich zapasach: [items]. Nie musisz mieć wszystkiego naraz, przyjmę to, co udało ci się zebrać. Masz coś ze sobą?"));

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "Dobrze, a co jeszcze masz?",
				null);

		ChatAction completeAction = new  MultipleActions(
				new DialogueNarrationAction("Carmen odkłada przyniesione składniki obok swoich zapasów."),
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("Dziękuję, mam już wszystkie składniki i mogę spokojnie wrócić do przygotowania lekarstw. Twoja pomoc oszczędziła mi wyprawy, podczas której ranni musieliby czekać. Weź te eliksiry na drogę - chcę, żebyś też miał czym zadbać o siebie."),
				DialogueRewardAction.xp(500),
				DialogueRewardAction.attackXP(500),
				DialogueRewardAction.defenseXP(500),
				DialogueRewardAction.karma(new IncreaseKarmaAction(10)),
				DialogueRewardAction.item("mały eliksir", 50)
				);

		/* add triggers for the item names */
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		for (final Map.Entry<String, Integer> entry : items.entrySet()) {
			String itemName = entry.getKey();

			String singular = Grammar.singular(itemName);
			List<String> sl = new ArrayList<String>();
			sl.add(itemName);

			// handle the porcino/porcini singular/plural case with item name "borowik"
			if (!singular.equals(itemName)) {
				sl.add(singular);
			}
			// also allow to understand the misspelled "porcinis"
			if (itemName.equals("borowik")) {
				sl.add("borowik");
			}

			npc.add(ConversationStates.QUESTION_2, sl, null,
					ConversationStates.QUESTION_2, null,
					new CollectRequestedItemsAction(
							itemName, QUEST_SLOT,
							"Dziękuję, odłożę to do zapasów. Każdy przyniesiony składnik przybliża mnie do przygotowania kolejnych lekarstw. Masz jeszcze coś z listy?",
							"Ten składnik mam już w potrzebnej ilości. Dziękuję.",
							completeAction, ConversationStates.ATTENDING,
							"Nie masz tego składnika przy sobie. Poszukaj go i wróć, kiedy coś zbierzesz."));
		}

		/* player says he didn't bring any items (says no) */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
			"Dobrze, wróć, kiedy coś zbierzesz. Daj znać, jeśli mogę ci jakoś #pomóc.",
				null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dobrze, resztę możesz przynieść później. Daj znać, jeśli mogę ci jeszcze #pomóc.", null);

    /* says quest and quest can't be started nor is active*/
		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				null,
			    ConversationStates.ATTENDING, 
			    "Na razie nie mam dla ciebie nowej prośby. Jeśli potrzebujesz leczenia albo lekarstw na drogę, chętnie ci pomogę.",
			    null);
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zioła dla Carmen",
				"Carmen, uzdrowicielka z Semos, potrzebuje składników do leczenia rannych. Mogę pomóc jej uzupełnić zapasy.",
				true);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "HerbsForCarmen";
	}

	public String getTitle() {
		
		return "Herbs for Carmen";
	}
	
	@Override
	public int getMinLevel() {
		return 3;
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Carmen";
	}
}
