package util;

import lombok.Getter;
import lombok.Setter;

/**
 * @author paulomimahidharia on 6/16/17.
 */
@Getter
@Setter
public class URLData {

    private String url;
    private int inDegree;
    private long entryTime;
    private int score;

    public URLData(){

    }

    public URLData(String url, int inDegree, long entryTime, int score)
    {
        this.url = url;
        this.inDegree = inDegree;
        this.entryTime = entryTime;
        this.score = score;

    }

    public String toString() {
        return "url: "+this.url+"-- indegree: "+this.inDegree;
    }

}
