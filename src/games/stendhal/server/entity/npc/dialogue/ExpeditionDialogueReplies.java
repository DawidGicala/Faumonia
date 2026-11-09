package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.server.core.rp.quest.ExpeditionXXV;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import java.util.*;

/** Explicit continuations of the expedition topic; old NPC quests keep their own branches. */
final class ExpeditionDialogueReplies {
    private ExpeditionDialogueReplies() { }
    static boolean supports(SpeakerNPC n){return Arrays.asList("Fag","Thyron","Hogart").contains(n.getName());}
    static boolean active(SpeakerNPC n){return supports(n)&&(n.getEngine().getCurrentState()==ConversationStates.EXPEDITION_DISCUSSION||n.getEngine().getCurrentState()==ConversationStates.EXPEDITION_OFFERED);}
    private static void add(Map<String,PilotDialogueReplies.Choice> rows,SpeakerNPC n,Player p,String id,String label,String command,String topic){
        if(n.getEngine().canHandle(p,command))rows.put(id,new PilotDialogueReplies.Choice(id,label,command,topic));
    }
    static void decorate(Map<String,PilotDialogueReplies.Choice> rows,SpeakerNPC n,Player p){
        if(!supports(n)||n.getEngine().getCurrentState()!=ConversationStates.ATTENDING||!ExpeditionXXV.eligible(p))return;
        boolean fag="Fag".equals(n.getName());
        add(rows,n,p,"xxv_open",fag?"Chcę omówić krótki zwiad do próby XXV.":"Porozmawiajmy o okuciach na wyprawę Mistrza.",fag?"zwiad wyprawy":"okucia wyprawy","zadanie");
    }
    static Map<String,PilotDialogueReplies.Choice> read(SpeakerNPC n,Player p){
        Map<String,PilotDialogueReplies.Choice> rows=new LinkedHashMap<String,PilotDialogueReplies.Choice>();
        if(n.getEngine().getCurrentState()==ConversationStates.EXPEDITION_OFFERED){
            add(rows,n,p,"accept","Tak, podejmę się tego zadania.","tak","tak");
            add(rows,n,p,"decline","Jeszcze się przygotuję.","nie","nie");
        }else if("Fag".equals(n.getName())){
            if(ExpeditionXXV.newQuest(p,ExpeditionXXV.HUNT))add(rows,n,p,"xxv_accept","Chcę przyjąć krótsze polowanie.","przyjmij zwiad","zadanie");
            else if(!p.isQuestCompleted(ExpeditionXXV.HUNT))add(rows,n,p,"xxv_report","Zdaję meldunek z polowania.","meldunek zwiadu","zadanie");
            if(!p.isQuestCompleted(ExpeditionXXV.HUNT))add(rows,n,p,"hints","Gdzie szukać tych przeciwników?","droga zwiadu","wskazówki");
        }else{
            String state=ExpeditionXXV.state(p,ExpeditionXXV.FITTINGS);int[] left=ExpeditionXXV.remaining(p);
            if("Thyron".equals(n.getName())){
                if(ExpeditionXXV.newQuest(p,ExpeditionXXV.FITTINGS))add(rows,n,p,"xxv_accept","Podejmę się zamówienia okuć.","przyjmij zamówienie","zadanie");
                if("issued".equals(state))add(rows,n,p,"give_parcel","Przynoszę paczkę okuć od Hogarta.","oddaj paczkę okuć",ExpeditionXXV.PACKAGE);
            }else if(left!=null){
                for(int i=0;i<3;i++)if(left[i]>0&&p.getNumberOfEquipped(ExpeditionXXV.MATERIALS[i])>0)
                    add(rows,n,p,"give_material_"+i,"Przekazuję: "+ExpeditionXXV.MATERIALS[i]+" - "+Math.min(left[i],p.getNumberOfEquipped(ExpeditionXXV.MATERIALS[i]))+".","przekaż materiał "+i,ExpeditionXXV.MATERIALS[i]);
            }else if("ready".equals(state)||"issued".equals(state))add(rows,n,p,"give_parcel","ready".equals(state)?"Odbieram paczkę dla Thyrona.":"Nie mam paczki. Sprawdź, czy można ją odtworzyć.","odbierz okucia",ExpeditionXXV.PACKAGE);
        }
        add(rows,n,p,"back","Porozmawiajmy o innych sprawach.","inne sprawy","wróć");
        add(rows,n,p,"goodbye","Do zobaczenia.","bye","bye");return rows;
    }
    static void master(Map<String,PilotDialogueReplies.Choice> rows,SpeakerNPC n,Player p){
        if(n.getEngine().getCurrentState()==ConversationStates.QUEST_OFFERED){
            add(rows,n,p,"accept","Podejmuję się próby XXV.","tak","tak");add(rows,n,p,"decline","Wrócę, gdy będę gotowy.","nie","nie");return;
        }
        String state=ExpeditionXXV.state(p,ExpeditionXXV.MASTER);
        if("fang".equals(state))add(rows,n,p,"give_master","Przekazuję kieł złotej kostuchy i kończę próbę.","oddaj kieł",ExpeditionXXV.FANG);
        else if("preparing".equals(state))add(rows,n,p,"quest",ExpeditionXXV.prepared(p)?"Przygotowania ukończone. Zleć mi rozpoznanie trasy.":"Przypomnij, jakie przygotowania zostały.","zadanie","zadanie");
        else if(state.startsWith("survey;"))add(rows,n,p,"quest",ExpeditionXXV.surveyed(p)==3?"Zdaję raport z trzech zbadanych miejsc.":"Przypomnij mi miejsca i stan rozpoznania.","zadanie","zadanie");
        else if(ExpeditionXXV.newQuest(p,ExpeditionXXV.MASTER))add(rows,n,p,"quest","Chcę poznać warunki próby XXV.","zadanie","zadanie");
    }
    static void items(List<NpcDialogueSnapshot.Item> rows,SpeakerNPC n,Player p){
        if("Fag".equals(n.getName())){
            if(p.isQuestCompleted(ExpeditionXXV.HUNT))return;
            Map<String,int[]> progress=ExpeditionXXV.kills(p);
            for(Map.Entry<String,marauroa.common.Pair<Integer,Integer>> e:ExpeditionXXV.targets().entrySet()){
                int[] count=progress.get(e.getKey());HuntDialogueItems.creature(rows,e.getKey(),e.getKey(),e.getValue().second(),count==null?0:count[0],false);
            }
        }else if(!p.isQuestCompleted(ExpeditionXXV.FITTINGS)){
            String state=ExpeditionXXV.state(p,ExpeditionXXV.FITTINGS);int[] left=ExpeditionXXV.remaining(p);
            if(left!=null||ExpeditionXXV.newQuest(p,ExpeditionXXV.FITTINGS)){
                for(int i=0;i<3;i++){int need=left==null?ExpeditionXXV.AMOUNTS[i]:left[i];if(need>0)JourneyDialogueItems.add(rows,p,ExpeditionXXV.MATERIALS[i],need,"give_material_"+i);}
            }else if("ready".equals(state)||"issued".equals(state))JourneyDialogueItems.add(rows,p,ExpeditionXXV.PACKAGE,1,"give_parcel");
        }
    }
    static void masterItems(List<NpcDialogueSnapshot.Item> rows,Player p){
        String state=ExpeditionXXV.state(p,ExpeditionXXV.MASTER);
        if("preparing".equals(state)){
            npcRow(rows,"Fag","Krótki zwiad Faga",p.isQuestCompleted(ExpeditionXXV.HUNT));
            npcRow(rows,"Thyron","Okucia dostarczone Thyronowi",p.isQuestCompleted(ExpeditionXXV.FITTINGS));
        }else if(state.matches("survey;[0-7]")){
            for(int i=0;i<3;i++)rows.add(new NpcDialogueSnapshot.Item("survey_"+i,ExpeditionXXV.PLACES[i],"useable/expedition/"+ExpeditionXXV.SPRITES[i],1,(ExpeditionXXV.surveyMask(p)&(1<<i))!=0?1:0,-1,100,0,"quest"));
        }else if("fang".equals(state))JourneyDialogueItems.add(rows,p,ExpeditionXXV.FANG,1,"give_master");
    }
    private static void npcRow(List<NpcDialogueSnapshot.Item> rows,String name,String label,boolean done){
        SpeakerNPC npc=NPCList.get().get(name);String sprite=npc!=null&&npc.has("class")?"npc/"+npc.get("class"):"gui/dialogue/quest";
        rows.add(new NpcDialogueSnapshot.Item("prepare_"+name,label,sprite,1,done?1:0,-1,100,0,"quest"));
    }
}
