/* $Id: HungryJoshua.java,v 1.30 2011/11/13 17:13:16 kymara Exp $ */
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

import games.stendhal.server.entity.npc.action.DialogueRewardAction;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EnableFeatureAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
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

/**
 * QUEST: Hungry Joshua 
 * 
 * PARTICIPANTS: 
 * <ul>
 * <li> Xoderos the blacksmith in Semos</li>
 * <li> Joshua the blacksmith in Ados</li>
 * </ul>
 * 
 * STEPS: 
 * <ul>
 * <li> Talk with Xoderos to activate the quest.</li>
 * <li> Make 5 sandwiches.</li>
 * <li> Talk with Joshua to give him the sandwiches.</li>
 * <li> Return to Xoderos with a message from Joshua.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> 200 XP</li>
 * <li> Karma: 10</li>
 * <li> ability to use the keyring</li>
 * </ul>
 * 
 * REPETITIONS: 
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class HungryJoshua extends AbstractQuest {
	private static final int FOOD_AMOUNT = 5;

	private static final String QUEST_SLOT = "hungry_joshua";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Zapytałem Xoderos w kuźni w Semos czy nie ma dla mnie jakiegoś zadania.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę pomagać Xoderosowi i Joshui.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "joshua", "done")) {
			res.add("Zgodziłem się na dostarczenie 5 kanapek do Joshui i powiedzenie mu, że mam jego 'jedzenie'.");
		}
		if ((questState.equals("start") && player.isEquipped("kanapka",
				FOOD_AMOUNT))
				|| questState.equals("done")) {
			res.add("Mam pięć kanapek, które zabiorę do Joshui.");
		}
		if (questState.equals("joshua") || questState.equals("done")) {
			res.add("Wziąłem jedzenie do Joshui, a on poprosił mnie o przekazanie wiadomości jego bratu Xoderosowi, że wszystko u niego jest w porządku mówiąc 'Joshua'.");
		}
		if (questState.equals("done")) {
			res.add("Przekazałem wiadomość Xoderosowi, a on naprawił mój rzemyk.");
		}
		return res;
	}

	private void step_1() {

		final SpeakerNPC npc = npcs.get("Xoderos");
		
		/** If quest is not started yet, start it. */
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, "Mój brat Joshua jest złotnikiem w Ados. Kiedy siada do pracy, potrafi zapomnieć o całym świecie, a ostatnio brakuje mu zapasów. Przydałby się ktoś, kto zaniesie mu #jedzenie i sprawdzi, czy wszystko u niego w porządku.",
			null);
		
		/** In case quest is completed */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Joshua ma już jedzenie, a ja wiem, że nic mu nie dolega. Dziękuję za drogę do Ados i za wiadomość. Teraz mogę skupić się na pracy.", null);

		/** In case quest is completed */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("food", "jedzenie"),
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Te pięć kanapek trafiło już do Joshui. Nie trzeba zanosić następnych w ramach tej przysługi. Dziękuję, że zadbano o mojego brata.", null);

		/** If quest is not started yet, start it. */
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("food","jedzenie"),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Zanieś pięć kanapek do Joshui, złotnika w Ados. Znajdziesz go w jego warsztacie; wystarczy powiedzieć, że przynosisz #jedzenie od brata. Podejmiesz się tej przysługi?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję, kamień spadł mi z serca. Zabierz pięć kanapek do warsztatu złotniczego w Ados i porozmawiaj z Joshuą o #jedzeniu. Gdy już z nim pomówisz, wróć i powiedz mi, jak się trzyma.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Szkoda, liczyłem, że pomożesz mojemu bratu. Nie mogę teraz porzucić kuźni, więc będę musiał poprosić kogoś innego. Jeśli zmienisz zdanie, wróć do sprawy jedzenia.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			"Joshua",
			null,
			ConversationStates.QUEST_OFFERED,
			"Joshua prowadzi warsztat złotniczy w Ados. Zapas pięciu kanapek pozwoli mu spokojnie popracować, a mnie przestanie dręczyć myśl, że chodzi głodny. Zaniesiesz mu je?",
			null);

		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("food", "kanapka", "sandwiches", "jedzenie", "kanapki", "kanapka"), 
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Potrzeba pięciu kanapek dla Joshui w warsztacie złotniczym w Ados. Zabierz wszystkie naraz i powiedz mu, że masz #jedzenie. Potem wróć do mnie z wiadomością od brata.", null);

		npc.add(ConversationStates.ATTENDING, "Joshua",
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Mój brat Joshua pracuje w warsztacie złotniczym w Ados. Podejdź do niego z pięcioma kanapkami i zapytaj o #jedzenie. Mam nadzieję, że przerwie pracę choć na chwilę.", null);
		
		/** remind to take the sandwiches */
		npc.add(
			ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, 
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Pamiętaj o pięciu kanapkach dla Joshui, złotnika w Ados. Kiedy je dostarczysz, wróć do kuźni i powiedz mi, czy u brata wszystko dobrze. Ta wiadomość będzie dla mnie równie ważna jak sam posiłek.",
			null);
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Joshua");

		/** If player has quest and has brought the food, ask for it */
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("food", "kanapka", "sandwiches", "jedzenie", "kanapki"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("kanapka", FOOD_AMOUNT)),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Kanapki? Dopiero teraz czuję, jak dawno nic nie jadłem. To pewnie Xoderos znów o mnie pomyślał - czy te pięć kanapek jest dla mnie?",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("kanapka", FOOD_AMOUNT));
		reward.add(DialogueRewardAction.xp(150));
		reward.add(new SetQuestAction(QUEST_SLOT, "joshua"));
		reward.add(DialogueRewardAction.karma(new IncreaseKarmaAction(15)));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES, 
			new PlayerHasItemWithHimCondition("kanapka", FOOD_AMOUNT),
			ConversationStates.ATTENDING,
			"Dziękuję, tego właśnie potrzebowałem. Wróć do Xoderosa w kuźni w Semos i powiedz mu, że Joshua ma się dobrze i dostał jedzenie. Znam mojego brata - dopóki nie usłyszy tego ode mnie lub od ciebie, będzie się zamartwiał.",
			new MultipleActions(reward));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES, 
			new NotCondition(new PlayerHasItemWithHimCondition("kanapka", FOOD_AMOUNT)),
			ConversationStates.ATTENDING, "Chyba nie masz już przy sobie wszystkich pięciu kanapek. Zajrzyj do plecaka i przynieś cały posiłek, wtedy chętnie go przyjmę. Do tej pory niczego ci nie zabrałem.", null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Rozumiem, nie będę zabierał cudzego posiłku. Jeśli te kanapki są jednak od Xoderosa, powiedz #tak, a przyjmę wszystkie pięć. Brat zawsze pamięta o sprawach, które mi umykają.",
			null);
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Xoderos");
		
		/** remind to complete the quest */
		npc.add(
			ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, 
			new QuestInStateCondition(QUEST_SLOT, "joshua"),
			ConversationStates.ATTENDING,
			"Udało ci się dotrzeć do warsztatu w Ados? Powiedz mi, jak miewa się #Joshua. Od kiedy wyruszyliście, co chwilę spoglądam w stronę drzwi.",
			null); 
		
		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("food", "kanapka", "sandwiches", "jedzenie", "kanapki"), 
			new QuestInStateCondition(QUEST_SLOT, "joshua"),
			ConversationStates.ATTENDING,
			"Najważniejsze, że jedzenie dotarło do brata. Teraz opowiedz mi, co przekazał #Joshua i czy ma się dobrze. Chcę wreszcie spokojnie wrócić do kowadła.", null);
			
		// ideally, make it so that this slot being done means
		// you get a keyring object instead what we currently
		// have - a button in the settings panel
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(DialogueRewardAction.xp(50));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(DialogueRewardAction.unlock("keyring", "Rzemyk na klucze"));	
		/** Complete the quest */
		npc.add(
			ConversationStates.ATTENDING, "Joshua", 
			new QuestInStateCondition(QUEST_SLOT, "joshua"),
			ConversationStates.ATTENDING,
			"Dobrze słyszeć, że Joshua ma się dobrze i wreszcie coś zjadł. W podzięce naprawię twoje kółko na klucze - od teraz możesz korzystać z rzemyka. Solidna robota powinna się odwdzięczyć czymś, co posłuży ci w podróży.",
			new MultipleActions(reward));
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Głodny Joshua",
				"Xoderos martwi się o swojego brata Joshuę, który mieszka w Ados, ponieważ miastu brakuje zapasów.",
				false);
		step_1();
		step_2();
		step_3();
        String hint = "Pięć kanapek zanieś do Joshui w warsztacie złotniczym w Ados. Przygotuje je Leander w piekarni w Semos: weź po pięć sztuk chleba, sera, szynki i sałaty, a potem powiedz mu: zrób 5 kanapka. U Joshui porozmawiaj o jedzeniu, a następnie wróć do Xoderosa z wiadomością.";
        npcs.get("Xoderos").add(ConversationStates.ATTENDING, "wskazówki", new QuestInStateCondition(QUEST_SLOT, "start"), ConversationStates.ATTENDING, hint, null);
        npcs.get("Joshua").add(ConversationStates.ATTENDING, "wskazówki", new QuestInStateCondition(QUEST_SLOT, "start"), ConversationStates.ATTENDING, hint, null);
        npcs.get("Joshua").add(ConversationStates.ATTENDING, "wiadomość", new QuestInStateCondition(QUEST_SLOT, "joshua"), ConversationStates.ATTENDING,
            "Powiedz Xoderosowi w kuźni w Semos, że Joshua dostał jedzenie i ma się dobrze. Wystarczy wspomnieć moje imię, a na pewno od razu zacznie wypytywać. Dziękuję, że oszczędzisz mu dalszych zmartwień.", null);

	}
	@Override
	public String getName() {
		return "HungryJoshua";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Xoderos";
	}
}
