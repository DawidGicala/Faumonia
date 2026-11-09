package games.stendhal.server.maps.quests;
import games.stendhal.server.core.rp.quest.ExpeditionXXV;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import java.util.*;
import static games.stendhal.server.entity.npc.ConversationStates.*;

public class ThyronExpeditionFittings extends AbstractQuest {
    public String getSlotName(){return ExpeditionXXV.FITTINGS;}
    public String getName(){return "Okucia na dalszą drogę";}
    public String getNPCName(){return "Thyron";}
    public int getMinLevel(){return 320;}
    public List<String> getHistory(Player p){return p.hasQuest(getSlotName())?Arrays.asList(ExpeditionXXV.fittingsSummary(p)):Collections.<String>emptyList();}
    public void addToWorld(){
        fillQuestInfo(getName(),"Przygotuj z Hogartem okucia i dostarcz je Thyronowi przed rozpoznaniem trasy Mistrza XXV.",false);
        SpeakerNPC t=npcs.get("Thyron"),h=npcs.get("Hogart");
        for(SpeakerNPC n:new SpeakerNPC[]{t,h}){
            n.add(ATTENDING,"okucia wyprawy",(p,s,e)->ExpeditionXXV.eligible(p),EXPEDITION_DISCUSSION,null,(p,s,e)->e.say(ExpeditionXXV.fittingsDialogue(p,n.getName())));
            n.add(EXPEDITION_DISCUSSION,"stan okuć",null,EXPEDITION_DISCUSSION,null,(p,s,e)->e.say(ExpeditionXXV.fittingsDialogue(p,n.getName())));
            ExpeditionQuestConversation.exit(n);
        }
        t.add(EXPEDITION_DISCUSSION,"przyjmij zamówienie",(p,s,e)->ExpeditionXXV.eligible(p)&&ExpeditionXXV.newQuest(p,getSlotName()),EXPEDITION_OFFERED,
            "Przed kolejną wyprawą trzeba zabezpieczyć przejścia, które długo pozostawały bez opieki. Hogart wykona okucia z 100 żelaza, 200 polan i 30 sztabek mithrilu; możesz zanosić mu je partiami. Podejmiesz się dostarczenia gotowej paczki?",null);
        t.add(EXPEDITION_OFFERED,ConversationPhrases.YES_MESSAGES,(p,s,e)->ExpeditionXXV.eligible(p)&&ExpeditionXXV.newQuest(p,getSlotName()),EXPEDITION_DISCUSSION,null,(p,s,e)->{
            p.setQuest(getSlotName(),"collect;100;200;30");e.say("Hogart czeka w kuźni głęboko pod kopalniami krasnoludów Orril. Porozmawiaj z nim o okuciach wyprawy, przekaż surowce i odbierz paczkę. Gdy przyniesiesz ją do mnie, potwierdzę gotowość sprzętu Mistrzowi.");
        });
        t.add(EXPEDITION_OFFERED,ConversationPhrases.NO_MESSAGES,null,EXPEDITION_DISCUSSION,"Przygotowania mogą zaczekać. Wróć, kiedy znajdziesz czas na współpracę z Hogartem.",null);
        t.add(EXPEDITION_DISCUSSION,"oddaj paczkę okuć",(p,s,e)->ExpeditionXXV.eligible(p)&&ExpeditionXXV.state(p,getSlotName()).equals("issued"),EXPEDITION_DISCUSSION,null,(p,s,e)->ExpeditionXXV.handOverPackage(p,e));
        for(int i=0;i<3;i++){final int index=i;
            h.add(EXPEDITION_DISCUSSION,"przekaż materiał "+i,(p,s,e)->s.getOriginalText().trim().equalsIgnoreCase("przekaż materiał "+index)&&ExpeditionXXV.eligible(p)&&ExpeditionXXV.remaining(p)!=null,EXPEDITION_DISCUSSION,null,(p,s,e)->ExpeditionXXV.deliverMaterial(p,index,e));
        }
        h.add(EXPEDITION_DISCUSSION,"odbierz okucia",(p,s,e)->ExpeditionXXV.eligible(p)&&(ExpeditionXXV.state(p,getSlotName()).equals("ready")||ExpeditionXXV.state(p,getSlotName()).equals("issued")),EXPEDITION_DISCUSSION,null,(p,s,e)->ExpeditionXXV.claimPackage(p,e));
    }
}
