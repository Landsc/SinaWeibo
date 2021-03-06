package com.boredream.weibo.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.boredream.bdcodehelper.view.TitleBarView;
import com.boredream.weibo.BaseActivity;
import com.boredream.weibo.R;
import com.boredream.weibo.adapter.StatusAdapter;
import com.boredream.weibo.constants.UserInfoKeeper;
import com.boredream.weibo.entity.Goods;
import com.boredream.weibo.entity.User;
import com.boredream.weibo.widget.UnderlineIndicatorView;
import com.bumptech.glide.Glide;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class UserInfoActivity extends BaseActivity implements OnCheckedChangeListener {

	private TitleBarView titlebar;
	// headerView - 用户信息
	private View user_info_head;
	private ImageView iv_avatar;
	private TextView tv_name;
	private TextView tv_follows;
	private TextView tv_fans;
	private TextView tv_sign;
	// shadow_tab - 顶部悬浮的菜单栏
	private View shadow_user_info_tab;
	private RadioGroup shadow_rg_user_info;
	private UnderlineIndicatorView shadow_uliv_user_info;
	private View user_info_tab;
	private RadioGroup rg_user_info;
	private UnderlineIndicatorView uliv_user_info;
	// headerView - 添加至列表中作为header的菜单栏
	private ImageView iv_user_info_head;
	private SmartRefreshLayout refresh;
	private ListView lv_user_info;
	// 用户相关信息
	private boolean isCurrentUser;
	private User user;
	private String userName;
	// 个人微博列表
	private List<Goods> statuses = new ArrayList<>();
	private StatusAdapter statusAdapter;
	private int curPage = 1;
	// 背景图片最小高度
	private int minImageHeight = -1;
	// 背景图片最大高度
	private int maxImageHeight = -1;

	private int curScrollY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_user_info);

		userName = getIntent().getStringExtra("userName");
		if(TextUtils.isEmpty(userName)) {
			isCurrentUser = true;
			user = UserInfoKeeper.getInstance().getCurrentUser();
		}

		initView();
		
		loadData();
	}

	private void initView() {
		titlebar = (TitleBarView) findViewById(R.id.titlebar);
		titlebar.setBackgroundResource(R.drawable.userinfo_navigationbar_background);
		titlebar.setLeftImage(R.drawable.userinfo_navigationbar_back_sel);
		titlebar.setLeftOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		initInfoHead();
		initTab();
		initListView();
	}

	// 初始化用户信息
	private void initInfoHead() {
		iv_user_info_head = (ImageView) findViewById(R.id.iv_user_info_head);
		
		user_info_head = View.inflate(this, R.layout.user_info_head, null);
		iv_avatar = (ImageView) user_info_head.findViewById(R.id.iv_avatar);
		tv_name = (TextView) user_info_head.findViewById(R.id.tv_name);
		tv_follows = (TextView) user_info_head.findViewById(R.id.tv_follows);
		tv_fans = (TextView) user_info_head.findViewById(R.id.tv_fans);
		tv_sign = (TextView) user_info_head.findViewById(R.id.tv_sign);
	}

	// 初始化菜单栏
	private void initTab() {
		// 悬浮显示的菜单栏
		shadow_user_info_tab = findViewById(R.id.user_info_tab);
		shadow_rg_user_info = (RadioGroup) findViewById(R.id.rg_user_info);
		shadow_uliv_user_info = (UnderlineIndicatorView) findViewById(R.id.uliv_user_info);
		
		shadow_rg_user_info.setOnCheckedChangeListener(this);
		shadow_uliv_user_info.setCurrentItemWithoutAnim(1);
		
		// 添加到列表中的菜单栏
		user_info_tab = View.inflate(this, R.layout.user_info_tab, null);
		rg_user_info = (RadioGroup) user_info_tab.findViewById(R.id.rg_user_info);
		uliv_user_info = (UnderlineIndicatorView) user_info_tab.findViewById(R.id.uliv_user_info);
		
		rg_user_info.setOnCheckedChangeListener(this);
		uliv_user_info.setCurrentItemWithoutAnim(1);
	}
	
	@SuppressLint("NewApi")
	private void initListView() {
		refresh = (SmartRefreshLayout) findViewById(R.id.refresh);
		lv_user_info = (ListView) findViewById(R.id.lv_user_info);
		initLoadingLayout();
		statusAdapter = new StatusAdapter(this, statuses);
		lv_user_info.setAdapter(statusAdapter);
		lv_user_info.addHeaderView(user_info_head);
		lv_user_info.addHeaderView(user_info_tab);
		refresh.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh(RefreshLayout refreshlayout) {
				loadStatuses(1);
			}
		});
		refresh.setOnLoadmoreListener(new OnLoadmoreListener() {
			@Override
			public void onLoadmore(RefreshLayout refreshlayout) {
				loadStatuses(curPage + 1);
			}
		});

		// 同微博详情页处理
