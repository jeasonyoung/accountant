package com.changheng.accountant.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.EditText;

public class ImgRightEditText extends EditText{

	public ImgRightEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public ImgRightEditText(Context paramContext, AttributeSet paramAttributeSet)
	  {
	    super(paramContext, paramAttributeSet);
	  }

	  public final void setRightImg(int paramInt)
	  {
	    setCompoundDrawablesWithIntrinsicBounds(0, 0, paramInt, 0);
	  }

	  protected void onDraw(Canvas paramCanvas)
	  {
	    super.onDraw(paramCanvas);
	  }

	  protected void onMeasure(int paramInt1, int paramInt2)
	  {
	    super.onMeasure(paramInt1, paramInt2);
	  }
}
