/* $Id: SourceSulfur2DView.java,v 1.12 2010/10/14 23:16:21 nhnb Exp $ */
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
package games.stendhal.client.gui.j2d.entity;

import games.stendhal.client.entity.ActionType;

/**
 * The 2D view of a sulfur source.
 */
class SourceSulfur2DView extends UseableEntity2DView {

	public SourceSulfur2DView() {
		super(ActionType.STONE);
	}

}
