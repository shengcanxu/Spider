package web.cano.spider;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.proxy.ProxyPool;

import java.util.*;

/**
 * Object contains setting for crawler.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @see web.cano.spider.processor.PageProcessor
 * @since 0.1.0
 */
public class Site {

    Logger logger = LoggerFactory.getLogger(getClass());

    private String domain;

    private List<String> userAgents = new ArrayList<String>();

    private Map<String, String> defaultCookies = new LinkedHashMap<String, String>();

    private Table<String, String, String> cookies = HashBasedTable.create();

    private String charset;

    /**
     * startUrls is the urls the crawler to start with.
     */
    private List<Request> startRequests = new ArrayList<Request>();

    private int sleepTime = 5000;

    private int retryTimes = 0;

    private int cycleRetryTimes = 0;

    private int timeOut = 5000;

    /**
     * 控制是广度优先还是深度优先爬取内容
     */
    private boolean deepFirst = false;

    /**
     * 控制爬虫不爬去超过maxDeep深度的内容
     */
    private int maxDeep = 1000;

    /**
     * 路径不为空是，不爬去网络上的网站页面，而是爬去保存在路径上的网站的拷贝
     */
    private String localSiteCopyLocation;

    /**
     * 爬取到多个结果后的分隔符
     */
    private String multiValueSeparator = "@#$";

    private boolean shouldSplitToMultipleValues = false;

    private static final Set<Integer> DEFAULT_STATUS_CODE_SET = new HashSet<Integer>();

    private Set<Integer> acceptStatCode = DEFAULT_STATUS_CODE_SET;

    private Map<String, String> headers = new HashMap<String, String>();

    private HttpHost httpProxy;

	private ProxyPool httpProxyPool;
	
    private boolean useGzip = true;

    /**
     * @see web.cano.spider.utils.HttpConstant.Header
     * @deprecated
     */
    public static interface HeaderConst {

        public static final String REFERER = "Referer";
    }


    static {
        DEFAULT_STATUS_CODE_SET.add(200);
    }

    /**
     * new a Site
     *
     * @return new site
     */
    public static Site me() {
        return new Site();
    }

    /**
     * Add a cookie with domain {@link #getDomain()}
     *
     * @param name
     * @param value
     * @return this
     */
    public Site addCookie(String name, String value) {
        defaultCookies.put(name, value);
        return this;
    }

    /**
     * Add a cookie with specific domain.
     *
     * @param domain
     * @param name
     * @param value
     * @return
     */
    public Site addCookie(String domain, String name, String value) {
        cookies.put(domain, name, value);
        return this;
    }

    /**
     * set user agent
     *
     * @param userAgent userAgent
     * @return this
     */
    public Site addUserAgent(String userAgent) {
        this.userAgents.add(userAgent);
        return this;
    }

    /**
     * get cookies
     *
     * @return get cookies
     */
    public Map<String, String> getCookies() {
        return defaultCookies;
    }

    /**
     * get cookies of all domains
     *
     * @return get cookies
     */
    public Map<String,Map<String, String>> getAllCookies() {
        return cookies.rowMap();
    }

    /**
     * get user agent
     *
     * @return user agent
     */
    public String getUserAgent() {
        if(this.userAgents.size() <= 1){
            String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36";
            this.userAgents.add(userAgent);
            return userAgent;
        }else{
            Random random = new Random();
            int index = random.nextInt(this.userAgents.size());
            return this.userAgents.get(index);
        }
    }

    /**
     * get domain
     *
     * @return get domain
     */
    public String getDomain() {
        if(domain == null || domain == ""){
            logger.error("Site Domain is not set!");
        }
        return domain;
    }

    /**
     * set the domain of site.
     *
     * @param domain
     * @return this
     */
    public Site setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Set charset of page manually.<br>
     * When charset is not set or set to null, it can be auto detected by Http header.
     *
     * @param charset
     * @return this
     */
    public Site setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * get charset set manually
     *
     * @return charset
     */
    public String getCharset() {
        return charset;
    }

    public int getTimeOut() {
        return timeOut;
    }

