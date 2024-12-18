/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import games.stendhal.client.GameScreen;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.j2d.entity.EntityView;
import games.stendhal.common.Direction;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;

/**
 * Main window keyboard handling.
 */
class GameKeyHandler implements KeyListener {
	private final StendhalClient client;
	private final GameScreen screen;

	/**
	 * Delayed direction release holder.
	 */
	private DelayedDirectionRelease directionRelease;

	/**
	 * Create a new GameKeyHandler.
	 * 
	 * @param client client to send direction commands
	 * @param screen screen where to direct game screen related commands
	 */
	GameKeyHandler(StendhalClient client, GameScreen screen) {
		this.client = client;
		this.screen = screen;
	}

	private JButton buttonChat;
	
	public void setButtonChat(JButton buttonChat) {
	    this.buttonChat = buttonChat;
	}
	 /**
     * Zmienna przechowująca stan, czy użytkownik może się poruszać (false = pisanie, true = poruszanie).
     */
    private boolean canMove = false;
	
    
    
    /**
     * Funkcja przełączająca tryb poruszania/pisania po naciśnięciu przycisku albo tyldy (~).
     */
    public void toggleMovementMode() {
        System.out.println("toggleMovementMode wywołane, canMove: " + !canMove);
        canMove = !canMove;

        if (canMove) {
            System.out.println("Możesz się poruszać!");
            if (buttonChat != null) {
                System.out.println("Ustawiam tekst przycisku na: Chat Off");
                buttonChat.setText("WSAD ON");
            } else {
                System.out.println("Przycisk jest null");
            }
        } else {
            System.out.println("Jesteś w trybie pisania!");
            if (buttonChat != null) {
                System.out.println("Ustawiam tekst przycisku na: Chat On");
                buttonChat.setText("WSAD OFF");
            } else {
                System.out.println("Przycisk jest null");
            }
        }
    }
	
	public void keyPressed(final KeyEvent e) {
        // Jeżeli wciśnięta tylda, przełącz tryb
        if (e.getKeyCode() == KeyEvent.VK_BACK_QUOTE) { // Tylda to BACKQUOTE
            toggleMovementMode(); // Przełącz tryb
            return; // Jeśli tylda to nie przechodzimy do dalszej logiki
        }

        // Jeżeli Shift jest wciśnięty, ignorujemy ruchy
		if (e.isShiftDown()) {
			/*
			 * We are going to use shift to move to previous/next line of text
			 * with arrows so we just ignore the keys if shift is pressed.
			 */
			return;
		}

	    // Ignorujemy WSAD, gdy canMove jest false
	    switch (e.getKeyCode()) {
	        case KeyEvent.VK_W:
	        case KeyEvent.VK_A:
	        case KeyEvent.VK_S:
	        case KeyEvent.VK_D:
	            if (!canMove) {
	                return; // Ignorujemy klawisze WSAD
	            }
	            break;
	    }

	    // Przetwarzanie klawiszy
	    switch (e.getKeyCode()) {
	        case KeyEvent.VK_R:
	            if (e.isControlDown()) {
	                /*
	                 * Ctrl+R Remove text bubbles
	                 */
	                screen.clearTexts();
	            }
	            break;

	        case KeyEvent.VK_LEFT:
	        case KeyEvent.VK_RIGHT:
	        case KeyEvent.VK_UP:
	        case KeyEvent.VK_DOWN:
	        case KeyEvent.VK_W:
	        case KeyEvent.VK_A:
	        case KeyEvent.VK_S:
	        case KeyEvent.VK_D:
        	
			/*
			 * Ctrl means face, otherwise move
			 */
			final Direction direction = keyCodeToDirection(e.getKeyCode());

			if (e.isAltGraphDown()) {
				final User user = User.get();

				final EntityView<?> view = screen.getEntityViewAt(user.getX()
						+ direction.getdx(), user.getY() + direction.getdy());

				if (view != null) {
					final IEntity entity = view.getEntity();
					if (!entity.equals(user)) {
						view.onAction();
					}
				}
			}

			processDirectionPress(direction, e.isControlDown());
			break;
		case KeyEvent.VK_0:
		case KeyEvent.VK_1:
		case KeyEvent.VK_2:
		case KeyEvent.VK_3:
		case KeyEvent.VK_4:
		case KeyEvent.VK_5:
		case KeyEvent.VK_6:
		case KeyEvent.VK_7:
		case KeyEvent.VK_8:
		case KeyEvent.VK_9:
			switchToSpellCastingState(e);
			break;
		}
	}

