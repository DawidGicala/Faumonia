/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.game;

/**
 * This enum represent the possible values returned by the create account
 * process. Caller should verify that the process ended in OK_ACCOUNT_CREATED.
 * 
 * @author miguel
 */
public enum Result {

	/** Account was created correctly. */
	OK_CREATED(true, "Konto zosta\u0142o utworzone."),

	/**
	 * Account was not created because one of the important parameters was
	 * missing.
	 */
	FAILED_EMPTY_STRING(false, "Konto nie zota\u0142o utworzone poniewa\u017C brakuje jednego z wa\u017Cnych parametr\u00F3w."),

	/**
	 * Account was not created because an invalid characters (letter, sign,
	 * number) was used
	 */
	FAILED_INVALID_CHARACTER_USED (false, "Konto nie zosta\u0142o utworzone poniewa\u017C zosta\u0142y u\u017Cyte nieprawid\u0142owe znaki (litera, znak, numer)."),

	/**
	 * Account was not created because any of the parameters are either too long
	 * or too short.
	 */
	FAILED_STRING_SIZE(false, "Konto nie zosta\u0142o utworzone poniewa\u017C jakie\u015B parametry s\u0105 za kr\u00F3tkie lub za d\u0142ugie."),

	/** Account was not created because this account already exists. */
	FAILED_PLAYER_EXISTS(false, "Konto nie zosta\u0142o utworzone poniewa\u017C ju\u017C istnieje."),

	/** Account was not created because there was an unspecified exception. */
	FAILED_EXCEPTION(false, "Konto nie zosta\u0142o utworzone poniewa\u017C wyst\u0105pi\u0142 nieokre\u015Blony wyj\u0105tek."),

	/** Character was not created because this character already exists. */
	FAILED_CHARACTER_EXISTS(false, "Konto nie zosta\u0142o utworzne poniewa\u017C ju\u017C istnieje."),

	/**
	 * The template passed to the create character method is not valid because
	 * it fails to pass the RP rules.
	 */
	FAILED_INVALID_TEMPLATE(false, "Szablon wcze\u015Bniej u\u017Cyty do tworzenia postaci jest nieprawid\u0142owy poniewa\u017C nie zaliczy\u0142 zasad (formu\u0142y) zawartej w RP."),

	/**
	 * String is too short
	 *
	 * @since 2.1
	 */
	FAILED_STRING_TOO_SHORT(false, "Konto nie zosta\u0142o utworzone poniewa\u017C jeden z parametr\u00F3w by\u0142 zbyt kr\u00F3tki."),

	/**
	 * String is too long
	 *
	 * @since 2.1
	 */
	FAILED_STRING_TOO_LONG(false, "Konto nie zosta\u0142o utworzone poniewa\u017C jeden z parametr\u00F3w jest zbyt d\u0142ugi."),

	/**
	 * Name is reserved
	 *
	 * @since 2.1
	 */
	FAILED_RESERVED_NAME(false, "Konto nie zosta\u0142o utworzone poniewa\u017C nazwa jest zarezerwowana (lub zawiera zarezerwowan\u0105 nazw\u0119)."),

	/**
	 * Password is too close to the username
	 *
	 * @since 2.1
	 */
	FAILED_PASSWORD_TOO_CLOSE_TO_USERNAME(false, "Konto nie zosta\u0142o utworzone poniewa\u017C has\u0142o jest zbyt podobne do loginu lub nazwy wojownika."),

	/**
	 * Password is too weak.
	 *
	 * @since 2.1
	 */
	FAILED_PASSWORD_TO_WEAK(false, "Konto nie zosta\u0142o utworzone poniewa\u017C has\u0142o jest zbyt s\u0142abe."),

	/**
	 * Too many accounts were created from this ip-address recently.
	 *
	 * @since 3.5
	 */
	FAILED_TOO_MANY(false, "Konto nie zosta\u0142o utworzone poniewa\u017C limit tworzenia konta dla Twojej sieci zosta\u0142 osi\u0105gni\u0119ty.\nSpr\u00F3buj p\u00F3\u017Aniej."),

	/**
	 * Server is offline, obviously not returned by the server but can be generated client side.
	 *
	 * @since 3.8.2
	 */
	FAILED_OFFLINE(false, "Tworzenie nie powiod\u0142o si\u0119 poniewa\u017C serwer jest tymczasowo niedost\u0119pny. Spr\u00F3buj p\u00F3\u017Aniej."),

	/**
	 * This server does not accept account creation
	 *
	 * @since 3.8.4
	 */
	FAILED_CREATE_ON_MAIN_INSTEAD(false, "Konta nie mog\u0105 zosta\u0107 utworzone na tym serwerze. Utw\u00F3rz konto na g\u0142\u00F3wnym serwerze i poczekaj chwil\u0119.");

	/**
	 * Textual description of the result 
	 */
	private String text;

	/**
	 * True if the account is successfully created. 
	 */
	private boolean created;

	Result(boolean created, String text) {
		this.created=created;
		this.text=text;
	}

	/**
	 * checks state of account creation represented by this result.
	 *  
	 * @return  true if account creation failed. false if account creation was successful.
	 */
	public boolean failed() {
		return !created;
	}

	/**
	 * Returns a textual description of the result.
	 * @return a textual description of the result.
	 */
	public String getText() {
		return text;
	}
}
