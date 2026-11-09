package games.stendhal.client.gui.dialogue;

import games.stendhal.common.NpcDialogueSnapshot;
import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.WoodStyle;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Renders actual wire snapshots recorded by the seven-quest engine tests. */
public final class AdditionalQuestDialogueRender {
    public static void main(String[] args) throws Exception {
        Path source=Paths.get(args[0]); Path target=Paths.get(args[1]); Files.createDirectories(target);
        List<Path> files=new ArrayList<Path>();
        try(java.util.stream.Stream<Path> stream=Files.list(source)) { stream.filter(p->p.toString().endsWith(".wire")).sorted().forEach(files::add); }
        if(files.size()!=14) throw new AssertionError("Expected 14 real scenes, got "+files.size());
        SwingUtilities.invokeAndWait(()->{
            try { StyledLookAndFeel look=new StyledLookAndFeel(WoodStyle.getInstance()); UIManager.setLookAndFeel(look); look.setDefaultFontSize(12);
                UIManager.getLookAndFeelDefaults().put("ClassLoader",AdditionalQuestDialogueRender.class.getClassLoader());
            } catch(Exception e) { throw new IllegalStateException(e); }
        });
        for(Path file:files) {
            List<String> values=new ArrayList<String>();
            try(DataInputStream in=new DataInputStream(Files.newInputStream(file))) { int count=in.readInt(); for(int i=0;i<count;i++) values.add(in.readUTF()); }
            NpcDialogueSnapshot snapshot=NpcDialogueSnapshot.fromWire(values);
            if(snapshot==null) throw new AssertionError(file);
            SwingUtilities.invokeAndWait(()->{
                NpcDialoguePanel panel=new NpcDialoguePanel(new NpcDialoguePanel.Listener(){
                    public void replyChosen(String id){} public void textSubmitted(String text){} public void hideRequested(){}
                });
                new NpcDialoguePresenter(panel).display(snapshot);
                panel.setSize(690,640); layout(panel);
                int height=Math.max(400,Math.min(720,panel.getPreferredSize().height));
                panel.setSize(690,height); layout(panel); layout(panel);
                BufferedImage image=new BufferedImage(690,height,BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics=image.createGraphics(); panel.printAll(graphics); graphics.dispose();
                try { ImageIO.write(image,"png",target.resolve(file.getFileName().toString().replace(".wire",".png")).toFile()); }
                catch(IOException e) { throw new IllegalStateException(e); }
            });
        }
        System.out.println("14 actual seven-quest scenes rendered");
    }
    private static void layout(Container c) { c.doLayout(); for(Component child:c.getComponents()) if(child instanceof Container) layout((Container)child); }
}
