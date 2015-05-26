package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.ViewGroup;
import com.astuetz.PagerSlidingTabStrip;
import com.genesys.gms.mobile.callback.demo.legacy.common.BaseActivity;
import com.genesys.gms.mobile.callback.demo.legacy.R;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTabActivity extends BaseActivity {
    private PagerSlidingTabStrip tabs;
	protected TabFragmentPagerAdapter adapter;
    private ViewPager pager;
    private int currentTab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new TabFragmentPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(adapter);
        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override public void onPageSelected(int position) {
                currentTab = position;
                invalidateOptionsMenu();
            }
        };
        pager.setOnPageChangeListener(onPageChangeListener);
        tabs.setShouldExpand(true);
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(onPageChangeListener);
	}
	
	private class TabFragmentPagerAdapter extends FragmentPagerAdapter {
        private Map<Integer, String> mFragmentTags;
        private FragmentManager mFragmentManager;

        public TabFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
            mFragmentManager = fm;
            mFragmentTags = new HashMap<Integer, String>();
		}

        @Override
        public CharSequence getPageTitle(int position) {
            return getTabTitle(position);
        }

		@Override
		public Fragment getItem(int i) {
            return createFragment(i);
		}

		@Override
		public int getCount() {
            return getTabCount();
		}

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object obj = super.instantiateItem(container, position);
            if (obj instanceof Fragment) {
                // record the fragment tag here.
                Fragment f = (Fragment) obj;
                String tag = f.getTag();
                mFragmentTags.put(position, tag);
            }
            return obj;
        }

        public Fragment getFragment(int position) {
            String tag = mFragmentTags.get(position);
            if (tag == null)
                return null;
            return mFragmentManager.findFragmentByTag(tag);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean superResult = super.onCreateOptionsMenu(menu);
        Integer menuRes = getFragmentMenu(currentTab);
		if(menuRes != null) {
			getMenuInflater().inflate(menuRes, menu);
			return true;
		}
		return superResult;
	}

    abstract public CharSequence getTabTitle(int which);
    abstract public Fragment createFragment(int which);
    abstract public int getTabCount();
    abstract public Integer getFragmentMenu(int which);

    public Fragment getFragment(int position) {
        return adapter.getFragment(position);
    }
}
