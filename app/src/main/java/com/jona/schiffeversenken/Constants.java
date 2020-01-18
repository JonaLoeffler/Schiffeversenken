package com.jona.schiffeversenken;

/**
 * Globale Konstanten
 */

public interface Constants {

	/**
	 * Intent Request codes
	 */
	public static final int REQUEST_CONNECT_DEVICE_SECURE = 0;
	public static final int REQUEST_ENABLE_BT = 1;

	/**
	 * Message types for data exchange between clients
	 * 
	 * HANDLER
	 */
	// what --> message type
	public static final int MESSAGE_CONNECTION_STATE_CHANGE = 2;
	public static final int MESSAGE_READ = 3;
	public static final int MESSAGE_WRITE = 4;
	public static final int MESSAGE_DEVICE_NAME = 5;
	public static final int MESSAGE_FAILURE = 6;
	public static final int MESSAGE_TOAST = 7;
	// arg1 --> connection states
	public static final int STATE_NONE = 8;
	public static final int STATE_LISTEN = 9;
	public static final int STATE_CONNECTING = 10;
	public static final int STATE_CONNECTED = 11;

	/**
	 * Message types for data exchange between clients
	 * 
	 * MESSAGE DECODER
	 */
	// what
	public static final int MESSAGE_SET_INITIAL_TURN = 12;
	public static final int MESSAGE_INTENT_STARTINGGAME = 13;
	public static final int MESSAGE_STARTINGGAME_NOW = 14;
	public static final int MESSAGE_PLAYER_TURN_CHANGE = 15;
	public static final int MESSAGE_GAME_STATE_CHANGE = 16;
	public static final int MESSAGE_GAME_EVENT = 17;
	public static final int REQUEST_GAME_STATE = 31;
	public static final int ANSWER_GAME_STATE = 32;
	// arg1 --> game states, different states of the game
	public static final int GAMESTATE_PLAYER_PLACING_SHIPS = 18;
	public static final int GAMESTATE_PLAYER_DONE_PLACING_SHIPS = 20;
	public static final int GAMESTATE_PLAYER_SELECTING_TILE = 22;
	public static final int GAMESTATE_PLAYER_SELECTED_TILE = 24;
	// arg1 --> game events, fire on X, hit on Y, ...
	public static final int EVENT_ENEMY_FIRED = 26;
	public static final int EVENT_HIT = 27;
	public static final int EVENT_MISS = 28;
	public static final int EVENT_SHIP_SUNK = 29;
	public static final int EVENT_ENEMY_WON = 30;
	public static final int EVENT_PLAYER_WON = 31;

	// arg2 --> values

	// other
	public static final String EXTRA_DEVICE_ADDRESS = "device_address";
	public static final String DEVICE_NAME = "device_name";
	public static final String FAILURE = "failure";
	public static final String TOAST = "toast";
}
