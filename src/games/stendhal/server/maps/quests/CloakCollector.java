/* $Id: CloakCollector.java,v 1.53 2011/11/13 17:12:18 kymara Exp $ */
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

import games.stendhal.common.ItemTools;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuest;
import games.stendhal.server.maps.quests.logic.BringListOfItemsQuestLogic;

import java.util.Arrays;
import java.util.List;

/**
 * QUEST: Cloak Collector
 * <p>
 * PARTICIPANTS: - Josephine, a young woman who live in Ados/Fado
 * <p>
 * STEPS:
 * <ul>
 * <li> Josephine asks you to bring her a cloak in every colour available on
 * the mainland 
 * <li> You bring cloaks to Josephine 
 * <li> Repeat until Josephine
 * received all cloaks. (Of course you can bring several cloaks at the same
 * time.) 
 * <li> Josephine gives you a reward
 * </ul>
 * <p>
 * REWARD: - black cloak - 10000 XP
 * <p>
 * REPETITIONS: - None.
 */
public class CloakCollector extends AbstractQuest implements BringListOfItemsQuest, games.stendhal.server.core.rp.quest.NativeCollectionDetails {

	private static final List<String> NEEDED_CLOAKS = Arrays.asList("peleryna",
			"peleryna elficka", "płaszcz krasnoludzki", "lazurowy płaszcz elficki", "płaszcz kamienny",
			"szmaragdowy płaszcz smoczy", "kościany płaszcz smoczy", "płaszcz licha", "płaszcz pustynny",
			"płaszcz orczy", "lazurowy płaszcz smoczy", "płaszcz z białego złota", "płaszcz ciemności", "prążkowany płaszcz lazurowy");

	private static final String QUEST_SLOT = "cloaks_collector";
	
	private BringListOfItemsQuestLogic bringItems;
	
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
		step_1();
		setupAbstractQuest();
		fillQuestInfo(
				"Płaszcze dla Kolekcjonera",
				"Josephine z Fado porównuje kroje i kolory płaszczy przynoszonych przez podróżnych. Pomóż zebrać brakujące egzemplarze z jej pierwszej kolekcji.",
				false);
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Josephine");

