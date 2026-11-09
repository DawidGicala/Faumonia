/* $Id: ToysCollector.java,v 1.49 2011/11/13 17:15:18 kymara Exp $ */
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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuest;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuestLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QUEST: Toys Collector
 * 
 * PARTICIPANTS: <ul>
 * <li> Anna, a girl who live in Ados </ul>
 * 
 * STEPS:
 * <ul><li> Anna asks for some toys
 * <li> You guess she might like a teddy, dice or dress
 * <li> You bring the toy to Anna
 * <li> Repeat until Anna received all toys. (Of course you can bring several
 * toys at the same time.)
 * <li> Anna gives you a reward
 * </ul>
 * REWARD:<ul>
 * <li> 3 pies
 * <li> 100 XP
 * <li> 10 Karma
 * </ul>
 * REPETITIONS: <ul><li> None.</ul>
 */
public class ToysCollector extends AbstractQuest implements BringListOfItemsQuest, games.stendhal.server.core.rp.quest.NativeCollectionDetails {

	private static final String QUEST_SLOT = "toys_collector";
	
	private BringListOfItemsQuestLogic bringItems;
	
	private static final List<String> neededToys = 
		Arrays.asList("pluszowy miś", "kości do gry", "koszula");
		
	// don't want to use the standard history for this kind of quest for anna as we dont want to say what she needs.
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }

	private void setupAbstractQuest() {
		final BringListOfItemsQuest concreteQuest = this;
		bringItems = new games.stendhal.server.maps.quests.logic.DialogueBringListOfItemsQuestLogic(concreteQuest);
		bringItems.addToWorld();
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		fillQuestInfo(
				"Kolekcjoner Zabawek",
				"Anna i jej przyjaciele z Ados chcieliby mieć więcej zabawek. Dowiedz się, czego im brakuje, i pomóż skompletować przedmioty do wspólnej zabawy.",
				false);
		setupAbstractQuest();
	}

	private void specialStuff() {
		getNPC().add(
				ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Powinieneś odejść nim wpadnę w tarapaty za rozmowę z tobą. Dowidzenia.",
				null);
	}

	public SpeakerNPC getNPC() {
		return npcs.get("Anna");
	}

	public List<String> getNeededItems() {
		return neededToys;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return Arrays.asList("lista", "kolekcja");
	}

	public List<String> getAdditionalTriggerPhraseForQuest() {
		return Arrays.asList("toys", "zabawki");
	}

	public double getKarmaDiffForQuestResponse() {
		return 8.0;
	}

	public String welcomeBeforeStartingQuest() {
        return "Cześć! Mama nie chce, żebym rozmawiała z obcymi, ale tutaj strasznie się nudzę. Przydałyby się zabawki, którymi moglibyśmy bawić się razem.";
    }

	public String welcomeDuringActiveQuest() {
        return "O, jesteś! Pamiętam wszystkie zabawki, które już dostałam. Udało ci się znaleźć jeszcze coś dla nas?";
    }

	public String welcomeAfterQuestIsCompleted() {
		return "Cześć! Jestem zajęta zabawą moimi zabawkami.";
	}

	public boolean shouldWelcomeAfterQuestIsCompleted() {
		return true;
	}

	public String respondToQuest() {
        return "Chciałabym mieć trzy różne zabawki, żeby Jens i George też mieli co robić. Nie muszą przyjść wszystkie naraz. Pomożesz mi je zebrać?";
    }

	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Zabawki są wspaniałe! Dziękuję!";
	}

	public String respondToQuestAcception() {
        return "Hurra, będziemy mieli czym się bawić! Przynoś zabawki po jednej, kiedy je znajdziesz. Będę pamiętać, co już dostałam.";
    }

	public String respondToQuestRefusal() {
        return "Szkoda, liczyłam na wspólną zabawę. Może jeszcze zmienisz zdanie i coś dla nas znajdziesz.";
    }
	
	// not used
	public String firstAskForMissingItems(final List<String> missingItems) {
        return "Na liście wciąż pozostają: " + Grammar.enumerateCollection(missingItems) + ". Przekaż po jednym brakującym egzemplarzu; resztę możesz donieść później.";
    }
	
	public String askForMissingItems(final List<String> missingItems) {
        return "Na liście wciąż pozostają: " + Grammar.enumerateCollection(missingItems) + ". Przekaż po jednym brakującym egzemplarzu; resztę możesz donieść później.";
    }

	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Dobrze. Wróć później.";
	}

	public String askForItemsAfterPlayerSaidHeHasItems() {
        return "Naprawdę coś masz? Pokaż mi zabawkę z naszej listy, a zaraz sprawdzę, czy już takiej nie dostałam.";
    }

	public String respondToItemBrought() {
        return "Ale fajna, dziękuję! Ta zabawka już zostaje z nami. Masz jeszcze którąś z pozostałych?";
    }

	public String respondToLastItemBrought() {
        return "Mamy już wszystkie zabawki, teraz nie będziemy się nudzić! Weź te tarty od Arlinda w podziękowaniu. My zajmiemy się zabawą, a tobie przydadzą się w drodze.";
    }

	public void rewardPlayer(final Player player) {
		final StackableItem pie = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"tarta");
		pie.setQuantity(30);
		player.equipOrPutOnGround(pie);
		player.addXP(2500);
		player.addatk_xp(1000);
		player.adddef_xp(1000);
		player.addKarma(10.0);
	}

	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Hej! To kłamstwo! Nie masz ze sobą "
				+ Grammar.a_noun(itemName) + ".";
	}

	public String respondToOfferOfNotMissingItem() {
        return "Taką zabawkę już mamy. Spróbuj znaleźć którąś z tych, których jeszcze brakuje.";
    }

	public String respondToOfferOfNotNeededItem() {
        return "Nie umiem bawić się czymś takim. Poszukaj proszę zabawki z naszej listy.";
    }

	@Override
	public String getName() {
		return "ToysCollector";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Anna";
	}



    public String getCollectionKind(){return "list";}
    public java.util.Map<String,Integer> getCollectionItems(){java.util.Map<String,Integer> m=new java.util.LinkedHashMap<String,Integer>();for(String n:getNeededItems())m.put(n,1);return m;}
    public String getCollectionHint(){return "Anna zbiera trzy różne zabawki. Przynoś je osobno w dowolnej kolejności; każdą dołoży do kolekcji. Pluszowy miś jest także potrzebny Plinkowi, więc zachowaj dla niego osobny egzemplarz.";}
    
}
