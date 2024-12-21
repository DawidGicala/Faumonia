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
package marauroa.common.net.message;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import marauroa.common.net.NetConst;

/**
 * This message indicate the client that the server has reject its login Message
 *
 * @see marauroa.common.net.message.Message
 */

public class MessageS2CLoginNACK extends Message {

	public enum Reasons {
		USERNAME_WRONG, 
		/** @since will be replaced by TOO_MANY_TRIES_USERNAME and TOO_MANY_TRIES_IP in the future */
		TOO_MANY_TRIES, 
		USERNAME_BANNED, 
		SERVER_IS_FULL, 
		GAME_MISMATCH, 
		PROTOCOL_MISMATCH, 
		INVALID_NONCE,
		/** @since 3.0 */
		USERNAME_INACTIVE,
		/** @since 3.0 */
		TOO_MANY_TRIES_USERNAME, 
		/** @since 3.0 */
		TOO_MANY_TRIES_IP,
		/** @since 3.7 */
		SEED_WRONG,
		/** @since 3.7.1 */
		ACCOUNT_MERGED;
	}

	static private String[] text = {
	        "Niepoprawna nazwa u\u017Cytkownika/has\u0142a.",
	        "Wyst\u0105pi\u0142o zbyt wiele niepoprawnych pr\u00F3b zalogowania do twojego konta lub z twojej sieci. "
	        	+ "Odczekaj kilka minut lub skontaktuj si\u0119 ze wsparciem.",
		    "Konto jest zablokowane.",
	        "Serwer jest pe\u0142ny.",
	        "Serwer dzia\u0142a na niekompatybilnej wersji gry. Zaktualizuj.",
	        "marauroa.common.network Nieprawid\u0142owa wersja protoko\u0142u: Uruchomiony "
	                + Integer.toString(NetConst.NETWORK_PROTOCOL_VERSION),
	        "Hash, kt\u00F3ry wysy\u0142asz jest nie zgodny z tym co wys\u0142a\u0142e\u015B wcze\u015Bniej.",
	        "Twoje konto zosta\u0142o oznaczone jako nieaktywne. Skontaktuj si\u0119 ze wsparciem.",
	        "Wyst\u0105pi\u0142o zbyt wiele nieudanych pr\u00F3b zalogowania do twojego konta. "
        	+ "Odczekaj kilka minut lub skontaktuj si\u0119 ze wsparciem.",
	        "Wyst\u0105pi\u0142o zbyt wiele nieudanych pr\u00F3b zalogowania z twojej sieci. "
        	+ "Odczekaj kilka minut lub skontaktuj si\u0119 ze wsparciem.",
        	"Nie powid\u0142o si\u0119 wst\u0119pne uwierzytelnienie. Spr\u00F3buj ponownie.",
        	"To konto zosta\u0142o po\u0142\u0105czone z innym kontem. U\u017Cyj loginu "
        	+"innego konta, aby zalogowa\u0107 lub skontaktuj si\u0119 ze wsparciem."};

	/** The reason of login rejection */
	private Reasons reason;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CLoginNACK() {
		super(MessageType.S2C_LOGIN_NACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param resolution
	 *            the reason to deny the login
	 */
	public MessageS2CLoginNACK(SocketChannel source, Reasons resolution) {
		super(MessageType.S2C_LOGIN_NACK, source);
		reason = resolution;
	}

	/**
	 * This method returns the resolution of the login event
	 *
	 * @return a byte representing the resolution given.
	 */
	public Reasons getResolutionCode() {
		return reason;
	}

	/**
	 * This method returns a String that represent the resolution given to the
	 * login event
	 *
	 * @return a string representing the resolution.
	 */
	public String getResolution() {
		return text[reason.ordinal()];
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C Login NACK) from (" + getAddress() + ") CONTENTS: (" + getResolution()
		        + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write((byte) reason.ordinal());
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		reason = Reasons.values()[in.readByte()];

		if (type != MessageType.S2C_LOGIN_NACK) {
			throw new IOException();
		}
	}
}
