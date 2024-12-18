/* $Id: PassiveEntityRespawnPointFactory.java,v 1.37 2012/12/12 22:38:17 tigertoes Exp $ */
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
package games.stendhal.server.entity.mapstuff.spawner;

import java.util.Arrays;

import marauroa.common.game.IRPZone.ID;

import org.apache.log4j.Logger;

/**
 * creates a PassiveEntityRespawnPoint.
 */
public class PassiveEntityRespawnPointFactory {
	private static Logger logger = Logger
			.getLogger(PassiveEntityRespawnPointFactory.class);

	/**
	 * creates a PassiveEntityRespawnPoint.
	 * 
	 * @param clazz
	 *            class
	 * @param type
	 *            type
	 * @param id
	 *            zone id
	 * @param x
	 *            x
	 * @param y
	 *            y
	 * @return PassiveEntityRespawnPoint or null in case some error occurred
	 */
	public static PassiveEntityRespawnPoint create(final String clazz,
			final int type, final ID id, final int x, final int y) {
		PassiveEntityRespawnPoint passiveEntityrespawnPoint = null;

		if (clazz.contains("herb")) {
			passiveEntityrespawnPoint = createHerb(type);

		} else if (clazz.contains("corn")) {
			passiveEntityrespawnPoint = createGrain(type);

		} else if (clazz.contains("mushroom")) {
			passiveEntityrespawnPoint = createMushroom(type);

		} else if (clazz.contains("resources")) {
			passiveEntityrespawnPoint = createResource(type);

		} else if (clazz.contains("sheepfood")) {
			passiveEntityrespawnPoint = new SheepFood();

		} else if (clazz.contains("vegetable")) {
			passiveEntityrespawnPoint = createVegetable(type);

		} else if (clazz.contains("jewelry")) {
			passiveEntityrespawnPoint = createJewelry(type);

		} else if (clazz.contains("sign")) {
			/*
			 * Ignore signs. The way to go is XML.
			 */
			return null;

		} else if (clazz.contains("fruits")) {
			passiveEntityrespawnPoint = createFruit(type);

		} else if (clazz.contains("meat_and_fish")) {
			passiveEntityrespawnPoint = createMeatAndFish(type);

		} else if (clazz.contains("dairy")) {
			passiveEntityrespawnPoint = createDairy(type);
		}

		if (passiveEntityrespawnPoint == null) {
			logger.error("Unknown Entity (class/type: " + clazz + ":" + type
					+ ") at (" + x + "," + y + ") of " + id + " found");
			return null;
		}

		return passiveEntityrespawnPoint;
	}

	private static PassiveEntityRespawnPoint createDairy(final int type) {
		PassiveEntityRespawnPoint passiveEntityrespawnPoint;
		switch (type) {
		case 0:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("jajo", 900);
			passiveEntityrespawnPoint
					.setDescription("Jeśli byłbyś kurą to chciałbyś znieść jajko w tym miejscu.");
			break;
		default:
			passiveEntityrespawnPoint = null;
			break;
		}
		return passiveEntityrespawnPoint;
	}

	private static PassiveEntityRespawnPoint createMeatAndFish(final int type) {
		PassiveEntityRespawnPoint passiveEntityrespawnPoint;
		switch (type) {
		case 0:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("mięso", 100);
			passiveEntityrespawnPoint.setDescription("Oto resztki po zwierzęciu. Może to być mięso.");
			break;
		case 1:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("szynka", 100);
			passiveEntityrespawnPoint.setDescription("Oto resztki po zwierzęciu. Wyglądają jak szynka.");
			break;
		case 2:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("udko", 100);
			passiveEntityrespawnPoint.setDescription("Oto resztki po zwierzęciu. Może to udko?");
			break;
		case 3:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("płotka", 100);
			passiveEntityrespawnPoint.setDescription("Oto jakieś połyskujące łuski ryby jakby to była płotka.");
			break;

		case 4:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("palia alpejska", 100);
			passiveEntityrespawnPoint.setDescription("Oto jakieś połyskujące łuski ryby jakby to była palia alpejska.");
			break;
		default:
			passiveEntityrespawnPoint = null;
			break;
		}
		return passiveEntityrespawnPoint;
	}

