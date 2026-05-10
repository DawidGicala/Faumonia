/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import marauroa.client.net.INetworkClientManagerInterface;
import marauroa.client.net.KeepAliveSender;
import marauroa.client.net.TCPNetworkClientManager;
import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.crypto.RSAPublicKey;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.Result;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SAction;
import marauroa.common.net.message.MessageC2SChooseCharacter;
import marauroa.common.net.message.MessageC2SCreateAccount;
import marauroa.common.net.message.MessageC2SCreateCharacter;
import marauroa.common.net.message.MessageC2SKeepAlive;
import marauroa.common.net.message.MessageC2SLoginRequestKey;
import marauroa.common.net.message.MessageC2SLoginSendNonceNameAndPassword;
import marauroa.common.net.message.MessageC2SLoginSendNonceNamePasswordAndSeed;
import marauroa.common.net.message.MessageC2SLoginSendPromise;
import marauroa.common.net.message.MessageC2SLogout;
import marauroa.common.net.message.MessageC2SOutOfSync;
import marauroa.common.net.message.MessageC2STransferACK;
import marauroa.common.net.message.MessageS2CCharacterList;
import marauroa.common.net.message.MessageS2CConnectNACK;
import marauroa.common.net.message.MessageS2CCreateAccountACK;
import marauroa.common.net.message.MessageS2CCreateAccountNACK;
import marauroa.common.net.message.MessageS2CCreateCharacterACK;
import marauroa.common.net.message.MessageS2CCreateCharacterNACK;
import marauroa.common.net.message.MessageS2CInvalidMessage;
import marauroa.common.net.message.MessageS2CLoginACK;
import marauroa.common.net.message.MessageS2CLoginMessageNACK;
import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.common.net.message.MessageS2CLoginSendKey;
import marauroa.common.net.message.MessageS2CLoginSendNonce;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.MessageS2CServerInfo;
import marauroa.common.net.message.MessageS2CTransfer;
import marauroa.common.net.message.MessageS2CTransferREQ;
import marauroa.common.net.message.TransferContent;

/**
 * It is a wrapper over all the things that the client should do. You should
 * extend this class at your game.
 *
 * @author miguel
 *
 */
public abstract class ClientFramework {

	private static final marauroa.common.Logger logger = Log4J.getLogger(ClientFramework.class);

	public final static int TIMEOUT = 10000;

	private final static int TIMEOUT_EXTENDED = 300000;

	private Timer keepAliveTimer = null;

	protected INetworkClientManagerInterface netMan;

	private final List<Message> messages;

	public ClientFramework(String loggingProperties) {
		Log4J.init(loggingProperties);
		messages = new LinkedList<Message>();
	}

	public ClientFramework() {
		Log4J.init();
		messages = new LinkedList<Message>();
	}

	public void connect(String host, int port) throws IOException {
		InetSocketAddress address = new InetSocketAddress(host, port);
		IOException originalException = null;
		boolean connected = false;

		try {
			Proxy proxy = discoverProxy(host, port);
			if (proxy != Proxy.NO_PROXY) {
				connect(proxy, address);
				connected = true;
			}
		} catch (IOException e) {
			originalException = e;
		} catch (IllegalArgumentException e) {
			originalException = new IOException(e);
		}

		if (!connected) {
			try {
				connect(Proxy.NO_PROXY, address);
			} catch (IOException e) {
				if (originalException != null) {
					throw originalException;
				}
				throw e;
			}
		}
	}

	private Proxy discoverProxy(String host, int port) throws IOException {
		ProxySelector selector = ProxySelector.getDefault();
		if (selector == null) {
			return Proxy.NO_PROXY;
		}

		String quotedHost = host;
		URI uri = null;
		if (!host.startsWith("[") && (host.indexOf(":") >= 0)) {
			quotedHost = "[" + host + "]";
		}
		try {
			uri = new URI("socket://" + quotedHost + ":" + port);
		} catch (URISyntaxException e) {
			IOException ioE = new IOException("Error, while discovering proxyserver: ");
			ioE.initCause(e);
			throw ioE;
		}

		List<Proxy> proxies = selector.select(uri);
		if (proxies == null) {
			return Proxy.NO_PROXY;
		}
		for (Proxy proxy : proxies) {
			if (proxy.type() == Proxy.Type.SOCKS) {
				return proxy;
			}
		}
		return Proxy.NO_PROXY;
	}

