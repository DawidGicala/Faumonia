/* $Id: ElvishArmor.java,v 1.39 2012/04/20 17:40:43 kymara Exp $ */
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
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuest;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuestLogic;

import java.util.Arrays;
import java.util.List;

/**
 * QUEST: ElvishArmor
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Lupos, an albino elf who live in Fado Forest</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Lupos wants to see every piece of elvish equipment you can bring him</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> 20000 XP</li>
 * <li> Karma:25</li>
 * <li> ability to sell elvish stuff and also drow sword</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class ElvishArmor extends AbstractQuest implements
		BringListOfItemsQuest {

	private static final String QUEST_SLOT = "elvish_armor";
	
	private BringListOfItemsQuestLogic bringItems;
	
	private static final List<String> NEEDEDITEMS = Arrays.asList(
			"zbroja elficka", "spodnie elfickie", "buty elfickie", "miecz elficki",
			"płaszcz elficki", "tarcza elficka");

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		return bringItems.getHistory(player);
	}


	private void setupAbstractQuest() {
		final BringListOfItemsQuest concreteQuest = this;
		bringItems = new BringListOfItemsQuestLogic(concreteQuest);
		bringItems.addToWorld();
	}

  	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Zbroja Elficka",
				"Lupos elf Albinos chce zdobyć więdze o produkcji elfickiego ekwipunku. Pyta młodych podróżnych o przyniesienie kilku próbek.",
				true);
		offerSteps();
		setupAbstractQuest();
	}

	public SpeakerNPC getNPC() {
		return npcs.get("Lupos");
	}

	public List<String> getNeededItems() {
		return NEEDEDITEMS;
	}

	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return Arrays.asList("equipment", "rynsztunku");
	}

	public List<String> getAdditionalTriggerPhraseForQuest() {
		return Arrays.asList("secrets", "sekretów");
	}

	public double getKarmaDiffForQuestResponse() {
		return 5.0;
	}

	public boolean shouldWelcomeAfterQuestIsCompleted() {
		return true;
	}

	public String welcomeBeforeStartingQuest() {
		return "Witaj wędrowcze, widzę że przyszedłeś z bardzo daleka. "
			+ "Interesuję się każdym kto widział naszych pobratymców, zielonych elfów z Nalwor. Pilnują swoich #sekretów";
	}

	public String respondToQuest() {
		return "Nie chcą ujawnić wiedzy jak stworzyć zieloną zbroję, tarcze i inne. Wolą je nazywać elfiszami. "
			+ "Zastanawiam się czy przybysz taki jak ty mógłby mi przynieść dowolny elfisz?";
	}

	public String respondToQuestAcception() {
		return "Sekrety zielonych elfów w końcu byłyby nareszcie nasze! Przynieś mi wszystkie elfisze jakie znajdziesz. Dobrze Cię wynagrodzę!";
	}

	public String respondToQuestRefusal() {
		return "Widzę, że jesteś niechętny do pomocy.";
	}

	public String welcomeDuringActiveQuest() {
		return "Witam! Mam nadzieję, że dobrze Ci idzie poszukiwanie elfiszowego #rynsztunku?";
	}

	// this one not actually used here
	public String firstAskForMissingItems(final List<String> missingItems) {
		return "Słyszałem opisy "
								+ Grammar.quantityplnoun(missingItems.size(), "item", "a")
								+ " są to: "
								+ Grammar.enumerateCollection(missingItems)
								+ ". Przyniesiesz mi je?";
	}

	public String askForMissingItems(final List<String> missingItems) {
		return "Słyszałem opisy "
								+ Grammar.quantityplnoun(missingItems.size(), "item", "a")
								+ " są to: "
								+ Grammar.enumerateCollection(missingItems)
								+ ". Obrabowałeś kogoś?";
	}

	public String askForItemsAfterPlayerSaidHeHasItems() {
		return "Wyśmienicie! Jaki #rynsztunek zrabowałeś?";
	}

	public String respondToItemBrought() {
		return "Doskonała robota. Zrabowałeś coś jeszcze?";
	}


	public String respondToLastItemBrought() {
		return "Zbadam to! Elfy Albinosy mają dług wdzięczności wobec Ciebie.";
	}
								
	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Kłamca! Nie masz "
										+ Grammar.a_noun(itemName)
										+ " ze sobą.";
	}
	public String respondToOfferOfNotMissingItem() {
		return "Już mi przyniosłeś ten elfisz rynsztunek.";
	}

	public String respondToOfferOfNotNeededItem() {
		return	"Nie sądzę, aby to był fragment elfisz zbroi...";
	}

	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Rozumiem, że zielone elfy dobrze się chronią. Jeżeli mógłbym coś innego dla Ciebie zrobić to powiedz.";
	}

	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Jestem teraz zajęty badaniem właściwości elfickich części zbroi, które mi przyniosłeś. To intrygujące. Dopóki nie będę ich produkował to będę je skupywał od Ciebie."
         + "Liczę na twoje zangażowanie."; 
	}

	public void rewardPlayer(final Player player) {
		player.addKarma(20.0);
		player.addXP(20000);
	}

	public String welcomeAfterQuestIsCompleted() {
		return "Pozdrawiam stary przyjacielu.";
	}

	// the bring list of items quest doesn't include this logic:
		// player returns when the quest is in progress and says quest
		//				"As you already know, I seek elvish #equipment.";


	private void offerSteps() {
  		final SpeakerNPC npc = npcs.get("Lupos");

		// player returns after finishing the quest and says offer
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Jeżeli znajdziesz więcej elfiszów to będę wdzięczny gdybyś mógł mi je #sprzedać. Kupię elficką: zbroję, spodnie, buty, płaszcz lub miecz. Kupiłbym także miecz elfów ciemności jeżeli będziesz miał.",
				null);


		// player returns when the quest is in progress and says offer
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Nie sądzę, abym mógł Tobie zaufać ... ", null);
	}



	@Override
	public String getName() {
		return "ElvishArmor";
	}

	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getNPCName() {
		return "Lupos";
	}
	
	// it's technically in Fado forest but much nearer Kirdneh city
	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}
}
