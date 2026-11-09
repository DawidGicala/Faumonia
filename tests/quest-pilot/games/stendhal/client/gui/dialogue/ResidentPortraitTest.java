package games.stendhal.client.gui.dialogue;
import static org.junit.Assert.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;
import org.junit.Test;
public class ResidentPortraitTest {
    @Test public void staticTomiSpriteIsNotCroppedIntoAnimationFrames() throws Exception {
        String sprite="npc/alternative/tomi";
        BufferedImage original=ImageIO.read(DialogueItemIcon.resource(sprite));
        Field field=DialogueItemIcon.class.getDeclaredField("image");field.setAccessible(true);
        BufferedImage actual=(BufferedImage)field.get(new DialogueItemIcon(sprite));
        assertEquals(original.getWidth(),actual.getWidth());assertEquals(original.getHeight(),actual.getHeight());
        for(int y=0;y<original.getHeight();y++)for(int x=0;x<original.getWidth();x++)assertEquals(original.getRGB(x,y),actual.getRGB(x,y));
    }
}