    /**
     * set timeout for downloader in ms
     *
     * @param timeOut
     */
    public Site setTimeOut(int timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    public String getMultiValueSeparator() {
        return multiValueSeparator;
    }

    public Site setMultiValueSeparator(String multiValueSeparator) {
        this.multiValueSeparator = multiValueSeparator;
        return this;
    }

    public boolean isShouldSplitToMultipleValues() {
        return shouldSplitToMultipleValues;
    }

    public Site setShouldSplitToMultipleValues(boolean shouldSplitToMultipleValues) {
        this.shouldSplitToMultipleValues = shouldSplitToMultipleValues;
        return this;
    }

    /**
     * Set acceptStatCode.<br>
     * When status code of http response is in acceptStatCodes, it will be processed.<br>
     * {200} by default.<br>
     * It is not necessarily to be set.<br>
     *
     * @param acceptStatCode
     * @return this
     */
    public Site setAcceptStatCode(Set<Integer> acceptStatCode) {
        this.acceptStatCode = acceptStatCode;
        return this;
    }

    /**
     * get acceptStatCode
     *
     * @return acceptStatCode
     */
    public Set<Integer> getAcceptStatCode() {
        return acceptStatCode;
    }


    /**
     * Set the interval between the processing of two pages.<br>
     * Time unit is micro seconds.<br>
     *
     * @param sleepTime
     * @return this
     */
    public Site setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    /**
     * Get the interval between the processing of two pages.<br>
     * Time unit is micro seconds.<br>
     *
     * @return the interval between the processing of two pages,
     */
    public int getSleepTime() {
        return sleepTime;
    }

    /**
     * Get retry times immediately when download fail, 0 by default.<br>
     *
     * @return retry times when download fail
     */
    public int getRetryTimes() {
        return retryTimes;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Put an Http header for downloader. <br/>
     * Use {@link #addCookie(String, String)} for cookie and {@link #addUserAgent(String)} for user-agent. <br/>
     *
     * @param key   key of http header, there are some keys constant in {@link HeaderConst}
     * @param value value of header
     * @return
     */
    public Site addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * Set retry times when download fail, 0 by default.<br>
     *
     * @return this
     */
    public Site setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    /**
     * When cycleRetryTimes is more than 0, it will add back to scheduler and try download again. <br>
     *
     * @return retry times when download fail
     */
    public int getCycleRetryTimes() {
        return cycleRetryTimes;
    }

    /**
     * Set cycleRetryTimes times when download fail, 0 by default. <br>
     *
     * @return this
     */
    public Site setCycleRetryTimes(int cycleRetryTimes) {
        this.cycleRetryTimes = cycleRetryTimes;
        return this;
    }

    public HttpHost getHttpProxy() {
        return httpProxy;
    }

    /**
     * set up httpProxy for this site
     *
     * @param httpProxy
     * @return
     */
    public Site setHttpProxy(HttpHost httpProxy) {
        this.httpProxy = httpProxy;
        return this;
    }

    public boolean isUseGzip() {
        return useGzip;
    }

    /**
     * Whether use gzip. <br>
     * Default is true, you can set it to false to disable gzip.
     *
     * @param useGzip
     * @return
     */
    public Site setUseGzip(boolean useGzip) {
        this.useGzip = useGzip;
        return this;
    }

    public Task toTask() {
        return new Task() {
            @Override
            public String getUUID() {
                return Site.this.getDomain();
            }

            @Override
            public Site getSite() {
                return Site.this;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Site site = (Site) o;

        if (cycleRetryTimes != site.cycleRetryTimes) return false;
        if (retryTimes != site.retryTimes) return false;
        if (sleepTime != site.sleepTime) return false;
        if (timeOut != site.timeOut) return false;
        if (acceptStatCode != null ? !acceptStatCode.equals(site.acceptStatCode) : site.acceptStatCode != null)
            return false;
        if (charset != null ? !charset.equals(site.charset) : site.charset != null) return false;
        if (defaultCookies != null ? !defaultCookies.equals(site.defaultCookies) : site.defaultCookies != null)
            return false;
        if (domain != null ? !domain.equals(site.domain) : site.domain != null) return false;
        if (headers != null ? !headers.equals(site.headers) : site.headers != null) return false;
        if (startRequests != null ? !startRequests.equals(site.startRequests) : site.startRequests != null)
            return false;
        if (userAgents.get(0) != null ? !userAgents.get(0).equals(site.userAgents.get(0)) : site.userAgents.get(0) != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = domain != null ? domain.hashCode() : 0;
        result = 31 * result + (userAgents.get(0) != null ? userAgents.get(0).hashCode() : 0);
        result = 31 * result + (defaultCookies != null ? defaultCookies.hashCode() : 0);
        result = 31 * result + (charset != null ? charset.hashCode() : 0);
        result = 31 * result + (startRequests != null ? startRequests.hashCode() : 0);
        result = 31 * result + sleepTime;
        result = 31 * result + retryTimes;
        result = 31 * result + cycleRetryTimes;
        result = 31 * result + timeOut;
        result = 31 * result + (acceptStatCode != null ? acceptStatCode.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Site{" +
                "domain='" + domain + "\'\n" +
                ", userAgent='" + userAgents.get(0) + "\'\n" +
                ", cookies=" + defaultCookies + "\n" +
                ", charset='" + charset + "\'\n" +
                ", startPages=" + startRequests + "\n" +
                ", sleepTime=" + sleepTime + "\n" +
                ", retryTimes=" + retryTimes + "\n" +
                ", cycleRetryTimes=" + cycleRetryTimes + "\n" +
                ", timeOut=" + timeOut + "\n" +
                ", acceptStatCode=" + acceptStatCode + "\n" +
                ", headers=" + headers + "\n" +
                '}';
    }

    /**
     * Set httpProxyPool, String[0]:ip, String[1]:port <br>
     *
     * @return this
     */
	public Site setHttpProxyPool(List<String[]> httpProxyList) {
		this.httpProxyPool=new ProxyPool(httpProxyList);
		return this;
	}

    public Site enableHttpProxyPool() {
        this.httpProxyPool=new ProxyPool();
        return this;
    }

	public ProxyPool getHttpProxyPool() {
		return httpProxyPool;
	}

	public HttpHost getHttpProxyFromPool() {
		return httpProxyPool.getProxy();
	}

	public void returnHttpProxyToPool(HttpHost proxy,int statusCode) {
		httpProxyPool.returnProxy(proxy,statusCode);
	}
	
	public Site setProxyReuseInterval(int reuseInterval) {
		this.httpProxyPool.setReuseInterval(reuseInterval);
		return this;
	}

    public boolean isDeepFirst() {
        return deepFirst;
    }

    public Site setDeepFirst(boolean deepFirst) {
        this.deepFirst = deepFirst;
        return this;
    }

    public int getMaxDeep() {
        return maxDeep;
    }

    public Site setMaxDeep(int maxDeep) {
        this.maxDeep = maxDeep;
        return  this;
    }

    public boolean isLocalSite(){
        return localSiteCopyLocation != null && localSiteCopyLocation.length() >0 ;
    }

    public String getLocalSiteCopyLocation() {
        return localSiteCopyLocation;
    }

    public Site setLocalSiteCopyLocation(String localSiteCopyLocation) {
        this.localSiteCopyLocation = localSiteCopyLocation;
        return this;
    }
}
