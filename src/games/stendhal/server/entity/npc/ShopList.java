/* $Id: ShopList.java,v 1.97 2013/07/14 07:15:10 antumdeluge Exp $ */
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
package games.stendhal.server.entity.npc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Singleton class that contains inventory and prices of NPC stores.
 */
public final class ShopList {

	static {
		final ShopList shops = ShopList.get();
		
		shops.add("juhas", "zwój semos", 400);
		shops.add("juhas", "zwój fado", 400);
		shops.add("juhas", "zwój ados", 800);
		shops.add("juhas", "zwój nalwor", 400);
		shops.add("juhas", "zwój kirdneh", 400);
		shops.add("juhas", "bilet turystyczny", 100000);

		shops.add("buycheng", "bursztyn", 20);
		shops.add("buycheng", "wielka perła", 1500);
		shops.add("buycheng", "bryłka mithrilu", 10);
		//bronek kupuje:
		shops.add("buydragonitems", "pazury wilcze", 5);
		shops.add("buydragonitems", "niedźwiedzie pazury", 8);
		shops.add("buydragonitems", "pazury tygrysie", 100);
		shops.add("buydragonitems", "pazur zielonego smoka", 5000);
		shops.add("buydragonitems", "pazur niebieskiego smoka", 5000);
		shops.add("buydragonitems", "pazur czerwonego smoka", 5000);
		shops.add("buydragonitems", "pazur czarnego smoka", 10000);
		shops.add("buydragonitems", "pazur złotego smoka", 15000);


		shops.add("buykacper", "śnieżka", 5);
		shops.add("buykacper", "kamienie", 6);
		shops.add("buykacper", "bryła lodu", 8);
		//rzeznik kupuje
		shops.add("buymieso", "mięso", 15); 
		shops.add("buymieso", "szynka", 25);
		shops.add("buymieso", "udko", 12);
		shops.add("buymieso", "stek", 35);
		//waldek kupuje
		shops.add("buyowoce", "wisienka", 8);
		shops.add("buyowoce", "jabłko", 5);
		shops.add("buyowoce", "jabłko niezgody", 15);
		shops.add("buyowoce", "poziomka", 5);
		shops.add("buyowoce", "truskawka", 8);
		shops.add("buyowoce", "gruszka", 6);
		shops.add("buyowoce", "kokos", 7);
		shops.add("buyowoce", "ananas", 8);
		

		shops.add("buyrareitems", "kierpce", 100);
		shops.add("buyrareitems", "chusta góralska", 200);
		shops.add("buyrareitems", "cuha góralska", 200);
		shops.add("buyrareitems", "góralska spódnica", 200);
		shops.add("buyrareitems", "góralski gorset", 300);
		shops.add("buyrareitems", "góralski kapelusz", 300);
		shops.add("buyrareitems", "portki bukowe", 300);
		shops.add("buyrareitems", "polska tarcza lekka", 500);
		shops.add("buyrareitems", "polska tarcza drewniana", 500);
		shops.add("buyrareitems", "ciupaga", 1000);
		shops.add("buyrareitems", "korale", 1000);
		shops.add("buyrareitems", "pas zbójecki", 1000);
		shops.add("buyrareitems", "polska tarcza kolcza", 1000);
		shops.add("buyrareitems", "polska płytowa tarcza", 2000);
		shops.add("buyrareitems", "spinka", 2000);
		shops.add("buyrareitems", "polska tarcza ciężka", 1500);
		shops.add("buyrareitems", "złota ciupaga", 5000);
		shops.add("buyrareitems", "szczerbiec", 1000000);

		shops.add("buyrybak", "dorsz", 5);
		shops.add("buyrybak", "palia alpejska", 7);
		shops.add("buyrybak", "płotka", 7);
		shops.add("buyrybak", "makrela", 8);
		shops.add("buyrybak", "okoń", 8);
		shops.add("buyrybak", "pokolec", 9);
		shops.add("buyrybak", "pstrąg", 9);
		shops.add("buyrybak", "błazenek", 10);
		
		shops.add("buyryby", "dorsz", 6);
		shops.add("buyryby", "palia alpejska", 7);
		shops.add("buyryby", "płotka", 7);
		shops.add("buyryby", "makrela", 8);
		shops.add("buyryby", "okoń", 9);
		shops.add("buyryby", "pokolec", 9);
		shops.add("buyryby", "pstrąg", 9);
		shops.add("buyryby", "błazenek", 11);
		//bogus kupuje :
		shops.add("buyskin", "piórko", 3);
		shops.add("buyskin", "skóra lwa", 2000);
		shops.add("buyskin", "skóra tygrysa", 2000);
		shops.add("buyskin", "skóra xenocium", 500);
		shops.add("buyskin", "skóra zielonego smoka", 2600);
		shops.add("buyskin", "skóra czerwonego smoka", 2600);
		shops.add("buyskin", "skóra niebieskiego smoka", 2600);
		shops.add("buyskin", "skóra złotego smoka", 2700);
		shops.add("buyskin", "skóra czarnego smoka", 3300);

		shops.add("buywarzywa", "marchew", 2);
		shops.add("buywarzywa", "sałata", 2);
		shops.add("buywarzywa", "pieczarka", 4);
		shops.add("buywarzywa", "opieńka miodowa", 6);
		shops.add("buywarzywa", "pomidor", 5);
		shops.add("buywarzywa", "kapusta", 6);
		shops.add("buywarzywa", "borowik", 6);
		shops.add("buywarzywa", "szpinak", 6);
		shops.add("buywarzywa", "brokuł", 7);
		shops.add("buywarzywa", "por", 7);
		shops.add("buywarzywa", "kalafior", 8);
		shops.add("buywarzywa", "cebula", 8);
		shops.add("buywarzywa", "cukinia", 10);
		//mysliwy kupuje
		shops.add("buyzeby", "kieł wilczy", 10);
		shops.add("buyzeby", "kieł niedźwiedzi", 15);
		shops.add("buyzeby", "dziób ptaka", 200);
		shops.add("buyzeby", "ząb potwora", 400);
		shops.add("buyzeby", "kieł smoka", 500);
		shops.add("buyzeby", "kieł złotej kostuchy", 5000);
		//zielarka kupuje
		shops.add("buyziola", "arandula", 12);
		shops.add("buyziola", "kokuda", 250);	
		shops.add("buyziola", "kekik", 28);
		shops.add("buyziola", "sclaria", 28);

		shops.add("mecz", "bilet na mecz", 250);
		shops.add("mecz", "piłka", 500);

		shops.add("sellkopalnia", "kilof", 150);
		shops.add("sellkopalnia", "łopata", 200);
		shops.add("sellkopalnia", "lina", 150);
		//bogus sprzedaje
		shops.add("sellskin", "buteleczka", 5);
		shops.add("sellskin", "butelka", 7);
		shops.add("sellskin", "krótki miecz", 500);
		shops.add("sellskin", "topór", 800);
		shops.add("sellskin", "kosa", 2000);

		shops.add("sellwarzywairek", "marchew", 5);
		shops.add("sellwarzywairek", "sałata", 10);
		shops.add("sellwarzywairek", "kapusta", 25);
		shops.add("sellwarzywairek", "pomidor", 20);
		shops.add("sellwarzywairek", "cukinia", 30);
		shops.add("sellwarzywairek", "borowik", 35);
		//Jagna sprzedaje:
		shops.add("urodziny", "sok z chmielu", 8); 
		shops.add("urodziny", "napój z winogron", 10); 
		shops.add("urodziny", "napój z oliwką", 50); 
		shops.add("urodziny", "shake waniliowy", 100); 
		shops.add("urodziny", "shake czekoladowy", 100); 
		shops.add("urodziny", "mięso", 20); 
		shops.add("urodziny", "szynka", 30); 
		shops.add("urodziny", "hotdog", 120); 
		shops.add("urodziny", "hotdog z serem", 140); 
		shops.add("urodziny", "kanapka z tuńczykiem", 340); 
		shops.add("urodziny", "kanapka", 310); 
		shops.add("urodziny", "tabliczka czekolady", 80); 
		shops.add("urodziny", "lukrecja", 80); 
    
		shops.add("food&drinks", "sok z chmielu", 10);
		shops.add("food&drinks", "napój z winogron", 15);
		shops.add("food&drinks", "flasza", 5);
		shops.add("food&drinks", "ser", 20);
		shops.add("food&drinks", "jabłko", 20);
		shops.add("food&drinks", "marchew", 10);
		shops.add("food&drinks", "mięso", 40);
		shops.add("food&drinks", "szynka", 80);
		shops.add("food&drinks", "buteleczka", 8);
		shops.add("food&drinks", "butelka", 10);

		shops.add("adosfoodseller", "jabłko", 50);
		shops.add("adosfoodseller", "marchew", 50);
		shops.add("adosfoodseller", "pusty worek", 10);
		shops.add("adosfoodseller", "młynek do cukru", 800);
		//SiandraNPC (ados) kupuje:
		shops.add("buyfood", "ser", 5);
		shops.add("buyfood", "mięso", 10);
		shops.add("buyfood", "szpinak", 10);
		shops.add("buyfood", "szynka", 20);
		shops.add("buyfood", "mąka", 25);
		shops.add("buyfood", "borowik", 30);

		shops.add("healing", "antidotum", 25);
		shops.add("healing", "mały eliksir", 50);
		shops.add("healing", "eliksir", 100);
		shops.add("healing", "duży eliksir", 250);
		//SarzinaNPC i Kendra MattoriNPC sprzedaje:
		shops.add("superhealing", "antidotum", 20);
		shops.add("superhealing", "mocne antidotum", 50);
		shops.add("superhealing", "eliksir", 100);
		shops.add("superhealing", "duży eliksir", 250);
		//HaizenNPC i JynathNPC sprzedaje:
		shops.add("scrolls", "zwój przywołania", 100);
		//Xhiphin ZohosNPC i OrchiwaldNPC sprzedaje:
		//HazelNPC sprzedaje:
		//Erodel BmudNPC w magic city sprzedaje:
		shops.add("allscrolls", "zwój przywołania", 100);

		shops.add("sellstuff", "mieczyk", 30);
		shops.add("sellstuff", "maczuga", 20);
		shops.add("sellstuff", "sztylecik", 50);
		shops.add("sellstuff", "drewniana tarcza", 40);
		shops.add("sellstuff", "koszula", 10);
		shops.add("sellstuff", "skórzany hełm", 15);
		shops.add("sellstuff", "peleryna", 10);
		shops.add("sellstuff", "skórzane spodnie", 15);
		shops.add("sellstuff", "lwia tarcza", 100);
		shops.add("sellstuff", "tarcza z czaszką", 250);
		shops.add("sellstuff", "zbroja łuskowa", 100);
		shops.add("sellstuff", "zbroja płytowa", 300);
		shops.add("sellstuff", "hełm wikingów", 150);
		shops.add("sellstuff", "misiurka", 300);
		//HagnurkNPC sprzedaje:
		shops.add("sellbetterstuff1", "zbroja lazurowa", 3000);
		shops.add("sellbetterstuff1", "buty lazurowe", 2000);
		shops.add("sellbetterstuff1", "lazurowy hełm", 2500);
		shops.add("sellbetterstuff1", "spodnie lazurowe", 2000);
		shops.add("sellbetterstuff1", "lazurowa tarcza", 4000);
		//GulimoNPC sprzedaje:
		shops.add("sellbetterstuff2", "zbroja cieni", 3400);
		shops.add("sellbetterstuff2", "buty cieni", 2800);
		shops.add("sellbetterstuff2", "płaszcz cieni", 3200);
		shops.add("sellbetterstuff2", "hełm cieni", 3000);
		shops.add("sellbetterstuff2", "spodnie cieni", 2800);
		shops.add("sellbetterstuff2", "tarcza cieni", 3400);

		shops.add("sellrangedstuff", "drewniany łuk", 300);
		shops.add("sellrangedstuff", "strzała", 5);

		shops.add("buystuff", "krótki miecz", 50);
		shops.add("buystuff", "miecz", 100);
		shops.add("buystuff", "tarcza ćwiekowa", 40);
		shops.add("buystuff", "zbroja ćwiekowa", 40);
		shops.add("buystuff", "hełm nabijany ćwiekami", 40);
		shops.add("buystuff", "spodnie nabijane ćwiekami", 40);
		shops.add("buystuff", "kolczuga", 40);
		shops.add("buystuff", "hełm kolczy", 40);
		shops.add("buystuff", "spodnie kolcze", 40);
		shops.add("buystuff", "karmazynowy hełm", 200);
		shops.add("buystuff", "spodnie karmazynowe", 200);
		shops.add("buystuff", "buty karmazynowe", 200);
		shops.add("buystuff", "płaszcz karmazynowy", 200);
		shops.add("buystuff", "zbroja karmazynowa", 300);
		shops.add("buystuff", "karmazynowa tarcza", 300);
		
		shops.add("selltools", "toporek", 80);
		shops.add("selltools", "topór jednoręczny", 100);
		shops.add("selltools", "topór", 85);
		shops.add("selltools", "berdysz", 500);
		shops.add("selltools", "katana", 1000);
		shops.add("selltools", "topór oburęczny", 300);
		shops.add("selltools", "nóż z kości", 10000);
		shops.add("selltools", "miecz", 200);
		shops.add("selltools", "krótki miecz", 100);
		shops.add("selltools", "maczeta", 300);
		shops.add("selltools", "sztylecik", 75);
		shops.add("selltools", "kilof", 1000);
		// enable these if you need them for a quest or something
		// shops.add("selltools", "pick", 50);
		// shops.add("selltools", "shovel", 50);
		shops.add("selltools", "pyrlik", 200);
		// used for harvest grain.
		shops.add("selltools", "kosa pordzewiała", 120);
        // for harvesting cane fields
		shops.add("selltools", "sierp", 400);
		shops.add("selltools", "misa do płukania złota", 230);
		//LorettaNPC w podziemiach orril.dwarfmine kupuje:
		shops.add("buyiron", "żelazo", 75);

		shops.add("buygrain", "kłos", 3);
		//OgnirNPC sprzedaje:
		shops.add("sellrings", "pierścień zaręczynowy", 5000);
		// gold  OgnirNPC skupuje:
		shops.add("buyprecious", "żelazo", 100);
		shops.add("buyprecious", "sztabka złota", 200);
		shops.add("buyprecious", "sztabka mithrilu", 400);
		shops.add("buyprecious", "szmaragd", 2000);
		shops.add("buyprecious", "szafir", 2000);
		shops.add("buyprecious", "rubin", 2000);
		shops.add("buyprecious", "obsydian", 4000);
		shops.add("buyprecious", "diament", 20000);

		// rare weapons shop
		shops.add("buyrare", "bułat", 300);
		shops.add("buyrare", "katana", 100);
		shops.add("buyrare", "berdysz", 200);
		shops.add("buyrare", "złoty młot", 300);

		// rare armor shop
		shops.add("buyrare", "kolczuga wzmocniona", 50);
		shops.add("buyrare", "złota kolczuga", 150);
		shops.add("buyrare", "zbroja płytowa", 120);
		shops.add("buyrare", "tarcza płytowa", 140);
		shops.add("buyrare", "lwia tarcza", 100);

		// rare elf weapons buyer  ElodrinNPC (nalwor) kupuje:
		shops.add("elfbuyrare", "topór bojowy", 100);
		shops.add("elfbuyrare", "topór oburęczny", 200);
		shops.add("elfbuyrare", "miecz dwuręczny", 200);
		shops.add("elfbuyrare", "pałasz", 200);
		shops.add("elfbuyrare", "kij", 150);
		shops.add("elfbuyrare", "wzmocniona lwia tarcza", 300);
		shops.add("elfbuyrare", "tarcza królewska", 500);

		// more rare weapons shop (fado)Yorphin BaosNPC kupuje:
		shops.add("buyrare2", "długi łuk", 600);
		shops.add("buyrare2", "klejony łuk", 800);
		shops.add("buyrare2", "kusza", 1200);
		shops.add("buyrare2", "kusza łowcy", 1400);
		shops.add("buyrare2", "lodowa kusza", 12000);
		shops.add("buyrare2", "sztylet orków", 1200);
		shops.add("buyrare2", "miecz demonów", 1500);
		shops.add("buyrare2", "miecz ognisty", 3000);
		shops.add("buyrare2", "miecz lodowy", 8000);
		shops.add("buyrare2", "sztylet mordercy", 8000);
		shops.add("buyrare2", "piekielny sztylet", 8000);
		

		// very rare armor shop (ados)MrothoNPV kupuje:
		shops.add("buyrare3", "złote spodnie", 2400);
		shops.add("buyrare3", "złota zbroja", 3000);
		shops.add("buyrare3", "złota tarcza", 3000);
		shops.add("buyrare3", "złoty hełm", 2800);
		shops.add("buyrare3", "buty złote", 2400);
		shops.add("buyrare3", "złoty płaszcz", 2800);

		// less rare armor shop (kobold city - kobolds drop some of these
		// things)
		shops.add("buystuff2", "skórzana zbroja łuskowa", 65);
		shops.add("buystuff2", "spodnie nabijane ćwiekami", 70);
		shops.add("buystuff2", "buty nabijane ćwiekami", 75);
		shops.add("buystuff2", "buty kolcze", 100);
		shops.add("buystuff2", "tarcza z czaszką", 200);
		shops.add("buystuff2", "hełm wikingów", 100);

		shops.add("sellstuff2", "buty skórzane", 10);
		shops.add("sellstuff2", "hełm nabijany ćwiekami", 60);
		shops.add("sellstuff2", "tarcza ćwiekowa", 80);
		shops.add("sellstuff2", "miecz", 200);

		// cloaks shop  IdaNPC kupuje:
		shops.add("buycloaks", "płaszcz karmazynowy", 200);
		shops.add("buycloaks", "prążkowany płaszcz lazurowy", 400);
		shops.add("buycloaks", "płaszcz orczy", 400);
		shops.add("buycloaks", "płaszcz licha", 1000);
		shops.add("buycloaks", "szmaragdowy płaszcz smoczy", 1400);
		shops.add("buycloaks", "kościany płaszcz smoczy", 1600);
		shops.add("buycloaks", "płaszcz elficki", 1300);
		shops.add("buycloaks", "peleryna elficka", 1400);
		shops.add("buycloaks", "lazurowy płaszcz elficki", 1200);
		shops.add("buycloaks", "lazurowy płaszcz smoczy", 1800);
		shops.add("buycloaks", "złoty płaszcz", 3000);
		shops.add("buycloaks", "płaszcz krasnoludzki", 3000);
		shops.add("buycloaks", "karmazynowy płaszcz smoczy", 2000);
		shops.add("buycloaks", "płaszcz cieni", 3400);
		shops.add("buycloaks", "płaszcz z białego złota", 3300);
		shops.add("buycloaks", "płaszcz ciemności", 3400);
		shops.add("buycloaks", "czarny płaszcz smoczy", 5000);
		shops.add("buycloaks", "płaszcz kamienny", 4000);
		shops.add("buycloaks", "płaszcz pustynny", 4200);
		shops.add("buycloaks", "płaszcz wampirzy", 8000);
		shops.add("buycloaks", "płaszcz chaosu", 4000);
		shops.add("buycloaks", "płaszcz balorughtów", 6000);
		shops.add("buycloaks", "płaszcz mainiocyjski", 5600);
		shops.add("buycloaks", "płaszcz xenocyjski", 5800);
		shops.add("buycloaks", "czarny płaszcz", 40000);

		// boots shop (mithrilbourgh)
		// Note the shop sign is done by hand in
		// games.stendhal.server.maps.mithrilbourgh.stores
		// Because I wanted to split boots and helmets
		// Please if you change anything, change also the sign (by hand)
		shops.add("boots&helm", "buty karmazynowe", 200);
		shops.add("boots&helm", "buty lazurowe", 300);
		shops.add("boots&helm", "krwawe buty", 400);
		shops.add("boots&helm", "orcze buty", 500);
		shops.add("boots&helm", "buty elfickie", 1200);
		shops.add("boots&helm", "buty złote", 2600);
		shops.add("boots&helm", "buty cieni", 3000);
		shops.add("boots&helm", "buty z białego złota", 3200);
		shops.add("boots&helm", "buty olbrzyma", 2400);
		shops.add("boots&helm", "buty ciemności", 3300);
		shops.add("boots&helm", "buty kamienne", 3800);
		shops.add("boots&helm", "buty barbarzyńcy", 3800);
		shops.add("boots&helm", "buty zielonego potwora", 4000);
		shops.add("boots&helm", "lodowe buty", 4400);
		shops.add("boots&helm", "buty chaosu", 4000);
		shops.add("boots&helm", "buty superczłowieka", 6000);
		shops.add("boots&helm", "buty mainiocyjskie", 5200);
		shops.add("boots&helm", "buty xenocyjskie", 5400);
		shops.add("boots&helm", "buty czarne ", 40000);

		// helmet shop (mithrilbourgh)
		// Note the shop sign is done by hand in
		// games.stendhal.server.maps.mithrilbourgh.stores
		shops.add("boots&helm", "karmazynowy hełm", 200);
		shops.add("boots&helm", "lazurowy hełm", 200);
		shops.add("boots&helm", "orczy hełm", 400);
		shops.add("boots&helm", "hełm elficki", 1600);
		shops.add("boots&helm", "złoty hełm", 2400);
		shops.add("boots&helm", "hełm z białego złota", 2800);
		shops.add("boots&helm", "hełm olbrzyma", 2400);
		shops.add("boots&helm", "hełm ciemności", 3100);
		shops.add("boots&helm", "hełm kamienny", 3800);
		shops.add("boots&helm", "hełm barbarzyńcy", 4000);
		shops.add("boots&helm", "kapelusz safari", 4000);
		shops.add("boots&helm", "hełm lodowy", 4400);
		shops.add("boots&helm", "hełm chaosu", 5000);
		shops.add("boots&helm", "hełm mainiocyjski", 5200);
		shops.add("boots&helm", "hełm libertyński", 30000);
		shops.add("boots&helm", "magiczny hełm kolczy", 30000);
		shops.add("boots&helm", "czarny hełm", 60000);

		// buy axes (woodcutter)Woody kupuje:
		shops.add("buyaxe", "halabarda", 1000);
		shops.add("buyaxe", "topór oburęczny złoty", 2000);
		shops.add("buyaxe", "topór oburęczny magiczny", 20000);
		shops.add("buyaxe", "topór Durina", 30000);
		shops.add("buyaxe", "kosa czarna", 30000);
		shops.add("buyaxe", "topór chaosu", 1000000);
		shops.add("buyaxe", "halabarda czarna", 50000);

		// buy chaos items (scared dwarf, after quest)
		shops.add("buychaos", "spodnie chaosu", 4000);
		shops.add("buychaos", "miecz chaosu", 8000);
		shops.add("buychaos", "buty chaosu", 3800);
		shops.add("buychaos", "hełm chaosu", 4800);
		shops.add("buychaos", "płaszcz chaosu", 3800);
		shops.add("buychaos", "tarcza chaosu", 8000);
		shops.add("buychaos", "zbroja chaosu", 9000);

		// buy elvish items (albino elf, after quest) LuposNPC kupuje:
		shops.add("buyelvish", "buty elfickie", 1200);
		shops.add("buyelvish", "spodnie elfickie", 1200);
		shops.add("buyelvish", "miecz elficki", 2000);
		shops.add("buyelvish", "hełm elficki", 1200);
		shops.add("buyelvish", "tarcza elficka", 1400);
		shops.add("buyelvish", "miecz elfów ciemności", 5000);
		shops.add("buyelvish", "płaszcz elficki", 1300);
		shops.add("buyelvish", "zbroja elficka", 1400);

		// magic items or 'relics' (witch in magic city)
		shops.add("buymagic", "miecz nieśmiertelnych", 20000);
		shops.add("buymagic", "spodnie nabijane klejnotami", 30000);
		shops.add("buymagic", "magiczna tarcza płytowa", 50000);
		shops.add("buymagic", "magiczna zbroja płytowa", 40000);
		shops.add("buymagic", "magiczny hełm kolczy", 30000);

		// red items (supplier in sedah city)
		shops.add("buyred", "zbroja karmazynowa", 300);

		// mainio items (despot in mithrilbourgh throne room)
		shops.add("buymainio", "zbroja mainiocyjska", 11000);
		shops.add("buymainio", "buty mainiocyjskie", 5000);
		shops.add("buymainio", "płaszcz mainiocyjski", 15000);
		shops.add("buymainio", "spodnie mainiocyjskie", 5000);
		shops.add("buymainio", "tarcza mainiocyjska", 11000);
		shops.add("buymainio", "zbroja xenocyjska", 12000);
		shops.add("buymainio", "buty xenocyjskie", 6000);
		shops.add("buymainio", "płaszcz xenocyjski", 20000);
		shops.add("buymainio", "spodnie xenocyjskie", 6000);
		shops.add("buymainio", "tarcza xenocyjska", 12000);
		shops.add("buymainio", "hełm mainiocyjski", 8000);
		
		// assassinhq principal Femme Fatale)
		shops.add("buy4assassins", "shuriken", 20);
		shops.add("buy4assassins", "płonący shuriken", 30);
		shops.add("buy4assassins", "sztylet mordercy", 10000);
		shops.add("buy4assassins", "buty superczłowieka", 8000);
		shops.add("buy4assassins", "spodnie superczłowieka", 8000);
		
		// mountain dwarf buyer of odds and ends -3 ados abandoned keep)Ritati DragontrackerNPC kupuje:
		shops.add("buyoddsandends", "czarna perła", 300);
		shops.add("buyoddsandends", "czterolistna koniczyna", 1000);
		shops.add("buyoddsandends", "kolorowe kulki", 300);
		shops.add("buyoddsandends", "zaczarowana igła", 100);
		shops.add("buyoddsandends", "zima zaklęta w kuli", 100);
		shops.add("buyoddsandends", "gruczoł przędzy", 400);
		shops.add("buyoddsandends", "polano", 30);
		shops.add("buyoddsandends", "ruda żelaza", 50);
		shops.add("buyoddsandends", "bryłka złota", 100);
		shops.add("buyoddsandends", "bryłka mithrilu", 200);

		// archery shop in nalwor) skupuje:
		shops.add("buyarcherstuff", "strzała", 2);
		shops.add("buyarcherstuff", "strzała żelazna", 4);
		shops.add("buyarcherstuff", "strzała złota", 5);
		shops.add("buyarcherstuff", "strzała płonąca", 8);
		shops.add("buyarcherstuff", "drewniany łuk", 100);
		shops.add("buyarcherstuff", "kusza", 1300);
		shops.add("buyarcherstuff", "długi łuk", 700);
		shops.add("buyarcherstuff", "klejony łuk", 800);
		shops.add("buyarcherstuff", "kusza łowcy", 1500);	
		
		// selling arrows   MrothoNPC(ados) sprzedaje:
		shops.add("sellarrows", "strzała", 4);
		shops.add("sellarrows", "strzała żelazna", 6);
		shops.add("sellarrows", "strzała złota", 9);
		shops.add("sellarrows", "strzała płonąca", 16);
		
		// assassinhq chief falatheen the dishwasher and veggie buyer)
		// sign is hard dorszed so if you change this change the sign
		shops.add("buyveggiesandherbs", "marchew", 5);
		shops.add("buyveggiesandherbs", "sałata", 10);
		shops.add("buyveggiesandherbs", "por", 25);
		shops.add("buyveggiesandherbs", "brokuł", 30);
		shops.add("buyveggiesandherbs", "cukinia", 10);
		shops.add("buyveggiesandherbs", "kalafior", 30);
		shops.add("buyveggiesandherbs", "pomidor", 20);
		shops.add("buyveggiesandherbs", "cebula", 20);
		shops.add("buyveggiesandherbs", "arandula", 20);
		shops.add("buyveggiesandherbs", "kokuda", 400);	
		shops.add("buyveggiesandherbs", "mandragora", 400);	
		shops.add("buyveggiesandherbs", "kekik", 100);
		shops.add("buyveggiesandherbs", "sclaria", 100);
		
		// gnome village buyer in 0 ados mountain n2 w2)
		shops.add("buy4gnomes", "skórzana zbroja", 10);
		shops.add("buy4gnomes", "maczuga", 10);
		shops.add("buy4gnomes", "skórzany hełm", 10);
		shops.add("buy4gnomes", "buty skórzane", 10);
		shops.add("buy4gnomes", "skórzane spodnie", 10);
		shops.add("buy4gnomes", "peleryna", 10);
		shops.add("buy4gnomes", "drewniana tarcza", 10);
		shops.add("buy4gnomes", "puklerz", 10);
		
		// hotdog lady in athor)Sara BethNPC kupuje:
		shops.add("buy4hotdogs", "paróweczka", 50);
		shops.add("buy4hotdogs", "kiełbasa serowa", 80);
		shops.add("buy4hotdogs", "chleb", 30);
		shops.add("buy4hotdogs", "cebula", 20);
		shops.add("buy4hotdogs", "prasowany tuńczyk", 100);
		shops.add("buy4hotdogs", "szynka", 25);
		shops.add("buy4hotdogs", "ser", 12);
		//Sara BethNPC sprzedaje:
		shops.add("sellhotdogs", "hotdog", 60);
		shops.add("sellhotdogs", "hotdog z serem", 70);
		shops.add("sellhotdogs", "kanapka z tuńczykiem", 310);
		shops.add("sellhotdogs", "kanapka", 300);
		shops.add("sellhotdogs", "shake waniliowy", 50);
		shops.add("sellhotdogs", "shake czekoladowy", 50);
		shops.add("sellhotdogs", "tabliczka czekolady", 80);
		shops.add("sellhotdogs", "zima zaklęta w kuli", 200);

		// magic city barmaid)  TrilliumNPC sprzedaje:
		shops.add("sellmagic", "hotdog", 60);
		shops.add("sellmagic", "hotdog z serem", 70);
		shops.add("sellmagic", "kanapka z tuńczykiem", 320);
		shops.add("sellmagic", "kanapka", 300);
		shops.add("sellmagic", "shake waniliowy", 50);
		shops.add("sellmagic", "shake czekoladowy", 50);
		shops.add("sellmagic", "tabliczka czekolady", 80);
		shops.add("sellmagic", "lukrecja", 100);
		
		// kirdneh city armor) LawrenceNPC kupuje:
		shops.add("buykirdneharmor", "kamienna zbroja", 5000);
		shops.add("buykirdneharmor", "lodowa zbroja", 6000);
		shops.add("buykirdneharmor", "zbroja xenocyjska", 6000);
		shops.add("buykirdneharmor", "zbroja barbarzyńcy", 4000);
		shops.add("buykirdneharmor", "szmaragdowa tarcza smocza", 4000);
		shops.add("buykirdneharmor", "tarcza mainiocyjska", 5000);
		shops.add("buykirdneharmor", "zbroja balorughtów", 6000);
		
		
		// amazon cloaks shop NicklesworthNPC skupuje:
		shops.add("buyamazoncloaks", "płaszcz wampirzy", 60000);
		shops.add("buyamazoncloaks", "płaszcz xenocyjski", 40000);
		shops.add("buyamazoncloaks", "płaszcz mainiocyjski", 40000);
		shops.add("buyamazoncloaks", "peleryna elficka", 2000);
		shops.add("buyamazoncloaks", "płaszcz licha", 10000);
		shops.add("buyamazoncloaks", "płaszcz kamienny", 4000);
		shops.add("buyamazoncloaks", "szmaragdowy płaszcz smoczy", 3000);
		shops.add("buyamazoncloaks", "karmazynowy płaszcz smoczy", 5000);
		shops.add("buyamazoncloaks", "kościany płaszcz smoczy", 3500);
		shops.add("buyamazoncloaks", "czarny płaszcz smoczy", 8000);
		shops.add("buyamazoncloaks", "lazurowy płaszcz smoczy", 4000);
		
		// kirdneh city fishy market)FishmongerNPC skupuje:
		shops.add("buyfishes", "okoń", 22);
		shops.add("buyfishes", "makrela", 20);
		shops.add("buyfishes", "płotka", 10);
		shops.add("buyfishes", "palia alpejska", 30);
		shops.add("buyfishes", "błazenek", 30);
		shops.add("buyfishes", "pokolec", 15);
		shops.add("buyfishes", "pstrąg", 45);
		shops.add("buyfishes", "dorsz", 10);

		// semos trading - swords)
		shops.add("tradeswords", "sztylecik", 10);
		
		// party time! For maria for example. Bit more expensive than normal
		shops.add("sellparty", "napój z oliwką", 50);
		shops.add("sellparty", "tabliczka czekolady", 80);
		shops.add("sellparty", "sok z chmielu", 10);
		shops.add("sellparty", "napój z winogron", 15);
		shops.add("sellparty", "shake waniliowy", 80);
		shops.add("sellparty", "lody", 50);
		shops.add("sellparty", "hotdog", 80);
		shops.add("sellparty", "kanapka", 300);
		
		
		// black items (balduin, when ultimate collector quest completed)BalduinNPC kupuje:
		shops.add("buyblack", "czarna zbroja", 200000);
		shops.add("buyblack", "buty czarne", 50000);
		shops.add("buyblack", "czarny płaszcz", 40000);
		shops.add("buyblack", "czarny hełm", 100000);
		shops.add("buyblack", "czarne spodnie", 40000);
		shops.add("buyblack", "czarna tarcza", 200000);
		shops.add("buyblack", "czarny miecz", 50000);
		shops.add("buyblack", "kosa czarna", 50000);
		shops.add("buyblack", "halabarda czarna", 300000);
		
		// ados market AlexanderNPC kupuje:
		shops.add("buyadosarmors", "zbroja żywiołów", 3000);
		
		// Athor ferry KlaasNPC kupuje:
		shops.add("buypoisons", "trucizna", 40);
		shops.add("buypoisons", "muchomor", 60);
		shops.add("buypoisons", "mocna trucizna", 60);
		shops.add("buypoisons", "śmiertelna trucizna", 100);
		shops.add("buypoisons", "bardzo mocna trucizna", 500);
		shops.add("buypoisons", "zabójcza trucizna", 2000);

		// Should have its own shop (buytraps)
		shops.add("buypoisons", "pułapka na gryzonie", 50);

		// Mine Town Revival Weeks Caroline
		shops.add("sellrevivalweeks", "woda", 10);

		// for ados botanical gardens or if you like, other cafes. 
		// expensive prices to make sure that the npc production of these items isn't compromised
		shops.add("cafe", "herbata", 200);
		shops.add("cafe", "woda", 50);
		shops.add("cafe", "shake czekoladowy", 300);
		shops.add("cafe", "kanapka", 300);
		shops.add("cafe", "kanapka z tuńczykiem", 330);
		
		shops.add("semosmarket", "marchew", 4);
		shops.add("semosmarket", "jabłko", 5);
		shops.add("semosmarket", "ser", 3);
		shops.add("semosmarket", "mięso", 5);
		shops.add("semosmarket", "szynka", 8);
		shops.add("semosmarket", "kiełbasa", 9);
		shops.add("semosmarket", "szpinak", 5);
		shops.add("semosmarket", "mąka", 5);
		shops.add("semosmarket", "borowik", 10);
		shops.add("semosmarket", "pieczarka", 8);
		shops.add("semosmarket", "sałata", 4);
		shops.add("semosmarket", "por", 6);
		shops.add("semosmarket", "brokuł", 7);
		shops.add("semosmarket", "cukinia", 4);
		shops.add("semosmarket", "kalafior", 6);
		shops.add("semosmarket", "pomidor", 10);
		shops.add("semosmarket", "cebula", 6);
		shops.add("semosmarket", "arandula", 4);
		shops.add("semosmarket", "banany", 30);
		shops.add("semosmarket", "kokos", 30);
		shops.add("semosmarket", "chleb", 10);
		shops.add("semosmarket", "kanapki", 15);
		shops.add("semosmarket", "okoń", 4);
		shops.add("semosmarket", "makrela", 4);
		shops.add("semosmarket", "płotka", 4);
		shops.add("semosmarket", "palia alpejska", 4);
		shops.add("semosmarket", "błazenek", 4);
		shops.add("semosmarket", "pokolec", 4);
		shops.add("semosmarket", "pstrąg", 4);
		shops.add("semosmarket", "dorsz", 4);
		
		shops.add("orcitems", "orczy hełm", 300);
		shops.add("orcitems", "orcza zbroja", 500);
		shops.add("orcitems", "płaszcz orczy", 300);
		shops.add("orcitems", "orcza tarcza", 600);
		shops.add("orcitems", "orcze spodnie", 200);
		shops.add("orcitems", "orcze buty", 200);
		shops.add("orcitems", "sztylet orków", 800);
		
		shops.add("blooditems", "krwawe buty", 400);
		shops.add("blooditems", "krwawa kolczuga", 700);
		shops.add("blooditems", "krwawe spodnie", 400);
		shops.add("blooditems", "krwawa suknia", 600);
		
		shops.add("whitegolditems", "hełm z białego złota", 3200);
		shops.add("whitegolditems", "zbroja z białego złota", 4000);
		shops.add("whitegolditems", "spodnie z białego złota", 3200);
		shops.add("whitegolditems", "buty z białego złota", 3100);
		shops.add("whitegolditems", "płaszcz z białego złota", 3400);
		shops.add("whitegolditems", "tarcza z białego złota", 4200);
		
		shops.add("kolekcja333", "piórko", 10);
		shops.add("kolekcja333", "pióro upadłego anioła", 1000);
		shops.add("kolekcja333", "pióro anioła", 1000);
		shops.add("kolekcja333", "pióro mrocznego anioła", 3000);
		shops.add("kolekcja333", "pióro archanioła", 3000);
		shops.add("kolekcja333", "pióro serafina", 20000);
		shops.add("kolekcja333", "kieł smoka", 1000);
		shops.add("kolekcja333", "skóra zielonego smoka", 2600);
		shops.add("kolekcja333", "skóra niebieskiego smoka", 2700);
		shops.add("kolekcja333", "skóra czerwonego smoka", 2800);
		shops.add("kolekcja333", "skóra czarnego smoka", 3000);
		shops.add("kolekcja333", "skóra złotego smoka", 2900);
		shops.add("kolekcja333", "skóra zwierzęca", 100);
		shops.add("kolekcja333", "skóra tygrysa", 2000);
		shops.add("kolekcja333", "skóra lwa", 2000);
		shops.add("kolekcja333", "skóra xenocium", 500);
		
		shops.add("yeticaveshop", "lodowa zbroja", 6000);
		shops.add("yeticaveshop", "lodowy młot bojowy", 20000);
		shops.add("yeticaveshop", "hełm lodowy", 5000);
		shops.add("yeticaveshop", "lodowe buty", 5000);
		shops.add("yeticaveshop", "lodowa kusza", 20000);
		shops.add("yeticaveshop", "lodowe rękawice", 8000);
		
		shops.add("giantitems", "buty olbrzyma", 2500);
		shops.add("giantitems", "hełm olbrzyma", 2500);
		
		shops.add("furgo1", "statuetka aniołka", 500);
		
		shops.add("startkasa", "puklerz", 20);
		shops.add("startkasa", "drewniana tarcza", 30);
		shops.add("startkasa", "skórzana zbroja łuskowa", 20);
		shops.add("startkasa", "zbroja ćwiekowa", 40);
		shops.add("startkasa", "skórzany hełm", 20);
		shops.add("startkasa", "peleryna", 10);
		shops.add("startkasa", "skórzane spodnie", 20);
		shops.add("startkasa", "spodnie kolcze", 100);
		shops.add("startkasa", "buty skórzane", 20);
		shops.add("startkasa", "buty kolcze", 80);
		shops.add("startkasa", "butelka wody", 40);
		shops.add("startkasa", "woda", 5);
		

		
			
		

	}

