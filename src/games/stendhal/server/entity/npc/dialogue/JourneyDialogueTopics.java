package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.QuestProgress;
import games.stendhal.server.core.rp.quest.JourneyQuestProgress;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.maps.quests.IQuest;

/** Informational continuations; this class never grants items or updates a quest. */
public final class JourneyDialogueTopics {
    private JourneyDialogueTopics() { }
    public static void install(SpeakerNPC npc){
        npc.add(ConversationStates.ATTENDING,"wątki",null,ConversationStates.ATTENDING,
                DialogueTopicText.forNpc(npc.getName()),null);
        npc.add(ConversationStates.ATTENDING,"wskazówki wyprawy",null,ConversationStates.ATTENDING,null,(p,s,n)->{
            StringBuilder text=new StringBuilder();
            for(IQuest q:JourneyQuestProgress.forNpc(npc.getName())){
                QuestProgress progress=JourneyQuestProgress.read(q,p);
                if(progress==null||progress.getState()==QuestProgress.State.DECLINED||progress.getState()==QuestProgress.State.COMPLETED)continue;
                if(text.length()>0)text.append("\n\n");text.append(progress.getNextStep());
                if(!progress.getObjectives().isEmpty()){
                    text.append("\nPozostało: ");boolean first=true;
                    for(QuestProgress.Objective o:progress.getObjectives())if(o.getCurrent()<o.getRequired()){
                        if(!first)text.append(", ");first=false;text.append(o.getLabel()).append(" - ").append(o.getRequired()-o.getCurrent());
                    }
                    if(first)text.append("wszystkie wymagania z listy są spełnione");text.append(".");
                }
            }
            n.say(text.length()==0?"Na razie nie czekam na nic od ciebie. Jeśli chcesz pomóc, zapytaj o zadanie.":text.toString());
        });
    }
}
