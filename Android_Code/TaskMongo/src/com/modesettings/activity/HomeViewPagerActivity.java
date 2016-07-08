package com.modesettings.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;



public class HomeViewPagerActivity extends Activity {

	
	 Integer[] imageId = {
	            R.drawable.one,
	            R.drawable.two,
	            R.drawable.three,
	            R.drawable.four,
	            R.drawable.five,
	            R.drawable.six,
	            R.drawable.seven };
	 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_home);
		
		 ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
	       viewPager.setAdapter(new CustomPagerAdapter(this));
	}
	public class CustomPagerAdapter extends PagerAdapter {
		 
	    private Context mContext;
	    private ImageView imageView;
	 
	    public CustomPagerAdapter(Context context) {
	        mContext = context;
	    }
	 
	    @Override
	    public Object instantiateItem(ViewGroup collection, int position) {
	       // ModelObject modelObject = ModelObject.values()[position];
	        LayoutInflater inflater = LayoutInflater.from(mContext);
	        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.viewpager, collection, false);
	        imageView =(ImageView)layout.findViewById(R.id.imageView);
	    	imageView.setImageResource(imageId[position]);
	    	TextView textView=(TextView)layout.findViewById(R.id.textView);
	    	if(position==6){
	    	
	    		textView.setText("Finish");
	    		
	    	}
	    	else{
	    		textView.setText("Skip>>");
	    	}
	    	textView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent mainIntent = new Intent(HomeViewPagerActivity.this,ListRulesActivity.class);
					HomeViewPagerActivity.this.startActivity(mainIntent);
					HomeViewPagerActivity.this.finish();
				}
			});
	        collection.addView(layout);
	        return layout;
	    }
	 
	    @Override
	    public void destroyItem(ViewGroup collection, int position, Object view) {
	        collection.removeView((View) view);
	    }
	 
	    @Override
	    public int getCount() {
	        return imageId.length;
	    }
	 
	    @Override
	    public boolean isViewFromObject(View view, Object object) {
	        return view == object;
	    }
	 
	    @Override
	    public CharSequence getPageTitle(int position) {
	    	imageView.setImageResource(imageId[position]);
	        return ""+position;
	    }
	 
	}
}