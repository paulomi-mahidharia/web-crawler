package ir;

/**
 * Created by paulomimahidharia on 6/21/17.
 */

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static util.URLHelper.getAllDocs;

public class MergeIndex {
    private final static String DOC_PATTERN = "<DOC>\\s(.+?)</DOC>";
    private final static String DOCNO_PATTERN = "<DOCNO>(.+?)</DOCNO>";
    private final static String TEXT_PATTERN = "<TEXT>(.+?)</TEXT>";
    private final static String URL_PATTERN = "<URL>(.+?)</URL>";
    private final static String HEAD_PATTERN = "<HEAD>(.+?)</HEAD>";
    private final static String AUTHOR_PATTERN = "<AUTHOR>(.+?)</AUTHOR>";
    private final static String HTML_PATTERN_2 = "<OUTLINKS>.*</OUTLINKS>";
    private final static String HTML_PATTERN = "<HTML>(.+?)</HTML>";
    private final static String DEPTH_PATTERN = "<DEPTH>(.+?)</DEPTH>";

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        Settings settings = Settings.builder()//.put("client.transport.sniff", true)
                .put("cluster.name", "paulbiypri").build();
                //.put("cluster.name", "elasticsearch").build();

        Client client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));

        Set<String> docs = getAllDocs();
        Map<String, Set<String>> inlinkMap = new HashMap<>();

        BufferedReader in = new BufferedReader(new FileReader(new File("/Users/paulomimahidharia/Desktop/IR/WebCrawler/InLinkF.txt")));
        String inline;
        int p = 0;
        while ((inline = in.readLine()) != null) {
            String key = inline.split("\t", 2)[0];
            if (!docs.contains(key)) continue;

            try {

                System.out.println(p);
                String[] allInlinks = inline.split("\t", 2)[1].replaceAll("https", "http").trim().split("\t");
                Set<String> inlinksSet = new HashSet<>(Arrays.asList(allInlinks));
                inlinkMap.put(key, inlinksSet);

            } catch (ArrayIndexOutOfBoundsException e) {

                inlinkMap.put(key, Collections.EMPTY_SET);
            }
            p = p + 1;
        }

        System.out.println(inlinkMap.size());
        System.out.println("Wrote map");

        /*File catWave = new File("/Users/paulomimahidharia/Desktop/IR/WebCrawler/final/LinkWavesFcat.txt");
        HashMap<String, ArrayList<Integer>> CatalogWave = createCatalog(catWave);*/

        int fileCount = 0;

        while (fileCount <= 39) {

            System.out.println("FILE : " + fileCount);

            File mFile = new File("/Users/paulomimahidharia/Desktop/IR/WebCrawler/Corpus" + fileCount + ".txt");
            String str = FileUtils.readFileToString(mFile);
            //BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(mFile)));

            //&& dc < 15 add to while to restrict no odf docs
            //while ((line = br1.readLine()) != null) {
            //doc = doc + line;
            Pattern DOCpattern = Pattern.compile(DOC_PATTERN, Pattern.DOTALL);
            Matcher DOCmatcher = DOCpattern.matcher(str);
            //int i = 1;

            while (DOCmatcher.find()) {

                //if (i > 100)
                //System.exit(0);

                // Save DOC
                String doc = DOCmatcher.group(1);

                // Extract DOCNO
                final Pattern DOCNOPattern = Pattern.compile(DOCNO_PATTERN);
                final Matcher DOCNOMAtcher = DOCNOPattern.matcher(doc);

                String docNo = "";
                if (DOCNOMAtcher.find()) {
                    docNo = DOCNOMAtcher.group(1).trim();
                }

                final Pattern URLPattern = Pattern.compile(URL_PATTERN);
                final Matcher URLMAtcher = URLPattern.matcher(doc);

                String url = "";
                if (URLMAtcher.find()) {
                    url = URLMAtcher.group(1).trim();
                    url = url.replaceAll("https", "http");
                }

                final Pattern HEADPattern = Pattern.compile(HEAD_PATTERN);
                final Matcher HEADMatcher = HEADPattern.matcher(doc);

                String head = "";
                if (HEADMatcher.find()) {
                    head = HEADMatcher.group(1).trim();
                }

                final Pattern DEPTHPattern = Pattern.compile(DEPTH_PATTERN);
                final Matcher DEPTHMatcher = DEPTHPattern.matcher(doc);

                int wave = 0;
                if (DEPTHMatcher.find()) {
                    wave = Integer.parseInt(DEPTHMatcher.group(1).trim());
                }

                final Pattern AUTHPattern = Pattern.compile(AUTHOR_PATTERN);
                final Matcher AUTHMatcher = AUTHPattern.matcher(doc);

                String auth = "";
                if (AUTHMatcher.find()) {
                    auth = AUTHMatcher.group(1).trim();
                    auth = auth.charAt(0) + auth.substring(1).toLowerCase();
                    //System.out.println(auth);
                    //System.exit(0);
                }

                System.out.println(docNo);

                Set<String> outlinks1;
                try {
                    String outlinks1raw = doc.substring(doc.indexOf("<OUTLINKS>") + 11, doc.indexOf("</OUTLINKS>") - 11).replaceAll("https", "http");
                    String[] alloutlinks = outlinks1raw.split("\n");
                    outlinks1 = new HashSet<>(Arrays.asList(alloutlinks));
                } catch (StringIndexOutOfBoundsException e) {
                    outlinks1 = new HashSet<>();
                }


                // Extract TEXT
                Pattern TEXTPattern = Pattern.compile(TEXT_PATTERN, Pattern.DOTALL);
                Matcher TEXTMatcher = TEXTPattern.matcher(doc);

                String text = "";

                while (TEXTMatcher.find()) {
                    text = text + " ";
                    text = text.concat(TEXTMatcher.group(1));
                }

                Pattern HTMLPattern = Pattern.compile(HTML_PATTERN, Pattern.DOTALL);
                Matcher HTMLMatcher = HTMLPattern.matcher(doc);

                String html = "";
                while (HTMLMatcher.find()) {
                    html = HTMLMatcher.group(1);
                }

                text = text.replaceAll("\n", " ");

                //add to elastic search
                //System.out.println(i);
                //i = 1 + i;

                //System.out.println("adding to ec");

                String key = docNo;
                key = key.replaceAll("https", "http");
                //System.out.println(key);
                //String raw="";
                //if(docNo.equals("https://en.wikipedia.org/wiki/South_Jeolla_Province"))
                //raw = getInlinks(CatalogIn.get(docNo));

                //String[] inLinks1raw = getInlinks(CatalogIn.get(docNo)).replaceAll("https", "http").replace("\n", "").trim().split("\t");
                //Set<String> inLinks1 = new HashSet<>(Arrays.asList(inLinks1raw));

                Set inLinks1;
                if (inlinkMap.get(docNo) == null)
                    inLinks1 = new HashSet();
                else
                    inLinks1 = inlinkMap.get(docNo);

                Set<String> author = new HashSet<>(Arrays.asList(auth));

                //obtain the handler to elasticsearch
                QueryBuilder qb = QueryBuilders.matchQuery("_id", key);

                SearchResponse scrollResp = client.prepareSearch("bpp5")
                        .setQuery(qb).execute().actionGet();

                if (scrollResp.getHits().getHits().length == 1) {

                    SearchHit result = scrollResp.getHits().getHits()[0];

                    List inlinks2 = (List) result.getSource().get("in_links");
                    List outlinks2 = (List) result.getSource().get("out_links");
                    List Auth2 = (List) result.getSource().get("author");

                    if (inLinks1 != null) {
                        inLinks1.addAll(inlinks2);
                    }

                    if (outlinks2 != null) {
                        outlinks1.addAll(outlinks2);
                    }

                    author.addAll(Auth2);

                    UpdateRequest updateRequest = new UpdateRequest("bpp5", "document", key)
                            .doc(jsonBuilder()
                                    .startObject()
                                    //.field("docno", key)
                                    //.field("url", url)
                                    //.field("depth", wave)
                                    .field("author", author)
                                    //.field("title", head)
                                    .field("in_links", inLinks1)
                                    .field("out_links", outlinks1)
                                    //.field("text", text)
                                    .field("html_Source", html)
                                    .endObject());

                    client.update(updateRequest).actionGet();

                    //}
                } else {
                    //System.out.println("response not exists");

                    IndexRequest indexRequest = new IndexRequest("bpp5", "document", key)
                            .source(jsonBuilder()
                                    .startObject()
                                    .field("docno", key)
                                    .field("url", url)
                                    .field("depth", wave)
                                    .field("author", author)
                                    .field("head", head)
                                    .field("in_links", inLinks1)
                                    .field("out_links", outlinks1)
                                    .field("text", text)
                                    .field("html_Source", html)
                                    .endObject()
                            );

                    client.index(indexRequest).actionGet();
                }
            }

            fileCount = fileCount + 1;
        }
    }
}
