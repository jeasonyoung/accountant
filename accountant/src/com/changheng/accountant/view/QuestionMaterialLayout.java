package com.changheng.accountant.view;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.changheng.accountant.R;

public class QuestionMaterialLayout extends RelativeLayout implements
		OnTouchListener {
	private Context context;
	private View view;
	private View iv;
	private WebView textView,testView;

	
	
	public int getTvHeight() {
		return tvHeight;
	}

	private View menu;
	private Handler mHandler;
	private int tvHeight;
	private boolean flag;
	/**
	 * 滚动显示和隐藏menu时，手指滑动需要达到的速度。
	 */
	public static final int SNAP_VELOCITY = 200;

	/**
	 * 屏幕高度值。
	 */
	private int screenHeight;

	/**
	 * menu最多可以滑动到的左边缘。值由menu布局的宽度来定，marginLeft到达此值之后，不能再减少。
	 */
	private int downEdge;

	/**
	 * menu最多可以滑动到的右边缘。值恒为0，即marginLeft到达0之后，不能增加。
	 */
	private int upEdge = -100;

	/**
	 * menu完全显示时，留给content的宽度值。
	 */
	private int menuPadding = 80;

	/**
	 * 主内容的布局。
	 */
	private View content;

	/**
	 * menu布局的参数，通过此参数来更改leftMargin的值。
	 */
	private RelativeLayout.LayoutParams menuParams;
	private RelativeLayout.LayoutParams contentParams;

	/**
	 * 记录手指按下时的纵坐标。
	 */
	private float yDown;

	/**
	 * 记录手指移动时的纵坐标。
	 */
	private float yMove;

	/**
	 * 记录手机抬起时的纵坐标。
	 */
	private float yUp;

	/**
	 * menu当前是显示还是隐藏。只有完全显示或隐藏menu时才会更改此值，滑动过程中此值无效。
	 */

	private int tempTopMargin;

	/**
	 * 用于计算手指滑动的速度。
	 */
	private VelocityTracker mVelocityTracker;

	public QuestionMaterialLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public QuestionMaterialLayout(Context paramContext,
			AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		this.context = paramContext;
		this.view = LayoutInflater.from(paramContext).inflate(
				R.layout.question_metrail, this, false);
		init();
	}

	private void init() {
		iv = view.findViewById(R.id.iamgeView);
		textView = (WebView) view.findViewById(R.id.content2);
		float size = context.getResources().getDimensionPixelSize(
				R.dimen.text_medium_size);
		int fontsize = px2sp(context, size);
		textView.getSettings().setDefaultFontSize(fontsize);
		textView.getSettings().setSupportZoom(false);
		WindowManager window = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		screenHeight = window.getDefaultDisplay().getHeight();
		mHandler = new Handler();
		menu = this;
		addView(this.view);
		this.view = null;
		System.gc();
	}

	public void initData(String text) {
		// System.out.println(text);
		textView.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);
//		testView = new WebView(context);
//		testView.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);
		Runnable showPopWindowRunnable = new Runnable() {
			@Override
			public void run() {
				// 如何根元素的width和height大于0说明activity已经初始化完毕
				if (textView != null && textView.getWidth() > 0
						&& textView.getContentHeight() > 0 ) {
					// 停止检测
					setData();
					mHandler.removeCallbacks(this);
				} else {
					// 如果activity没有初始化完毕则等待5毫秒再次检测
					mHandler.postDelayed(this, 5);
				}
			}
		};
		// 开始检测
		mHandler.post(showPopWindowRunnable);
	}

	private void setData() {
		tvHeight = (int) (textView.getContentHeight() * textView.getScale());
		System.out.println("文本高度：" + textView.getContentHeight()+",计算高度：");
		menuParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
		contentParams = (RelativeLayout.LayoutParams) textView.getLayoutParams();
		// 将menu的高度设置为屏幕高度减去menuPadding，减去标题栏高度
		if (tvHeight + 50 > screenHeight - menuPadding) {
			menuParams.height = screenHeight - menuPadding;
			contentParams.height = screenHeight - menuPadding - 50;
			contentParams.topMargin = 0;
		} else {
			if(tvHeight+50 < screenHeight/2)
			{
				menuParams.height = screenHeight/2 + 50;
				contentParams.height = screenHeight/2;
				tvHeight = screenHeight/2;
			}else{
				contentParams.height = tvHeight;
				menuParams.height = tvHeight + 50;
			}
			contentParams.topMargin = 0;
		}
		// 下边缘的值赋值为menu宽度的负数
		downEdge = -menuParams.height + 50;
		// menu的leftMargin设置为左边缘的值，这样初始化时menu就变为不可见
		textView.setLayoutParams(contentParams);
		if(flag)
		{
			menuParams.topMargin = downEdge;
			menu.setLayoutParams(menuParams);
		}else
		{
			flag = true;
		}
		iv.setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		createVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 手指按下时，记录按下时的横坐标
			yDown = event.getRawY();
			tempTopMargin = menuParams.topMargin;
			break;
		case MotionEvent.ACTION_MOVE:
			// 手指移动时，对比按下时的纵坐标，计算出移动的距离，来调整menu的topMargin值，从而显示和隐藏menu
			yMove = event.getRawY();
			int distanceY = (int) (yMove - yDown); // 总坐标移动的距离
			menuParams.topMargin = tempTopMargin + distanceY;
			contentParams.topMargin = Math.abs(menuParams.topMargin);
			contentParams.height = menuParams.height
					- Math.abs(menuParams.topMargin) - 50;
			if (menuParams.topMargin < downEdge) // 下边缘是个负数
			{
				menuParams.topMargin = downEdge;
				contentParams.height = tvHeight;
				contentParams.topMargin = 0;
			} else if (menuParams.topMargin > upEdge) {
				menuParams.topMargin = upEdge;
				contentParams.topMargin = 100;
				contentParams.height = menuParams.height - 100 - 50;
			}
			textView.setLayoutParams(contentParams);
			menu.setLayoutParams(menuParams);
			break;
		case MotionEvent.ACTION_UP:
			// 手指抬起时，进行判断当前手势的意图，从而决定是滚动到menu界面，还是滚动到content界面
			yUp = event.getRawY();
			int speed = getScrollVelocity(); // 滑动速度
			if (yUp - yDown > 0 && speed > SNAP_VELOCITY ) // 表示向下滑动
			{
				// 整个都显示
//				scrollToMenu();
//				contentParams.height = menuParams.height-50;
//				contentParams.topMargin = 0;
//				textView.setLayoutParams(contentParams);
			} else if (yUp - yDown < 0 && speed > SNAP_VELOCITY) {
				scrollToContent();
			}
			recycleVelocityTracker();
			break;
		}
		return true;
	}

	/**
	 * 将屏幕滚动到menu界面，滚动速度设定为30.
	 */
	private void scrollToMenu() {
		new ScrollTask().execute(30);
	}

	/**
	 * 将屏幕滚动到content界面，滚动速度设定为-30.
	 */
	private void scrollToContent() {
		new ScrollTask().execute(-30);
	}

	/**
	 * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
	 * 
	 * @param event
	 *            content界面的滑动事件
	 */
	private void createVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	/**
	 * 获取手指在content界面滑动的速度。
	 * 
	 * @return 滑动速度，以每秒钟移动了多少像素值为单位。
	 */
	private int getScrollVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getXVelocity();
		return Math.abs(velocity);
	}

	/**
	 * 回收VelocityTracker对象。
	 */
	private void recycleVelocityTracker() {
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}

	class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... speed) {
			int topMargin = menuParams.topMargin;
			// 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
			while (true) {
				topMargin = topMargin + speed[0];
				if (topMargin > upEdge) {
					topMargin = upEdge;
					break;
				}
				if (topMargin < downEdge) {
					topMargin = downEdge;
					break;
				}
				publishProgress(topMargin);
				// 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。
				sleep(20);
			}
			return topMargin;
		}

		@Override
		protected void onProgressUpdate(Integer... leftMargin) {
			menuParams.topMargin = leftMargin[0];
			menu.setLayoutParams(menuParams);
		}

		@Override
		protected void onPostExecute(Integer leftMargin) {
			menuParams.topMargin = leftMargin;
			menu.setLayoutParams(menuParams);
//			contentParams.height = menuParams.height
//					- Math.abs(menuParams.topMargin) - 50;
//			textView.setLayoutParams(contentParams);
		}
	}

	/**
	 * 使当前线程睡眠指定的毫秒数。
	 * 
	 * @param millis
	 *            指定当前线程睡眠多久，以毫秒为单位
	 */
	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}
}
