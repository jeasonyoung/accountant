package com.changheng.accountant.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.changheng.accountant.R;

public class QuestionGridAdapter extends BaseAdapter{
	private Context context;
	private String[] data;
	private SparseBooleanArray isDone;
	//
	private int[] trueOfFalse;
	public QuestionGridAdapter(Context context,String[] data,SparseBooleanArray isDone,int[] trueOfFalse) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.data = data;
		this.isDone = isDone;
		this.trueOfFalse = trueOfFalse;
		System.out.println("trueOfFalse3 == "+trueOfFalse);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.length;
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data[position];
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	public android.view.View getView(int position, android.view.View v, android.view.ViewGroup parent) {
		ViewHolder holder = null;
		if(v == null)
		{
			v = LayoutInflater.from(context).inflate(R.layout.list_question_option, null);
			holder = new ViewHolder();
			holder.textView = (TextView) v.findViewById(R.id.optionTextView);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		holder.textView.setText(data[position]);
		if(isDone!=null&&isDone.get(Integer.parseInt(data[position])-1))
		{
			if(trueOfFalse != null)
			{
				int colorid = trueOfFalse[Integer.parseInt(data[position])-1]==1?context.getResources().getColor(R.color.green)
						:trueOfFalse[Integer.parseInt(data[position])-1]==-1?context.getResources().getColor(R.color.red)
								:context.getResources().getColor(R.color.transparent);
				v.setBackgroundColor(colorid);
			}else
				v.setBackgroundColor(context.getResources().getColor(R.color.lightbule));
			
		}
		return v;
	};
	static class ViewHolder
	{
		TextView textView;
	}
}
