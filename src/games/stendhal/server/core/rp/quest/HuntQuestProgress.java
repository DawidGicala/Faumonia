package games.stendhal.server.core.rp.quest;

import games.stendhal.common.QuestProgress;
import games.stendhal.common.QuestProgress.Objective;
import games.stendhal.common.QuestProgress.State;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.KilledInSumForQuestCondition;
import games.stendhal.server.maps.quests.*;
import java.util.*;
import marauroa.common.Pair;

/** Interprets native kill baselines by creature name, never by map iteration order. */
public final class HuntQuestProgress {
    private HuntQuestProgress() { }
    public static IQuest forNpc(String name) {
        switch(name) {
            case "Walt":return new KillAdosSpiders(); case "Orchiwald":return new KillAlbinos();
            case "Cal":return new KillAmazon(); case "Dodo":return new KillAngels();
            case "Kape":return new KillBarbarians(); case "Think":return new KillChaos();
            case "Ivar":return new KillChaosMonsters(); case "Maerion":return new KillDarkElves();
            case "Django":return new KillDragons(); case "Loretta":return new KillDwarfs();
            case "Linda":return new KillEasyMonsters(); case "Lorithien":return new KillElfes();
            case "Yance":return new KillFairy(); case "Verdan":return new KillForVerdan();
            case "Wod":return new KillForWod(); case "Rade":return new KillGiants();
            case "Jenny":return new KillGnomes(); case "Octavio":return new KillHigh();
            case "Fag":return new KillLast(); case "JJ":return new KillMadaram();
            case "Staro":return new KillMag(); case "Bate":return new KillMediumMonsters();
            case "Andy":return new KillMonks(); case "Sausid":return new KillSausid();
            case "Morgrin":return new KillSpiders(); case "Luck":return new KillStone();
            case "Natan":return new KillXeno(); case "John":return new CleanAthorsUnderground();
            case "Thorgal":return new CollectChaosThorgal(); default:return null;
        }
    }
    public static boolean supports(IQuest q){return q instanceof NativeHuntDetails||q instanceof KillSpiders||q instanceof KillDarkElves||q instanceof CollectChaosThorgal;}
    private static QuestProgress snap(IQuest q,State state,String hint,List<Objective> goals){return new QuestProgress(q.getName(),state,q.getNPCName(),hint,goals);}
    public static String targetText(NativeHuntDetails q,Player p){
        StringBuilder b=new StringBuilder();int sum=q.getDialogueHuntTotal(p);
        if(sum>0)b.append("Łącznie ").append(sum).append(" przeciwników z grupy: ");
        for(Map.Entry<String,Pair<Integer,Integer>> e:q.getDialogueHuntTargets(p).entrySet()){
            if(b.length()>0&&b.charAt(b.length()-1)!=' ')b.append(", ");b.append(e.getKey());
            if(sum==0)b.append(" - ").append(e.getValue().first()+e.getValue().second());
        }
        return b.toString();
    }
    public static QuestProgress read(IQuest q,Player p){
        if(!supports(q)||!p.hasQuest(q.getSlotName()))return null;
        if(q.isCompleted(p))return snap(q,State.COMPLETED,"Sprawa zakończona. Nagroda została rozliczona.",Collections.<Objective>emptyList());
        String step=p.getQuest(q.getSlotName(),0);
        if("rejected".equals(step))return snap(q,State.DECLINED,"Wróć do rozmowy, jeśli zdecydujesz się pomóc.",Collections.<Objective>emptyList());
        try {
            if(q instanceof NativeHuntDetails)return recorded(q,(NativeHuntDetails)q,p);
            if(q instanceof KillSpiders||q instanceof KillDarkElves)return flagged(q,p,step);
            if(q instanceof CollectChaosThorgal&&"start".equals(step)){
                List<Objective> goals=new ArrayList<Objective>();boolean ready=true;
                for(String item:CollectChaosThorgal.getRequiredItems()){
                    int count=p.getNumberOfEquipped(item);ready&=count>0;goals.add(new Objective(item,item,Math.min(1,count),1,-1));
                }
                return snap(q,ready?State.READY_TO_DELIVER:State.ACTIVE,"Thorgal potrzebuje całego zestawu chaosu jednocześnie. Wróć do niego w tunelach pod Semos z siedmioma elementami; po dostawie otworzy dla ciebie handel zwojami.",goals);
            }
        }catch(IllegalArgumentException e){ /* Do not repair or reset unfamiliar quest saves from a view. */ }
        return snap(q,State.UNKNOWN,"Nie można wyświetlić postępu tego zadania. Zgłoś problem administracji, podając nazwę zadania.",Collections.<Objective>emptyList());
    }
    private static QuestProgress recorded(IQuest q,NativeHuntDetails h,Player p){
        if(!"start".equals(p.getQuest(q.getSlotName(),0)))throw new IllegalArgumentException("Unknown hunt phase");
        String record=p.getQuest(q.getSlotName(),h.getDialogueKillIndex());
        if(record==null)throw new IllegalArgumentException("Missing baseline");
        String[] tokens=record.split(",",-1);Map<String,Pair<Integer,Integer>> targets=h.getDialogueHuntTargets(p);
        if(tokens.length==0||tokens.length%5!=0||tokens.length>320||tokens.length/5!=targets.size())throw new IllegalArgumentException("Invalid baseline size");
        Map<String,int[]> counts=new TreeMap<String,int[]>();int total=0,required=h.getDialogueHuntTotal(p);
        for(int i=0;i<tokens.length;i+=5){
            String name=tokens[i];Pair<Integer,Integer> target=targets.get(name);
            if(target==null||counts.containsKey(name))throw new IllegalArgumentException("Unknown or repeated target");
            int soloNeed=Integer.parseInt(tokens[i+1]),sharedNeed=Integer.parseInt(tokens[i+2]);
            int soloBase=Integer.parseInt(tokens[i+3]),sharedBase=Integer.parseInt(tokens[i+4]);
            if(soloNeed<0||sharedNeed<0||soloBase<0||sharedBase<0)throw new IllegalArgumentException("Negative hunt data");
            // Recorded requirements remain authoritative for an already accepted mission.
            int solo=Math.max(0,p.getSoloKill(name)-soloBase),shared=Math.max(0,p.getSharedKill(name)-sharedBase);
            counts.put(name,new int[]{soloNeed,sharedNeed,solo,shared});total+=solo+shared;
        }
        List<Objective> goals=new ArrayList<Objective>();
        if(required>0)goals.add(new Objective("hunt_total","Łączny postęp wyprawy",Math.min(total,required),required,-1));
        for(Map.Entry<String,int[]> e:counts.entrySet()){
            int[] c=e.getValue();
            if(required==0) {
                if(c[0]>0)goals.add(new Objective("solo:"+e.getKey(),e.getKey()+" (samodzielnie)",Math.min(c[2],c[0]),c[0],-1));
                if(c[0]+c[1]<=0)throw new IllegalArgumentException("Missing target count");
                goals.add(new Objective("kill:"+e.getKey(),e.getKey(),Math.min(c[2]+c[3],c[0]+c[1]),c[0]+c[1],-1));
            }
        }
        boolean ready=required>0?new KilledInSumForQuestCondition(q.getSlotName(),h.getDialogueKillIndex(),required).fire(p,null,null):new KilledForQuestCondition(q.getSlotName(),h.getDialogueKillIndex()).fire(p,null,null);
        return snap(q,ready?State.READY_TO_DELIVER:State.ACTIVE,ready?"Wymagania wyprawy są spełnione. Wróć do zleceniodawcy i złóż meldunek.":h.getDialogueHuntHint(p)+" Liczą się walki od przyjęcia tej wyprawy, także zaliczone wspólnie.",goals);
    }
    private static QuestProgress flagged(IQuest q,Player p,String step){
        boolean spiders=q instanceof KillSpiders;
        if(!"start".equals(step)&&!"started".equals(step))throw new IllegalArgumentException("Unknown observed hunt");
        boolean legacy="start".equals(step);
        List<String> names=spiders?KillSpiders.getDialogueCreatures():legacy?Arrays.asList("elf ciemności łucznik","elf ciemności kapitan","coś"):KillDarkElves.getDialogueCreatures();
        List<Objective> goals=new ArrayList<Objective>();boolean ready=true;int index=1;
        for(String name:names){boolean done=legacy?p.hasKilled(name):name.equals(p.getQuest(q.getSlotName(),index++));ready&=done;goals.add(new Objective("kill:"+name,name,done?1:0,1,-1));}
        if(!spiders){int count=p.getNumberOfEquipped("amulet");ready&=count>0;goals.add(new Objective("amulet","Amulet mutanta coś",Math.min(1,count),1,-1));}
        return snap(q,ready?State.READY_TO_DELIVER:State.ACTIVE,spiders?"Zejdź do piwnicy szkoły magii. Pokonaj osobiście pająka, ptasznika i królową z tej piwnicy, potem wróć do Morgrina.":"Przejdź przez sekretny pokój poniżej królewskiej sali Nalwor. Pokonaj osobiście wskazane mroczne elfy w ich kryjówce i przynieś Maerionowi amulet z mutanta coś.",goals);
    }
    public static String progressText(IQuest q,Player p){
        QuestProgress progress=read(q,p);if(progress==null)return "Omów ze mną wyprawę, a ustalimy jej cel.";
        StringBuilder text=new StringBuilder(progress.getNextStep());
        for(Objective o:progress.getObjectives())if(o.getRequired()>0&&o.getCurrent()<o.getRequired())text.append("\n").append(o.getLabel()).append(": ").append(o.getCurrent()).append('/').append(o.getRequired());
        return text.toString();
    }
    public static List<String> history(IQuest q,Player p){
        List<String> rows=new ArrayList<String>();QuestProgress progress=read(q,p);if(progress==null)return rows;
        if(progress.getState()==State.COMPLETED){
            rows.add("Wyprawa została rozliczona. Odebrałem nagrodę od NPC: "+q.getNPCName()+".");
            if(q.isRepeatable(p))rows.add("Mogę wrócić i zapytać o kolejne zlecenie.");
        }else if(progress.getState()==State.DECLINED)rows.add("Odmówiłem udziału w wyprawie. Mogę wrócić do rozmowy.");
        else {
            rows.add("Zleceniodawca: "+q.getNPCName()+". "+progress.getNextStep());
            for(Objective o:progress.getObjectives())rows.add(o.getLabel()+": "+o.getCurrent()+" / "+o.getRequired());
        }
        return rows;
    }
}
