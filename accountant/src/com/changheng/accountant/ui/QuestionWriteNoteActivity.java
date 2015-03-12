package com.changheng.accountant.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.entity.ExamNote;

public class QuestionWriteNoteActivity extends BaseActivity implements OnClickListener{
	private EditText editNoteEditText;
	private TextView editSizeText;
	private ExamNote note;
	private String qid,username,paperId,classId;
	private final static int maxLength = 1000;
	private PaperDao dao;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_question_doexam_notebook);
		this.editNoteEditText = (EditText) this
				.findViewById(R.id.editNoteEditText);
		this.editSizeText = (TextView) this
				.findViewById(R.id.notebook_editSizeText);
		this.dao = new PaperDao(this);
		Intent mIntent = this.getIntent();
		this.qid = mIntent.getStringExtra("qid");

		////恢复登录的状态，
		((AppContext) getApplication()).recoverLoginStatus();
		
		this.username = ((AppContext)getApplication()).getUsername();
		this.paperId = mIntent.getStringExtra("paperid");
		this.classId = mIntent.getStringExtra("classid");
		String text = this.dao.findNoteContent(qid,username);
		if(text!=null)
			this.editNoteEditText.setText(text);
		this.editNoteEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				QuestionWriteNoteActivity.this.editSizeText.setText("已输入: "
						+ s.length() + "/" + maxLength);
			}
		});
		//设置最大长度
		this.editNoteEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
		//绑定事件
		this.findViewById(R.id.returnbtn).setOnClickListener(this);
		this.findViewById(R.id.exam_notebook_btn).setOnClickListener(this);
		this.findViewById(R.id.submitBtn).setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.returnbtn:
			this.finish();
			return;
		case R.id.exam_notebook_btn:
			submit();
			return;
		case R.id.submitBtn:
			submit();
			return;
		}
	}
	private void submit()
	{
		String content = this.editNoteEditText.getText().toString();
		if(content.trim().length()==0)
		{
			Toast.makeText(this, "请输入笔记内容", Toast.LENGTH_SHORT).show();
			return;
		}
		String addTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA).format(new Date());
		note = new ExamNote(qid,addTime,content,username,paperId,classId);
		dao.insertNote(note);
		Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
		this.finish();
	}
	@Override
	protected void onPause() {
		super.onPause();
//		MobclickAgent.onPause(this);
	};
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		MobclickAgent.onResume(this);
		
	}
}
