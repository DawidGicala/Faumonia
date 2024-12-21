/* $Id: UpdateConverter.java,v 1.33 2011/09/25 17:24:49 nhnb Exp $ */
/***************************************************************************
 *					(C) Copyright 2003-2011 - Stendhal					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package games.stendhal.server.entity.player;

import games.stendhal.common.ItemTools;
import games.stendhal.common.KeyedSlotUtil;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.item.HouseKey;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.slot.KeyedSlot;
import games.stendhal.server.entity.slot.PlayerSlot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import marauroa.common.Pair;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

import org.apache.log4j.Logger;

/**
 * converts player objects to the most recent version by adding attributes,
 * transforming quest states and similar migrations.
 */
public abstract class UpdateConverter {
	private static Logger logger = Logger.getLogger(UpdateConverter.class);

	private static final List<String> ITEM_NAMES_OLD = Arrays.asList(
			"flail_+2", "leather_armor_+1", "leather_cuirass_+1",
			"chain_armor_+1", "scale_armor_+1", "chain_armor_+3",
			"scale_armor_+2", "twoside_axe_+3", "elf_cloak_+2", "mace_+1",
			"mace_+2", "hammer_+3", "chain_helmet_+2", "golden_helmet_+3",
			"longbow_+1", "lion_shield_+1"
	);
	private static final List<String> ITEM_NAMES_NEW = Arrays.asList(
			"morning star", "leather scale armor", "pauldroned leather cuirass",
			"enhanced chainmail", "iron scale armor", "golden chainmail",
			"pauldroned iron cuirass", "golden twoside axe", "blue elf cloak", "enhanced mace",
			"golden mace", "golden hammer", "aventail", "horned golden helmet",
			"composite bow", "enhanced lion shield"
	);

	private static final List<String> ITEM_NAMES_OLD_0_66 = Arrays.asList(
			"key golden", "key silver", "book black", "book blue",
			"duergar elder", "duergar black", "giant elder",
			"chaos sorceror"
	);
	private static final List<String> ITEM_NAMES_NEW_0_66 = Arrays.asList(
			"golden key", "silver key", "black book", "blue book",
			"elder duergar", "black duergar", "elder giant",
			"chaos sorcerer"
	);
	 //Zmiana nazw itemow w katalogu armors z angielskich na polski w wersji pol.0.27.3
	private static final List<String> OLD_ARMORS_POL_0_27_3 = Arrays.asList(
	    "dress","leather armor","leather cuirass","leather scale armor","pauldroned leather cuirass","studded armor",
	    "chain armor","enhanced chainmail","scale armor","iron scale armor","golden chainmail","pauldroned iron cuirass","zardzewiała zbroja",
	    "plate armor","red armor","blue armor","elvish armor","barbarian armor","golden armor","shadow armor","dwarvish armor","góralska cucha","stone armor",
	    "ice armor","chaos armor","xeno armor","mainio armor","light armor","magic plate armor","killer armor",
	    "black armor","mithril armor");
	private static final List<String> NEW_ARMORS_POL_0_27_3 = Arrays.asList(
      "koszula","skórzana zbroja","skórzany kirys","skórzana zbroja łuskowa","skórzany kirys z naramiennikami","zbroja ćwiekowa",
      "kolczuga","kolczuga wzmocniona","zbroja łuskowa","żelazna zbroja łuskowa","złota kolczuga","żelazny kirys z naramiennikami","zardzewiała zbroja płytowa",
      "zbroja płytowa","zbroja karmazynowa","zbroja lazurowa","zbroja elficka","zbroja barbarzyńcy","złota zbroja","zbroja cieni","zbroja krasnoludzka","cuha góralska","kamienna zbroja",
      "lodowa zbroja","zbroja chaosu","zbroja xenocyjska","zbroja mainiocyjska","zbroja światła","magiczna zbroja płytowa","zbroja zabójcy",
      "czarna zbroja","zbroja z mithrilu");
  
