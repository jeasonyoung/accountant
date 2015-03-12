package com.changheng.accountant.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.entity.Chapter;
import com.changheng.accountant.entity.Course;
import com.changheng.accountant.ui.ChapterDetailActivity;

public class CourseExpandableAdapter extends BaseExpandableListAdapter {
	private Context context;
	private ArrayList<Course> groups;
	private ArrayList<ArrayList<Chapter>> children;
	private float x, ux;
	private Button curBtn;

	public CourseExpandableAdapter(Context context, ArrayList<Course> group,
			ArrayList<ArrayList<Chapter>> child) {
		this.children = child;
		this.context = context;
		this.groups = group;
	}

	// 获得指定组中的指定索引的子选项数据
	public Object getChild(int groupPosition, int childPosition) {
		try {
			return children.get(groupPosition).get(childPosition);
		} catch (Exception e) {
			return null;
		}
	}

	// 获得指定子项的ID
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	// 获得指定子项的view组件
	public View getChildView(final int groupPosition,final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_choosecourse_list,
					null);
			holder = new ViewHolder();
			holder.txt = (TextView) convertView.findViewById(R.id.list_title);
			holder.icon = (ImageView) convertView
					.findViewById(R.id.home_list_icon);
			holder.exerBtn = (Button) convertView.findViewById(R.id.exercise);
			holder.icon.setImageResource(R.drawable.icon_chapter1);	//章的图标
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT); // , 1是可选写的
			lp.setMargins(15, 0, 0, 0);
			holder.icon.setLayoutParams(lp);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.txt.setText(getChild(groupPosition, childPosition).toString());
		// 为每一个view项设置触控监听
		convertView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				final ViewHolder holder = (ViewHolder) v.getTag();
				// 当按下时处理
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// 设置背景为选中状态
//					v.setBackgroundResource(R.drawable.mm_listitem_pressed);
					v.setBackgroundColor(v.getResources().getColor(R.color.lightbule));
					// 获取按下时的x轴坐标
					x = event.getX();
					// 判断之前是否出现了删除按钮如果存在就隐藏
//					if (curBtn != null && curBtn.getVisibility()==View.VISIBLE) {
//						curBtn.setVisibility(View.GONE);
//					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {// 松开处理
					// 设置背景为未选中正常状态
//					v.setBackgroundResource(R.drawable.mm_listitem_simple);
					v.setBackgroundColor(v.getResources().getColor(R.color.lightbule2));
					// 获取松开时的x坐标
					ux = event.getX();
					// 判断当前项中按钮控件不为空时
					if (curBtn == null || curBtn.getVisibility()==View.GONE) {
						// 按下和松开绝对值差当大于20时显示删除按钮，否则不显示
						if (Math.abs(x - ux) > 20) {
//							holder.exerBtn.setVisibility(View.VISIBLE);	【到时候需要再进行修改】
							curBtn = holder.exerBtn;
						}else
						{
							gotoChapterDetail(groupPosition, childPosition);
						}
					}else
					{
//						gotoChapterDetail(groupPosition, childPosition);
						curBtn.setVisibility(View.GONE);
					}
				} 
				else if (event.getAction() == MotionEvent.ACTION_MOVE) {// 当滑动时背景为选中状态
//					v.setBackgroundResource(R.drawable.mm_listitem_pressed);
					v.setBackgroundColor(v.getResources().getColor(R.color.lightbule));
				} else {// 其他模式
//					// 设置背景为未选中正常状态
//					v.setBackgroundResource(R.drawable.mm_listitem_simple);
					v.setBackgroundColor(v.getResources().getColor(R.color.lightbule2));
				}
				return true;
			}
		});
		// 为删除按钮添加监听事件，实现点击删除按钮时删除该项
		holder.exerBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (curBtn != null)
					curBtn.setVisibility(View.GONE);
					System.out.println("点击练习按钮");
			}
		});
		return convertView;
	}

	// 取得指定组中所有子项的个数
	public int getChildrenCount(int groupPosition) {
		try {
			return children.get(groupPosition).size();
		} catch (Exception e) {
			return 0;
		}
	}

	// 取得指定组的数据
	public Object getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	// 取得指定组的个数
	public int getGroupCount() {
		return groups.size();
	}

	// 取得指定索引的ID
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	// 取得指定组的View组件
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.item_choosecourse_list, null);
		TextView txt = (TextView) convertView.findViewById(R.id.list_title);
		txt.setText(groups.get(groupPosition).getCourseName());
//		txt.setTextSize(convertView.getResources().getDimension(R.dimen.text_medium_size));
		ImageView arrow = (ImageView) convertView.findViewById(R.id.arrow);
		if (isExpanded) {
			arrow.setImageResource(R.drawable.ic_arrow_down);
		} else {
			arrow.setImageResource(R.drawable.ic_arrow);
		}
		return convertView;
	}

	// 如果返回true表示子项和组的ID始终表示一个固定的组件对象
	public boolean hasStableIds() {
		return true;
	}

	// 判断指定的子选择项是否被选择
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	private void gotoChapterDetail(int groupPosition,int childPosition)
	{
		Intent intent = new Intent(context,ChapterDetailActivity.class);
		intent.putExtra("pid", ((Chapter)getChild(groupPosition, childPosition)).getChapterId());
		intent.putExtra("classid", ((Course)getGroup(groupPosition)).getCourseId());
		intent.putExtra("chapterName", getChild(groupPosition, childPosition).toString());
		intent.putExtra("className", getGroup(groupPosition).toString());
		context.startActivity(intent);
	}
	static class ViewHolder {
		TextView txt;
		ImageView icon;
		Button exerBtn;
	}
}