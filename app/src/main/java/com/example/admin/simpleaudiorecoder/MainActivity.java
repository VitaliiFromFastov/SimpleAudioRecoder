package com.example.admin.simpleaudiorecoder;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener  {
    ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        viewPager =(ViewPager) findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(this);

        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        viewPager.setAdapter(new CategoryAdapter(this,getSupportFragmentManager()));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);


    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.recorder_title));

        viewPager.setCurrentItem(0);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
setTitle(getString(R.string.recorder_title));
        } else {
setTitle(getString(R.string.player_title));
}
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


}
