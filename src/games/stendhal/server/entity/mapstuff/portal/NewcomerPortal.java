package games.stendhal.server.entity.mapstuff.portal;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.core.events.NewcomerGuide;
/** Both exits from the starting area share the same real completion requirements. */
public final class NewcomerPortal extends AccessCheckingPortal {
    public NewcomerPortal(){super((String)null);}
    @Override protected boolean isAllowed(RPEntity user){return user instanceof Player&&NewcomerGuide.canLeave((Player)user);}
    @Override public boolean onUsed(RPEntity user){
        boolean leaving=user instanceof Player&&NewcomerGuide.inStart((Player)user);
        boolean used=super.onUsed(user);
        if(used&&leaving)NewcomerGuide.crossedBarrier((Player)user);
        return used;
    }
    @Override protected void rejected(RPEntity user){if(user instanceof Player)sendMessage(user,"Przed dalszą drogą dokończ: "+NewcomerGuide.missing((Player)user)+". Przycisk Przewodnik przy czacie wskaże następny krok. Pomoc graczy: /k twoje pytanie.");}
}
