package com.changheng.accountant.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.changheng.accountant.AppConfig;
import com.changheng.accountant.R;
import com.changheng.accountant.entity.Course;

public class CourseSelectAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private ArrayList<Course> courseList;
	private AppConfig appConfig;
	public CourseSelectAdapter(Context context,AppConfig appConfig,ArrayList<Course> list) {
		// TODO Auto-generated constructor stub
		this.mInflater = LayoutInflater.from(context);
		this.courseList = list;
		this.appConfig = appConfig;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(courseList == null)
		return 0;
		return courseList.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(courseList == null)
		return null;
		return courseList.get(position);
	}
	public long getItemId(int position) { return position;};
	public android.view.View getView(int position, android.view.View v, android.view.ViewGroup parent) {
		ViewHolder holder = null;
		if(v == null)
		{
			holder = new ViewHolder();
			v = mInflater.inflate(R.layout.item_choose_course_list, null);
			holder.box = (CheckBox) v.findViewById(R.id.checkBox);
			v.setTag(holder);
		} else
			holder = (ViewHolder) v.getTag();
		String ids = appConfig.get(AppConfig.CONF_SELECTED_COURSEID);
		final Course c = courseList.get(position);
		if(ids==null||ids.contains(c.getCourseId()))
		{
			holder.box.setChecked(true);
		}else
		{
			holder.box.setChecked(false);
		}
		holder.box.setText(c.getCourseName());
		holder.box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				String ids = appConfig.get(AppConfig.CONF_SELECTED_COURSEID);
				String cid = c.getCourseId()+",";
				if(isChecked) //选中了
				{
					if(ids == null)
					{
						appConfig.set(AppConfig.CONF_SELECTED_COURSEID, cid);
						return;
					}
					if(ids.contains(cid))
					{
						return;
					}
					appConfig.set(AppConfig.CONF_SELECTED_COURSEID, ids+cid);
				}else
				{
					if(ids != null)
						ids = ids.substring(0, ids.indexOf(cid))+ids.substring(ids.indexOf(cid)+cid.length(), ids.length());
					appConfig.set(AppConfig.CONF_SELECTED_COURSEID, ids);
				}
			}
		});
		return v;
	};
	static class ViewHolder 
	{
		CheckBox box;
	}
}
