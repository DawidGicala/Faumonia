/* $Id: ProgressStatusEvent.java,v 1.5 2011/08/16 19:10:47 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.events;

import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.progress.ProgressLogController;
import games.stendhal.common.QuestProgress;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * adjust the quest progress view
 *
 * @author hendrik
 */
public class ProgressStatusEvent extends Event<RPEntity> {
	private static Logger logger = Logger.getLogger(ProgressStatusEvent.class);

	/**
	 * executes the event
	 */
	@Override
	public void execute() {
		try {
			// Marauroa decodes an empty list as a list with one empty string.
			List<String> data = "[]".equals(event.get("data"))
					? Collections.<String>emptyList() : event.getList("data");
			if (!event.has("progress_type")) {
				ProgressLogController.get().showCategories(data);
			} else if (!event.has("item")) {
				ProgressLogController.get().showCategorySummary(event.get("progress_type"), data);
			} else {
				QuestProgress progress = event.has("quest_progress")
						? QuestProgress.fromWire(event.getList("quest_progress")) : null;
				ProgressLogController.get().showDescription(event.get("progress_type"), event.get("item"),
						event.get("description"), event.has("information") ? event.get("information") : "",
						data, progress, event.has("passive"));
			}
		} catch (RuntimeException e) {
			logger.error("Failed to process progress status. Event: " + event, e);
		}
	}

}