		// player asks about an individual cloak before accepting the quest
		for(final String itemName : NEEDED_CLOAKS) {
			npc.add(ConversationStates.QUEST_OFFERED, itemName, null,
				ConversationStates.QUEST_OFFERED, null,
				new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						Expression obj = sentence.getObject(0);
						if (obj!=null && !obj.getNormalized().equals(itemName)) {
							raiser.say("Nie znam " + obj.getOriginal() + ". Możesz podać nazwę innego płaszcza?");
						} else {
							final Item item = SingletonRepository.getEntityManager().getItem(itemName);
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("Nie widziałeś jeszcze żadnego? Cóż to jest ");
						
							if (item == null) {
								stringBuilder.append(itemName);
							} else {
								stringBuilder.append(ItemTools.itemNameToDisplayName(item.getItemSubclass()));
							}
						
							stringBuilder.append(". znajdziesz to?");
							raiser.say("Szukam właśnie tego egzemplarza: " + itemName + ". Inny płaszcz o podobnym kolorze nie zastąpi go w kolekcji. Przynieś go, kiedy trafisz na właściwy okaz.");
						}
					}

					@Override
					public String toString() {
						return "describe item";
					}
			});
		}
	}

	public List<String> getAdditionalTriggerPhraseForQuest() {
		return Arrays.asList("clothes", "ubrania");
	}

	public SpeakerNPC getNPC() {
		return npcs.get("Josephine");
	}

	public List<String> getNeededItems() {
		return NEEDED_CLOAKS;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	public List<String> getTriggerPhraseToEnumerateMissingItems() {
		return Arrays.asList("cloaks", "płaszcze");
	}

	public double getKarmaDiffForQuestResponse() {
		return 5.0;
	}

	public boolean shouldWelcomeAfterQuestIsCompleted() {
		return false;
	}

	public String welcomeBeforeStartingQuest() {
        return "Witaj! Zawsze najpierw zauważam krój i kolor ubrania, dopiero potem całą resztę. Szczególnie lubię płaszcze - chciałabym zebrać różne, także te, których tutaj nie widuję.";
    }

	public String welcomeDuringActiveQuest() {
        return "Witaj ponownie! Każdy przyniesiony płaszcz ma już swoje miejsce. Pokaż, co udało ci się znaleźć, a sprawdzę braki w kolekcji.";
    }
	
	public String welcomeAfterQuestIsCompleted() {
		return "Witaj znowu, kochany. Płaszcze wciąż wygladają wspaniale. Dziękuję!";
	}

	public String respondToQuest() {
        return "Zwykła peleryna i smoczy płaszcz mogą wyglądać jak rzeczy z dwóch różnych światów. Właśnie dlatego chcę mieć je obok siebie w jednej kolekcji. Pomożesz mi znaleźć po jednym egzemplarzu z listy?";
    }

	public String respondToQuestAcception() {
        return "Wspaniale, zaczynamy kompletowanie! Przynoś po jednym brakującym płaszczu, w dowolnej kolejności. Zapamiętam każdy otrzymany egzemplarz.";
    }

	public String respondToQuestAfterItHasAlreadyBeenCompleted() {
		return "Witaj znowu, kochany. Płaszcze wciąż wyglądają wspaniale. Dziękuję!";
	}

	public String respondToQuestRefusal() {
        return "Szkoda, już wyobrażałam sobie te kolory obok siebie. Nie nalegam; wróć, jeśli nabierzesz ochoty na poszukiwania.";
    }

	public String askForItemsAfterPlayerSaidHeHasItems() {
        return "Pokaż, który płaszcz udało ci się zdobyć. Przyjmę brakujący egzemplarz, a potem możemy obejrzeć następny.";
    }

	public String firstAskForMissingItems(final List<String> missingItems) {
        return "Na liście wciąż pozostają: " + Grammar.enumerateCollection(missingItems) + ". Przekaż po jednym brakującym egzemplarzu; resztę możesz donieść później.";
    }

	public String askForMissingItems(final List<String> missingItems) {
        return "Na liście wciąż pozostają: " + Grammar.enumerateCollection(missingItems) + ". Przekaż po jednym brakującym egzemplarzu; resztę możesz donieść później.";
    }

	public String respondToItemBrought() {
        return "Ten egzemplarz świetnie uzupełni zbiór! Powieszę go obok pozostałych. Masz jeszcze coś z brakującej listy?";
    }

	public String respondToLastItemBrought() {
        return "Teraz kolekcja jest kompletna i wreszcie mogę porównać wszystkie te kolory. Przyjmij czarny płaszcz w podziękowaniu; tobie przyda się bardziej niż mnie. Dziękuję za cierpliwe poszukiwania.";
    }

	public String respondToOfferOfNotExistingItem(final String itemName) {
		return "Nie widzę tego płaszcza w twoim ekwipunku: " + itemName + ". Przynieś go, a obejrzymy go razem.";
	}

	public String respondToOfferOfNotMissingItem() {
        return "Ten płaszcz już od ciebie dostałam. Poszukajmy miejsca dla czegoś, czego wciąż brakuje w kolekcji.";
    }

	public String respondToOfferOfNotNeededItem() {
        return "Tego egzemplarza nie ma na obecnej liście płaszczy. Sprawdź brakujące nazwy, żebym mogła przyjąć właściwą rzecz.";
    }

	public String respondToPlayerSayingHeHasNoItems(final List<String> missingItems) {
		return "Dobrze, wróć, kiedy znajdziesz któryś z brakujących płaszczy.";
	}

	public void rewardPlayer(final Player player) {
		final Item blackcloak = SingletonRepository.getEntityManager().getItem("czarny płaszcz");
		blackcloak.setBoundTo(player.getName());
		player.equipOrPutOnGround(blackcloak);
		player.addKarma(50.0);
		player.addXP(500000);
		player.addatk_xp(100000);
		player.adddef_xp(200000);
	}

	@Override
	public String getName() {
		return "CloakCollector";
	}
	
	// You can start collecting just with a simple cloak which you can buy, but maybe not a good idea to send to Fado too early.
	@Override
	public int getMinLevel() {
		return 250;
	}

	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}

	@Override
	public String getNPCName() {
		return "Josephine";
	}



    public String getCollectionKind(){return "list";}
    public java.util.Map<String,Integer> getCollectionItems(){java.util.Map<String,Integer> m=new java.util.LinkedHashMap<String,Integer>();for(String n:getNeededItems())m.put(n,1);return m;}
    public String getCollectionHint(){return "Josephine zbiera po jednym płaszczu z pierwszej listy. Możesz dostarczać je w dowolnej kolejności, po kilka przy jednej wizycie. Pełna kolekcja otworzy następną prośbę.";}
    
}
