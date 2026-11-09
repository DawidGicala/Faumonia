package games.stendhal.client.events;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.tutorial.TutorialGuideController;
import games.stendhal.common.TutorialGuideSnapshot;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
public final class TutorialGuideEvent extends Event<RPEntity> {
    @Override public void execute(){try{final TutorialGuideSnapshot s=TutorialGuideSnapshot.fromWire(event.getList("data"));SwingUtilities.invokeLater(()->TutorialGuideController.get().receive(s));}catch(RuntimeException ex){Logger.getLogger(getClass()).warn("Invalid tutorial guide",ex);}}
}
