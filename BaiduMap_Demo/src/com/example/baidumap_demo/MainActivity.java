package com.example.baidumap_demo;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

public class MainActivity extends Activity {
	private MapView mapView;
	// Mapview的控制器
	private BaiduMap baiduMap;
	// 兴趣点检索器
	private PoiSearch poiSearch;
	// 定义一个webView
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 验证秘钥
		// 传入当前的Application:
		// 注册有name为com.baidu.lbsapi.API_KEY的元数据
		// 百度地图内部将取出对应的value去后台比较正确与否
		SDKInitializer.initialize(getApplicationContext());
		// 装载一个含有MapView的布局文件
		setContentView(R.layout.activity_main);
		// 获取MapView对象
		mapView = (MapView) findViewById(R.id.mapView);
		webView = (WebView) findViewById(R.id.webView);
		// 设置MapView的属性
		// 第一步 ：获得MapView的控制器
		// 第二步：设置属性
		baiduMap = mapView.getMap();
		// 设置是否开启交通路况图
		baiduMap.setTrafficEnabled(true);
		// 设置地图模式
		baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		// 设置缩放级别[3,19] 级别越大越近
		baiduMap.setMaxAndMinZoomLevel(19, 12);
		// 在地图上显示一个点
		setOnePointToMap();
		// 设置标点监听器
		setPointListener();
		// 获取POI兴趣点
		getPoiResult();
	}

	private void getPoiResult() {
		// 生成Poi检索器对象
		poiSearch = PoiSearch.newInstance();
		// 为检索器添加监听
		poiSearch
				.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {

					@Override
					public void onGetPoiResult(PoiResult arg0) {
						// 当获取到兴趣点结果时触发
						MyPoiOverlay poiOverlay = new MyPoiOverlay(baiduMap);
						baiduMap.setOnMarkerClickListener(poiOverlay);
						poiOverlay.setData(arg0);
						poiOverlay.addToMap();
					}

					@Override
					public void onGetPoiDetailResult(PoiDetailResult arg0) {
						// 当获取到兴趣点详情时触发

						String url = arg0.getDetailUrl();
						Log.i("", "#######：有结果：" + url);
						webView.getSettings().setJavaScriptEnabled(true);
						webView.setScrollBarStyle(0);
						WebSettings webSettings = webView.getSettings();
						webSettings.setAllowFileAccess(true);
						webSettings.setBuiltInZoomControls(true);
						webView.setWebViewClient(new WebViewClient() {
							@Override
							public boolean shouldOverrideUrlLoading(
									WebView view, String url) {
								return false;
							}

						}

						);
						webView.loadUrl(url);
					}
				});

	}

	private void setPointListener() {
		// 设置标点的监听器
		baiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				// 当标点被用户点击时触发该方法
				Log.i("", "当前用户点击的标题为：" + marker.getTitle());
				// 设置弹窗
				// 参数：View,位置,点击弹窗的监听器，Y轴偏移量（-50）
				// 生成一个Button对象，并设置其属性
				Button btn = new Button(getApplicationContext());
				btn.setBackgroundColor(Color.BLUE);
				btn.setText(marker.getTitle());
				// 将Button变成平面的图片（截图设置）
				BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
						.fromView(btn);
				InfoWindow infoWindow = new InfoWindow(bitmapDescriptor, marker
						.getPosition(), -50, new OnInfoWindowClickListener() {

					@Override
					public void onInfoWindowClick() {
						// 当用户点击弹窗时触发
						Log.i("", "您点击了弹窗~~~");
						// 开始检索
						poiSearch.searchInCity(new PoiCitySearchOption()
								.keyword("美食").city("北京").pageCapacity(10)
								.pageNum(0));
						// 隐藏弹窗
						baiduMap.hideInfoWindow();
					}
				});
				// 显示弹窗对象
				baiduMap.showInfoWindow(infoWindow);
				return false;
			}
		});

	}

	private void setOnePointToMap() {
		// 在地图上设置一个点(图片，位置，标题)
		// 第一步：创建一个覆盖层属性对象，并设置属性
		// 第二步：创建图片表述器
		// 第三步：设置位置（指定位置，定位）
		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
				.fromResource(R.drawable.btn_rating_star_on_disabled_holo_light);
		// 纬经度
		// 116.343615,39.94791
		LatLng latLng = new LatLng(39.94791, 116.343615);
		OverlayOptions overlayOptions = new MarkerOptions().title("动物园")
				.icon(bitmapDescriptor).position(latLng).rotate(45);
		// 将设置好数据的覆盖层添加到地图上
		baiduMap.addOverlay(overlayOptions);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mapView.onPause();
	}

	class MyPoiOverlay extends PoiOverlay {

		public MyPoiOverlay(BaiduMap arg0) {
			super(arg0);
		}

		@Override
		public boolean onPoiClick(int position) {
			// 当前覆盖层中的POI被点击时触发
			// getPoiResult():获取当前覆盖层结果对象
			// getAllPoi()：结果对象调用getAllPoi获取到当前显示的所有兴趣点的集合
			List<PoiInfo> lists = getPoiResult().getAllPoi();
			// 通过传入的position来获取到指定用户点击的poi点信息对象
			final PoiInfo poiInfo = lists.get(position);
			// 从兴趣点信息对象中获取需要的信息内容
			Toast.makeText(getApplicationContext(),
					"您当前点击的兴趣点为：" + poiInfo.name, Toast.LENGTH_LONG).show();
			// 弹出弹窗，弹窗中内容:兴趣点名称
			// 设置弹窗
			// 参数：View,位置,点击弹窗的监听器，Y轴偏移量（-50）
			// 生成一个Button对象，并设置其属性
			Button btn = new Button(getApplicationContext());
			btn.setBackgroundColor(Color.BLUE);
			btn.setText(poiInfo.name);
			// 将Button变成平面的图片（截图设置）
			BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
					.fromView(btn);
			InfoWindow infoWindow = new InfoWindow(bitmapDescriptor,
					poiInfo.location, -50, new OnInfoWindowClickListener() {

						@Override
						public void onInfoWindowClick() {
							// 当用户点击弹窗时触发
							Log.i("", "您点击了兴趣点弹窗~~~");
							// 在弹窗监听器中，二次检索兴趣点的详情（通过uid）
							// 开始搜索指定uid的详情
							poiSearch
									.searchPoiDetail(new PoiDetailSearchOption()
											.poiUid(poiInfo.uid));
							// 隐藏弹窗
							baiduMap.hideInfoWindow();
						}
					});
			// 显示弹窗对象
			baiduMap.showInfoWindow(infoWindow);

			return super.onPoiClick(position);
		}

	}
}
