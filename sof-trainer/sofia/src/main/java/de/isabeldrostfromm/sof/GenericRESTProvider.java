package de.isabeldrostfromm.sof;

import java.io.Closeable;

import java.util.LinkedHashSet;

import lombok.extern.log4j.Log4j;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.ClientConfig;
import io.searchbox.client.config.ClientConstants;

import org.elasticsearch.index.query.QueryBuilder;


@Log4j
public abstract class GenericRESTProvider implements ExampleProvider, Closeable {
    public JestClient client;
    public QueryBuilder qbuilder;
    public int start;
    public int total;

    protected GenericRESTProvider(JestClient client, QueryBuilder qbuilder, int start, int total) {
        this.client = client;
        this.start = start;
        this.total = total;
        this.qbuilder = qbuilder;
    }

    protected static JestClient initClient() {
        ClientConfig conf = new ClientConfig();
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.add("http://localhost:9200");
        conf.getServerProperties().put(ClientConstants.SERVER_LIST, set);
        
        JestClientFactory factory = new JestClientFactory();
        factory.setClientConfig(conf);
        return factory.getObject();
    }
    
    @Override
    public void close() {
        try {
            client.shutdownClient();
        } catch (Throwable t) {
            log.error("Unable to close JestClient ", t);
        }
    }
}