/**
 * %HEADER%
 */
package net.sf.genomeview.gui.components;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.UniqueFeatureHitDialog;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;

public class CollisionMap {
    private Map<Rectangle, ArrayList<Location>> locationMap = new HashMap<Rectangle, ArrayList<Location>>();

    private Model model;

    public CollisionMap(Model m) {
        this.model = m;
    }

    public void clear() {

        locationMap.clear();
    }

    public Location uniqueLocation(int x, int y) {

        Set<Location> locHits = locationHits(x, y);
        if (locHits.size() == 0)
            return null;
        Feature f = uniqueFeature(x, y);
        for (Location l : locHits) {
            if (l.getParent().equals(f))
                return l;
        }
        throw new RuntimeException(
                "The feature was found, but the corresponding location was missing. This should never happen.");

    }

    public Feature uniqueFeature(int x, int y) {
        Set<Feature> hits = featureHits(x, y);
        if (hits.size() == 0)
            return null;
        if (hits.size() == 1)
            return hits.iterator().next();
        return new UniqueFeatureHitDialog(hits, model).value();
    }

    public Set<Location> locationHits(int x, int y) {
        Set<Location> hits = new HashSet<Location>();
        for (Rectangle r : locationMap.keySet()) {
            if (r.contains(x, y)) {
                List<Location> pair = locationMap.get(r);
                hits.addAll(pair);
            }
        }
        return hits;
    }

    public Set<Feature> featureHits(int x, int y) {
        Set<Feature> hits = new HashSet<Feature>();
        for (Location l : locationHits(x, y)) {
            hits.add(l.getParent());
        }

        return hits;
    }

    public boolean nearBorder(int x, int y) {
        for (Rectangle rec : locationMap.keySet()) {
            double a = Math.abs(x - rec.getMinX());
            double b = Math.abs(x - rec.getMaxX());
            boolean yCheck = y >= rec.y && y <= rec.y + rec.height;
            if ((a < 2 || b < 2) && yCheck) {
                return true;
            }
        }
        return false;
    }

    public Location borderHit(int x, int y) {
        Set<Location> hits = new HashSet<Location>();
        for (Rectangle rec : locationMap.keySet()) {
            double a = Math.abs(x - rec.getMinX());
            double b = Math.abs(x - rec.getMaxX());
            boolean yCheck = y >= rec.y && y <= rec.y + rec.height;
            if ((a < 2 || b < 2) && yCheck) {
                hits.addAll(locationMap.get(rec));
            }
        }
        if (hits.size() == 0)
            return null;

        Set<Feature> fh = new HashSet<Feature>();
        for (Location l : hits) {
            fh.add(l.getParent());
        }
        Feature f;
        if (fh.size() == 1)
            f = fh.iterator().next();
        else {

            SortedSet<Feature> intersection = model.getFeatureSelection();
            intersection.retainAll(fh);

            if (intersection.size() == 1)
                f = intersection.first();
            else
                f = new UniqueFeatureHitDialog(fh, model).value();

        }
        for (Location l : hits) {
            if (l.getParent().equals(f))
                return l;
        }
        return null;
    }

    public void addLocation(Rectangle r, Location l) {
        if (!locationMap.containsKey(r))
            locationMap.put(r, new ArrayList<Location>());
        locationMap.get(r).add(l);

    }

    public boolean collision(Rectangle rectangle) {
        for (Rectangle r : locationMap.keySet()) {
            if (rectangle.intersects(r))
                return true;
        }
        return false;

    }
}
