/**
 * %HEADER%
 */
package net.sf.genomeview.data.source;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.genomeview.core.Configuration;
import net.sf.jannot.Annotation;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Qualifier;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.exception.SaveFailedException;
import net.sf.jannot.source.DataSource;

import org.biomoby.client.CentralImpl;
import org.biomoby.client.MobyRequest;
import org.biomoby.registry.meta.RegistriesList;
import org.biomoby.registry.meta.Registry;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobySecondaryData;
import org.biomoby.shared.MobyService;
import org.biomoby.shared.NoSuccessException;
import org.biomoby.shared.data.MobyContentInstance;
import org.biomoby.shared.data.MobyDataComposite;
import org.biomoby.shared.data.MobyDataDateTime;
import org.biomoby.shared.data.MobyDataInt;
import org.biomoby.shared.data.MobyDataJob;
import org.biomoby.shared.data.MobyDataObject;
import org.biomoby.shared.data.MobyDataObjectVector;
import org.biomoby.shared.data.MobyDataSecondaryInstance;
import org.biomoby.shared.data.MobyDataString;


public class MobySource extends DataSource {

    private static final String DEFAULT_CONTEXT = "gene";

    private String mobyRegistry;

    private String bogasGetRegionService;

    private String bogasSaveRegionService;

    private Registry registry;

    private Central worker;

    private URL bogasURL;

    private String locusId;

    private GregorianCalendar date;

    private String login = "anonymous";

    private String pass = "empty";

    boolean anonymous = true;

    public MobySource(URL bogasURL) {
        this.bogasURL = bogasURL;
        this.mobyRegistry = Configuration.get("MOBY_CENTRAL");
        this.bogasGetRegionService = Configuration.get("BOGAS_GET_REGION_SERVICE");
        this.bogasSaveRegionService = Configuration.get("BOGAS_SAVE_REGION_SERVICE");

        System.out.println("Reading from " + this.bogasGetRegionService);
        System.out.println("Saving to " + this.bogasSaveRegionService);

    }

    @Override
    public Entry[] read() throws ReadFailedException {
        setProgress(20);
        setComment("Initializing the Moby request");
        MobyRequest mr = initRequest();
        setProgress(70);
        setComment("Loading locus data from BOGAS");
        MobyDataComposite region = loadRegion(mr);
        setProgress(90);
        setComment("Constructing GenomeView data");
        setDone();
        return construct(region);
    }

    private MobyRequest createRequest(String locusId, String genome, int release, String context, GregorianCalendar date)
            throws ReadFailedException {

        registry = initializeRegistry();
        worker = initializeWorker();

        this.locusId = locusId;
        this.date = date;

        MobyService templateService = new MobyService(bogasGetRegionService);

        try {
            setProgress(30);
            setComment("Searching for web service");
            MobyService[] validServices = worker.findService(templateService);
            MobyRequest mr = new MobyRequest(worker);
            mr.setService(validServices[0]);
            mr.getService().getServiceType().setRegistry(registry);

            // supply input
            MobyContentInstance content = new MobyContentInstance();
            MobyDataJob job = new MobyDataJob();

            setProgress(40);
            setComment("Fetching data types from Moby Central");
            job.put("locus_id", new MobyDataString(locusId, registry));
            job.put("release", new MobyDataInt(release, registry));
            job.put("date", new MobyDataDateTime("date", date, registry));
            job.put("genome", new MobyDataString(genome, registry));

            content.put(job);
            mr.setInput(content);

            // supply parameters
            List<MobyDataSecondaryInstance> paramsList = new ArrayList<MobyDataSecondaryInstance>();
            paramsList.add(new MobyDataSecondaryInstance(new MobySecondaryData("context"), context));
            paramsList.add(new MobyDataSecondaryInstance(new MobySecondaryData("userid"), login));
            paramsList.add(new MobyDataSecondaryInstance(new MobySecondaryData("passwd"), pass));

            MobyDataSecondaryInstance[] params = new MobyDataSecondaryInstance[0];
            params = paramsList.toArray(params);
            mr.setSecondaryInput(params);

            return mr;
        } catch (MobyException me) {
            throw new ReadFailedException(me);
        } catch (ParserConfigurationException pce) {
            throw new ReadFailedException(pce);
        }

    }

