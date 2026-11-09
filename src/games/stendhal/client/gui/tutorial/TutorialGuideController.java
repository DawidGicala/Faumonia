package games.stendhal.client.gui.tutorial;

import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.TutorialGuideSnapshot;
import java.awt.*;
import javax.swing.*;

/** Non-modal guide. Quest advancement remains authoritative on the server. */
public final class TutorialGuideController {
    public static final String ENABLED="tutorial_guide.enabled";
    private static final TutorialGuideController INSTANCE=new TutorialGuideController();
    public static TutorialGuideController get(){return INSTANCE;}
    private TutorialGuideSnapshot current;
    private volatile TutorialGuideSnapshot marker;
    private JDialog window;
    private Window conversationWindow;
    private TutorialGuidePanel panel;
    private boolean dismissed,manual;
    private final Timer positionTimer=new Timer(500,e->refreshVisibility());
    private TutorialGuideController(){}
    public void receive(TutorialGuideSnapshot s){
        boolean finishedNow=current!=null&&current.automatic&&!s.automatic;current=s;
        if(window==null&&j2DClient.get()!=null)createWindow();
        if(window==null)return;
        panel.showStep(s);if(finishedNow)manual=false;
        fit();refreshVisibility();positionTimer.start();
    }
    public void uiReady(){if(current!=null)receive(current);}
    public void reset(){current=null;marker=null;dismissed=false;manual=false;positionTimer.stop();if(window!=null)window.setVisible(false);}
    public boolean reopen(){if(current==null)return false;dismissed=false;manual=true;panel.expand();fit();refreshVisibility();positionTimer.start();return true;}
    public void settingsChanged(){manual=false;dismissed=false;refreshVisibility();}
    public void setConversationWindow(Window conversation){conversationWindow=conversation;}
    private boolean enabled(){return Boolean.parseBoolean(WtWindowManager.getInstance().getProperty(ENABLED,"true"));}
    private void createWindow(){
        window=new JDialog(j2DClient.get().getMainFrame());window.setUndecorated(true);window.setModal(false);window.setFocusableWindowState(false);
        panel=new TutorialGuidePanel(()->{dismissed=true;manual=false;refreshVisibility();},this::fit);window.setContentPane(panel);
    }
    private void fit(){if(window==null||j2DClient.get()==null)return;Rectangle map=j2DClient.get().getGameAreaBoundsOnScreen();
        Dimension preferred=panel.getPreferredSize();window.setSize(Math.min(preferred.width,Math.max(180,map.width-16)),Math.min(preferred.height,Math.max(96,map.height-16)));position();}
    private void position(){if(window==null||j2DClient.get()==null)return;Rectangle map=j2DClient.get().getGameAreaBoundsOnScreen();window.setLocation(map.x+Math.max(4,map.width-window.getWidth()-8),map.y+8);}
    private void refreshVisibility(){
        if(window==null||current==null)return;
        boolean visible=!dismissed&&(manual||enabled()&&current.automatic)&&j2DClient.get()!=null&&j2DClient.get().getMainFrame().isShowing()
            &&(j2DClient.get().getMainFrame().getExtendedState()&Frame.ICONIFIED)==0;
        marker=visible?current:null;fit();updateVisibility(window,visible,conversationWindow);
    }
    /** Repeated setVisible(true) raises an existing AWT window above the conversation. */
    static void updateVisibility(Window guide,boolean visible,Window conversation){
        if(guide.isVisible()==visible)return;
        guide.setVisible(visible);
        if(visible&&conversation!=null&&conversation.isVisible())conversation.toFront();
    }
    public void paintMarker(Graphics2D graphics,String zone,int x,int y,int width,int height){TutorialGuideMarker.paint(graphics,marker,zone,x,y,width,height);}
    public TutorialGuideSnapshot marker(){return marker;}
}
