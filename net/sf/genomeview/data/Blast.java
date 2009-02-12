/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Blast {

    public static void nucleotideBlast(String header,String seq) {
        String url = "http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=Web&PAGE=Nucleotides&QUERY=%3E"+header+"%0A" + seq;

        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void proteinBlast(String header, String protein) {
        String url = "http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?CMD=Web&PAGE=Proteins&QUERY=%3E"+header+"%0A" + protein;

        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }}
}