    public MobyDataComposite loadRegion(MobyRequest mr) throws ReadFailedException {
        try {
            // sync call
            MobyContentInstance answer = mr.invokeService();
            // we're only submitting one job and expecting one single region
            // object in it.
            // So get the first job and the first object
            MobyDataJob answerJob = answer.values().iterator().next();
            MobyDataComposite region = (MobyDataComposite) answerJob.getPrimaryDataObjects()[0];

            return region;
        } catch (ParserConfigurationException e) {
            System.err.println(e);
            throw new ReadFailedException(e);
        } catch (NoSuccessException e) {
            System.err.println(e);
            throw new ReadFailedException(e);
        } catch (Exception e) {
            System.err.println(e);
            throw new ReadFailedException(e);
        }
    }

    private Entry[] construct(MobyDataComposite region) {

        // get the source
        MobyDataComposite sourceObj = (MobyDataComposite) region.get("Source");
        int taxId = ((MobyDataInt) sourceObj.get("taxID")).intValue();
        String genoType = ((MobyDataString) sourceObj.get("genotype")).getValue();
        String acc = ((MobyDataString) sourceObj.get("accession")).getValue();
        String release = ((MobyDataString) sourceObj.get("release")).getValue();

        // get the sequence
        MobyDataComposite dnaObj = (MobyDataComposite) region.get("DNASequence");
        // int dnaLength = ((MobyDataInt) dnaObj.get("Length")).intValue();
        String dnaString = ((MobyDataString) dnaObj.get("SequenceString")).getValue();

        // start and end point of the sequence
        int start = ((MobyDataInt) region.get("start")).intValue();
        // int end = ((MobyDataInt) region.get("end")).intValue();

        MobyDataObject contObjs = region.get("ElementContainer");
        ArrayList<ElementContainer> containerList = getContainerList(contObjs);

        // create the new sequence

        Entry entry = new Entry(this);

        entry.sequence.setSequence(dnaString, start);

        entry.description.addAccessionNumber(acc);
        entry.description.setTaxonomicDivision(Integer.toString(taxId));
        entry.description.addDescriptionValue("Genotype", genoType);
        entry.description.addDescriptionValue("Release", release);

        // iterate the containers and attach the features
        // TODO implement relationships!
        for (ElementContainer cont : containerList) {
            Feature feat = new Feature(this);

            feat.setType(Type.get(cont.type));

            feat.setStrand(cont.strand);

            // iterate the SubElements and add the (composite) location to the
            // feature
            SortedSet<Location> locSet = new TreeSet<Location>();
            for (SubElement sub : cont.subList) {
                Location loc = new Location(sub.start, sub.stop);
                locSet.add(loc);
            }
            feat.setLocation(locSet);

            // add all key value pairs (qualifiers) to the feature
            String evidence = cont.evidence;
            feat.addQualifier(new Qualifier("evidence", evidence));
            for (String key : cont.featureElements.keySet()) {
                feat.addQualifier(new Qualifier(key, cont.featureElements.get(key)));
            }

            entry.annotation.add(feat);

        }

        // Our newly created model will contain just one single entry
        Entry[] entries = new Entry[1];
        entries[0] = entry;

        return entries;
    }

