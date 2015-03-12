package com.changheng.accountant.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.entity.ExamRecord;

public class PaperListAdapter2 extends BaseAdapter{
	private Context context;
	private List<ExamRecord> records;
	public PaperListAdapter2(Context context,List<ExamRecord> records) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.records = records;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(records!=null)
			return records.size();
		return 0;
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(records!=null)
			return records.get(position);
		return null;
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(convertView==null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.list_question_exam, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.paper_name_TextView);
			holder.img = (ImageView) convertView.findViewById(R.id.paper_tyep_imgView);
			holder.info = (TextView) convertView.findViewById(R.id.paper_info_TextView);
			holder.lastTime = (TextView) convertView.findViewById(R.id.lastTimeTextView);
			convertView.setTag(holder);
		}else
		{
			holder = (ViewHolder) convertView.getTag(); 
		}
		ExamRecord r = records.get(position);
		holder.title.setText(r.getPapername());
		if(r.getMode() == 1) holder.img.setImageResource(R.drawable.record_exercise_img);
		if(r.getAnswers()!=null)
		{
			if(r.getMode()==1)
				holder.info.setText("答题进度: "+r.getHasDone()+"/"+r.getPaperscore());
			else
				holder.info.setText("上次得分: "+r.getScore()+"分");
			holder.info.setTextColor(context.getResources().getColor(R.color.red));
		}else
		{
			holder.info.setText("继续考试");
			holder.info.setTextColor(context.getResources().getColor(R.color.blue));
		}
		holder.lastTime.setText(r.getLastTime());
		return convertView;
	}
	static class ViewHolder
	{
		TextView title,info,lastTime;
		ImageView img;
		//Button doExam;
	}
}
