package games.stendhal.server.core.rp.quest;
import games.stendhal.common.QuestProgress;
import games.stendhal.common.QuestProgress.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import java.util.*;

/** Reads existing collection formats without rewriting a player's save. */
public final class CollectionQuestProgress {
    private CollectionQuestProgress() { }
    public static List<IQuest> all(){return Arrays.<IQuest>asList(new AdosBadgeQuest(), new AndrzejCollector(), new BanditItemQuest(), new CloakCollector(), new CloakCollector2(), new CloaksForBario(), new DailyGeneralQuest(), new DailyItemQuest(), new DailyMonsterQuest(), new DailyMonsterQuest1(), new DailyMonsterQuest2(), new DragonCave(), new FadoBadgeQuest(), new KirdnehBadgeQuest(), new NalworBadgeQuest(), new SemosBadgeQuest(), new StuffForBaldemar(), new StuffForVulcanus(), new SuppliesForPhalk(), new SwordsForNnyddion(), new TakeGoldforGrafindle(), new ToysCollector(), new WeaponsCollector(), new WeaponsCollector2(), new WeeklyCollectorQuest(), new WeeklyItemQuest(), new ememedal(), new obsmedal(), new rubmedal(), new sapmedal());}
    public static List<IQuest> forNpc(String name){
        List<IQuest> found=new ArrayList<IQuest>();
        for(IQuest q:all())if(name.equalsIgnoreCase(q.getNPCName())||q instanceof TakeGoldforGrafindle&&"Lorithien".equals(name)||q instanceof SuppliesForPhalk&&Arrays.asList("Wrvil","Mrotho").contains(name))found.add(q);
        return found;
    }
    public static String missingReward(IQuest q){
        String item=q instanceof ememedal?"szmaragdowy medal":q instanceof sapmedal?"obsydianowy medal":q instanceof rubmedal?"szafirowy medal":q instanceof obsmedal?"diamentowy medal":"";
        return !item.isEmpty()&&games.stendhal.server.core.engine.SingletonRepository.getEntityManager().getItem(item)==null?item:"";
    }
    public static boolean supports(IQuest q){return q instanceof NativeCollectionDetails;}
    public static NativeCollectionDetails details(IQuest q){return (NativeCollectionDetails)q;}
    public static String kind(IQuest q){return details(q).getCollectionKind();}
    public static boolean done(IQuest q,Player p){return q instanceof CloakCollector2?"done;rewarded".equals(p.getQuest(q.getSlotName())):q.isCompleted(p);}
    public static Map<String,Integer> remaining(IQuest q,Player p){
        NativeCollectionDetails d=details(q);Map<String,Integer> all=d.getCollectionItems(),r=new LinkedHashMap<String,Integer>();String v=p.getQuest(q.getSlotName());
        if(v==null||"rejected".equals(v))return all;
        if(done(q,p)||v.startsWith("forging;")||q instanceof CloakCollector2&&"done".equals(v))return r;
        String k=d.getCollectionKind();
        if("sequence".equals(k)){
            String[] parts=v.split(";",-1);if(parts.length!=all.size()+1||!"start".equals(parts[0]))throw new IllegalArgumentException("Unknown sequence");
            int index=1;for(Map.Entry<String,Integer> e:all.entrySet()){int count=Integer.parseInt(parts[index++]);if(count<0||count>e.getValue())throw new IllegalArgumentException("Invalid contribution");if(count<e.getValue())r.put(e.getKey(),e.getValue()-count);}
        }else if("list".equals(k)){
            Set<String> seen=new HashSet<String>();for(String n:v.split(";"))if(!n.isEmpty()){if(!all.containsKey(n)||!seen.add(n))throw new IllegalArgumentException("Unknown collected item");}
            for(String n:all.keySet())if(!seen.contains(n))r.put(n,1);
        }else if("collection".equals(k)||"random".equals(k)||"badge".equals(k)){
            String record="random".equals(k)?v.split(";",-1)[0]:v;
            for(String n:record.split(";",-1)){String[] pair=n.split("=",-1);if(pair.length!=2||!all.containsKey(pair[0])||r.containsKey(pair[0]))throw new IllegalArgumentException("Unknown request");int count=Integer.parseInt(pair[1]);if(count<=0)throw new IllegalArgumentException("Invalid request");r.put(pair[0],count);}
            if(!"collection".equals(k)&&r.size()!=1)throw new IllegalArgumentException("Expected one request");
        }else if("count".equals(k)){
            int n=Integer.parseInt(v);String item=all.keySet().iterator().next();if(n<1||n>all.get(item))throw new IllegalArgumentException("Invalid count");r.put(item,n);
        }else if(q instanceof TakeGoldforGrafindle&&"lorithien".equals(v))r.putAll(all);
        else if(q instanceof SuppliesForPhalk){
            if("start".equals(v))r.putAll(JourneyQuestProgress.items("kanapka",3,"sok z chmielu",3,"napój z winogron",3));
            else if(v.matches("clothes;(none|cloak);(none|armor)"))r.putAll(JourneyQuestProgress.items("płaszcz krasnoludzki",1,"zbroja krasnoludzka",1));
            else throw new IllegalArgumentException("Unknown supply stage");
        }
        return r;
    }
    public static String itemText(Map<String,Integer> items){StringBuilder b=new StringBuilder();for(Map.Entry<String,Integer> e:items.entrySet()){if(b.length()>0)b.append(", ");b.append("money".equals(e.getKey())?"monety":e.getKey()).append(" - ").append(e.getValue());}return b.toString();}
    private static QuestProgress snap(IQuest q,State state,String npc,String hint,List<Objective> goals){return new QuestProgress(q.getName(),state,npc,hint,goals);}
    public static QuestProgress read(IQuest q,Player p){
        if(!supports(q)||!p.hasQuest(q.getSlotName()))return null;
        NativeCollectionDetails d=details(q);String v=p.getQuest(q.getSlotName()),k=d.getCollectionKind(),npc=q.getNPCName();List<Objective> goals=new ArrayList<Objective>();
        if(done(q,p))return snap(q,State.COMPLETED,npc,"Zadanie ukończone.",goals);
        if("rejected".equals(v))return snap(q,State.DECLINED,npc,"Możesz wrócić do rozmowy, jeśli zdecydujesz się pomóc.",goals);
        try{
            if(v.startsWith("forging;")&&"sequence".equals(k)){
                long start=Long.parseLong(v.split(";",-1)[1]),left=start+d.getCollectionWait()*60000L-System.currentTimeMillis();
                return snap(q,left>0?State.ACTIVE:State.READY_TO_DELIVER,npc,left>0?"Przygotowanie trwa. Pozostało około "+Math.max(1,(left+59999)/60000)+" min. Rzemieślnik ma już potrzebne materiały.":"Przygotowanie zakończone. Wróć do rozmówcy i odbierz nagrodę.",goals);
            }
            if(q instanceof CloakCollector2&&"done".equals(v))return snap(q,State.READY_TO_DELIVER,npc,"Kolekcja jest kompletna. Josephine ma dla ciebie nieodebrane buty zabójcy.",goals);
            if("dailykill".equals(k)){
                String[] tokens=v.split(";",-1)[0].split(",",-1);if(tokens.length!=5)throw new IllegalArgumentException("Invalid hunt record");
                int ns=Integer.parseInt(tokens[1]),nh=Integer.parseInt(tokens[2]),bs=Integer.parseInt(tokens[3]),bh=Integer.parseInt(tokens[4]);if(ns<0||nh<0||bs<0||bh<0||ns+nh<=0)throw new IllegalArgumentException("Invalid hunt baseline");
                int solo=Math.max(0,p.getSoloKill(tokens[0])-bs),shared=Math.max(0,p.getSharedKill(tokens[0])-bh);
                if(ns>0)goals.add(new Objective("solo:"+tokens[0],tokens[0]+" (samodzielnie)",Math.min(solo,ns),ns,-1));
                goals.add(new Objective("kill:"+tokens[0],tokens[0],Math.min(solo+shared,ns+nh),ns+nh,-1));
                boolean ready=new games.stendhal.server.entity.npc.condition.KilledForQuestCondition(q.getSlotName(),0).fire(p,null,null);
                return snap(q,ready?State.READY_TO_DELIVER:State.ACTIVE,npc,d.getCollectionHint(),goals);
            }
            if(q instanceof TakeGoldforGrafindle&&"start".equals(v))return snap(q,State.ACTIVE,"Lorithien","Odbierz przesyłkę złota na poczcie w Nalwor, a potem wróć do Grafindla w banku.",goals);
            Map<String,Integer> left=remaining(q,p);boolean ready=true;
            if(left.isEmpty()&&!"sequence".equals(k))throw new IllegalArgumentException("Unknown empty phase");
            for(Map.Entry<String,Integer> e:left.entrySet()){
                int count=q instanceof SuppliesForPhalk&&v.startsWith("clothes;")?JourneyQuestProgress.personalItemCount(p,e.getKey(),"Phalk"):p.getNumberOfEquipped(e.getKey());
                boolean cumulative=Arrays.asList("sequence","list","collection","count").contains(k);
                goals.add(new Objective(e.getKey(),e.getKey()+(q instanceof SuppliesForPhalk&&v.startsWith("clothes;")?" (dla Phalka)":""),cumulative?0:Math.min(count,e.getValue()),e.getValue(),cumulative?count:-1));ready&=count>=e.getValue();
            }
            if(!d.getCollectionKill().isEmpty()){boolean killed=d.getCollectionSolo()?p.hasKilledSolo(d.getCollectionKill()):p.hasKilled(d.getCollectionKill());goals.add(new Objective("kill:"+d.getCollectionKill(),d.getCollectionKill()+(d.getCollectionSolo()?" (samodzielnie)":""),killed?1:0,1,-1));ready&=killed;}
            return snap(q,ready?State.READY_TO_DELIVER:State.ACTIVE,npc,d.getCollectionHint(),goals);
        }catch(IllegalArgumentException ex){return snap(q,State.UNKNOWN,npc,"Nie można wyświetlić postępu tego zadania. Zgłoś problem administracji, podając nazwę zadania.",Collections.<Objective>emptyList());}
    }
    public static String progressText(IQuest q,Player p){
        NativeCollectionDetails d=details(q);QuestProgress progress=read(q,p);
        if(!missingReward(q).isEmpty())return "Tego odznaczenia nie można obecnie zdobyć. Zachowaj pozostałe materiały.";
        if(progress==null){
            StringBuilder b=new StringBuilder(d.getCollectionHint());
            if("badge".equals(d.getCollectionKind()))for(Map.Entry<String,String> e:d.getCollectionPrerequisites().entrySet())b.append(" ").append(p.isQuestCompleted(e.getKey())?"Ukończone: ":"Do zrobienia: ").append(e.getValue()).append('.');
            else if(!Arrays.asList("random","dailykill").contains(d.getCollectionKind()))b.append(" Potrzebne rzeczy: ").append(itemText(d.getCollectionItems())).append('.');
            return b.toString();
        }
        StringBuilder b=new StringBuilder(progress.getNextStep());
        if(!progress.getObjectives().isEmpty()){b.append(" Postęp: ");for(Objective o:progress.getObjectives())b.append(o.getLabel()).append(" - ").append(o.getCurrent()).append('/').append(o.getRequired()).append(o.getCarried()>=0?" (masz: "+o.getCarried()+")":"").append("; ");}
        return b.toString();
    }
    public static List<String> history(IQuest q,Player p){return p.hasQuest(q.getSlotName())?Arrays.asList(progressText(q,p)):Collections.<String>emptyList();}
}
