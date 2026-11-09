package games.stendhal.client.gui;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChatTopicStyleTest {
    @Test public void blueTopicsRemainClickableWithoutUnderlining() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            KTextEdit chat=new KTextEdit();Style link=chat.textPane.getStyle("link");
            assertFalse(StyleConstants.isUnderline(link));assertNotNull(link.getAttribute("linkact"));
            assertEquals(new java.awt.Color(90,170,255),StyleConstants.getForeground(link));
        });
    }
}
