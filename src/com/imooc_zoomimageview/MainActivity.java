package com.imooc_zoomimageview;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.view.ZoomImageView;

public class MainActivity extends Activity {
	private ViewPager myVp;

	private int[] mImgs = new int[] { R.drawable.img1, R.drawable.img2,
			R.drawable.img3 };
	
	private ImageView[] mImaViews = new ImageView[mImgs.length];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.vp);

		myVp = (ViewPager) findViewById(R.id.myVp);
		myVp.setAdapter(new PagerAdapter() {
			
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				ZoomImageView imageView = new ZoomImageView(getApplicationContext());
				imageView.setImageResource(mImgs[position]);
				container.addView(imageView);
				mImaViews[position] = imageView;
				
				return imageView;
			}
			
			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				container.removeView(mImaViews[position]);
			}
			
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			@Override
			public int getCount() {
				return mImaViews.length;
			}
		});
		
		
	}
}
