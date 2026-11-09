/* $Id: JailedBarbarian.java,v 1.26 2011/11/13 17:14:15 kymara Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * QUEST: The Jailed Barbarian
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Lorenz, the jailed barbarian in a hut on Amazon Island</li>
 * <li>Esclara the Amazon Princess</li>
 * <li>Ylflia the Princess of Kalavan</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>1. Lorenz ask you for a scythe to bring him</li>
 * <li>2. You have to ask Princess Esclara for a 'reason'</li>
 * <li>3. You have to bring him an egg</li>
 * <li>4. You have to inform Princess Ylflia</li>
 * <li>5. You have to bring him a barbarian armor</li>
 * <li>6. You get a reward.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>You get 20 gold bars</li>
 * <li>Karma: 15</li>
 * <li>You get 52,000 experience points in all</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Not repeatable.</li>
 * </ul>
 */
 
 public class JailedBarbarian extends AbstractQuest {
 	private static final String QUEST_SLOT = "jailedbarb";
 	
	private static Logger logger = Logger.getLogger(JailedBarbarian.class);

 	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void step1() {
		final SpeakerNPC npc = npcs.get("Lorenz");	
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Nie zamierzam spędzić reszty życia w tej chacie na wyspie Amazon. Amazonki pilnują mnie, a do tego otaczają tymi okropnymi kwiatami. Pomożesz mi przygotować ucieczkę?",
				null);
							
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję za pomoc! Teraz mogę uciec!",
				null);
		

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Zacznijmy od tych kwiatów, bo już nie mogę na nie patrzeć. Przynieś jedną #kosę, sprawną i zwyczajną, nie starą kosę. Powiedz, gdy będziesz ją mieć.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Odejdź ktoś inny mi pomoże!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}
	
	private void step2() {
	final SpeakerNPC npc = npcs.get("Lorenz");	
	
	    final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("kosa"));
		reward.add(new IncreaseXPAction(1000));
		reward.add(new SetQuestAction(QUEST_SLOT, "capture"));
		reward.add(new IncreaseKarmaAction(10));
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("kosa","kosy"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
				new PlayerHasItemWithHimCondition("kosa")),
				ConversationStates.ATTENDING, 
				"Dobra kosa, wreszcie zrobię tu trochę miejsca. Teraz idź do Princess Esclary na tej samej wyspie i zapytaj ją o #Lorenz. Chcę wiedzieć, co rozpowiada o moim uwięzieniu.",
				new MultipleActions(reward));

		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("kosa","kosy"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("kosa"))),
			ConversationStates.ATTENDING,
			"Nie masz jeszcze kosy! Idź i zdobądź jakąś dla mnie. Pospiesz się!",
			null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING,
				"Już się Ciebie pytałem o przyniesienie mi #kosy do ścięcia tych kwiatków!",
				null);
	}
	
	private void step3() {
	final SpeakerNPC npc = npcs.get("Princess Esclara");
	
		npc.add(ConversationStates.ATTENDING, "Lorenz",
				new QuestInStateCondition(QUEST_SLOT, "capture"),
				ConversationStates.ATTENDING,
				"Lorenz i jego towarzysze kopali #tunel na naszą wyspę bez naszej zgody. Nie zamknęłyśmy go tu dla zabawy. Jeśli chcesz usłyszeć jego wersję, wróć do niego i zapytaj o tunel.",
				new SetQuestAction(QUEST_SLOT, "princess"));
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("tunnel", "tunel"),
				new QuestInStateCondition(QUEST_SLOT, "princess"),
				ConversationStates.ATTENDING, "Jestem teraz wściekła i nie chce już o tym rozmawiać! Jeżeli chcesz się dowiedzieć więcej to musisz go zapytać o #tunel!",
				null);	

	}
	
	private void step4() {
	final SpeakerNPC npc = npcs.get("Lorenz");
	
		npc.add(ConversationStates.ATTENDING, Arrays.asList("tunnel", "tunel"),
				new QuestInStateCondition(QUEST_SLOT, "princess"),
				ConversationStates.ATTENDING, 
				"A więc znów opowiada o tunelu. Nie mam teraz siły się spierać, bo burczy mi w brzuchu. Przynieś jedno #jajo, potem pomyślimy, co dalej.",
				new SetQuestAction(QUEST_SLOT, "jajo"));	
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "capture"),
				ConversationStates.ATTENDING,
				"Proszę zapytaj Princess Esclara dlaczego tutaj jestem! Sądzę, że wypowiadając moje imię sprowokujesz ją do wyjawienia powodu",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "princess"),
				ConversationStates.ATTENDING,
				"Założę się, że Princess Esclara powiedziała, że zostałem uwięziony za #tunel ... ",
				null);
	}
	
	private void step5() {
		final SpeakerNPC npc = npcs.get("Lorenz");	
		
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("jajo"));
		reward.add(new IncreaseXPAction(1000));
		reward.add(new SetQuestAction(QUEST_SLOT, "jailed"));
		reward.add(new IncreaseKarmaAction(10)); 
		
		npc.add(ConversationStates.ATTENDING, "jajo",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "jajo"),
						new PlayerHasItemWithHimCondition("jajo")),
						ConversationStates.ATTENDING, 
						"To pomoże mi odzyskać siły. Udaj się teraz do Princess Ylflii w zamku Kalavan i powiedz, że Lorenz jest #uwięziony na wyspie Amazon. Może ona zechce o mnie pamiętać.",
						new MultipleActions(reward));

		npc.add(
			ConversationStates.ATTENDING, "jajo",
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "jajo"), new NotCondition(new PlayerHasItemWithHimCondition("jajo"))),
			ConversationStates.ATTENDING,
			"Nie widzę jaja!!",
			null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "jajo"),
				ConversationStates.ATTENDING,
				"Prosiłem Ciebie o przyniesienie #jaja!",
				null);
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("jailed", "uwięziony"),
				new QuestInStateCondition(QUEST_SLOT, "jailed"),
				ConversationStates.ATTENDING, "Wiem to, *Jestem* uwięziony! Potrzebuję Ciebie, abyś powiedział Princess Ylflia, że jestem tutaj!",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "jailed"),
				ConversationStates.ATTENDING,
				"Potrzebuję Ciebie, abyś poszedł do Princess Ylflia i powiedział, że jestem tutaj #uwięziony.",
				null);
	}
	
	private void step6() {
	final SpeakerNPC npc = npcs.get("Princess Ylflia");
	
		npc.add(ConversationStates.ATTENDING, Arrays.asList("jailed", "Lorenz", "uwięziony"),
				new QuestInStateCondition(QUEST_SLOT, "jailed"),
				ConversationStates.ATTENDING, 
				"Lorenz jest uwięziony? Nie wspominaj o naszej rozmowie mojemu ojcu. Przekaż Lorenzowi moje #pozdrowienia i sprawdź, czy nie potrzebuje jeszcze pomocy.",
				new SetQuestAction(QUEST_SLOT, "spoken"));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("spoken", "pozdrowienia"),
				new QuestInStateCondition(QUEST_SLOT, "spoken"),
				ConversationStates.ATTENDING, "Idź i przekaż Lorenz moje #pozdrowienia.",
				null);

	}

	private void step7() {
	final SpeakerNPC npc = npcs.get("Lorenz");
	
		npc.add(ConversationStates.ATTENDING, Arrays.asList("spoken", "pozdrowienia"),
				new QuestInStateCondition(QUEST_SLOT, "spoken"),
				ConversationStates.ATTENDING, 
				"Dobrze wiedzieć, że Ylflia o mnie nie zapomniała. Do ucieczki brakuje mi już tylko zbroi barbarzyńcy. Przynieś ją i powiedz #zbroję; bez osłony daleko nie zajdę.",
				new SetQuestAction(QUEST_SLOT, "armor"));
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "spoken"),
				ConversationStates.ATTENDING,
				"Teraz musisz porozmawiać z Princess Ylflia ... Mam nadzieję, że przesłała swoje najgorętsze #pozdrowienia...",
				null);
	}
	
	private void step8() {
		final SpeakerNPC npc = npcs.get("Lorenz");	

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("zbroja barbarzyńcy"));
		reward.add(new IncreaseXPAction(250000));
		reward.add(new IncreaseAtkXPAction(30000));
		reward.add(new IncreaseDefXPAction(40000));
		reward.add(new EquipItemAction("sztabka złota", 33));
		reward.add(new EquipItemAction("sztabka mithrilu", 12));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(100)); 
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("armor", "zbroja", "zbroję"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "armor"),
						new PlayerHasItemWithHimCondition("zbroja barbarzyńcy")),
						ConversationStates.ATTENDING, 
						"Zbroja pasuje, mogę wreszcie przygotować ucieczkę. Weź część złota i mithrilu, które ukryłem przed Esclarą, jako zapłatę za wszystkie przysługi. O pochodzenie tych sztabek lepiej jej nie pytaj.",
						new MultipleActions(reward));

		npc.add(
			ConversationStates.ATTENDING, "armor",
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "armor"), new NotCondition(new PlayerHasItemWithHimCondition("zbroja barbarzyńcy"))),
			ConversationStates.ATTENDING,
			"Nie posiadasz zbroi barbarzyńcy przy sobie! Idź i zdobądź!",
			null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "armor"),
				ConversationStates.ATTENDING,
				"Czekam na  #zbroję barbarzyńcy od Ciebie. Teraz jestem dość silny, aby uciec.",
				null);
	}
	
	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Uwięziony Barbarzyńca",
				"Lorenz jest więźniem Amazonek na wyspie Amazon. Pomóż mu przygotować ucieczkę i poznaj sprawę tunelu oraz jego znajomości z księżniczką Ylflią.",
				true);
		step1();
		step2();
		step3();
		step4();
		step5();
		step6();
		step7();
		step8();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Odnalazłem drogę do chaty Lorenza na wyspie Amazon.");
		res.add("Lorenz potrzebuje kosę, ale nie starą, aby ściąć kwiaty. Uważa je za brzydkie.");
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomóc Lorenzowi w wycięciu kwiatów. Za cokolwiek został skazany, założę się, że na to zasłużył.");
			return res;
		} 
		if ("start".equals(questState)) {
			return res;
		} 
		res.add("Lorenz chce abym udał się do Princess Esclara na wyspie Amazon z zapytaniem: 'dlaczego został pojmany'. Muszę powiedzieć jej jego imię aby jej przypomnieć.");
		if ("capture".equals(questState)) {
			return res;
		} 
		res.add("Księżniczka powiedziała mi, że Lorenz jest skazany za kopanie tunelu! Tak więc musze do niego wrócić i mu powiedzieć, że to za tunel.");
		if ("princess".equals(questState)) {
			return res;
		}
		res.add("Lorenz nagle zgłodniał i zażądał dostarczenia mu jajka. On jest naprawdę wybuchowy.");
		if ("jajo".equals(questState)) {
			return res;
		}
		res.add("Teraz muszę powiedzieć Princess Ylflia, dlaczego Lorenz nie było tak długo ... Nie jestem nawet pewien, czy zna ją! Ale i tak muszę powiedzieć jej jego imię.");
		if ("jailed".equals(questState)) {
			return res;
		}
		res.add("Princess Ylflia prosiła mnie, aby przekazał Lorencowi pozdrowienia.");
		if ("spoken".equals(questState)) {
			return res;
		}
		res.add("Lorenz w końcu postanowił spróbować uwolnić się. Muszę mu znaleść zbroję barbarzyńcy.");
		if ("armor".equals(questState)) {
			return res;
		}
		res.add("Przyniosłem Lorenzowi zbroje! Dał mi zrabowane złoto i mithril Księżniczce.	W nagrodę za ukończenie zadania otrzymałem: 250.000 pkt doświadczenia, 30.000 pkt ataku, 40.000 pkt obrony, 100 pkt karmy, 33 sztabki złota i 12 sztabek mithrilu.");
		if (isCompleted(player)) {
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}
	
	@Override
	public String getName() {
		return "JailedBarbarian";
	}
	
	// Amazon is dangerous below this level - don't hint to go there
	@Override
	public int getMinLevel() {
		return 300;
	}
	
	@Override
	public String getRegion() {
		return Region.AMAZON_ISLAND;
	}
	@Override
	public String getNPCName() {
		return "Lorenz";
	}
}
 
