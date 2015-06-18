package com.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

public class ZoomImageView extends ImageView implements OnGlobalLayoutListener,
		OnScaleGestureListener, OnTouchListener {

	private boolean initOnce;

	/**
	 * ��ʼ��ʱ���ŵ�ֵ
	 */
	private float mInitScale;

	/**
	 * ˫���ﵽ��ֵ
	 */
	private float mMidScale;

	/**
	 * ���Ŵ�ֵ
	 */
	private float mMaxScale;

	private Matrix mScaleMatrix;

	/**
	 * �����û���ָ����ʱ���ű���
	 */
	private ScaleGestureDetector mScaleGestureDetector;

	// ---------------------------�����ƶ�
	/**
	 * ��¼��һ����һ�ζ�㴥�ص�����
	 */
	private int mLastPointCount;

	private float mLastX;
	private float mLastY;

	private int mTouchSlop;// ϵͳ�� �ж��û���ָ�Ƿ��ƶ��ıȽ�ֵ
	private boolean isCanDrag;

	private boolean isCheckLeftAndRight;
	private boolean isCheckTopAndBottom;

	// --------------------˫���Ŵ�����С
	private GestureDetector mGestureDetector;
	private boolean isAutoScale;//�Ƿ�����˫��������,��ʱ���账��
	
	
	public ZoomImageView(Context context) {
		this(context, null);
	}

	public ZoomImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mScaleMatrix = new Matrix();
		super.setScaleType(ScaleType.MATRIX);// ������������

		mScaleGestureDetector = new ScaleGestureDetector(context, this);

		setOnTouchListener(this);

		// Distance in dips a touch can wander before we think the user is
		// scrolling
		mTouchSlop = ViewConfiguration.get(context).getTouchSlop();
		mGestureDetector = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if(isAutoScale)
							return true;
						
						float x = e.getX();
						float y = e.getY();

						if (getScale() < mMidScale) {
//							mScaleMatrix.postScale(mMidScale / getScale(),
//									mMidScale / getScale(), x, y);
//							setImageMatrix(mScaleMatrix);
							postDelayed(new AutoScaleRunnable(mMidScale, x, y), 16);
							isAutoScale = true;
						} else {
//							mScaleMatrix.postScale(mInitScale / getScale(),
//									mInitScale / getScale(), x, y);
//							setImageMatrix(mScaleMatrix);
							postDelayed(new AutoScaleRunnable(mInitScale, x, y), 16);
							isAutoScale = true;
						}

						return true;
					}
				});
	}

	/**
	 * �ݶȷŴ�
	 * 
	 * @author Administrator
	 * 
	 */
	private class AutoScaleRunnable implements Runnable {
		/**
		 * ���ŵ�Ŀ��ֵ
		 */
		private float mTargetScale;
		// �������ĵ�
		private float x;
		private float y;

		private final float BIGGER = 1.07f;
		private final float SMALLER = 0.93f;

		private float tmpScale;

		public AutoScaleRunnable(float mTargetScale, float x, float y) {
			this.mTargetScale = mTargetScale;
			this.x = x;
			this.y = y;

			if (getScale() < mTargetScale) {
				tmpScale = BIGGER;
			}

			if (getScale() > mTargetScale) {
				tmpScale = SMALLER;
			}
		}

		@Override
		public void run() {
			// ��������
			mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
			checkBorderAndCenterWhenScale();
			setImageMatrix(mScaleMatrix);
			float currentScale = getScale();

			if ((tmpScale > 1.0f && currentScale < mTargetScale)
					|| (tmpScale < 1.0f && currentScale > mTargetScale)) {
				postDelayed(this, 16);//��ִ��run
			} else {
				//����ΪĿ��ֵ
				float scale = mTargetScale / currentScale;
				mScaleMatrix.postScale(scale, scale, x, y);
				checkBorderAndCenterWhenScale();
				setImageMatrix(mScaleMatrix);
				isAutoScale = false;//���Ž���,�����ٴ�˫��
			}

		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	/**
	 * ��ȫ�ֲ��ֻ滭��ɺ�,��ȡImageView������ɵ�ͼƬ
	 */
	@Override
	public void onGlobalLayout() {
		if (!initOnce) {
			// �ÿؼ��Ŀ�͸�
			int width = getWidth();
			int height = getHeight();

			// �õ�ͼƬ �Լ���͸�
			Drawable d = getDrawable();
			if (d == null) {
				return;
			}

			int dw = d.getIntrinsicWidth();
			int dh = d.getIntrinsicHeight();

			float scale = 1.0f;
			if (dw >= width && dh <= height) {
				scale = width * 1.0f / dw;
			}

			if (dh >= height && dw <= width) {
				scale = height * 1.0f / dh;
			}

			if ((dw >= width && dh >= height) || (dw <= width && dh <= height)) {
				scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
			}

			// if(dw < width && dh < height){
			// scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
			// }

			// Log.i("--", "width = " + width + ", height = " + height +
			// ", dw = " + dw
			// + ", dh = " + dh + ",, scale = " + scale);

			mInitScale = scale;
			mMaxScale = 4 * mInitScale;
			mMidScale = 2 * mInitScale;

			// ��ͼƬ�ƶ����ؼ�������
			int dx = getWidth() / 2 - dw / 2;
			int dy = getHeight() / 2 - dh / 2;

			mScaleMatrix.postTranslate(dx, dy);
			mScaleMatrix.postScale(mInitScale, mInitScale, getWidth() / 2,
					getHeight() / 2);
			setImageMatrix(mScaleMatrix);
			initOnce = true;
		}
	}

	/**
	 * �õ���ǰͼƬ������ֵ
	 * 
	 * @return
	 */
	public float getScale() {
		float[] values = new float[9];
		mScaleMatrix.getValues(values);
		return values[Matrix.MSCALE_X];
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		// Log.i("--","onScale");
		float scale = getScale();
		float scaleFactor = detector.getScaleFactor();

		if (getDrawable() == null) {
			return true;
		}

		// ���ŷ�Χ�Ŀ���
		if ((scale < mMaxScale && scaleFactor > 1.0f)
				|| (scale > mInitScale && scaleFactor < 1.0f)) {
			if (scale * scaleFactor < mInitScale) {
				scaleFactor = mInitScale / scale;
			}

			if (scale * scaleFactor > mMaxScale) {
				scale = mMaxScale / scale;
			}

			// ����
			mScaleMatrix.postScale(scaleFactor, scaleFactor,
					detector.getFocusX(), detector.getFocusY());

			checkBorderAndCenterWhenScale();

			setImageMatrix(mScaleMatrix);
		}

		return true;
	}

	/**
	 * ���ͼƬ�Ŵ����С�Ŀ�͸ߣ��Լ�right��left��top��bottom
	 * 
	 * @return
	 */
	private RectF getMatrixRectF() {
		Matrix matrix = mScaleMatrix;
		RectF rectF = new RectF();

		Drawable d = getDrawable();
		if (d != null) {
			rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			matrix.mapRect(rectF);
		}

		return rectF;
	}

	/**
	 * ������ʱ�����ϼ���Ƿ������м䣬�Ƿ�¶���߽�
	 */
	private void checkBorderAndCenterWhenScale() {
		RectF rectF = getMatrixRectF();
		float deltaX = 0;
		float deltaY = 0;

		int width = getWidth();
		int height = getHeight();

		if (rectF.width() >= width) {
			if (rectF.left > 0) {
				deltaX = -rectF.left;
			}

			if (rectF.right < width) {
				deltaX = width - rectF.right;
			}
		}

		if (rectF.height() >= height) {
			if (rectF.top > 0) {
				deltaY = -rectF.top;
			}

			if (rectF.bottom < height) {
				deltaY = height - rectF.bottom;
			}
		}

		// �����Ȼ��߸߶�С�ڿؼ��Ŀ�͸ߣ����������
		if (rectF.width() < width) {
			deltaX = width / 2f - rectF.right + rectF.width() / 2;
		}

		if (rectF.height() < height) {
			deltaY = height / 2f - rectF.bottom + rectF.height() / 2;
		}

		mScaleMatrix.postTranslate(deltaX, deltaY);

	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
		/** �������return true */
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event)) {
			return true;
		}

		mScaleGestureDetector.onTouchEvent(event);

		// ���ĵ��λ��
		float x = 0;
		float y = 0;
		// �õ���㴥�ص�����
		int pointCount = event.getPointerCount();
		for (int i = 0; i < pointCount; i++) {
			x += event.getX();
			y += event.getY();
		}

		x /= pointCount;
		y /= pointCount;

		if (mLastPointCount != pointCount) {
			isCanDrag = false;
			mLastX = x;
			mLastY = y;
			mLastPointCount = pointCount;
		}
		
		RectF rectF = getMatrixRectF();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		if(rectF.width() > getWidth() + 0.01 || rectF.height() > getHeight() + 0.01){
			//��ϣ�������ؼ�����
			if(getParent() instanceof ViewPager)
				getParent().requestDisallowInterceptTouchEvent(true);
		}
		break;
		
		case MotionEvent.ACTION_MOVE:
			if(rectF.width() > getWidth() + 0.01 || rectF.height() > getHeight() + 0.01){
				//��ϣ�������ؼ�����
				if(getParent() instanceof ViewPager)
					getParent().requestDisallowInterceptTouchEvent(true);
			}
			
			
			float dx = x - mLastX;
			float dy = y - mLastY;

			if (!isCanDrag) {
				isCanDrag = isMoveAction(dx, dy);
			}

			if (isCanDrag) {
				// ���ͼƬ���ƶ�
//				RectF rectF = getMatrixRectF();
				if (getDrawable() != null) {
					isCheckLeftAndRight = isCheckTopAndBottom = true;

					// ������С�ڿؼ��Ŀ��, ���ܺ����ƶ�
					if (rectF.width() < getWidth()) {
						isCheckLeftAndRight = false;
						dx = 0;
					}
					// ����߶�С�ڿؼ��ĸ߶ȣ����������ƶ�
					if (rectF.height() < getHeight()) {
						isCheckTopAndBottom = false;
						dy = 0;
					}

					mScaleMatrix.postTranslate(dx, dy);
					checkBorderWhenTranslate();
					setImageMatrix(mScaleMatrix);
				}
			}

			mLastX = x;
			mLastY = y;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mLastPointCount = 0;
			break;
		}

		return true;
	}

	/**
	 * ���ƶ�ʱ�����б߽���,������������ƶ�
	 */
	private void checkBorderWhenTranslate() {
		RectF rectF = getMatrixRectF();
		float deltaX = 0;
		float deltaY = 0;

		int width = getWidth();
		int height = getHeight();

		if (rectF.top > 0 && isCheckTopAndBottom) {
			deltaY = -rectF.top;
		}

		if (rectF.bottom < height && isCheckTopAndBottom) {
			deltaY = height - rectF.bottom;
		}

		if (rectF.left > 0 && isCheckLeftAndRight) {
			deltaX = -rectF.left;
		}

		if (rectF.right < width && isCheckLeftAndRight) {
			deltaX = width - rectF.right;
		}

		mScaleMatrix.postTranslate(deltaX, deltaY);

	}

	/**
	 * �ж��Ƿ��ƶ�
	 * 
	 * @param dx
	 * @param dy
	 * @return
	 */
	private boolean isMoveAction(float dx, float dy) {

		return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
	}

}
