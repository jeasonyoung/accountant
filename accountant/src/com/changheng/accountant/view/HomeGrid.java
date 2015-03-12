package com.changheng.accountant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class HomeGrid extends GridView {
	public HomeGrid(Context paramContext) {
		super(paramContext);
	}

	public HomeGrid(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	public HomeGrid(Context paramContext, AttributeSet paramAttributeSet,
			int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
	}

	public void onMeasure(int widthMeasureSpec, int paramInt2) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}