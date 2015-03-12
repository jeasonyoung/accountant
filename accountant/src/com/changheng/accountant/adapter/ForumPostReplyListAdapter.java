package com.changheng.accountant.adapter;

import java.util.ArrayList;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.R;
import com.changheng.accountant.entity.ForumPostReply;
import com.changheng.accountant.ui.ForumPostDetailActivity;
import com.changheng.accountant.ui.ImageZoomActivity;
import com.changheng.accountant.ui.OnWebViewImageListener;
import com.changheng.accountant.ui.UIHelper;
import com.changheng.accountant.util.BitmapManager;
import com.changheng.accountant.util.StringUtils;

@SuppressLint("JavascriptInterface")
public class ForumPostReplyListAdapter extends BaseAdapter {
	private ArrayList<ForumPostReply> list;
	private Context context;
	private BitmapManager bmpManager;
	public ForumPostReplyListAdapter(Context context,ArrayList<ForumPostReply> list,BitmapManager bmpManager) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.list = list;
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
			return list.get(position);
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
			v = LayoutInflater.from(context).inflate(R.layout.forum_post_reply_item, null);
			holder = new ViewHolder();
			holder.headView = (ImageView) v.findViewById(R.id.topic_user_avator);
			holder.author = (TextView) v.findViewById(R.id.topic_user_name);
			holder.floor = (TextView) v.findViewById(R.id.topic_user_floor);
			holder.preContext = (TextView) v.findViewById(R.id.topic_lou_textView1);
			holder.contentTv =(TextView) v.findViewById(R.id.reply_content_tv);
			holder.contentWebView = (WebView) v.findViewById(R.id.reply_content_webview);
			initWebView(holder.contentWebView);
			holder.location = (TextView) v.findViewById(R.id.topic_user_address);
			holder.time = (TextView) v.findViewById(R.id.topic_user_time);
			holder.replyBtn = (Button) v.findViewById(R.id.topic_user_reply);
			holder.btnLisener = new BtnListener(position);
			holder.replyBtn.setOnClickListener(holder.btnLisener);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		ForumPostReply reply = list.get(position);
		String faceURL = reply.getFace();
		if(StringUtils.isEmpty(faceURL)){
			holder.headView.setImageResource(R.drawable.img_head_default);
		}else if(faceURL.endsWith("portrait.gif"))
		{
			holder.headView.setImageResource(R.drawable.img_head_default);
		}else{
			bmpManager.loadBitmap(faceURL, holder.headView);
		}
		((BtnListener)holder.btnLisener).setPosition(position);
		holder.headView.setTag(reply);
		holder.headView.setOnClickListener(faceClickListener);
		holder.author.setText(reply.getReplyAuthor());
		holder.floor.setText(reply.getFloor()+"楼");
		if(reply.getReplyFloor()==0)
		{
			holder.preContext.setVisibility(View.GONE);
		}else
		{
			holder.preContext.setText("回复"+reply.getReplyFloor()+"楼 "+reply.getReplyTo()+" 的内容");
		}
		String content = reply.getContent();
		if(isContainImg(content)) //包含图
		{
			holder.contentTv.setVisibility(View.GONE);
			holder.contentWebView.setVisibility(View.VISIBLE);
			holder.contentWebView.loadDataWithBaseURL(null, content, "text/html",
					"utf-8", null);
		}else
		{
			holder.contentTv.setVisibility(View.VISIBLE);
			holder.contentWebView.setVisibility(View.GONE);
			holder.contentTv.setText(content);
		}
		holder.location.setText(reply.getLocation());
		holder.time.setText(reply.getReplyDate());
		return v;
	}
	private boolean isContainImg(String content)
	{
		//不包含图片直接就用TextView了
		boolean bool1 = Pattern.compile("[\\s\\S]*(<img[^>]+src=\")(\\S+)\"[\\s\\S]*(/?>)").matcher(content)
				.matches();
		boolean bool2 = Pattern.compile("[\\s\\S]*(<IMG[^>]+src=\")(\\S+)\"[\\s\\S]*(/?>)").matcher(content)
				.matches();
		return bool1||bool2;
		
	}
	private void initWebView(WebView contentWebView)
	{
		contentWebView.getSettings().setJavaScriptEnabled(true);
		//设置显示图片适应屏幕
		contentWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		contentWebView.setWebViewClient(new SampleWebViewClient());
//		contentWebView.loadUrl("http://www.babytree.com/community/topic_mobile.php?id=42052");//
//		contentWebView.loadUrl("file:///android_asset/other/about.html");
		contentWebView.addJavascriptInterface(new OnWebViewImageListener() {
			@Override
			public void onImageClick(String bigImageUrl) {
//				if (bigImageUrl != null)
//					UIHelper.showImageZoomDialog(cxt, bigImageUrl);
				System.out.println("iamgeUrl = "+bigImageUrl);
				Toast.makeText(context, bigImageUrl, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(context,ImageZoomActivity.class);
				intent.putExtra("imgUrl", bigImageUrl);
				context.startActivity(intent);
			}
		}, "mWebViewImageListener");
	}
	private class SampleWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.contains("file:///") == true) {
				view.loadUrl(url);
				return true;
			} else {
				Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				context.startActivity(in);
				return true;
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
//			loading.setVisibility(View.GONE);
			super.onPageFinished(view, url);
		}
	}
	static class ViewHolder
	{
		ImageView headView;
		TextView author;	//回复人
		TextView floor;	 //第几楼
		TextView preContext; //回复xx楼 xxxx 
		TextView contentTv;
		WebView contentWebView;
		TextView location;
		TextView time;
		Button replyBtn;
		OnClickListener btnLisener;
	}
	private class BtnListener implements OnClickListener
	{
		private int position;
		public BtnListener(int position) {
			// TODO Auto-generated constructor stub
			this.position = position;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ForumPostReply r = list.get(position);
			((ForumPostDetailActivity)context).showKeyboard("回复"+(r.getFloor())+"楼 "+r.getReplyAuthor()
									,r.getId(),r.getReplyAuthor());
		}
		public void setPosition(int position)
		{
			this.position = position;
		}
	}
	private View.OnClickListener faceClickListener = new View.OnClickListener(){
		public void onClick(View v) {
			ForumPostReply reply = (ForumPostReply)v.getTag();
			UIHelper.showUserCenter(v.getContext(), reply.getFace(), reply.getReplyAuthor(),reply.getLocation());
		}
	};
}
