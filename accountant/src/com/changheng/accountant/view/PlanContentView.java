package com.changheng.accountant.view;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.entity.Knowledge;

public class PlanContentView extends LinearLayout{
	private TextView title;
	private TextView content;
	private View view;
	public PlanContentView(Context context, AttributeSet paramAttributeSet) {
		super(context, paramAttributeSet);
		// TODO Auto-generated constructor stub
		this.view = LayoutInflater.from(context).inflate(R.layout.section_list_subitem,
				this, false);
		init();
	}
	private void init()
	{
		this.title = (TextView) view.findViewById(R.id.txt_subtitle);
		this.content = (TextView) view.findViewById(R.id.txt_content);
		addView(this.view);
		this.view = null;
		System.gc();
	}
	public void fillTextView(int i,Knowledge k)
	{
		if(k!=null)
		{
			this.title.setText(i+", "+k.getTitle());
			this.content.setText(Html.fromHtml(k.getFullContent()));
//			this.content.setText(k.getFullContent());
		}
	}
}