    public void saveOwn(Entry[] entries) throws SaveFailedException {

        setProgress(20);
        setComment("Looking up the web service");
        MobyService templateService = new MobyService(bogasSaveRegionService);

        try {
            MobyService[] validServices = worker.findService(templateService);
            MobyRequest mr = new MobyRequest(worker);
            mr.setService(validServices[0]);
            mr.getService().getServiceType().setRegistry(registry);

            // supply input
            MobyContentInstance content = new MobyContentInstance();

            // create moby jobs
            setProgress(40);
            setComment("Creating the Moby objects");
            for (Entry entry : entries) {
                MobyDataJob job = new MobyDataJob();
                MobyDataObject region = createRegionObject(entry);
                job.put("locus_id", new MobyDataString(this.locusId, registry));
                job.put("date", new MobyDataDateTime("date", this.date, registry));
                job.put("region", region);

                content.put(job);
            }

            mr.setInput(content);

            // supply parameters
            List<MobyDataSecondaryInstance> paramsList = new ArrayList<MobyDataSecondaryInstance>();
            paramsList.add(new MobyDataSecondaryInstance(new MobySecondaryData("userid"), login));
            paramsList.add(new MobyDataSecondaryInstance(new MobySecondaryData("passwd"), pass));

            MobyDataSecondaryInstance[] params = new MobyDataSecondaryInstance[0];
            params = paramsList.toArray(params);
            mr.setSecondaryInput(params);

            // make call
            setProgress(70);
            setComment("Invoking web service");
            MobyContentInstance answer = mr.invokeService();
            Iterator<MobyDataJob> i = answer.values().iterator();
            for (MobyDataJob job = i.next(); i.hasNext();) {
                MobyDataInt answerStatus = (MobyDataInt) job.getPrimaryDataObjects()[0];
                if (answerStatus.intValue() == 1){
                    setDone();
                    throw new SaveFailedException("Bogas service returned save failed status.");
                }
            }

        } catch (ParserConfigurationException e) {
            System.err.println(e);
            setDone();
            throw new SaveFailedException(e);
        } catch (NoSuccessException e) {
            System.err.println(e);
            setDone();
            throw new SaveFailedException(e);
        } catch (Exception e) {
            System.err.println(e);
            setDone();
            throw new SaveFailedException(e);
        }
        
    }

    private MobyDataObject createRegionObject(Entry entry) {

        MobyDataComposite regionObj = new MobyDataComposite("Region", "region", registry);
        // return regionObj;
        MobyDataComposite sourceObj = new MobyDataComposite("Source", registry);
        String accessions = new String();
        for (String acc : entry.description.getAccessionNumbers()) {
            accessions.concat(" " + acc);
        }

        sourceObj.put("accession", new MobyDataString(accessions, registry));
        sourceObj.put("taxID", new MobyDataInt(Integer.parseInt(entry.description.getTaxonomicDivision()), registry));
        sourceObj.put("genotype", new MobyDataString(entry.description.getDescriptionValue("Genotype"), registry));
        sourceObj.put("release", new MobyDataString(entry.description.getDescriptionValue("Release"), registry));
        regionObj.put("Source", sourceObj);

        MobyDataComposite dnaObj = new MobyDataComposite("DNASequence", registry);
        dnaObj.put("Length", new MobyDataInt(entry.sequence.getSequence().length(), registry));
        // dnaObj.put("SequenceString", new
        // MobyDataString(entry.sequence.getSequence(), registry));
        dnaObj.put("SequenceString", new MobyDataString(new String(), registry));
        regionObj.put("DNASequence", dnaObj);

        regionObj.put("start", new MobyDataInt(entry.sequence.getStartPos(), registry));
        regionObj.put("end", new MobyDataInt(entry.sequence.getEndPos(), registry));

        Annotation a = entry.annotation;
        // int counter = 0;
        for (Type type : Type.values()) {
            for (Feature feat : a.getByType(type)) {
                MobyDataComposite container = new MobyDataComposite("ElementContainer", registry);
                container.put("strand", new MobyDataInt(feat.strand().getValue(), registry));
                container.put("type", new MobyDataString(feat.type().toString(), registry));

                // location
                for (Location loc : feat.location()) {
                    MobyDataComposite sub = new MobyDataComposite("SubElement", registry);
                    sub.put("start", new MobyDataInt(loc.start(), registry));
                    sub.put("stop", new MobyDataInt(loc.end(), registry));
                    container.put("SubElement", sub);
                }
                // qualifiers
                boolean evidenceSet = false;
                for (String qualKey : feat.getQualifiersKeys()) {
                    if (qualKey.equals("evidence")) {
                        String ev = new String();
                        for (Qualifier q : feat.qualifier(qualKey)) {
                            ev.concat(q.getValue());
                        }
                        container.put("evidence", new MobyDataString(ev, registry));
                        evidenceSet = true;
                    } else {
                        for (Qualifier q : feat.qualifier(qualKey)) {
                            MobyDataComposite keyVal = new MobyDataComposite("simple_key_value_pair", registry);
                            keyVal.put("key", new MobyDataString(qualKey, registry));
                            keyVal.put("value", new MobyDataString(q.getValue(), registry));
                            container.put("FeatureElement", keyVal);
                        }
                    }
                }
                if (!evidenceSet) {
                    container.put("evidence", new MobyDataString(new String(), registry));
                }
                regionObj.put("ElementContainer", container);
                // counter++;
                // FIXME saving 50 features takes long... saving 188 features
                // times out
                // if (counter>=50){
                // break;
                // }
            }
        }

        // System.out.println("Counter = "+counter);

        return regionObj;
    }

