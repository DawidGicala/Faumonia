// $Id: ShoutBehaviour.java,v 1.3 2010/08/08 15:52:56 kymara Exp $
package games.stendhal.server.entity.npc.behaviour.impl;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.common.NotificationType;

/**
 * causes the speaker npc to loop a repeated monologue while he is not attending a player.
 * the text for repeating can have more than one option, in which case he says each in turn.
 * 
 * @author kymara
 */
public final class ShoutBehaviour implements TurnListener {


	private final SpeakerNPC speakerNPC;
	private final String[] repeatedText;
	private int i = 0;
	private int minutes;
	
	/**
	 * Creates a new ShoutBehaviour.
	 * 
	 * @param speakerNPC
	 *            SpeakerNPC
	 * @param repeatedText
	 *            text to repeat
	 * @param minutes
	 * 			  after how many minutes to repeat text
	 */
	public ShoutBehaviour(final SpeakerNPC speakerNPC,
			final String[] repeatedText, final int minutes) {
		this.speakerNPC = speakerNPC;
		this.repeatedText = repeatedText;
		this.minutes = minutes;
		SingletonRepository.getTurnNotifier().notifyInTurns(1, this);
	}

	public void onTurnReached(final int currentTurn) {
		if (speakerNPC.getEngine().getCurrentState() == ConversationStates.IDLE) {
			String tekst = repeatedText[i % repeatedText.length];
			SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG, "Ogłoszenie: " + tekst);
			speakerNPC.setCurrentState(ConversationStates.IDLE);
			if (i == Integer.MAX_VALUE) {
				// deal with overflow (only takes 9 hours :P)
				// probably means there is a better way to do it, but this should work...
				i = 0;
			} else { 
				i = i + 1;
			}
		}
		// Schedule so we are notified again in 
		SingletonRepository.getTurnNotifier().notifyInSeconds(minutes*60, this);
	}
}
