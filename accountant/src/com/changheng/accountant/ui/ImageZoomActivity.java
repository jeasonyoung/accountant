package com.changheng.accountant.ui;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.changheng.accountant.AppException;
import com.changheng.accountant.R;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.FileUtils;
import com.changheng.accountant.util.ImageUtils;
import com.changheng.accountant.util.StringUtils;
import com.polites.android.GestureImageView;

public class ImageZoomActivity extends BaseActivity implements OnClickListener{
	private Button saveImgBtn;
	private TextView title;
	private GestureImageView iv;
	private RelativeLayout loading;
	private LinearLayout reloadLayout;
	private String imgURL;
	private Handler handler;
	private Bitmap bitmap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_imagezoom);
		initViews();
		imgURL = getIntent().getStringExtra("imgUrl");
		initData();
	}
	private void initViews()
	{
		iv = (GestureImageView) this.findViewById(R.id.image);
		loading = (RelativeLayout) this.findViewById(R.id.loading);
		title = (TextView) this.findViewById(R.id.title);
		title.setText("图片预览");
		saveImgBtn = (Button) this.findViewById(R.id.btn_save); //暂时不可见
		reloadLayout = (LinearLayout) this.findViewById(R.id.reload);
		this.findViewById(R.id.btn_goback).setOnClickListener(this);
		saveImgBtn.setOnClickListener(this);
		this.findViewById(R.id.btn_reload).setOnClickListener(this);
		handler = new GetImageHandler(this);
	}
	private void initData()
	{
		//开线程去获取图片,并缓存
		new Thread() {
			public void run() {
				Message msg = new Message();
				Bitmap bmp = null;
				String filename = FileUtils.getFileName(imgURL);
				try {
					// 读取本地图片
					if (imgURL.endsWith("portrait.gif")
							|| StringUtils.isEmpty(imgURL)) {
						bmp = BitmapFactory
								.decodeResource(iv.getResources(),
										R.drawable.photo_no_photo);
					}
					if (bmp == null) {
						// 是否有缓存图片
						// Environment.getExternalStorageDirectory();返回/sdcard
						String filepath = getFilesDir() + File.separator
								+ filename;
						File file = new File(filepath);
						if (file.exists()) {
							bmp = ImageUtils.getBitmap(iv.getContext(),
									filename);
						}
					}
					if (bmp == null) {
						bmp = ApiClient.getNetBitmap(imgURL);
						if (bmp != null) {
							try {
								// 写图片缓存
								ImageUtils.saveImage(iv.getContext(),
										filename, bmp);
							} catch (IOException e) {
								e.printStackTrace();
							}
							// 缩放图片
//							bmp = ImageUtils.reDrawBitMap(ImageZoomActivity.this,
//									bmp);
						}
					}
					msg.what = 1;
					msg.obj = bmp;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_goback:
			this.finish();
			break;
		case R.id.btn_save:
			saveImg();
			break;
		case R.id.btn_reload:
			initData();
			break;
		}
	}
	private void saveImg()
	{
		//检测是否挂载SD卡
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			UIHelper.ToastMessage(this, "SD卡不存在");
			return;
		}
		try
		{
			String saveImagePath = Environment.getExternalStorageDirectory().getPath()+File.separator+"CHAccountant"+File.separator+"Image";
			ImageUtils.saveImageToSD(this,
					saveImagePath + ImageUtils.getTempFileName()
							+ ".jpg", bitmap, 100);
			UIHelper.ToastMessage(this, "保存成功");
		} catch (IOException e) {
			e.printStackTrace();
			UIHelper.ToastMessage(this, "保存失败");
		}
		
	}
	static class GetImageHandler extends Handler
	{
		private WeakReference<ImageZoomActivity> weak;
		public GetImageHandler(ImageZoomActivity context)
		{
			weak = new WeakReference<ImageZoomActivity>(context);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			ImageZoomActivity activity = weak.get();
			switch(msg.what)
			{
			case 1:
				activity.loading.setVisibility(View.GONE);
				activity.iv.setImageBitmap((Bitmap) msg.obj);
				activity.bitmap = (Bitmap) msg.obj;
				activity.saveImgBtn.setVisibility(View.VISIBLE);
				break;
			case -1:
				activity.loading.setVisibility(View.GONE);
				activity.reloadLayout.setVisibility(View.VISIBLE);
				UIHelper.ToastMessage(activity, "读取图片失败");
				break;
			}
		}
	}
}
