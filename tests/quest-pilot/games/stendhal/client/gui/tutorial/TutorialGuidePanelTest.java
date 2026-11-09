package games.stendhal.client.gui.tutorial;

import games.stendhal.client.gui.styled.*;
import games.stendhal.common.TutorialGuideSnapshot;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TutorialGuidePanelTest {
    static TutorialGuideSnapshot sample(){return new TutorialGuideSnapshot("hayunn","Poznaj Hayunna","Podejdź do Hayunna. Kliknij pole czatu na dole, wpisz „cześć” i naciśnij Enter.","int_semos_guard_house",4,9,"Hayunn","greet",0,true);}
    static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container)layout((Container)child);}
    @Test public void foldHelpAndNewStepsKeepTheirContentAccessible() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            TutorialGuidePanel p=new TutorialGuidePanel(()->{},()->{});p.showStep(sample());p.setSize(p.getPreferredSize());layout(p);
            assertTrue(p.isExpanded());int expanded=p.getPreferredSize().height;
            click(p,"Zwiń");assertFalse(p.isExpanded());assertTrue(p.getPreferredSize().height<expanded);
            click(p,"Jak grać?");assertTrue(p.isExpanded());assertNotNull(find(p,JScrollPane.class));
            p.showStep(new TutorialGuideSnapshot("water_buy","Kup jedną wodę","Wybierz ofertę i kup 1 wodę za 5 monet.","int_semos_guard_house",18,6,"Miter","greet",0,true));
            JEditorPane text=find(p,JEditorPane.class);
            try{String value=text.getDocument().getText(0,text.getDocument().getLength());assertTrue(value.contains("5 monet"));assertFalse(value.contains("/k Gdzie"));assertFalse(value.contains("Czy mogę ci"));}catch(Exception e){throw new AssertionError(e);}
            click(p,"Jak grać?");click(p,"Wróć do celu");assertEquals(expanded,p.getPreferredSize().height);
        });
    }
    static void click(Container c,String name){for(Component x:c.getComponents()){if(x instanceof JButton&&name.equals(((JButton)x).getText())){((JButton)x).doClick();return;}if(x instanceof Container)click((Container)x,name);}}
    static <T> T find(Container c,Class<T> type){for(Component x:c.getComponents()){if(type.isInstance(x))return type.cast(x);if(x instanceof Container){T result=find((Container)x,type);if(result!=null)return result;}}return null;}
    /** Actual Swing components under the game's skin, rendered off-screen for visual review. */
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(()->{try{
            UIManager.setLookAndFeel(new StyledLookAndFeel(WoodStyle.getInstance()));
            TutorialGuidePanel p=new TutorialGuidePanel(()->{},()->{});p.showStep(sample());
            File out=new File(args[0]);out.mkdirs();
            for(int i=0;i<2;i++){
                p.setHelpOpen(i==1);p.setSize(p.getPreferredSize());layout(p);layout(p);
                BufferedImage image=new BufferedImage(p.getWidth(),p.getHeight(),BufferedImage.TYPE_INT_RGB);
                Graphics2D g=image.createGraphics();p.printAll(g);g.dispose();ImageIO.write(image,"png",new File(out,i==0?"guide.png":"guide-help.png"));
            }
            BufferedImage marker=new BufferedImage(440,245,BufferedImage.TYPE_INT_RGB);Graphics2D g=marker.createGraphics();
            g.setColor(new Color(75,67,50));g.fillRect(0,0,440,245);
            TutorialGuideMarker.paint(g,sample(),sample().zone,180,180,440,245);g.dispose();ImageIO.write(marker,"png",new File(out,"marker.png"));
        }catch(Exception e){throw new RuntimeException(e);}});
        System.exit(0);
    }
}
