package com.changheng.accountant.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.entity.Chapter;

public class ChapterListAdapter extends BaseAdapter {
	private List<Chapter> list;
	private LayoutInflater inflater;
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(list == null)
			return 0;
		return list.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(list == null)
			return null;
		return list.get(position);
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
			v = inflater.inflate(R.layout.item_choosecourse_list, null);
			holder = new ViewHolder();
			holder.title = (TextView) v.findViewById(R.id.list_title);
			holder.count = (TextView) v.findViewById(R.id.txt_count);
			holder.count.setVisibility(View.VISIBLE);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		holder.title.setText(list.get(position).toString());
		holder.count.setText("共 "+list.get(position).getQuestionCount()+" 题");
		return v;
	}
	public ChapterListAdapter(Context context,List<Chapter> list) {
		// TODO Auto-generated constructor stub
		this.inflater = LayoutInflater.from(context);
		this.list = list;
	}
	static class ViewHolder
	{
		TextView title;
		TextView count;
	}
}
