package com.changheng.accountant.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.entity.ExamQuestion;
import com.changheng.accountant.entity.ExamRule;
import com.changheng.accountant.entity.Knowledge;
import com.changheng.accountant.ui.QuestionDoExamActivity1;
import com.changheng.accountant.util.URLs;
import com.google.gson.Gson;

public class KnowlegdgeListAdapter extends BaseAdapter{
	private ArrayList<Knowledge> list;
	private Context mContext;
	private ProgressDialog proDialog;
	public KnowlegdgeListAdapter(Context context,ArrayList<Knowledge> list) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.list = list;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(list!=null)
		{
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
			v = LayoutInflater.from(mContext).inflate(R.layout.plan_knowledge_list_item, null);
			holder = new ViewHolder();
			holder.title = (TextView) v.findViewById(R.id.knowledge_title);
			holder.title.setOnClickListener(new TitleClickListener(holder));
			holder.btn = (Button) v.findViewById(R.id.btn_practice);
			holder.content = (WebView) v.findViewById(R.id.knowlege_content_webview);
			holder.star = (RatingBar) v.findViewById(R.id.star_bar);
			//(int)mContext.getResources().getDimension(R.dimen.text_medium_size)
			float size = mContext.getResources().getDimensionPixelSize(R.dimen.text_medium_size);
			int fontsize = px2sp(mContext, size);
			holder.content.getSettings().setDefaultFontSize(fontsize);
			v.setTag(holder);
		}else
			holder = (ViewHolder) v.getTag();
		Knowledge k = list.get(position);
		holder.title.setText(k.getTitle());
		final String knowledgeId = k.getKnowledgeId();
		holder.btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				practice(knowledgeId);
			}
		});
		holder.content.loadDataWithBaseURL(null, k.getFullContent(), "text/html","utf-8", null);
		if(k.getQuestionCount() == -1) holder.star.setVisibility(View.GONE);
		else {
			int stars = starRating(k.getQuestionCount()); 
			holder.star.setNumStars(stars);
			holder.star.setRating(stars);
		}
		return v;
	}
	static class ViewHolder
	{
		TextView title;
		Button btn;
		WebView content;
//		TextView content;
		RatingBar star;
	}
	private class TitleClickListener implements OnClickListener{
		ViewHolder holder;
		
		public TitleClickListener(ViewHolder holder) {
			super();
			this.holder = holder;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(holder.content.getVisibility()==View.GONE)
			{
				holder.content.setVisibility(View.VISIBLE);
			}else
			{
				holder.content.setVisibility(View.GONE);
			}
		}
	};
	private void practice(final String knowledgeId)
	{
		AppContext appContext = (AppContext) ((Activity)mContext).getApplication();
		if(appContext.isTimeOver())
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle("提示");
			builder.setMessage("抱歉，使用的期限已过，您可以去官网购买。");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
							Uri uri = Uri.parse(URLs.URL_BUY);
							Intent it = new Intent(Intent.ACTION_VIEW, uri);
							mContext.startActivity(it);
						}
					});
			builder.setCancelable(true);
			builder.create().show();
			return;
		}
		final ArrayList<ExamQuestion> questionList = new ArrayList<ExamQuestion>();
		final ArrayList<ExamRule> ruleList = new ArrayList<ExamRule>();
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if(proDialog!=null &&proDialog.isShowing())
				{
					proDialog.dismiss();
				}
				switch(msg.what)
				{
				case 1:
					if(questionList==null||questionList.size()==0)
					{
						Toast.makeText(mContext, "暂时没有题目", Toast.LENGTH_SHORT).show();
						return;
					}
					Gson gson = new Gson();
					Intent intent = new Intent(mContext, QuestionDoExamActivity1.class);
//					intent.putExtra("paperId",qpi.zuheid);
					intent.putExtra("knowledgeId", knowledgeId);
					intent.putExtra("action", "practice");
					intent.putExtra("ruleListJson", gson.toJson(ruleList));
					intent.putExtra("cursor", 0);
					mContext.startActivity(intent);
					break;
				case -1:
					Toast.makeText(mContext, "暂时没有题目", Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(mContext, "加载出错，稍后再试", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
		if(proDialog == null)
		{
			proDialog = ProgressDialog.show(mContext, null, "加载中...", true, true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		new Thread(){
			public void run() {
				try{
					PaperDao dao = new PaperDao(mContext);
					questionList.addAll(dao.findQuestionOfKnowledge(null, knowledgeId));
					ruleList.addAll(getRuleList(questionList));
					handler.sendEmptyMessage(1);
				}catch(NullPointerException e)
				{
					e.printStackTrace();
					handler.sendEmptyMessage(-1);
				}catch(Exception e)
				{
					handler.sendEmptyMessage(2);
				}
			};
			private List<ExamRule> getRuleList(List<ExamQuestion> questionList) {
				if(questionList==null||questionList.size()==0)
				{
					return null;
				}
				List<ExamRule> list = new ArrayList<ExamRule>();
				StringBuffer buf = new StringBuffer();
				//按题型的1,2,3,4来分
				for (int i = 1; i < 5; i++) {
					for (ExamQuestion q : questionList) {

						if (String.valueOf(i).equals(q.getQType())) {
							buf.append(q.getQid()).append(",");
						}
					}
					if(buf.length() > 0){
						ExamRule r = new ExamRule();
						r.setRuleTitle(getTitleStr(i));
						r.setOrderInPaper(i);
						if (buf.length() > 0) {
							r.setContainQids(buf.substring(0, buf.lastIndexOf(",")));
							buf.delete(0, buf.length());
						}
						list.add(r);
					}
				}
				return list;
			}
			private String getTitleStr(int i) {
				return i == 1 ? "单选题" : i == 2 ? "多选题" : i == 3 ? "不定项"
						: i == 4 ? "判断题" : "综合题";
			}
		}.start();
	}
	
	private int starRating(int questionCount)
	{
		if(questionCount<=10)
			return 1;
		else if(questionCount<30)
			return 2;
		else if(questionCount<50)
			return 3;
		else if(questionCount<80)
			return 4;
		else
			return 5;
	}
	private int px2sp(Context context, float pxValue) {  
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (pxValue / fontScale + 0.5f);  
    } 
}
