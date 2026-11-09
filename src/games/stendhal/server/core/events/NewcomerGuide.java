package games.stendhal.server.core.events;

import games.stendhal.common.TutorialGuideSnapshot;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.events.TutorialGuideEvent;
import games.stendhal.server.entity.npc.dialogue.NpcDialogueService;
import java.util.*;

/** Progress comes from real quests. Hiding client help never grants access or rewards. */
public final class NewcomerGuide {
    public static final String KEY="guide_v1";
    public static final String CITY="0_semos_city", HOUSE="int_semos_guard_house", MASTER="int_semos_master_house", CAVE="-1_semos_village_cave";
    public static final String[] QUESTS={"task_hayunn","main_task_1","help_sedro","cheese_for_soro"};
    private static final String[] NAMES={"Hayunn - woda","Mistrz I - szczury","Sedro - gnomy","Soro - sery"};
    private NewcomerGuide(){}
    public static void newCharacter(Player p){
        if(!p.hasSlot("!tutorial"))p.addSlot("!tutorial");
        if(p.getSlot("!tutorial").size()==0)p.getSlot("!tutorial").add(new marauroa.common.game.RPObject());
        p.setKeyedSlot("!tutorial",KEY,"active");p.setKeyedSlot("!tutorial","guide_migrated","1");
    }
    public static boolean inStart(Player p){String z=p.getZone().getName();return HOUSE.equals(z)||MASTER.equals(z)||CAVE.equals(z)||CITY.equals(z)&&p.getX()<67;}
    public static void initialize(Player p){
        if(p.getZone()==null)return;
        if(p.getKeyedSlot("!tutorial",KEY)==null){
            // Existing adventurers keep their old access, without inventing quest completions.
            p.setKeyedSlot("!tutorial",KEY,p.getLevel()>=5||!inStart(p)?"legacy":"active");
        }
        if(p.getKeyedSlot("!tutorial","guide_migrated")==null){
            migrateHunt(p,"main_task_1",new String[]{"szczur","szczur jaskiniowy"},new int[]{3,1});
            migrateHunt(p,"help_sedro",new String[]{"gnom","panna gnom"},new int[]{1,1});
            String cheese=p.getQuest("cheese_for_soro");
            if(cheese!=null&&cheese.matches("ser=[0-9]+")){
                int left=Integer.parseInt(cheese.substring(4));
                // Preserve delivery credit: old 10 total becomes 3. No automatic reward.
                if(left>3)p.setQuest("cheese_for_soro","ser="+Math.max(0,left-7));
                else p.setQuest("cheese_for_soro","ser=0");
            }
            p.setKeyedSlot("!tutorial","guide_migrated","1");
        }
    }
    private static void migrateHunt(Player p,String slot,String[] names,int[] amounts){
        String state=p.getQuest(slot);if(state==null||!state.startsWith("start;"))return;
        String[] parts=state.split(";",-1);if(parts.length<2)return;
        String[] a=parts[1].split(",");if(a.length%5!=0)return;
        Map<String,String[]> old=new HashMap<String,String[]>();
        for(int i=0;i<a.length;i+=5)old.put(a[i],Arrays.copyOfRange(a,i,i+5));
        List<String> record=new ArrayList<String>();
        for(int i=0;i<names.length;i++){
            String[] row=old.get(names[i]);if(row==null)return;
            try{for(int j=1;j<5;j++)Integer.parseInt(row[j]);}catch(NumberFormatException e){return;}
            row[1]="0";row[2]=Integer.toString(amounts[i]);Collections.addAll(record,row);
        }
        parts[1]=String.join(",",record);p.setQuest(slot,String.join(";",parts));
    }
    public static int completed(Player p){int count=0;for(String q:QUESTS)if(p.isQuestCompleted(q))count++;return count;}
    public static boolean canLeave(Player p){initialize(p);String mode=p.getKeyedSlot("!tutorial",KEY);return "legacy".equals(mode)||"done".equals(mode)||p.getLevel()>=5&&completed(p)==4;}
    public static String missing(Player p){List<String> left=new ArrayList<String>();for(int i=0;i<4;i++)if(!p.isQuestCompleted(QUESTS[i]))left.add(NAMES[i]);if(p.getLevel()<5)left.add("poziom 5 (masz "+p.getLevel()+")");return String.join(", ",left);}
    public static void crossedBarrier(Player p){
        if(p.getZone()!=null&&!inStart(p)&&canLeave(p)){
            p.setKeyedSlot("!tutorial",KEY,"done");tick(p);
        }
    }
    public static void tick(Player p){
        if(p.getZone()==null)return;
        initialize(p);
        if("active".equals(p.getKeyedSlot("!tutorial",KEY))&&!inStart(p)&&completed(p)==4&&p.getLevel()>=5)p.setKeyedSlot("!tutorial",KEY,"done");
        TutorialGuideSnapshot s=read(p);
        // Transient stamp belongs to this player instance; nothing is stored in the database.
        if(!s.toWire().equals(p.getTutorialGuideStamp())){p.setTutorialGuideStamp(s.toWire());p.addEvent(new TutorialGuideEvent(s));p.notifyWorldAboutChanges();}
    }
    public static TutorialGuideSnapshot read(Player p){
        String mode=p.getKeyedSlot("!tutorial",KEY);
        if("legacy".equals(mode)||"done".equals(mode)) {
            String tips = beginnerTips(p.getLevel());
            boolean automatic = !"done".equals(mode)&&!tips.isEmpty();
            return new TutorialGuideSnapshot(automatic ? "tips_" + (p.getLevel()<7?"chat":"master") : "finished",
                automatic ? "Przydatne wskazówki" : "Dalsza droga",
                !tips.isEmpty() ? tips : "Mistrz Faumonii zleca główne zadania odpowiednie do twojego poziomu. W rozmowach z NPC korzystaj z niebieskich słów i gotowych odpowiedzi. Gdy utkniesz, poproś graczy o pomoc: /k twoje pytanie.",
                p.getZone().getName(), -1, -1, "Otworzysz ponownie: menu > Przewodnik", "chat", completed(p), automatic);
        }
        if(!p.isQuestCompleted(QUESTS[0])){
            if(!active(p,QUESTS[0]))return npc(p,"hayunn","Poznaj Hayunna","W oknie rozmowy zapytaj: „Czy mogę ci w czymś pomóc?”. Przeczytaj jego prośbę i zgódź się przynieść wodę.","Hayunn",HOUSE,4,9,"dialogue");
            if(p.isEquipped("woda",1))return npc(p,"water_return","Oddaj wodę Hayunnowi","Masz już butelkę wody. Wybierz odpowiedź o przyniesionej wodzie, a potem przekaż ją Hayunnowi.","Hayunn",HOUSE,4,9,"bag");
            return npc(p,"water_buy","Kup jedną wodę","Wybierz „Pokaż mi swoją ofertę”. Znajdź wodę, ustaw ilość 1 i potwierdź zakup za 5 monet. Butelka trafi do plecaka.","Miter",HOUSE,18,6,"shop");
        }
        if(!p.isQuestCompleted(QUESTS[1]))return hunting(p,QUESTS[1],"master","Pierwsze polowanie","Mistrz Faumonii",MASTER,3,5,"3 szczury i 1 szczura jaskiniowego");
        if(!p.isQuestCompleted(QUESTS[2]))return hunting(p,QUESTS[2],"sedro","Pomóż Sedro","Sedro",CITY,20,39,"1 gnoma i 1 pannę gnom");
        if(!p.isQuestCompleted(QUESTS[3])){
            if(!active(p,QUESTS[3]))return npc(p,"soro","Zapasy dla Soro","Zapytaj Soro, czy możesz mu pomóc. Potrzebuje 3 serów - zgódź się je przynieść. Możesz oddać także sery zdobyte wcześniej.","Soro",CITY,54,43,"dialogue");
            int left=3;String state=p.getQuest(QUESTS[3]);try{left=Integer.parseInt(state.substring(state.indexOf('=')+1));}catch(RuntimeException ignored){}
            if(p.isEquipped("ser")||left==0)return npc(p,"cheese_return","Zanieś sery Soro","Wybierz odpowiedź o przyniesionych serach i oddaj je Soro. Możesz dostarczać je po kilka.\n\nBrakuje jeszcze: "+left+". W plecaku: "+p.getNumberOfEquipped("ser")+".","Soro",CITY,54,43,"bag");
            return goal(p,"cheese_hunt","Zdobądź sery","Kliknij szczura prawym przyciskiem i wybierz „Atakuj”. Po walce kliknij jego zwłoki prawym przyciskiem, wybierz „Otwórz” i przeciągnij ser do plecaka.\n\nPotrzebujesz jeszcze "+left+". Zachowaj te sery dla Soro.",CAVE,49,71,"Szczury w jaskini","bag",true);
        }
        if(p.getLevel()<5)return goal(p,"level_hunt","Zdobądź poziom 5","Masz już ukończone cztery zadania. Pokonaj jeszcze kilka szczurów, aby zdobyć 5 poziom. Twój poziom: "+p.getLevel()+".",CAVE,56,70,"Szczury w jaskini","health",true);
        return goal(p,"barrier","Ruszaj w dalszą drogę","Masz ukończone cztery zadania i wymagany poziom. Podejdź do bariery przy Thyronie i przejdź w prawo, na wschód. Po kolejne zadania wracaj do Mistrza Faumonii.",CITY,66,45,"Bariera przy Thyronie","map",true);
    }
    public static String beginnerTips(int level) {
        String chat = level < 7 ? "Potrzebujesz pomocy? Napisz /k i treść pytania, aby zapytać innych graczy. " : "";
        String master = level < 20 ? "Mistrz Faumonii zleca kolejne główne zadania. Zaglądaj do niego po awansie." : "";
        return chat + master;
    }
    private static boolean active(Player p,String slot){return p.hasQuest(slot)&&!"rejected".equals(p.getQuest(slot,0));}
    private static TutorialGuideSnapshot hunting(Player p,String slot,String id,String title,String name,String zone,int x,int y,String targets){
        if(!active(p,slot))return npc(p,id+"_accept","master".equals(id)?"Poznaj Mistrza Faumonii":title,("master".equals(id)?"Wybierz pierwszy etap i zapytaj o zadanie.":"Zapytaj, czy możesz pomóc.")+" Zgódź się na polowanie.\n\nPo przyjęciu zadania pokonaj: "+targets+".",name,zone,x,y,"dialogue");
        if(new KilledForQuestCondition(slot,1).fire(p,null,null))return npc(p,id+"_return","Odbierz nagrodę", "Polowanie jest skończone. Wybierz odpowiedź o wykonanym zadaniu i odbierz nagrodę.",name,zone,x,y,"dialogue");
        String progress=huntProgress(p,slot);
        return goal(p,id+"_hunt",title,"Kliknij wybranego potwora prawym przyciskiem i wybierz „Atakuj”. Walcz z jednym naraz i pilnuj paska PZ. Po pokonaniu wymaganych potworów wróć po nagrodę.\n\nPostęp:\n"+progress.replace(", ","\n"),CAVE,"master".equals(id)?56:11,"master".equals(id)?70:69,"master".equals(id)?"Szczury w jaskini":"Gnomy w jaskini","health",true);
    }
    public static String huntProgress(Player p,String slot){String v=p.getQuest(slot,1);if(v==null)return "";String[] a=v.split(",");List<String> result=new ArrayList<String>();try{for(int i=0;i+4<a.length;i+=5){int total=Integer.parseInt(a[i+1])+Integer.parseInt(a[i+2]);int done=Math.max(0,p.getSoloKill(a[i])+p.getSharedKill(a[i])-Integer.parseInt(a[i+3])-Integer.parseInt(a[i+4]));result.add(a[i]+": "+Math.min(total,done)+"/"+total);}}catch(NumberFormatException e){return "Nie można wyświetlić postępu polowania. Zgłoś problem administracji.";}return String.join(", ",result)+".";}
    private static TutorialGuideSnapshot npc(Player p,String id,String title,String text,String name,String zone,int x,int y,String anchor){
        SpeakerNPC n=NPCList.get().get(name);if(n!=null&&n.getZone()!=null){zone=n.getZone().getName();x=n.getX();y=n.getY();}
        if(n!=null&&n.getAttending()==p&&n.getEngine().getCurrentState()!=ConversationStates.IDLE){
            anchor="reply";
            String[] replies="water_return".equals(id)?new String[]{"give_woda","supplies"}
                :"cheese_return".equals(id)?new String[]{"claim","give_ser","supplies"}
                :"water_buy".equals(id)?new String[]{"offer"}
                :new String[]{"stage_1","accept","know","ingredients","quest"};
            String next=NpcDialogueService.guideReply(n,p,replies);
            if(next.isEmpty())next=NpcDialogueService.guideReply(n,p,"back","later","cancel");
            if(!next.isEmpty())text="Wybierz odpowiedź: „"+next+"”.\n\nPrzeczytaj odpowiedź rozmówcy. Przewodnik pokaże ci kolejny krok.";
            if("water_buy".equals(id)){
                NpcDialogueService.GuidePurchase quote=NpcDialogueService.guidePurchase(n,p);
                if(n.getEngine().getCurrentState()==ConversationStates.BUY_PRICE_OFFERED){
                    if(quote!=null&&"woda".equals(quote.item)&&quote.amount==1&&p.getNumberOfEquipped("money")>=quote.price){
                        anchor="purchase";
                        text="Wybierz „"+NpcDialogueService.guideReply(n,p,"confirm")+"”. Kupisz 1 wodę za "+quote.price+" monet. Po zakupie zanieś ją Hayunnowi.";
                    }else{
                        anchor="cancel_purchase";
                        String reason=quote!=null&&"woda".equals(quote.item)&&quote.amount==1
                            ?"Nie masz dość monet na tę wodę. Potrzebujesz "+quote.price+", masz "+p.getNumberOfEquipped("money")+". Możesz sprzedać Miterowi niepotrzebne łupy."
                            :"Hayunn potrzebuje tylko 1 wody. Ten zakup nie pasuje do jego prośby.";
                        text=reason+"\n\nWybierz „"+NpcDialogueService.guideReply(n,p,"cancel")+"”, żeby wrócić do sklepu bez wydawania pieniędzy.";
                    }
                }else if(n.getEngine().getCurrentState()==ConversationStates.SELL_PRICE_OFFERED){
                    text="Sprawdź, ile Miter zapłaci za twój przedmiot. Jeśli chcesz go sprzedać, wybierz „"+NpcDialogueService.guideReply(n,p,"confirm")+"”. Potem wróć do zakupu wody.";
                }else if(NpcDialogueService.guideShopOpen(n,p)){
                    anchor="shop";
                    text="W sklepie znajdź wiersz „woda”. Ustaw ilość 1 i kliknij przy nim „Kup”. Samo otwarcie sklepu niczego nie kupuje.\n\nJeśli sklep graficzny jest wyłączony, wpisz w rozmowie: kupię 1 woda.";
                }
            }
        }else if(n!=null&&n.getAttending()!=null&&p.getZone().getName().equals(zone)){
            anchor="wait";text=name+" rozmawia teraz z innym graczem. Poczekaj, aż skończy, i napisz „cześć”.\n\n"+text;
        }else{
            anchor="greet";
            String who="Hayunn".equals(name)?"Hayunna":"Miter".equals(name)?"Mitera":"Mistrz Faumonii".equals(name)?"Mistrza Faumonii":name;
            text="Podejdź do "+who+". Kliknij pole czatu na dole, wpisz „cześć” i naciśnij Enter.";
        }
        return goal(p,id,title,text,zone,x,y,name,anchor,true);
    }
    private static TutorialGuideSnapshot goal(Player p,String id,String title,String text,String target,int x,int y,String waypoint,String anchor,boolean automatic){
        String zone=p.getZone().getName();
        // Do not leave a newcomer with an old conversation covering the next destination.
        for(String name:Arrays.asList("Hayunn","Miter","Mistrz Faumonii","Sedro","Soro")){
            SpeakerNPC speaking=NPCList.get().get(name);
            if(speaking!=null&&speaking.getAttending()==p&&speaking.getEngine().getCurrentState()!=ConversationStates.IDLE
                    &&speaking.getZone()!=null&&zone.equals(speaking.getZone().getName())&&!name.equals(waypoint)){
                String goodbye=NpcDialogueService.guideReply(speaking,p,"goodbye");
                String close=goodbye.isEmpty()?"Zamknij okno rozmowy przyciskiem X.":"Wybierz „"+goodbye+"” albo zamknij okno rozmowy przyciskiem X.";
                return new TutorialGuideSnapshot(id,"Zakończ rozmowę",close+"\n\nNastępny cel: "+title+".",zone,speaking.getX(),speaking.getY(),name,"farewell",completed(p),automatic);
            }
        }
        if(!zone.equals(target)){
            anchor="walk";
            if(CITY.equals(zone)){if(HOUSE.equals(target)){x=16;y=34;waypoint="Wejście do domku startowego";}else if(MASTER.equals(target)){x=29;y=40;waypoint="Dom Mistrza Faumonii";text="Wejdź do wskazanego czerwonego domu. W środku wybierz pierwsze drzwi po lewej - tam czeka Mistrz Faumonii.\n\n"+text;}else if(CAVE.equals(target)){x=25;y=50;waypoint="Niebieskie wejście do jaskini";}}
            else if(HOUSE.equals(zone)){x=13;y=16;waypoint="Wyjście z domku";}
            else if(MASTER.equals(zone)){x=p.getX()<10?9:14;y=p.getX()<10?5:1;waypoint="Wyjście z domu Mistrza";}
            else if(CAVE.equals(zone)){x=31;y=76;waypoint="Schody do Semos";}
            else{x=-1;y=-1;waypoint="Wróć do Semos";}
            text="Najpierw przejdź do: "+waypoint+".\n\n"+text;
        }else if(MASTER.equals(zone)&&p.getX()>=10){x=11;y=5;waypoint="Wejście do sali Mistrza";anchor="walk";text="Przejdź do sali Mistrza.\n\n"+text;}
        return new TutorialGuideSnapshot(id,title,text,zone,x,y,waypoint,anchor,completed(p),automatic);
    }
}
