package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.core.rp.quest.MasterStage;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.semos.masterhouse.MasterNPC.MasterSpeaker;
import java.util.*;

final class MasterDialogueReplies {
    private MasterDialogueReplies() { }
    static boolean supports(SpeakerNPC npc){return npc instanceof MasterSpeaker;}
    static Map<String,PilotDialogueReplies.Choice> read(SpeakerNPC npc,Player p){
        MasterSpeaker master=(MasterSpeaker)npc;
        Map<String,PilotDialogueReplies.Choice> choices=new LinkedHashMap<String,PilotDialogueReplies.Choice>();
        if(!npc.isTalking())return choices;
        MasterStage stage=master.selected(p);ConversationStates state=npc.getEngine().getCurrentState();
        if(stage==null){
            for(MasterStage s:master.stages())if(s.visible(p)&&!s.completed(p))
                add(choices,npc,p,"stage_"+s.number,"Etap "+s.number+" - "+(s.active(p)?"kontynuuj zadanie":"poziom "+s.level()),"etap "+s.number,"zadanie");
            add(choices,npc,p,"history","Które próby mam już za sobą?","historia stopni","zadanie");
            add(choices,npc,p,"job","Opowiedz mi o sobie.","praca","praca");
            add(choices,npc,p,"help","Co robić dalej i gdzie szukać pomocy?","pomoc","pomoc");
        }else if(stage.number==25&&!stage.completed(p)){
            ExpeditionDialogueReplies.master(choices,npc,p);
        }else if(!stage.completed(p)){
            if(state==ConversationStates.QUEST_OFFERED){
                add(choices,npc,p,"accept","Spróbuję. Możesz na mnie liczyć.","tak","tak");
                add(choices,npc,p,"decline","Wrócę, gdy będę gotowy.","nie","nie");
            }else if(state==ConversationStates.QUEST_ITEM_QUESTION){
                add(choices,npc,p,"give_master","Przekazuję wymagane przedmioty.","tak",stage.itemName(p));
                add(choices,npc,p,"later","Jeszcze ich nie przyniosłem.","nie","nie");
            }else{
                add(choices,npc,p,"quest",stage.active(p)?"Wróciłem w sprawie zadania.":"Co mam zrobić?","zadanie","zadanie");
                if(stage.active(p))add(choices,npc,p,"supplies","Co jeszcze zostało do zrobienia?","postęp","zadanie");
            }
        }
        if(stage!=null)add(choices,npc,p,"back","Wróćmy do listy etapów.","etapy","wróć");
        add(choices,npc,p,"goodbye","Do zobaczenia.","bye","bye");
        return choices;
    }
    private static void add(Map<String,PilotDialogueReplies.Choice> result,SpeakerNPC n,Player p,String id,String label,String command,String topic){
        if(n.getEngine().canHandle(p,command))result.put(id,new PilotDialogueReplies.Choice(id,label,command,topic==null?"zadanie":topic));
    }
    static void items(List<NpcDialogueSnapshot.Item> rows,SpeakerNPC npc,Player p){
        MasterStage s=((MasterSpeaker)npc).selected(p);if(s==null||s.completed(p))return;
        if(s.number==25){ExpeditionDialogueReplies.masterItems(rows,p);return;}
        if(s.hunt()){
            if(s.active(p))for(Map.Entry<String,int[]> e:s.kills(p).entrySet())
                HuntDialogueItems.creature(rows,e.getKey().replace(" (solo)",""),e.getKey(),e.getValue()[1],e.getValue()[0],false);
            else for(Map.Entry<String,marauroa.common.Pair<Integer,Integer>> e:s.targets().entrySet())
                HuntDialogueItems.creature(rows,e.getKey(),e.getKey(),e.getValue().first()+e.getValue().second(),0,false);
        }else if(s.active(p)&&s.itemName(p)!=null&&!s.itemName(p).isEmpty()&&s.itemAmount(p)>0)
            JourneyDialogueItems.add(rows,p,s.itemName(p),s.itemAmount(p),"give_master");
    }
}
