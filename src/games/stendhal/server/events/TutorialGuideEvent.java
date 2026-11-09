package games.stendhal.server.events;
import games.stendhal.common.TutorialGuideSnapshot;
import marauroa.common.game.*;
import marauroa.common.game.Definition.*;
public final class TutorialGuideEvent extends RPEvent {
    public static void generateRPClass(){RPClass c=new RPClass(TutorialGuideSnapshot.EVENT);c.add(DefinitionClass.ATTRIBUTE,"data",Type.VERY_LONG_STRING);}
    public TutorialGuideEvent(TutorialGuideSnapshot s){super(TutorialGuideSnapshot.EVENT);put("data",s.toWire());}
}
