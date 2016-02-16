/**
 * %HEADER%
 */
package net.sf.jannot.tdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jannot.Data;
import net.sf.jannot.pileup.Pile;
import net.sf.jannot.pileup.PileNormalization;
import net.sf.jannot.pileup.PileTools;
import net.sf.jannot.tdf.TDFData.TrackType;

import org.broad.igv.tdf.TDFDataset;
import org.broad.igv.tdf.TDFReader;
import org.broad.igv.tdf.TDFTile;
import org.broad.igv.track.WindowFunction;

/**
 * @author Thomas Abeel
 * 
 */
public class TDFData implements Data<Pile>, PileNormalization {

	private Logger log = Logger.getLogger(TDFData.class.getCanonicalName());
	private String chr;

	private int size = -1;

	private WindowFunction wf;

	private TDFReader tr;

	private int maxZoom;
	private TrackType trackType;
	
	public TrackType trackType(){
		return trackType;
	}

	public String label() {
		String out = tr.getLocator().replace('\\', '/');
		return out.substring(out.lastIndexOf('/') + 1);

	}
	
	enum TrackType{
		SENSEAWARECOVERAGE,COVERAGE,OTHER;
	}
	
	/**
	 * @param chr
	 * @param tr
	 */
	public TDFData(String chr, TDFReader tr) {
		this.chr = chr;
		this.tr = tr;
		trackType=TrackType.OTHER;
		try{
			TrackType.valueOf(tr.getTrackType());
		}catch(IllegalArgumentException ie){
			log.warning("JAnnot does not recognize this track type: "+tr.getTrackType());
		}
		log.info("Track type: "+tr.getTrackType());
		log.info("Datasets: "+tr.getDatasetNames());
		log.info("Groups: "+tr.getGroupNames());
		log.info("Track names: "+Arrays.toString(tr.getTrackNames()));
		maxZoom = tr.getMaxZoom();

		wf = tr.getWindowFunctions().get(0);

	}

	public List<WindowFunction> availableWindowFunctions() {
		return tr.getWindowFunctions();
	}

	public void requestWindowFunction(WindowFunction wf) {
		this.wf = wf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get(int, int)
	 */
	@Override
	public Iterable<Pile> get(int start, int end) {
		log.log(Level.FINEST, "TDF query: " + start + "\t" + end);
		if (size < 0) {
			TDFDataset data = tr.getDataset(chr, 0, wf);
			this.size = data.getTileWidth();
			log.info("Setting TDF data size to " + this.size);

		}
		if (end < 0)
			end = size;
		int tmpSize = size;
		int zoom = 0;
		while (tmpSize / 2 > (end - start + 1)) {
			tmpSize /= 2;
			zoom++;
		}

		if (zoom > maxZoom) {
			zoom = -1;
		}

		TDFDataset data = tr.getDataset(chr, zoom, wf);
		
		ArrayList<Pile> out = new ArrayList<Pile>();
		for (TDFTile tft : data.getTiles(start, end)) {
			for (int i = 0; i < tft.getSize(); i++) {
				float[] arr = new float[tft.noValues()];
				for (int j = 0; j < tft.noValues(); j++) {
					arr[j] = tft.getValue(j, i);
					if (Float.isNaN(arr[j]) || Float.isInfinite(arr[j]))
						arr[j] = 0;
				}
				int s = tft.getStartPosition(i);
				int e = tft.getEndPosition(i);
				if (e >= start && s <= end) {
					Pile tmp = PileTools.create(s, arr);
					tmp.setLength(e - s);
					out.add(tmp);
				}
			}

		}
		return out;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get()
	 */
	@Override
	public Iterable<Pile> get() {
		return get(1, size);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#canSave()
	 */
	@Override
	public boolean canSave() {
		return false;
	}

	/**
	 * @param wf2
	 * @return
	 */
	public boolean isCurrentWindowFunction(WindowFunction wf) {
		return this.wf == wf;
	}

	@Override
	public boolean supportsNormalization() {
		return true;
	}

}
