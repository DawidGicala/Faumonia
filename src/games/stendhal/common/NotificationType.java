/* $Id: NotificationType.java,v 1.16 2012/05/30 18:50:04 kiheru Exp $ */
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
package games.stendhal.common;

import java.awt.Color;

//
//

/**
 * A logical notification type, which can be mapped to UI specific contexts.
 * This would be similar to logical styles vs. physical styles in HTML.
 */
public enum NotificationType {
	CLIENT("client") {
		@Override
		public Color getColor() {
			return COLOR_CLIENT;
		}
	},
	ERROR("error") {
		@Override
		public Color getColor() {
			return COLOR_ERROR;
		}
	},
	HEAL("heal") {
		@Override
		public Color getColor() {
			return COLOR_POSITIVE;
		}
		@Override
		public String getStyleDescription() {
			return REGULAR;
		}
	},
	INFORMATION("information") {
		@Override
		public Color getColor() {
			return COLOR_INFORMATION;
		}
	},
	NEGATIVE("negative") {
		@Override
		public Color getColor() {
			return COLOR_NEGATIVE;
		}
		@Override
		public String getStyleDescription() {
			return REGULAR;
		}
	},
	DAMAGE("negative") {
		@Override
		public Color getColor() {
			return COLOR_NEGATIVE;
		}
		@Override
		public String getStyleDescription() {
			return REGULAR;
		}
	},
	NORMAL("normal") {
		@Override
		public Color getColor() {
			return COLOR_NORMAL;
		}
	},
	NORMALBLACK("normalblack") {
		@Override
		public Color getColor() {
			return COLOR_NORMALBLACK;
		}
	},
	POISON("poison") {
		@Override
		public Color getColor() {
			return COLOR_NEGATIVE;
		}
		@Override
		public String getStyleDescription() {
			return REGULAR;
		}
	},
	POSITIVE("positive") {
		@Override
		public Color getColor() {
			return COLOR_POSITIVE;
		}
		@Override
		public String getStyleDescription() {
			return REGULAR;
		}
	},
	EMOTE("emote") {
		@Override
		public Color getColor() {
			return COLOR_EMOTE;
		}
	},
	GROUP("group") {
		@Override
		public Color getColor() {
			return COLOR_GROUP;
		}
	},
	PRIVMSG("privmsg") {
		@Override
		public Color getColor() {
			return COLOR_PRIVMSG;
		}
	},
	RESPONSE("response") {
		@Override
		public Color getColor() {
			return COLOR_RESPONSE;
		}
	},
	SCENE_SETTING("scene_setting") {
		@Override
		public Color getColor() {
			return COLOR_SCENE_SETTING;
		}
	},
	SERVER("server") {
		@Override
		public Color getColor() {
			return COLOR_PRIVMSG;
		}
	},
	SIGNIFICANT_NEGATIVE("significant_negative") {
		@Override
		public Color getColor() {
			return COLOR_SIGNIFICANT_NEGATIVE;
		}
	},
	SIGNIFICANT_POSITIVE("significant_positive") {
		@Override
		public Color getColor() {
			return COLOR_SIGNIFICANT_POSITIVE;
		}
	},
	TUTORIAL("tutorial") {
		@Override
		public Color getColor() {
			return COLOR_TUTORIAL;
		}
	},
	SUPPORT("support") {
		@Override
		public Color getColor() {
			return COLOR_SUPPORT;
		}
	},
	TELLALL("tellall") {
		@Override
		public Color getColor() {
			return COLOR_TELLALL;
		}
	},
	DETAILED("detailed") {
		@Override
		public String getStyleDescription() {
			return REGULAR;
		}
	};
	public static final Color COLOR_CLIENT = Color.gray;

	public static final Color COLOR_ERROR = new Color(255, 80, 80);

	public static final Color COLOR_INFORMATION = Color.orange;

	public static final Color COLOR_NEGATIVE = Color.red;

	public static final Color COLOR_NORMAL = new Color(200, 240, 240);

	public static final Color COLOR_NORMALBLACK = Color.black;

	public static final Color COLOR_POSITIVE = new Color(128, 255, 128);
	
	// dark blue
	public static final Color COLOR_GROUP = new Color(120, 120, 255);

	// muted purple
	public static final Color COLOR_EMOTE = new Color(220, 200, 160);

	public static final Color COLOR_PRIVMSG = new Color(200, 255, 200);

	// dark green
	public static final Color COLOR_RESPONSE = new Color(128, 255, 128);
	
	// dark brown 
	public static final Color COLOR_SCENE_SETTING = Color.gray;

	public static final Color COLOR_SIGNIFICANT_NEGATIVE = Color.pink;

	// bright turquoise blue
	public static final Color COLOR_SIGNIFICANT_POSITIVE = new Color(65,
			105, 225);

	// purple
	public static final Color COLOR_TUTORIAL = new Color(255, 220, 255);
	
	// strong bright orange
	public static final Color COLOR_SUPPORT = new Color(255, 220, 200);
	
	// strong bright yellow
	public static final Color COLOR_TELLALL = new Color(255, 255, 180);
	
	// TODO: review thinking here of using constants.
	// these are tied to the ones in client.KTextEdit.gui.initStylesForTextPane
	// so should we tie them together somehow?
	// also the definitions are crazy.
	
	// normal is bold
	public static final String NORMALSTYLE = "normal";
	// regular is not bold
	public static final String REGULAR = "regular";
	// fwiw, "bold" is blue, italic, bigger than normal, bold and blue.
	
	/**
	 * The mapping mnemonic.
	 */
	protected String mnemonic;

	/**
	 * Create a notification type.
	 *
	 * @param mnemonic
	 *            The mapping mnemonic.
	 */
	private NotificationType(final String mnemonic) {
		this.mnemonic = mnemonic;
	}

	//
	// NotificationType
	//

	/**
	 * Get the mapping mnemonic (programmatic name).
	 *
	 * @return The mapping mnemonic.
	 */
	public String getMnemonic() {
		return mnemonic;
	}

	/**
	 * Get the color that is tied to a notification type.
	 *
	 * @return The appropriate color.
	 */
	public Color getColor() {
					return COLOR_NORMAL;
	}

	/**
	 * Get the style that is tied to a notification type.
	 *
	 * @return The appropriate style.
	 */
	public String getStyleDescription() {
		return NORMALSTYLE;
	}

	/**
	 * Get notification type for server messages that the client can show
	 * without problems. Call this instead of using SERVER directly.
	 * 
	 * @param clientVersion
	 * @return appropriate type
	 */
	public static NotificationType getServerNotificationType(String clientVersion) {
		if ((clientVersion != null) && (Version.compare(clientVersion, "0.40") > 0)) {
			return NotificationType.SERVER;
		} else {
			return NotificationType.PRIVMSG;
		}
	}
}
