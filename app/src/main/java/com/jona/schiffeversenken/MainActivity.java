package com.jona.schiffeversenken;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jona.common.BluetoothService;
import com.jona.common.DeviceListActivity;
import com.jona.common.SettingsActivity;
import com.jona.common.SquareLayout;
import com.jona.utility.Util;

class DragShadow extends View.DragShadowBuilder {

	// drag shadow beim setzen der schiffe

	public DragShadow(View view) {
		super(view);
	}

	@Override
	public void onDrawShadow(Canvas canvas) {
		super.onDrawShadow(canvas);
	}

	@Override
	public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
		View v = getView();
		int height = (int) v.getHeight();
		int width = (int) v.getWidth();

		shadowSize.set(width, height);
		shadowTouchPoint.set(width / 5, height / 2);
	}
}

public class MainActivity extends FragmentActivity implements Constants {

	/**
	 * one of the game fragments, this one holds the enemy's grid
	 */
	public class GameFragmentOpponent extends Fragment implements OnClickListener {

		private ImageButton btn_TileSelected;
		private View selectedEnemyTile;
		private SquareLayout sqareLayoutOpponent;

		public GameBoard gbOpponent;

		/**
		 * wird ausgefuehrt sobald feld ausgew�hlt und best�tigt wurde
		 */
		private void actionTileSelected(View selectedEnemyTile) {
			if (!gbOpponent.isTileUsed((Integer) selectedEnemyTile.getTag())) {
				sendMessage(MESSAGE_GAME_EVENT, EVENT_ENEMY_FIRED, (Integer) selectedEnemyTile.getTag());
				btn_TileSelected.setVisibility(View.GONE);
				selectedEnemyTile.setBackground(getResources().getDrawable(R.drawable.ic_tile_frame));
				selectedEnemyTile = null;
				setPlayerGameState(GAMESTATE_PLAYER_SELECTED_TILE);
			} else {

			}
		}

		/**
		 * initialisiert das gegnerische feld
		 */
		private void initGridView() {
			gbOpponent = (GameBoard) findViewById(R.id.gameboard_opponent);
			for (int i = 0; i < 100; i++) {
				View img = new View(getActivity());
				img.setLayoutParams(new GameBoard.LayoutParams(2, 2));
				img.setBackground(getResources().getDrawable(R.drawable.ic_tile_frame));
				img.bringToFront();
				img.setTag(i);
				img.setOnClickListener(this);
				gbOpponent.addView(img);
			}
		}

		/**
		 * onClickListener fuer die einzelnen tiles des Feldes, zustaendig fuer die
		 * auswahl eines feldes
		 */
		@Override
		public void onClick(View view) {
			if (Integer.parseInt(view.getTag().toString()) >= 0 && Integer.parseInt(view.getTag().toString()) < 100) {
				if (playerGameState == GAMESTATE_PLAYER_SELECTING_TILE) {
					Log.d(TAG, "Tile selected");
					if (selectedEnemyTile != null) {
						if (selectedEnemyTile == view) {
							view.setBackground(getResources().getDrawable(R.drawable.ic_tile_frame));
							btn_TileSelected.setVisibility(View.GONE);
							selectedEnemyTile = null;
						} else {
							selectedEnemyTile.setBackground(getResources().getDrawable(R.drawable.ic_tile_frame));
							view.setBackground(getResources().getDrawable(R.drawable.ic_tile_frame_selected));
							btn_TileSelected.setVisibility(View.VISIBLE);
							selectedEnemyTile = view;
						}
					} else {
						view.setBackground(getResources().getDrawable(R.drawable.ic_tile_frame_selected));
						btn_TileSelected.setVisibility(View.VISIBLE);
						selectedEnemyTile = view;
					}
					int tag = (Integer) view.getTag();
					Log.d(TAG, "Tile clicked, position: " + tag);
				}
			}
		}

		/**
		 * erstellt die Views
		 */
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_game_opponent, container, false);
			sqareLayoutOpponent = (SquareLayout) rootView.findViewById(R.id.sqarelayout_opponent);
			sqareLayoutOpponent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