	public void connect(Proxy proxy, InetSocketAddress serverAddress) throws IOException {
		netMan = new TCPNetworkClientManager(proxy, serverAddress);
	}

	private Message getMessage(int timeout) throws InvalidVersionException, TimeoutException,
	        BannedAddressException {
		Message msg = null;

		if (messages.isEmpty()) {
			msg = netMan.getMessage(timeout);

			if (msg instanceof MessageS2CConnectNACK) {
				throw new BannedAddressException();
			}

			if (msg == null) {
				throw new TimeoutException();
			}
		} else {
			msg = messages.remove(0);
		}

		logger.debug("CF getMessage: " + msg);
		return msg;
	}

	@Deprecated
	public synchronized void resync() {
		MessageC2SOutOfSync msg = new MessageC2SOutOfSync();
		netMan.addMessage(msg);
	}

	public synchronized void login(String username, String password)
	        throws InvalidVersionException, TimeoutException, LoginFailedException,
	        BannedAddressException {
		login(username, password, null);
	}

	@SuppressWarnings("null")
	public synchronized void login(String username, String password, String seed)
	        throws InvalidVersionException, TimeoutException, LoginFailedException,
	        BannedAddressException {
		int received = 0;
		RSAPublicKey key = null;
		byte[] clientNonce = null;
		byte[] serverNonce = null;

		netMan.addMessage(new MessageC2SLoginRequestKey(null, getGameName(), getVersionNumber()));

		int timeout = TIMEOUT;
		while (received < 3) {
			Message msg = getMessage(timeout);
			timeout = TIMEOUT_EXTENDED;

			switch (msg.getType()) {
				case S2C_INVALIDMESSAGE: {
					throw new LoginFailedException(((MessageS2CInvalidMessage) msg).getReason());
				}
				case S2C_LOGIN_SENDKEY: {
					logger.debug("Received Key");
					key = ((MessageS2CLoginSendKey) msg).getKey();

					clientNonce = Hash.random(Hash.hashLength());
					netMan.addMessage(new MessageC2SLoginSendPromise(null, Hash.hash(clientNonce)));
					break;
				}
				case S2C_LOGIN_SENDNONCE: {
					logger.debug("Received Server Nonce");
					if (serverNonce != null) {
						throw new LoginFailedException("Already received a serverNonce");
					}

					serverNonce = ((MessageS2CLoginSendNonce) msg).getHash();
					byte[] b1 = Hash.xor(clientNonce, serverNonce);
					if (b1 == null) {
						throw new LoginFailedException("Incorrect hash b1");
					}

					byte[] b2 = Hash.xor(b1, Hash.hash(password));
					if (b2 == null) {
						throw new LoginFailedException("Incorrect hash b2");
					}

					byte[] cryptedPassword = key.encodeByteArray(b2);
					if (seed != null) {
						byte[] bs = null;
						try {
							bs = seed.getBytes("UTF-8");
						} catch (UnsupportedEncodingException e) {
							logger.error(e, e);
						}
						if (bs.length != 16) {
							throw new LoginFailedException("Seed has not the correct length.");
						}
						byte[] b3 = null;
						b3 = Hash.xor(b1, bs);
						if (b3 == null) {
							throw new LoginFailedException("Incorrect hash seed");
						}
						byte[] cryptedSeed = key.encodeByteArray(b3);
						netMan.addMessage(new MessageC2SLoginSendNonceNamePasswordAndSeed(null,
					        clientNonce, username, cryptedPassword, cryptedSeed));
					} else {
						netMan.addMessage(new MessageC2SLoginSendNonceNameAndPassword(null,
					        clientNonce, username, cryptedPassword));
					}
					break;
				}
				case S2C_LOGIN_ACK:
					logger.debug("Login correct");
					onPreviousLogins(((MessageS2CLoginACK) msg).getPreviousLogins());
					received++;
					break;

				case S2C_CHARACTERLIST:
					logger.debug("Received Character list");
					String[] characters = ((MessageS2CCharacterList) msg).getCharacters();
					onAvailableCharacters(characters);
					Map<String, RPObject> characterDetails = ((MessageS2CCharacterList) msg).getCharacterDetails();
					onAvailableCharacterDetails(characterDetails);
					received++;
					break;

				case S2C_SERVERINFO:
					logger.debug("Received Server info");
					String[] info = ((MessageS2CServerInfo) msg).getContents();
					onServerInfo(info);
					received++;
					break;

				case S2C_LOGIN_NACK:
					MessageS2CLoginNACK msgNACK = (MessageS2CLoginNACK) msg;
					logger.debug("Login failed. Reason: " + msgNACK.getResolution());
					throw new LoginFailedException(msgNACK.getResolution(), msgNACK.getResolutionCode());

				case S2C_LOGIN_MESSAGE_NACK:
					MessageS2CLoginMessageNACK msgMessageNACK = (MessageS2CLoginMessageNACK) msg;
					logger.debug("Login failed. Reason: " + msgMessageNACK.getReason());
					throw new LoginFailedException(msgMessageNACK.getReason());

				default:
					messages.add(msg);
			}
		}
	}

