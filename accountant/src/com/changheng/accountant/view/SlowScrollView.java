package com.changheng.accountant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * 控制滑动速度的
 * @author Administrator
 *
 */
public class SlowScrollView extends ScrollView{
	 public SlowScrollView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
     public SlowScrollView(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     public SlowScrollView(Context context) {
         super(context);
     }
 
     /**
      * 滑动事件
      */
     @Override
     public void fling(int velocityY) {
         super.fling(velocityY / 2);		//滑动速度减慢到原来二分之一的速度
     }
}
