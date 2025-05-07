package coverage;

import java.awt.geom.Point2D;
import java.util.List;

public class CoverageTestStatistics {
	public static final double C = 2.9970248;
	
	protected static final float[] ZA2 = {1.645F, 1.96F, 2.576F};
	protected static final float[] CI = {1.5F, 1.75F, 2.0F, 2.5F, 3.0F};
	
	private static final int[] cl_90 = {91, 67, 52, 33, 23};
	private static final int[] cl_95 = {129, 95, 73, 47, 33};
	private static final int[] cl_99 = {222, 164, 126, 81, 56};
	
	private enum FullConfidenceInterval {CI_1_5, CI_1_75, CI_2_0, CI_2_5, CI_3_0}
	private enum ConfidenceLevel {CL_90, CL_95, CL_99}

	private float dopplerFrequency;  // V/y
	private float fadeRate;  // in Hz
	private float confidenceLevel;
	private float fullConfidenceIntervalLength; // reliability of survey results
	private float searchRadius;
	private float targetSAR;
	private float computedSAR; //  Tp/Tt
	private float tilesPassed;
	private float tilesTotal;	
	private Point2D.Float tileDimension;
	private int samplesTaken;
	private float za2;
	
	private List<TestTile> testTiles;
	
	public float getDopplerFrequency() {
		return dopplerFrequency;
	}

	public void setDopplerFrequency(float dopplerFrequency) {
		this.dopplerFrequency = dopplerFrequency;
	}

	public float getFadeRate() {
		return fadeRate;
	}
	
	public void setFadeRate(float fadeRate) {
		this.fadeRate = fadeRate;
	}

	public float getConfidenceLevel() {
		return confidenceLevel;
	}

	public void setConfidenceLevel(float confidenceLevel) {
		this.confidenceLevel = confidenceLevel;
	}

	public float getFullConfidenceIntervalLength() {
		return fullConfidenceIntervalLength;
	}

	public void setFullConfidenceIntervalLength(float fullConfidenceIntervalLength) {
		this.fullConfidenceIntervalLength = fullConfidenceIntervalLength;
	}

	public float getSearchRadius() {
		return searchRadius;
	}

	public void setSearchRadius(float searchRadius) {
		this.searchRadius = searchRadius;
	}

	public float getTargetSAR() {
		return targetSAR;
	}

	public void setTargetSAR(float targetSAR) {
		this.targetSAR = targetSAR;
	}

	public float getComputedSAR() {
		return computedSAR;
	}

	public void setComputedSAR(float computedSAR) {
		this.computedSAR = computedSAR;
	}

	public float getTilesPassed() {
		return tilesPassed;
	}

	public void setTilesPassed(float tilesPassed) {
		this.tilesPassed = tilesPassed;
	}

	public float getTilesTotal() {
		return tilesTotal;
	}

	public void setTilesTotal(float tilesTotal) {
		this.tilesTotal = tilesTotal;
	}

	public Point2D.Float getTileDimension() {
		return tileDimension;
	}

	public void setTileDimension(Point2D.Float tileDimension) {
		this.tileDimension = tileDimension;
	}

	public int getSamplesTaken() {
		return samplesTaken;
	}

	public void setSamplesTaken(int samplesTaken) {
		this.samplesTaken = samplesTaken;
	}

	public List<TestTile> getTestTiles() {
		return testTiles;
	}

	public void setTestTiles(List<TestTile> testTiles) {
		this.testTiles = testTiles;
	}

	public CoverageTestStatistics(List<TestTile> testTiles) {
		this.testTiles = testTiles;
	}

	public int getSubsamplesRequired(FullConfidenceInterval fullConfidenceInterval, ConfidenceLevel confidenceLevel) {
		int subsamplesRequired = 0;
		switch (confidenceLevel) {
			case CL_90 :
				subsamplesRequired = cl_90[fullConfidenceInterval.ordinal()];
				break;
			case CL_95 :
				subsamplesRequired =  cl_95[fullConfidenceInterval.ordinal()];
				break;
			case CL_99 :
				subsamplesRequired =  cl_99[fullConfidenceInterval.ordinal()];
				break;
		}
		return subsamplesRequired;
	}
	
	public double getSamplesRequired() {
		return (Math.pow(za2, 2) * targetSAR * (1 - targetSAR)) / (Math.pow((fullConfidenceIntervalLength / 2), 2));
	}
	
	public void setza2(float za2) {
		this.za2 = za2;
	}
	
	public double getDistanceRequired(int waveLengths, double frequencyMHz) {
		final double waveLength = C / (frequencyMHz / 1E6);
		return waveLengths * waveLength;
	}

}
