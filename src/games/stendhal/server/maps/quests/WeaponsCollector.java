/* $Id: WeaponsCollector.java,v 1.67 2012/04/19 18:26:42 kymara Exp $ */
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
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuest;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuestLogic;

import java.util.Arrays;
import java.util.List;

/**
 * QUEST: The Weapons Collector
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Balduin, a hermit living on a mountain between Semos and Ados
 * </ul>
 * <p>
 * STEPS:
 * <ul>
 * <li> Balduin asks you for some weapons.
 * <li> You get one of the weapons somehow, e.g. by killing a monster.
 * <li> You bring the weapon up the mountain and give it to Balduin.
 * <li> Repeat until Balduin received all weapons. (Of course you can bring up
 * several weapons at the same time.)
 * <li> Balduin gives you an ice sword in exchange.
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> ice sword
 * <li> 5000 XP
 * </ul>
 * <p>
 * REPETITIONS:
 * <li> None.
 */
public class WeaponsCollector extends AbstractQuest implements BringListOfItemsQuest, games.stendhal.server.core.rp.quest.NativeCollectionDetails {

	private static final List<String> neededWeapons = Arrays.asList("berdysz",
			"topór bojowy", "pałasz", "kiścień", "halabarda", "katana",
			"złoty buzdygan", "bułat", "kosa", "młot bojowy");

	private static final String QUEST_SLOT = "weapons_collector";

  private BringListOfItemsQuestLogic bringItems;
	
	@Override
	public List<String> getHistory(final Player player) {
        return games.stendhal.server.core.rp.quest.CollectionQuestProgress.history(this, player);
    }

	@Override
	public void addToWorld() {
		super.addToWorld();
			fillQuestInfo(
				"Kolekcjoner Broni",
				"Balduin, pustelnik z gór Ados, kolekcjonuje broń z różnych stron krainy. Zapytaj o brakujące egzemplarze i przynieś je do jego pierwszej kolekcji.",
				true);
		setupAbstractQuest();
	}

	private void setupAbstractQuest() {
		final BringListOfItemsQuest concreteQuest = this;
		bringItems = new games.stendhal.server.maps.quests.logic.DialogueBringListOfItemsQuestLogic(concreteQuest);
		bringItems.addToWorld();
	}

	public SpeakerNPC getNPC() {
		return npcs.get("Balduin");
	}

	public List<String> getNeededItems() {
		return neededWeapons;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return Arrays.asList("collection", "kolekcji", "kolekcja");
	}

	public List<String> getAdditionalTriggerPhraseForQuest() {
		return ConversationPhrases.EMPTY;
	}

	public double getKarmaDiffForQuestResponse() {
		return 0;
	}

	public String welcomeBeforeStartingQuest() {
        return "Pozdrawiam, jestem Balduin. Zbieram broń od młodości, bo po kształcie ostrza nieraz poznasz więcej niż po słowach właściciela. W mojej kolekcji nadal są luki.";
    }

	public String welcomeDuringActiveQuest() {
        return "Witaj ponownie. Wszystkie przekazane okazy są bezpieczne, a brakujące mam zapisane. Przynosisz coś, czym możemy uzupełnić kolekcję?";
    }

	public String welcomeAfterQuestIsCompleted() {
		return "Witam! Jeszcze raz dziękuję za skompletowanie mojej kolekcji.";
	}

	public boolean shouldWelcomeAfterQuestIsCompleted() {
		// because of WeaponsCollector2
		return false; 
	}

	public String respondToQuest() {
        return "Przez lata zebrałem sporo broni, ale nie każdy rodzaj udało mi się zdobyć. Chcę zachować po jednym egzemplarzu z listy, aby móc porównywać ich wykonanie. Pomożesz mi dokończyć ten zbiór?";
    }

	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Moja kolekcja jest już skompletowana! Jeszcze raz dziękuję.";
	}

	public String respondToQuestAcception() {
        return "Dobrze, odłożę dla kolekcji każdą przyniesioną sztukę. Możesz przynosić broń w dowolnej kolejności, po jednym brakującym egzemplarzu. Za pełną kolekcję odwdzięczę się parą mieczy.";
    }

	public String respondToQuestRefusal() {
        return "Rozumiem, to nie jest krótka lista. Wróć, jeśli podczas podróży zechcesz zbierać okazy również dla mnie.";
    }

	public String askForMissingItems(final List<String> missingItems) {
        return "Na liście wciąż pozostają: " + Grammar.enumerateCollection(missingItems) + ". Przekaż po jednym brakującym egzemplarzu; resztę możesz donieść później.";
    }
	public String firstAskForMissingItems(final List<String> missingItems) {
        return "Na liście wciąż pozostają: " + Grammar.enumerateCollection(missingItems) + ". Przekaż po jednym brakującym egzemplarzu; resztę możesz donieść później.";
    }


	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Daj mi znać jeżeli coś znajdziesz "
				+ Grammar.itthem(missingItems.size()) + ". Żegnaj.";
	}

	public String askForItemsAfterPlayerSaidHeHasItems() {
        return "Pokaż znalezioną broń. Sprawdzę ją z listą i odłożę do kolekcji, jeśli jeszcze jej brakuje.";
    }

	public String respondToItemBrought() {
        return "Dobry okaz, dziękuję. Zapisuję go jako przyjęty; drugi taki sam nie będzie już potrzebny. Masz jeszcze coś z listy?";
    }

	public String respondToLastItemBrought() {
        return "Ostatnia luka została uzupełniona. Przyjmij miecz leworęczny i praworęczny w zamian za wytrwałe kompletowanie zbioru. Niech służą ci równie dobrze, jak te okazy będą służyć mojej kolekcji.";
    }

	public void rewardPlayer(final Player player) {
		final Item lhandsword = SingletonRepository.getEntityManager().getItem(
			"miecz leworęczny");
		lhandsword.setBoundTo(player.getName());
		player.equipOrPutOnGround(lhandsword);
		final Item rhandsword = SingletonRepository.getEntityManager().getItem(
			"miecz praworęczny");
		rhandsword.setBoundTo(player.getName());
		player.equipOrPutOnGround(rhandsword);
		player.addXP(50000);
		player.addatk_xp(20000);
		player.adddef_xp(30000);
	}

	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Może jestem stary, ale nic nie masz "
				+ Grammar.a_noun(itemName)
				+ ". Czego tak naprawdę chcesz ode mnie?";
	}

	public String respondToOfferOfNotMissingItem() {
        return "Ten rodzaj broni już mam od ciebie. Sprawdź pozostałe braki, zanim oddasz następny egzemplarz.";
    }

	public String respondToOfferOfNotNeededItem() {
        return "Tego rodzaju broni nie szukam w obecnej kolekcji. Potrzebuję dokładnie okazów zapisanych na liście.";
    }

	@Override
	public String getName() {
		return "WeaponsCollector";
	}

 	// it can be a long quest so they can always start it before they can necessarily finish all
	@Override
	public int getMinLevel() {
		return 80;
	}

	@Override
	public String getNPCName() {
		return "Balduin";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_SURROUNDS;
	}



    public String getCollectionKind(){return "list";}
    public java.util.Map<String,Integer> getCollectionItems(){java.util.Map<String,Integer> m=new java.util.LinkedHashMap<String,Integer>();for(String n:getNeededItems())m.put(n,1);return m;}
    public String getCollectionHint(){return "Balduin chce zachować różne rodzaje broni w swojej kolekcji. Przekazuj po jednym brakującym egzemplarzu w dowolnej kolejności. Po zamknięciu tej listy będzie miał kolejne zamówienie.";}
    
}
