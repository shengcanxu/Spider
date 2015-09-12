package web.cano.spider;

import com.alibaba.fastjson.JSON;
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
    private int cycleTriedTimes = 0;

    private List<Page> targetPages = new ArrayList<Page>();
    private List<Page> nextPages = new ArrayList<Page>();

    private int depth =0;

    private boolean skip = false;

    /**
     * true if want to parsed this request even it's already parsed
     */
    private boolean isRefresh = false;

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

    public int getCycleTriedTimes() {
        return cycleTriedTimes;
    }

    public Page setCycleTriedTimes(int cycleTriedTimes) {
        this.cycleTriedTimes = cycleTriedTimes;
        return this;
    }

    /**
     * Whether to skip the result.<br>
     * Result which is skipped will not be processed by Pipeline.
     *
     * @return whether to skip the result
     */
    public boolean isSkip() {
        return skip;
    }

    /**
     * Set whether to skip the result.<br>
     * Result which is skipped will not be processed by Pipeline.
     *
     * @param skip whether to skip the result
     * @return this
     */
    public Page setSkip(boolean skip) {
        this.skip = skip;
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
    public void putField(String key, String field) {
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


    public List<Page> getTargetPages() {
        return targetPages;
    }

    public List<Page> getNextPages() {
        return nextPages;
    }

    /**
     * add urls to fetch
     *
     * @param pageUrls
     */
    public void addTargetPage(List<String> pageUrls) {
        synchronized (targetPages) {
            for (String s : pageUrls) {
                if (StringUtils.isBlank(s) || s.equals("#") || s.startsWith("javascript:")) {
                    continue;
                }
                s = UrlUtils.canonicalizeUrl(s, url.toString());
                targetPages.add(new Page(s,this));
            }
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

    public boolean isRefresh() {
        return isRefresh;
    }

    public Page setRefresh(boolean isRefresh) {
        this.isRefresh = isRefresh;
        return this;
    }

    public void setPageItems(PageItems pageItems) {
        this.pageItems = pageItems;
    }

    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("<$Page>");
        json.append("<$fatherPage>" + fatherPage.getUrl() + "<$/fatherPage>");
        json.append("<$url>" + this.url + "<$/url>");
        json.append("<$statusCode>" + statusCode + "<$/statusCode>") ;
        json.append("<$cycleTriedTimes>" + cycleTriedTimes + "<$/cycleTriedTimes>");
        json.append("<$depth>" + depth + "<$/depth>");
        json.append("<$skip>" + skip + "<$/skip>" );
        json.append("<$isRefresh>" + isRefresh + "<$/isRefresh>");
        json.append(pageItems.toJson());
        json.append("<$/Page>");
        return json.toString();
    }

    public static Page fromJson(String json){
        String fatherPageUrl = json.substring(json.indexOf("<$fatherPage>")+13, json.lastIndexOf("<$/fatherPage>"));
        String url = json.substring(json.indexOf("<$url>")+6, json.lastIndexOf("<$/url>"));
        int statusCode = Integer.parseInt(json.substring(json.indexOf("<$statusCode>") + 13, json.lastIndexOf("<$/statusCode>")));
        int cycleTriedTimes = Integer.parseInt(json.substring(json.indexOf("<$cycleTriedTimes>") + 18, json.lastIndexOf("<$/cycleTriedTimes>")));
        int depth = Integer.parseInt(json.substring(json.indexOf("<$depth>")+8, json.lastIndexOf("<$/depth>")));
        boolean skip = Boolean.parseBoolean(json.substring(json.indexOf("<$skip>")+7, json.lastIndexOf("<$/skip>")));
        boolean isRefresh = Boolean.parseBoolean(json.substring(json.indexOf("<$isRefresh>")+12, json.lastIndexOf("<$/isRefresh>")));
        String pageItemsString = json.substring(json.indexOf("<$PageItems>"), json.lastIndexOf("<$/PageItems>")+13);

        Page fatherPage = new Page(fatherPageUrl);
        Page page = new Page(url,fatherPage);
        page.setStatusCode(statusCode);
        page.setCycleTriedTimes( cycleTriedTimes);
        page.setDepth(depth);
        page.setSkip(skip);
        page.setRefresh(isRefresh);
        PageItems pageItems = PageItems.fromJson(pageItemsString,page);
        page.setPageItems(pageItems);
        return page;
    }
}
