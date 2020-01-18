package com.jona.schiffeversenken;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class Ship extends View {

	private static final String TAG = "Ship";

	private int orientation = GameBoard.LayoutParams.ORIENTATION_EAST;
	private int type;
	private int position;
	private int shipLength;
	private boolean selected = false;

	public Ship(Context context) {
		super(context);
	}

	public Ship(Context context, int shipType) {
		super(context);
		this.type = shipType;
		setImage();
		setLength();
		setLayoutParams(new GameBoard.LayoutParams(position, type, orientation));
	}

	public Ship(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public Ship(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.com_jona_schiffeversenken_Ship, 0, 0);
		try {
			position = a.getInt(R.styleable.com_jona_schiffeversenken_Ship_position, position);
			type = a.getInt(R.styleable.com_jona_schiffeversenken_Ship_layout_type, type);
			orientation = a.getInt(R.styleable.com_jona_schiffeversenken_Ship_ship_orientation, orientation);
		} catch (Exception e) {
			Log.d(TAG, "Reading attributes failed");
		} finally {
			a.recycle();
		}

		setImage();
		setLength();
	}

	private void setImage() {
		switch (orientation) {
		case GameBoard.LayoutParams.ORIENTATION_EAST:

			switch (type) {
			case GameBoard.LayoutParams.LAYOUT_TYPE_AIRCRAFTCARRIER:
				setBackground(getResources().getDrawable(R.drawable.ship_aircraft_carrier_east));
				break;
			case GameBoard.LayoutParams.LAYOUT_TYPE_BATTLESHIP:
				setBackground(getResources().getDrawable(R.drawable.ship_battleship_east));
				break;
			case GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_1:
			case GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_2:
				setBackground(getResources().getDrawable(R.drawable.ship_cruiser_east));
				break;
			case GameBoard.LayoutParams.LAYOUT_TYPE_DESTROYER:
				setBackground(getResources().getDrawable(R.drawable.ship_destroyer_east));
				break;
			default:
				break;
			}
			break;

		case GameBoard.LayoutParams.ORIENTATION_NORTH:

			switch (type) {
			case GameBoard.LayoutParams.LAYOUT_TYPE_AIRCRAFTCARRIER:
				setBackground(getResources().getDrawable(R.drawable.ship_aircraft_carrier_north));
				break;
			case GameBoard.LayoutParams.LAYOUT_TYPE_BATTLESHIP:
				setBackground(getResources().getDrawable(R.drawable.ship_battleship_east_north));
				break;
			case GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_1:
			case GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_2:
				setBackground(getResources().getDrawable(R.drawable.ship_cruiser_north));
				break;
			case GameBoard.LayoutParams.LAYOUT_TYPE_DESTROYER:
				setBackground(getResources().getDrawable(R.drawable.ship_destroyer_north));
				break;
			default:
				break;
			}
			break;
		}
	}

	private void setLength() {
		switch (type) {
		case GameBoard.LayoutParams.LAYOUT_TYPE_AIRCRAFTCARRIER:
			shipLength = 5;
			break;
		case GameBoard.LayoutParams.LAYOUT_TYPE_BATTLESHIP:
			shipLength = 4;
			break;
		case GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_1:
		case GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_2:
			shipLength = 3;
			break;
		case GameBoard.LayoutParams.LAYOUT_TYPE_DESTROYER:
			shipLength = 2;
			break;
		default:
			break;
		}
	}

	/**
	 * Setze die Orientierung des Schiffes ohne es im Layout wirksam zu machen
	 */
	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	/**
	 * Setze die Position des Schiffes ohne es im Layout wirksam zu machen
	 */
	public void setPosition(int position) {
		if (position >= 0 && position < 100) {
			this.position = position;
		}
	}

	/**
	 * setzen ob schiff ausgewaehlt/im drag ist
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
		if (selected) {
			setBackground(getResources().getDrawable(R.drawable.ic_tile_frame_selected));
		} else {
			setBackground(getResources().getDrawable(R.color.transparent));
		}
	}

	/**
	 * Aktualisiert die �nderungen im Layout
	 */
	public void confirmChangesInLayout() {
		setImage();
		GameBoard.LayoutParams lp = (GameBoard.LayoutParams) this.getLayoutParams();
		lp.setPosition(this.position);
		lp.setOrientation(this.orientation);
		lp.setType(this.type);
		setVisibility(View.VISIBLE);
		setLayoutParams(lp);
		Log.d(TAG, "Confirmed changes to: " + position + ", orientation to: " + orientation + " and type to: " + type
				+ ", requesting layout");

		invalidate();
		requestLayout();
		getParent().requestLayout();
	}

	public int getOrientation() {
		return orientation;
	}

	public int getType() {
		return type;
	}

	public int getPosition() {
		return position;
	}

	public int getShipLength() {
		return shipLength;
	}

	public boolean isSelected() {
		return selected;
	}

}