	public void keyReleased(final KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_UP:
		case KeyEvent.VK_DOWN:
        case KeyEvent.VK_W:
        case KeyEvent.VK_A:
        case KeyEvent.VK_S:
        case KeyEvent.VK_D:
			/*
			 * Ctrl means face, otherwise move
			 */
			processDirectionRelease(keyCodeToDirection(e.getKeyCode()),
					e.isControlDown());
		}
	}

	public void keyTyped(final KeyEvent e) {
		// Ignore. All the work is done in keyPressed and keyReleased methods.
	    // Sprawdzamy, czy klawisz to W, S, A, D i czy canMove jest true
	    char keyChar = e.getKeyChar(); // Znak wciśniętego klawisza
	    if (canMove && (keyChar == 'w' || keyChar == 'a' || keyChar == 's' || keyChar == 'd')) {
	        e.consume(); // Zatrzymanie dalszego przetwarzania zdarzenia
	    }
	}

	/**
	 * Process delayed direction release
	 */
	void processDelayedDirectionRelease() {
		if ((directionRelease != null) && directionRelease.hasExpired()) {
			client.removeDirection(directionRelease.getDirection(),
					directionRelease.isFacing());

			directionRelease = null;
		}
	}

	/**
	 * Convert a keycode to the corresponding direction.
	 * 
	 * @param keyCode The keycode.
	 * 
	 * @return The direction, or <code>null</code>.
	 */
	private Direction keyCodeToDirection(final int keyCode) {
		switch (keyCode) {
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_A:
            return Direction.LEFT;
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_D:
            return Direction.RIGHT;
        case KeyEvent.VK_UP:
        case KeyEvent.VK_W:
            return Direction.UP;
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_S:
            return Direction.DOWN;

		default:
			return null;
		}
	}

	/**
	 * Handle direction press actions.
	 * 
	 * @param direction The direction.
	 * @param facing If facing only.
	 */
	private void processDirectionPress(final Direction direction,
			final boolean facing) {
		if (directionRelease != null) {
			if (directionRelease.check(direction, facing)) {
				directionRelease = null;
				return;
			} else {
				/*
				 * Flush pending release
				 */
				client.removeDirection(directionRelease.getDirection(),
						directionRelease.isFacing());

				directionRelease = null;
			}
		}

		client.addDirection(direction, facing);
	}

	/**
	 * Handle direction release actions.
	 * 
	 * @param direction The direction.
	 * @param facing If facing only.
	 */
	private void processDirectionRelease(final Direction direction,
			final boolean facing) {
		if (directionRelease != null) {
			if (directionRelease.check(direction, facing)) {
				/*
				 * Ignore repeats
				 */
				return;
			} else {
				/*
				 * Flush previous release
				 */
				client.removeDirection(directionRelease.getDirection(),
						directionRelease.isFacing());
			}
		}

		directionRelease = new DelayedDirectionRelease(direction, facing);
	}

	/**
	 * Switch the screen to spell casting state.
	 * 
	 * @param e
	 */
	private void switchToSpellCastingState(KeyEvent e) {
		screen.switchToSpellCasting(e);
	}

	private static class DelayedDirectionRelease {
		/**
		 * The maximum delay between auto-repeat release-press.
		 */
		protected static final long DELAY = 50L;

		protected long expiration;

		protected Direction dir;

		protected boolean facing;

		public DelayedDirectionRelease(final Direction dir, final boolean facing) {
			this.dir = dir;
			this.facing = facing;

			expiration = System.currentTimeMillis() + DELAY;
		}

		/**
		 * Check if a new direction matches the existing one, and if so, reset
		 * the expiration point.
		 * 
		 * @param dir The direction.
		 * @param facing The facing flag.
		 * 
		 * @return <code>true</code> if this is a repeat.
		 */
		public boolean check(final Direction dir, final boolean facing) {
			if (!this.dir.equals(dir)) {
				return false;
			}

			if (this.facing != facing) {
				return false;
			}

			final long now = System.currentTimeMillis();

			if (now >= expiration) {
				return false;
			}

			expiration = now + DELAY;

			return true;
		}

		/**
		 * Get the direction.
		 * 
		 * @return The direction.
		 */
		public Direction getDirection() {
			return dir;
		}

		/**
		 * Determine if the delay point has been reached.
		 * 
		 * @return <code>true</code> if the delay time has been reached.
		 */
		public boolean hasExpired() {
			return System.currentTimeMillis() >= expiration;
		}

		/**
		 * Determine if the facing only option was used.
		 * 
		 * @return <code>true</code> if facing only.
		 */
		public boolean isFacing() {
			return facing;
		}
	}
}