/* $Id: MovementTargetIsOnBoardValidator.java,v 1.3 2010/09/19 02:24:37 nhnb Exp $ */
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
package games.stendhal.server.entity.mapstuff.game.movevalidator;

import games.stendhal.server.entity.item.token.BoardToken;
import games.stendhal.server.entity.mapstuff.game.GameBoard;
import games.stendhal.server.entity.player.Player;

/**
 * Is the movement target on board?
 *
 * @author hendrik
 */
public class MovementTargetIsOnBoardValidator implements MoveValidator {

	public boolean validate(GameBoard board, Player player, BoardToken token, int xIndex, int yIndex) {
		if ((xIndex < 0) || (yIndex < 0)) {
			player.sendPrivateText("Połóż krążek na planszy gry.");
			return false;
		}
		return true;
	}

}
