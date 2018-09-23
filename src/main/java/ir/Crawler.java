package ir;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.URLData;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static util.URLHelper.*;

public class Crawler {

    private static final int SIZE = 20000;
    private static final int SEEDURLS = 5;

    private static LinkedHashMap<String, URLData> listOfURLs = new LinkedHashMap<>();
    private static HashMap<String, Integer> URLTyre = new HashMap<>();
    private static HashMap<String, Set<String>> URLInLinks = new HashMap<>();
    private static HashMap<String, LinkedHashMap<String, URLData>> URLOutlinks = new HashMap<>();
    private static Set<String> URLsVisited = new HashSet<>();
    private static HashMap<String, Date> DomainEntryTime = new HashMap<>();

    private static int noOfURLSinQueue = 0;
    private static int noOfURLScrawled = 0;

    public static void main(String a[]) throws URISyntaxException, InterruptedException, IOException {

        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        int fileNo = 0;
        int depth = 0;

        PrintWriter corpusF = new PrintWriter("Corpus" + fileNo + ".txt", "UTF-8");
        PrintWriter inlink = new PrintWriter("InLinkF.txt", "UTF-8");
        PrintWriter outlinkF = new PrintWriter("OutLinkF.txt", "UTF-8");
        PrintWriter waves = new PrintWriter("LinkWavesF.txt", "UTF-8");

        String SeedURL1 = "http://en.wikipedia.org/wiki/List_of_maritime_disasters";
        String canSeedURL1 = canonicalizeURL(SeedURL1);
        //DomainEntryTime.put(getDomainName(canSeedURL1), new Date());
        listOfURLs.put(canSeedURL1, (new URLData(SeedURL1, 1, System.currentTimeMillis(), 0)));
        URLTyre.put(canSeedURL1, 0);

        String SeedURL2 = "http://en.wikipedia.org/wiki/Sinking_of_MV_Sewol";
        String canSeedURL2 = canonicalizeURL(SeedURL2);
        listOfURLs.put(canSeedURL2, new URLData(SeedURL2, 1, System.currentTimeMillis(), 0));
        URLTyre.put(canSeedURL2, 0);
        //DomainEntryTime.put(getDomainName(canSeedURL2), new Date());

        String SeedURL4 = "http://en.wikipedia.org/wiki/MV_Sewol";
        String canSeedURL4 = canonicalizeURL(SeedURL4);
        listOfURLs.put(canSeedURL4, new URLData(SeedURL4, 1, System.currentTimeMillis(), 0));
        URLTyre.put(canSeedURL4, 0);
        //DomainEntryTime.put(getDomainName(canSeedURL4), new Date());*/

        String SeedURL3 = "http://www.nbcnews.com/storyline/south-korea-ferry-disaster";
        String canSeedURL3 = canonicalizeURL(SeedURL3);
        listOfURLs.put(canSeedURL3, new URLData(SeedURL3, 1, System.currentTimeMillis(), 0));
        URLTyre.put(canSeedURL3, 0);
        //DomainEntryTime.put(getDomainName(canSeedURL3), new Date());

        String SeedURL5 = "http://askakorean.blogspot.com/2014/04/the-sewol-tragedy-part-i-accident.html";
        String canSeedURL5 = canonicalizeURL(SeedURL5);
        listOfURLs.put(canSeedURL5, new URLData(SeedURL5, 1, System.currentTimeMillis(), 0));
        URLTyre.put(canSeedURL5, 0);

        noOfURLSinQueue = SEEDURLS;
        int outlinkCount = 0;

        outerloop:
        while (noOfURLScrawled <= SIZE) {


            List list = new LinkedList(listOfURLs.entrySet());
            for (Iterator it = list.iterator(); ((java.util.Iterator<Map.Entry<String, URLData>>) it).hasNext(); ) {

                if (noOfURLScrawled == SIZE) {

                    break outerloop;
                }

                Map.Entry<String, URLData> entry = ((java.util.Iterator<Map.Entry<String, URLData>>) it).next();

                String canURL = entry.getKey();
                URLData canURLData = entry.getValue();
                String rawULR = canURLData.getUrl();

                if (URLsVisited.contains(stripProtocol(canURL))) {
                    listOfURLs.remove(canURL);
                    continue;
                }
                URLsVisited.add(stripProtocol(canURL));

                try {
                    Document doc;

                    //pause
                    Date date = new Date();
                    if (DomainEntryTime.get(getDomainName(canURL)) != null) {
                        long mseconds = date.getTime() - DomainEntryTime.get(getDomainName(canURL)).getTime();
                        if (mseconds < 1000) {

                            Thread.sleep(1000 - mseconds);
                        }
                    }

                    doc = Jsoup.connect(rawULR).get();
                    DomainEntryTime.put(getDomainName(canURL), new Date());

                    if (!isWebPageInEnglish(doc)) {
                        listOfURLs.remove(canURL);
                        if (URLInLinks.containsKey(canURL))
                            URLInLinks.remove(canURL);
                        continue;
                    }

                    String txtDoc = doc.text();
                    String title = doc.title();
                    String rawURL = canURLData.getUrl();

                    if (!isTextValid(txtDoc) || getScore(txtDoc) < 5) {
                        listOfURLs.remove(canURL);
                        if (URLInLinks.containsKey(canURL))
                            URLInLinks.remove(canURL);
                        continue;
                    }

                    listOfURLs.put(canURL, new URLData(rawULR, canURLData.getInDegree(), canURLData.getEntryTime(), getScore(txtDoc)));

                    outlinkF.print(canURL + "\t");

                    if (noOfURLScrawled % 500 == 0 && noOfURLScrawled != 0) {

                        corpusF.close();
                        fileNo = fileNo + 1;
                        corpusF = new PrintWriter("Corpus" + fileNo + ".txt", "UTF-8");
                    }

                    noOfURLScrawled = noOfURLScrawled + 1;

                    //writing to file
                    corpusF.print("<DOC>\n" +
                            "<DOCNO>" + canURL + "</DOCNO>\n" +
                            "<HEAD>" + title + "</HEAD>\n" +
                            "<AUTHOR>PAULOMI</AUTHOR>\n" +
                            "<URL>" + rawURL + "</URL>\n" +
                            "<DEPTH>" + depth + "</DEPTH>\n" +
                            "<TEXT>" + txtDoc + "\n</TEXT>\n" +
                            "<HTML>\n" + doc.html() + "\n</HTML>\n" +
                            "<OUTLINKS>\n");

                    URLTyre.put(canURL, depth);
                    System.out.println(noOfURLScrawled + " SIZE: " + listOfURLs.size() + " " + canURL + " popping");

                    Elements links = doc.select("a[href]");
                    LinkedHashMap<String, URLData> outlinks = new LinkedHashMap<>();

                    for (Element link : links) {

                        String hlink = link.attr("abs:href");
                        if (hlink.isEmpty()
                                || hlink.contains("www.bloomberg.com")
                                || hlink.contains("online.wsj.com")
                                || hlink.contains("www.blogger.com")
                                || hlink.contains("news.donga.com")
                                || hlink.contains("www.huffingtonpost.com")
                                || hlink.contains("www.yahoo.com/news"))
                            continue;

                        String canHlink = canonicalizeURL(hlink);
                        URL hlinkURL = new URL(canHlink);
                        boolean validity = isRobotAllowed(hlinkURL);

                        if (URLsVisited.contains(stripProtocol(canHlink)) || !isURLRequired(canHlink) || !validity) {
                            continue;
                        }


                        int indegree;
                        int score;
                        long entryTime;

                        URLData hlinkData = outlinks.get(canHlink);
                        updateInlinks(canHlink, canURL);

                        if (hlinkData == null) {

                            // Print unique outlink
                            outlinkCount = outlinkCount + 1;
                            outlinkF.print(canHlink + "\t");
                            corpusF.print(canHlink + "\n");

                            score = 0;
                            indegree = 1;
                            entryTime = System.currentTimeMillis();

                        } else {

                            score = hlinkData.getScore();
                            indegree = hlinkData.getInDegree() + 1;
                            entryTime = hlinkData.getEntryTime();
                        }

                        //if (depth < 2) {
                        outlinks.put(canHlink, new URLData(hlink, indegree, entryTime, score));
                        //}

                        //}
                    }

                    corpusF.print("</OUTLINKS>\n" + "</DOC>\n");

                    //System.out.println(outlinkCount);
                    outlinkF.print("\n");
                    URLOutlinks.put(canURL, outlinks);
                    noOfURLSinQueue = noOfURLSinQueue - 1;

                } catch (Exception e) {
                    System.out.println("Exception : " + e);
                    //e.printStackTrace();
                    listOfURLs.remove(canURL);
                    System.out.println("SIZE : " + listOfURLs.size());
                }

            }

            outlinkCount = 0;
            System.out.println("BEOFRE SORT : " + listOfURLs.size());
            listOfURLs = sortByValues(listOfURLs);
            LinkedHashMap<String, URLData> loadedListOfURLs = loadOutlinkURL(listOfURLs);
            listOfURLs.clear();

            //System.out.println("CLEARED : " + listOfURLs.size());
            listOfURLs = loadedListOfURLs;
            System.out.println("AFTER SORT AND LOAD OUTLINKS : " + listOfURLs.size());

            if(depth == 0) break outerloop;
            depth = depth + 1;
            noOfURLSinQueue = listOfURLs.size();
            URLOutlinks.clear();
        }

        for (Map.Entry<String, Set<String>> m : URLInLinks.entrySet()) {

            Set<String> al = m.getValue();
            inlink.print(m.getKey() + "\t");
            for (String s : al) {
                inlink.print(s + "\t");
            }
            inlink.print("\n");

        }

        for (Map.Entry<String, Integer> m : URLTyre.entrySet()) {
            waves.print(m.getKey() + "\t" + m.getValue() + "\n");
        }

        corpusF.flush();
        corpusF.close();

        inlink.flush();
        inlink.close();

        outlinkF.flush();
        outlinkF.close();

        waves.flush();
        waves.close();
    }