    private Map<String, String> parseParameters(URL bogasURL) {
        // host and path are not used
        String host = bogasURL.getProtocol() + "://" + bogasURL.getAuthority();
        String path = bogasURL.getPath();

        // only the query part is of importance
        String query = bogasURL.getQuery();

        Map<String, String> b = new HashMap<String, String>();
        String[] keyValues = query.split("&");
        for (String keyValue : keyValues) {
            String[] split = keyValue.split("=");
            b.put(split[0], split[1]);
        }
        return b;
    }

    private ArrayList<ElementContainer> getContainerList(MobyDataObject contObjs) {
        ArrayList<ElementContainer> containerList = new ArrayList<ElementContainer>();
        // iterate over the containers and elements
        if (contObjs instanceof MobyDataComposite) {
            // only one single elementContainer
            containerList = getContainerList((MobyDataComposite) contObjs);
        } else if (contObjs instanceof MobyDataObjectVector) {
            // we have multiple elementContainers
            containerList = getContainerList((MobyDataObjectVector) contObjs);
        }
        return containerList;
    }

    private ArrayList<ElementContainer> getContainerList(MobyDataObjectVector contObjs) {
        ArrayList<ElementContainer> contList = new ArrayList<ElementContainer>();

        for (MobyDataObject contObj : contObjs) {
            MobyDataComposite cont = (MobyDataComposite) contObj;
            contList.addAll(getContainerList(cont));
        }

        return contList;
    }

    /**
     * Takes a list of MobyDataComposite objects, extracts the actual
     * information and returns the list of ElementContainers
     * 
     * @param contObj
     * @return
     */
    private ArrayList<ElementContainer> getContainerList(MobyDataComposite contObj) {
        ElementContainer cont = new ElementContainer();
        ArrayList<ElementContainer> contList = new ArrayList<ElementContainer>();
        contList.add(cont);

        MobyDataString evidence = (MobyDataString) contObj.get("evidence");
        cont.evidence = evidence.getValue();

        MobyDataString typeObject = (MobyDataString) contObj.get("type");
        cont.type = typeObject.getValue();

        MobyDataInt strand = (MobyDataInt) contObj.get("strand");
        cont.strand = Strand.get(strand.intValue());

        // get SubElements
        MobyDataObject subs = contObj.get("SubElement");
        if (subs instanceof MobyDataObjectVector) {
            cont.subList = getSubList((MobyDataObjectVector) subs);
        } else if (subs instanceof MobyDataComposite) {
            cont.subList = getSubList((MobyDataComposite) subs);
        }

        // get featureElements
        MobyDataObject feats = contObj.get("FeatureElement");
        if (feats != null) {
            if (feats instanceof MobyDataObjectVector) {
                cont.featureElements = getKeyValueList((MobyDataObjectVector) feats);
            } else if (subs instanceof MobyDataComposite) {
                cont.featureElements = getKeyValueList((MobyDataComposite) feats);
            }
        }

        return contList;
    }

