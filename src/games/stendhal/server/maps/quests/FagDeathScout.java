package games.stendhal.server.maps.quests;
import games.stendhal.server.core.rp.quest.ExpeditionXXV;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.player.Player;
import java.util.*;
import static games.stendhal.server.entity.npc.ConversationStates.*;

/** A one-off expedition report, independent of Fag's existing KillLast challenge. */
public class FagDeathScout extends AbstractQuest {
    public String getSlotName(){return ExpeditionXXV.HUNT;}
    public String getName(){return "Zwiad przed wyprawą";}
    public String getNPCName(){return "Fag";}
    public int getMinLevel(){return 320;}
    public List<String> getHistory(Player p){return p.hasQuest(getSlotName())?Arrays.asList(ExpeditionXXV.huntSummary(p)):Collections.<String>emptyList();}
    public void addToWorld(){
        fillQuestInfo(getName(),"Krótki zwiad Faga poprzedzający dwudziestą piątą próbę Mistrza Faumonii.",false);
        SpeakerNPC n=npcs.get("Fag");
        n.add(ATTENDING,"zwiad wyprawy",(p,s,e)->ExpeditionXXV.eligible(p),EXPEDITION_DISCUSSION,null,(p,s,e)->e.say(ExpeditionXXV.huntSummary(p)));
        n.add(EXPEDITION_DISCUSSION,"przyjmij zwiad",(p,s,e)->ExpeditionXXV.eligible(p)&&ExpeditionXXV.newQuest(p,getSlotName()),EXPEDITION_OFFERED,
            "Nie potrzebuję kolejnej opowieści o potworach widzianych z daleka. Zmierz się z kilkoma odmianami śmierci i wielkich kostuch, a dowiemy się, z czym liczyć się podczas wyprawy. Przyjmujesz krótsze polowanie z pokazanej listy?",null);
        n.add(EXPEDITION_OFFERED,ConversationPhrases.YES_MESSAGES,(p,s,e)->ExpeditionXXV.eligible(p)&&ExpeditionXXV.newQuest(p,getSlotName()),EXPEDITION_DISCUSSION,null,(p,s,e)->{
            p.setQuest(getSlotName(),"start");new StartRecordingKillsAction(getSlotName(),1,ExpeditionXXV.targets()).fire(p,s,e);
            e.say("Zapisuję początek zwiadu. Wcześniejsze zwycięstwa nie zastąpią nowego meldunku; każda wskazana odmiana ma własny cel. "+ExpeditionXXV.huntSummary(p));
        });
        n.add(EXPEDITION_OFFERED,ConversationPhrases.NO_MESSAGES,null,EXPEDITION_DISCUSSION,"Rozumiem. Przygotuj zapasy i wróć, gdy zechcesz przyjąć ten zwiad.",null);
        n.add(EXPEDITION_DISCUSSION,"meldunek zwiadu",(p,s,e)->ExpeditionXXV.eligible(p)&&ExpeditionXXV.state(p,getSlotName()).startsWith("start;"),EXPEDITION_DISCUSSION,null,(p,s,e)->{
            if(ExpeditionXXV.kills(p).size()==6&&new KilledForQuestCondition(getSlotName(),1).fire(p,s,e.getEntity())){
                p.setQuest(getSlotName(),"done");e.say("Meldunek jest kompletny. Wiesz już, jakich przeciwników spotkamy na trasie, i wykazałeś się czymś więcej niż odwagą na słowach. Mistrz uzna ten zwiad przy przygotowaniu swojej dwudziestej piątej próby.");
            }else e.say("Brakuje jeszcze części meldunku. "+ExpeditionXXV.huntSummary(p));
        });
        n.add(EXPEDITION_DISCUSSION,"cele zwiadu",null,EXPEDITION_DISCUSSION,null,(p,s,e)->e.say(ExpeditionXXV.huntSummary(p)));
        n.add(EXPEDITION_DISCUSSION,"droga zwiadu",null,EXPEDITION_DISCUSSION,
            "Czarnych śmierci szukaj w zachodnich jaskiniach Nalwor, a srebrnych w jaskini pod północno-zachodnią górą Ados; schody są na polu 56,95. Złotą śmierć znajdziesz między innymi we wschodniej części kopalni złota Orril. Wielkie kostuchy, także różowe i złote, występują w jaskini rzeki Orril; sprawdzaj nazwy przeciwników, bo małe kostuchy nie zaliczają tego zwiadu.",null);
        ExpeditionQuestConversation.exit(n);
    }
}
