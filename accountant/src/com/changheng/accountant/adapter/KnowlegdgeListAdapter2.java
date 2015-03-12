package com.changheng.accountant.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.entity.Knowledge;

public class KnowlegdgeListAdapter2 extends BaseAdapter{
	private LayoutInflater inflater;
	private ArrayList<Knowledge> list;
	
	public KnowlegdgeListAdapter2(Context context,ArrayList<Knowledge> list) {
		// TODO Auto-generated constructor stub
		this.inflater = LayoutInflater.from(context);
		this.list = list;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(list!=null)
		{
			System.out.println("list size = "+list.size());
			return list.size();
		}
		return 0;
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(list!=null){
			return list.get(position);
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
			v = inflater.inflate(R.layout.row, null);
			holder = new ViewHolder();
			holder.title = (TextView) v.findViewById(R.id.menu_item);
			holder.title.setTextColor(v.getResources().getColor(R.color.lightbule));
			v.setTag(holder);
		}else
			holder = (ViewHolder) v.getTag();
		System.out.println(position + " "+list.get(position).getTitle());
		holder.title.setText(list.get(position).getTitle());
		return v;
	}
	static class ViewHolder
	{
		TextView title;
	}
}