	private static PassiveEntityRespawnPoint createFruit(final int type) {
		PassiveEntityRespawnPoint passiveEntityrespawnPoint;
		switch (type) {
		case 0:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("kokos", 800);
			passiveEntityrespawnPoint
					.setDescription("Oto miejsce, gdzie kokos spadają na ziemię.");
			break;
		case 1:
			passiveEntityrespawnPoint = new VegetableGrower("pomidor", "tomato");
			passiveEntityrespawnPoint
					.setDescription("Oto roślina pomidor.");
			break;
		case 2:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("ananas", 1200);
			break;
		case 3:
			passiveEntityrespawnPoint = new VegetableGrower("arbuz", "watermelon");
			passiveEntityrespawnPoint
					.setDescription("Oto arbuz.");
			break;
		case 4:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("banan", 1000);
			passiveEntityrespawnPoint
					.setDescription("Wygląda na miejsce gdzie spada banan.");
			break;
		case 5:
			passiveEntityrespawnPoint = new VegetableGrower("winogrona", "grapes");
			passiveEntityrespawnPoint
					.setDescription("Oto winogrona.");
			break;
		case 6:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("gruszka", 500);
			passiveEntityrespawnPoint
					.setDescription("Wygląda na miejsce gdzie spada gruszka.");
			break;
		case 7:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("granat", 800);
			passiveEntityrespawnPoint
					.setDescription("Wygląda na miejsce gdzie spada granat.");
			break;
		case 8:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("oliwka", 1200);
			passiveEntityrespawnPoint
					.setDescription("Wygląda na miejsce gdzie spada oliwka.");
			break;
		default:
			passiveEntityrespawnPoint = null;
			break;
		}
		return passiveEntityrespawnPoint;
	}

