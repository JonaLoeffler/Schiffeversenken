package com.jona.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

@SuppressWarnings("unused")
public class SquareLayout extends LinearLayout {

	private static final String TAG = "SquareLayout";

	private int parentHeight;
	private int parentWidth;

	public SquareLayout(Context context) {
		super(context);
	}

	public SquareLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// make sure view is definitely square
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);

		parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		parentWidth = MeasureSpec.getSize(widthMeasureSpec);

		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int size = width > height ? height : width;
		setMeasuredDimension(size, size);

		// Log.d(TAG, "ImageView is sqare");
	}
}
