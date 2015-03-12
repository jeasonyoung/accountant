package com.changheng.accountant.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.entity.QuestionAdapterData;

public class QuestionCommonAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private ArrayList<QuestionAdapterData> dataList;
	private int action;
	public QuestionCommonAdapter(Context context,ArrayList<QuestionAdapterData> datalist,int action) {
		// TODO Auto-generated constructor stub
		this.mInflater = LayoutInflater.from(context);
		this.dataList = datalist;
		this.action = action;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return dataList.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return dataList.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(v == null)
		{
			v = mInflater.inflate(R.layout.item_choosecourse_list, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) v.findViewById(R.id.home_list_icon);
			holder.title = (TextView) v.findViewById(R.id.list_title);
			holder.count = (TextView) v.findViewById(R.id.txt_count);
			int rid = action==0?R.drawable.icon_book:action==1?R.drawable.icon_timer:R.drawable.icon_frequent;
			holder.icon.setImageResource(rid);
			holder.count.setVisibility(View.VISIBLE);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		holder.title.setText(dataList.get(position).getTitle());
		holder.count.setText("共 "+dataList.get(position).getCount()+" 个");
		return v;
	}
	static class ViewHolder
	{
		ImageView icon;
		TextView title;
		TextView count;
	}
}
