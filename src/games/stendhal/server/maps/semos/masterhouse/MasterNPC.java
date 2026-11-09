package games.stendhal.server.maps.semos.masterhouse;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.quest.MasterStage;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.IQuest;
import java.util.*;

/** One world object and one conversation. The original stages are guarded topics. */
public class MasterNPC implements ZoneConfigurator {
    public static class MasterSpeaker extends SpeakerNPC {
        private final Map<Integer,MasterStage> stages=new TreeMap<Integer,MasterStage>();
        private MasterStage selected;
        private Player selectedPlayer;
        public MasterSpeaker(){super(MasterStage.NPC_NAME);}
        public MasterStage stage(int number,IQuest quest){
            MasterStage stage=stages.get(number);
            if(stage==null){stage=new MasterStage(this,number,quest);stages.put(number,stage);}
            return stage;
        }
        public Collection<MasterStage> stages(){return Collections.unmodifiableCollection(stages.values());}
        public MasterStage selected(Player p){return selectedPlayer==p?selected:null;}
        public void select(MasterStage stage,Player p){selected=stage;selectedPlayer=p;}
        @Override public void setAttending(RPEntity p){
            if(p!=getAttending()){selected=null;selectedPlayer=null;}
            super.setAttending(p);
        }
        private void menu(Player p){
            select(null,p);
            getEngine().setCurrentState(ConversationStates.ATTENDING);
            StringBuilder b=new StringBuilder("Witaj. Poprowadzę cię przez kolejne stopnie wojownika Faumonii. Wybierz etap z listy lub powiedz #zadanie, a wskażę twoją najbliższą sprawę.");
            MasterStage next=recommended(p);
            if(next==null)b.append(" Wszystkie dostępne stopnie masz ukończone. Możesz przejrzeć swoją historię.");
            else b.append(" Najbliższy temat: etap ").append(next.number).append(" (poziom ").append(next.level()).append(").");
            say(b.toString());
        }
        public MasterStage recommended(Player p){
            for(MasterStage s:stages.values())if(s.active(p))return s;
            for(MasterStage s:stages.values())if(s.visible(p)&&!s.completed(p))return s;
            return null;
        }
        @Override protected void createPath(){setPath(null);}
        @Override protected void createDialog(){
            add(ConversationStates.IDLE,ConversationPhrases.GREETING_MESSAGES,null,ConversationStates.ATTENDING,null,(p,s,n)->menu(p));
            add(ConversationStates.ANY,Arrays.asList("zadania","etapy","wątki","tematy","wróć"),
                (p,s,n)->Arrays.asList("zadania","etapy","wątki","tematy","wróć").contains(s.getOriginalText().trim().toLowerCase(Locale.ROOT)),
                ConversationStates.ATTENDING,null,(p,s,n)->menu(p));
            add(ConversationStates.ATTENDING,ConversationPhrases.QUEST_MESSAGES,(p,s,n)->selected(p)==null,ConversationStates.ATTENDING,null,(p,s,n)->{
                MasterStage next=recommended(p);if(next==null)menu(p);else next.open(p);
            });
            add(ConversationStates.ATTENDING,"postęp",(p,s,n)->selected(p)!=null,ConversationStates.ATTENDING,null,(p,s,n)->selected(p).open(p));
            add(ConversationStates.ANY,"historia stopni",null,ConversationStates.ATTENDING,null,(p,s,n)->{
                select(null,p);StringBuilder b=new StringBuilder("Ukończone stopnie: ");boolean any=false;
                for(MasterStage stage:stages.values())if(stage.completed(p)){if(any)b.append(", ");b.append(stage.number);any=true;}
                if(!any)b.append("jeszcze żaden");say(b.append(". Twój postęp jest zachowany; do aktywnej sprawy wrócisz przez temat zadania.").toString());
            });
            for(int i=1;i<=25;i++){
                final int stageNumber=i;
                add(ConversationStates.ANY,"etap "+i,(p,s,n)->stages.containsKey(stageNumber)
                        && s.getOriginalText().trim().equalsIgnoreCase("etap "+stageNumber),ConversationStates.ATTENDING,null,(p,s,n)->{
                    MasterStage stage=stages.get(stageNumber);
                    if(!stage.visible(p)){say("Najpierw ukończ etap "+(stageNumber-1)+". Numer zadania nie zastępuje wcześniejszych wymagań.");return;}
                    stage.open(p);
                });
            }
            addJob("Przez lata przemierzałem Faumonię i wiem, ile kosztuje źle przygotowana wyprawa. Teraz sprawdzam kolejne pokolenia wojowników i pomagam im wybrać następny cel.");
            addHelp("Wybierz #zadanie, aby sprawdzić swój etap, lub #etapy, żeby wrócić do listy. Jeśli potrzebujesz pomocy innych wojowników, wpisz na czacie /k i swoją wiadomość.");
            addGoodbye("Do zobaczenia. Dbaj o ekwipunek i wróć, kiedy będziesz gotowy.");
        }
    }
    public void configureZone(StendhalRPZone zone,Map<String,String> attributes){
        MasterSpeaker n=new MasterSpeaker();n.setEntityClass("masternpc");n.setPosition(3,5);n.setDirection(Direction.RIGHT);n.initHP(100);
        n.setDescription("Widzisz Mistrza Faumonii, doświadczonego wojownika z Semos. Uważnie ocenia twoje przygotowanie, zanim wskaże kolejną próbę.");zone.add(n);
    }
}