	private static PassiveEntityRespawnPoint createJewelry(final int type) {
		PassiveEntityRespawnPoint passiveEntityrespawnPoint;
		switch (type) {
		case 0:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("rubin", 6000);
			passiveEntityrespawnPoint.setDescription("Oto ślad jakiś czerwonych kryształów.");
			break;
		case 1:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("szafir", 6000);
			passiveEntityrespawnPoint.setDescription("Oto dowód, że są tutaj szafiry.");
			break;
		case 2:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("szmaragd", 6000);
			passiveEntityrespawnPoint.setDescription("Oto ślad, że występuje tutaj cenny szmaragdy.");
			break;
		case 3:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("prezent", 20000);
			passiveEntityrespawnPoint.setDescription("Widzisz przytulne miejsce pod choinką.");
			break;
		case 4:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("prezent świąteczny 2020", 25000);
			passiveEntityrespawnPoint.setDescription("Widzisz przytulne miejsce pod choinką.");
			break;
		case 5:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("rubin", 336000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 6:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("szafir", 312000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 7:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("szmaragd", 288000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 8:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("obsydian", 864000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 9:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("ruda żelaza", 24000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 10:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("ruda żelaza", 36000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 11:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("ruda żelaza", 60000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 12:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("bryłka złota", 36000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 13:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("bryłka złota", 48000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 14:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("bryłka złota", 108000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 15:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("bryłka mithrilu", 36000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 16:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("bryłka mithrilu", 96000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 17:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("błękitny kamyczek szlachetny", 24000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 18:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("błękitny kamyczek szlachetny", 48000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 19:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("błękitny kamyczek szlachetny", 72000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 20:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("błękitne kamyczki szlachetne", 60000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 21:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("błękitne kamyczki szlachetne", 120000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 22:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("błękitny kamień szlachetny", 96000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 23:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("błękitny kamień szlachetny", 168000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 24:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("błękitne kamienie szlachetne", 288000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 25:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("czerwony kamyczek szlachetny", 48000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 26:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("czerwony kamyczek szlachetny", 156000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 27:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("czerwony kamień szlachetny", 180000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		case 28:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("czerwone kamienie szlachetne", 372000);
			passiveEntityrespawnPoint.setDescription("Widzisz stary kamieniołom. Co jakiś czas wiatr odkrywa resztki znajdujących się tu złóż...");
			break;
		default:
			passiveEntityrespawnPoint = null;
			break;
		}
		return passiveEntityrespawnPoint;
	}

	private static PassiveEntityRespawnPoint createVegetable(final int type) {
		PassiveEntityRespawnPoint passiveEntityrespawnPoint;
		switch (type) {
		case 0:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("jabłko", 500);
			passiveEntityrespawnPoint.setDescription("Oto miejsce, gdzie jabłka spadają na ziemię.");
			break;
		case 1:
			passiveEntityrespawnPoint = new VegetableGrower("marchew", "carrot");
			break;
		case 2:
			passiveEntityrespawnPoint = new VegetableGrower("sałata", "salad");
			break;
		case 3:
			passiveEntityrespawnPoint = new VegetableGrower("brokuł", "broccoli");
			break;
		case 4:
			passiveEntityrespawnPoint = new VegetableGrower("kalafior", "cauliflower");
			break;
		case 5:
			passiveEntityrespawnPoint = new VegetableGrower("kapusta pekińska", "chinese cabbage");
			break;
		case 6:
			passiveEntityrespawnPoint = new VegetableGrower("por", "leek");
			break;
		case 7:
			passiveEntityrespawnPoint = new VegetableGrower("cebula", "onion");
			break;
		case 8:
			passiveEntityrespawnPoint = new VegetableGrower("cukinia", "courgette");
			break;
		case 9:
			passiveEntityrespawnPoint = new VegetableGrower("szpinak", "spinach");
			break;
		case 10:
			passiveEntityrespawnPoint = new VegetableGrower("kapusta", "collard");
			break;
		case 11:
			passiveEntityrespawnPoint = new VegetableGrower("czosnek", "garlic");
			break;
		case 12:
			passiveEntityrespawnPoint = new VegetableGrower("karczoch", "artichoke");
			break;
		default:
			passiveEntityrespawnPoint = null;
			break;
		}
		return passiveEntityrespawnPoint;
	}

	private static PassiveEntityRespawnPoint createResource(final int type) {
		PassiveEntityRespawnPoint passiveEntityrespawnPoint;
		switch (type) {
		case 0:
			passiveEntityrespawnPoint = new VegetableGrower("polano", "wood");
			passiveEntityrespawnPoint.setDescription("Widzisz w ziemi odcisk na kształt polana.");
			break;
		case 1:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("ruda żelaza", 3000);
			passiveEntityrespawnPoint.setDescription("Oto mała grudka rudy żelaza.");
			break;

		case 2:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("sztabka złota", 9000);
			passiveEntityrespawnPoint.setDescription("Oto ślad błyszczącego złota.");
			break;
		case 3:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("sztabka mithrilu", 16000);
			passiveEntityrespawnPoint.setDescription("Oto ślad błyszczącego mithrilu.");
			break;
		case 4:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("bryłka złota", 6000);
			passiveEntityrespawnPoint.setDescription("Oto mały, błyszczący kawałek złota.");
			break;
		case 5:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("bryłka mithrilu", 12000);
			passiveEntityrespawnPoint.setDescription("Oto mały kawałek bryłki mithrilu.");
			break;
		default:
			passiveEntityrespawnPoint = null;
			break;

		}
		return passiveEntityrespawnPoint;
	}

	private static PassiveEntityRespawnPoint createMushroom(final int type) {
		PassiveEntityRespawnPoint passiveEntityrespawnPoint;
		switch (type) {
		case 0:
			passiveEntityrespawnPoint = new VegetableGrower("pieczarka", "button mushroom");
			passiveEntityrespawnPoint.setDescription("Zauważyłeś małą pieczarkę rosnącą w ziemi.");
			break;
		case 1:
			passiveEntityrespawnPoint = new VegetableGrower("borowik", "porcini");
			passiveEntityrespawnPoint.setDescription("Zauważyłeś małego borowika rosnącego w ziemi.");
			break;
		case 2:
			passiveEntityrespawnPoint = new VegetableGrower("muchomor", "toadstool");
			passiveEntityrespawnPoint.setDescription("Zauważyłeś małego muchomora rosnącego w ziemi.");
			break;
		default:
			passiveEntityrespawnPoint = null;
			break;
		}
		return passiveEntityrespawnPoint;
	}

	private static PassiveEntityRespawnPoint createHerb(final int type) {
		PassiveEntityRespawnPoint passiveEntityrespawnPoint;
		switch (type) {
		case 0:
			passiveEntityrespawnPoint = new VegetableGrower("arandula", "arandula");
			break;
		case 1:
			passiveEntityrespawnPoint = new VegetableGrower("kekik", "kekik");
			break;
		case 2:
			passiveEntityrespawnPoint = new VegetableGrower("sclaria", "sclaria");
			break;
		case 3:
			passiveEntityrespawnPoint = new VegetableGrower("mandragora", "mandragora");
			break;
		case 4:
			passiveEntityrespawnPoint = new PassiveEntityRespawnPoint("mech renifera", 300);
			passiveEntityrespawnPoint
					.setDescription("Oto miejsce gdzie rośnie mech renifera.");
			break;
		default:
			passiveEntityrespawnPoint = null;
			break;
		}
		return passiveEntityrespawnPoint;
	}
	
	private static PassiveEntityRespawnPoint createGrain(final int type) {
		PassiveEntityRespawnPoint passiveEntityrespawnPoint;
		switch (type) {
		case 0:
			passiveEntityrespawnPoint = new GrainField("zboże", "grain", Arrays.asList("kosa", "kosa pordzewiała", "kosa czarna"));
			break;

		case 1:
			passiveEntityrespawnPoint = new GrainField("trzcina cukrowa", "sugar cane", Arrays.asList("sierp"));
			break;
		default:
			passiveEntityrespawnPoint = null;
			break;
		}
		return passiveEntityrespawnPoint;
	}
}
