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
import java.util.List;
import java.util.Random;

public class FaumoniaNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {

    final List<String> texts = List.of(
	
    "Zacząłeś jakieś zadanie ale nie wiesz co robić dalej? Wejdź w #Menu w prawym górnym rogu ekranu, " +
    "następnie naciśnij na #Dziennik #Zadań i odszukaj swoje zadanie.",
	
	"Zadanie na liście, ale co dalej? Sprawdź #Dziennik #Zadań w #Menu – znajdziesz tam wszystkie potrzebne wskazówki.",
	
	"Zacząłeś zadanie, ale zgubiłeś trop? Otwórz #Dziennik #Zadań przez #Menu i sprawdź swoje aktywne misje!",
    
    "Zastanawiasz się gdzie expić? Sprawdź zakładkę Expowiska na naszej stronie internetowej " +
    "i ruszaj do walki! #https://faumonia.pl/expowiska/",
	
	"Nie wiesz, gdzie zdobywać doświadczenie? Odwiedź zakładkę #Expowiska na stronie i znajdź idealne miejsce do expienia! #https://faumonia.pl/expowiska/",
	
	"Najlepsze miejsca do expienia czekają na Ciebie! Sprawdź zakładkę #Expowiska na naszej stronie i wyrusz na podbój Faumonii! #https://faumonia.pl/expowiska/",

    "Twórzmy razem dobrą atmosferę w grze - Nie przeszkadzaj innym w grze. Za natarczywe przeszkadzanie innym " +
    "zostaniesz ukarany więzieniem nawet na 8 godzin!",
	
	"Szanujmy się nawzajem! Natarczywe przeszkadzanie innym wojownikom może skończyć się więzieniem nawet na 8 godzin.",
	
	"Nie psuj zabawy innym – za przeszkadzanie w grze grozi więzienie! Pamiętaj, Faumonia to miejsce dla wszystkich.",
	
	"Dbajmy o dobrą atmosferę – przeszkadzanie innym wojownikom to szybka droga do więzienia. Pokaż klasę!",
	
	"Plagi, wskazówki, zabawne momenty i więcej! Znajdziesz to wszystko na naszym TikToku – zobacz sam! #https://www.tiktok.com/@faumonia",

	/**	"Chcesz kupić jakieś przedmioty ale nikt nie chce ich sprzedać? Sprawdź listę sprzedawców w krainie Faumonii " +
    "na naszej stronie! #https://faumonia.pl/sklepy/", */

    "Czy obserwujesz już nas na TikToku? Obserwuj nasz profil i bądź na bieżąco! #https://www.tiktok.com/@faumonia",

    "Potrzebujesz pomocy? Sprawdź poradniki na naszej stronie i poznaj lepiej krainę Faumonii! " +
    "#https://faumonia.pl/poradniki/",

    "Pamiętaj, że najnowsze wiadomości z krainy Faumonii znajdziesz na naszej stronie internetowej. " +
    "#https://faumonia.pl/aktualnosci-ze-swiata-faumonii/",

    "Masz jakieś pytanie lub chcesz coś sprzedać? Skorzystaj z chatu globalnego wpisując #/k #twoja #wiadomość",

    "Trenujesz swoje umiejętności skilla, ataku i obrony? Pilnuj swojej postaci w przeciwnym razie ktoś " +
    "może Cie zabić i zabrać Twój ekwipunek. Na arenie treningowej dozwolona jest walka na śmierć i życie!",

    /** "Chcesz sprawdzić listę przedmiotów dostępnych w naszej krainie? Sprawdź naszą stronę internetową! " +
    "#https://faumonia.pl/przedmioty/", */

    "Czy obserwujesz nas już na Facebooku i Instagramie? Zaobserwuj nas i otrzymuj ekspresowe powiadomienia " +
    "o plagach i konkursach! #https://www.facebook.com/faumoniaonline #https://www.instagram.com/faumoniaonline/",

    "Szukasz ciekawych zadań? Sprawdź zakładkę Zadania na naszej stronie internetowej i odkrywaj Faumonie! " +
    "#https://faumonia.pl/zadania/",

    "Szukamy chętnych wojowników do długofalowej pomocy przy rozwoju gry. Jesteś zmotywowany i chcesz pomóc " +
    "długoterminowo? Skontaktuj się z administratorem #FaumoniaOnline",

    /** "Nie wiesz gdzie znaleźć konkretnego NPC? Skorzystaj z listy NPC na naszej stronie internetowej! " +
    "#https://faumonia.pl/npc/", */

    "Twórzmy razem dobrą atmosferę w grze - Nie przeklinaj na chacie globalnym. Za natarczywe przeklinanie na " +
    "chacie globalnym zostaniesz ukarany wyciszeniem nawet na 7 dni!",

    "Zastanawiasz się gdzie znajdziesz konkretnego potwora? Odwiedź bank Semos i porozmawiaj na ten temat " +
    "ze spacerującym tam #Starkadem",

    "Nie wiesz gdzie znajdziesz konkretną mapę? Sprawdź atlas map na naszej stronie internetowej! " +
    "#https://faumonia.pl/atlas-map/",

    /** "Czy wiesz, że na naszej stronie internetowej znajdziesz informacje na temat wszystkich potworów w naszej " +
    "krainie? #https://faumonia.pl/bestariusz/", */

    /** "Chcesz sprzedać swoje przedmioty ale nikt nie chce ich kupić? Być może kupi je od Ciebie któryś z kupców " +
    "Faumonii. Sprawdź na naszej stronie internetowej! #https://faumonia.pl/sklepy/", */

    "Chcesz podnieść swoje umiejętności ataku i obrony i łatwiej pokonywać wrogów? Wybierz się na arenę treningową " +
    "i poćwicz swoje umiejętności w walce z innymi graczami. Uważaj! Na arenie treningowej dozwolona jest walka " +
    "na śmierć i życie!",

    "Chcesz sprzedać swoje cenne przedmioty ale nie możesz znaleźć kupca? Dołącz do naszej specjalnej grupy na " +
    "Facebooku: #Faumonia #- #Gracze i sprzedaj go innym graczom! #https://www.facebook.com/groups/faumoniaonline",

    "Widziałeś już filmy z naszej krainy? Koniecznie zaobserwuj nas na YouTubie! #https://www.youtube.com/@faumonia",

    "Masz grupę przyjaciół i chcesz utworzyć wspólny czat? Wejdź w zakładkę #Grupa w lewym dolnym rogu, " +
    "następnie naciśnij środkową ikonkę i załóż nową grupę. Aby zaprosić kolege wpisz komendę #/group #invite " +
    "#nazwagracza Teraz możesz rozmawiać ze swoimi znajomymi używając grupowego chatu #/p #wiadomość",

    "Ktoś Cie obraża? Wpisz komende #/ignore #nazwagracza a już nigdy nie zobaczysz jego wiadomości."
	
	"Nowy w Faumonii? A może potrzebujesz wskazówek? Zajrzyj do naszych poradników i poznaj świat gry! #https://faumonia.pl/poradniki/",
	
	"Zgubiłeś się w świecie Faumonii? Poradniki na naszej stronie wskażą Ci drogę! #https://faumonia.pl/poradniki/",
	
	"Nie przegap nowości z Faumonii! Najnowsze wiadomości znajdziesz tutaj: #https://faumonia.pl/aktualnosci-ze-swiata-faumonii/",
	
	"Zajrzyj na stronę Faumonii, aby być na bieżąco z wydarzeniami i nowościami! #https://faumonia.pl",
	
	"Ćwicz swoje umiejętności na arenie treningowej, ale bądź ostrożny – inni wojownicy mogą zaatakować cię w każdej chwili!",
	
	"Rozwijasz swoje umiejętności? Pilnuj postaci podczas treningu – na arenie obowiązuje walka na śmierć i życie, a porażka oznacza stratę ekwipunku i cennych skilli!",
	
	"Plagi, konkursy i wydarzenia – śledź Faumonię na Facebooku i Instagramie, aby nie przegapić nic ważnego! #https://www.facebook.com/faumoniaonline #https://www.instagram.com/faumoniaonline/",
	
	"Obserwujesz nas na Facebooku i Instagramie? Jeśli nie, to czas nadrobić zaległości – najświeższe informacje czekają na Ciebie! #https://www.facebook.com/faumoniaonline #https://www.instagram.com/faumoniaonline/",
	
	"Szukasz nowych przygód? Zajrzyj do zakładki Zadania na stronie Faumonii i sprawdź, jakie wyzwania czekają! #https://faumonia.pl/zadania/",
	
	"Chcesz więcej wyzwań? Zakładka Zadania na naszej stronie to miejsce, gdzie znajdziesz misje, które warto podjąć! #https://faumonia.pl/zadania/",
	
	"Poznawaj Faumonię i podejmuj ciekawe zadania – wszystkie znajdziesz w zakładce Zadania na naszej stronie! #https://faumonia.pl/zadania/",
	
	"Twoja wiedza i zaangażowanie mogą pomóc Faumonii rosnąć! Jeśli chcesz wspierać rozwój gry, zgłoś się do administratora.",
	
	"Masz pomysły i chcesz pomóc w rozwoju Faumonii? Poszukujemy zmotywowanych wojowników do współpracy – skontaktuj się z administratorem!",
	
	"Pamiętaj, że chat globalny to miejsce do rozmów, a nie przekleństw – łamanie zasad grozi wyciszeniem nawet na 7 dni!",
	
	"Dbaj o atmosferę w Faumonii – nie używaj wulgaryzmów na chacie globalnym. Przekraczanie granic to pewna kara w postaci wyciszenia.",
	
	"Zasady chatu globalnego są proste: brak wulgaryzmów. Natarczywe przeklinanie to prosta droga do wyciszenia aż na tydzień!",
	
	"Nie możesz znaleźć konkretnego potwora? Starkad w banku Semos zna ich wszystkie kryjówki – odwiedź go i zapytaj!",
	
	"Gdzie znaleźć potwora, którego szukasz? Starkad, który przechadza się po banku Semos, zna odpowiedź na twoje pytanie.",
	
	"Szukasz bestii, która ci umyka? Odwiedź Starkada w banku Semos – wie wszystko o mieszkańcach Faumonii.",
	
	"Atlas map na stronie Faumonii to idealne miejsce, aby poznać teren – sprawdź już teraz! #https://faumonia.pl/atlas-map/",
	
	"Nie wiesz, gdzie znaleźć odpowiednią mapę? Zajrzyj do atlasu map na naszej stronie i odkrywaj Faumonię! #https://faumonia.pl/atlas-map/",
	
	"Szukasz kupca na swoje przedmioty? Nasza grupa na Facebooku to idealne miejsce, by znaleźć nabywców! #https://www.facebook.com/groups/faumoniaonline",
	
	"Chcesz sprzedać coś wartościowego? Dołącz do grupy Faumonia – Gracze na Facebooku i znajdź zainteresowanych kupców! #https://www.facebook.com/groups/faumoniaonline",
	
	"Nie możesz znaleźć kupca w grze? Spróbuj w naszej grupie na Facebooku – Faumonia – Gracze. Handel jeszcze nigdy nie był tak prosty! #https://www.facebook.com/groups/faumoniaonline",
	
	"Faumonia na YouTube to miejsce pełne epickich bitew i poradników! Sprawdź nasz kanał: #https://www.youtube.com/@faumonia",
	
	"Chcesz utworzyć własny czat z przyjaciółmi? Przejdź do zakładki #Grupa w lewym dolnym rogu i załóż drużynę! Zaproś znajomych komendą #/group #invite #nazwagracza.",
	
	"Grupowy chat to idealne miejsce do planowania działań z przyjaciółmi. Załóż drużynę w zakładce #Grupa i komunikuj się, używając #/p #wiadomość.",
	
	"Nie chcesz widzieć wiadomości od kogoś, kto Ci przeszkadza? Wpisz #/ignore #nazwagracza i ciesz się spokojem!",
	
	"Ktoś Cię denerwuje lub obraża? Użyj #/ignore #nazwagracza, aby ukryć wszystkie jego wiadomości.",
	
	"Dołącz do naszej społeczności na Discordzie i bądź na bieżąco z najnowszymi wydarzeniami w Faumonii! Znajdziesz tam miejsce na pytania, handel, fanarty i wiele więcej. Kliknij w ikonę Discorda na naszej stronie i dołącz już teraz!",
	
	"Chcesz na bieżąco rozmawiać z innymi wojownikami? Dołącz do naszego Discorda i bądź częścią rosnącej społeczności Faumonii. Znajdziesz tu kanały do wymiany przedmiotów, pytań, porad oraz luźnych rozmów!",
	
	"Faumonia teraz na Discordzie! Rozmawiaj z innymi wojownikami, sprzedawaj przedmioty, wymieniaj się poradami i śledź najnowsze ogłoszenia. Znajdziesz tam wszystko, czego potrzebujesz, by cieszyć się grą jeszcze bardziej!"
	
	"Śledź #ogłoszenia na Discordzie, aby zawsze być na bieżąco z nowościami w Faumonii! Informacje o eventach, aktualizacjach i ogłoszeniach czekają na Ciebie.",
	
	"Masz ochotę na luźną rozmowę? Na Discordzie Faumonii, w kanale #ogólny, spotkasz wojowników z całej krainy, którzy chętnie podzielą się swoimi przygodami.",
	
	"Potrzebujesz wskazówki lub nie wiesz, co robić? Zadaj swoje pytanie na kanale #pytania na Discordzie Faumonii i znajdź odpowiedź!",
	
	"Na Discordzie Faumonii, w kanale #pochwal-sie, możesz podzielić się swoimi osiągnięciami i zdobyczami. Pokaż, co udało Ci się osiągnąć!",
	
	"Na Discordzie Faumonii znajdziesz miejsce na każdą rozmowę – kanał #offtop czeka na Twoje przemyślenia i tematy spoza gry.",
	
	"Tworzysz memy albo fanarty? Kanał #memy-i-fanarty na Discordzie Faumonii to idealne miejsce, by podzielić się swoją twórczością i zyskać uznanie społeczności!",
	
	"Faumonia is home to warriors of many languages. Use #english-only on our Discord to communicate with everyone in one universal language!",
	
	"Mistrzowie Gry to seria wyzwań o rosnącym poziomie trudności, która przeprowadzi Cie przez całą rozgrywkę w krainie Faumonii. Szczegółowe informacje znajdziesz na stronie: #https://faumonia.pl/zadania-mistrzowie-gry/"
	
	"Chcesz zdobyć doświadczenie i nagrody, pomagając mieszkańcom Semos? Odwiedź naszą stronę, aby poznać listę zadań dostępnych w mieście. #https://faumonia.pl/zadania-miasto-semos/",
	
	"Nie wiesz, które potwory musisz pokonać w zadaniach odnawialnych? Odwiedź naszą stronę, aby uzyskać szczegółowe informacje o celach i nagrodach. https://faumonia.pl/zadania-zadania-odnawialne-na-pokonanie-potworow/",
	
	"Marzysz o zdobyciu najpotężniejszych przedmiotów w Faumonii? Sprawdź listę zadań, które Ci to umożliwią! Szczegóły na stronie: #https://faumonia.pl/zadania-na-najsilniejsze-przedmioty-w-grze/",
	
	"Ados skrywa niejedną tajemnicę i trudne zadania. Chcesz dowiedzieć się, co na Ciebie czeka? Sprawdź szczegóły na stronie! #https://faumonia.pl/zadania-stolica-ados/",
	
	"Fado, Kirdneh i Kalavan skrywają niejedną tajemnicę. Poznaj zadania, które możesz tam wykonać – wszystko znajdziesz na stronie! #https://faumonia.pl/zadania-w-poludniowych-miastach-fado-kirdneh-kalavan/"
	
	
	
	
	
  );

    Random random = new Random();

    String randomText = texts.get(random.nextInt(texts.size()));

    zone.setMessage(randomText);
}




	  new ShoutBehaviour(buildSemosTownhallAreaFaumonia(zone), text, 45);
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
