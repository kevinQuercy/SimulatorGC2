package data;

import java.util.Random;

/** @file
 * 
 * Simulate one container
 */

public class ContainerSimu {
	private static final int maxWeight = 200; // kg
	private static final int minAddWeight = 20; // kg
	private static final int maxAddWeight = maxWeight; // kg
	private static final int maxVolume = 150; // L
	private static final int minAddVolume = 20; // L
	private static final int maxAddVolume = maxVolume; // L
	
	private int containerId;
	private int weight; // kg
	private int volume; // L

	public ContainerSimu(int containerId) {
		super();
		this.containerId = containerId;
		empty();
	}
	
	public int getContainerId() {
		return containerId;
	}

	public int getWeight() {
		return weight;
	}

	public int getVolume() {
		return volume;
	}

	public int getVolumeMax() {
		return maxVolume;
	}

	public void randomFill(Random random) {
		// add some weight
		weight += minAddWeight + random.nextInt(maxAddWeight-minAddWeight);
		if (weight > maxWeight) weight = maxWeight;
		
		// add some volume
		volume += minAddVolume + random.nextInt(maxAddVolume-minAddVolume);
		if (volume > maxVolume) volume = maxVolume;
	}
	
	public void empty() {
		weight = 0;
		volume = 0;
	}
}
