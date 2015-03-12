package com.changheng.accountant.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.changheng.accountant.R;

public class MainGridAdapter extends BaseAdapter{
	private TypedArray imagebtns;
	private StateListDrawable[] bds;
	private String[] texts;
	private Context context;
	private int i = 0;
	private int j = 0;
	private int k = 0;
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return texts.length;
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return Integer.valueOf(this.texts[position]);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if(v==null)
		{
			v = LayoutInflater.from(context).inflate(R.layout.item_main_grid, null);
			if (this.i != 0)
		      {
				if(this.k<this.i) this.k = this.i;
		        AbsListView.LayoutParams localLayoutParams = new AbsListView.LayoutParams(this.i, this.k);
		        localLayoutParams.width = this.i;
		        localLayoutParams.height = this.k;
		        v.setLayoutParams(localLayoutParams);
		      }
			holder = new ViewHolder();
			holder.img = (ImageView) v.findViewById(R.id.home_item_icon);
			if(j!=0)
				holder.img.setLayoutParams(new LinearLayout.LayoutParams(j,j));
			holder.txt = (TextView) v.findViewById(R.id.gridview_name);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		bds[position].setCallback(v);
		v.setBackgroundDrawable(bds[position]);
		try{
			holder.img.setImageResource(this.imagebtns.getResourceId(position, -1));//setImageDrawable(r.getDrawable(this.imagebtns.getResourceId(position, -1)));//
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally
		{
			if(position==imagebtns.length()-1)
				imagebtns.recycle();
		}
		holder.txt.setText(this.texts[position]);
		return v;
	}
	public MainGridAdapter(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		Resources localResources = this.context.getResources();
		imagebtns = localResources.obtainTypedArray(R.array.home_grid_drawable_ids);
		texts = localResources.getStringArray(R.array.home_grid_titles);
		bds = getDrawables(localResources);
		//屏幕的高宽
		WindowManager localWindowManager = (WindowManager)context.getSystemService("window");
		int width = localWindowManager.getDefaultDisplay().getWidth();
	    int height  = localWindowManager.getDefaultDisplay().getHeight();
	    //密度
	    float destiny = context.getResources().getDisplayMetrics().density;
	    this.i = ((width - 4 * (int)context.getResources().getDimension(R.dimen.home_grid_space)) / 3);
	    this.j = width/8;
	    this.k = (int) ((height-280*destiny)/3); //计算一个格子的高度
	}
	private StateListDrawable[] getDrawables(Resources localResources) {
		StateListDrawable[] ds = new StateListDrawable[imagebtns.length()];
		TypedArray rids = localResources.obtainTypedArray(R.array.home_grid_color_ids);
		int background = R.drawable.background2;
		Resources r = context.getResources();
		int pressed = android.R.attr.state_pressed;
		// int window_focused = android.R.attr.state_window_focused;
		// int focused = android.R.attr.state_focused;
		// int selected = android.R.attr.state_selected;
		for (int i = 0; i < ds.length; i++) {
			ds[i] = new StateListDrawable();
			// ds[i].addState(new int []{pressed , window_focused},
			// r.getDrawable(background));
			// ds[i].addState(new int []{pressed , -focused},
			// r.getDrawable(rids[i]));
			// ds[i].addState(new int []{selected },
			// r.getDrawable(rids[i]));
			// ds[i].addState(new int []{focused }, r.getDrawable(rids[i]));
			ds[i].addState(new int[] { pressed }, r.getDrawable(background));
			ds[i].addState(new int[0], r.getDrawable(rids.getResourceId(i, -1)));
		}
		rids.recycle();
		return ds;
	}
	static class ViewHolder 
	{
		ImageView img;
		TextView txt;
	}
}
