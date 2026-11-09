package games.stendhal.common;

import java.util.*;

/** Private, bounded description of one authoritative server-side tutorial goal. */
public final class TutorialGuideSnapshot {
    public static final String EVENT="tutorial_guide";
    public final String step,title,text,zone,waypoint,anchor;
    public final int x,y,completed;
    public final boolean automatic;
    public TutorialGuideSnapshot(String step,String title,String text,String zone,int x,int y,
            String waypoint,String anchor,int completed,boolean automatic){
        this.step=bounded(step,80);this.title=bounded(title,160);this.text=bounded(text,2400);
        this.zone=bounded(zone,160);this.waypoint=bounded(waypoint,200);this.anchor=bounded(anchor,30);
        if(x < -1||y < -1||x>10000||y>10000||completed<0||completed>4)throw new IllegalArgumentException("Invalid guide coordinates/progress");
        this.x=x;this.y=y;this.completed=completed;this.automatic=automatic;
    }
    private static String bounded(String value,int max){if(value==null||value.length()>max)throw new IllegalArgumentException("Invalid guide field");return value;}
    /** Short map instruction, without adding protocol fields. */
    public String actionHint() {
        if ("walk".equals(anchor)) return "Przejdź tędy";
        if ("greet".equals(anchor)) return "Przywitaj się: „cześć”";
        if ("wait".equals(anchor)) return "Poczekaj na swoją kolej";
        if ("shop".equals(anchor)) return "Wybierz wodę w sklepie";
        if ("purchase".equals(anchor)) return "Potwierdź zakup wody";
        if ("cancel_purchase".equals(anchor)) return "Wróć do zakupu jednej wody";
        if ("farewell".equals(anchor)) return "Zakończ rozmowę";
        if ("reply".equals(anchor)) return "Wybierz odpowiedź w rozmowie";
        if ("water_buy".equals(step)) return "Kup 1 wodę za 5 monet";
        if ("water_return".equals(step)) return "Oddaj butelkę wody";
        if ("cheese_return".equals(step)) return "Oddaj sery Soro";
        if ("cheese_hunt".equals(step)) return "Zbierz ser ze zwłok szczurów";
        if (step.endsWith("_hunt")) return "Pokonaj wskazane potwory";
        if (step.endsWith("_return")) return "Odbierz nagrodę";
        if ("barrier".equals(step)) return "Przejdź na wschód";
        return "Sprawdź bieżący cel";
    }
    public boolean isConversationAction(){return Arrays.asList("reply","shop","purchase","cancel_purchase","farewell").contains(anchor);}
    public List<String> toWire(){return Arrays.asList("1",step,title,text,zone,Integer.toString(x),Integer.toString(y),waypoint,anchor,Integer.toString(completed),Boolean.toString(automatic));}
    public static TutorialGuideSnapshot fromWire(List<String> wire){
        if(wire==null||wire.size()!=11||!"1".equals(wire.get(0))||!("true".equals(wire.get(10))||"false".equals(wire.get(10))))throw new IllegalArgumentException("Invalid guide packet");
        return new TutorialGuideSnapshot(wire.get(1),wire.get(2),wire.get(3),wire.get(4),Integer.parseInt(wire.get(5)),Integer.parseInt(wire.get(6)),wire.get(7),wire.get(8),Integer.parseInt(wire.get(9)),Boolean.parseBoolean(wire.get(10)));
    }
}
