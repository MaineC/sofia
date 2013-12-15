package de.isabeldrostfromm.sof.termvector;


import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.mahout.math.map.OpenObjectDoubleHashMap;
import org.elasticsearch.common.collect.Sets;

import com.google.common.base.Preconditions;
import com.google.gson.internal.StringMap;

import de.isabeldrostfromm.sof.Example;
import de.isabeldrostfromm.sof.ProviderIterator;

public class RESTProviderIterator extends ProviderIterator {
    @SuppressWarnings("rawtypes")
    private ArrayList<StringMap> hits;
    private int cursor = -1;
    private RESTProvider connection;
    private Vectoriser v = new Vectoriser();
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public RESTProviderIterator(Map result, RESTProvider connection) {
        Preconditions.checkNotNull(result);

        if (result.isEmpty()) {
            this.hits = new ArrayList<StringMap>();
        } else {
            StringMap obj_1 = (StringMap) result.get("hits");
            if (obj_1 == null)  System.out.println(result);
            hits = (ArrayList<StringMap>) obj_1.get("hits");
        }
        this.connection = connection;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Example parse() {
        if ( (cursor + 1) < hits.size()) {
            cursor++;
            StringMap entry = hits.get(cursor);
            String id = (String) (String) entry.get("_id");
            
            Map doc = connection.getTermVectors(id);
            StringMap<StringMap> termVectors = (StringMap) doc.get("term_vectors");
            OpenObjectDoubleHashMap<String> titleParse = new OpenObjectDoubleHashMap<String>();
            StringMap<StringMap> titleVector = (StringMap) termVectors.get("title");
            StringMap<StringMap> titleTerms = (StringMap) titleVector.get("terms");
            for (Entry<String, StringMap> titleTerm : titleTerms.entrySet()) {
                String term = titleTerm.getKey();
                StringMap<Double> freqEntry = titleTerm.getValue();
                double freq = (Double) freqEntry.get("term_freq");
                titleParse.put(term, freq);
            }
            
            StringMap<StringMap> bodyVectors = (StringMap) termVectors.get("body");
            StringMap<StringMap> bodyTerms = (StringMap) bodyVectors.get("terms");
            OpenObjectDoubleHashMap<String> bodyParse = new OpenObjectDoubleHashMap<String>();
            for (Entry<String, StringMap> bodyTerm : bodyTerms.entrySet()) {
                String term = bodyTerm.getKey();
                StringMap<Double> freqEntry = bodyTerm.getValue();
                double freq = (Double) freqEntry.get("term_freq");
                bodyParse.put(term, freq);
            }

            Map docMeta = connection.getMetaData(id);
            
            StringMap<String> source = (StringMap)docMeta.get("_source");
            String state = source.get("open_status");
            String reputation = source.get("reputation_at_post_creation");
            String tag1 = source.get("tag_1");
            String tag2 = source.get("tag_2");
            String tag3 = source.get("tag_3");
            String tag4 = source.get("tag_4");
            String tag5 = source.get("tag_5");
            Set<String> tags = Sets.newHashSet(tag1, tag2, tag3, tag4, tag5);

            ParsedDocument parsedDoc = ParsedDocument.of(bodyParse, state, titleParse, Double.parseDouble(reputation), tags);
            return Example.of(v.vectorise(parsedDoc), state);
        }
        return null;
    }

}
