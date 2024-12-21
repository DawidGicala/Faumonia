/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.bot.postman;

import java.util.Date;
import java.util.StringTokenizer;

import marauroa.client.ClientFramework;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

import org.apache.log4j.Logger;

/**
 * Postman.
 *
 * @author hendrik
 */
public class Postman {

	private static final String Y_COORD = "4";
	private static final String X_COORD = "5";
	private static final String POSTMAN_ZONE = "int_zakopane_postoffice";

	private static final String GREETING = "Witam jestem listonoszem. W czym mog\u0119 #pom\u00F3c?";
	private static final String INTRO = "Przechowuje wiadomo\u015Bci dla niedost\u0119pnych wojownik\u00F3w i dostarczam je gdy ju\u017C si\u0119 zaloguj\u0105.\n";
	private static final String HELP_MESSAGE = "U\u017Cyj:\n/msg postman help \t\t To wiadomo\u015B\u0107 pomocy\n/msg postman tell #wojownik #wiadomo\u015B\u0107 \t Dostarcz\u0119 twoj\u0105 #wiadomo\u015B\u0107, gdy podany #wojownik zaloguje si\u0119 do gry.";

	private static Logger logger = Logger.getLogger(Postman.class);
	private final ClientFramework clientManager;
	private String lastShout;
	private static Postman instance;

	/**
	 * Creates a new postman.
	 * 
	 * @param clientManager
	 *            ClientManager
	 * @param postmanIRC
	 *            postmanIRC
	 */
	public Postman(final ClientFramework clientManager) {
		this.clientManager = clientManager;
		instance = this;
	}