//		lv_user_info.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//			@Override
//			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//				if(minImageHeight == -1) {
//					minImageHeight = iv_user_info_head.getHeight();
//				}
//
//				if(maxImageHeight == -1) {
//					Rect rect = iv_user_info_head.getDrawable().getBounds();
//					maxImageHeight = rect.bottom - rect.top;
//				}
//
//				if(minImageHeight - scrollY < maxImageHeight) {
//					iv_user_info_head.layout(0, 0, iv_user_info_head.getWidth(),
//							minImageHeight - scrollY);
//				} else {
//					iv_user_info_head.layout(0,
//							-scrollY - (maxImageHeight - minImageHeight),
//							iv_user_info_head.getWidth(),
//							-scrollY - (maxImageHeight - minImageHeight) + iv_user_info_head.getHeight());
//				}
//			}
//		});
		
		iv_user_info_head.addOnLayoutChangeListener(new OnLayoutChangeListener() {

			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, 
					int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if(curScrollY == bottom - oldBottom) {
					iv_user_info_head.layout(0, 0, 
							iv_user_info_head.getWidth(), 
							oldBottom);
				}
			}
		});
		lv_user_info.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				iv_user_info_head.layout(0, 
						user_info_head.getTop(), 
						iv_user_info_head.getWidth(), 
						user_info_head.getTop() + iv_user_info_head.getHeight());
				
				if(user_info_head.getBottom() < titlebar.getBottom()) {
					shadow_user_info_tab.setVisibility(View.VISIBLE);
					titlebar.setBackgroundResource(R.drawable.navigationbar_background);
					titlebar.setLeftImage(R.drawable.navigationbar_back_sel);
					titlebar.getTvTitle().setVisibility(View.VISIBLE);
				} else {
					shadow_user_info_tab.setVisibility(View.GONE);
					titlebar.setBackgroundResource(R.drawable.userinfo_navigationbar_background);
					titlebar.setLeftImage(R.drawable.userinfo_navigationbar_back_sel);
					titlebar.getTvTitle().setVisibility(View.GONE);
				}
			}
		});
	}

	private void initLoadingLayout() {
//		ILoadingLayout loadingLayout = plv_user_info.getLoadingLayoutProxy();
//		loadingLayout.setPullLabel("");
//		loadingLayout.setRefreshingLabel("");
//		loadingLayout.setReleaseLabel("");
//		loadingLayout.setLoadingDrawable(new ColorDrawable(R.color.transparent));
	}

	private void loadData() {
		if(isCurrentUser) {
			// 如果是当前授权用户,直接设置信息
			setUserInfo();
		} else {
			// 如果是查看他人,调用获取用户信息接口
			loadUserInfo();
		}
		
		// 加载用户所属微博列表
		loadStatuses(1);
	}

	private void setUserInfo() {
		if(user == null) {
			return;
		}
		tv_name.setText(user.getNickname());
		titlebar.setTitleText(user.getNickname());

		Glide.with(this).load(user.getAvatarUrl()).into(iv_avatar);
		tv_follows.setText("关注");
		tv_fans.setText("粉丝");
		tv_sign.setText("简介: 这个人很懒什么都没有留下");
	}
	
	private void loadUserInfo() {
		// TODO: 2017/8/15 id
//		WbHttpRequest.getInstance()
//				.getApiService()
//				.getUserById()
//				.compose(RxComposer.<User>commonProgress(this))
//				.subscribe(new SimpleDisObserver<User>() {
//					@Override
//					public void onNext(User user) {
//						setUserInfo();
//					}
//				});
	}
	
	private void loadStatuses(final int page) {
		// userName为空是看自己，否则看别人
//		String uid;
//		String uname;
//		if (TextUtils.isEmpty(userName)) {
//			uid = accessToken.getUid();
//			uname = null;
//		} else {
//			uid = null;
//			uname = userName;
//		}
//		WeiboHttpRequest.getSingleton()
//				.getApiService()
//				.statusesUser_timeline(uid, uname, page)
//				.compose(RxComposer.<StatusListResponse>commonProgress(this))
//				.subscribe(new SimpleDisObserver<StatusListResponse>() {
//					@Override
//					public void onNext(StatusListResponse response) {
//						showLog("status comments = " + response);
//
//						if(page == 1) {
//							statuses.clear();
//						}
//
//						addStatus(response);
//					}
//
//					@Override
//					public void onError(Throwable e) {
//						// TODO: 2017/8/8
////						lv_home.onRefreshComplete();
//					}
//				});
	}
	
