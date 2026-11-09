package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.common.QuestProgress;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.quest.JourneyQuestProgress;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import java.util.*;

final class JourneyDialogueItems {
    private JourneyDialogueItems() { }
    static void append(List<NpcDialogueSnapshot.Item> rows,SpeakerNPC npc,Player p){
        Item fish=FishermansLicenseQuiz.displayedFish(npc,p);
        if(fish!=null){
            rows.clear();rows.add(new NpcDialogueSnapshot.Item("exam_fish","Jaki to gatunek?","items/"+fish.getItemClass()+"/"+fish.getItemSubclass(),0,0,-1,100,0,""));return;
        }
        Set<String> shown=new HashSet<String>();
        for(IQuest q:JourneyQuestProgress.forNpc(npc.getName())){
            QuestProgress progress=JourneyQuestProgress.read(q,p);
            if(progress!=null&&progress.getState()!=QuestProgress.State.COMPLETED&&progress.getState()!=QuestProgress.State.DECLINED){
                for(QuestProgress.Objective o:progress.getObjectives()){
                    if(o.getId().startsWith("kill:")){
                        games.stendhal.server.entity.creature.Creature c=SingletonRepository.getEntityManager().getCreature(o.getId().substring(5));
                        if(c!=null&&c.has("class")&&c.has("subclass"))rows.add(new NpcDialogueSnapshot.Item("journey_kill",o.getLabel(),"monsters/"+c.get("class")+"/"+c.get("subclass"),1,o.getCurrent(),-1,100,0,"supplies"));
                    }else if(o.getId().startsWith("npc:")){
                        String name=o.getId().substring(4);if(!name.isEmpty())name=Character.toUpperCase(name.charAt(0))+name.substring(1);
                        SpeakerNPC other=NPCList.get().get(name);if(other!=null&&other.has("class"))rows.add(new NpcDialogueSnapshot.Item((q instanceof FindGhosts?"journey_news_":"journey_visit_")+ResidentDialogueReplies.id(name),name,"npc/"+other.get("class"),1,o.getCurrent(),-1,100,0,"give_"+ResidentDialogueReplies.id(name.toLowerCase(Locale.ROOT))));
                    }else if(shown.add(o.getId())){
                        int required=o.getCarried()>=0?o.getRequired()-o.getCurrent():o.getRequired();
                        if(required>0){
                            if(q instanceof KanmararnSoldiers){
                                Item item=SingletonRepository.getEntityManager().getItem(o.getId());
                                if(item!=null)rows.add(new NpcDialogueSnapshot.Item("journey_"+rows.size(),o.getLabel(),"items/"+item.getItemClass()+"/"+item.getItemSubclass(),required,o.getCurrent(),-1,100,0,"mapa".equals(o.getId())?"give_map":"supplies"));
                            }else add(rows,p,o.getId(),required,replyFor(npc,o.getId()));
                        }
                    }
                }
            }else if(npc.getEngine().getCurrentState()==ConversationStates.QUEST_OFFERED){for(Map.Entry<String,Integer> e:JourneyQuestProgress.requirements(q).entrySet())if(shown.add(e.getKey()))add(rows,p,e.getKey(),e.getValue(),"give_"+e.getKey());}
        }
        if("Ognir".equals(npc.getName())&&npc.getEngine().getCurrentState()==ConversationStates.QUEST_ITEM_BROUGHT){
            for(Map.Entry<String,Integer> e:JourneyQuestProgress.items("pierścień szmaragdowy",1,"money",100000,"sztabka złota",40,"szmaragd",10,"diament",1).entrySet())add(rows,p,e.getKey(),e.getValue(),"confirm");
        }
        if("Markovich".equals(npc.getName())&&p.isQuestInState("vs_quest","start")){
            if(p.isEquipped("czara")||(p.hasQuest("sicky_fill_goblet")&&!p.isQuestCompleted("sicky_fill_goblet"))){rows.clear();return;}
            rows.clear();for(Map.Entry<String,Integer> e:JourneyQuestProgress.items("pusta czara",1,"truchło wampira",7,"truchło nietoperza",7,"pierścień z czaszką",1).entrySet())add(rows,p,e.getKey(),e.getValue(),"fill");
        }
    }
    private static String replyFor(SpeakerNPC npc,String item){
        if("Ceryl".equals(npc.getName())||"Jynath".equals(npc.getName()))return "give_book";
        if("napój z oliwką".equals(item))return "give_drink";
        if("orchidea".equals(item))return "give_flower";
        if("zbroja barbarzyńcy".equals(item))return "give_armor";
        if("klucz do więzienia Kanmararn".equals(item))return "give_key";
        if("uszkodzona różdżka".equals(item))return "give_staff";
        if("paczka jedzenia".equals(item))return "give_food";
        return "give_"+item;
    }
    static void add(List<NpcDialogueSnapshot.Item> rows,Player p,String name,int required,String reply){
        Item item=SingletonRepository.getEntityManager().getItem(name);if(item==null)return;
        rows.add(new NpcDialogueSnapshot.Item("journey_"+rows.size(),name,"items/"+item.getItemClass()+"/"+item.getItemSubclass(),required,p.getNumberOfEquipped(name),-1,100,0,ResidentDialogueReplies.id(reply)));
    }
}
