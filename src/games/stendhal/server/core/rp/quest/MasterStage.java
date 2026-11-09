package games.stendhal.server.core.rp.quest;

import games.stendhal.common.Level;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.npc.action.DialogueRewardAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.semos.masterhouse.MasterNPC.MasterSpeaker;
import java.util.*;
import marauroa.common.Pair;

/** Registers the existing stage actions on one NPC, guarded by its selected topic. */
public final class MasterStage {
    public static final String NPC_NAME="Mistrz Faumonii";
    private static final int[] LEVELS={0,0,5,10,15,20,30,35,40,60,70,75,80,100,110,120,150,180,200,220,240,250,250,260,300,320};
    public final int number;
    public final IQuest quest;
    private final MasterSpeaker npc;
    private final List<Greeting> greetings=new ArrayList<Greeting>();
    private Map<String,Pair<Integer,Integer>> targets=Collections.emptyMap();
    private static final class Greeting {
        final ChatCondition condition; final ConversationStates state; final String text;
        Greeting(ChatCondition c,ConversationStates s,String t){condition=c;state=s;text=t;}
    }
    public MasterStage(MasterSpeaker npc,int number,IQuest quest){this.npc=npc;this.number=number;this.quest=quest;}
    public static MasterStage register(int number,IQuest quest){
        SpeakerNPC n=NPCList.get().get(NPC_NAME);
        if(!(n instanceof MasterSpeaker))throw new IllegalStateException("Brak wspólnego Mistrza Faumonii");
        return ((MasterSpeaker)n).stage(number,quest);
    }
    public String getName(){return NPC_NAME;}
    public String slot(){return "main_task_"+number;}
    public int level(){return LEVELS[number];}
    public static int rewardXP(int level){
        if(level<0||level>=Level.maxLevel())throw new IllegalArgumentException("Unsupported master level: "+level);
        long difference=(long)Level.getXP(level+1)-Level.getXP(level);
        return (int)Math.min(Integer.MAX_VALUE,difference*5L/2L);
    }
    public boolean completed(Player p){return p.isQuestCompleted(slot());}
    public boolean active(Player p){return p.hasQuest(slot())&&!completed(p)&&!"rejected".equals(p.getQuest(slot(),0));}
    public boolean visible(Player p){return number==1||active(p)||completed(p)||p.isQuestCompleted("main_task_"+(number-1));}
    public boolean accessible(Player p){return !completed(p)&&(active(p)||(visible(p)&&p.getLevel()>=level()));}
    public void setHuntTargets(Map<String,Pair<Integer,Integer>> values){targets=new LinkedHashMap<String,Pair<Integer,Integer>>(values);}
    public Map<String,Pair<Integer,Integer>> targets(){return Collections.unmodifiableMap(targets);}
    public boolean hunt(){return !targets.isEmpty();}
    public String itemName(Player p){
        if(quest instanceof MultiStepMasterQuest)return ((MultiStepMasterQuest)quest).masterItem(p);
        if(!active(p)||hunt())return "";
        if(number==24)return p.getQuest(slot(),1);
        try{return p.getRequiredItemName(slot(),0);}catch(RuntimeException e){return "";}
    }
    public int itemAmount(Player p){
        if(quest instanceof MultiStepMasterQuest)return ((MultiStepMasterQuest)quest).masterItemAmount(p);
        if(!active(p)||hunt())return 0;
        try{return number==24?Integer.parseInt(p.getQuest(slot(),2)):p.getRequiredItemQuantity(slot(),0);}catch(RuntimeException e){return 0;}
    }
    public Map<String,int[]> kills(Player p){
        Map<String,int[]> result=new LinkedHashMap<String,int[]>();
        if(!active(p)||!hunt())return result;
        String value=p.getQuest(slot(),1);if(value==null)return result;
        String[] parts=value.split(",");
        if(parts.length%5!=0)return result;
        try {for(int i=0;i<parts.length;i+=5){
            String name=parts[i];int solo=Integer.parseInt(parts[i+1]),shared=Integer.parseInt(parts[i+2]);
            int doneSolo=Math.max(0,p.getSoloKill(name)-Integer.parseInt(parts[i+3]));
            int doneShared=Math.max(0,p.getSoloKill(name)+p.getSharedKill(name)-Integer.parseInt(parts[i+3])-Integer.parseInt(parts[i+4])-solo);
            if(solo>0)result.put(name+" (solo)",new int[]{Math.min(solo,doneSolo),solo});
            if(shared>0)result.put(name,new int[]{Math.min(shared,doneShared),shared});
        }}catch(NumberFormatException e){result.clear();}
        return result;
    }
    public String summary(Player p){
        if(quest instanceof MultiStepMasterQuest)return ((MultiStepMasterQuest)quest).masterSummary(p);
        StringBuilder b=new StringBuilder("Etap ").append(number).append(" - wymagany poziom: ").append(level()).append(". ");
        if(completed(p))return b.append(MasterStory.completed(number)).toString();
        if(p.getLevel()<level()&&!active(p))return b.append("Zdobywaj doświadczenie i wróć, gdy będziesz gotowy. W dzienniku możesz sprawdzić pozostałe zadania mieszkańców.").toString();
        b.append("Nagroda EXP za ukończenie tego etapu: ").append(rewardXP(level())).append(". ");
        if(!active(p))return b.append(MasterStory.introduction(number)).toString();
        if(hunt()){
            Map<String,int[]> counts=kills(p);
            b.append("\nPostęp polowania:\n");
            for(Map.Entry<String,int[]> e:counts.entrySet())b.append(e.getKey()).append(" ").append(e.getValue()[0]).append("/").append(e.getValue()[1]).append("\n");
            if(counts.isEmpty())b.append("nie można go teraz wyświetlić. Zgłoś problem administracji. ");
            else b.append("Po wypełnieniu warunków zdaj mi sprawę z zadania.");
        }else{
            String item=itemName(p);int amount=itemAmount(p);
            if(item!=null&&!item.isEmpty()&&amount>0)b.append("\nPrzynieś: ").append(item).append(" - ").append(amount).append(". Masz przy sobie: ").append(p.getNumberOfEquipped(item)).append(".");
            else b.append("Nie można teraz ustalić dalszych celów tej próby. Zachowaj przedmioty i zgłoś problem administracji.");
        }
        return b.toString();
    }
    public void open(Player p){
        npc.select(this,p);npc.getEngine().setCurrentState(ConversationStates.ATTENDING);
        npc.say(summary(p));
        if(quest instanceof MultiStepMasterQuest||!accessible(p)||!active(p)||hunt())return;
        Sentence hello=ConversationParser.parse("hi");
        for(Greeting g:greetings)if(g.condition==null||g.condition.fire(p,hello,npc)){
            // Opening a topic is read-only. Only the later explicit answer can deliver.
            npc.getEngine().setCurrentState(g.state);break;
        }
    }
    public void add(ConversationStates state,String trigger,ChatCondition condition,ConversationStates next,String reply,ChatAction action){add(state,Arrays.asList(trigger),condition,next,reply,action);}
    public void add(final ConversationStates state,Collection<String> triggers,final ChatCondition condition,ConversationStates next,String reply,final ChatAction action){
        if(state==ConversationStates.IDLE){
            if(action!=null)throw new IllegalArgumentException("Master greeting must be read-only");
            greetings.add(new Greeting(condition,next,reply));return;
        }
        ChatCondition guarded=(p,s,e)->npc.selected(p)==this&&accessible(p)
            &&(state!=ConversationStates.QUEST_OFFERED||!active(p))
            &&(condition==null||condition.fire(p,s,e));
        ChatAction wrapped=(p,s,n)->{
            boolean done=completed(p);
            if(action!=null)action.fire(p,s,n);
            if(!done&&completed(p))DialogueRewardAction.xp(rewardXP(level())).fire(p,s,n);
        };
        npc.add(state,triggers,guarded,next,MasterStory.clean(reply),wrapped);
    }
}
