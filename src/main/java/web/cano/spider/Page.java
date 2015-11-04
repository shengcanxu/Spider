package web.cano.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import web.cano.spider.selector.Html;
import web.cano.spider.selector.Json;
import web.cano.spider.utils.UrlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Object storing extracted result and urls to fetch.<br>
 * Not thread safe.<br>
 * Main method：                                               <br>
 * {@link #getUrl()} get url of current page                   <br>
 * {@link #getHtml()}  get content of current page                 <br>
 * {@link #getPageItems()} get extract results to be used in {@link web.cano.spider.pipeline.Pipeline}<br><br>
 *
 * @author code4crafter@gmail.com <br>
 * @see web.cano.spider.downloader.Downloader
 * @see web.cano.spider.processor.PageProcessor
 * @since 0.1.0
 */
public class Page {
    private Page fatherPage;

    /**
     * 用于爬取子页面
     */
    private String parentPageKey = null;
    private int subPagesNumber = 0;

    /**
     * 用于爬取分页
     */
    private String multiplePageKey = null;
    private int multiplePageNumber = 0;
    private String multiplePageItemName;
    private int multiplePageIndex;

    private Request request;

    private List<PageItem> pageItems = new ArrayList<PageItem>();

    private Html html;
    private Json json;
    private String rawText;
    private byte[] resourceBytes;
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

    /**
     * 标识page是不是仅仅为了测试
     */
    private boolean isTest = false;

    /**
     * 标识page是不是resource，而不是html
     */
    private boolean isResource = false;

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
     * get html content of page
     *
     * @return html
     */
    public Html getHtml() {
        if (html == null) {
            if(isTest){
                html = new Html(rawText);
            }else {
                html = new Html(UrlUtils.fixAllRelativeHrefs(rawText, getUrl()));
            }
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
     */
    public void addTargetPages(List<String> pageUrls) {
        for (String s : pageUrls) {
            if (StringUtils.isBlank(s) || s.equals("#") || s.startsWith("javascript:")) {
                continue;
            }
            if (!isTest) s = UrlUtils.canonicalizeUrl(s, url.toString());
            Page page = new Page(s, this);
            addTargetPage(page);
        }
    }

    /**
     * add urls to fetch
     */
    public void addTargetPage(String url){
        if(StringUtils.isBlank(url) || url.equals("#") || url.startsWith("javascript:")){
            return;
        }
        url = UrlUtils.canonicalizeUrl(url, url.toString());
        Page page = new Page(url, this);
        addTargetPage(page);
    }


    /**
     * add pages to fetch
     *
     * @param page
     */
    public void addTargetPage(Page page) {
        synchronized (targetPages) {
            page.setFatherPage(this);
            targetPages.add(page);
        }
    }

    public void addNextPage(Page page){
        synchronized (nextPages) {
            page.setFatherPage(this);
            nextPages.add(page);
        }
    }

    public void addNextPages(List<String> pageUrls) {
        for (String s : pageUrls) {
            if (StringUtils.isBlank(s) || s.equals("#") || s.startsWith("javascript:")) {
                continue;
            }
            if (!isTest) s = UrlUtils.canonicalizeUrl(s, url.toString());
            Page page = new Page(s, this);
            addNextPage(page);
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

    public byte[] getResourceBytes() {
        return resourceBytes;
    }

    public Page setResourceBytes(byte[] resourceBytes) {
        this.resourceBytes = resourceBytes;
        return this;
    }

    public boolean isTest() {
        return isTest;
    }

    public Page setTest(boolean isTest) {
        this.isTest = isTest;
        return this;
    }

    public boolean isResource() {
        return isResource;
    }

    public Page setIsResource(boolean isResource) {
        this.isResource = isResource;
        return this;
    }

    public Page getFatherPage() {
        return fatherPage;
    }

    public Page setFatherPage(Page fatherPage) {
        this.fatherPage = fatherPage;
        return this;
    }

    public String getParentPageKey() {
        return parentPageKey;
    }

    public Page setParentPageKey(String parentPageKey) {
        this.parentPageKey = parentPageKey;
        return this;
    }

    public boolean isSubPage(){
        return this.parentPageKey != null;
    }

    public boolean hasSubPages() {
        return subPagesNumber != 0;
    }

    public Page setSubPagesNumber(int subPagesNumber) {
        this.subPagesNumber = subPagesNumber;
        return this;
    }

    public int getSubPagesNumber(){
        return this.subPagesNumber;
    }

    public String getMultiplePageKey() {
        return multiplePageKey;
    }

    public Page setMultiplePageKey(String multiplePageKey) {
        this.multiplePageKey = multiplePageKey;
        return this;
    }

    public boolean isSubMultiplePage(){
        return this.multiplePageKey != null;
    }

    public int getMultiplePageNumber() {
        return multiplePageNumber;
    }

    public Page setMultiplePageNumber(int multiplePageNumber) {
        this.multiplePageNumber = multiplePageNumber;
        return this;
    }

    public String getMultiplePageItemName() {
        return multiplePageItemName;
    }

    public boolean hasMultiplePages(){
        return multiplePageNumber != 0;
    }

    public Page setMultiplePageItemName(String multiplePageItemName) {
        this.multiplePageItemName = multiplePageItemName;
        return this;
    }

    public int getMultiplePageIndex() {
        return multiplePageIndex;
    }

    public Page setMultiplePageIndex(int multiplePageIndex) {
        this.multiplePageIndex = multiplePageIndex;
        return this;
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public Page setRefresh(boolean isRefresh) {
        this.isRefresh = isRefresh;
        return this;
    }

    public List<PageItem> getPageItems() {
        return pageItems;
    }

    public Page setPageItems(List<PageItem> pageItems) {
        this.pageItems = pageItems;
        return this;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        String fatherUrl = fatherPage == null ? "" : fatherPage.getUrl();
        jsonObject.put("fatherPage", fatherUrl);
        jsonObject.put("url", url);
        jsonObject.put("statusCode", statusCode);
        jsonObject.put("cycleTriedTimes", cycleTriedTimes);
        jsonObject.put("depth", depth);
        jsonObject.put("skip", skip);
        jsonObject.put("isRefresh", isRefresh);
        jsonObject.put("isTest", isTest);
        jsonObject.put("isResource", isResource);

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < pageItems.size(); i++) {
            jsonArray.add(pageItems.get(i));
        }
        jsonObject.put("items", jsonArray);

        System.out.println(jsonObject.toJSONString());
        return jsonObject.toJSONString();
    }

    public static Page fromJson(String json){
        JSONObject object = JSON.parseObject(json);
        String fatherUrl = object.getString("fatherPage");
        Page fatherPage = (fatherUrl == null || fatherUrl.trim().length() == 0) ? null : new Page(fatherUrl);
        Page page = new Page(object.getString("url"),fatherPage);
        page.setStatusCode(object.getInteger("statusCode"));
        page.setCycleTriedTimes(object.getInteger("cycleTriedTimes"));
        page.setDepth(object.getInteger("depth"));
        page.setSkip(object.getBoolean("skip"));
        page.setRefresh(object.getBoolean("isRefresh"));
        page.setTest(object.getBoolean("isTest"));
        page.setIsResource(object.getBoolean("isResource"));

        JSONArray jsonArray = object.getJSONArray("items");
        List<PageItem> list = new ArrayList<PageItem>();
        for(int i=0; i<jsonArray.size();i++){
            PageItem item = jsonArray.getObject(i,PageItem.class);
            list.add(item);
        }
        page.setPageItems(list);

        return page;
    }

    public PageItem getPageItemByName(String name){
        for(PageItem item : pageItems){
            if(item.getItemName().equals(name)){
                return item;
            }
        }
        return null;
    }

    public Page setPageItemValue(String name, Object value){
        for(PageItem item : pageItems){
            if(item.getItemName().equals(name)){
                item.setItemValue(value);
            }
        }
        return this;
    }

}
