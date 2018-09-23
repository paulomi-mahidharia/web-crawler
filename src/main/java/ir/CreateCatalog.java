package ir;

/**
 * Created by paulomimahidharia on 6/21/17.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class CreateCatalog {

    public static void main(String[] args) throws IOException {
        printCatalog("/Users/paulomimahidharia/Desktop/IR/WebCrawler/InLinkF.txt", "InLinkFcat.txt");
        //printCatalog("/Users/paulomimahidharia/Desktop/IR/WebCrawler/final/OutLinkF.txt", "final/OutLinkFcat.txt");
        //printCatalog("/Users/paulomimahidharia/Desktop/IR/WebCrawler/final/LinkWavesF.txt", "final/LinkWavesFcat.txt");
    }

    private static void printCatalog(String fileName, String catalogName) throws FileNotFoundException, UnsupportedEncodingException {

        File cat = new File(fileName);
        HashMap<String, ArrayList<Integer>> finalCat = createCatalog(cat);

        PrintWriter writer = new PrintWriter(catalogName, "UTF-8");

        for (Map.Entry m : finalCat.entrySet()) {
            ArrayList<Integer> al = (ArrayList<Integer>) m.getValue();
            writer.println(m.getKey() + " " + al.get(0) + " " + al.get(1));
        }

        writer.close();
    }


    private static HashMap<String,ArrayList<Integer>>  createCatalog(File FileName) {
        System.out.println("creating cat");
        HashMap<String,ArrayList<Integer>> Offset = new HashMap<String,ArrayList<Integer>>();


        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(FileName)));
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String line;
        int start = 0;
        int end = 0;
        try {
            while( (line = br.readLine()) != null){
                int lengthOfLine = line.length();
                end = start + lengthOfLine;
                String[] IdAndIl =	line.split("\t");

                String key1 = (String)(IdAndIl[0]);
                if(key1.equals("https://en.wikipedia.org/wiki/MV_Sewol"))
                    System.out.println(line);
                ArrayList<Integer> arraylist = new ArrayList<Integer>();
                arraylist.add(start);
                arraylist.add(end);


                Offset.put(key1, arraylist);
                start = end + 1;

            }
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("creating cat done");

        return Offset;
    }
}
