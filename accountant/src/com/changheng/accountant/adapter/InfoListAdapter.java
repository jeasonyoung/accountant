package com.changheng.accountant.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.entity.Info;

public class InfoListAdapter extends BaseAdapter{
	private ArrayList<Info> list;
	private LayoutInflater mInflater;
	public InfoListAdapter(Context context,ArrayList<Info> list) {
		mInflater = LayoutInflater.from(context);
		this.list = list;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(list!=null)
		{
			return list.size();
		}
		return 0;
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(list!=null)
		{
			list.get(position);
		}
		return null;
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
			v = mInflater.inflate(R.layout.item_information_list, null);
			holder = new ViewHolder();
			holder.title = (TextView) v.findViewById(R.id.list_title);
			holder.time = (TextView) v.findViewById(R.id.txt_time);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		Info info = list.get(position);
		holder.title.setText(info.getTitle());
		holder.time.setText(info.getAddTime());
		return v;
	}
	static class ViewHolder {
		TextView title;
		TextView time;
	}
}
