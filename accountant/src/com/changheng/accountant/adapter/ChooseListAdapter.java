package com.changheng.accountant.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.entity.ExamRule;
import com.changheng.accountant.ui.QuestionChooseActivity;
import com.changheng.accountant.view.MyGridView;

public class ChooseListAdapter extends BaseAdapter{
	private Context context;
	private QuestionChooseActivity activity;
	private List<ExamRule> ruleList;
	private SparseBooleanArray isDone;
	private int[] trueOfFalse;
	public ChooseListAdapter(Context context,QuestionChooseActivity activity,List<ExamRule> ruleList,SparseBooleanArray isDone,int[] trueOfFalse) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.activity = activity;
		this.ruleList = ruleList;
		this.isDone = isDone;
		this.trueOfFalse = trueOfFalse;
		System.out.println("trueOfFalse2 == "+trueOfFalse);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(ruleList==null)
			return 1;
		return ruleList.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
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
		final ExamRule r = ruleList.get(position);
		if(v == null)
		{
			v = LayoutInflater.from(context).inflate(R.layout.list_question_directory, null);
			holder = new ViewHolder();
			holder.textView = (TextView) v.findViewById(R.id.directory_exam_RulesTextView);
			holder.gridView = (MyGridView) v.findViewById(R.id.directory_exam_grid);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		holder.textView.setText(r.getRuleTitle());
		int length = r.getQuestionNum();
		String[] data = new String[length];
		int t = measureTotal(position);
		for(int i=0;i<length;i++)
		{
			data[i] = String.valueOf((i+1+t));
		}
		holder.gridView.setAdapter(new QuestionGridAdapter(context,data,isDone,trueOfFalse));
		holder.gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				 Intent data=new Intent();  
		         data.putExtra("ruleTitle", r.getRuleTitle());  
		         data.putExtra("action", trueOfFalse==null?"DoExam":"practice");
		         data.putExtra("cursor", Integer.parseInt(((TextView)arg1.findViewById(R.id.optionTextView)).getText().toString())-1);  
		         //设置请求代码20  
		         activity.setResult(20, data);  
		         //结束Activity  
		         activity.finish();  
			}
		});
		return v;
	}
	static class ViewHolder
	{
		MyGridView gridView;
		TextView textView;
	}
	private int measureTotal(int position)
	{
		int total = 0;
		for(int i=position-1;i>=0;i--)
		{
			total+= ruleList.get(i).getQuestionNum();
		}
		return total;
	}
}
