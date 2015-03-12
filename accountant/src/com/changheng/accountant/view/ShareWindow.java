package com.changheng.accountant.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.changheng.accountant.R;

public class ShareWindow extends PopupWindow {
	private Context mContext;
	private View view;
	private GridView gridView;
	
	public ShareWindow(Context context,OnItemClickListener listener) {
		super(context);
		mContext = context;
		init(listener);
	}
	private void init(OnItemClickListener listener)
	{
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService("layout_inflater");
		view = inflater.inflate(R.layout.share_dialog, null);
		this.setContentView(view);
		gridView = (GridView) view.findViewById(R.id.share_grid_items);
		gridView.setAdapter(new ShareGridAdapter(mContext));
		gridView.setOnItemClickListener(listener);
		((Button)view.findViewById(R.id.share_btn_cancle)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ShareWindow.this.dismiss();
			}
		});
		view.findViewById(R.id.share_layout_parent).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ShareWindow.this.dismiss();
			}
		});
		this.setBackgroundDrawable(new BitmapDrawable());
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.MATCH_PARENT);
	}
	private class ShareGridAdapter extends BaseAdapter
	{
		private TypedArray imagebtns;
		private String[] texts;
		private Context context;
		public ShareGridAdapter(Context context) {
			// TODO Auto-generated constructor stub
			this.context = context;
			Resources localResources = this.context.getResources();
			imagebtns = localResources.obtainTypedArray(R.array.share_grid_drawable_ids);
			texts = localResources.getStringArray(R.array.share_grid_titles);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return texts.length;
		}
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return texts[position];
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
			if(v == null)
			{
				holder = new ViewHolder();
				v = LayoutInflater.from(context).inflate(R.layout.share_grid_item, null);
				holder.icon = (ImageView) v.findViewById(R.id.share_btn_icon);
				holder.txt = (TextView) v.findViewById(R.id.share_btn_title);
				v.setTag(holder);
			}
			holder = (ViewHolder) v.getTag();
			holder.icon.setImageResource(imagebtns.getResourceId(position, -1));
			holder.txt.setText(texts[position]);
			return v;
		}
		class ViewHolder
		{
			ImageView icon;
			TextView txt;
		}
	}
}
