package games.stendhal.server.entity.npc.dialogue;
import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.common.QuestProgress;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.quest.*;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import java.util.*;
final class CollectionDialogueItems {
    static void append(List<NpcDialogueSnapshot.Item> rows,SpeakerNPC npc,Player p){
        Set<String> shown=new HashSet<String>();
        for(IQuest q:CollectionQuestProgress.forNpc(npc.getName())){
            QuestProgress progress=CollectionQuestProgress.read(q,p);
            if(progress!=null&&!Arrays.asList(QuestProgress.State.COMPLETED,QuestProgress.State.DECLINED,QuestProgress.State.UNKNOWN).contains(progress.getState())){
                for(QuestProgress.Objective o:progress.getObjectives()){
                    if(o.getId().startsWith("kill:")||o.getId().startsWith("solo:")){
                        String name=o.getId().substring(5);Creature c=SingletonRepository.getEntityManager().getCreature(name);String sprite=c!=null&&c.has("class")&&c.has("subclass")?"monsters/"+c.get("class")+"/"+c.get("subclass"):"gui/dialogue/attack_xp";
                        rows.add(new NpcDialogueSnapshot.Item("collection_kill_"+rows.size(),o.getLabel(),sprite,o.getRequired(),o.getCurrent(),-1,100,0,"deliver"));
                    }else if(shown.add(o.getId())){
                        if(q instanceof SuppliesForPhalk&&!"Phalk".equals(npc.getName()))continue;
                        int count=o.getCarried()>=0?o.getCarried():o.getCurrent();
                        add(rows,p,o.getId(),o.getLabel(),o.getRequired(),count,"give_"+o.getId());
                    }
                }
            }else if(npc.getEngine().getCurrentState()==ConversationStates.QUEST_OFFERED||npc.getEngine().getCurrentState()==ConversationStates.QUEST_2_OFFERED){
                if(q instanceof CloakCollector&&p.isQuestCompleted(q.getSlotName())||q instanceof WeaponsCollector&&p.isQuestCompleted(q.getSlotName()))continue;
                if(q instanceof CloakCollector2&&!p.isQuestCompleted("cloaks_collector")||q instanceof WeaponsCollector2&&!p.isQuestCompleted("weapons_collector"))continue;
                if(Arrays.asList("sequence","list","collection","count").contains(CollectionQuestProgress.kind(q)))for(Map.Entry<String,Integer> e:CollectionQuestProgress.details(q).getCollectionItems().entrySet())if(shown.add(e.getKey()))add(rows,p,e.getKey(),e.getKey(),e.getValue(),p.getNumberOfEquipped(e.getKey()),"accept");
            }
        }
        if(Arrays.asList("Wrvil","Mrotho").contains(npc.getName())&&"clothes".equals(p.getQuest("supplies_for_phalk",0))){
            String item="Wrvil".equals(npc.getName())?"strzała żelazna":"żelazo";int index="Wrvil".equals(npc.getName())?1:2;
            if("none".equals(p.getQuest("supplies_for_phalk",index)))add(rows,p,item,item,index==1?20:10,p.getNumberOfEquipped(item),"phalk");
        }
    }
    static void add(List<NpcDialogueSnapshot.Item> rows,Player p,String name,String label,int required,int carried,String reply){
        Item item=SingletonRepository.getEntityManager().getItem(name);if(item!=null)rows.add(new NpcDialogueSnapshot.Item("collection_"+rows.size(),label,"items/"+item.getItemClass()+"/"+item.getItemSubclass(),required,carried,-1,100,0,ResidentDialogueReplies.id(reply)));
    }
}
