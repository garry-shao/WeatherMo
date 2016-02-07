package org.qmsos.environmo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.util.Log;
import android.view.MenuItem;

public class MapActivity extends AppCompatActivity implements OnMenuItemClickListener, CordovaInterface {

	private static final String START_URL = "file:///android_asset/www/index.html";
	
	private static final String TAG = MapActivity.class.getSimpleName();
	
	private SystemWebView systemWebView;
	private CordovaWebView cordovaWebView;
	private ExecutorService threadPool;
	private CordovaPlugin activityResultCallback;
	private CordovaPlugin permissionResultCallback;	
	private int activityResultRequestCode;

	private long cityId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.activity_weather_map);
		toolbar.inflateMenu(R.menu.menu_map_layer);
		toolbar.setOnMenuItemClickListener(this);
		
		ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(this);
		
        threadPool = Executors.newCachedThreadPool();
        
		systemWebView = (SystemWebView) findViewById(R.id.weather_map_view);
		cordovaWebView = new CordovaWebViewImpl(new SystemWebViewEngine(systemWebView));
		cordovaWebView.init(this, parser.getPluginEntries(), parser.getPreferences());

		cityId = getIntent().getLongExtra(MainUpdateService.EXTRA_KEY_CITY_ID, -1);
		
		cordovaWebView.loadUrl(assembleStartUrl(null, cityId));
	}

	@Override
	protected void onDestroy() {
		if (systemWebView != null && systemWebView.getCordovaWebView() != null) {
			systemWebView.getCordovaWebView().handleDestroy();
		}
		
		super.onDestroy();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_layer_precipitation:
			cordovaWebView.loadUrl(assembleStartUrl("precipitation", cityId));
			return true;
		case R.id.menu_layer_rain:
			cordovaWebView.loadUrl(assembleStartUrl("rain", cityId));
			return true;
		case R.id.menu_layer_snow:
			cordovaWebView.loadUrl(assembleStartUrl("snow", cityId));
			return true;
		case R.id.menu_layer_clouds:
			cordovaWebView.loadUrl(assembleStartUrl("clouds", cityId));
			return true;
		case R.id.menu_layer_pressure:
			cordovaWebView.loadUrl(assembleStartUrl("pressure", cityId));
			return true;
		case R.id.menu_layer_temperature:
			cordovaWebView.loadUrl(assembleStartUrl("temp", cityId));
			return true;
		case R.id.menu_layer_windspeed:
			cordovaWebView.loadUrl(assembleStartUrl("wind", cityId));
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
        if(permissionResultCallback != null)
        {
            try {
				permissionResultCallback.onRequestPermissionResult(requestCode, permissions, grantResults);
			} catch (JSONException e) {
				e.printStackTrace();
			}
            permissionResultCallback = null;
        }
	}

	@Override
	public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        setActivityResultCallback(command);
        try {
            startActivityForResult(intent, requestCode);
        } catch (RuntimeException e) { // E.g.: ActivityNotFoundException
            activityResultCallback = null;
            throw e;
        }		
	}

	@Override
	public void setActivityResultCallback(CordovaPlugin plugin) {
	    // Cancel any previously pending activity.
        if (activityResultCallback != null) {
            activityResultCallback.onActivityResult(activityResultRequestCode, Activity.RESULT_CANCELED, null);
        }
        activityResultCallback = plugin;
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public Object onMessage(String id, Object data) {
        if ("exit".equals(id)) {
            finish();
        }
        return null;
	}

	@Override
	public ExecutorService getThreadPool() {
		return threadPool;
	}

	@Override
	public void requestPermission(CordovaPlugin plugin, int requestCode, String permission) {
        permissionResultCallback = plugin;
        
        String[] permissions = { permission };
        getActivity().requestPermissions(permissions, requestCode);		
	}

	@Override
	public void requestPermissions(CordovaPlugin plugin, int requestCode, String[] permissions) {
        permissionResultCallback = plugin;
        
        getActivity().requestPermissions(permissions, requestCode);
	}

	@Override
	public boolean hasPermission(String permission) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int result = checkSelfPermission(permission);
			return PackageManager.PERMISSION_GRANTED == result;
		} else {
			return true;
		}
	}

	private String assembleStartUrl(String layer, long cityId) {
		StringBuilder builder;
		if (layer == null) {
			builder = new StringBuilder(START_URL + "?");
		} else {
			builder = new StringBuilder(START_URL + "?l=" + layer);
		}
		
		if (cityId > 0) {
			String extra = assembleStartUrlExtra(cityId);
			if (extra != null) {
				builder.append(extra);
			}
		}
		
		return builder.toString();
	}
	
	private String assembleStartUrlExtra(long cityId) {
		if (cityId <= 0) {
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		
		Cursor cursor = null;
		try {
			String[] projection = { MainProvider.KEY_LONGITUDE, MainProvider.KEY_LATITUDE };
			String where = MainProvider.KEY_CITY_ID + " = " + cityId;
			
			cursor = getContentResolver().query(
					MainProvider.CONTENT_URI_CITIES, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long longitude = cursor.getLong(cursor.getColumnIndexOrThrow(MainProvider.KEY_LONGITUDE));
				long latitude = cursor.getLong(cursor.getColumnIndexOrThrow(MainProvider.KEY_LATITUDE));
				
				int zoomlevel = 4;
				
				builder.append("&lon=");
				builder.append(longitude);
				builder.append("&lat=");
				builder.append(latitude);
				builder.append("&zoom=");
				builder.append(zoomlevel);
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "The column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		return builder.toString();
	}

}