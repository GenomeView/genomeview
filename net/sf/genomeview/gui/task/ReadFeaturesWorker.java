/**
 * %HEADER%
 */
package net.sf.genomeview.gui.task;

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
    protected Entry[] doInBackground()  {
        try {
            System.out.println("reading stuff in background");
            Entry[] out = model.addFeatures(source);
            System.out.println(out.length);
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        

    }

    // @Override
    // public void done(){
    // if (added!=null){
    // int count = 0;
    // for (Entry e : added){
    // if (e.sequence.size() != 0)
    // count++;
    // }
    // if (count > 0) {
    // setDoneMessage("There were "
    // + count
    // + " feature sets loaded with sequence.\n\n " +
    // "GenomeView will be unable to save the sequence and it will " +
    // "be lost if you save these files again!");
    // setDoneType(JOptionPane.WARNING_MESSAGE);
    // }
    // }
    // super.done();
    // }

}
