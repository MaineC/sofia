package de.isabeldrostfromm.sof.termvector;

import io.searchbox.AbstractAction;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Get;
import io.searchbox.core.Get.Builder;
import io.searchbox.core.Search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.google.gson.internal.StringMap;

import de.isabeldrostfromm.sof.Example;
import de.isabeldrostfromm.sof.ExampleProvider;
import de.isabeldrostfromm.sof.GenericRESTProvider;

public class RESTProvider extends GenericRESTProvider {

    private RESTProvider(JestClient client, QueryBuilder qbuilder, int start, int total) {
        super(client, qbuilder, start, total);
    }

    /**
     * Returns a document provider that provides documents that match the string value in the given field.
     * @param field the field to search for terms
     * @param value the term to search for in field
     * @param start first hit position to consider
     * @param total total number of hits to return
     * @return fully initiated document provider
     * */
    public static ExampleProvider filterInstance(String field, String value, int start, int total) {
        QueryBuilder qbuilder = QueryBuilders.termQuery(field, value);
        return instance(qbuilder, start, total);
    }

    
    /**
     * Returns a document provider that provides documents that do *NOT* match the string value in the given field.
     * @param field the field to search for terms
     * @param value the term to search for in field
     * @param start first hit position to consider
     * @param total total number of hits to return
     * @return fully initiated document provider
     * */
    public static ExampleProvider negatedFilterInstance(String field, String value, int start, int total) {
        QueryBuilder qbuilder = QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(field, value));
        return instance(qbuilder, start, total);
    }

    public static ExampleProvider instance(QueryBuilder qbuilder, int start, int total) {
        return new RESTProvider(initClient(), qbuilder, start, total); 
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Iterator<Example> iterator() {
        Search search = new Search(Search.createQueryWithBuilder(this.qbuilder.toString()));
        search.addParameter("from", this.start);
        search.addParameter("size", this.total); 
        search.addParameter("fields", "[]");
        search.addParameter("sort", "post_creation_date");
        search.addIndex("sof-sample");
        JestResult result;
        try {
            result = this.client.execute(search);
        } catch (Exception e) {
            return new RESTProviderIterator(new HashMap<String, StringMap>(), this);
        }
        RESTProviderIterator iter = new RESTProviderIterator(result.getJsonMap(), this);
        return iter;
    }

    @SuppressWarnings({ "rawtypes" })
    public Map getTermVectors(String id) {
        TermVectorAction tv = new TermVectorAction(id);
        tv.addType("sof-document");
        tv.addIndex("sof-sample");
        JestResult result;
        try {
            result = this.client.execute(tv);
        } catch (Exception e) {
            return new HashMap<String, StringMap>();
        }
        return result.getJsonMap();
    }
    
    @SuppressWarnings("rawtypes")
    public Map getMetaData(String id) {
        Builder getB = new Get.Builder(id);
        Get get = getB.index("sof-sample").type("sof-document").build();
        JestResult result;
        try {
            result = this.client.execute(get);
        } catch (Exception e) {
            return new HashMap<String, StringMap>();
        }
        return result.getJsonMap();
    }

    private class TermVectorAction extends AbstractAction {

        private String id;
        
        public TermVectorAction(String id) {
            this.id = id;
        }

        public void addIndex(String name) {
            indexName = name;
        }

        public void addType(String type) {
            typeName = type;
        }

        @Override
        public String getURI() {
            StringBuilder sb = new StringBuilder();
            sb.append(buildURI(indexName, typeName, id));
            sb.append("/" + "_termvector");
            return sb.toString();
        }

        @Override
        public String getRestMethodName() {
            return "GET";
        }

        @Override
        public String getName() {
            return "TERMVECTOR";
        }

        @Override
        public String getPathToResult() {
            return "termvector";
        }
        
    }
}
