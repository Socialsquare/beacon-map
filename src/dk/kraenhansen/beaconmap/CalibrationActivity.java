package dk.kraenhansen.beaconmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;

import dk.kraenhansen.beaconmap.model.Calibration;
import dk.kraenhansen.beaconmap.model.CalibrationFixpoint;
import dk.kraenhansen.beaconmap.model.FixedBeacon;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CalibrationActivity extends Activity {

	private static final String TAG = "dk.kraenhansen.beaconmap";
	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);

	private BeaconManager beaconManager;
	
	private CalibrationFixpoint currentCalibrationFixpoint;
	
	private Calibration calibration = new Calibration();
	
	class CalibrateButtonOnTouchListener implements OnTouchListener {
		
		private CalibrationFixpoint fixpoint;
		
		public CalibrateButtonOnTouchListener(CalibrationFixpoint fixpoint) {
			this.fixpoint = fixpoint;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				currentCalibrationFixpoint = fixpoint;
			} else if(event.getAction() == MotionEvent.ACTION_UP) {
				currentCalibrationFixpoint = null;
			}
			return false;
		}
	}
	
	class CalibrationDataRow {
		TextView uniqueBeacons;
		TextView samples;
	}
	
	/*
	 * The data rows in the table layout - one row pr. calibration point.
	 */
	private Map<CalibrationFixpoint, CalibrationDataRow> calibrationDataRows = new HashMap<CalibrationFixpoint, CalibrationDataRow>();
	
	private void updateDataTable() {
		for(CalibrationFixpoint f: calibrationDataRows.keySet()) {
			CalibrationDataRow row = calibrationDataRows.get(f);
			row.uniqueBeacons.setText( String.valueOf(calibration.getNumberOfUniqueBeacons(f)) );
			row.samples.setText( String.valueOf(calibration.getNumberOfSamples(f)) );
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);
		
		calibration.addFixpoint(new CalibrationFixpoint("Upper Left", -1.0, 1.0));
		calibration.addFixpoint(new CalibrationFixpoint("Upper Right", 1.0, 1.0));
		calibration.addFixpoint(new CalibrationFixpoint("Lower Left", -1.0, -1.0));
		calibration.addFixpoint(new CalibrationFixpoint("Lower Right", 1.0, -1.0));
		
		LinearLayout calibrateButtons = (LinearLayout) findViewById(R.id.calibrateButtons);
		TableLayout dataTable = (TableLayout) findViewById(R.id.dataTable);
		
		for(CalibrationFixpoint f: calibration.getFixpoints()) {
			// Create calibration buttons
			Button b = new Button(this, null, android.R.attr.buttonStyleSmall);
			b.setText("Calibrate " + f.getTitle() + " corner");
			CalibrateButtonOnTouchListener onTouchListener = new CalibrateButtonOnTouchListener(f);
			b.setOnTouchListener(onTouchListener);
			
			calibrateButtons.addView(b);
			
			TableRow fixtureRow = new TableRow(this	);
			
			TextView titleTextView = new TextView(this);
			titleTextView.setText(f.getTitle());
			fixtureRow.addView(titleTextView);
			
			TextView uniqueBeaconsTextView = new TextView(this);
			uniqueBeaconsTextView.setText("?");
			fixtureRow.addView(uniqueBeaconsTextView);
			
			TextView samplesTextView = new TextView(this);
			samplesTextView.setText("?");
			fixtureRow.addView(samplesTextView);
			
			CalibrationDataRow calibrationDataRow = new CalibrationDataRow();
			calibrationDataRow.uniqueBeacons = uniqueBeaconsTextView;
			calibrationDataRow.samples = samplesTextView;
			calibrationDataRows.put(f, calibrationDataRow);
			
			dataTable.addView(fixtureRow);
		}
		
		Button estimateLocationsButton = (Button) findViewById(R.id.estimateLocationsButton);
		estimateLocationsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Estimating beacon locations.");
				calibration.estimateBeaconLocations();
			}
		});
		
		Button saveCalibrationButton = (Button) findViewById(R.id.saveCalibrationButton);
		saveCalibrationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "Saving calibration.");
				calibration.save("calibrations");
			}
		});

		beaconManager = new BeaconManager(getApplicationContext());

		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
			@Override
			public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
				if(currentCalibrationFixpoint != null) {
					for(Beacon b: beacons) {
						double distance = ((double) b.getRssi()) / -100;
						calibration.recordDistance(currentCalibrationFixpoint, b.getMacAddress(), distance);
					}
					updateDataTable();
				}
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
	protected void onDestroy() {
		super.onDestroy();
		beaconManager.disconnect();
	}
}
