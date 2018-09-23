package util;



import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.print.Doc;
import javax.swing.text.html.HTMLDocument;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by paulomimahidharia on 6/16/17.
 */
public class URLHelper {
    private static PrintWriter printWriter;

    private static HashMap<String, ArrayList<String>> disallowListCache = new HashMap<String, ArrayList<String>>();

    public static void main(String args[]) throws IOException {

        printWriter = new PrintWriter("docNo.txt");

        printAllDocs();
    }

    public static String canonicalizeURL(String url) {

        if (url.startsWith("//"))
            url = "http:" + url;


        if (url.startsWith("www"))
            url = "http://" + url;

        int i = url.indexOf('/', 1 + url.indexOf('/', 1 + url.indexOf('/')));

        if(i > 0){

            String firstPart = url.substring(0, i);
            String secondPart = url.substring(i);

            firstPart = firstPart.toLowerCase();
            secondPart = secondPart.replaceAll("/+", "/");

            url = firstPart + secondPart;
        }

        if(url.contains("http")){
            url = url.replace(":80", "");
        }

        if(url.contains("https")){
            url = url.replace(":443", "");
        }


        int i1 = url.indexOf('#');
        if(i1 >= 0){
            url = url.substring(0, i1);
        }


        int i2 = url.indexOf('?');
        if(i2 >= 0){
            url = url.substring(0, i2);
        }

        url = url.trim();

        return url;
    }

    public static String getDomainName(String url) throws URISyntaxException {

        URI uri = new URI(url);
        return uri.getHost();
    }

    public static boolean isRobotAllowed(URL urlToCheck) {
        String host = urlToCheck.getHost().toLowerCase();

        // Retrieve host's disallow list from cache.
        ArrayList<String> disallowList = disallowListCache.get(host);

        // If list is not in the cache, download and cache it.
        if (disallowList == null) {
            disallowList = new ArrayList<String>();

            try {
                URL robotsFileUrl =
                        new URL("http://" + host + "/robots.txt");

                // Open connection to robot file URL for reading.
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(
                                robotsFileUrl.openStream()));

                // Read robot file, creating list of disallowed paths.
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.indexOf("Disallow:") == 0) {
                        String disallowPath =
                                line.substring("Disallow:".length());

                        // Check disallow path for comments and
                        // remove if present.
                        int commentIndex = disallowPath.indexOf("#");
                        if (commentIndex != -1) {
                            disallowPath =
                                    disallowPath.substring(0, commentIndex);
                        }

                        // Remove leading or trailing spaces from
                        // disallow path.
                        disallowPath = disallowPath.trim();

                        // Add disallow path to list.
                        disallowList.add(disallowPath);
                    }
                }

