package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.*;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.quest.*;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import java.util.*;
import marauroa.common.Pair;

final class HuntDialogueItems {
    private HuntDialogueItems() { }
    static void append(List<NpcDialogueSnapshot.Item> rows,SpeakerNPC n,Player p){
        IQuest q=HuntQuestProgress.forNpc(n.getName());if(q==null)return;
        QuestProgress progress=HuntQuestProgress.read(q,p);
        if(progress!=null&&(progress.getState()==QuestProgress.State.ACTIVE||progress.getState()==QuestProgress.State.READY_TO_DELIVER)){
            for(QuestProgress.Objective goal:progress.getObjectives()){
                if(goal.getId().startsWith("kill:")||goal.getId().startsWith("solo:"))creature(rows,goal.getId().substring(5),goal.getLabel(),goal.getRequired(),goal.getCurrent(),false);
                else if(goal.getId().equals("hunt_total"))rows.add(new NpcDialogueSnapshot.Item("hunt_total",goal.getLabel(),"gui/dialogue/attack_xp",goal.getRequired(),goal.getCurrent(),-1,100,0,"supplies"));
                else JourneyDialogueItems.add(rows,p,goal.getId(),goal.getRequired(),"supplies");
            }
            if(q instanceof NativeHuntDetails&&((NativeHuntDetails)q).getDialogueHuntTotal(p)>0)
                for(String name:((NativeHuntDetails)q).getDialogueHuntTargets(p).keySet())creature(rows,name,name,0,0,true);
        }else if(n.getEngine().getCurrentState()==ConversationStates.QUEST_OFFERED&&q instanceof NativeHuntDetails){
            for(Map.Entry<String,Pair<Integer,Integer>> e:((NativeHuntDetails)q).getDialogueHuntTargets(p).entrySet())
                creature(rows,e.getKey(),e.getKey(),e.getValue().first()+e.getValue().second(),0,false);
        }
    }
    static void creature(List<NpcDialogueSnapshot.Item> rows,String name,String label,int required,int current,boolean member){
        Creature c=SingletonRepository.getEntityManager().getCreature(name);
        // Optional members removed from world data are not suggested as hunting locations.
        // Their saved counters remain intact; required targets must always stay visible.
        if(member&&c==null)return;
        // Keep required targets visible even if a resource is temporarily unavailable.
        // A shipped sprite can still represent an existing saved objective.
        String sprite=c!=null&&c.has("class")&&c.has("subclass")?"monsters/"+c.get("class")+"/"+c.get("subclass")
            : "arachne".equals(name)?"monsters/hybrid/arachne":"gui/dialogue/attack_xp";
        rows.add(new NpcDialogueSnapshot.Item((member?"hunt_member_":"hunt_kill_")+rows.size(),label,sprite,required,current,-1,100,0,"supplies"));
    }
}
