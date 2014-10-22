package dk.kraenhansen.beaconmap.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.estimote.sdk.utils.L;

public class Calibration {
	
	private List<CalibrationFixpoint> fixpoints = new ArrayList<CalibrationFixpoint>();
	
	private Map<CalibrationFixpoint, Map<String, List<Double>>> distancesPrBeaconPrFixpoint = new HashMap<CalibrationFixpoint, Map<String, List<Double>>>();
	
	public Calibration() {
		
	}
	
	public List<CalibrationFixpoint> getFixpoints() {
		return fixpoints;
	}

	public void addFixpoint(CalibrationFixpoint fixpoint) {
		fixpoints.add(fixpoint);
		// Initialize the data structure for collecting distances.
		Map<String, List<Double>> distancesPrBeacon = new HashMap<String, List<Double>>();
		distancesPrBeaconPrFixpoint.put(fixpoint, distancesPrBeacon);
	}
	
	public void recordDistance(CalibrationFixpoint fixpoint, String beaconMacAddress, Double distance) {
		Map<String, List<Double>> distancesPrBeacon = distancesPrBeaconPrFixpoint.get(fixpoint);
		List<Double> distances = distancesPrBeacon.get(beaconMacAddress);
		if(distances == null) {
			distances = new ArrayList<Double>();
			distancesPrBeacon.put(beaconMacAddress, distances);
		}
		distances.add(distance);
	}

	public int getNumberOfUniqueBeacons(CalibrationFixpoint fixpoint) {
		Map<String, List<Double>> distancesPrBeacon = distancesPrBeaconPrFixpoint.get(fixpoint);
		return distancesPrBeacon.size();
	}

	public int getNumberOfSamples(CalibrationFixpoint fixpoint) {
		Map<String, List<Double>> distancesPrBeacon = distancesPrBeaconPrFixpoint.get(fixpoint);
		int samples = 0;
		for(String macAddress: distancesPrBeacon.keySet()) {
			List<Double> beaconSamples = distancesPrBeacon.get(macAddress);
			samples += beaconSamples.size();
		}
		return samples;
	}
	
	private Map<String, Map<CalibrationFixpoint, List<Double>>> distancesPrBeacon() {
		Map<String, Map<CalibrationFixpoint, List<Double>>> result = new HashMap<String, Map<CalibrationFixpoint, List<Double>>>();
		for(CalibrationFixpoint fixpoint: fixpoints) {
			Map<String, List<Double>> distancesPrBeacon = distancesPrBeaconPrFixpoint.get(fixpoint);
			for(String mac: distancesPrBeacon.keySet()) {
				List<Double> distances = distancesPrBeacon.get(mac);
				Map<CalibrationFixpoint, List<Double>> distancesPrFixpoint = result.get(mac);
				if(distancesPrFixpoint == null) {
					distancesPrFixpoint = new HashMap<CalibrationFixpoint, List<Double>>();
					result.put(mac, distancesPrFixpoint);
				}
				distancesPrFixpoint.put(fixpoint, distances);
			}
		}
		return result;
	}

	public List<FixedBeacon> estimateBeaconLocations() {
		L.d("Estimate!");
		List<FixedBeacon> fixedBeacons = new ArrayList<FixedBeacon>();
		Map<String, Map<CalibrationFixpoint, List<Double>>> calibrationDataPrBeacon = distancesPrBeacon();
		for(String mac: calibrationDataPrBeacon.keySet()) {
			FixedBeacon fixedBeacon = new FixedBeacon(mac);
			fixedBeacon.estimateLocation(calibrationDataPrBeacon.get(mac));
			fixedBeacons.add(fixedBeacon);
		}
		return fixedBeacons;
	}

	public void save(String path) {
		Log.d(this.getClass().getCanonicalName(), "Saving calibration to " + path);
	}
}