	public synchronized boolean chooseCharacter(String character) throws TimeoutException,
	        InvalidVersionException, BannedAddressException {
		Message msgCC = new MessageC2SChooseCharacter(null, character);
		netMan.addMessage(msgCC);

		int received = 0;

		while (received != 1) {
			Message msg = getMessage(TIMEOUT_EXTENDED);

			switch (msg.getType()) {
				case S2C_CHOOSECHARACTER_ACK:
					logger.debug("Choose Character ACK");
					keepAliveTimer = new Timer("KeepAlive", true);
					keepAliveTimer.schedule(new KeepAliveSender(netMan), 1000, 10000);
					return true;

				case S2C_CHOOSECHARACTER_NACK:
					logger.debug("Choose Character NACK");
					return false;

				default:
					messages.add(msg);
			}
		}

		return false;
	}

	public synchronized AccountResult createAccount(String username, String password, String email)
	        throws TimeoutException, InvalidVersionException, BannedAddressException {
		return createAccount(username, password, email, false);
	}

	public synchronized AccountResult createAccount(String username, String password, String email, boolean marketingConsent)
	        throws TimeoutException, InvalidVersionException, BannedAddressException {
		Message msgCA = new MessageC2SCreateAccount(null, username, password, email, marketingConsent);

		netMan.addMessage(msgCA);

		int received = 0;

		AccountResult result = null;

		while (received != 1) {
			Message msg = getMessage(TIMEOUT_EXTENDED);

			switch (msg.getType()) {
				case S2C_INVALIDMESSAGE: {
					result = new AccountResult(Result.FAILED_EXCEPTION, username);
					break;
				}
				case S2C_CREATEACCOUNT_ACK:
					logger.debug("Create account ACK");

					MessageS2CCreateAccountACK msgack = (MessageS2CCreateAccountACK) msg;
					result = new AccountResult(Result.OK_CREATED, msgack.getUsername());

					received++;
					break;

				case S2C_CREATEACCOUNT_NACK:
					logger.debug("Create account NACK");
					MessageS2CCreateAccountNACK nack = (MessageS2CCreateAccountNACK) msg;
					result = new AccountResult(nack.getResolutionCode(), username);

					received++;
					break;

				default:
					logger.debug("Unexpected method while waiting for confirmation of account creation: " + msg);
			}
		}

		return result;
	}

