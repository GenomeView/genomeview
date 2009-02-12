/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.menu.selection.NCBIdnaBlastAction;
import net.sf.genomeview.gui.menu.selection.NCBIproteinBlastAction;
import net.sf.jannot.Feature;
import net.sf.jannot.utils.SequenceTools;
import be.abeel.util.ExtensionManager;

public class SequenceViewDialog extends JDialog implements Observer {

    private static final long serialVersionUID = 1L;

    private Model model;

    private JTextArea sequenceText = new JTextArea();

    private int exitCode = 1;

    public final static int BUTTON_CLOSE = 0;

    private final static int NUC_MODE = 0;

    private final static int PROT_MODE = 1;

    private static final String TO_PROT_CAPTION = "Protein view";

    private static final String TO_NUC_CAPTION = "Nucleotide view";

    private int viewMode = NUC_MODE;

    private static final String LINE_BREAK = System.getProperty("line.separator");

    private Map<Feature, String> nucList;

    private Map<Feature, String> protList;

    private final JButton toggleButton = new JButton(TO_PROT_CAPTION);

    public SequenceViewDialog(final Model model) {
        // this.setModal(true);

        this.setLayout(new BorderLayout());
        this.setTitle("Sequence view");
        this.setAlwaysOnTop(true);
        this.model = model;
        model.addObserver(this);

        Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        Border bevelBorder = BorderFactory.createBevelBorder(1);
        Border buttonBorder = BorderFactory.createCompoundBorder(bevelBorder, emptyBorder);

        JScrollPane scroll = new JScrollPane(sequenceText);
        sequenceText.setLineWrap(true);
        sequenceText.setColumns(50);
        sequenceText.setRows(30);
        sequenceText.setEditable(false);

        fillWithNucleotides();

        // buttons

        JButton closeButton = new JButton("Close");
        JButton exportButton = new JButton("Export as Fasta...");

        JButton clipboardButton = new JButton("Copy to clipboard");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createGlue());

