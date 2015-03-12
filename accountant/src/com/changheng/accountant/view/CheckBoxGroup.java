package com.changheng.accountant.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.changheng.accountant.R;

public class CheckBoxGroup extends LinearLayout{
	private List<OptionLayout> checkboxList = new ArrayList<OptionLayout>();
	private StringBuffer buf = new StringBuffer();
	public CheckBoxGroup(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
	}
	public CheckBoxGroup(Context context, AttributeSet attrs)
	{
		super(context,attrs);
	}
	@Override
	public void addView(View child, int index) {
		// TODO Auto-generated method stub
		super.addView(child, index);
		checkboxList.add((OptionLayout) child);
	}
	@Override
	public void addView(View child) {
		// TODO Auto-generated method stub
		super.addView(child);
		checkboxList.add((OptionLayout) child);
	}
	public String getValue()
	{
		if(buf.length()>0)
		{
			buf.delete(0, buf.length());
		}
		for(OptionLayout cb: checkboxList)
		{
			if(cb.isChecked())
			{
				buf.append(cb.getValue()).append(",");
			}
		}
		return buf.length()>0?buf.deleteCharAt(buf.length()-1).toString():"";
	}
	@Override
	public void removeAllViews() {
		// TODO Auto-generated method stub
		super.removeAllViews();
		checkboxList.removeAll(checkboxList);
	}
	@Override
	public void removeViewAt(int index) {
		// TODO Auto-generated method stub
		super.removeViewAt(index);
		checkboxList.remove(index);
	}
	
	//����ѡ�л���
	public void setOnlyOneCheck(OptionLayout box)
	{
		for(OptionLayout cb: checkboxList)
		{
			if(box.getId() != cb.getId() && cb.getType()==OptionLayout.RADIO_BUTTON)
			{
				cb.setChecked(false);
			}
		}
	}
	public void clearCheck()
	{
		for(OptionLayout cb: checkboxList)
		{
			cb.setChecked(false);
		}
	}
	public void forbidden(boolean flag)
	{
		for(OptionLayout cb: checkboxList)
		{
			cb.setClickable(flag);
		}
	}
	public int getChildCount()
	{
		if(checkboxList!=null)
			return checkboxList.size();
		return 0;
	}
	public OptionLayout getChildAt(int index)
	{
		if(checkboxList!=null)
			return checkboxList.get(index);
		return null;
	}
	/**
	 * 设置特定答案值的字体的颜色
	 * @param color
	 * @param value
	 */
	public void setFontColor(int rightColor,String rightAnswer,int wrongColor,String userAnswer,String type)
	{
		if("2".equals(type)||"3".equals(type))
		{
			for(OptionLayout cb: checkboxList)
			{
				if(rightAnswer.contains(cb.getValue()))
				{
					cb.setFontColor(rightColor);
				}
			}
			return;
		}
		if("1".equals(userAnswer))
		{
			userAnswer = "A";
		}else if("0".equals(userAnswer))
		{
			userAnswer = "B";
		}
		if("1".equals(rightAnswer))
		{
			rightAnswer = "A";
		}else if("0".equals(rightAnswer))
		{
			rightAnswer = "B";
		}
		for(OptionLayout cb: checkboxList)
		{
			if(userAnswer.contains(cb.getValue()))
			{
				cb.setFontColor(wrongColor);
				cb.setButtonDrawable(R.drawable.exercise_option_f);
				cb.setTextWithLine();
			}
			if(rightAnswer.contains(cb.getValue()))
			{
				cb.setFontColor(rightColor);
				cb.setButtonDrawable(R.drawable.exercise_option_t);
				cb.resetTextStyle();
			}
		}
	}
	public void setFontSize(float size)
	{
		for(OptionLayout cb: checkboxList)
		{
			cb.setFontSize(size);
		}
	}
}
