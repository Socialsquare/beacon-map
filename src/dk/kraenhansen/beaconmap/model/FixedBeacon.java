package dk.kraenhansen.beaconmap.model;

import java.util.List;
import java.util.Map;

import android.util.Log;

public class FixedBeacon {

	private static final String TAG = "dk.kraenhansen.beaconmap.estimation";
	
	private String mac;
	
	public FixedBeacon(String mac) {
		this.mac = mac;
	}
	
	private double x;
	private double y;
	
	public void estimateLocation(Map<CalibrationFixpoint, List<Double>> map) {
		Log.d(TAG, "Estimate location of the fixed beacon " + mac);
		for(CalibrationFixpoint f: map.keySet()) {
			Log.d(TAG, "\tIn relation to the calibration fixpoint " + f.getTitle() + " the distances are:");
			double averageDistance = 0;
			List<Double> samples = map.get(f);
			for(Double distance: samples) {
				averageDistance += distance;
			}
			averageDistance /= samples.size();
			Log.d(TAG, "\t\tAverage distance = " + averageDistance + " from " + samples.size() + " samples.");
		}
	}
}
