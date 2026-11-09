/* $Id: VampireSword.java,v 1.57 2013/03/23 12:11:17 madmetzger Exp $ */
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

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * QUEST: The Vampire Sword
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li>Hogart, a retired master dwarf smith, forgotten below the dwarf mines in
 * Orril.</li>
 * <li>Markovich, a sick vampire who will fill the goblet.</li>
 * </ul>
 * <p>
 * STEPS:
 * <ul>
 * <li>Hogart tells you the story of the Vampire Lord.</li>
 * <li>He offers to forge a Vampire Sword for you if you bring him what it
 * needs.</li>
 * <li>Go to the catacombs, kill 7 vampirettes to get to the 3rd level, kill 7
 * killer bats and the vampire lord to get the required items to fill the
 * goblet.</li>
 * <li>Fill the goblet and come back.</li>
 * <li>You get some items from the Catacombs and kill the Vampire Lord.</li>
 * <li>You get the iron needed in the usual way by collecting iron ore and
 * casting in Semos.</li>
 * <li>Hogart forges the Vampire Sword for you.</li>
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li>Vampire Sword</li>
 * <li>5000 XP</li>
 * </ul>
 * <p>
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class VampireSword extends AbstractQuest {

	private static final int REQUIRED_ENTRAILS = 200;

	private static final int REQUIRED_MINUTES = 10;

	private static final String QUEST_SLOT = "vs_quest";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void prepareQuestOfferingStep() {
		
		final SpeakerNPC npc = npcs.get("Hogart");
		
		// Player asks about quests, and had previously rejected or never asked: offer it
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Potrafię wykuć krwiopijcę, lecz zwykła stal nie wystarczy do takiej broni. Musisz pokonać Lorda Wampira w katakumbach Semos, zdobyć napełnioną czarę i przynieść mi 200 trucheł wampira. Podejmiesz się wyprawy?",
			null);
		
		// Player asks about quests, but has finished this quest
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Dlaczego mnie niepokoisz? Dostałeś miecz, idź i użyj go!",
			null);
		
		// Player asks about quests, but has not finished this quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Dlaczego mnie niepokoisz skoro jeszcze nie skończyłeś zadania?",
				null);

		final List<ChatAction> gobletactions = new LinkedList<ChatAction>();
		gobletactions.add(new EquipItemAction("pusta czara"));
		gobletactions.add(new IncreaseKarmaAction(5.0));
		gobletactions.add(new SetQuestAction(QUEST_SLOT, "start"));
		// Player wants to do the quest
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, "Weź pustą czarę do Markovicha w katakumbach pod cmentarzem Semos. Napełni ją za 7 trucheł wampira, 7 trucheł nietoperza i pierścień z czaszką; przygotowanie trwa 5 minut. Mnie przynieś pełną czarę oraz dodatkowe 200 trucheł wampira, już po pokonaniu Lorda Wampira.",
			new MultipleActions(gobletactions));
		
		// Player doesn't want to do the quest; remember this, but they can ask again to start it.
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Zapomnij o tym. Musisz mieć lepszy miecz od tego, który chcę ci wykuć? Dowidzenia.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.addReply(Arrays.asList("catacombs", "katakumb", "katakumbach"), "Katakumby na północ od Semos występowały już w starodawnych #legendach.");

		npc.addReply(Arrays.asList("goblet", "czara", "czarę"), "Czarę napełnia Markovich w katakumbach Semos. Oddaj mu pustą czarę, pierścień z czaszką, 7 trucheł wampira i 7 trucheł nietoperza, a po 5 minutach odbierz gotową czarę. Potem wróć do mnie z nią i 200 truchłami wampira.");
	}

	private void prepareGobletFillingStep() {

		final SpeakerNPC npc = npcs.get("Markovich");
        npc.setDescription("Widzisz Markovicha, chorego wampira w katakumbach Semos. Mówi z wysiłkiem i co chwilę przerywa mu kaszel.");
        npc.addJob("Dawniej sam zdobywałem to, czego potrzebowałem. Teraz choroba zatrzymuje mnie przy kotle; mogę jeszcze napełnić specjalną czarę, jeśli przyniesiesz składniki. O pracę zapytaj, mówiąc napełnij.");
        games.stendhal.server.entity.npc.dialogue.JourneyDialogueTopics.install(npc);

		npc.addGoodbye("*kaszlnięcie* ... dowidzenia ... *kaszlnięcie*");
		npc.addReply(
			Arrays.asList("blood", "truchło wampira", "truchło nietoperza", "krew"),
			"Stary wampir też musi jakoś przetrwać. Przynieś pustą czarę, 7 trucheł wampira, 7 trucheł nietoperza i pierścień z czaszką, a w 5 minut przygotuję zawartość dla Hogarta. Powiedz #napełnij, gdy zechcesz zlecić pracę.");

		npc.addReply(Arrays.asList("lord", "vampire", "skull ring", "pierścień z czaszką", "lorda", "wampirem", "wampir"),
			"Lord Wampir rządzi w tych Katakumbach! Boję się go. Mógłbym Ci tylko pomóc jeżeli zabiłbyś go, przyniósł mi pierścień z czaszką oraz #czarę.");

		npc.addReply(
			Arrays.asList("empty goblet", "goblet", "pusta czara", "czara"),
			"Tylko potężny talizman jak ten kocioł lub specjalny kielich mogą zawierać krew.");

		// The sick vampire is only a producer. He doesn't care if your quest slot is active, or anything.
		// So to ensure that the vampire lord must have been killed, we made the skull ring a required item
		// Which the vampire lord drops if the quest is active as in games.stendhal.server.maps.semos.catacombs.VampireLordCreature
		// But, it could have been done other ways using quests slot checks and killed conditions
		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();	
		requiredResources.put("truchło wampira", 7);
		requiredResources.put("truchło nietoperza", 7);
		requiredResources.put("pierścień z czaszką", 1);
		requiredResources.put("pusta czara", 1);
		final ProducerBehaviour behaviour = new ProducerBehaviour(
				"sicky_fill_goblet", Arrays.asList("fill", "napełnij"), "czara", requiredResources,
				5 * 60, true);
		new ProducerAdder().addProducer(npc, behaviour,
			"Proszę nie próbuj mnie zabijać...Jestem tylko starym chorym #wampirem. Czy masz #krew, którą mógłbym wypić? Jeżeli masz #'pustą czarę' to napełnię ją krwią z mojego kotła.");
        npc.add(ConversationStates.ATTENDING, "odbiór czary", new QuestActiveCondition("sicky_fill_goblet"), ConversationStates.ATTENDING, null,
                (player,sentence,raiser) -> behaviour.giveProduct(raiser,player));


	}

	private void prepareForgingStep() {

		final SpeakerNPC npc = npcs.get("Hogart");

		final List<ChatAction> startforging = new LinkedList<ChatAction>();
		startforging.add(new DropItemAction("czara"));
		startforging.add(new DropItemAction("truchło wampira", 200));
		startforging.add(new IncreaseKarmaAction(5.0));
		startforging.add(new SetQuestAction(QUEST_SLOT, "forging;"));
		startforging.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		// Player returned with goblet and had killed the vampire lord, and has iron, so offer to forge the sword.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT,"start"),
					new PlayerHasItemWithHimCondition("czara"),
					new KilledCondition("lord wampir"),
					new PlayerHasItemWithHimCondition("truchło wampira", REQUIRED_ENTRAILS)),
			ConversationStates.ATTENDING, 
			"Przyniosłeś wszystko czego potrzebuję do wyrobienia krwiopijcy. Wróć za "
			+ REQUIRED_MINUTES
			+ " minutę" + ", a będzie gotowy", 
			new MultipleActions(startforging));
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "praca hogarta",
			new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
					new QuestInStateCondition(QUEST_SLOT,"start"),
					new PlayerHasItemWithHimCondition("czara"),
					new KilledCondition("lord wampir"),
					new PlayerHasItemWithHimCondition("truchło wampira", REQUIRED_ENTRAILS)),
			ConversationStates.ATTENDING, 
			"Przyniosłeś wszystko czego potrzebuję do wyrobienia krwiopijcy. Wróć za "
			+ REQUIRED_MINUTES
			+ " minutę" + ", a będzie gotowy", 
			new MultipleActions(startforging));

		// Player returned with goblet and had killed the vampire lord, so offer to forge the sword if iron is brought
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()), 
						new QuestInStateCondition(QUEST_SLOT,"start"),
						new PlayerHasItemWithHimCondition("czara"),
						new KilledCondition("lord wampir"),
						new NotCondition(new PlayerHasItemWithHimCondition("truchło wampira", REQUIRED_ENTRAILS))),
		ConversationStates.QUEST_ITEM_BROUGHT, 
		"Stoczyłeś ciężkie boje, aby przynieść ten kielich. Użyję jego zawartość do wykucia ( #forge ) miecza zwanego krwiopijcą",
		null);
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "praca hogarta",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(), 
						new QuestInStateCondition(QUEST_SLOT,"start"),
						new PlayerHasItemWithHimCondition("czara"),
						new KilledCondition("lord wampir"),
						new NotCondition(new PlayerHasItemWithHimCondition("truchło wampira", REQUIRED_ENTRAILS))),
		ConversationStates.QUEST_ITEM_BROUGHT, 
		"Stoczyłeś ciężkie boje, aby przynieść ten kielich. Użyję jego zawartość do wykucia ( #forge ) miecza zwanego krwiopijcą",
		null);
		
		// Player has only an empty goblet currently, remind to go to Catacombs
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT,"start"),
					new PlayerHasItemWithHimCondition("pusta czara"),
					new NotCondition(new PlayerHasItemWithHimCondition("czara"))), 
			ConversationStates.ATTENDING, 
			"Zgubiłeś drogę? Katakumby są na północ od Semos." +
			" Nie wracaj tutaj bez napełnionej czary! Dowidzenia! ",
			null);
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "praca hogarta",
			new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
					new QuestInStateCondition(QUEST_SLOT,"start"),
					new PlayerHasItemWithHimCondition("pusta czara"),
					new NotCondition(new PlayerHasItemWithHimCondition("czara"))), 
			ConversationStates.ATTENDING, 
			"Zgubiłeś drogę? Katakumby są na północ od Semos." +
			" Nie wracaj tutaj bez napełnionej czary! Dowidzenia! ",
			null);
		
		// Player has a goblet (somehow) but did not kill a vampire lord
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),  
						new QuestInStateCondition(QUEST_SLOT,"start"),
						new PlayerHasItemWithHimCondition("czara"),
						new NotCondition(new KilledCondition("lord wampir"))),
		ConversationStates.ATTENDING, 
		"Do pracy potrzebuję pełnej czary i dowodu, że udało ci się pokonać Lorda Wampira. Sam kielich nie zastąpi tego zwycięstwa. Wróć do katakumb Semos i doprowadź wyprawę do końca.",
		null);
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "praca hogarta",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),  
						new QuestInStateCondition(QUEST_SLOT,"start"),
						new PlayerHasItemWithHimCondition("czara"),
						new NotCondition(new KilledCondition("lord wampir"))),
		ConversationStates.ATTENDING, 
		"Do pracy potrzebuję pełnej czary i dowodu, że udało ci się pokonać Lorda Wampira. Sam kielich nie zastąpi tego zwycięstwa. Wróć do katakumb Semos i doprowadź wyprawę do końca.",
		null);
		
		// Player lost the empty goblet?
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT,"start"),
						new NotCondition(new PlayerHasItemWithHimCondition("pusta czara")),
						new NotCondition(new PlayerHasItemWithHimCondition("czara"))), 
			ConversationStates.QUESTION_1, 
			"Mam nadzieje, że nie zgubiłeś czary! Potrzebujesz następnej?", 
			null);
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "praca hogarta",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestInStateCondition(QUEST_SLOT,"start"),
						new NotCondition(new PlayerHasItemWithHimCondition("pusta czara")),
						new NotCondition(new PlayerHasItemWithHimCondition("czara"))), 
			ConversationStates.QUESTION_1, 
			"Mam nadzieje, że nie zgubiłeś czary! Potrzebujesz następnej?", 
			null);

		// Player lost the empty goblet, wants another
		npc.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.IDLE, "Mam jeszcze pustą czarę. Weź ją i dopilnuj, żeby tym razem dotarła do Markovicha. Bez pełnej czary nie rozpocznę wykuwania miecza.", 
			new EquipItemAction("pusta czara"));
		
		// Player doesn't have the empty goblet but claims they don't need another.
		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Dlaczego tutaj wróciłeś? Idź zgładzić jakiegoś wampira! Dowidzenia!",
			null);
		
		// Returned too early; still forging
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Jeszcze nie wykułem miecza. Przyjdź za " +
						""));
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "praca hogarta",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Jeszcze nie wykułem miecza. Przyjdź za " +
						""));
		
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new IncreaseAtkXPAction(10000));
		reward.add(new IncreaseDefXPAction(10000));
		reward.add(new IncreaseXPAction(50000));
		reward.add(new IncreaseKarmaAction(50.0));
		// here true means: yes, bound to player, in which case we also have to speciy the amount: 1
		reward.add(new EquipItemAction("krwiopijca", 1, true));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
			ConversationStates.ATTENDING, 
			"Krwiopijca jest gotowy. Dostarczona krew związała się z ostrzem, a twoja wyprawa dowiodła, że umiesz stawić czoło jego dawnemu panu. Odbierz broń i pamiętaj, ile kosztowało jej wykucie.", 
			new games.stendhal.server.entity.npc.action.QuestFeeAction(QUEST_SLOT, new MultipleActions(reward)));
        // Reuse the native stage and its guards inside the current conversation.
        npc.add(ConversationStates.ATTENDING, "praca hogarta",
				new AndCondition(new games.stendhal.server.entity.npc.condition.AlwaysTrueCondition(),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
			ConversationStates.ATTENDING, 
			"Krwiopijca jest gotowy. Dostarczona krew związała się z ostrzem, a twoja wyprawa dowiodła, że umiesz stawić czoło jego dawnemu panu. Odbierz broń i pamiętaj, ile kosztowało jej wykucie.", 
			new games.stendhal.server.entity.npc.action.QuestFeeAction(QUEST_SLOT, new MultipleActions(reward)));

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			Arrays.asList("forge", "wykuj"),
			null,
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Przynieś mi "
				+ REQUIRED_ENTRAILS
				+ " #truchło wampira, aby stworzyć miecz. Nie zapomnij też przynieść czary z krwią wampira.",
			null);

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			"truchło wampira",
			null,
			ConversationStates.IDLE,
			"Zabij wampira i przynieś mi jego truchło. Musze wiedzieć, że jesteś godny tego miecza!",
			null);
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Krwiopijca",
				"Hogart, dawny kowal krasnoludów, zna tajemnicę miecza wysysającego życie. Wysłuchaj jego opowieści i dowiedz się, czego potrzebuje do wykucia krwiopijcy.",
				false);
		prepareQuestOfferingStep();
		prepareGobletFillingStep();
		prepareForgingStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Hogarta w kuźni krasnalów.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie potrzebny mi miecz zwany krwiopijcą");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Chcę miecz wysysający krew. Potrzebuję wrócić do Hogarta z czarą wypełnioną krwią. Powinienem wybrać się do katakumb Semos!");
		}
		if ((questState.equals("start") && player.isEquipped("czara"))
				|| questState.equals("done")) {
			res.add("Wziąłem pełną czarę do Hogarata i teraz potrzebuję uzbierać 200 sztuk wampirzego truchła");
		}
		if (player.getQuest(QUEST_SLOT).startsWith("forging;")) {
			res.add("Dostarczyłem truchła i czarę do Hogarta. Teraz wyrabia mój miecz.");
		}
		if (questState.equals("done")) {
			res.add("Nareszcie dostałem krwiopijcę.	W nagrodę za ukończenie zadania otrzymałem: 50.000 pkt doświadczenia, 10.000 pkt ataku, 10.000 pkt obrony, 50 pkt karmy i krwiopijce.");
		}
		return res;
	}

	@Override
	public String getName() {
		return "VampireSword";
	}
	
	@Override
	public int getMinLevel() {
		return 50;
	}

	@Override
	public String getNPCName() {
		return "Hogart";
	}
	
	@Override
	public String getRegion() {
		return Region.ORRIL_MINES;
	}
}
