package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.view.Menu;
import com.genesys.gms.mobile.callback.demo.legacy.R;

public abstract class AbstractTabActivity extends AbstractGenesysActivity {
	
	protected static class TabElement {
		public final String text;
		public final Fragment fragment;
		public final int icon;
		public final Integer menuRes;
		
		public TabElement(String text, Fragment fragment, int icon, Integer menuRes) {
			this.text = text;
			this.fragment = fragment;
			this.icon = icon;
			this.menuRes = menuRes;
		}
	}
	
	protected final TabElement[] tabs;
	private TabFragmentPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private TabElement currentTab;

	protected AbstractTabActivity(TabElement[] tabs) {
		this.tabs = tabs;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        pagerAdapter = new TabFragmentPagerAdapter(getFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(
			new ViewPager.SimpleOnPageChangeListener() {
				@Override public void onPageSelected(int position) {
					getSupportActionBar().setSelectedNavigationItem(position);
					invalidateOptionsMenu();
				}
			}
		);
        
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
				viewPager.setCurrentItem(tab.getPosition());
				currentTab = tabs[tab.getPosition()];
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
			}
		};
        
        for (int i = 0; i < tabs.length; i++) {
            actionBar.addTab(
            		actionBar.newTab()
                            .setText(tabs[i].text)
                            .setIcon(tabs[i].icon)
                            .setTabListener(tabListener));
        }
	}
	
	private class TabFragmentPagerAdapter extends FragmentPagerAdapter {
		public TabFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			return tabs[i].fragment;
		}

		@Override
		public int getCount() {
			return tabs.length;
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean superResult = super.onCreateOptionsMenu(menu);
		if(currentTab != null && currentTab.menuRes != null) {
			getMenuInflater().inflate(currentTab.menuRes, menu);
			return true;
		}
		return superResult;
	}
}
