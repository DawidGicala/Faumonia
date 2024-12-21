/* $Id: FaumoniaNPC.java,v 1.15 2023/07/13 00:08:30 Szygolek / FaumoniaOnline Exp $ */
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
package games.stendhal.server.maps.testing_grid;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.ShoutBehaviour;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

public class FaumoniaNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String[] text = {												
								"Atlas map na stronie Faumonii to idealne miejsce, aby poznać teren – sprawdź już teraz! #https://faumonia.pl/atlas-map/","Marzysz o zdobyciu najpotężniejszych przedmiotów w Faumonii? Sprawdź listę zadań, które Ci to umożliwią! Szczegóły na stronie: #https://faumonia.pl/zadania-na-najsilniejsze-przedmioty-w-grze/","Tworzysz memy albo fanarty? Kanał #memy-i-fanarty na Discordzie Faumonii to idealne miejsce, by podzielić się swoją twórczością i zyskać uznanie społeczności!","Nie możesz znaleźć kupca w grze? Spróbuj w naszej grupie na Facebooku – Faumonia – Gracze. Handel jeszcze nigdy nie był tak prosty! #https://www.facebook.com/groups/faumoniaonline","Faumonia na YouTube to miejsce pełne epickich bitew i poradników! Sprawdź nasz kanał: #https://www.youtube.com/@faumonia","Plagi, wskazówki, zabawne momenty i więcej! Znajdziesz to wszystko na naszym TikToku – zobacz sam! #https://www.tiktok.com/@faumonia","Zgubiłeś się w świecie Faumonii? Poradniki na naszej stronie wskażą Ci drogę! #https://faumonia.pl/poradniki/","Dbaj o atmosferę w Faumonii – nie używaj wulgaryzmów na chacie globalnym. Przekraczanie granic to pewna kara w postaci wyciszenia.","Potrzebujesz pomocy? Sprawdź poradniki na naszej stronie i poznaj lepiej krainę Faumonii! #https://faumonia.pl/poradniki/","Ktoś Cie obraża? Wpisz komendę #/ignore #nazwagracza a już nigdy nie zobaczysz jego wiadomości.","Chcesz więcej wyzwań? Zakładka Zadania na naszej stronie to miejsce, gdzie znajdziesz misje, które warto podjąć! #https://faumonia.pl/zadania/","Gdzie znaleźć potwora, którego szukasz? Starkad, który przechadza się po banku Semos, zna odpowiedź na twoje pytanie.","Nowy w Faumonii? A może potrzebujesz wskazówek? Zajrzyj do naszych poradników i poznaj świat gry! #https://faumonia.pl/poradniki/","Masz grupę przyjaciół i chcesz utworzyć wspólny czat? Wejdź w zakładkę #Grupa w lewym dolnym rogu, następnie naciśnij środkową ikonkę i załóż nową grupę. Aby zaprosić kolegę wpisz komendę #/group #invite #nazwagracza.","Szukasz bestii, która ci umyka? Odwiedź Starkada w banku Semos – wie wszystko o mieszkańcach Faumonii.","Śledź #ogłoszenia na Discordzie, aby zawsze być na bieżąco z nowościami w Faumonii! Informacje o eventach, aktualizacjach i ogłoszeniach czekają na Ciebie.","Rozwijasz swoje umiejętności? Pilnuj postaci podczas treningu – na arenie obowiązuje walka na śmierć i życie, a porażka oznacza stratę ekwipunku i cennych skilli!","Faumonia teraz na Discordzie! Rozmawiaj z innymi wojownikami, sprzedawaj przedmioty, wymieniaj się poradami i śledź najnowsze ogłoszenia. Znajdziesz tam wszystko, czego potrzebujesz, by cieszyć się grą jeszcze bardziej!","Czy obserwujesz nas już na Facebooku i Instagramie? Zaobserwuj nas i otrzymuj ekspresowe powiadomienia o plagach i konkursach! #https://www.facebook.com/faumoniaonline #https://www.instagram.com/faumoniaonline/","Twórzmy razem dobrą atmosferę w grze - Nie przeszkadzaj innym w grze. Za natarczywe przeszkadzanie innym zostaniesz ukarany więzieniem nawet na 8 godzin!","Mistrzowie Gry to seria wyzwań o rosnącym poziomie trudności, która przeprowadzi Cie przez całą rozgrywkę w krainie Faumonii. Szczegółowe informacje znajdziesz na stronie: #https://faumonia.pl/zadania-mistrzowie-gry/","Zasady chatu globalnego są proste: brak wulgaryzmów. Natarczywe przeklinanie to prosta droga do wyciszenia aż na tydzień!","Atlas map na stronie Faumonii to idealne miejsce, aby poznać teren – sprawdź już teraz! #https://faumonia.pl/atlas-map/","Plagi, konkursy i wydarzenia – śledź Faumonię na Facebooku i Instagramie, aby nie przegapić nic ważnego! #https://www.facebook.com/faumoniaonline #https://www.instagram.com/faumoniaonline/","Masz pomysły i chcesz pomóc w rozwoju Faumonii? Poszukujemy zmotywowanych wojowników do współpracy – skontaktuj się z administratorem!","Ados skrywa niejedną tajemnicę i trudne zadania. Chcesz dowiedzieć się, co na Ciebie czeka? Sprawdź szczegóły na stronie! #https://faumonia.pl/zadania-stolica-ados/","Twoja wiedza i zaangażowanie mogą pomóc Faumonii rosnąć! Jeśli chcesz wspierać rozwój gry, zgłoś się do administratora.","Zastanawiasz się gdzie expić? Sprawdź zakładkę Expowiska na naszej stronie internetowej i ruszaj do walki! #https://faumonia.pl/expowiska/","Chcesz zdobyć doświadczenie i nagrody, pomagając mieszkańcom Semos? Odwiedź naszą stronę, aby poznać listę zadań dostępnych w mieście. #https://faumonia.pl/zadania-miasto-semos/","Nie przegap nowości z Faumonii! Najnowsze wiadomości znajdziesz tutaj: #https://faumonia.pl/aktualnosci-ze-swiata-faumonii/","Chcesz więcej wyzwań? Zakładka Zadania na naszej stronie to miejsce, gdzie znajdziesz misje, które warto podjąć! #https://faumonia.pl/zadania/","Nie wiesz, gdzie zdobywać doświadczenie? Odwiedź zakładkę #Expowiska na stronie i znajdź idealne miejsce do expienia! #https://faumonia.pl/expowiska/","Gdzie znaleźć potwora, którego szukasz? Starkad, który przechadza się po banku Semos, zna odpowiedź na twoje pytanie.","Nie wiesz, gdzie znaleźć odpowiednią mapę? Zajrzyj do atlasu map na naszej stronie i odkrywaj Faumonię! #https://faumonia.pl/atlas-map/","Nie możesz znaleźć konkretnego potwora? Starkad w banku Semos zna ich wszystkie kryjówki – odwiedź go i zapytaj!","Szukasz kupca na swoje przedmioty? Nasza grupa na Facebooku to idealne miejsce, by znaleźć nabywców! #https://www.facebook.com/groups/faumoniaonline","Fado, Kirdneh i Kalavan skrywają niejedną tajemnicę. Poznaj zadania, które możesz tam wykonać – wszystko znajdziesz na stronie! #https://faumonia.pl/zadania-w-poludniowych-miastach-fado-kirdneh-kalavan/","Ćwicz swoje umiejętności na arenie treningowej, ale bądź ostrożny – inni wojownicy mogą zaatakować cię w każdej chwili!","Szukasz nowych przygód? Zajrzyj do zakładki Zadania na stronie Faumonii i sprawdź, jakie wyzwania czekają! #https://faumonia.pl/zadania/","Nie chcesz widzieć wiadomości od kogoś, kto Ci przeszkadza? Wpisz #/ignore #nazwagracza i ciesz się spokojem!","Twórzmy razem dobrą atmosferę w grze - Nie przeklinaj na chacie globalnym. Za natarczywe przeklinanie na chacie globalnym zostaniesz ukarany wyciszeniem nawet na 7 dni!","Masz ochotę na luźną rozmowę? Na Discordzie Faumonii, w kanale #ogólny, spotkasz wojowników z całej krainy, którzy chętnie podzielą się swoimi przygodami.","Plagi, wskazówki, zabawne momenty i więcej! Znajdziesz to wszystko na naszym TikToku – zobacz sam! #https://www.tiktok.com/@faumonia","Potrzebujesz wskazówki lub nie wiesz, co robić? Zadaj swoje pytanie na kanale #pytania na Discordzie Faumonii i znajdź odpowiedź!","Faumonia is home to warriors of many languages. Use #english-only on our Discord to communicate with everyone in one universal language!","Na Discordzie Faumonii znajdziesz miejsce na każdą rozmowę – kanał #offtop czeka na Twoje przemyślenia i tematy spoza gry.","Chcesz sprzedać coś wartościowego? Dołącz do grupy Faumonia – Gracze na Facebooku i znajdź zainteresowanych kupców! #https://www.facebook.com/groups/faumoniaonline","Nie możesz znaleźć kupca w grze? Spróbuj w naszej grupie na Facebooku – Faumonia – Gracze. Handel jeszcze nigdy nie był tak prosty! #https://www.facebook.com/groups/faumoniaonline","Nie wiesz, które potwory musisz pokonać w zadaniach odnawialnych? Odwiedź naszą stronę, aby uzyskać szczegółowe informacje o celach i nagrodach. #https://faumonia.pl/zadania-zadania-odnawialne-na-pokonanie-potworow/","Śledź #ogłoszenia na Discordzie, aby zawsze być na bieżąco z nowościami w Faumonii! Informacje o eventach, aktualizacjach i ogłoszeniach czekają na Ciebie.","Chcesz utworzyć własny czat z przyjaciółmi? Przejdź do zakładki #Grupa w lewym dolnym rogu i załóż drużynę! Zaproś znajomych komendą #/group #invite #nazwagracza.","Masz pomysły i chcesz pomóc w rozwoju Faumonii? Poszukujemy zmotywowanych wojowników do współpracy – skontaktuj się z administratorem!","Chcesz zdobyć doświadczenie i nagrody, pomagając mieszkańcom Semos? Odwiedź naszą stronę, aby poznać listę zadań dostępnych w mieście. #https://faumonia.pl/zadania-miasto-semos/","Masz grupę przyjaciół i chcesz utworzyć wspólny czat? Wejdź w zakładkę #Grupa w lewym dolnym rogu, następnie naciśnij środkową ikonkę i załóż nową grupę. Aby zaprosić kolegę wpisz komendę #/group #invite #nazwagracza.","Twórzmy razem dobrą atmosferę w grze - Nie przeszkadzaj innym w grze. Za natarczywe przeszkadzanie innym zostaniesz ukarany więzieniem nawet na 8 godzin!","Zastanawiasz się gdzie expić? Sprawdź zakładkę Expowiska na naszej stronie internetowej i ruszaj do walki! #https://faumonia.pl/expowiska/","Zasady chatu globalnego są proste: brak wulgaryzmów. Natarczywe przeklinanie to prosta droga do wyciszenia aż na tydzień!","Marzysz o zdobyciu najpotężniejszych przedmiotów w Faumonii? Sprawdź listę zadań, które Ci to umożliwią! Szczegóły na stronie: #https://faumonia.pl/zadania-na-najsilniejsze-przedmioty-w-grze/","Chcesz więcej wyzwań? Zakładka Zadania na naszej stronie to miejsce, gdzie znajdziesz misje, które warto podjąć! #https://faumonia.pl/zadania/","Faumonia teraz na Discordzie! Rozmawiaj z innymi wojownikami, sprzedawaj przedmioty, wymieniaj się poradami i śledź najnowsze ogłoszenia. Znajdziesz tam wszystko, czego potrzebujesz, by cieszyć się grą jeszcze bardziej!","Fado, Kirdneh i Kalavan skrywają niejedną tajemnicę. Poznaj zadania, które możesz tam wykonać – wszystko znajdziesz na stronie! #https://faumonia.pl/zadania-w-poludniowych-miastach-fado-kirdneh-kalavan/","Plagi, konkursy i wydarzenia – śledź Faumonię na Facebooku i Instagramie, aby nie przegapić nic ważnego! #https://www.facebook.com/faumoniaonline #https://www.instagram.com/faumoniaonline/","Gdzie znaleźć potwora, którego szukasz? Starkad, który przechadza się po banku Semos, zna odpowiedź na twoje pytanie.","Czy obserwujesz nas już na Facebooku i Instagramie? Zaobserwuj nas i otrzymuj ekspresowe powiadomienia o plagach i konkursach! #https://www.facebook.com/faumoniaonline #https://www.instagram.com/faumoniaonline/","Atlas map na stronie Faumonii to idealne miejsce, aby poznać teren – sprawdź już teraz! #https://faumonia.pl/atlas-map/","Potrzebujesz wskazówki lub nie wiesz, co robić? Zadaj swoje pytanie na kanale #pytania na Discordzie Faumonii i znajdź odpowiedź!","Plagi, wskazówki, zabawne momenty i więcej! Znajdziesz to wszystko na naszym TikToku – zobacz sam! #https://www.tiktok.com/@faumonia","Rozwijasz swoje umiejętności? Pilnuj postaci podczas treningu – na arenie obowiązuje walka na śmierć i życie, a porażka oznacza stratę ekwipunku i cennych skilli!","Śledź #ogłoszenia na Discordzie, aby zawsze być na bieżąco z nowościami w Faumonii! Informacje o eventach, aktualizacjach i ogłoszeniach czekają na Ciebie.","Ados skrywa niejedną tajemnicę i trudne zadania. Chcesz dowiedzieć się, co na Ciebie czeka? Sprawdź szczegóły na stronie! #https://faumonia.pl/zadania-stolica-ados/","Twoja wiedza i zaangażowanie mogą pomóc Faumonii rosnąć! Jeśli chcesz wspierać rozwój gry, zgłoś się do administratora.","Nie wiesz, które potwory musisz pokonać w zadaniach odnawialnych? Odwiedź naszą stronę, aby uzyskać szczegółowe informacje o celach i nagrodach. #https://faumonia.pl/zadania-zadania-odnawialne-na-pokonanie-potworow/","Chcesz sprzedać coś wartościowego? Dołącz do grupy Faumonia – Gracze na Facebooku i znajdź zainteresowanych kupców! #https://www.facebook.com/groups/faumoniaonline"								
							};
	  new ShoutBehaviour(buildSemosTownhallAreaFaumonia(zone), text, 30);
	}

	/**
	 * A Faumonia of three cadets. He has an information giving role.
	 * @param zone zone to be configured with this npc
	 * @return the built NPC
	 */
	private SpeakerNPC buildSemosTownhallAreaFaumonia(final StendhalRPZone zone) {
		// We create an NPC
		final SpeakerNPC npc = new SpeakerNPC("Faumonia Ogłoszenia") {

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Och cześć. Rozmawialiśmy o przerwie. Moi trzej kadeci dostali właśnie nagrodę od Mayora za pomoc w obronie Semos.");
				addJob("Opiekuję się tymi trzema kadetami. Potrzebują wiele wskazówek. Muszę wracać. Posłuchaj ich może się czegoś nowego nauczysz!");
				addHelp("Mogę coś ci poradzić w kwestii broni ( #weapon ).");
				addQuest("Mogę ci doradzić w sprawie Twojej broni ( #weapon ).");
				addOffer("Mogę opowiedzieć o Twojej broni ( #weapon ) jeżeli mogę.");
				addGoodbye("Nie zapomnij posłuchać nauk dla moich trzech kadetów. Mogą się okazać pomocne!");
				add(ConversationStates.ATTENDING, "weapon", null, ConversationStates.ATTENDING,
				        null, new ChatAction() {

					        public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					        	final Item weapon = player.getWeapon();
					        	if (weapon != null) {
					        		String comment;
					        		// this is, loosely, the formula used for damage of a weapon (not taking level into account here)
					        		final float damage = (weapon.getAttack() + 1) / weapon.getAttackRate();
					        		if (weapon.getName().endsWith(" hand sword")) {
					        			// this is a special case, we deal with explicitly
					        			comment = "Widzę, że używasz dwóch mieczy. Zadają sporo strat, ale nie możesz wtedy nosić tarczy. Będzie ciężko z obroną, gdy ktoś cię zaatakuje.";
					        		} else if (damage >= 5) {
					        			comment = "Oto " + weapon.getName() + " to potężna broń. Jak na wagę to zadaje dobre straty.";
					        			if (weapon.getAttackRate() < 3) {
					        				comment += " Pomimo wagi jest użyteczna. Niski atak nie pomaga w starciu z silnymi potworami. Czasami cięższa będzie lepsza niż ta broń.";
					        			} else if (weapon.getAttackRate() > 6) {
					        				comment +=  " Powinna być użyteczna na silniejsze potwory. Pamiętaj, że coś słabego, a szybkiego może wystarczyć przeciwko potworom z niskim poziomem.";
					        			}
					        		} else {
					        			comment = "Nie sądzisz, że Twój " + weapon.getName() + " zadaje małe straty? Powinieneś poszukać czegoś z lepszym atakiem w stosunku do wagi.";
					        			if (weapon.getAttackRate() < 3) {
					        				comment += " W końcu możesz uderzać szybko. Może być dobre przeciwko słabszym potworom niż ty.";
					        			}
						       		}
					        		// simple damage doesn't take into account lifesteal. this is a decision the player must make, so inform them about the stats
					        		if (weapon.has("lifesteal")) {
					        			double lifesteal = weapon.getDouble("lifesteal");
					        			if (lifesteal > 0) {
					        				comment += " Pozytywne wysysanie życia " + lifesteal + " podnosi Twoje zdrowie, gdy go używasz.";
					        			} else {
					        				comment += " Negatywne wysysanie życia " + lifesteal + " odbije się na Twoim zdrowiu, gdy będziesz tego używał.";
					        			}
					        		}
					        		raiser.say(comment);
					        	} else {
					        		// player didn't have a weapon, as getWeapon returned null.
					        		raiser.say("Och, nie mogę nic opowiedzieć o Twojej broni o ile jakieś nie będziesz używał. To niezbyt mądre w dzisiejszych czasach!");
					        	}
							} 
					    }
				);
			}
		};
		npc.setLevel(150);
		npc.setDescription("Oto Lieutenant Drilenun, który rozmawia ze swoimi trzema kadetami.");
		npc.setEntityClass("bossmannpc");
		npc.setPosition(23, 15);
		npc.initHP(100);
		zone.add(npc);
		
		return npc;
	}
}
