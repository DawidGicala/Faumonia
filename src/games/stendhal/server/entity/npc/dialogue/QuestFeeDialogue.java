package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.logic.QuestGoldFees;
import java.util.*;

final class QuestFeeDialogue {
    static Map<String,PilotDialogueReplies.Choice> decorate(Map<String,PilotDialogueReplies.Choice> choices,SpeakerNPC npc,Player p){
        Map<String,PilotDialogueReplies.Choice> result=new LinkedHashMap<String,PilotDialogueReplies.Choice>(choices);
        boolean quiz = "Santiago".equals(npc.getName())
                && npc.getEngine().getCurrentState()==games.stendhal.server.entity.npc.ConversationStates.QUESTION_1;
        if (!quiz && !QuestGoldFees.information(npc,p).isEmpty()) {
            QuestGoldFees.installInformation(npc);
            if (npc.getEngine().canHandle(p,"koszty zadań")) {
                result.put("fee_information",new PilotDialogueReplies.Choice("fee_information",
                        "Za co mam zapłacić i ile to kosztuje?","koszty zadań","money"));
            }
        }
        for(QuestGoldFees.Fee f:QuestGoldFees.pending(npc,p)){
            String id="give_fee_"+f.id+"_"+f.price(p);
            result.put(id,new PilotDialogueReplies.Choice(id,"Płacę "+f.price(p)+" monet za "+f.purpose+".",f.command(),"money"));
        }
        return result;
    }
    static void append(List<NpcDialogueSnapshot.Item> rows,SpeakerNPC npc,Player p){
        List<QuestGoldFees.Fee> visible=new ArrayList<QuestGoldFees.Fee>(QuestGoldFees.pending(npc,p));
        games.stendhal.server.entity.npc.ConversationStates state=npc.getEngine().getCurrentState();
        if (state==games.stendhal.server.entity.npc.ConversationStates.QUEST_OFFERED
                || state==games.stendhal.server.entity.npc.ConversationStates.QUEST_2_OFFERED
                || "koszty zadań".equals(NpcDialogueService.lastCommand(npc,p)) || AncientCloakDialogue.active(npc,p)) {
            for (QuestGoldFees.Fee f:QuestGoldFees.information(npc,p)) {
                if (!f.paid(p) && !visible.contains(f) && (!AncientCloakDialogue.active(npc,p) || f.id==16)) { visible.add(f); }
            }
        }
        for(QuestGoldFees.Fee f:visible){
            games.stendhal.server.entity.item.Item money=games.stendhal.server.core.engine.SingletonRepository.getEntityManager().getItem("money");
            if(money!=null)rows.add(new NpcDialogueSnapshot.Item("fee_"+f.id,"Opłata: "+f.purpose,
                "items/"+money.getItemClass()+"/"+money.getItemSubclass(),f.price(p),p.getNumberOfEquipped("money"),-1,100,0,
                f.pending(p)?"give_fee_"+f.id+"_"+f.price(p):"fee_information"));
        }
    }
}
