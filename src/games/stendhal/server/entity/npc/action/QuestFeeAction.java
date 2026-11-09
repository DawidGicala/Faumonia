package games.stendhal.server.entity.npc.action;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.logic.QuestGoldFees;

/** Guards the entire completion transaction, before its reply, drops and rewards. */
public final class QuestFeeAction implements ChatAction {
    private final String slot;
    private final ChatAction action;
    public QuestFeeAction(String slot,ChatAction action){this.slot=slot;this.action=action;}
    public boolean prepare(Player p,EventRaiser npc){return QuestGoldFees.requirePaid(p,slot,npc);}
    public void fire(Player p,Sentence s,EventRaiser npc){if(prepare(p,npc))action.fire(p,s,npc);}
}
