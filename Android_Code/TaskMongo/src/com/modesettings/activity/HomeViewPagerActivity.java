package com.modesettings.activity;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path.FillType;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;



public class HomeViewPagerActivity extends Activity {
	private RadioGroup radioGroup;
	private LinearLayout  lay_radio;
	
	 Integer[] imageId = {
	            R.drawable.one,
	            R.drawable.two,
	            R.drawable.three,
	            R.drawable.four,
	            R.drawable.five,
	            R.drawable.six,
	            R.drawable.seven };
	 private RadioButton radioButton1,radioButton2,radioButton3,radioButton4,radioButton5,radioButton6,radioButton7;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_home);
		
		 setRadioButtons();
		 ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		 viewPager.setAdapter(new CustomPagerAdapter(this));
		 viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
	            @Override
	            public void onPageSelected(int position) {
	            	  if(position==0){
	      		        radioGroup.check(radioButton1.getId());
	      		       
	      			 }
	      			 else if(position==1){
	      				 radioGroup.check(radioButton2.getId()); 
	      				
	      			 }
	      			 else if(position==2){
	      				 radioGroup.check(radioButton3.getId()); 
	      				
	      			 }
	      			 else if(position==3){
	      				 radioGroup.check(radioButton4.getId()); 
	      				
	      			 }
	      			 else if(position==4){
	      				 radioGroup.check(radioButton5.getId()); 
	      				
	      			 }
	      			 else if(position==5){
	      				 radioGroup.check(radioButton6.getId()); 
	          			 }
	      			 
	      			 else if(position==6){
	      				 radioGroup.check(radioButton7.getId()); 
	      				
	      			 }
	            }

	            @Override
	            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	            }

	            @Override
	            public void onPageScrollStateChanged(int state) {
	            }
	        });


	    }
	     
	
	private void setRadioButtons()
	{
	     	lay_radio=(LinearLayout)findViewById(R.id.lay_radio);
		    radioGroup=(RadioGroup)findViewById(R.id.viewgroup);
		     
		    radioButton1=(RadioButton)findViewById(R.id.radioButton1);
		    radioButton2=(RadioButton)findViewById(R.id.radioButton2);
		    radioButton3=(RadioButton)findViewById(R.id.radioButton3);
		    radioButton4=(RadioButton)findViewById(R.id.radioButton4);
		    radioButton5=(RadioButton)findViewById(R.id.radioButton5);
		    radioButton6=(RadioButton)findViewById(R.id.radioButton6);
		    radioButton7=(RadioButton)findViewById(R.id.radioButton7);
			 
		
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
	        TextView textView=(TextView)layout.findViewById(R.id.textView);
	        System.err.println("position="+position);
	      
	        if(position==6){
	        	textView.setText("Skip>>");
	        }
	        else if(position==0){
	        	radioGroup.check(radioButton1.getId());
	        	textView.setText("");
	        }
	        else{
	        	
	        	textView.setText("");
	        }
	        
	        imageView =(ImageView)layout.findViewById(R.id.imageView);
	    	imageView.setBackgroundResource(imageId[position]);
	 
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