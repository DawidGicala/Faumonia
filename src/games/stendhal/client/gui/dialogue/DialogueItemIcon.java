package games.stendhal.client.gui.dialogue;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Icon;

/** Uses the same bundled item sprites as inventory slots, with no external image lookup. */
final class DialogueItemIcon implements Icon {
    private static final Map<String, BufferedImage> CACHE = new HashMap<String, BufferedImage>();
    private final BufferedImage image;
    private final int size;
    static URL resource(String sprite) {
        if (sprite != null && (sprite.matches("gui/(chat|forbidden|buddy_online)")
                || sprite.matches("gui/dialogue/(greeting|farewell|confirm|decline|conversation|back|quest|hint|handover|complete|xp|attack_xp|defense_xp|karma|health|unlock)"))) {
            return DialogueItemIcon.class.getClassLoader().getResource("data/" + sprite + ".png");
        }
        if ("sheep".equals(sprite) || "baby_dragon".equals(sprite)) {
            return DialogueItemIcon.class.getClassLoader().getResource("data/sprites/"+sprite+".png");
        }
        if (sprite == null || !(sprite.matches("(items|monsters|npc)/[A-Za-z0-9_/-]+")
                || sprite.matches("useable/expedition/(lamp|tablet|anchors)")) || sprite.contains("..")) { return null; }
        return DialogueItemIcon.class.getClassLoader().getResource("data/sprites/" + sprite + ".png");
    }
    DialogueItemIcon(String sprite) {
        this(sprite, 32);
    }
    DialogueItemIcon(String sprite, int size) {
        this.size = size;
        if (!CACHE.containsKey(sprite)) {
            BufferedImage loaded = null;
            try { URL url = resource(sprite); if (url != null) { loaded = ImageIO.read(url); } }
            catch (IOException e) { /* Names and quantities remain usable without a sprite. */ }
            if (loaded != null && sprite.startsWith("gui/dialogue/")) {
                // The authoring PNG can be large; cache only the pixel-sized UI sprite.
                BufferedImage small = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g = small.createGraphics();
                g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(loaded, 0, 0, 32, 32, null); g.dispose(); loaded = small;
            }
            CACHE.put(sprite, loaded);
        }
        BufferedImage sheet = CACHE.get(sprite);
        if (sheet != null && (sprite.startsWith("monsters/") || sprite.startsWith("npc/") || "sheep".equals(sprite) || "baby_dragon".equals(sprite))) {
            int width = sheet.getWidth() / 3, height = sheet.getHeight() / 4;
            image = sprite.startsWith("npc/alternative/") ? sheet : sheet.getSubimage(width, 2 * height, width, height);
        } else if (sheet != null && (sprite.startsWith("items/") || sprite.startsWith("useable/expedition/"))) {
            image = sheet.getSubimage(0, 0, Math.min(32, sheet.getWidth()), Math.min(32, sheet.getHeight()));
        } else { image = sheet; }
    }
    public int getIconWidth() { return size; }
    public int getIconHeight() { return size; }
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        if (image != null) {
            int width = image.getWidth(), height = image.getHeight();
            double scale = Math.min((double) size / width, (double) size / height);
            int dw = Math.max(1, (int) (width * scale)), dh = Math.max(1, (int) (height * scale));
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(image, x + (size - dw) / 2, y + (size - dh) / 2, dw, dh, null);
            g.dispose();
        }
    }
}
