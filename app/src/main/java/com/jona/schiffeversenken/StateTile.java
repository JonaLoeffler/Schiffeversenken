package com.jona.schiffeversenken;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class StateTile extends View {

	private static final String TAG = "StateTile";

	public static final int TILESTATE_NONE = 0;
	public static final int TILESTATE_MISS = 1;
	public static final int TILESTATE_HIT = 2;

	private int state = TILESTATE_NONE;
	private int position;

	public StateTile(Context context, int position, int state) {
		super(context);
		GameBoard.LayoutParams lp = new GameBoard.LayoutParams(1, 1);
		lp.setType(GameBoard.LayoutParams.LAYOUT_TYPE_STATETILE);
		setLayoutParams(lp);

		this.state = state;
		this.position = position;
		setImage();
	}

	public StateTile(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public StateTile(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.com_jona_schiffeversenken_Ship, 0, 0);
		try {
			position = a.getInt(R.styleable.com_jona_schiffeversenken_Ship_position, position);
		} catch (Exception e) {
			Log.d(TAG, "Reading attributes failed");
		} finally {
			a.recycle();
		}
	}

	/**
	 * Aktualisiert die Änderungen im Layout
	 */
	public void confirmChangesInLayout() {
		setImage();
		GameBoard.LayoutParams lp = (GameBoard.LayoutParams) this.getLayoutParams();
		lp.setType(GameBoard.LayoutParams.LAYOUT_TYPE_STATETILE);
		lp.setPosition(this.position);
		setVisibility(View.VISIBLE);
		setLayoutParams(lp);
		bringToFront();

		invalidate();
		requestLayout();
		getParent().requestLayout();
	}

	private void setImage() {
		switch (state) {
		case TILESTATE_HIT:
			setBackground(getResources().getDrawable(R.drawable.ic_tile_hit));
			break;
		case TILESTATE_MISS:
			setBackground(getResources().getDrawable(R.drawable.ic_tile_miss));
			break;
		case TILESTATE_NONE:
			setBackground(null);
			break;

		default:
			break;
		}
	}

	public void setState(int aState) {
		state = aState;
		setImage();
	}

	public void setPosition(int posID) {
		position = posID;
	}

	public int getPosition() {
		return position;
	}

}
