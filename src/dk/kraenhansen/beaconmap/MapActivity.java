package dk.kraenhansen.beaconmap;

import java.util.List;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MapActivity extends Activity {

	private static final String TAG = "dk.kraenhansen.beaconmap";
	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);

	private BeaconManager beaconManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		beaconManager = new BeaconManager(getApplicationContext());

		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
			@Override
			public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {

				TextView statusTextView = (TextView) findViewById(R.id.status_text);
				String foundNBeacons = getString(R.string.found_n_beacons);
				String statusText = String.format(foundNBeacons, Integer.valueOf(beacons.size()));
				statusTextView.setText(statusText);
				
				Log.d(TAG, "Ranged beacons: " + beacons);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Should be invoked in #onStart.
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
					beaconManager.setForegroundScanPeriod(1500, 0);
				} catch (RemoteException e) {
					Log.e(TAG, "Cannot start ranging", e);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if(id == R.id.action_calibrate) {
			Intent intent = new Intent(this, CalibrationActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		beaconManager.disconnect();
	}
}
