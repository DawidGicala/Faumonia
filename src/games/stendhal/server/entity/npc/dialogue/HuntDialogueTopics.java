package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.server.core.rp.quest.HuntQuestProgress;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.maps.quests.IQuest;

public final class HuntDialogueTopics {
    private HuntDialogueTopics() { }
    public static void install(SpeakerNPC npc){
        final IQuest quest=HuntQuestProgress.forNpc(npc.getName());if(quest==null)return;
        npc.add(ConversationStates.ATTENDING,"wątki",null,ConversationStates.ATTENDING,
            DialogueTopicText.forNpc(npc.getName()),null);
        npc.add(ConversationStates.ATTENDING,"wskazówki wyprawy",null,ConversationStates.ATTENDING,null,
            (p,s,n)->n.say(HuntQuestProgress.progressText(quest,p)));
    }
}