	public synchronized CharacterResult createCharacter(String character, RPObject template)
	        throws TimeoutException, InvalidVersionException, BannedAddressException {
		Message msgCA = new MessageC2SCreateCharacter(null, character, template);

		netMan.addMessage(msgCA);

		int received = 0;

		CharacterResult result = null;

		while (received != 2) {
			Message msg = getMessage(TIMEOUT_EXTENDED);

			switch (msg.getType()) {
				case S2C_CREATECHARACTER_ACK:
					logger.debug("Create character ACK");

					MessageS2CCreateCharacterACK msgack = (MessageS2CCreateCharacterACK) msg;

					result = new CharacterResult(Result.OK_CREATED, msgack.getCharacter(), msgack
					        .getTemplate());
					received++;
					break;

				case S2C_CHARACTERLIST:
					logger.debug("Received Character list");
					String[] characters = ((MessageS2CCharacterList) msg).getCharacters();
					onAvailableCharacters(characters);
					Map<String, RPObject> characterDetails = ((MessageS2CCharacterList) msg).getCharacterDetails();
					onAvailableCharacterDetails(characterDetails);

					received++;
					break;

				case S2C_CREATECHARACTER_NACK:
					logger.debug("Create character NACK");
					MessageS2CCreateCharacterNACK reply = (MessageS2CCreateCharacterNACK) msg;

					result = new CharacterResult(reply.getResolutionCode(), character, template);
					return result;
			}
		}

		return result;
	}

	public void send(RPAction action) {
		MessageC2SAction msgAction = new MessageC2SAction(null, action);
		netMan.addMessage(msgAction);
	}

	public synchronized boolean logout() throws InvalidVersionException, TimeoutException,
	        BannedAddressException {
		Message msgL = new MessageC2SLogout(null);

		netMan.addMessage(msgL);
		int received = 0;

		while (received != 1) {
			Message msg = getMessage(TIMEOUT);
			switch (msg.getType()) {
				case S2C_LOGOUT_ACK:
					logger.debug("Logout ACK");
					if (keepAliveTimer != null) {
						keepAliveTimer.cancel();
					}
					return true;

				case S2C_LOGOUT_NACK:
					logger.debug("Logout NACK");
					return false;
			}
		}

		return false;
	}

	public void close() {
		if (keepAliveTimer != null) {
			keepAliveTimer.cancel();
		}

		if (netMan != null) {
			netMan.finish();
		}
	}

	public synchronized boolean loop(@SuppressWarnings("unused") int delta) {
		boolean receivedMessages = false;

		messages.addAll(((TCPNetworkClientManager) netMan).getMessages());

		for (Message msg : messages) {
			receivedMessages = true;

			switch (msg.getType()) {
				case S2C_PERCEPTION: {
					logger.debug("Processing Message Perception");
					MessageS2CPerception msgPer = (MessageS2CPerception) msg;
					onPerception(msgPer);

					break;
				}

				case S2C_TRANSFER_REQ: {
					logger.debug("Processing Content Transfer Request");
					List<TransferContent> items = ((MessageS2CTransferREQ) msg).getContents();

					items = onTransferREQ(items);

					MessageC2STransferACK reply = new MessageC2STransferACK(null, items);
					netMan.addMessage(reply);

					break;
				}

				case S2C_TRANSFER: {
					logger.debug("Processing Content Transfer");
					List<TransferContent> items = ((MessageS2CTransfer) msg).getContents();
					onTransfer(items);

					break;
				}
			}
		}

		messages.clear();

		return receivedMessages;
	}

	public void sendKeepAlive() {
		MessageC2SKeepAlive msg = new MessageC2SKeepAlive();
		netMan.addMessage(msg);
	}

	public boolean getConnectionState() {
		return netMan.getConnectionState();
	}

	abstract protected void onPerception(MessageS2CPerception message);

	abstract protected List<TransferContent> onTransferREQ(List<TransferContent> items);

	abstract protected void onTransfer(List<TransferContent> items);

	abstract protected void onAvailableCharacters(String[] characters);

	protected void onAvailableCharacterDetails(@SuppressWarnings("unused") Map<String, RPObject> characters) {
		// stub
	}

	abstract protected void onServerInfo(String[] info);

	abstract protected String getGameName();

	abstract protected String getVersionNumber();

	abstract protected void onPreviousLogins(List<String> previousLogins);
}