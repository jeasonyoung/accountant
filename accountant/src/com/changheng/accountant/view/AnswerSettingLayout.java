package com.changheng.accountant.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.changheng.accountant.R;

public class AnswerSettingLayout extends LinearLayout {
	private SeekBar light_seekbar;
	private FontSizeChangeListerner onFontSizeChangeListener;
	private ItemChangeListerner onItemChangeListener;
	private LightChangeListerner onLightChangeListerner;
	private TextView paper_num;
	private SharedPreferences preferences;
	private TextView sc_full_screen;
	private View station;
	private ImageView title_image;
	private TextView title_info;
	private TextView vibrate_mode;
	private View view;
	public TextView yj_mode;
	private RadioButton btn_big;
	private RadioButton btn_small;
	private Context context;
	private float fonsize = 16.0f;
	private ImageView hasnew;

	public AnswerSettingLayout(Context paramContext) {
		super(paramContext);
	}

	public AnswerSettingLayout(Context paramContext,
			AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		this.context = paramContext;
		this.view = LayoutInflater.from(paramContext).inflate(
				R.layout.answer_setting_dialog, this, false);
		init();
	}

	public void init() {
		this.preferences = this.context.getSharedPreferences("wdkaoshi", 0);
		this.fonsize = this.preferences.getFloat("fontsize", 16);
		this.light_seekbar = ((SeekBar) this.view
				.findViewById(R.id.light_seekbar));
		this.btn_small = ((RadioButton) this.view.findViewById(R.id.btn_small));
		this.btn_big = ((RadioButton) this.view.findViewById(R.id.btn_big));
		this.sc_full_screen = ((TextView) this.view
				.findViewById(R.id.sc_full_screen));
		int isFullScreen = this.preferences.getInt("isFullScreen", 0);
		if(isFullScreen==0){
			this.sc_full_screen.setTag(0);
		}
		else
		{
			this.sc_full_screen.setTag(1);
			sc_full_screen.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.full_screen_hov), null, null);
			sc_full_screen.setText("取消全屏");
		}
		this.vibrate_mode = ((TextView) this.view
				.findViewById(R.id.vibrate_mode));
		int isVibrate =  this.preferences.getInt("isVibrate", 0);
		if(isVibrate == 0)
		{
			vibrate_mode.setTag(0);
		}else
		{
			this.vibrate_mode.setTag(1);
			vibrate_mode.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.vibrate_on), null, null);
			vibrate_mode.setText("取消震动");
		}
		this.yj_mode = ((TextView) this.view.findViewById(R.id.yj_mode));
		this.light_seekbar.setMax(255);
		int i = Settings.System.getInt(this.context.getContentResolver(),
				"screen_brightness", 255);
		int j = this.preferences.getInt("nowbrightness", 0);
		if (j == 0)
			this.light_seekbar.setProgress(i);
		else
			this.light_seekbar.setProgress(j);
		this.light_seekbar
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onProgressChanged(
							SeekBar paramAnonymousSeekBar,
							int paramAnonymousInt, boolean paramAnonymousBoolean) {
					}

					public void onStartTrackingTouch(
							SeekBar paramAnonymousSeekBar) {
					}

					public void onStopTrackingTouch(
							SeekBar paramAnonymousSeekBar) {
						int i = paramAnonymousSeekBar.getProgress();
						if (i < 10)
							i = 10;
						if (AnswerSettingLayout.this.onLightChangeListerner != null)
							AnswerSettingLayout.this.onLightChangeListerner
									.onLightChangeClick(i);
						SharedPreferences.Editor localEditor = AnswerSettingLayout.this.preferences
								.edit();
						localEditor.putInt("nowbrightness", i);
						localEditor.commit();
					}
				});
		this.btn_small.setOnClickListener(this.l);
		this.btn_big.setOnClickListener(this.l);
		this.sc_full_screen.setOnClickListener(this.l);
		this.vibrate_mode.setOnClickListener(this.l);
		addView(this.view);
	}
	public FontSizeChangeListerner getOnFontSizeChangeListener() {
		return onFontSizeChangeListener;
	}

	public void setOnFontSizeChangeListener(
			FontSizeChangeListerner onFontSizeChangeListener) {
		this.onFontSizeChangeListener = onFontSizeChangeListener;
	}

	public ItemChangeListerner getOnItemChangeListener() {
		return onItemChangeListener;
	}

	public void setOnItemChangeListener(ItemChangeListerner onItemChangeListener) {
		this.onItemChangeListener = onItemChangeListener;
	}

	public LightChangeListerner getOnLightChangeListerner() {
		return onLightChangeListerner;
	}

	public void setOnLightChangeListerner(
			LightChangeListerner onLightChangeListerner) {
		this.onLightChangeListerner = onLightChangeListerner;
	}
	private OnClickListener l = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			SharedPreferences.Editor localEditor = AnswerSettingLayout.this.preferences
					.edit();
			switch(v.getId())
			{
			case R.id.btn_big:
				if(fonsize < 22)	//最大22
				{
					fonsize++ ;
					AnswerSettingLayout.this.onFontSizeChangeListener.changeSize(fonsize);
				}
				localEditor.putFloat("fontsize", fonsize);
				break;
			case R.id.btn_small:
				if(fonsize > 12)	//最小12
				{
					fonsize-- ;
					AnswerSettingLayout.this.onFontSizeChangeListener.changeSize(fonsize);
				}
				localEditor.putFloat("fontsize", fonsize);
				break;
			case R.id.sc_full_screen:
				onItemChangeListener.onItemClick(v.getId());
				if(v.getTag().equals(0))
				{
					sc_full_screen.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.full_screen_hov), null, null);
					sc_full_screen.setText("取消全屏");
					sc_full_screen.setTag(1);
					localEditor.putInt("isFullScreen", 1);
				}else
				{
					sc_full_screen.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.full_screen_no), null, null);
					sc_full_screen.setText("全屏显示");
					sc_full_screen.setTag(0);
					localEditor.putInt("isFullScreen", 0);
				}
				break;
			case R.id.vibrate_mode:
				if(v.getTag().equals(0))
				{
					localEditor.putInt("isVibrate", 1);
					vibrate_mode.setTag(1);
					vibrate_mode.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.vibrate_on), null, null);
					vibrate_mode.setText("取消震动");
				}else
				{
					localEditor.putInt("isVibrate", 0);
					vibrate_mode.setTag(0);
					vibrate_mode.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.vibrate_no), null, null);
					vibrate_mode.setText("答错震动");
				}
			}
			localEditor.commit();
		}
	};
	public static abstract interface FontSizeChangeListerner {
		public abstract void changeSize(float size);
	}

	public static abstract interface ItemChangeListerner {
		public abstract void onItemClick(int paramInt);
	}

	public static abstract interface ItemClickListerner {
		public abstract void onItemClick();
	}

	public static abstract interface LightChangeListerner {
		public abstract void onLightChangeClick(int paramInt);
	}
}

