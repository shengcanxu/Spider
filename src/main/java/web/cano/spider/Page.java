package web.cano.spider;

import org.apache.commons.lang3.StringUtils;
import web.cano.spider.selector.Html;
import web.cano.spider.selector.Json;
import web.cano.spider.utils.UrlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Object storing extracted result and urls to fetch.<br>
 * Not thread safe.<br>
 * Main method：                                               <br>
 * {@link #getUrl()} get url of current page                   <br>
 * {@link #getHtml()}  get content of current page                 <br>
 * {@link #putField(String, Object)}  save extracted result            <br>
 * {@link #getPageItems()} get extract results to be used in {@link web.cano.spider.pipeline.Pipeline}<br><br>
 *
 * @author code4crafter@gmail.com <br>
 * @see web.cano.spider.downloader.Downloader
 * @see web.cano.spider.processor.PageProcessor
 * @since 0.1.0
 */
public class Page {
    private Page fatherPage;

    private Request request;

    private PageItems pageItems = new PageItems(this);

    private Html html;

    private Json json;

    private String rawText;

    private String url;

    private int statusCode;

    private boolean needCycleRetry;

    private List<Page> targetPages = new ArrayList<Page>();
    private List<Page> nextPages = new ArrayList<Page>();

    private int depth =0;

    private Page() {

    }

    public Page(String  url){
        this(url,null);
    }

    public Page(String url, Page fatherPage){
        if (StringUtils.isBlank(url) || url.equals("#")) {
            this.url = null;
        }else {
            this.url = url;
        }

        this.fatherPage = fatherPage;
    }

    public Page setSkip(boolean skip) {
        pageItems.setSkip(skip);
        return this;

    }

    /**
     * depth of the page in parsing list
     * @return
     */
    public int getDepth(){
        return this.depth;
    }

    public void setDepth(int depth){
        this.depth = depth;
    }

    /**
     * store extract results
     *
     * @param key
     * @param field
     */
    public void putField(String key, Object field) {
        pageItems.putItem(key, field);
    }

    /**
     * get html content of page
     *
     * @return html
     */
    public Html getHtml() {
        if (html == null) {
            html = new Html(UrlUtils.fixAllRelativeHrefs(rawText, request.getUrl()));
        }
        return html;
    }

    /**
     * get json content of page
     *
     * @return json
     * @since 0.5.0
     */
    public Json getJson() {
        if (json == null) {
            json = new Json(rawText);
        }
        return json;
    }

    /**
     * @param html
     * @deprecated since 0.4.0
     * The html is parse just when first time of calling {@link #getHtml()}, so use {@link #setRawText(String)} instead.
     */
    public void setHtml(Html html) {
        this.html = html;
    }

    public List<Page> getTargetPages() {
        return targetPages;
    }

    public List<Page> getNextPages() {
        return nextPages;
    }

    /**
     * add urls to fetch
     *
     * @param requests
     */
    public void addTargetRequests(List<String> requests) {
        synchronized (targetPages) {
            for (String s : requests) {
                if (StringUtils.isBlank(s) || s.equals("#") || s.startsWith("javascript:")) {
                    continue;
                }
                s = UrlUtils.canonicalizeUrl(s, url.toString());
                targetPages.add(new Page(s,this));
            }
        }
    }


    /**
     * add page to fetch
     *
     * @param page
     */
    public void addPagesToScheduler(Page page) {
        if (page.getUrl() == null) {
            return;
        }
        synchronized (targetPages) {
            targetPages.add(page);
        }
    }

    /**
     * add pages to fetch
     *
     * @param page
     */
    public void addTargetPage(Page page) {
        synchronized (targetPages) {
            targetPages.add(page);
        }
    }

    public void addNextPage(Page page){
        synchronized (nextPages) {
            nextPages.add(page);
        }
    }

    /**
     * get url of current page
     *
     * @return url of current page
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * get request of current page
     *
     * @return request
     */
    public Request getRequest() {
        return request;
    }

    public boolean isNeedCycleRetry() {
        return needCycleRetry;
    }

    public void setNeedCycleRetry(boolean needCycleRetry) {
        this.needCycleRetry = needCycleRetry;
    }

    public void setRequest(Request request) {
        this.request = request;
        //this.pageItems.setRequest(request);
    }

    public PageItems getPageItems() {
        return pageItems;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getRawText() {
        return rawText;
    }

    public Page setRawText(String rawText) {
        this.rawText = rawText;
        return this;
    }

    public Page getFatherPage() {
        return fatherPage;
    }

    public void setFatherPage(Page fatherPage) {
        this.fatherPage = fatherPage;
    }

    @Override
    public String toString() {
        return "Page{" +
                "request=" + request +
                ", resultItems=" + pageItems +
                ", rawText='" + rawText + '\'' +
                ", url=" + url +
                ", statusCode=" + statusCode +
                ", targetPages=" + targetPages +
                '}';
    }
}
