package games.stendhal.server.entity.npc.dialogue;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import java.util.*;
/** Services and explanations at the start use the same validated dialogue channel. */
final class StarterDialogueReplies {
    static boolean supports(SpeakerNPC npc){return Arrays.asList("Miter","Thyron","Jorin").contains(npc.getName());}
    static Map<String,PilotDialogueReplies.Choice> read(SpeakerNPC n,Player p){
        Map<String,PilotDialogueReplies.Choice> r=new LinkedHashMap<String,PilotDialogueReplies.Choice>();
        ConversationStates s=n.getEngine().getCurrentState();if(s==ConversationStates.IDLE)return r;
        if(s==ConversationStates.BUY_PRICE_OFFERED){add(r,n,p,"confirm","Tak, kupuję.","tak");add(r,n,p,"cancel","Nie, rezygnuję.","nie");}
        else if(s==ConversationStates.SELL_PRICE_OFFERED){add(r,n,p,"confirm","Tak, sprzedaję za podaną cenę.","tak");add(r,n,p,"cancel","Nie, zatrzymam ten przedmiot.","nie");}
        else if(s==ConversationStates.ATTENDING){
            if("Miter".equals(n.getName())){add(r,n,p,"offer","Pokaż mi swoją ofertę.","oferta");add(r,n,p,"heal","Potrzebuję leczenia.","ulecz");
                add(r,n,p,"buyback","Co mogę ci sprzedać?","skup");
                if("skup".equals(NpcDialogueService.lastCommand(n,p)) || "sell".equals(NpcDialogueService.branch(n,p))){
                    int i=0;
                    for(Map.Entry<String,Integer> e:games.stendhal.server.core.engine.SingletonRepository.getShopList().get("miterbuyback").entrySet()){
                        if(p.isEquipped(e.getKey()))add(r,n,p,"sell_"+i,"Sprzedam: "+e.getKey()+" ("+e.getValue()+" monet).","sprzedam "+e.getKey());
                        i++;
                    }
                }}
            else{add(r,n,p,"barrier","Czego brakuje mi przed dalszą drogą?","bariera");add(r,n,p,"quests","Jakie zadania mam wykonać na początek?","zadania");}
            add(r,n,p,"help","Jak korzystać z gry i gdzie szukać pomocy?","pomoc");add(r,n,p,"job","Opowiedz mi o sobie.","praca");
        }
        add(r,n,p,"goodbye","Do zobaczenia.","bye");return r;
    }
    private static void add(Map<String,PilotDialogueReplies.Choice> r,SpeakerNPC n,Player p,String id,String label,String command){if(n.getEngine().canHandle(p,command))r.put(id,new PilotDialogueReplies.Choice(id,label,command,command));}
}
