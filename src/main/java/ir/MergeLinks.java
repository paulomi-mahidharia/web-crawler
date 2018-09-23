package ir;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import static util.URLHelper.getAllDocs;

/**
 * Created by paulomimahidharia on 7/3/17.
 */
public class MergeLinks {

    private final static String OUTLINK = "mergedOutlinks.txt";
    private final static String INLINK = "mergedInlinks.txt";

    private static PrintWriter inlinkWriter;
    private static PrintWriter outlinkWriter;

    private static Settings settings;

    private static Client client;

    private static int indLinks = 0;



    public static void main(String args[]) throws IOException {

        //Save merged links
        getMergedLinkGraph();

        //Get unique count
        getUniqueLinks();
    }

    public static void getUniqueLinks() throws IOException {

        settings = Settings.builder().put("cluster.name", "paulbiypri").build();

        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));

        Set<String> docs = getAllDocs();

        for(String doc: docs) {

            doc = doc.trim().replace("https", "http");

            QueryBuilder qb = QueryBuilders.matchQuery("_id", doc);

            SearchResponse scrollResp = client.prepareSearch("bpp")
                    .setQuery(qb).execute().actionGet();

            if (scrollResp.getHits().getHits().length == 1) {

                SearchHit result = scrollResp.getHits().getHits()[0];

                List author = (List) result.getSource().get("author");

                if (author.contains("Paulomi") && author.size() == 1) {
                    indLinks = indLinks + 1;
                }
            }
        }

        System.out.println(indLinks);
    }

    public static void getMergedLinkGraph() throws UnknownHostException, FileNotFoundException {

        settings = Settings.builder().put("cluster.name", "paulbiypri").build();

        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));


        inlinkWriter = new PrintWriter(INLINK);
        outlinkWriter = new PrintWriter(OUTLINK);

         SearchResponse scrollResp = client
                .prepareSearch("bpp")
                .setSize(10000)
                .setScroll(new TimeValue(60000))
                .execute()
                .actionGet();

        String scrollId;
        int totalHits = scrollResp.getHits().getHits().length;

        updateMergedLinkGraph(scrollResp.getHits().getHits());

        if (totalHits > 10000) {
            scrollId = scrollResp.getScrollId();
            while (totalHits > 10000) {

                scrollResp = client
                        .prepareSearchScroll(scrollId)
                        .setScroll(new TimeValue(60000))
                        .execute()
                        .actionGet();

                updateMergedLinkGraph(scrollResp.getHits().getHits());

                totalHits = totalHits - 10000;
            }

        }

    }

    private static void updateMergedLinkGraph(SearchHit[] hits) {

        for (SearchHit result : hits) {

            String docId = result.getId();
            List inlinks = (List) result.getSource().get("in_links");
            List outlinks = (List) result.getSource().get("out_links");

            //write inlinks
            inlinkWriter.print(docId + "=");
            for (Object inlink : inlinks) {
                inlinkWriter.print(inlink.toString() + "\t");
            }
            inlinkWriter.print("\n");

            //write outlinks
            outlinkWriter.print(docId + "=");
            for (Object outlink : outlinks) {
                outlinkWriter.print(outlink.toString() + "\t");
            }
            outlinkWriter.print("\n");
        }
    }
}
