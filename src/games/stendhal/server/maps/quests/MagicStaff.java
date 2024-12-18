/* $Id: HelpMrsYeti.java,v 1.16 2012/04/24 17:01:18 kymara Exp $ */
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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.creature.Pet;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasPetOrSheepCondition;
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
import marauroa.common.Pair;

import org.apache.log4j.Logger;

 public class MagicStaff extends AbstractQuest {
	 
	 	private static Logger logger = Logger.getLogger(MagicStaff.class);

	private static final String QUEST_SLOT = "magicstaff";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void Start() {
		final SpeakerNPC npc = npcs.get("Samaron");	

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Na świecie istnieją trzy magiczne części, z których można stworzyć niezwykle potężną różdżkę. Te artefakty są ukryte w różnych zakątkach świata i tylko najodważniejsi zdołają je odnaleźć. Jeśli jesteś zainteresowany i chcesz podjąć się tego wyzwania, napisz #tak",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Gratulacje! Udało Ci się zdobyć potężną różdżkę. Pamiętaj, że z wielką mocą wiąże się wielka odpowiedzialność. Uważaj na siebie!",
				null);


		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Znam lokalizację tylko jednej z tych części - znajduje się ona na wyspie Athor. Tamtejszy czarodziej #Woy'Tyk będzie w stanie Cię poprowadzić. Powiedz mu po prostu #różdżka, a on wskaże Ci drogę. Dzięki tej części powinieneś być w stanie zlokalizować pozostałe dwie. Ruszaj bez zwłoki i nie trać czasu!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Szkoda, że nie chcesz podjąć się tego wyzwania i zdobyć tak potężnej różdżki. To Twoja strata, ale może kiedyś zmienisz zdanie.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void Pierwszy() {
	final SpeakerNPC npc = npcs.get("Woy'tyk");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("kawałek", "różdżka"),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING, "Skąd wiesz o tej potężnej broni? Nieważne... posiadam tę różdżkę, ale niestety jest uszkodzona. Jednak za jej pomocą można odnaleźć dwie pozostałe części, które przywrócą jej dawny blask. Aby udowodnić, że jesteś godzien tej różdżki, musisz przynieść mieszkańcom Wyspy Athor trochę #jedzenia. W Semos znajduje się piekarnia prowadzona przez #Leandera - on powinien pomóc Ci z tym zadaniem. Powiedz mu po prostu #jedzenie",
				new SetQuestAction(QUEST_SLOT, "first"));

		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("jedzenia","jedzenie","przynieś"),
			new NotCondition(new QuestInStateCondition(QUEST_SLOT, "first")),
			
			ConversationStates.ATTENDING,
			"Zanim dostaniesz tę różdżkę, musisz przynieść tubylcom trochę jedzenia. Pomoże Ci w tym #Leander w Semos. Powiedz mu po prostu #jedzenie.",
			new SetQuestAction(QUEST_SLOT, "first1"),
			null);

		npc.add(ConversationStates.ATTENDING, Arrays.asList("jedzenie","gotowe","przyniesione"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "first1"),
				new PlayerHasItemWithHimCondition("paczka jedzenia")),
				ConversationStates.ATTENDING, "Bardzo dobrze! Teraz przynieś mi jeszcze dziesięć zwykłych różdżek - chcę nauczyć dzieci na Athor posługiwania się magią. Będę też potrzebował trochę magii - 2500 magii ziemi i 2000 magii ognia wystarczy. Powiedz #gotowe, jak wrócisz.",
				new MultipleActions(new SetQuestAction(QUEST_SLOT, "first2"), new DropItemAction("paczka jedzenia")));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("gotowe","przyniesione"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "first1"),
				new NotCondition(new PlayerHasItemWithHimCondition("paczka jedzenia"))),
				ConversationStates.ATTENDING, "Rozumiem, że byłeś już u Leandera? Co z jedzeniem?",
				null);

		final List<ChatAction> potionactions = new LinkedList<ChatAction>();
		potionactions.add(new DropItemAction("różdżka",10));
		potionactions.add(new DropItemAction("magia ziemi",2500));
		potionactions.add(new DropItemAction("magia płomieni",2000));
		potionactions.add(new EquipItemAction("uszkodzona różdżka"));
		potionactions.add(new IncreaseXPAction(5000));
		potionactions.add(new IncreaseAtkXPAction(1000));
		potionactions.add(new IncreaseDefXPAction(1000));
		potionactions.add(new SetQuestAction(QUEST_SLOT, "gotfirstpiece"));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("jedzenie","gotowe","przyniesione"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "first2"),
								 new PlayerHasItemWithHimCondition("magia ziemi",2500),
								 new PlayerHasItemWithHimCondition("magia płomieni", 2000),
								 new PlayerHasItemWithHimCondition("różdżka", 10)),
				ConversationStates.ATTENDING, "Widzę, że masz wszystko, czego potrzebujemy: dziesięć zwykłych różdżek, 2500 jednostek magii ziemi i 2000 jednostek magii płomieni. Doskonale! Oto uszkodzona różdżka. Teraz musisz udać się do Kirdneh i poszukać #Ylrika. On na pewno będzie wiedział, gdzie znajdują się pozostałe części #różdżki. Powodzenia!",
				new MultipleActions(potionactions));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("jedzenia","gotowe","przyniesione"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "first2"),
								 new NotCondition(
												  new AndCondition(
																  new PlayerHasItemWithHimCondition("magia ziemi",2500),
																  new PlayerHasItemWithHimCondition("magia płomieni", 2000),
																  new PlayerHasItemWithHimCondition("różdżka", 10)))),
				ConversationStates.ATTENDING, "Przynieś mi dziesięć zwykłych różdżek, 2500 jednostek magii ziemi i 2000 jednostek magii płomieni - chcę nauczyć dzieci na Athor posługiwania się magią. Pamiętaj, że magia ziemi i magii płomieni są kluczowe do naszego sukcesu. Gdy będziesz miał wszystko, wróć do mnie i powiedz #gotowe. Dzięki!", null);


	}

	private void Zarcie() {

	final SpeakerNPC npc = npcs.get("Leander");
		npc.add(ConversationStates.ATTENDING, "jedzenie",
				new QuestInStateCondition(QUEST_SLOT, "first"),
				ConversationStates.ATTENDING, "Jak to na Athor nie ma wystarczająco jedzenia? Oczywiście, że przygotuję dla nich racje żywnościowe, ale nie mam odpowiedniej ilości składników. Sam załatwię trochę, ale Ty musisz przynieść mi 60 sztuk mięsa z kraba. To powinno załatwić sprawę. ",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "first1", 1.0));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("mięso", "jedzenie"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "first1"),
				new PlayerHasItemWithHimCondition("mięso z kraba",60)),
				ConversationStates.ATTENDING, "Świetnie, widzę, że przyniosłeś 60 sztuk mięsa z kraba. Okej, przygotowałem paczkę z #jedzeniem. Teraz zanieś ją na Athor. Mieszkańcy będą Ci wdzięczni za pomoc. ",
				new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "first1", 1.0), new DropItemAction("mięso z kraba",60), new EquipItemAction("paczka jedzenia",1)));

	}

	private void Drugi() {
	final SpeakerNPC npc = npcs.get("Ylrik");
	
		final String extraTrigger = "różdżka";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		final List<ChatAction> tookpotionactions = new LinkedList<ChatAction>();
		tookpotionactions.add(new DropItemAction("uszkodzona różdżka"));
		tookpotionactions.add(new IncreaseKarmaAction(10.0));
		tookpotionactions.add(new IncreaseXPAction(5000));
		tookpotionactions.add(new IncreaseAtkXPAction(1000));
		tookpotionactions.add(new IncreaseDefXPAction(1000));
		tookpotionactions.add(new SetQuestAction(QUEST_SLOT, "ptaszory"));


		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "gotfirstpiece"),
								 new PlayerHasItemWithHimCondition("uszkodzona różdżka")),
				ConversationStates.ATTENDING, "Masz uszkodzoną różdżkę? To oznacza, że znasz Woy'tyka. Słuchaj, posiadam magiczny katalizator oraz kryształ mocy, czyli elementy potrzebne do naprawienia tej różdżki. Jednak nie ma nic za darmo. Ostatnio na wschód od Kirdneh pojawiła się plaga ptaków. Są one bardzo silne i sami nie dajemy im rady. Idź i przynieś nam 40 muchomorów - tym sposobem je wytrujemy. Ja w międzyczasie popatrzę, co da się zrobić z tą różdżką. Jak wrócisz, powiedz #różdżka", 
				new MultipleActions(tookpotionactions));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "ptaszory"),
								 new PlayerHasItemWithHimCondition("muchomor",40)),
				ConversationStates.ATTENDING, "Widzę, że masz muchomory. Doskonale! Oto obiecane części różdżki: magiczny katalizator i kryształ mocy. Wróć teraz do osoby, która wykona dla Ciebie taką różdżkę. Samaron na pewno posiada takie umiejętności. Powiedz mu #różdżka",new MultipleActions(
				new DropItemAction("muchomor", 40),
				new EquipItemAction("magiczny katalizator", 1),
				new EquipItemAction("kryształ mocy", 1),
				new EquipItemAction("uszkodzona różdżka", 1),
				new SetQuestAction(QUEST_SLOT, "powrot")));
				

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "ptaszory"),
				new NotCondition(new PlayerHasItemWithHimCondition("muchomory",40))),
				ConversationStates.ATTENDING, "Przynieś najpierw 40 muchomorów, które obiecałeś. Potrzebujemy ich, aby poradzić sobie z plagą ptaków.",
				null);
		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "gotfirstpiece"), new NotCondition(new PlayerHasItemWithHimCondition("uszkodzona różdżka"))),
			ConversationStates.ATTENDING,
			"Przynieś uszkodzoną różdżkę. Bez niej nie możemy rozpocząć naprawy.",
			null);
	}


	private void Powrot() {
	final SpeakerNPC npc = npcs.get("Samaron");

		final String extraTrigger = "różdżka";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "killing"));
		actions.add(new DropItemAction("uszkodzona różdżka", 1));
		actions.add(new DropItemAction("magiczny katalizator", 1));

		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "powrot"),
								new PlayerHasItemWithHimCondition("uszkodzona różdżka", 1),
								 new PlayerHasItemWithHimCondition("magiczny katalizator", 1),
								 new PlayerHasItemWithHimCondition("kryształ mocy", 1)),
			ConversationStates.ATTENDING,
			"Znalazłeś wszystkie trzy części! To niesamowite! Daj mi je, zacznę je studiować. Kryształ mocy zostaw na razie przy sobie - przyniesiesz mi go później. Teraz przynieś mi jeszcze diament, 1000 jednostek magii deszczu, 500 jednostek magii mroku, 250 jednostek magii światła oraz pazur zielonego, czarnego i złotego smoka. Potrzebuję również podstawę różdżki... może to być różdżka Swaroga. Bez tych składników nie będę w stanie złożyć różdżki w całość. Kiedy zdobędziesz wszystkie materiały, wróć do mnie i powiedz #różdżka",
			new MultipleActions(actions));
	
		final List<ChatAction> actions2 = new LinkedList<ChatAction>();
		actions2.add(new DropItemAction("magia deszczu", 1000));
		actions2.add(new DropItemAction("magia mroku", 500));
		actions2.add(new DropItemAction("magia światła", 250));
		actions2.add(new DropItemAction("diament", 1));
		actions2.add(new DropItemAction("pazur zielonego smoka", 1));
		actions2.add(new DropItemAction("pazur czarnego smoka", 1));
		actions2.add(new DropItemAction("pazur złotego smoka", 1));
		actions2.add(new DropItemAction("różdżka Swaroga", 1));
		actions2.add(new DropItemAction("kryształ mocy", 1));
		actions2.add(new EquipItemAction("różdżka mokoszy", 1));
		actions2.add(new SetQuestAction(QUEST_SLOT, "done"));

	npc.add(ConversationStates.ATTENDING, Arrays.asList("różdżka","gotowe","przyniesione"),
								new AndCondition(
								 new PlayerHasItemWithHimCondition("magia światła", 250),
								 new PlayerHasItemWithHimCondition("magia deszczu", 1000),
								 new PlayerHasItemWithHimCondition("magia mroku", 500),
								 new PlayerHasItemWithHimCondition("różdżka Swaroga", 1),
								 new PlayerHasItemWithHimCondition("kryształ mocy", 1),
								 new PlayerHasItemWithHimCondition("diament", 1),
								 new PlayerHasItemWithHimCondition("pazur zielonego smoka", 1),
								 new PlayerHasItemWithHimCondition("pazur czarnego smoka", 1),
								 new PlayerHasItemWithHimCondition("pazur złotego smoka", 1)
								 ),
				ConversationStates.ATTENDING, "Widzę, że masz wszystkie potrzebne materiały: diament, magia deszczu, magia mroku, magia światła oraz pazury zielonego, czarnego i złotego smoka, a także różdżkę Swaroga. To naprawdę imponujące! Poczekaj chwilę, zabiorę się za składanie... Gotowe! Proszę, oto Twoja różdżka. Wykorzystaj jej moc mądrze. ",
				new MultipleActions(actions2));
	}

	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Magiczna Różdżka",
				"Znajdź trzy części magicznej różdżki, aby stworzyć niezwykle potężny artefakt. Te magiczne części są ukryte w różnych zakątkach świata. Aby je odnaleźć, musisz podjąć się wielu wyzwań i pokonać liczne przeszkody. Tylko najodważniejsi i najzręczniejsi zdołają ukończyć to zadanie i zdobyć różdżkę Mokoszy, która może przeważyć szalę w każdej bitwie.",
				true);
		Start();
		Pierwszy();
		Zarcie();
		Drugi();
		Powrot();
	}

  @Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem potężnego maga w górach Ados, który opowiedział mi o magicznej różdżce składającej się z trzech części");
			res.add("Mam je odnaleźć, aby zdobyć potężny artefakt.");
			if ("rejected".equals(questState)) {
				res.add("Zrezygnowałem z zadania i nie chcę szukać żadnej różdżki.");
				return res;
			} 
			if ("start".equals(questState)) {
				res.add("Muszę znaleźć Woy'tyka na wyspie Athor. On może znać lokalizację jednej z części magicznej różdżki.");
				return res;
			} 
			if ("first".equals(questState)) {
				res.add("Woy'tyk zlecił mi przyniesienie jedzenia dla mieszkańców Athor, aby udowodnić swoją wartość. Muszę znaleźć Leandera w Semos, który mi pomoże.");
				return res;
			} 
			if ("first1".equals(questState)) {
				res.add("Powinienem wrócić na wyspę Athor z jedzeniem dla mieszkańców.");
				return res;
			} 
			if ("first2".equals(questState)) {
				res.add("Woy'tyk chce, żebym przyniósł 10 różdżek i magie dla mieszkańców Athor. Potrzebuję 2500 jednostek magii ziemi i 2000 jednostek magii ognia.");
				return res;
			} 
			if ("gotfirstpiece".equals(questState)) {
				res.add("Mam uszkodzoną różdżkę. Powinienem udać się do Kirdneh i znaleźć Ylrika, który może wiedzieć, gdzie są pozostałe części różdżki.");
				return res;
			} if ("ptaszory".equals(questState)) {
				res.add("Ylrik poprosił mnie o zdobycie 40 muchomorów, aby poradzić sobie z plagą ptaków na wschód od Kirdneh.");
				return res;
			} if ("powrot".equals(questState)) {
				res.add("Powinienem wrócić do Samarona z magicznym katalizatorem, kryształem mocy i uszkodzoną różdżką, aby mógł kontynuować swoje badania.");
				return res;
			} if ("killing".equals(questState)) {
				res.add("Samaron potrzebuje jeszcze diament, 1000 jednostek magii deszczu, 500 jednostek magii mroku, 250 jednostek magii światła oraz pazur zielonego, czarnego i złotego smoka, aby złożyć różdżkę w całość.");
				return res;
			} 
			if ("done".equals(questState)) {
				res.add("Pomogłem Samoronowi i dostałem potężną różdżkę.	W nagrodę za ukończenie zadania otrzymałem: 10.000 pkt doświadczenia i różdżkę Mokszy.");
				return res;
			} 
			final List<String> debug = new ArrayList<String>();
			debug.add("");
			logger.error("");
			return debug;

	}
	
	@Override
	public String getName() {
		return "MagicStaff";
	}

		@Override
	public int getMinLevel() {
		return 300;
	}

	@Override
	public String getNPCName() {
		return "Samaron";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}

