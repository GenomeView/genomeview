/**
 * %HEADER%
 */
package net.sf.jannot.bigwig;

import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.jannot.Data;
import net.sf.jannot.pileup.Pile;
import net.sf.jannot.pileup.PileNormalization;
import net.sf.jannot.pileup.PileTools;

import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BBZoomLevelHeader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.RPChromosomeRegion;
import org.broad.igv.bbfile.WigItem;
import org.broad.igv.bbfile.ZoomDataRecord;
import org.broad.igv.bbfile.ZoomLevelIterator;

/**
 * @author Thomas Abeel
 * 
 */
public class BigWigData implements Data<Pile>, PileNormalization {

	private Logger log = Logger.getLogger(BigWigData.class.getCanonicalName());
	private String chr;

	// public int zoom = 0;
	//
	 private int size=-1;

	// private WindowFunction wf;

	private BBFileReader tr;

	// private int maxZoom;

	public String label() {
		String out = tr.getLocator().toString().replace('\\', '/');
		return out.substring(out.lastIndexOf('/') + 1);

	}

	private int[] zoomReductionLevels = null;

	/**
	 * @param chr
	 * @param tr
	 */
	public BigWigData(String chr, BBFileReader tr) {
		this.chr = chr;
		this.tr = tr;

		zoomReductionLevels=new int[tr.getZoomLevelCount()+1];
		zoomReductionLevels[0]=5;
		for (BBZoomLevelHeader header : tr.getZoomLevels().getZoomLevelHeaders()) {
			System.out.println("$$" + header.getZoomLevel() + "\t" + header.getReductionLevel());
			zoomReductionLevels[header.getZoomLevel()] = header.getReductionLevel();
			// header.print();
		}
		RPChromosomeRegion b=tr.getChromosomeBounds(tr.getChromosomeID(chr),tr.getChromosomeID(chr));
		size=b.getEndBase();
		System.out.println(chr+"\t"+b.getStartBase()+"\t"+b.getEndBase());
//		System.out.println(tr.getChromosomeBounds());
//		System.out.println(tr.getChromosomeRegions());
		// /maxZoom = tr.getMaxZoom();
		// for (String s : tr.getDatasetNames()) {
		// String[] arr = s.split("/");
		// System.out.println("Checking: "+s);
		// if (!arr[2].equals("raw")) {
		// int currentZoom = Integer.parseInt(arr[2].substring(1));
		// if (currentZoom > maxZoom)
		// maxZoom = currentZoom;
		// }
		//
		// }

		// wf = tr.getWindowFunctions().get(0);
		// TDFDataset data = tr.getDataset(chr, 0, wf);
		// log.log(Level.INFO,"nTiles: " + data.getNumberOfTiles());
		// log.log(Level.INFO,"tileSize: " + data.getTileWidth());
		//
		// this.size = data.getTileWidth();
		// log.log(Level.INFO,"Zooms: ");
		// int tmpSize = this.size;
		// int tmpZoom = 0;
		// while (tmpZoom <= maxZoom) {
		// log.log(Level.INFO,"Z" + tmpZoom + "\t" + tmpSize);
		// tmpSize /= 2;
		// tmpZoom++;
		// }

	}

	// public List<WindowFunction> availableWindowFunctions() {
	// return tr.getWindowFunctions();
	// }

	// public void requestWindowFunction(WindowFunction wf){
	// this.wf=wf;
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get(int, int)
	 */
	@Override
	public Iterable<Pile> get(int start, int end) {
		// if(size<0)
		// setSize();
		// int tmpSize = size;
		// int zoom = 0;
		int idx = 0;
		while ((end - start + 1) / 400 > zoomReductionLevels[idx])
			idx++;
		if(idx>0)
			idx--;
//		System.out.println("ZL: "+idx+"\t"+zoomReductionLevels[idx]);
		// while (tmpSize / 2 > (end - start + 1)) {
		// tmpSize /= 2;
		// zoom++;
		// }
		//
		// if (zoom > maxZoom) {
		// zoom = -1;
		// }

		// TDFDataset data = tr.getDataset(chr, zoom, wf);
		
		ArrayList<Pile> out = new ArrayList<Pile>();
		
		if(idx>0)
			fillZoom(out,start,end,idx);
		else
			fillWig(out,start,end);
		

		// for (TDFTile tft : data.getTiles(start, end)) {
		// for (int i = 0; i < tft.getSize(); i++) {
		// int s = tft.getStartPosition(i);
		// int e = tft.getEndPosition(i);
		// float vf = tft.getValue(0, i);
		// float vr=0;
		// if(tft.noValues()>1)
		// vr = tft.getValue(1, i);
		// if(Float.isNaN(vf)||Float.isInfinite(vf))
		// vf=0;
		// if(Float.isNaN(vr)||Float.isInfinite(vr))
		// vr=0;
		// if (e > start && s < end) {
		// Pile tmp = new Pile(s, vf, vr, null);
		// tmp.setLen(e - s);
		// out.add(tmp);
		// }
		// }
		//
		// }
		return out;

	}

	private void fillZoom(ArrayList<Pile> out, int start, int end,int zoom) {
		ZoomLevelIterator zlIter = tr.getZoomLevelIterator(zoom,chr, start, chr, end, false);
		while (zlIter.hasNext()) {
			ZoomDataRecord rec = zlIter.next();
			int n = rec.getBasesCovered();
			if (n > 0) {
				Pile tmp=PileTools.create(rec.getChromStart(), rec.getMeanVal());
//				DoublePile tmp = new DoublePile(rec.getChromStart(), rec.getMeanVal(), 0, null);
				tmp.setLength(rec.getChromEnd() - rec.getChromStart());
//				System.out.println("Base covered = " + n);
//				double mean = rec.getSumData() / n;
//				System.out.println(rec.getChromName() + "\t" + rec.getChromStart() + "\t" + rec.getChromEnd() + "\t"
//						+ mean);
				out.add(tmp);
			}

		}
		
	}

	private void fillWig(ArrayList<Pile> out, int start, int end) {
		BigWigIterator zlIter = tr.getBigWigIterator( chr, start, chr, end, false);
		while (zlIter.hasNext()) {
			WigItem rec = zlIter.next();
			int n = rec.getEndBase()-rec.getStartBase();//.getBasesCovered();
			if (n > 0) {
//				DoublePile tmp = new DoublePile(rec.getStartBase(),rec.getWigValue(), 0, null);
				Pile tmp=PileTools.create(rec.getStartBase(),rec.getWigValue());
				tmp.setLength(n);
//				System.out.println("Base covered = " + n);
//				double mean = rec.getSumData() / n;
//				System.out.println(rec.getChromName() + "\t" + rec.getChromStart() + "\t" + rec.getChromEnd() + "\t"
//						+ mean);
				out.add(tmp);
			}

		}
		
	}

	// private void setSize() {
	// System.out.println("Setting size...");
	// TDFDataset data = tr.getDataset(chr, 0, wf);
	// this.size = data.getTileWidth();
	// }

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

	@Override
	public boolean supportsNormalization() {
		// FIXME implementing PileNormalization? Yes. Actually supporting it? No.
		return false;
	}

//	/**
//	 * @param wf2
//	 * @return
//	 */
//	public boolean isCurrentWindowFunction(WindowFunction wf) {
//		return this.wf == wf;
//	}

}