                // Add new disallow list to cache.
                disallowListCache.put(host, disallowList);
            } catch (Exception e) {
            /* Assume robot is allowed since an exception
	           is thrown if the robot file doesn't exist. */
                return true;
            }
        }

	    /* Loop through disallow list to see if the
	       crawling is allowed for the given URL. */
        String file = urlToCheck.getFile();
        for (int i = 0; i < disallowList.size(); i++) {
            String disallow = disallowList.get(i);
            if (file.startsWith(disallow)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isWebPageInEnglish(Document doc) {

        if(doc.title().equalsIgnoreCase("Ask a Korean!: The Sewol Tragedy: Part I - The Accident"))
            return true;

        Element taglang = doc.select("html").first();
        return (taglang.attr("lang").equalsIgnoreCase("en"));
    }

    private static String[] unwantedKeyword = new String[]{"facebook",
            "instagram",
            "twitter",
            "shop",
            "wikimedia",
            "ads",
            "foursquare",
            "mediawiki",
            "aenetworks",
            "contact_us",
            "plus.google.com",
            "fyi.tv",
            "email",
            "support",
            "emails",
            "wiki/special:",
            "wiki/help:",
            ":verifiability",
            "portal:featured_content",
            "portal:current_events",
            "special:random",
            "help:contents",
            "wikipedia:about",
            "wikipedia:community_portal",
            "special:recentchanges",
            "wikipedia:file_upload_wizard",
            "special",
            "wikipedia:general_disclaimer",
            "en.m.",
            "action=edit",
            "help:category",
            "international_standard_book_number",
            ".pdf",
            ".svg",
            "file:",
            "youtube",
            "\\.tv",
            "mylifetime",
            "intellectualproperty",
            "integrated_authority",
            "citation",
            ".jpg",
            ".jpeg",
            ".png",
            ".txt",
            "facebook",
            "about",
            "policy",
            "twitter",
            "maps.google",
            "map.",
            "instagram",
            "contact",
            "policy",
            "license",
            "subscribe",
            ".php",
            ".asp",
            ".aspx",
            "mailto:"};

    public static boolean isURLRequired(String str1) {
        String str = str1.toLowerCase();

        return Arrays.stream(unwantedKeyword).parallel().noneMatch(str::contains);
    }

    public static String stripProtocol(String url) {

        return url.substring(url.lastIndexOf("://") + 3);
    }

    public static boolean isTextValid(String txt) {

        String str = txt.toLowerCase();

        return  (str.contains("disaster") && (str.contains("marine") || str.contains("ferry") ||
                str.contains("maritime") || str.contains("shipwreck") || str.contains("sink") ||
                str.contains("sewol")));

        //return  (str.contains("disaster") || str.contains("marine") || str.contains("ferry") || str.contains("shipwreck") || str.contains("sewol") || str.contains("maritime"));
//                str.contains("maritime") || str.contains("shipwreck") || str.contains("sink") ||
//                str.contains("sewol")));

    }

    public static int getScore(String text) {
        String textForDoc = text.toLowerCase();
        int i = 0;

        Pattern MY_PATTERN =
                Pattern.compile("disaster|marine|ferry|sewol|shipwreak|maritime");

        Matcher m = MY_PATTERN.matcher(textForDoc);
        while (m.find()) {
            i++;
        }

        return i;
    }

    public static LinkedHashMap<String, URLData> sortByValues(HashMap<String, URLData> map) {

        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new IndegreeComparison());

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        LinkedHashMap<String, URLData> sortedHashMap = new LinkedHashMap<>();
        for (Iterator it = list.iterator(); ((java.util.Iterator<Map.Entry<String, URLData>>) it).hasNext(); ) {

            Map.Entry<String, URLData> entry = ((java.util.Iterator<Map.Entry<String, URLData>>) it).next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public static Set<String> getAllDocs() throws IOException {

        Set<String> docs = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(new File("/Users/paulomimahidharia/Desktop/IR/WebCrawler/docNo.txt")));
        String line;
        while((line = br.readLine()) != null){
            docs.add(line.trim());
        }
        return docs;
    }


    public static void printAllDocs() throws IOException {

        String DOC_PATTERN = "<DOC>\\s(.+?)</DOC>";
        String DOCNO_PATTERN = "<DOCNO>(.+?)</DOCNO>";
        //Set<String> docs = new HashSet<>();

        int fileCount = 0;
        //while (fileCount <= 39) {
            System.out.println("READING FROM : "+fileCount);

            File mFile = new File("/Users/paulomimahidharia/Desktop/IR/WebCrawler/Corpus"+fileCount+".txt");
            String str = FileUtils.readFileToString(mFile);

            Pattern DOCpattern = Pattern.compile(DOC_PATTERN, Pattern.DOTALL);
            Matcher DOCmatcher = DOCpattern.matcher(str);
            int i = 1;

            while (DOCmatcher.find()) {
                String doc = DOCmatcher.group(1);

                final Pattern DOCNOPattern = Pattern.compile(DOCNO_PATTERN);
                final Matcher DOCNOMAtcher = DOCNOPattern.matcher(doc);

                String docNo = "";
                if (DOCNOMAtcher.find()) {
                    docNo = DOCNOMAtcher.group(1).trim();
                    //docs.add(docNo);
                    printWriter.println(docNo);
                }
            //}
            //fileCount = fileCount + 1;
        }

        printWriter.close();

        //return docs;
    }


}

class IndegreeComparison implements Comparator<Map.Entry<String, URLData>> {

    public int compare(Map.Entry<String, URLData> e11, Map.Entry<String, URLData> e21) {
        URLData e1 = e11.getValue();
        URLData e2 = e21.getValue();
        if (e1.getInDegree() < e2.getInDegree()) {
            return 1;
        }
        if (e1.getInDegree() == e2.getInDegree()) {
            if (e1.getScore() < e2.getScore()) {
                return 1;
            }
            if (e1.getScore() == e2.getScore()) {
                if (e1.getEntryTime() < e2.getEntryTime()) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }

    }
}