        buttonPanel.add(toggleButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(exportButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(exportButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(new JButton(new NCBIdnaBlastAction(model)));
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(new JButton(new NCBIproteinBlastAction(model)));
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(clipboardButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(closeButton);
        buttonPanel.setBorder(buttonBorder);

        this.getRootPane().setDefaultButton(closeButton);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitCode = SequenceViewDialog.BUTTON_CLOSE;
                dispose();

            }

        });

        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewMode = (viewMode + 1) % 2;
                update(null, null);

            }

        });

        clipboardButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringWriter sw = new StringWriter();
                if (viewMode == NUC_MODE) {
                    writeNucs(new PrintWriter(sw));
                } else if (viewMode == PROT_MODE) {
                    writeProts(new PrintWriter(sw));
                }
                StringSelection ss = new StringSelection(sw.toString());
                clip.setContents(ss, null);

            }

        });
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileDialog = new JFileChooser();
                fileDialog.showSaveDialog(model.getParent());
                File save = fileDialog.getSelectedFile();
                if (save != null) {
                    save = ExtensionManager.extension(save, "fasta");
                    // TODO wrap on 80 chars
                    // create content and write to file
                    try {
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(save)));
                        if (viewMode == NUC_MODE) {
                            writeNucs(writer);
                        } else if (viewMode == PROT_MODE) {
                            writeProts(writer);
                        }
                        writer.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

            }

        });

        this.add(scroll, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        this.pack();
        StaticUtils.right(this, model.getParent());

    }

    private void fillWithNucleotides() {
        if (nucList == null) {
            nucList = createNucList();
        }
        String nucs = new String();
        for (Feature feat : nucList.keySet()) {
            nucs += ">" + feat.toString();
            nucs += LINE_BREAK;
            nucs += nucList.get(feat);
            nucs += LINE_BREAK + LINE_BREAK;
        }
        this.sequenceText.setText(nucs);
    }

    private Map<Feature, String> createNucList() {
        Map<Feature, String> newList = new HashMap<Feature, String>();
        // String nucs = new String();
        Set<Feature> feats = model.getFeatureSelection();
        for (Feature feat : feats) {
            String nucs = SequenceTools.extractSequence(model.getSelectedEntry().sequence, feat);
            // SymbolList symbols = feat.getSymbols();
            // nucs = symbols.seqString();
            newList.put(feat, nucs);
        }
        return newList;
    }

    private Map<Feature, String> createProtList() {
        Map<Feature, String> newList = new HashMap<Feature, String>();
        Set<Feature> feats = model.getFeatureSelection();
        for (Feature feat : feats) {
            String seq = SequenceTools.extractSequence(model.getSelectedEntry().sequence, feat);
            String prots = SequenceTools.translate(seq);

            newList.put(feat, prots);
        }
        return newList;
    }

    private void fillWithProteins() {
        if (protList == null) {
            protList = createProtList();
        }
        String prots = new String();
        for (Feature feat : protList.keySet()) {
            prots += ">" + feat.toString();
            prots += LINE_BREAK;
            prots += protList.get(feat);
            prots += LINE_BREAK + LINE_BREAK;
        }
        this.sequenceText.setText(prots);
    }

    private void writeProts(PrintWriter writer) {
        if (protList == null) {
            protList = createProtList();
        }
        for (Feature feat : protList.keySet()) {
            writer.println(">" + feat.toString());
            String prots = protList.get(feat);
            int cursor = 0;
            int line = 80;
            while (cursor < prots.length()) {
                int offset = Math.min(80, prots.length() - cursor);
                writer.println(prots.substring(cursor, cursor + offset));
                cursor += line;
            }
            writer.println();
        }
    }

    private void writeNucs(PrintWriter writer) {
        if (nucList == null) {
            nucList = createNucList();
        }
        for (Feature feat : nucList.keySet()) {
            writer.println(">" + feat.toString());
            String nucs = nucList.get(feat);
            int cursor = 0;
            int line = 80;
            while (cursor < nucs.length()) {
                int offset = Math.min(80, nucs.length() - cursor);
                writer.println(nucs.substring(cursor, cursor + offset));
                cursor += line;
            }
            writer.println();
        }
    }

    public int showSequenceViewDialog() {
        this.setVisible(true);
        return exitCode;
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        nucList = null;
        protList = null;
        if (viewMode == NUC_MODE) {
            fillWithNucleotides();
            toggleButton.setText(TO_PROT_CAPTION);
        } else {
            fillWithProteins();
            toggleButton.setText(TO_NUC_CAPTION);
        }
        highlight(sequenceText);

    }

    // Highlight the occurrences of the word "public"

    // Creates highlights around all occurrences of pattern in textComp
    private void highlight(JTextComponent textComp) {
        // First remove all old highlights
        removeHighlights(textComp);

        try {
            Highlighter hilite = textComp.getHighlighter();
            Document doc = textComp.getDocument();
            String text = doc.getText(0, doc.getLength());
            int pos = 0;

            // Search for pattern
            while ((pos = text.indexOf("M", pos)) >= 0) {
                // Create highlighter using private painter and apply around
                // pattern
                hilite.addHighlight(pos, pos + 1, new MyHighlightPainter(Color.green));
                pos++;
            }
            // Search for pattern
            while ((pos = text.indexOf("*", pos)) >= 0) {
                // Create highlighter using private painter and apply around
                // pattern
                hilite.addHighlight(pos, pos + 1, new MyHighlightPainter(Color.RED));
                pos++;
            }
        } catch (BadLocationException e) {
        }
    }

    // A private subclass of the default highlight painter
    class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public MyHighlightPainter(Color color) {
            super(color);
        }
    }

    // Removes only our private highlights
    public void removeHighlights(JTextComponent textComp) {
        Highlighter hilite = textComp.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (int i = 0; i < hilites.length; i++) {
            if (hilites[i].getPainter() instanceof MyHighlightPainter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }

}
