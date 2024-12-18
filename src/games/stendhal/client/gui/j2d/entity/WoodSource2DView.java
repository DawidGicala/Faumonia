/* $Id: WoodSource2DView.java,v 1.15 2011/04/28 18:34:25 edi18028 Exp $ */
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
 * The 2D view of a wood source.
 */
class WoodSource2DView extends UseableEntity2DView {

	public WoodSource2DView() {
		super(ActionType.WOOD);
	}

}
