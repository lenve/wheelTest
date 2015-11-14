package com.lenve.wheel;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements OnWheelChangedListener {

	/**
	 * 省
	 */
	private String[] provinceArray;
	/**
	 * 省-市
	 */
	private Map<String, String[]> citiesMap;
	/**
	 * 市-区县
	 */
	private Map<String, String[]> areasMap;
	/**
	 * 省的滚动View
	 */
	private WheelView provinceView;
	/**
	 * 城市的滚动View
	 */
	private WheelView cityView;
	/**
	 * 区县的滚动View
	 */
	private WheelView areaView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		provinceView = (WheelView) this.findViewById(R.id.province_view);
		cityView = (WheelView) this.findViewById(R.id.city_view);
		areaView = (WheelView) this.findViewById(R.id.area_view);
		initJson();
		initView();
	}

	private void initView() {
		provinceView.setViewAdapter(new ArrayWheelAdapter<String>(
				MainActivity.this, provinceArray));
		// 默认显示北京直辖市里边的市（只有北京市）
		cityView.setViewAdapter(new ArrayWheelAdapter<String>(
				MainActivity.this, citiesMap.get("北京")));
		// 默认显示北京市里边的区县
		areaView.setViewAdapter(new ArrayWheelAdapter<String>(
				MainActivity.this, areasMap.get("北京")));

		// 默认显示第一项
		provinceView.setCurrentItem(0);
		// 默认显示第一项
		cityView.setCurrentItem(0);
		// 默认显示第一项
		areaView.setCurrentItem(0);
		// 页面上显示7项
		provinceView.setVisibleItems(7);
		cityView.setVisibleItems(7);
		areaView.setVisibleItems(7);
		// 添加滑动事件
		provinceView.addChangingListener(this);
		cityView.addChangingListener(this);
	}

	private void initJson() {
		citiesMap = new HashMap<String, String[]>();
		areasMap = new HashMap<String, String[]>();
		InputStream is = null;
		try {
			StringBuffer sb = new StringBuffer();
			is = getAssets().open("city.json");
			int len = -1;
			byte[] buf = new byte[1024];
			while ((len = is.read(buf)) != -1) {
				sb.append(new String(buf, 0, len, "gbk"));
			}
			JSONArray ja = new JSONArray(sb.toString());
			provinceArray = new String[ja.length()];
			String[] citiesArr = null;
			for (int i = 0; i < provinceArray.length; i++) {
				JSONObject jsonProvince = ja.getJSONObject(i);
				provinceArray[i] = jsonProvince.getString("name");
				JSONArray jsonCities = jsonProvince.getJSONArray("city");
				citiesArr = new String[jsonCities.length()];
				for (int j = 0; j < citiesArr.length; j++) {
					JSONObject jsonCity = jsonCities.getJSONObject(j);
					citiesArr[j] = jsonCity.getString("name");
					JSONArray jsonAreas = jsonCity.getJSONArray("area");
					String[] areaArr = new String[jsonAreas.length()];
					for (int k = 0; k < jsonAreas.length(); k++) {
						areaArr[k] = jsonAreas.getString(k);
					}
					areasMap.put(citiesArr[j], areaArr);
				}
				citiesMap.put(provinceArray[i], citiesArr);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 滚动事件
	 */
	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		if (wheel == provinceView) {
			// 更新省的时候不仅要更新市同时也要更新区县
			updateCity();
			updateArea();
		} else if (wheel == cityView) {
			// 更新市的时候只用更新区县即可
			updateArea();
		}
	}

	private void updateArea() {
		// 获得当前显示的City的下标
		int cityIndex = cityView.getCurrentItem();
		// 获得当前显示的省的下标
		int provinceIndex = provinceView.getCurrentItem();
		// 获得当前显示的省的名字
		String proviceName = provinceArray[provinceIndex];
		// 获得当前显示的城市的名字
		String currentName = citiesMap.get(proviceName)[cityIndex];
		// 根据当前显示的城市的名字获得该城市下所有的区县
		String[] areas = areasMap.get(currentName);
		// 将新获得的数据设置给areaView
		areaView.setViewAdapter(new ArrayWheelAdapter<String>(
				MainActivity.this, areas));
		// 默认显示第一项
		areaView.setCurrentItem(0);
	}

	private void updateCity() {
		// 获得当前显示的省的下标
		int currentIndex = provinceView.getCurrentItem();
		// 获得当前显示的省的名称
		String currentName = provinceArray[currentIndex];
		// 根据当前显示的省的名称获得该省中所有的市
		String[] cities = citiesMap.get(currentName);
		// 将新获得的数据设置给cityView
		cityView.setViewAdapter(new ArrayWheelAdapter<String>(
				MainActivity.this, cities));
		// 默认显示第一项
		cityView.setCurrentItem(0);
	}

	public void onClick(View v) {
		// 获得当前显示的省的下标
		int provinceIndex = provinceView.getCurrentItem();
		// 获得当前显示的省的名称
		String provinceName = provinceArray[provinceIndex];
		// 获得当前显示的城市的下标
		int cityIndex = cityView.getCurrentItem();
		// 获得当前显示的城市的名称
		String cityName = citiesMap.get(provinceName)[cityIndex];
		// 获得当前显示的区县的下标
		int areaIndex = areaView.getCurrentItem();
		Toast.makeText(
				this,
				"您选择的地区是" + provinceArray[provinceIndex] + cityName
						+ areasMap.get(cityName)[areaIndex], Toast.LENGTH_SHORT)
				.show();
	}
}
