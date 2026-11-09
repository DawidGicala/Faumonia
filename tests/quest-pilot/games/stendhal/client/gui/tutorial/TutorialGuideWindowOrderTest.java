package games.stendhal.client.gui.tutorial;

import java.awt.Window;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

/** Observe show/raise requests without displaying any windows on the desktop. */
public class TutorialGuideWindowOrderTest {
    private static class ObservedWindow extends Window {
        private static final long serialVersionUID=1L;
        boolean shown;int showCalls,raiseCalls;
        ObservedWindow(){super((Window)null);}
        @Override public boolean isVisible(){return shown;}
        @Override public void setVisible(boolean value){shown=value;showCalls++;}
        @Override public void toFront(){raiseCalls++;}
    }
    @Test public void guideRefreshDoesNotRaiseItOverConversation() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            ObservedWindow guide=new ObservedWindow(),conversation=new ObservedWindow();conversation.shown=true;
            try{
                TutorialGuideController.updateVisibility(guide,true,conversation);
                assertEquals(1,guide.showCalls);assertEquals(1,conversation.raiseCalls);
                for(int i=0;i<10;i++)TutorialGuideController.updateVisibility(guide,true,conversation);
                assertEquals(1,guide.showCalls);assertEquals(1,conversation.raiseCalls);
                TutorialGuideController.updateVisibility(guide,false,conversation);assertFalse(guide.shown);
                conversation.shown=false;TutorialGuideController.updateVisibility(guide,true,conversation);
                assertTrue(guide.shown);assertEquals(1,conversation.raiseCalls);
            }finally{guide.dispose();conversation.dispose();}
        });
    }
}
