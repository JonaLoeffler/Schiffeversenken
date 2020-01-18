package com.jona.schiffeversenken;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ShipPicker extends ViewGroup {

	private int parentHeight;
	private int parentWidth;

	private int childHeight;
	private int childWidth;

	private final Rect mTmpChildRect = new Rect();

	public ShipPicker(Context context) {
		super(context);
	}

	public ShipPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ShipPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * prevent Layout from scrolling
	 */
	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		parentWidth = MeasureSpec.getSize(widthMeasureSpec);

		childHeight = parentHeight / 5;
		childWidth = parentWidth / 5;

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {

				GameBoard.LayoutParams lp = (GameBoard.LayoutParams) child.getLayoutParams();

				switch (lp.type) {
				case GameBoard.LayoutParams.LAYOUT_TYPE_AIRCRAFTCARRIER:
					mTmpChildRect.top = 0;
					mTmpChildRect.left = 0;
					mTmpChildRect.bottom = childHeight;
					mTmpChildRect.right = 5 * childWidth;
					break;
				case GameBoard.LayoutParams.LAYOUT_TYPE_BATTLESHIP:
					mTmpChildRect.top = childHeight;
					mTmpChildRect.left = 0;
					mTmpChildRect.bottom = 2 * childHeight;
					mTmpChildRect.right = 4 * childWidth;
					break;
				case GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_1:
					mTmpChildRect.top = 2 * childHeight;
					mTmpChildRect.left = 0;
					mTmpChildRect.bottom = 3 * childHeight;
					mTmpChildRect.right = 3 * childWidth;
					break;
				case GameBoard.LayoutParams.LAYOUT_TYPE_CRUISER_2:
					mTmpChildRect.top = 3 * childHeight;
					mTmpChildRect.left = 0;
					mTmpChildRect.bottom = 4 * childHeight;
					mTmpChildRect.right = 3 * childWidth;
					break;
				case GameBoard.LayoutParams.LAYOUT_TYPE_DESTROYER:
					mTmpChildRect.top = 4 * childHeight;
					mTmpChildRect.left = 0;
					mTmpChildRect.bottom = 5 * childHeight;
					mTmpChildRect.right = 2 * childWidth;
					break;

				default:
					break;
				}

				child.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);
			}
		}
	}
}