    //Zmiana nazw itemow w katalogu arrows z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_ARROWS_POL_0_27_3 = Arrays.asList( 
      "wooden arrow","steel arrow","stalowy bełt","golden arrow","złoty bełt","power arrow","płonący bełt");
  private static final List<String> NEW_ARROWS_POL_0_27_3 = Arrays.asList(
      "strzała","strzała żelazna","bełt stalowy","strzała złota","bełt złoty","strzała płonąca","bełt płonący");
      
    //Zmiana nazw itemow w katalogu axes z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_AXES_POL_0_27_3 = Arrays.asList(
      "sickle","old scythe","small axe","hand axe","axe","battle axe","scythe","twoside axe",
      "bardiche","golden twoside axe","halberd","magic twoside axe","durin axe","black scythe",
      "chaos axe","black halberd");
  private static final List<String> NEW_AXES_POL_0_27_3 = Arrays.asList(
      "sierp","kosa pordzewiała","toporek","topór jednoręczny","topór","topór bojowy","kosa","topór oburęczny",
      "berdysz","topór oburęczny złoty","halabarda","topór oburęczny magiczny","topór Durina","kosa czarna",
      "topór chaosu","halabarda czarna");
      
    //Zmiana nazw itemow w katalogu books z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_BOOKS_POL_0_27_3 = Arrays.asList( 
      "black book","blue book");
  private static final List<String> NEW_BOOKS_POL_0_27_3 = Arrays.asList(     
      "księga czarna","księga lazurowa");
      
    //Zmiana nazw itemow w katalogu boots z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_BOOTS_POL_0_27_3 = Arrays.asList(
      "leather boots","studded boots","chain boots","red boots","steel boots","blue boots",
      "elvish boots","golden boots","shadow boots","stone boots","chaos boots",
      "mainio boots","green thing boots","xeno boots","black boots","mithril boots",
      "killer boots");
  private static final List<String> NEW_BOOTS_POL_0_27_3 = Arrays.asList(
      "buty skórzane","buty nabijane ćwiekami","buty kolcze","buty karmazynowe","buty żelazne","buty lazurowe",
      "buty elfickie","buty złote","buty cieni","buty kamienne","buty chaosu",
      "buty mainiocyjskie","buty zielonego potwora","buty xenocyjskie","buty czarne","buty z mithrilu",
      "buty zabójcy");
      
    //Zmiana nazw itemow w katalogu boxes z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_BOXES_POL_0_27_3 = Arrays.asList(
      "present","basket","stocking");
  private static final List<String> NEW_BOXES_POL_0_27_3 = Arrays.asList(          
      "prezent","koszyk","skarpeta");
      
    //Zmiana nazw itemow w katalogu cloaks z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_CLOAKS_POL_0_27_3 = Arrays.asList(      
      "góralska chusta","cloak","elf cloak","dwarf cloak","desert cloak","red cloak","elvish cloak",
      "blue striped cloak","blue elf cloak","stone cloak","green dragon cloak","bone dragon cloak",
      "lich cloak","vampire cloak","shadow cloak","blue dragon cloak","red dragon cloak","black dragon cloak",
      "golden cloak","chaos cloak","mainio cloak","xeno cloak","black cloak","mithril cloak","rift cloak");
  private static final List<String> NEW_CLOAKS_POL_0_27_3 = Arrays.asList(     
      "chusta góralska","peleryna","peleryna elficka","płaszcz krasnoludzki","płaszcz pustynny","płaszcz karmazynowy","płaszcz elficki",
      "prążkowany płaszcz lazurowy","lazurowy płaszcz elficki","płaszcz kamienny","szmaragdowy płaszcz smoczy","kościany płaszcz smoczy",
      "płaszcz licha","płaszcz wampirzy","płaszcz cieni","lazurowy płaszcz smoczy","karmazynowy płaszcz smoczy","czarny płaszcz smoczy",
      "złoty płaszcz","płaszcz chaosu","płaszcz mainiocyjski","płaszcz xenocyjski","czarny płaszcz","płaszcz z mithrilu","płaszcz z otchłani");            
    
    //Zmiana nazw itemow w katalogu clubs z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_CLUBS_POL_0_27_3 = Arrays.asList(
      "club","staff","mace","flail","enhanced mace","golden mace","skull staff","morning star","hammer","golden hammer",
      "war hammer","ice war hammer","chaos hammer","ugmash","vulcano hammer","club of thorns","necromancers staff");
  private static final List<String> NEW_CLUBS_POL_0_27_3 = Arrays.asList(
      "maczuga","kij","buzdygan","kiścień","piernacz","złoty buzdygan","kij z czaszką","złoty kiścień","pyrlik","złoty młot",
      "młot bojowy","lodowy młot bojowy","młot chaosu","kropacz","młot wulkanów","młot Thora","kij przywoływania nieumarłych");
               
    //Zmiana nazw itemow w katalogu containers z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_CONTAINERS_POL_0_27_3 = Arrays.asList(
      "empty sack","flask","bottle","big bottle","eared bottle","fat bottle","short rounded bottle",
      "short triangular bottle","slim bottle","squared bottle","tall bottle","triangular bottle",
      "empty goblet","goblet");
  private static final List<String> NEW_CONTAINERS_POL_0_27_3 = Arrays.asList(    
      "pusty worek","flasza","buteleczka","butelka","butla czwórniaczka","butla","mała okrągła butelka",
      "mała trójkątna butelka","wąska butelka","kwadratowa butelka","wysoka butelka","trójkątna butelka",
      "pusta czara","czara");
      
    //Zmiana nazw itemow w katalogu documents z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_DOCUMENTS_POL_0_27_3 = Arrays.asList(    
      "note","coupon","closed envelope","opened envelope","sealed envelope",
      "unsealed envelope","assassins id","map","blank scroll","ice scroll");
  private static final List<String> NEW_DOCUMENTS_POL_0_27_3 = Arrays.asList(    
      "karteczka","kupon","koperta zaklejona","koperta otwarta","koperta zalakowana","koperta ze złamaną pieczęcią",
      "licencja na zabijanie","mapa","zwój papieru","lodowy zwój");
      
    //Zmiana nazw itemow w katalogu drinks z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_DRINKS_POL_0_27_3 = Arrays.asList(
      "fierywater","mild koboldish torcibud","strong koboldish torcibud","vsop koboldish torcibud",
      "apple juice","beer","wine","pina colada","tea","milk","antidote","greater antidote",
      "poison","greater poison","deadly poison","disease poison","mega poison",
      "twilight elixir","minor potion","soup","fish soup","potion","greater potion",
      "mega potion","love potion","giant potion");
  private static final List<String> NEW_DRINKS_POL_0_27_3 = Arrays.asList(    
      "ekstrakt litworowy","nalewka litworowa","mocna nalewka litworowa","leżakowana nalewka litworowa",
      "sok jabłkowy","sok z chmielu","napój z winogron","napój z oliwką","filiżanka herbaty","mleko","antidotum","mocne antidotum",
      "trucizna","mocna trucizna","śmiertelna trucizna","zabójcza trucizna","bardzo mocna trucizna",
      "eliksir mroku","mały eliksir","zupa","zupa rybna","eliksir","duży eliksir",
      "wielki eliksir","eliksir miłości","gigantyczny eliksir");
      
    //Zmiana nazw itemow w katalogu flowers z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_FLOWERS_POL_0_27_3 = Arrays.asList(  
      "rhosyd","rose","daisies","pansy","zantedeschia","niezapominajka");
  private static final List<String> NEW_FLOWERS_POL_0_27_3 = Arrays.asList(    
      "orchidea","róża","stokrotki","bratek","bielikrasa","niezapominajki");
    
    //Zmiana nazw itemow w katalogu food z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_FOOD_POL_0_27_3 = Arrays.asList(  
      "cherry","butter","cheese","coconut","icecream","pineapple","artichoke","carrot","salad",
      "tomato","apple","black apple","orange","bread","button mushroom","porcini","spinach",
      "courgette","collard","onion","garlic","cauliflower","broccoli","leek","perch","mackerel",
      "roach","char","clownfish","surgeonfish","red lionfish","egg","chicken","meat","steak",
      "trout","cod","ham","toadstool","fairy cake","crepes suzette","sandwich","pie","fish pie",
      "apple pie","cherry pie","easter egg","chocolate bar","tuna sandwich",
      "sausage","cheese sausage","cheeseydog","vanilla shake","chocolate shake",
      "honey","sugar","licorice","grilled steak");
  private static final List<String> NEW_FOOD_POL_0_27_3 = Arrays.asList(   
      "wisienka","osełka masła","ser","kokos","lody","ananas","karczoch","marchew","sałata",
      "pomidor","jabłko","jabłko niezgody","pomarańcza","chleb","pieczarka","borowik","szpinak",
      "cukinia","kapusta","cebula","czosnek","kalafior","brokuł","por","okoń","makrela",
      "płotka","palia alpejska","błazenek","pokolec","skrzydlica","jajo","udko","mięso","stek",
      "pstrąg","dorsz","szynka","muchomor","mufinka","naleśniki z polewą czekoladową","kanapka","tarta","tarta z rybnym nadzieniem",
      "jabłecznik","ciasto z wiśniami","jajo wielkanocne","tabliczka czekolady","kanapka z tuńczykiem",
      "paróweczka","kiełbasa serowa","hotdog z serem","shake waniliowy","shake czekoladowy",
      "miód","cukier","lukrecja","grillowany stek");
   
    //Zmiana nazw itemow w katalogu gloves z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_GLOVES_POL_0_27_3 = Arrays.asList(
      "leather gloves","leather enchanced gloves","leather strong gloves",
      "plate gloves","red gloves","blue gloves","fire gloves",
      "ice gloves","golden gloves","shadow gloves","black gloves",
      "mithril gloves");
  private static final List<String> NEW_GLOVES_POL_0_27_3 = Arrays.asList(
      "skórzane rękawice","skórzane wzmocnione rękawice","skórzane twarde rękawice",
      "rękawice płytowe","karmazynowe rękawice","lazurowe rękawice","ogniste rękawice",
      "lodowe rękawice","złote rękawice","rękawice cieni","czarne rękawice",
      "rękawice z mithrilu");
      
    //Zmiana nazw itemow w katalogu helmets z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_HELMETS_POL_0_27_3 = Arrays.asList(  
      "leather helmet","trophy helmet","robins hat","studded helmet",
      "chain helmet","desert helmet","viking helmet","aventail",
      "red helmet","blue helmet","golden helmet","shadow helmet",
      "horned golden helmet","chaos helmet","mainio helmet",
      "magic chain helmet","liberty helmet","killer helmet",
      "black helmet","mithril helmet","elvish helmet");
  private static final List<String> NEW_HELMETS_POL_0_27_3 = Arrays.asList(    
      "skórzany hełm","zdobyczny hełm","kapelusz leśniczego","hełm nabijany ćwiekami",
      "hełm kolczy","kapelusz safari","hełm wikingów","misiurka",
      "karmazynowy hełm","lazurowy hełm","złoty hełm","hełm cieni",
      "złoty hełm wikingów","hełm chaosu","hełm mainiocyjski",
      "magiczny hełm kolczy","hełm libertyński","hełm zabójcy",
      "czarny hełm","hełm z mithrilu","hełm elficki");
      
    //Zmiana nazw itemow w katalogu jewellery z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_JEWELLERY_POL_0_27_3 = Arrays.asList(  
      "emerald","sapphire","carbuncle","obsidian","diamond","black pearl",
      "ametyst crystal","emerald crystal","sapphire crystal","carbuncle crystal",
      "obsidian crystal");
  private static final List<String> NEW_JEWELLERY_POL_0_27_3 = Arrays.asList(    
      "szmaragd","szafir","rubin","obsydian","diament","czarna perła",
      "kryształ ametystu","kryształ szmaragdu","kryształ szafiru","kryształ rubinu",
      "kryształ obsydianu");
      
    //Zmiana nazw itemow w katalogu keys z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_KEYS_POL_0_27_3 = Arrays.asList(  
      "dungeon silver key","lich gold key","minotaur key","nalwor bank key",
      "kotoch prison key","kanmararn prison key","sedah gate key",
      "pet sanctuary key","tsoh key","eD key","Zrc key","Zion key",
      "key zakonu","house key","master key","small key");
  private static final List<String> NEW_KEYS_POL_0_27_3 = Arrays.asList(       
      "srebrny klucz do lochów","złoty klucz Licha","klucz minotaura","klucz do banku Nalwor",
      "klucz do więzienia Kotoch","klucz do więzienia Kanmararn","klucz do bram Sedah",
      "klucz do sanktuarium dla zwierząt","klucz do siedziby TSOH","klucz do siedziby eDragons",
      "klucz do siedziby ZRC","klucz do siedziby Zion","klucz do bram Zakonu",
      "klucz do drzwi","klucz GM","kluczyk Zary");
      
    //Zmiana nazw itemow w katalogu legs z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_LEGS_POL_0_27_3 = Arrays.asList(
      "leather legs","studded legs","chain legs","red legs","elvish legs",
      "blue legs","golden legs","shadow legs","mainio legs","chaos legs",
      "xeno legs","jewelled legs","killer legs","black legs","mithril legs");
  private static final List<String> NEW_LEGS_POL_0_27_3 = Arrays.asList(
      "skórzane spodnie","spodnie nabijane ćwiekami","spodnie kolcze","spodnie karmazynowe","spodnie elfickie",
      "spodnie lazurowe","złote spodnie","spodnie cieni","spodnie mainiocyjskie","spodnie chaosu",
      "spodnie xenocyjskie","spodnie nabijane klejnotami",
      "spodnie zabójcy","czarne spodnie","spodnie z mithrilu"); 
      
    //Zmiana nazw itemow w katalogu miscs z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_MISCS_POL_0_27_3 = Arrays.asList(
      "bulb","seed","giant heart","dice","cards","teddy","vampirette entrails",
      "bat entrails","suntan cream","marbles","canned tuna","snowglobe",
      "bobbin");
  private static final List<String> NEW_MISCS_POL_0_27_3 = Arrays.asList(
      "bulwa","nasionka","serce olbrzyma","kości do gry","karty do gry","pluszowy miś",
      "truchło wampira","truchło nietoperza","olejek do opalania","kolorowe kulki",
      "prasowany tuńczyk","zima zaklęta w kuli","szpulka do maszyny");
      
    //Zmiana nazw itemow w katalogu missiles z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_MISSILES_POL_0_27_3 = Arrays.asList(
      "snowball","vomit","dart","power dart","fire shuriken",
      "wooden spear","stones");
  private static final List<String> NEW_MISSILES_POL_0_27_3 = Arrays.asList(
      "śnieżka","wymioty","strzałka","płonąca strzałka","płonący shuriken",
      "włócznia","kamienie");
      
  //Zmiana nazw itemow w katalogu ranget z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_RANGET_POL_0_27_3 = Arrays.asList(
      "wooden bow","longbow","composite bow","crossbow","hunter crossbow",
      "mithril bow","light crossbow");
  private static final List<String> NEW_RANGET_POL_0_27_3 = Arrays.asList(
      "drewniany łuk","długi łuk","klejony łuk","kusza","kusza łowcy",
      "łuk z mithrilu","lekka kusza");
      
    //Zmiana nazw itemow w katalogu relics z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_RELICS_POL_0_27_3 = Arrays.asList(
      "angel figurine","skull ring");
  private static final List<String> NEW_RELICS_POL_0_27_3 = Arrays.asList( 
      "figurka aniołka","pierścień z czaszką"); 
      
    //Zmiana nazw itemow w katalogu resources z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_RESOURCES_POL_0_27_3 = Arrays.asList( 
      "pazury niedźwiedzie","wood","sugar cane","grain","flour","iron ore","coal","wool",
      "yarn","cloth","silver ore","gold nugget","mithril nugget",
      "iron","silver bar","gold bar","mithril bar","golden egg",
      "spotted egg","reptile egg","silk gland","silk thread",
      "oil","mithril thread","mithril fabric","horse hair","beeswax",
      "candle","salt","sulfur","dziub ptaka");
  private static final List<String> NEW_RESOURCES_POL_0_27_3 = Arrays.asList(    
      "niedźwiedzie pazury","polano","trzcina cukrowa","zboże","mąka","ruda żelaza","węgiel","wełna",
      "przędza","sukno","ruda srebra","bryłka złota","bryłka mithrilu",
      "żelazo","sztabka srebra","sztabka złota","sztabka mithrilu","złote jajo",
      "nakrapiane jajo","gadzie jajo","gruczoł przędzy","przędza jedwabna",
      "olejek","przędza z mithrilu","sukno z mithrilu","koński włos","wosk pszczeli",
      "świeca","sól","siarka","dziób ptaka");
      
    //Zmiana nazw itemow w katalogu rings z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_RINGS_POL_0_27_3 = Arrays.asList( 
      "wedding ring","engagement ring","emerald ring");
  private static final List<String> NEW_RINGS_POL_0_27_3 = Arrays.asList(
      "obrączka ślubna","pierścień zaręczynowy","pierścień szmaragdowy");
      
    //Zmiana nazw itemow w katalogu scrolls z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_SCROLLS_POL_0_27_3 = Arrays.asList(
      "summon scroll","nalwor city scroll","ados city scroll",
      "fado city scroll","kirdneh city scroll","kalavan city scroll",
      "marked scroll","invitation scroll","semos city scroll",
      "empty scroll","rainbow beans","balloon","twilight moss",
      "zwój zakopane","zwój gród kraka","last minute","magiczny scroll",
      "tajemny tsoh scroll","scroll do zakonu","eD scroll");
  private static final List<String> NEW_SCROLLS_POL_0_27_3 = Arrays.asList( 
      "zwój przywołania","zwój nalwor","zwój ados","zwój fado","zwój kirdneh",
      "zwój kalavan","zwój zapisany","zwój weselny","zwój semos","niezapisany zwój",
      "magiczne fasolki","balonik","mroczny mech","zwój tatrzański","zwój krakowski",
      "bilet turystyczny","magiczny bilet","zwój TSOH","zwój ZRC","zwój eDragons");
      
    //Zmiana nazw itemow w katalogu shields z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_SHIELDS_POL_0_27_3 = Arrays.asList(
      "buckler","wooden shield","desert shield","studded shield","plate shield","lion shield",
      "polish wooden shield","ice shield","unicorn shield","fire shield","polish chain shield",
      "enhanced lion shield","polish light shield","skull shield","light shield","crown shield",
      "polish metal shield","red shield","lich shield","polish heavy shield","blue shield",
      "elvish shield","golden shield","green dragon shield","shadow shield","mainio shield",
      "chaos shield","magic plate shield","xeno shield","black shield","mithril shield");
  private static final List<String> NEW_SHIELDS_POL_0_27_3 = Arrays.asList( 
      "puklerz","drewniana tarcza","tarcza piaskowa","tarcza ćwiekowa","tarcza płytowa","lwia tarcza",
      "Faumonia  tarcza drewniana","lodowa tarcza","tarcza jednorożca","ognista tarcza","Faumonia  tarcza kolcza",
      "wzmocniona lwia tarcza","Faumonia  tarcza lekka","tarcza z czaszką","tarcza jaśniejąca","tarcza królewska",
      "Faumonia  płytowa tarcza","karmazynowa tarcza","tarcza licha","Faumonia  tarcza ciężka","lazurowa tarcza",
      "tarcza elficka","złota tarcza","szmaragdowa tarcza smocza","tarcza cieni","tarcza mainiocyjska",
      "tarcza chaosu","magiczna tarcza płytowa","tarcza xenocyjska","czarna tarcza","tarcza z mithrilu");
      
    //Zmiana nazw itemow w katalogu special z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_SPECIAL_POL_0_27_3 = Arrays.asList(
      "lucky charm","mithril clasp","mythical egg","magical eggshells","ball");
  private static final List<String> NEW_SPECIAL_POL_0_27_3 = Arrays.asList(
      "czterolistna koniczyna","brosza z mithrilu","mityczne jajo","magiczne skorupki","piłka");
      
    //Zmiana nazw itemow w katalogu swords z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_SWORDS_POL_0_27_3 = Arrays.asList(
      "orc blood","orc dagger","shadow sword","khopesh","assassin dagger","knife",
      "night dagger","obsidian knife","soul dagger","golden blade","dagger",
      "dark dagger","hell dagger","chaos dagger","short sword","r hand sword",
      "l hand sword","sword","imperator sword","scimitar",
      "buster","orc sword","demon sword","vampire sword","claymore","biting sword",
      "fire sword","ice sword","chaos sword","broadsword","golden orc sword",
      "elvish sword","drow sword","great sword","orcish sword","immortal sword",
      "xeno sword","black sword","demon fire sword");
  private static final List<String> NEW_SWORDS_POL_0_27_3 = Arrays.asList(    
      "krwawy miecz orków","sztylet orków","miecz cieni","chopesz","sztylet mordercy","mieczyk",
      "czarny sztylet","obsydianowy saks","miecz dusz","złota klinga","sztylecik",
      "sztylet mroku","piekielny sztylet","sztylet chaosu","krótki miecz","miecz praworęczny",
      "miecz leworęczny","miecz","miecz cesarski","bułat",
      "pogromca","klinga orków","miecz demonów","krwiopijca","miecz dwuręczny","miecz zaczepny",
      "miecz ognisty","miecz lodowy","miecz chaosu","pałasz","złota klinga orków",
      "miecz elficki","miecz elfów ciemności","półtorak","miecz orków","miecz nieśmiertelnych",
      "miecz xenocyjski","czarny miecz","ognisty miecz demonów");
      
    //Zmiana nazw itemow w katalogu tokens z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_TOKENS_POL_0_27_3 = Arrays.asList(
      "arrow game token","o board token","x board token");
  private static final List<String> NEW_TOKENS_POL_0_27_3 = Arrays.asList(
      "wskaźnik","kółko do gry","krzyżyk do gry"); 
      
    //Zmiana nazw itemow w katalogu tools z angielskich na polski w wersji pol.0.27.3
  private static final List<String> OLD_TOOLS_POL_0_27_3 = Arrays.asList(       
      "food mill","sugar mill","pestle and mortar","gold pan","fishing rod","magical scissors","magical needle");
  private static final List<String> NEW_TOOLS_POL_0_27_3 = Arrays.asList(      
      "młynek","młynek do cukru","moździerz z tłuczkiem","misa do płukania złota","wędka","zaczarowane nożyczki","zaczarowana igła");    
                                                      
	/**
	 * quest name, quest index, creatures to kill.
	 */
	private static final HashMap<String, Pair<Integer, List<String>>> KILL_QUEST_NAMES;
	static {
		KILL_QUEST_NAMES = new HashMap<String, Pair<Integer, List<String>>>();
		KILL_QUEST_NAMES.put("meet_hayunn", 
				new Pair<Integer, List<String>>(1, Arrays.asList(
					"szczur")));
			
		KILL_QUEST_NAMES.put("clean_storage", 
				new Pair<Integer, List<String>>(1, Arrays.asList(
					"szczur",
					"szczur jaskiniowy","wąż")));
			
		KILL_QUEST_NAMES.put("club_thorns", 
				new Pair<Integer, List<String>>(1, Arrays.asList(
					"szef górskich orków")));
			
		KILL_QUEST_NAMES.put("kill_dhohr_nuggetcutter", 
				new Pair<Integer, List<String>>(1, Arrays.asList(
					"gigantyczny krasnal", 
					"górski krasnal", 
					"górski starszy krasnal", 
					"górski krasnal bohater", 
					"górski krasnal lider")));
			
		KILL_QUEST_NAMES.put("kill_gnomes", 
				new Pair<Integer, List<String>>(1, Arrays.asList(
					"gnom", 
					"gnom zwiadowca", 
					"gnom kawalerzysta")));
			
		KILL_QUEST_NAMES.put("sad_scientist", 
				new Pair<Integer, List<String>>(1, Arrays.asList(
					"Sergej Elos")));
	}

	private static final List<String> POL_ITEM_NAMES_OLD = Arrays.asList(
			"różdżka elficka", "trójząb", "różdżka wędrowcy", "różdżka niebios",
			"różdżka ognia", "różdżka Trzygłowa", "magia ognia",
			"magia życia", "magia nieba", "magia ciemności", "magia jasności"
	);
	private static final List<String> POL_ITEM_NAMES_NEW = Arrays.asList(
			"różdżka", "trójząb Trzygłowa", "różdżka Strzyboga", "różdżka Wołosa",
			"różdżka Swaroga", "różdżka Peruna", "magia płomieni",
			"magia ziemi", "magia deszczu", "magia mroku", "magia światła"
	);

	/**
	 * Update old item names to the current naming.
	 *
	 * @param name
	 * @return the currentName of an Item
	 */
	public static String updateItemName(String name) {
		if (name != null) {
    		// handle renamed items
    		int idx = ITEM_NAMES_OLD.indexOf(name);
    		if (idx != -1) {
    			name = ITEM_NAMES_NEW.get(idx);
    		}

    		// Remove underscore characters from old database item names - ConversationParser
    		// is now capable to work with space separated item names.
    		name = ItemTools.itemNameToDisplayName(name);

    		// rename some additional items to fix grammar in release 0.66
    		idx = ITEM_NAMES_OLD_0_66.indexOf(name);
    		if (idx != -1) {
    			name = ITEM_NAMES_NEW_0_66.get(idx);
    		}

    		//Zmiana nazw itemow w katalogu armors z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_ARMORS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_ARMORS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu arrows z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_ARROWS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_ARROWS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu axes z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_AXES_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_AXES_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu books z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_BOOKS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_BOOKS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu boots z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_BOOTS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_BOOTS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu boxes z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_BOXES_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_BOXES_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu cloaks z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_CLOAKS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_CLOAKS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu clubs z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_CLUBS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_CLUBS_POL_0_27_3.get(idx);
    		}

        //Zmiana nazw itemow w katalogu containers z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_CONTAINERS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_CONTAINERS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu documents z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_DOCUMENTS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_DOCUMENTS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu drinks z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_DRINKS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_DRINKS_POL_0_27_3.get(idx);
    		}

        //Zmiana nazw itemow w katalogu flowers z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_FLOWERS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_FLOWERS_POL_0_27_3.get(idx);
    		}

        //Zmiana nazw itemow w katalogu food z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_FOOD_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_FOOD_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu gloves z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_GLOVES_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_GLOVES_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu helmets z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_HELMETS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_HELMETS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu jewellery z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_JEWELLERY_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_JEWELLERY_POL_0_27_3.get(idx);
    		}

        //Zmiana nazw itemow w katalogu keys z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_KEYS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_KEYS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu legs z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_LEGS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_LEGS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu miscs z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_MISCS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_MISCS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu miscs z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_MISSILES_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_MISSILES_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu ranget z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_RANGET_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_RANGET_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu relics z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_RELICS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_RELICS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu resources z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_RESOURCES_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_RESOURCES_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu rings z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_RINGS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_RINGS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu scrolls z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_SCROLLS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_SCROLLS_POL_0_27_3.get(idx);
    		}
    		
    			//Zmiana nazw itemow w katalogu shields z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_SHIELDS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_SHIELDS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu special z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_SPECIAL_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_SPECIAL_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu swords z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_SWORDS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_SWORDS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu tokens z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_TOKENS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_TOKENS_POL_0_27_3.get(idx);
    		}
    		
    		//Zmiana nazw itemow w katalogu tools z angielskich na polski w wersji pol.0.27.3
    		idx = OLD_TOOLS_POL_0_27_3.indexOf(name);
    		if (idx != -1) {
    			name = NEW_TOOLS_POL_0_27_3.get(idx);
    		}

    		idx = POL_ITEM_NAMES_OLD.indexOf(name);
    		if (idx != -1) {
    			name = POL_ITEM_NAMES_NEW.get(idx);
    		}
		}

		return name;
	}

	public static Item updateItem(String name) {
		// process the old keys for houses, now that we have change locks implemented
		Item item;
		if (name.startsWith("private key ")) {
			// which zone the house is in
			final String zoneName;
			final String doorId;
			// number tracks the lock changes
			final int number = 0;
			final String[] parts = name.split(" ");
			if (parts.length > 2) {
			   	try {
					// house number
					final int id;
					id = Integer.parseInt(parts[2]);
					if (id < 26) {
						zoneName = "kalavan";
					} else if (id < 50) {
						zoneName = "kirdneh";
					} else {
						zoneName = "ados";
					}
					doorId = zoneName + " house " + Integer.toString(id);
					// now set the infostring of the house key to doorId;number;
					item = SingletonRepository.getEntityManager().getItem("klucz do drzwi");
					((HouseKey) item).setup(doorId, number, null);
				} catch (final NumberFormatException e) {
					// shouldn't happen - give up and this will generate a warning
					item = SingletonRepository.getEntityManager().getItem(name);
				}
			} else {
				// shouldn't happen - give up and this will generate a warning
				item = SingletonRepository.getEntityManager().getItem(name);
			}
		} else {
			// item wasn't private key, just make it as normal
			item = SingletonRepository.getEntityManager().getItem(name);
		}
		return item;
	}

	/**
     * Updates a player RPObject from an old version of Stendhal.
     * 
     * @param object
     *            RPObject representing a player
     */
    public static void updatePlayerRPObject(final RPObject object) {
    	final String[] slotsNormal = { "bag", "rhand", "lhand", "head", "neck", "armor",
    			"legs", "glove", "medal", "feet", "finger", "cloak", "fingerb", "bank", "bank_ados",
    			"zaras_chest_ados", "bank_fado", "bank_nalwor", "bank_zakopane", "bank_tsoh",
    			"spells", "keyring", "money", "trade" };
    
    	final String[] slotsSpecial = { "!quests", "!kills", "!buddy", "!ignore",
    			"!visited", "skills", "!tutorial"};
    
    	// Port from 0.03 to 0.10
    	if (!object.has("base_hp")) {
    		object.put("base_hp", "100");
    		object.put("hp", "100");
    	}
    
    	// Port from 0.13 to 0.20
    	if (!object.has("outfit")) {
    		object.put("outfit", new Outfit().getCode());
    	}
    
    	// create slots if they do not exist yet:
    
    	// Port from 0.20 to 0.30: bag, rhand, lhand, armor, head, legs, feet
    	// Port from 0.44 to 0.50: cloak, bank
    	// Port from 0.57 to 0.58: bank_ados, bank_fado
    	// Port from 0.58 to ?: bank_nalwor, keyring, finger
    	for (final String slotName : slotsNormal) {
    		if (!object.hasSlot(slotName)) {
    			object.addSlot(new PlayerSlot(slotName));
    		}
    	}
    
    	// Port from 0.44 to 0.50: !buddy
    	// Port from 0.56 to 0.56.1: !ignore
    	// Port from 0.57 to 0.58: skills
    	for (final String slotName : slotsSpecial) {
    		if (!object.hasSlot(slotName)) {
    			object.addSlot(new KeyedSlot(slotName));
    		}
    		final RPSlot slot = object.getSlot(slotName);
    		if (slot.size() == 0) {
    			final RPObject singleObject = new RPObject();
    			slot.add(singleObject);
    		}
    	}
    
    	// Port from 0.30 to 0.35
    	if (!object.has("atk_xp")) {
    		object.put("atk_xp", "0");
    		object.put("def_xp", "0");
    	}
    
    	if (object.has("devel")) {
    		object.remove("devel");
    	}
    
    	// From 0.44 to 0.50
    	if (!object.has("release")) {
    		object.put("release", "0.00");
    		object.put("atk", "10");
    		object.put("def", "10");
    	}
    
    	if (!object.has("age")) {
    		object.put("age", "0");
    	}
    
    	if (!object.has("karma")) {
    		// A little beginner's luck
    		object.put("karma", 10);
    	}
    	if (!object.has("mana")) {
    		object.put("mana", 0);
    	}
    	if (!object.has("base_mana")) {
    		object.put("base_mana", 0);
    	}
    
    	// Renamed to skills
    	if (object.has("!skills")) {
    		object.remove("!skills");
    	}
    
    	if (!object.has("height")) {
    		object.put("height", 2);
    	}
    	if (!object.has("width")) {
    		object.put("width", 1);
    	}
    
    	// port to 0.66
    	transformKillSlot(object);

    	// port to 0.81 because of a bug in 0.80 which allowed 0 hp by double killing on logout during dying
    	if (object.getInt("hp") <= 0) {
    		logger.warn("Setting hp to 1 for player " + object);
    		object.put("hp", 1);
    	}
    	
    	// port to 0.85 added buddy list as map - copy buddies to map
    	if (object.hasSlot("!buddy")) {
    		for (RPObject buddy : object.getSlot("!buddy")) {
    			for (final String buddyname : buddy) {
    				if (buddyname.startsWith("_")) {
    					boolean online = false;
    					if (buddy.get(buddyname).equals("1")) {
    						online = true;
    					}
    					//strip out the _ in the beginning
    					object.put("buddies", buddyname.substring(1), online);
    				}
    			}
    			buddy.remove("_db_id");
    		}
    		// remove buddy slot for 0.87
    		object.removeSlot("!buddy");
		}
		object.remove("buddies", "db_id");

		//port to 0.86: port keymap to feature map, karama_indicator as feature
		if (object.hasSlot("!features")) {
			if (KeyedSlotUtil.getKeyedSlot(object, "!features", "keyring") != null) {
				object.put("features", "keyring", "");
			}
		}
		if (KeyedSlotUtil.getKeyedSlot(object, "!quests", "learn_karma") != null) {
			object.put("features", "karma_indicator", "");
		}

		// port to 0.89: fix age
		if (object.has("age")) {
			if (!object.has("release") || (object.get("release").compareTo("0.26.1") <= 0)) {
				object.put("age", object.getInt("age") * 180 / 200);
			}
		}

		// port to 0.97: expire all temporary outfits
		if (object.has("outfit_org") && !object.has("outfit_expire_age")) {
			object.put("outfit_expire_age", 0);
		}

		// port to 29.2
		if (!object.has("gender")) {
			object.put("gender", "M");
		}
	}

	/**
	 * Transform kill slot content to the new kill recording system.
	 * @param object 
	 */
	static void transformKillSlot(final RPObject object) {
		final RPObject kills = KeyedSlotUtil.getKeyedSlotObject(object, "!kills");

		if (kills != null) {
    		final RPObject newKills = new RPObject();
    		for (final String attr : kills) {
    			// skip "id" entries
    			if (!attr.equals("id")) {
        			String newAttr = attr;
        			String value = kills.get(attr);

        			// Is it stored using the old recording system without an dot?
        			if (attr.indexOf('.') < 0) {
        				newAttr = updateItemName(newAttr);
        				newAttr = value + "." + newAttr;
        				value = "1";
        			}

        			newKills.put(newAttr, value);
    			}
    		}

    		final RPSlot slot = object.getSlot("!kills");
    		slot.remove(kills.getID());
    		slot.add(newKills);
		}
	}

	/**
	 * Update the quest slot to the current version.
	 * @param player
	 */
	public static void updateQuests(final Player player) {
		final EntityManager entityMgr = SingletonRepository.getEntityManager();

		// rename old quest slot "Valo_concoct_potion" to "valo_concoct_potion"
		// We avoid to lose potion in case there is an entry with the old and the new name at the same
		// time by combining them by calculating the minimum of the two times and the sum of the two amounts.
		migrateSumTimedQuestSlot(player, "Valo_concoct_potion", "valo_concoct_potion");

		// From 0.66 to 0.67
		// update quest slot content, 
		// replace "_" with " ", for item/creature names
		for (final String questSlot : player.getQuests()) {

			if (player.hasQuest(questSlot)) {
				final String itemString = player.getQuest(questSlot);

				final String[] parts = itemString.split(";");

				final StringBuilder buffer = new StringBuilder();
				boolean first = true;

				for (int i = 0; i < parts.length; ++i) {
					final String oldName = parts[i];

					// Convert old item names to their new representation with correct grammar
					// and without underscores.
					String newName = UpdateConverter.updateItemName(oldName);

					// check for valid item and creature names if the update converter changed the name
					if (!newName.equals(oldName)) {
						if (!entityMgr.isCreature(newName) && !entityMgr.isItem(newName)) {
							newName = oldName;
						}
					}

					if (first) {
						buffer.append(newName);
						first = false;
					} else {
						buffer.append(';');
						buffer.append(newName);
					}
				}

				player.setQuest(questSlot, buffer.toString());
			}
		}

		// fix quest slots for kills quests.		
		fixKillQuestsSlots(player);
		
		// fix DailyMonsterQuest slot
		fixDailyMonsterQuestSlot(player);
		
		// fix Maze
		fixMazeQuestSlot(player);
		
	}

	private static void fixMazeQuestSlot(Player player) {
		final String QUEST_SLOT = "maze";
		
		// if player didnt started quest --> exit
		if(!player.hasQuest(QUEST_SLOT)) {
			return;
		}
		
		final String questSlot = player.getQuest(QUEST_SLOT);
		
		// if player's quest slot is already updated --> exit
		if(Arrays.asList(questSlot.split(";")).size()>1) {
			return;
		}
		
		player.setQuest(QUEST_SLOT, 0, "start");
		player.setQuest(QUEST_SLOT, 1, questSlot);
		player.setQuest(QUEST_SLOT, 2, "0");
		
    }

	private static void fixDailyMonsterQuestSlot(final Player player) {
		final String QUEST_SLOT = "daily";
		
		// if player didnt started quest, exiting
		if(!player.hasQuest(QUEST_SLOT)) {
			return;
		}
		
		final String questInfo = player.getQuest(QUEST_SLOT, 0);
		
		// if player completed quest, exiting
		if(questInfo.equals("done")) {
			return;
		}
		// if player already updated, exiting
		if(Arrays.asList(questInfo.split(",")).size()==5) {
			return;
		}
		
		// now fix player's quest slot
		player.setQuest(QUEST_SLOT, 0, player.getQuest(QUEST_SLOT, 0)+",0,1,0,0");
	}
	
	/**
	 * fix old-style kill quests slots.
	 * @param player - player which quest slots will fix.
	 */
	private static void fixKillQuestsSlots(final Player player) {
		for(String questSlot: KILL_QUEST_NAMES.keySet()) {
			// if player have no extra info in quest slot, we will add it :-)
			if(player.getQuest(questSlot)==null) {
				continue;
			}
			if(player.getQuest(questSlot).equals("start")) {
				final List<String> creatures = KILL_QUEST_NAMES.get(questSlot).second();
				StringBuilder sb=new StringBuilder("");
				for(int i=0; i<creatures.size(); i++) {
					sb.append(creatures.get(i)+",0,1,0,0,");
				}
				final String result = sb.toString();
				player.setQuest(questSlot, 
						KILL_QUEST_NAMES.get(questSlot).first(),
						// will not record last semicolon.
						result.substring(0, result.length()-1));
			}
		}
	}
	
	 // update the name of a quest to the new spelling