//	private void addStatus(StatusListResponse response) {
//		for(Status status : response.getStatuses()) {
//			if(!statuses.contains(status)) {
//				statuses.add(status);
//			}
//		}
//		statusAdapter.notifyDataSetChanged();
//
//		if(curPage < response.getTotal_number()) {
//			addFootView(footView);
//		} else {
//			removeFootView(footView);
//		}
//	}
	
	private void addFootView(View footView) {
		if(lv_user_info.getFooterViewsCount() == 1) {
			lv_user_info.addFooterView(footView);
		}
	}
	
	private void removeFootView(View footView) {
		if(lv_user_info.getFooterViewsCount() > 1) {
			lv_user_info.removeFooterView(footView);
		}
	}

	private void syncRadioButton(RadioGroup group, int checkedId) {
		int index = group.indexOfChild(group.findViewById(checkedId));
		
		if(shadow_user_info_tab.getVisibility() == View.VISIBLE) {
			shadow_uliv_user_info.setCurrentItem(index);
			
			((RadioButton)rg_user_info.findViewById(checkedId)).setChecked(true);
			uliv_user_info.setCurrentItemWithoutAnim(index);
		} else {
			uliv_user_info.setCurrentItem(index);
			
			((RadioButton)shadow_rg_user_info.findViewById(checkedId)).setChecked(true);
			shadow_uliv_user_info.setCurrentItemWithoutAnim(index);
		}
		
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// 同步悬浮和列表中的标题栏状态
		syncRadioButton(group, checkedId);
		
//		switch (checkedId) {
//		case R.id.rb_info:
//			if(shadow_user_info_tab.getVisibility() == View.VISIBLE) {
//				rb_info.setChecked(true);
//				uliv_user_info.setCurrentItemWithoutAnim(0);
//				
//				shadow_rb_info.setChecked(true);
//				shadow_uliv_user_info.setCurrentItem(0);
//			} else {
//				rb_info.setChecked(true);
//				uliv_user_info.setCurrentItem(0);
//				
//				shadow_rb_info.setChecked(true);
//				shadow_uliv_user_info.setCurrentItemWithoutAnim(0);
//			}
//			break;
//		case R.id.rb_status:
//			if(shadow_user_info_tab.getVisibility() == View.VISIBLE) {
//				rb_status.setChecked(true);
//				uliv_user_info.setCurrentItemWithoutAnim(1);
//				
//				shadow_rb_status.setChecked(true);
//				shadow_uliv_user_info.setCurrentItem(1);
//			} else {
//				rb_status.setChecked(true);
//				uliv_user_info.setCurrentItem(1);
//				
//				shadow_rb_status.setChecked(true);
//				shadow_uliv_user_info.setCurrentItemWithoutAnim(1);
//			}
//			break;
//		case R.id.rb_photos:
//			if(shadow_user_info_tab.getVisibility() == View.VISIBLE) {
//				rb_photos.setChecked(true);
//				uliv_user_info.setCurrentItemWithoutAnim(2);
//				
//				shadow_rb_photos.setChecked(true);
//				shadow_uliv_user_info.setCurrentItem(2);
//			} else {
//				rb_photos.setChecked(true);
//				uliv_user_info.setCurrentItem(2);
//				
//				shadow_rb_photos.setChecked(true);
//				shadow_uliv_user_info.setCurrentItemWithoutAnim(2);
//			}
//			break;
//		case R.id.rb_manager:
//			if(shadow_user_info_tab.getVisibility() == View.VISIBLE) {
//				rb_manager.setChecked(true);
//				uliv_user_info.setCurrentItemWithoutAnim(3);
//				
//				shadow_rb_manager.setChecked(true);
//				shadow_uliv_user_info.setCurrentItem(3);
//			} else {
//				rb_manager.setChecked(true);
//				uliv_user_info.setCurrentItem(3);
//				
//				shadow_rb_manager.setChecked(true);
//				shadow_uliv_user_info.setCurrentItemWithoutAnim(3);
//			}
//			break;
//
//		default:
//			break;
//		}
	}


}