				@SuppressWarnings("deprecation")
				@Override
				public void onGlobalLayout() {
					if (Build.VERSION.SDK_INT < 16) {
						sqareLayoutOpponent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					} else {
						sqareLayoutOpponent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
					initGridView();
				}
			});

			btn_TileSelected = (ImageButton) rootView.findViewById(R.id.btn_tile_selected);
			btn_TileSelected.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (selectedEnemyTile != null) {
						Log.d(TAG, "Fired on tile '" + selectedEnemyTile.getTag() + "'");
						actionTileSelected(selectedEnemyTile);
					} else {
						Log.d(TAG, "No tile selected");
					}
				}
			});
			return rootView;
		}
	}

	/**
	 * One of the game fragments, this one holds the player's own grid
	 */
	public class GameFragmentPlayer extends Fragment implements OnClickListener, OnLongClickListener {

		private Ship selectedShip;
		private ImageButton btnTurnLeft, btnTurnRight;
		private Button btnDone;
		private SquareLayout sqareLayoutPlayer;
		private ShipPicker picker;

		private boolean enemyDonePlacingShips;

		public GameBoard gbPlayer;
		public Ship shipAirCraftCarrier, shipBattleShip, shipCruiser1, shipCruiser2, shipDestroyer;

		/**
		 * wird aufgerufen, nachdem alle schiffe gesetzt und best�tigt wurden
		 */
		private void actionDonePlacingShips() {
			Log.d(TAG, "Done placing ships");
			// interface ver�ndern f�r neue spielphase
			btnTurnLeft.setVisibility(View.GONE);
			btnTurnRight.setVisibility(View.GONE);
			btnDone.setVisibility(View.GONE);
			setPlayerGameState(GAMESTATE_PLAYER_DONE_PLACING_SHIPS);

			for (int i = 0; i < gbPlayer.getChildCount(); i++) {
				gbPlayer.getChildAt(i).setOnClickListener(null);
				gbPlayer.getChildAt(i).setOnDragListener(null);
				gbPlayer.getChildAt(i).setOnLongClickListener(null);
			}

			// koordination zwischen clients um spiel zu beginnen
			if (!enemyDonePlacingShips) {
				Log.d(TAG, "Enemy not ready, sending message");
				sendMessage(MESSAGE_GAME_STATE_CHANGE, GAMESTATE_PLAYER_DONE_PLACING_SHIPS, 0);
			} else {
				setPlayerGameState(GAMESTATE_PLAYER_SELECTING_TILE);

			}
		}

		/**
		 * dreht das ausgewaehlte schiff nach links wenn moeglich
		 */
		private void actionTurnLeft() {
			if (selectedShip != null) {
				gbPlayer.placeShipInGrid(selectedShip.getPosition(), GameBoard.LayoutParams.ORIENTATION_NORTH, selectedShip);
			}
		}

		/**
		 * dreht das ausgewaehlte schiff nach rechts wenn moeglich
		 */
		private void actionTurnRight() {
			if (selectedShip != null) {
				gbPlayer.placeShipInGrid(selectedShip.getPosition(), GameBoard.LayoutParams.ORIENTATION_EAST, selectedShip);
			}
		}

		/**
		 * initialisiert das gegnerische feld
		 */
		private void initGridView() {
			for (int i = 0; i < 100; i++) {
				View tile = new View(getActivity());
				tile.setLayoutParams(new GameBoard.LayoutParams(1, 1));
				tile.setBackground(getResources().getDrawable(R.drawable.ic_tile_frame));
				tile.setTag(i);
				// ondraglistener auf jedes feld um schiffe zu setzen
				tile.setOnDragListener(new TileOnDragListener());
				gbPlayer.addView(tile);
			}
		}

		/**
		 * onclick methoden fuer die schiffe
		 */
		@Override
		public void onClick(View view) {
			startDrag(view);
		}

		@Override
		public boolean onLongClick(View view) {
			if (view.getParent() == picker || view.getParent() == gbPlayer)
				startDrag(view);
			return false;
		}

		/**
		 * erstellt die Views
		 */
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_game_player, container, false);
			sqareLayoutPlayer = (SquareLayout) rootView.findViewById(R.id.sqarelayout_player);

			gbPlayer = (GameBoard) rootView.findViewById(R.id.gameboard_player);
			picker = (ShipPicker) rootView.findViewById(R.id.ship_picker);

			shipAirCraftCarrier = new Ship(rootView.getContext(), GameBoard.LayoutParams.LAYOUT_TYPE_AIRCRAFTCARRIER);
			shipBattleShip = new Ship(rootView.getContext(), GameBoard.LayoutParams.LAYOUT_TYPE_BATTLESHIP);
			shipCruiser1 = new Ship(rootView.getContext(), GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_1);
			shipCruiser2 = new Ship(rootView.getContext(), GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_2);
			shipDestroyer = new Ship(rootView.getContext(), GameBoard.LayoutParams.LAYOUT_TYPE_DESTROYER);

			shipAirCraftCarrier.setTag(GameBoard.LayoutParams.LAYOUT_TYPE_AIRCRAFTCARRIER);
			shipBattleShip.setTag(GameBoard.LayoutParams.LAYOUT_TYPE_BATTLESHIP);
			shipCruiser1.setTag(GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_1);
			shipCruiser2.setTag(GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_2);
			shipDestroyer.setTag(GameBoard.LayoutParams.LAYOUT_TYPE_DESTROYER);

			picker.addView(shipAirCraftCarrier);
			picker.addView(shipBattleShip);
			picker.addView(shipCruiser1);
			picker.addView(shipCruiser2);
			picker.addView(shipDestroyer);

			sqareLayoutPlayer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

				@SuppressWarnings("deprecation")
				@Override
				public void onGlobalLayout() {
					Log.d(TAG, "OnGlobalLayout");
					if (Build.VERSION.SDK_INT < 16) {
						sqareLayoutPlayer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					} else {
						sqareLayoutPlayer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
					initGridView();
				}
			});

			btnTurnLeft = (ImageButton) rootView.findViewById(R.id.btn_turn_left);
			btnTurnLeft.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					actionTurnLeft();
				}
			});

			btnTurnRight = (ImageButton) rootView.findViewById(R.id.btn_turn_right);
			btnTurnRight.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					actionTurnRight();
				}
			});

			btnDone = (Button) rootView.findViewById(R.id.btn_done_placing_ships);
			btnDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					actionDonePlacingShips();
				}
			});

			picker.setOnDragListener(new PickerOnDragListener());

			for (int i = 0; i < picker.getChildCount(); i++) {
				picker.getChildAt(i).setOnLongClickListener(this);
				picker.getChildAt(i).setOnClickListener(this);
			}

			return rootView;
		}

		/**
		 * startet drag and drop geste f�r schiffe, +shadow
		 */
		private void startDrag(View shipView) {
			shipView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

			selectedShip = (Ship) shipView;

			btnTurnLeft.setVisibility(View.VISIBLE);
			btnTurnRight.setVisibility(View.VISIBLE);

			ClipData data = ClipData.newPlainText("", "");
			DragShadow shadow = new DragShadow(shipView);
			shipView.startDrag(data, shadow, shipView, 0);
		}
	}

	/**
	 * A simple pager adapter that represents 2 ScreenSlidePageFragment objects,
	 * in sequence.
	 */
	private class GameFragmentPagerAdapter extends FragmentPagerAdapter {
		public GameFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}

		@Override
		public Fragment getItem(int position) {
			Log.d(TAG, "Placing Fragments in pager");
			if (position == 0) {
				return gameFragmentPlayer;
			} else if (position == 1) {
				return gameFragmentOpponent;
			} else {
				return null;
			}
		}
	}

	// TODO m�ssen es zwei ondraglistener sein???
	/**
	 * custom ondraglistener, setzt schiffe bei drag geste auf entsprechendes
	 * feld
	 */
	private class PickerOnDragListener implements View.OnDragListener {

		@Override
		public boolean onDrag(View aPicker, DragEvent event) {
			Ship ship = (Ship) event.getLocalState();

			// different possible types of dragevents
			final int action = event.getAction();
			switch (action) {
			case DragEvent.ACTION_DRAG_STARTED:
				return true;
			case DragEvent.ACTION_DRAG_ENTERED:
				// reparenting if needed
				if (ship.getParent() != aPicker) {
					gameFragmentPlayer.gbPlayer.removeView(ship);
					ship.setOrientation(GameBoard.LayoutParams.ORIENTATION_EAST);
					MainActivity.this.gameFragmentPlayer.picker.addView(ship);
					ship.confirmChangesInLayout();
				}
				return true;
			case DragEvent.ACTION_DRAG_LOCATION:
				break;
			case DragEvent.ACTION_DROP:
				return true;
			case DragEvent.ACTION_DRAG_EXITED:
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				break;
			default:
				return false;
			}

			return false;
		}
	}

	private class TileOnDragListener implements View.OnDragListener {

		@Override
		public boolean onDrag(View v, DragEvent event) {
			int position;
			Ship ship = (Ship) event.getLocalState();

			// different possible types of dragevents
			final int action = event.getAction();
			switch (action) {
			case DragEvent.ACTION_DRAG_STARTED:
				return true;
			case DragEvent.ACTION_DRAG_ENTERED:
				// reparenting if needed
				if (ship.getParent() != gameFragmentPlayer.gbPlayer) {
					gameFragmentPlayer.picker.removeView(ship);
					Log.d(TAG, "Ship type: " + ship.getType());
					gameFragmentPlayer.gbPlayer.addView(ship);
					Log.d(TAG, "Views in gbPlayer: " + gameFragmentPlayer.gbPlayer.getChildCount());
				}

				position = (Integer) v.getTag();
				Log.d(TAG, "Ship dragged over " + position);

				ship = gameFragmentPlayer.gbPlayer.placeShipInGrid(position, ship.getOrientation(), ship);

				if (gameFragmentPlayer.picker.getChildCount() == 0) {
					Button btndone = (Button) findViewById(R.id.btn_done_placing_ships);
					btndone.setEnabled(true);
				}

				return true;
			case DragEvent.ACTION_DRAG_LOCATION:
				break;
			case DragEvent.ACTION_DROP:
				return true;
			case DragEvent.ACTION_DRAG_EXITED:
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				break;
			default:
				return false;
			}

			return false;
		}
	}

	private static final String TAG = "MainActivity";

	/**
	 * The number of pages in the FragmentPager
	 */
	private static final int NUM_PAGES = 2;

	/**
	 * spielphase des spielers,
	 */
	private int playerGameState = GAMESTATE_PLAYER_PLACING_SHIPS;

	/**
	 * punkte des spielers, bei 0 sind alle schiffe versenk
	 */
	private int pointCount = 17;

	/**
	 * The pager widget, which handles animation and allows swiping horizontally
	 * to access to game fragments
	 */
	private ViewPager mPager;

	/**
	 * The gameFragment attributes
	 */
	private GameFragmentOpponent gameFragmentOpponent = null;
	private GameFragmentPlayer gameFragmentPlayer = null;

	/**
	 * Name of the connected device
	 */
	private String mConnectedDeviceName = null;

	/**
	 * String buffer for outgoing messages
	 */
	private StringBuffer mOutStringBuffer;

	/**
	 * Local Bluetooth adapter
	 */
	private BluetoothAdapter mBluetoothAdapter = null;

	/**
	 * Member object for the chat services
	 */
	private BluetoothService mService = null;

	/**
	 * Layout Views
	 */
	private ImageButton btn_settings, btn_bluetooth;
	private TextView tv_connectionstate, tv_gamestate;

	/**
	 * MAC adresse des verbundenen ger�ts, f�r sp�tere wiederherstellung der
	 * verbindung
	 */
	private String mConnectedDeviceAddress;

	/**
	 * The Handler that gets information from the BluetoothService
	 */
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MESSAGE_CONNECTION_STATE_CHANGE:
				Log.d(TAG, "Refreshing connection state change");
				switch (msg.arg1) {
				case Constants.STATE_CONNECTED:
					setConnectionState(getString(R.string.title_connected_to) + " " + mConnectedDeviceName);
					btn_bluetooth.setImageDrawable(getResources().getDrawable((R.drawable.ic_bluetooth_connected_black)));
					MainActivity.this.sendMessage(REQUEST_GAME_STATE, 0, 0);
					break;
				case Constants.STATE_CONNECTING:
					setConnectionState(getString(R.string.title_connecting));
					btn_bluetooth.setImageDrawable(getResources().getDrawable((R.drawable.ic_bluetooth_searching_black)));
					break;
				case Constants.STATE_LISTEN:
					setConnectionState(getString(R.string.title_listening));
					btn_bluetooth.setImageDrawable(getResources().getDrawable((R.drawable.ic_bluetooth_black)));
					break;
				case Constants.STATE_NONE:
					setConnectionState(getString(R.string.title_not_connected));
					btn_bluetooth.setImageDrawable(getResources().getDrawable((R.drawable.ic_bluetooth_black)));
					break;
				default:
					break;
				}
				break;

			case MESSAGE_READ:
				// nachricht empfangen, an messageDecoder weiterleiten
				Log.d(TAG, "Message received, passing on to decoder");
				byte[] readBuf = (byte[]) msg.obj;
				String readMessage = new String(readBuf, 0, msg.arg1);
				messageDecoder(readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
				Log.d(TAG, "Connected Device's name: " + mConnectedDeviceName);
				break;
			case Constants.MESSAGE_TOAST:
				Toast.makeText(getApplication(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}
	};

	/**
	 * Establish a connection with another device
	 */
	private void connectDevice(String deviceAddress) {
		Log.d(TAG, "Device selected, attempting to connect to address: " + deviceAddress);
		// get the bluetooth device
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
		// Attempt to connect to the device
		mService.connect(device);
	}

	/**
	 * two players have to be in the same gamestate when connecting
	 */
	private void checkIfConnectionIsLegal(int enemyGameState) {

		if ((enemyGameState == GAMESTATE_PLAYER_PLACING_SHIPS && playerGameState == GAMESTATE_PLAYER_PLACING_SHIPS)
				|| (enemyGameState == GAMESTATE_PLAYER_SELECTED_TILE && playerGameState == GAMESTATE_PLAYER_SELECTING_TILE)
				|| (enemyGameState == GAMESTATE_PLAYER_SELECTING_TILE && playerGameState == GAMESTATE_PLAYER_SELECTED_TILE)
				|| (enemyGameState == GAMESTATE_PLAYER_SELECTED_TILE && playerGameState == GAMESTATE_PLAYER_DONE_PLACING_SHIPS)
				|| (enemyGameState == GAMESTATE_PLAYER_DONE_PLACING_SHIPS && playerGameState == GAMESTATE_PLAYER_PLACING_SHIPS)
				|| (enemyGameState == GAMESTATE_PLAYER_PLACING_SHIPS && playerGameState == GAMESTATE_PLAYER_DONE_PLACING_SHIPS)

		) {
		} else {
			Toast.makeText(this, R.string.title_illegal_connection, Toast.LENGTH_LONG).show();
			mService.start();
		}
	}

	private void ensureDiscoverable() {
		// make device discoverable
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 900);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Message decoder, decodes strings from bluetooth connection and decides
	 * what to do
	 */
	private void messageDecoder(String string) {

		Log.d(TAG, "Decoding Message: " + string);

		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == '#') {
				String stringRest = string.substring(i);
				string = string.substring(0, i);
				string.replace("#", "");

				if (stringRest.length() > 2) {
					if (stringRest.charAt(0) == '#')
						stringRest = stringRest.substring(1);
					messageDecoder(stringRest);
				}
			}
		}
		Message msg = Util.stringToMessage(string);
		switch (msg.what) {
		case MESSAGE_GAME_STATE_CHANGE:
			switch (msg.arg1) {
			case GAMESTATE_PLAYER_PLACING_SHIPS:
				break;
			case GAMESTATE_PLAYER_DONE_PLACING_SHIPS:
				gameFragmentPlayer.enemyDonePlacingShips = true;
				Log.d(TAG, "Enemy done placing Ships");
				break;
			case GAMESTATE_PLAYER_SELECTING_TILE:
				break;
			case GAMESTATE_PLAYER_SELECTED_TILE:
				break;
			default:
				break;
			}
		case MESSAGE_GAME_EVENT:
			switch (msg.arg1) {
			case EVENT_ENEMY_FIRED:
				Log.d(TAG, "Dealing with enemy fire");
				if (gameFragmentPlayer.gbPlayer.isShipHit(msg.arg2)) {
					gameFragmentPlayer.gbPlayer.addStateTile(msg.arg2, StateTile.TILESTATE_HIT);
					sendMessage(MESSAGE_GAME_EVENT, EVENT_HIT, msg.arg2);
					pointCount--;
					if (pointCount == 0) {
						actionGameLost();
					}
				} else {
					gameFragmentPlayer.gbPlayer.addStateTile(msg.arg2, StateTile.TILESTATE_MISS);
					sendMessage(MESSAGE_GAME_EVENT, EVENT_MISS, msg.arg2);
				}
				setPlayerGameState(GAMESTATE_PLAYER_SELECTING_TILE);
				break;
			case EVENT_HIT:
				Log.d(TAG, "Hit returned");
				gameFragmentOpponent.gbOpponent.addStateTile(msg.arg2, StateTile.TILESTATE_HIT);
				break;
			case EVENT_MISS:
				Log.d(TAG, "Miss returned");
				gameFragmentOpponent.gbOpponent.addStateTile(msg.arg2, StateTile.TILESTATE_MISS);
				break;
			case EVENT_SHIP_SUNK:
				break;
			case EVENT_ENEMY_WON:
				sendMessage(MESSAGE_GAME_EVENT, EVENT_PLAYER_WON, pointCount);
				showGameOverScreen(pointCount, msg.arg2, true);
				break;
			case EVENT_PLAYER_WON:
				showGameOverScreen(pointCount, msg.arg2, false);
			default:
				break;
			}

		case REQUEST_GAME_STATE:
			sendMessage(ANSWER_GAME_STATE, this.playerGameState, 0);
			break;
		case ANSWER_GAME_STATE:
			checkIfConnectionIsLegal(msg.arg1);
			break;
		default:
			break;
		}
	}

	private void showGameOverScreen(int pointsplayer, int pointsopponent, boolean winner) {

		// show game over fragment
		GameOverFragment fragment = new GameOverFragment(17 - pointsplayer, 17 - pointsopponent, winner);
		getSupportFragmentManager().beginTransaction().add(R.id.container_gameover_fragment, fragment).commit();
		mPager.setVisibility(View.GONE);
		tv_gamestate.setVisibility(View.GONE);
		findViewById(R.id.page_indicator).setVisibility(View.GONE);
	}

	private void actionGameLost() {
		sendMessage(MESSAGE_GAME_EVENT, EVENT_ENEMY_WON, pointCount);
	}

	/**
	 * Handles the information coming from the selectDevice Activity and
	 * connects to a new device
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// when devicelistactivity returns a device to connect to
			if (resultCode == Activity.RESULT_OK) {
				mConnectedDeviceAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				connectDevice(mConnectedDeviceAddress);
			}
			break;
		case REQUEST_ENABLE_BT:
			// when the request to enable bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				setupGame();
			} else {
				this.finish();
			}
		default:
			break;
		}
	}

	/**
	 * erlaubt es, im pager zur�ckzubl�ttern
	 */
	@Override
	public void onBackPressed() {
		if (mPager.getCurrentItem() == 0) {
			// If the user is currently looking at the first step, allow the
			// system to handle the
			// Back button. This calls finish() on this activity and pops the
			// back stack.
			super.onBackPressed();
		} else {
			// Otherwise, select the previous step.
			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
		}
	}

	/**
	 * onCreate der MainActivity, alle wichtigen views und attribute werden
	 * initialisiert
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// if the adapter is null the device does not support bluetooth
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			this.finish();
		}

		// find layout views
		tv_connectionstate = (TextView) findViewById(R.id.tv_connectionstate);
		tv_gamestate = (TextView) findViewById(R.id.tv_gamestate);

		// Instantiate the GameFragments
		gameFragmentOpponent = new GameFragmentOpponent();
		gameFragmentPlayer = new GameFragmentPlayer();

		// initiate buttons
		btn_bluetooth = (ImageButton) findViewById(R.id.btn_bluetooth);
		btn_bluetooth.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent selectDevice = new Intent(getApplicationContext(), DeviceListActivity.class);
				startActivityForResult(selectDevice, REQUEST_CONNECT_DEVICE_SECURE);
			}
		});

		btn_settings = (ImageButton) findViewById(R.id.btn_settings);
		btn_settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				opensettings();
			}
		});

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		GameFragmentPagerAdapter mPagerAdapter = new GameFragmentPagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setOffscreenPageLimit(NUM_PAGES);
		mPager.setPageTransformer(true, new ZoomOutPageTransformer());
		SimpleViewPagerIndicator pageIndicator = (SimpleViewPagerIndicator) findViewById(R.id.page_indicator);
		pageIndicator.setViewPager(mPager);
	}

	/**
	 * bluetooth service beendet wenn app geschlossen wird
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mService != null) {
			mService.stop();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mService.getState() == Constants.STATE_NONE) {
				// Start the Bluetooth chat services
				mService.start();
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

			ensureDiscoverable();

			// Otherwise, setup the game session
		} else if (mService == null) {
			setupGame();
		}

	}

	/**
	 * Opens the settings activity
	 */
	private void opensettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	/**
	 * sends a message to the handler of the connected device
	 */
	private void sendMessage(int what, int arg1, int arg2) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;

		String string = Util.messageToString(msg);

		// Check that we're actually connected before trying anything
		if (mService.getState() != Constants.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (string.length() > 0) {
			// Get the message bytes and tell the BluetoothService to write
			byte[] send = string.getBytes();
			mService.write(send);

			// Reset out string buffer to zero
			mOutStringBuffer.setLength(0);
		}
	}

	/**
	 * Updates the connection state in the UI
	 */
	private void setConnectionState(CharSequence status) {
		tv_connectionstate.setText(status);
	}

	/**
	 * Updates gameState in UI
	 */
	private void setPlayerGameState(int state) {

		playerGameState = state;

		switch (state) {
		case GAMESTATE_PLAYER_DONE_PLACING_SHIPS:
			tv_gamestate.setText(getResources().getString(R.string.state_done_placing_ships));
			break;
		case GAMESTATE_PLAYER_PLACING_SHIPS:
			tv_gamestate.setText(getResources().getString(R.string.state_placing_ships));
			break;
		case GAMESTATE_PLAYER_SELECTED_TILE:
			tv_gamestate.setText(getResources().getString(R.string.state_enemy_selecting_tile));
			break;
		case GAMESTATE_PLAYER_SELECTING_TILE:
			tv_gamestate.setText(getResources().getString(R.string.state_selecting_tile));
			break;

		default:
			break;
		}

	}

	/**
	 * set up the UI and the background operations for the game
	 */
	private void setupGame() {
		setPlayerGameState(GAMESTATE_PLAYER_PLACING_SHIPS);

		// Initialize the BluetoothService to perfom bluetooth connections
		mService = new BluetoothService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}
}
