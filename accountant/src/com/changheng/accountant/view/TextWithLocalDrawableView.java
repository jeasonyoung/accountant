package com.changheng.accountant.view;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.AttributeSet;
import android.widget.TextView;

import com.changheng.accountant.AppException;
import com.changheng.accountant.R;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.FileUtils;
import com.changheng.accountant.util.ImageUtils;

public class TextWithLocalDrawableView extends TextView {
	private Context a;
	private ImageGetter imageGetter = new ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			// TODO Auto-generated method stub
			Drawable localDrawable = null;
			try {
				int i = getResources().getIdentifier(source, "drawable",
						a.getPackageName());
				localDrawable = getResources().getDrawable(i);
//				localDrawable.setBounds(0, 0,
//						localDrawable.getIntrinsicWidth(),
//						localDrawable.getIntrinsicHeight());
			} catch (Exception e) {
				Bitmap bmp = null;
				String filename = FileUtils.getFileName(source);
				String filepath = a.getFilesDir() + File.separator + filename;
				File file = new File(filepath);
				if (file.exists()) {
//					return Drawable.createFromPath(filepath);
					bmp = ImageUtils.getBitmap(a, filename);
				}
				if (bmp == null) {
					try{
						bmp = ApiClient.getNetBitmap(source);
						if (bmp != null) {
							try {
								// 写图片缓存
							ImageUtils.saveImage(a, filename, bmp);
							} catch (IOException e1) {
									e1.printStackTrace();
							}
						}
						localDrawable = new BitmapDrawable(bmp);
						localDrawable.setBounds(0, 0,
								localDrawable.getIntrinsicWidth(),
								localDrawable.getIntrinsicHeight());
					}catch(AppException e2)
					{
						e2.printStackTrace();
						localDrawable = getResources().getDrawable(R.drawable.topic_img_empty);
						localDrawable.setBounds(0, 0,
								localDrawable.getIntrinsicWidth(),
								localDrawable.getIntrinsicHeight());
					// 缩放图片
					// bmp = ImageUtils.reDrawBitMap(ImageZoomActivity.this,
					// bmp);
					}
				}else
				{
					localDrawable = new BitmapDrawable(bmp);
					localDrawable.setBounds(0, 0,
							localDrawable.getIntrinsicWidth(),
							localDrawable.getIntrinsicHeight());
				}
			}
			return localDrawable;
		}
	};
	public TextWithLocalDrawableView(Context context) {
		super(context);
		this.a = context;
	}

	public TextWithLocalDrawableView(Context paramContext,
			AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		this.a = paramContext;
	}

	public TextWithLocalDrawableView(Context paramContext,
			AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		this.a = paramContext;
	}

	public void setEmojiText(String paramString) {
		setText(Html.fromHtml(paramString, this.imageGetter, null));
	}
}
