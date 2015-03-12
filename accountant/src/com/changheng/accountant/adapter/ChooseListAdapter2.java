package com.changheng.accountant.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.ui.QuestionChooseActivity;
import com.changheng.accountant.view.MyGridView;

public class ChooseListAdapter2 extends BaseAdapter{
	private Context context;
	private QuestionChooseActivity activity;
	private int[] questionList;
	private String action;
	public ChooseListAdapter2(Context context,QuestionChooseActivity activity,int[] questionList,String action) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.activity = activity;
		this.action = action;
		this.questionList = questionList;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
			return 1;
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
		v = LayoutInflater.from(context).inflate(R.layout.list_question_directory, null);
		MyGridView gridView = (MyGridView) v.findViewById(R.id.directory_exam_grid);
		gridView.setAdapter(new QuestionGridAdapter2(context,questionList,null));
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if("submitPaper".equals(action))
				{
					Intent data=new Intent();  
		         	data.putExtra("action", "showQuestionWithAnswer");  
		         	data.putExtra("cursor", Integer.parseInt(((TextView)arg1.findViewById(R.id.optionTextView)).getText().toString())-1);  
		         	//设置请求代码20  
		         	activity.setResult(20, data);  
		         	//结束Activity  
		         	activity.finish();
				}else
				{
					activity.showAnswer(Integer.parseInt(((TextView)arg1.findViewById(R.id.optionTextView)).getText().toString())-1);
				}
			}
		});
		return v;
	}
}