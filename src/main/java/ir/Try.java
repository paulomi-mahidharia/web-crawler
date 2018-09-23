package ir;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;

import static util.URLHelper.isTextValid;
import static util.URLHelper.sortByValues;

/**
 * Created by paulomimahidharia on 6/21/17.
 */
public class Try {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException

    {
        File cat = new File("/Users/paulomimahidharia/Desktop/IR/WebCrawler/final/OutLinkF.txt");

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(cat)));
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String line;
        try {
            while((line = br.readLine()) != null) {

                String[] splits = line.split("\t", 2);

                if(splits[0].trim().equalsIgnoreCase("https://en.wikipedia.org/wiki/Sinking_of_MV_Sewol")){

                    System.out.println(splits[1].trim().length());
                }

                //System.exit(0);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


}
