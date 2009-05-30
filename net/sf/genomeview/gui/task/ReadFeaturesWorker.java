/**
 * %HEADER%
 */
package net.sf.genomeview.gui.task;

import java.util.HashSet;

import javax.swing.JOptionPane;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;
import net.sf.jannot.source.DataSource;

/**
 * Starts a new thread to load entry data in the background.
 * 
 * @author thpar
 * @author thabe
 * 
 */
public class ReadFeaturesWorker extends DataSourceWorker<Entry[]> {

	public ReadFeaturesWorker(DataSource source, Model model) {
		super(source, model);
	}

	Entry[] added;

	@Override
	protected Entry[] doInBackground() {
		try {
			System.out.println("reading stuff in background");
			Entry[] data = source.read();
			if (likelyMultipleAlign(data)) {
				int result=JOptionPane
						.showConfirmDialog(
								pb,
								"The data looks like a multiple alignment, would you like to load it as such?\n\n"+source,
								"Multiple alignment?",
								JOptionPane.YES_NO_OPTION);
				if(result==JOptionPane.OK_OPTION){
					System.out.println("Adding multiple alignment: "+source);
					model.addAlignment(source,data);
					 pb.done();
					return data;
				}
			}
			if(syntenic(data)){
				model.addSyntenic(source,data);
				return data;
			}else{
			System.out.println("Adding features: "+source);
			model.addFeatures(source, data);
			System.out.println(data.length);
			 pb.done();
			return data;
			}
		} catch (Exception e) {
			e.printStackTrace();
			 pb.done();
			return null;
		}

	}

	private boolean syntenic(Entry[] data) {
		return data[0].syntenic.getAll().size()>0;
			
	}

	/*
	 * Check if all sequences are the same size and if there is no other data
	 * besides the sequences.
	 */
	private boolean likelyMultipleAlign(Entry[] data) {	
		HashSet<Integer>lengths=new HashSet<Integer>();
		lengths.add(0);
		int annotation=0;
		for (Entry e : data) {
			lengths.add(e.sequence.size());
			annotation+=e.annotation.getAll().size();
			
		}
		return annotation==0&&lengths.size()==2&&data.length>1;
		
	}

}
