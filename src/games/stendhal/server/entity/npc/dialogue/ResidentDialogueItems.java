package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.common.QuestProgress;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.quest.ResidentQuestProgress;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import java.util.*;

final class ResidentDialogueItems {
    private ResidentDialogueItems() { }
    static void append(List<NpcDialogueSnapshot.Item> rows,SpeakerNPC npc,Player player){
        IQuest q=ResidentQuestProgress.forNpc(npc.getName());
        if("Hackim Easso".equals(npc.getName())&&Arrays.asList("hackim","pies").contains(player.getQuest("mrsyeti",0)))q=new HelpMrsYeti();
        if(q!=null){
            QuestProgress progress=ResidentQuestProgress.read(q,player);
            if(progress!=null&&progress.getState()!=QuestProgress.State.COMPLETED&&progress.getState()!=QuestProgress.State.DECLINED){
                for(QuestProgress.Objective o:progress.getObjectives()){
                    if(q instanceof SheepGrowing){
                        rows.add(new NpcDialogueSnapshot.Item("resident_sheep","Waga owcy","sheep",100,o.getCurrent(),-1,100,0,"give_sheep"));continue;
                    }
                    if(q instanceof FindRatChildren){
                        SpeakerNPC child=games.stendhal.server.entity.npc.NPCList.get().get(Character.toUpperCase(o.getId().charAt(0))+o.getId().substring(1));
                        if(child!=null&&child.has("class"))rows.add(new NpcDialogueSnapshot.Item("resident_"+o.getId(),child.getName(),"npc/"+child.get("class"),0,Math.max(0,o.getCarried()),-1,100,0,"give_"+o.getId()));
                        continue;
                    }
                    int needed=o.getCarried()>=0?o.getRequired()-o.getCurrent():o.getRequired();
                    if(needed>0)add(rows,player,o.getId(),needed,"give_"+o.getId());
                }
            }else if(npc.getEngine().getCurrentState()==ConversationStates.QUEST_OFFERED){
                for(Map.Entry<String,Integer> e:ResidentQuestProgress.requirements(q).entrySet())add(rows,player,e.getKey(),e.getValue(),"give_"+e.getKey());
            }
        }
        if("tomi".equals(npc.getName()))add(rows,player,"miecz lodowy",1,"give_sword");
        if("Mrs. Yeti".equals(npc.getName())&&player.isQuestInState("mrsyeti","dragon")){
            games.stendhal.server.entity.creature.Pet pet=player.getPet();
            rows.add(new NpcDialogueSnapshot.Item("resident_dragon","Mały smok","baby_dragon",1,pet!=null&&"baby_dragon".equals(pet.get("type"))?1:0,-1,100,0,"give_dragon"));
        }
        if(npc.getName().equals(player.getQuest("pizza_delivery",0)))add(rows,player,"pizza",1,"give_pizza");
        // Link each unique delivery row to the explicit action icon where the label is not the item name.
        Map<String,String> ids=new HashMap<String,String>();
        ids.put("flasza","give_flask");ids.put("arandula","give_arandula");ids.put("węgiel","give_coal");
        ids.put("tabliczka czekolady","give_sweet");ids.put("lody","give_sweet");ids.put("zupa rybna","give_soup");
        ids.put("olejek do opalania","give_cream");ids.put("eliksir miłości","give_potion");ids.put("mieczyk","give_knife");ids.put("tarta","give_tarts");
        for(int i=0;i<rows.size();i++){
            NpcDialogueSnapshot.Item r=rows.get(i);String id=ids.get(r.getName());
            if(id!=null&&r.getId().startsWith("resident_"))rows.set(i,new NpcDialogueSnapshot.Item(r.getId(),r.getName(),r.getSprite(),r.getRequired(),r.getOwned(),-1,100,0,id));
        }
    }
    private static void add(List<NpcDialogueSnapshot.Item> rows,Player p,String name,int required,String reply){
        Item item=SingletonRepository.getEntityManager().getItem(name);if(item==null)return;
        rows.add(new NpcDialogueSnapshot.Item("resident_"+rows.size(),name,"items/"+item.getItemClass()+"/"+item.getItemSubclass(),required,p.getNumberOfEquipped(name),-1,100,0,ResidentDialogueReplies.id(reply)));
    }
}