	/**
	 * Processes a talk event.
	 * 
	 * @param object
	 *            the talking person
	 */
	public void processPublicTalkEvent(final RPObject object) {

		try {
			if (object == null) {
				return;
			}
			final int xdiff = object.getInt("x") - Integer.parseInt(X_COORD);
			final int ydiff = object.getInt("y") - Integer.parseInt(Y_COORD);
			if (xdiff * xdiff + ydiff * ydiff > 2) {
				logger.debug("***Postman*** object x: " + object.getInt("x") + ", object y: " + object.getInt("y")
							 + ", postman x " + Integer.parseInt(X_COORD) + ", postman y: " + Integer.parseInt(Y_COORD)
							 + ", xdiff: " + xdiff + ", ydiff:  " + ydiff);
				return;
			}

			if (object.getRPClass().getName().equals("player")
					&& object.has("name")) {
				if (object.has("text")) {
					if (!object.get("name").equals("postman")) {
						final String text = object.get("text");
						String playerName = "";
						playerName = object.get("name");

						final java.text.Format formatter = new java.text.SimpleDateFormat(
								"[HH:mm:ss] ");
						final String dateString = formatter.format(new Date());
						System.err.println(dateString + playerName + ": "
								+ text);

						final StringTokenizer st = new StringTokenizer(text, " ");
						String cmd = "";
						if (st.hasMoreTokens()) {
							cmd = st.nextToken();
						}
						if (cmd.equalsIgnoreCase("hi")
								|| cmd.equalsIgnoreCase("hello")
								|| cmd.equalsIgnoreCase("hallo")
								|| cmd.equalsIgnoreCase("greetings")
								|| cmd.equalsIgnoreCase("hola")
								|| cmd.equalsIgnoreCase("cze\u015B\u0107")) {
							chat(GREETING);
						} else if (cmd.equalsIgnoreCase("bye")
								|| cmd.equalsIgnoreCase("goodbye")
								|| cmd.equalsIgnoreCase("farewell")
								|| cmd.equalsIgnoreCase("cya")
								|| cmd.equalsIgnoreCase("adios")
								|| cmd.equalsIgnoreCase("dowidzenia")) {
							chat("Dowidzenia.");
						} else if (cmd.equalsIgnoreCase("help")
								|| cmd.equalsIgnoreCase("pom\u00F3c")
								|| cmd.equalsIgnoreCase("pomoc")
								|| cmd.equalsIgnoreCase("pomocy")
								|| cmd.equalsIgnoreCase("info")
								|| cmd.equalsIgnoreCase("job")
								|| cmd.equalsIgnoreCase("praca")
								|| cmd.equalsIgnoreCase("offer")
								|| cmd.equalsIgnoreCase("oferta")
								|| cmd.equalsIgnoreCase("letter")
								|| cmd.equalsIgnoreCase("list")
								|| cmd.equalsIgnoreCase("parcel")) {
							chat(INTRO + HELP_MESSAGE);
						} else if (cmd.equalsIgnoreCase("msg")
								|| cmd.equalsIgnoreCase("tell")) {
							onTell(playerName, st);
						}
					}
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error(e, e);
		}
	}

	/**
	 * Processes a talk event.
	 * 
	 * @param object
	 *            RPObject
	 * @param texttype
	 *            texttype
	 * @param text
	 *            text
	 */
	public void processPrivateTalkEvent(final RPObject object, final String texttype, final String text) {

		try {
			if (object == null) {
				return;
			}
			if (object.getRPClass().getName().equals("player")
					&& object.has("name")) {
				if (object.get("name").equals("postman")) {

					final java.text.Format formatter = new java.text.SimpleDateFormat(
							"[HH:mm:ss] ");
					final String dateString = formatter.format(new Date());
					System.err.println(dateString + text);

					final StringTokenizer st = new StringTokenizer(text, " ");
					final String from = st.nextToken();

					// tells
					final String arianneCmd = st.nextToken();

					// you:
					st.nextToken(); 

					if (arianneCmd.equals("tells") 
						|| arianneCmd.equals("powiedzia\u0142")) {
						// Command was send by a player
						// cmd
						String cmd = st.nextToken();
						if (cmd.startsWith("/")) {
							cmd = cmd.substring(1);
						}
						if (cmd.equalsIgnoreCase("tell")
								|| cmd.equalsIgnoreCase("msg")
								|| cmd.equalsIgnoreCase("/tell")
								|| cmd.equalsIgnoreCase("/msg")) {
							onTell(from, st);
						} else if (cmd.equalsIgnoreCase("hi")
								|| cmd.equalsIgnoreCase("cze\u015B\u0107")) {
							tell(from, GREETING);
						} else if (cmd.equalsIgnoreCase("help")
								|| cmd.equalsIgnoreCase("pom\u00F3c")
								|| cmd.equalsIgnoreCase("pomocy")
								|| cmd.equalsIgnoreCase("info")
								|| cmd.equalsIgnoreCase("job")
								|| cmd.equalsIgnoreCase("praca")
								|| cmd.equalsIgnoreCase("letter")
								|| cmd.equalsIgnoreCase("list")
								|| cmd.equalsIgnoreCase("offer")
								|| cmd.equalsIgnoreCase("oferta")
								|| cmd.equalsIgnoreCase("parcel")) {
							tell(from, INTRO + HELP_MESSAGE);
						} else {
							tell(from,
									"Przepraszam, ale nie rozumiem Ciebie. (Zapomnia\u0142e\u015B o \"tell\"?)\n"
											+ HELP_MESSAGE);
						}
					} else if (text.matches("Administrator SHOUTS:") || text.matches("Administrator KRZYCZY:")) {
					} else if (text.matches("[^:]* shouts: .*") || text.matches("[^:]* krzyczy: .*")) {
						if (!text.equals(lastShout)) {
							lastShout = text;
						}
					} else if (text.matches("[^:]* rented .*") || text.matches("[^:]* wynaj\u0119ty .*")) {
						// for signs
					} else if (texttype.equalsIgnoreCase("support")) {
							dumpPlayerPosition();
					}
				}

			}
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error(e, e);
		}
	}

	private void onTell(final String from, final StringTokenizer st) {
		String param = null;
		String msg = "";
		// System.err.println("!" + from + "! !" + cmd + "! !" + msg + "!");
		if (st.hasMoreTokens()) {
			
			// player
			param = st.nextToken();
		}
		if (st.hasMoreTokens()) {
			// the rest of the message
			msg = st.nextToken("\0").trim();
		}

		final RPAction action = new RPAction();
		action.put("type", "storemessageonbehalfofplayer");
		action.put("source", from);
		action.put("target", param);
		action.put("text", msg);
		clientManager.send(action);
	}

	private void tell(final String to, final String message) {
		if (to.equals("postman")) {
			logger.warn("I am not speaking to myself: " + message);
			return;
		}
		final RPAction tell = new RPAction();
		tell.put("type", "tell");
		tell.put("target", to);
		tell.put("text", message);
		clientManager.send(tell);
	}

	private void chat(final String message) {
		final RPAction chat = new RPAction();
		chat.put("type", "chat");
		chat.put("text", message);
		clientManager.send(chat);
	}

	/**
	 * teleports postman to his favorite spot
	 */
	void teleportPostman() {
		final RPAction teleport = new RPAction();
		teleport.put("type", "teleport");
		teleport.put("target", "postman");
		teleport.put("zone", POSTMAN_ZONE);
		teleport.put("x", X_COORD);
		teleport.put("y", Y_COORD);
		clientManager.send(teleport);
	}

	private void dumpPlayerPosition() {
		final RPAction chat = new RPAction();
		chat.put("type", "script");
		chat.put("target", "PlayerPositionMonitoring.class");
		clientManager.send(chat);
	}

	/**
	 * gets the postman intance
	 *
	 * @return Postman
	 */
	public static Postman get() {
		return instance;
	}


	/**
	 * sends a support answer
	 *
	 * @param sender  sender
	 * @param target  target
	 * @param message message
	 */
	public void supportAnswer(String sender, String target, String message) {
		final RPAction tell = new RPAction();

		tell.put("type", "supportanswer");
		tell.put("sender", sender);
		tell.put("target", target);
		tell.put("text", message);
		clientManager.send(tell);
	}

	/**
	 * sends a support answer
	 *
	 * @param sender  sender
	 * @param target  target
	 * @param hours   hours
	 * @param message message
	 */
	public void ban(String sender, String target, String hours, String message) {
		final RPAction ban = new RPAction();

		ban.put("type", "ban");
		ban.put("sender", sender);
		ban.put("target", target);
		ban.put("hours", hours);
		ban.put("reason", message);
		clientManager.send(ban);
	}

	/**
	 * sends a support answer
	 *
	 * @param sender  sender
	 * @param ip      ip
	 * @param mask    mask
	 * @param message message
	 */
	public void ipban(String sender, String ip, String mask, String message) {
		final RPAction ban = new RPAction();

		ban.put("type", "ipban");
		ban.put("sender", sender);
		ban.put("ip", ip);
		ban.put("mask", mask);
		ban.put("reason", message);
		clientManager.send(ban);
	}

	/**
	 * shouts as an NPC
	 *
	 * @param charname name of character
	 * @param target  target
	 * @param message message
	 */
	public void npcShout(String charname, String target, String message) {
		final RPAction action = new RPAction();
		action.put("type", "script");
		action.put("sender", charname);
		action.put("target", "NPCShout.class");
		action.put("args", target + " " + message);
		clientManager.send(action);
	}

	/**
	 * sends a support query
	 *
	 * @param charname name of character
	 * @param message message
	 */
	public void support(String charname, String message) {
		final RPAction action = new RPAction();
		action.put("type", "support");
		action.put("sender", charname);
		action.put("text", message);
		clientManager.send(action);
	}

	/**
	 * attaches an admin note
	 * 
	 * @param charname name of character
	 * @param target  target player
	 * @param message message
	 */
	public void adminNote(String charname, String target, String message) {
		final RPAction action = new RPAction();
		action.put("type", "adminnote");
		action.put("sender", charname);
		action.put("target", target);
		action.put("note", message);
		clientManager.send(action);
	}

	/**
	 * shouts as admin
	 * 
	 * @param charname name of character
	 * @param message message
	 */
	public void tellAll(String charname, String message) {
		final RPAction action = new RPAction();
		action.put("type", "tellall");
		action.put("sender", charname);
		action.put("text", message);
		clientManager.send(action);
	}
}
