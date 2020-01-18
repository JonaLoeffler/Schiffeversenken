package com.jona.schiffeversenken;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class PlacingShipsDialog extends ViewGroup {

	private int parentHeight;
	private int parentWidth;

	private int childHeight;
	private int childWidth;

	Rect mTmpRect = new Rect();

	public PlacingShipsDialog(Context context) {
		super(context);
	}

	public PlacingShipsDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PlacingShipsDialog(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
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

		childHeight = parentHeight / 2;
		childWidth = parentWidth / 2;

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				switch (i) {
				case 0:
					mTmpRect.top = 0;
					mTmpRect.left = 0;
					mTmpRect.right = childWidth;
					mTmpRect.bottom = childHeight;
					break;
				case 1:
					mTmpRect.top = 0;
					mTmpRect.left = childWidth;
					mTmpRect.right = 2 * childWidth;
					mTmpRect.bottom = childHeight;
					break;
				case 2:
					mTmpRect.top = childHeight;
					mTmpRect.left = 0;
					mTmpRect.right = childWidth;
					mTmpRect.bottom = 2 * childHeight;
					break;
				case 3:
					mTmpRect.top = childHeight;
					mTmpRect.left = childWidth;
					mTmpRect.right = 2 * childWidth;
					mTmpRect.bottom = 2 * childHeight;
					break;

				default:
					break;
				}
				child.layout(mTmpRect.left, mTmpRect.top, mTmpRect.right,
						mTmpRect.bottom);
			}
		}
	}
}
