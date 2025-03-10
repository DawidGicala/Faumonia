package games.stendhal.server.entity.npc.interaction;

import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.List;
import java.util.Observable;
import java.util.Observer;


/**
 * chatting between 2 NPCs
 * @author yoriy
 */
public final class NPCChatting implements Observer, TurnListener {
	private final SpeakerNPC first;
	private final SpeakerNPC second;
	private int count=0;
	private final List<String> conversations; 
	final private Observer next;
	final String explainations;

	
	/**
	 * constructor
	 * @param first - first npc (who strarting conversation)
	 * @param second - second npc
	 * @param n - observer n
	 */
	public NPCChatting(
			SpeakerNPC first, 
			SpeakerNPC second, 
			List<String> conversations, 
			String explainations, 
			Observer n) {
		this.first=first;
		this.second=second;
		this.conversations=conversations;
		this.explainations=explainations;
		this.next=n;

	}
	
	private void setupDialog() {
		if(second.isTalking()) {
			second.say("Przepraszam "+second.getAttending().getName()+
					", ale "+explainations);
			second.setCurrentState(ConversationStates.IDLE);
		}
		second.setCurrentState(ConversationStates.ATTENDING);
		second.setAttending(first);
		second.stop();
		onTurnReached(0);
	}
	
	public void update(Observable o, Object arg) {
		first.clearPath();
		first.pathnotifier.deleteObservers();
		count=0;
		setupDialog();
	}

	public void onTurnReached(int currentTurn) {
		first.faceToward(second);
		second.faceToward(first);
		if((count%2)==0) {
			first.say(conversations.get(count));
		} else {
			second.say(conversations.get(count));
		}
		count++;
		if(count==conversations.size()) {
			TurnNotifier.get().dontNotify(this);
			second.setCurrentState(ConversationStates.IDLE);
			second.followPath();
            // going ahead
			next.update(null, null);
			return;
		}
		TurnNotifier.get().dontNotify(this);
		TurnNotifier.get().notifyInSeconds(8, this);
	}
}	


