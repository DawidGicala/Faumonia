package games.stendhal.server.entity.npc.dialogue;
import games.stendhal.server.core.rp.quest.*;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.maps.quests.IQuest;
public final class CollectionDialogueTopics {
    private CollectionDialogueTopics() { }
    public static void install(SpeakerNPC npc){
        if(CollectionQuestProgress.forNpc(npc.getName()).isEmpty())return;
        if(HuntQuestProgress.forNpc(npc.getName())==null)npc.add(ConversationStates.ATTENDING,"wątki",null,ConversationStates.ATTENDING,DialogueTopicText.forNpc(npc.getName()),null);
        npc.add(ConversationStates.ATTENDING,"warunki dostawy",null,ConversationStates.ATTENDING,null,(p,s,n)->{
            StringBuilder text=new StringBuilder();
            for(IQuest q:CollectionQuestProgress.forNpc(npc.getName())){
                if(CollectionQuestProgress.done(q,p))continue;
                if(q.getName().equals("CloakCollector2")&&!p.isQuestCompleted("cloaks_collector")||q.getName().equals("WeaponsCollector2")&&!p.isQuestCompleted("weapons_collector"))continue;
                if(text.length()>0)text.append(" ");text.append(CollectionQuestProgress.progressText(q,p));
            }
            n.say(text.length()>0?text.toString():"Nic więcej nie potrzebuję. Dziękuję za pomoc. O co jeszcze chcesz zapytać?");
        });
    }
}
