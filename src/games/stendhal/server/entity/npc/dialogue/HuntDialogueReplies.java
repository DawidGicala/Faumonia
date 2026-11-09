package games.stendhal.server.entity.npc.dialogue;

import games.stendhal.common.QuestProgress;
import games.stendhal.server.core.rp.quest.HuntQuestProgress;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import games.stendhal.server.entity.npc.dialogue.PilotDialogueReplies.Choice;
import java.util.*;

/** One current quest continuation, followed by useful services and conversation. */
final class HuntDialogueReplies {
    private HuntDialogueReplies() { }
    static boolean supports(SpeakerNPC n){return HuntQuestProgress.forNpc(n.getName())!=null;}
    static void add(Map<String,Choice> rows,SpeakerNPC n,Player p,String id,String label,String command){
        if(n.getEngine().canHandle(p,command))rows.put(id,new Choice(id,label,command,command));
    }
    static Map<String,Choice> read(SpeakerNPC n,Player p){
        Map<String,Choice> rows=new LinkedHashMap<String,Choice>();IQuest q=HuntQuestProgress.forNpc(n.getName());
        ConversationStates state=n.getEngine().getCurrentState();if(q==null||state==ConversationStates.IDLE)return rows;
        String last=NpcDialogueService.lastCommand(n,p),value=p.getQuest(q.getSlotName());
        boolean available=value==null||"rejected".equals(p.getQuest(q.getSlotName(),0))||q.isRepeatable(p);
        if(state==ConversationStates.QUEST_OFFERED){
            if(available){add(rows,n,p,"accept","Tak, podejmę się tej wyprawy.","tak");add(rows,n,p,"decline","Nie jestem jeszcze gotowy. Wrócę później.","nie");}
        }else if(state==ConversationStates.ATTENDING){
            boolean active=value!=null&&!q.isCompleted(p)&&!"rejected".equals(value);
            String command=q instanceof KillSpiders||q instanceof KillDarkElves||q instanceof CollectChaosThorgal?"meldunek":"zadanie";
            QuestProgress progress=HuntQuestProgress.read(q,p);
            if(active){
                boolean ready=progress!=null&&progress.getState()==QuestProgress.State.READY_TO_DELIVER;
                if(ready||!command.equalsIgnoreCase(last))add(rows,n,p,"supplies",q instanceof CollectChaosThorgal?"Przynoszę ekwipunek do tuneli chaosu.":ready?"Pokonałem wskazane potwory.":"Które potwory jeszcze zostały?",command);
                if(!"wskazówki wyprawy".equalsIgnoreCase(last))add(rows,n,p,"hints",q instanceof CollectChaosThorgal?"Przypomnij mi wymagany zestaw ekwipunku.":"Przypomnij mi cele i miejsca poszukiwań.","wskazówki wyprawy");
            }else if(available&&!"zadanie".equalsIgnoreCase(last))add(rows,n,p,"quest","Czy potrzebujesz mojej pomocy?",q instanceof CollectChaosThorgal?"meldunek":"zadanie");
            else if(q.isCompleted(p)&&!"zadanie".equalsIgnoreCase(last)&&!(q instanceof CollectChaosThorgal)&&!(q instanceof KillDarkElves))add(rows,n,p,"cooldown","Czy nadszedł już czas na kolejną wyprawę?","zadanie");
            if(!"oferta".equalsIgnoreCase(last))add(rows,n,p,"offer","Pokaż mi swoją ofertę.","oferta");
            if(!"praca".equalsIgnoreCase(last))add(rows,n,p,"job","Opowiedz mi o sobie.","praca");
            if(!"pomoc".equalsIgnoreCase(last))add(rows,n,p,"help","Co warto wiedzieć w tej okolicy?","pomoc");
            if("Jenny".equals(n.getName())&&!"zmiel".equalsIgnoreCase(last))add(rows,n,p,"mill","Chcę zlecić zmielenie zboża.","zmiel");
            if(!"root".equals(NpcDialogueService.branch(n,p)))add(rows,n,p,"back","Porozmawiajmy o czymś innym.","wątki");
        }else{
            String label=state==ConversationStates.BUY_PRICE_OFFERED?"Tak, kupuję.":state==ConversationStates.SELL_PRICE_OFFERED?"Tak, sprzedaję.":"Tak, potwierdzam.";
            add(rows,n,p,"confirm",label,"tak");add(rows,n,p,"cancel","Nie teraz. Wrócę do tego później.","nie");
        }
        add(rows,n,p,"goodbye","Do zobaczenia.","bye");return rows;
    }
}