	private static ShopList instance;

	/**
	 * Returns the Singleton instance.
	 * 
	 * @return The instance
	 */
	public static ShopList get() {
		if (instance == null) {
			instance = new ShopList();
		}
		return instance;
	}

	private final Map<String, Map<String, Integer>> contents;

	private ShopList() {
		contents = new HashMap<String, Map<String, Integer>>();
	}

	/**
	 * gets the items offered by a shop with their prices
	 *
	 * @param name name of shop
	 * @return items and prices
	 */
	public Map<String, Integer> get(final String name) {
		return contents.get(name);
	}
	
	/**
	 * gets a set of all shops
	 *
	 * @return set of shops
	 */
	public Set<String> getShops() {
		return contents.keySet();
	}

	/**
	 * converts a shop into a human readable form
	 *
	 * @param name   name of shop
	 * @param header prefix
	 * @return human readable description
	 */
	public String toString(final String name, final String header) {
		final Map<String, Integer> items = contents.get(name);

		final StringBuilder sb = new StringBuilder(header + "\n");

		for (final Entry<String, Integer> entry : items.entrySet()) {
			sb.append(entry.getKey() + " \t" + entry.getValue() + "\n");
		}

		return sb.toString();
	}

	/**
	 * Add an item to a shop
	 * 
	 * @param name the shop name
	 * @param item the item to add
	 * @param price the price for the item
	 */
	public void add(final String name, final String item, final int price) {
		Map<String, Integer> shop;

		if (contents.containsKey(name)) {
			shop = contents.get(name);
		} else {
			shop = new LinkedHashMap<String, Integer>();
			contents.put(name, shop);
		}

		shop.put(item, price);
	}
}
