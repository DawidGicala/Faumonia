/* $Id: CommonPassword.java,v 1.4 2010/12/19 16:58:10 nhnb Exp $ */
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
package games.stendhal.server.core.account;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import marauroa.common.game.Result;

/**
 * checks the password against a list of common passwords
 *
 * @author hendrik
 */
public class CommonPassword implements AccountParameterValidator {
	private List<String> commonPasswords = Arrays.asList(
			"stendhal", "stendhal1", 
			"password", "password1", 
			"passwort", "passwort1", 
			"arianne", "marauroa",
			"112233", "123123", "12345", "123456", "1234567", "12345678", "123456789", "1234567890",
			"jesus", "love", "game", "letmein", 
			"qwerty", "qwertz", "monkey", "test", "master", "killer",
			"abc123", "fuckyou", "asdfg", "fucku", "test",
			"kurwa", "kurwamac", "kurwa mac", "cipa", "chuj", "huj",
			"FaumoniaOnline", "FaumoniaOnline1", "FaumoniaOnline2",
			"pol1", "pol2", "polpol"
		);

	private String parameterValue;

	public CommonPassword(String parameterValue) {
		this.parameterValue = parameterValue;
	}

	public Result validate() {
		if (commonPasswords.contains(parameterValue.toLowerCase(Locale.ENGLISH))) {
			return Result.FAILED_PASSWORD_TO_WEAK;
		}
		return null;
	}

}