    private Map<String, String> getKeyValueList(MobyDataComposite feat) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(feat.get("key").getValue(), feat.get("value").getValue());
        return map;
    }

    private Map<String, String> getKeyValueList(MobyDataObjectVector feats) {
        Map<String, String> map = new HashMap<String, String>();
        for (MobyDataObject featObj : feats) {
            MobyDataComposite feat = (MobyDataComposite) featObj;
            map.put(feat.get("key").getValue(), feat.get("value").getValue());
        }
        return map;
    }

    private ArrayList<SubElement> getSubList(MobyDataComposite subObj) {
        SubElement sub = new SubElement();
        ArrayList<SubElement> subList = new ArrayList<SubElement>();
        subList.add(sub);

        sub.start = ((MobyDataInt) subObj.get("start")).intValue();
        sub.stop = ((MobyDataInt) subObj.get("stop")).intValue();

        return subList;
    }

    private ArrayList<SubElement> getSubList(MobyDataObjectVector subObjs) {
        ArrayList<SubElement> subList = new ArrayList<SubElement>();

        for (MobyDataObject subObj : subObjs) {
            MobyDataComposite sub = (MobyDataComposite) subObj;
            subList.addAll(getSubList(sub));
        }

        return subList;
    }

    private Central initializeWorker() {
        try {
            Central worker;
            worker = new CentralImpl(registry.getEndpoint(), registry.getNamespace());
            worker.setCacheMode(true);
            return worker;
        } catch (MobyException e) {
            System.err.println(e);
            return null;
        }
    }

    private Registry initializeRegistry() {
        // hard coded list of available registries
        RegistriesList regList = new RegistriesList();
        final String regName = mobyRegistry;
        // get the testing reg out of the list and make a new 'worker' with it.
        try {
            Registry registry = regList.get(regName);
            return registry;
        } catch (MobyException e) {
            System.err.println("Registry " + regName + " not found in hardcoded list.");
            System.err.println(e);
            return null;
        }
    }

    /**
     * Convenience class to temporary save ElementContainers
     * 
     * @author thpar
     * 
     */
    class ElementContainer {
        public String evidence = new String();

        public String type = new String();

        public Strand strand = Strand.UNKNOWN;

        public ArrayList<SubElement> subList = new ArrayList<SubElement>();

        public Map<String, String> featureElements = new HashMap<String, String>();

    }

    /**
     * Convenience class to temporary save SubElements
     * 
     * @author thpar
     */
    class SubElement {
        public int start = 0;

        public int stop = 0;
    }

    @Override
    public boolean isDestructiveSave() {
        return false;
    }

    @Override
    public String toString() {
        return (anonymous ? "(anonymous)" : "(" + login + ")") + bogasURL.toString();
    }

    private MobyRequest initRequest() throws ReadFailedException {
        // disect the bogas url and get only the relevant parameters out of it
        Map<String, String> bogas = parseParameters(bogasURL);
        String locusId = bogas.get("locus_id");
        String genome = bogas.get("genome");
        int release = Integer.parseInt(bogas.get("release"));
        String context = bogas.get("context");
        if (context == null) {
            context = DEFAULT_CONTEXT;
        }

        String dateString = bogas.get("date");
        GregorianCalendar date = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            date.setTime(df.parse(dateString));
        } catch (ParseException pe) {
            pe.printStackTrace();
            System.err.println("Could not parse date. Using current time.");
            date.setTime(new Date());
        }

        // login credentials
        if (bogas.get("login") != null && bogas.get("pass") != null) {
            login = bogas.get("login");
            pass = bogas.get("pass");
            anonymous = false;
        }

        // use these parameters to call MobyCentral and retrieve a region object
        // that contains the entries

        MobyRequest mr = createRequest(locusId, genome, release, context, date);
        return mr;
    }

}