//	private static void renameQuestSlot(Player player, String oldName, String newName) {
//		String questState = player.getQuest(oldName);
//
//		if (questState != null) {
//			player.setQuest(newName, questState);
//			player.removeQuest(oldName);
//		}
//	}

	 // update the name of a quest to the new spelling and accumulate the content
	private static void migrateSumTimedQuestSlot(final Player player, final String oldName, final String newName) {
		final String oldState = player.getQuest(oldName);

		if (oldState != null) {
			String questState = oldState;
			final String newState = player.getQuest(newName);

			if (newState != null) {
				final String[] oldParts = oldState.split(";");
				final String[] newParts = newState.split(";");

				if ((oldParts.length == 3) && (newParts.length == 3)) {
					try {
        				final int oldAmount = Integer.parseInt(oldParts[0]);
        				int newAmount = Integer.parseInt(newParts[0]);
        				final String oldItem = oldParts[1];
        				final String newItem = newParts[1];
        				final long oldTime = Long.parseLong(oldParts[2]);
        				long newTime = Long.parseLong(newParts[2]);

        				if (oldItem.equals(newItem)) {
        					newAmount += oldAmount;

        					if (oldTime < newTime) {
        						newTime = oldTime;
        					}

        					questState = Integer.toString(newAmount) + ';' + newItem + ';' + Long.toString(newTime);
        				}
        			} catch (final NumberFormatException e) {
        				e.printStackTrace();
        			}
				}
			}

			player.setQuest(newName, questState);
			player.removeQuest(oldName);
		}
	}
	
	

}
