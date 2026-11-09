/* SPDX-License-Identifier: GPL-2.0-or-later */
package games.stendhal.client.events;

import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.dialogue.NpcDialogueController;
import games.stendhal.common.NpcDialogueSnapshot;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;

/** Receives an NPC snapshot attached privately to the attending player. */
public final class NpcDialogueEvent extends Event<RPEntity> {
    private static final Logger LOGGER = Logger.getLogger(NpcDialogueEvent.class);
    @Override public void execute() {
        try {
            final NpcDialogueSnapshot snapshot = NpcDialogueSnapshot.fromWire(event.getList("data"));
            SwingUtilities.invokeLater(() -> NpcDialogueController.get().receive(snapshot));
        } catch (RuntimeException e) {
            LOGGER.warn("Invalid NPC dialogue snapshot", e);
        }
    }
}