    private static LinkedHashMap<String, URLData> loadOutlinkURL(HashMap<String, URLData> map) {

        List list = new LinkedList(map.entrySet());
        LinkedHashMap<String, URLData> newURLMap = new LinkedHashMap<>();

        for (Iterator it = list.iterator(); ((java.util.Iterator<Map.Entry<String, URLData>>) it).hasNext(); ) {

            Map.Entry<String, URLData> entry = ((java.util.Iterator<Map.Entry<String, URLData>>) it).next();
            String url = entry.getKey();

            if (URLOutlinks.get(url) != null) {

                for (String outlink : URLOutlinks.get(url).keySet()) {

                    if (newURLMap.containsKey(outlink)) {
                        URLData data = newURLMap.get(outlink);
                        int indegree = data.getInDegree() + URLOutlinks.get(url).get(outlink).getInDegree();
                        newURLMap.put(outlink, new URLData(data.getUrl(), indegree, data.getEntryTime(), data.getScore()));

                    } else {
                        newURLMap.put(outlink, URLOutlinks.get(url).get(outlink));
                    }
                }
            }

        }
        return newURLMap;
    }

    private static void updateInlinks(String canHlink, String canURL) {

        if (URLInLinks.get(canHlink) == null) {
            Set<String> al = new HashSet<>();
            al.add(canURL);
            URLInLinks.put(canHlink, al);
        } else {
            Set<String> inlinks = URLInLinks.get(canHlink);
            inlinks.add(canURL);
            URLInLinks.put(canHlink, inlinks);
        }
    }
}

