package com.changheng.accountant.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.entity.ForumPost;
import com.changheng.accountant.ui.UIHelper;
import com.changheng.accountant.util.BitmapManager;
import com.changheng.accountant.util.StringUtils;
import com.changheng.accountant.view.TextWithLocalDrawableView;

public class ForumListAdapter extends BaseAdapter{
	private LayoutInflater mLayoutInflater;
	private ArrayList<ForumPost> list;
	private BitmapManager 				bmpManager;
	public ForumListAdapter(Context context,ArrayList<ForumPost> list,BitmapManager bmpManager) {
		// TODO Auto-generated constructor stub
		this.mLayoutInflater = LayoutInflater.from(context);
		this.list = list;
		if(bmpManager == null)
			this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(context.getResources(), R.drawable.img_head_default));
		else
			this.bmpManager = bmpManager;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(list!=null)
			return list.size();
		return 0;
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(list!=null)
		{
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
			v = mLayoutInflater.inflate(R.layout.forum_list_item, null);
			holder = new ViewHolder();
			holder.headView = (ImageView) v.findViewById(R.id.iv_head);
			holder.title = (TextWithLocalDrawableView) v.findViewById(R.id.txt_title);
			holder.author = (TextView) v.findViewById(R.id.txt_author_name);
			holder.time = (TextView) v.findViewById(R.id.txt_last_response_ts);
			holder.pvCount = (TextView) v.findViewById( R.id.txt_pv_count);
			holder.responseCount = (TextView) v.findViewById(R.id.txt_response_count);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		ForumPost post = list.get(position);
		String faceURL = post.getFace();
		if(StringUtils.isEmpty(faceURL)){
			holder.headView.setImageResource(R.drawable.img_head_default);
		}else if(faceURL.endsWith("portrait.gif"))
		{
			holder.headView.setImageResource(R.drawable.img_head_default);
		}else{
			bmpManager.loadBitmap(faceURL, holder.headView);
		}
		holder.headView.setOnClickListener(faceClickListener);
		holder.headView.setTag(post);
		holder.title.setEmojiText(post.getTitle());
		holder.author.setText(post.getAuthor());
		holder.time.setText(post.getPubDate());
		holder.pvCount.setText(String.valueOf(post.getViewCount()));
		holder.responseCount.setText(String.valueOf(post.getAnswerCount()));
		return v;
	}
	static class ViewHolder
	{
		ImageView headView; //头像
		TextWithLocalDrawableView title;//标题
		TextView author;//作者
		TextView time; //时间
		TextView pvCount;//查看次数
		TextView responseCount;//回复数
	}
	private View.OnClickListener faceClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			ForumPost tweet = (ForumPost)v.getTag();
			UIHelper.showUserCenter(v.getContext(), tweet.getFace(),tweet.getAuthor(), tweet.getArea());
		}
	};
}
