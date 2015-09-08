package web.cano.spider;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import web.cano.spider.utils.HttpConstant;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object contains url to crawl.<br>
 * It contains some additional information.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
public class Request implements Serializable {

    private static final long serialVersionUID = 2062192774891352043L;

    private int statusCode = 0;
    private HttpHost proxy;

    private String url;

    private String method;


    private Page page;

    /**
     * store the namevaluepair for post request
     */
    private List<NameValuePair> postData;

    /**
     * store the content extracted from parseurl page
     */
    private Map<String, String> contents;

    private Request() {
    }

    public Request(Page page) {
        this(page,false);
    }

    public Request(Page page,boolean isPost){
        if(isPost){
            this.url = page.getUrl();
            this.setMethod(HttpConstant.Method.POST);
        }else{
            this.url = page.getUrl();
        }

        this.page = page;
    }

//    public Request(String url, int depth){
//        this.url = url;
//        this.depth = depth;
//    }


    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Map<String, String> getContents() {
        return contents;
    }

    public Request addContents(Map<String, String> content){
        if(contents == null){
            contents = new HashMap<String, String>();
        }
        contents.putAll(content);
        return this;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request request = (Request) o;

        if (!url.equals(request.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * The http method of the request. Get for default.
     * @return httpMethod
     * @see web.cano.spider.utils.HttpConstant.Method
     * @since 0.5.0
     */
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * return null if request method is not post
     * @return
     */
    public NameValuePair[] getPostData() {
        if(method == HttpConstant.Method.POST) {
            return (NameValuePair[]) postData.toArray();
        }else{
            return null;
        }
    }

    public void addPostData(String name, String value) {
        NameValuePair nameValuePair = new BasicNameValuePair(name,value);
        this.postData.add(nameValuePair);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Request setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public HttpHost getProxy() {
        return proxy;
    }

    public Request setProxy(HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }


    @Override
    public String toString() {
        return "Request{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
