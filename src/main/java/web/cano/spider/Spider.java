package web.cano.spider;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.downloader.Downloader;
import web.cano.spider.downloader.HttpClientDownloader;
import web.cano.spider.pipeline.*;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.scheduler.QueueScheduler;
import web.cano.spider.scheduler.Scheduler;
import web.cano.spider.thread.CountableThreadPool;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Entrance of a crawler.<br>
 * A spider contains four modules: Downloader, Scheduler, PageProcessor and
 * Pipeline.<br>
 * Every module is a field of Spider. <br>
 * The modules are defined in interface. <br>
 * You can customize a spider with various implementations of them. <br>
 * Examples: <br>
 * <br>
 * A simple crawler: <br>
 * Spider.create(new SimplePageProcessor("http://my.oschina.net/",
 * "http://my.oschina.net/*blog/*")).run();<br>
 * <br>
 * Store results to files by FilePipeline: <br>
 * Spider.create(new SimplePageProcessor("http://my.oschina.net/",
 * "http://my.oschina.net/*blog/*")) <br>
 * .pipeline(new FilePipeline("/data/temp/webmagic/")).run(); <br>
 * <br>
 * Use FileCacheQueueScheduler to store urls and cursor in files, so that a
 * Spider can resume the status when shutdown. <br>
 * Spider.create(new SimplePageProcessor("http://my.oschina.net/",
 * "http://my.oschina.net/*blog/*")) <br>
 * .scheduler(new FileCacheQueueScheduler("/data/temp/webmagic/cache/")).run(); <br>
 *
 * @author code4crafter@gmail.com <br>
 * @see Downloader
 * @see Scheduler
 * @see PageProcessor
 * @see Pipeline
 * @since 0.1.0
 */
public class Spider implements Runnable, Task {

    protected Downloader downloader;

    protected List<Pipeline> pipelines = new ArrayList<Pipeline>();

    protected PageProcessor pageProcessor;

    protected List<Page> startPages;

    protected Site site;

    protected String uuid;

    protected Scheduler scheduler = new QueueScheduler();

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected CountableThreadPool threadPool;

    protected ExecutorService executorService;

    protected int threadNum = 1;

    protected AtomicInteger stat = new AtomicInteger(STAT_INIT);

    protected boolean exitWhenComplete = true;

    protected final static int STAT_INIT = 0;

    protected final static int STAT_RUNNING = 1;

    protected final static int STAT_STOPPED = 2;

    protected boolean spawnUrl = true;

    protected boolean destroyWhenExit = true;

    private ReentrantLock newUrlLock = new ReentrantLock();

    private Condition newUrlCondition = newUrlLock.newCondition();

    private List<SpiderListener> spiderListeners;

    private final AtomicLong pageCount = new AtomicLong(0);

    private Date startTime;

    private int emptySleepTime = 30000;

    //是否应该从redis中恢复上次的url set
    private boolean recoverUrlSet = false;

    //是否应该将url set 保存到数据库
    private boolean saveUrlSet = false;

    /**
     * create a spider with pageProcessor.
     *
     * @param pageProcessor
     * @return new spider
     * @see PageProcessor
     */
    public static Spider create(PageProcessor pageProcessor) {
        return new Spider(pageProcessor);
    }

    /**
     * create a spider with pageProcessor.
     *
     * @param pageProcessor
     */
    private Spider(PageProcessor pageProcessor) {
        this.pageProcessor = pageProcessor;
        this.site = pageProcessor.getSite();
        this.uuid = pageProcessor.getClass().getSimpleName();
    }

    /**
     * set scheduler for Spider
     *
     * @param scheduler
     * @return this
     * @Deprecated
     * @see #setScheduler(web.cano.spider.scheduler.Scheduler)
     */
    public Spider scheduler(Scheduler scheduler) {
        return setScheduler(scheduler);
    }

    /**
     * set scheduler for Spider
     *
     * @param scheduler
     * @return this
     * @see Scheduler
     * @since 0.2.1
     */
    public Spider setScheduler(Scheduler scheduler) {
        checkIfRunning();
        Scheduler oldScheduler = this.scheduler;
        this.scheduler = scheduler;
        if (oldScheduler != null) {
            Page page;
            while ((page = oldScheduler.poll(this)) != null) {
                this.scheduler.push(page, this);
            }
        }
        return this;
    }

    /**
     * add a pipeline for Spider
     *
     * @param pipeline
     * @return this
     * @see #addPipeline(web.cano.spider.pipeline.Pipeline)
     * @deprecated
     */
    public Spider pipeline(Pipeline pipeline) {
        return addPipeline(pipeline);
    }

    /**
     * add a pipeline for Spider
     *
     * @param pipeline
     * @return this
     * @see Pipeline
     * @since 0.2.1
     */
    public Spider addPipeline(Pipeline pipeline) {
        checkIfRunning();
        this.pipelines.add(pipeline);
        return this;
    }


    /**
     * set the downloader of spider
     *
     * @param downloader
     * @return this
     * @see Downloader
     */
    public Spider setDownloader(Downloader downloader) {
        checkIfRunning();
        this.downloader = downloader;
        return this;
    }

    protected void initComponent() {
        if (downloader == null) {
            this.downloader = new HttpClientDownloader();
        }
        pipelines.add(new AlignMultiVlauesPipeline());
        pipelines.add(new CombineMultiPagesPipeline());
        pipelines.add(new CombineSubPagesPipeline());
        pipelines.add(new ConsolePipeline());

        downloader.setThread(threadNum);
        if (threadPool == null || threadPool.isShutdown()) {
            if (executorService != null && !executorService.isShutdown()) {
                threadPool = new CountableThreadPool(threadNum, executorService);
            } else {
                threadPool = new CountableThreadPool(threadNum);
            }
        }
        if (startPages != null) {
            if(site.isDeepFirst()){
                for(int i= startPages.size()-1; i>=0; i--){
                    scheduler.push(startPages.get(i),this);
                }
            }else{
                for(int i=0; i< startPages.size(); i++){
                    scheduler.push(startPages.get(i),this);
                }
            }
            startPages.clear();
        }
        startTime = new Date();
    }

    @Override
    public void run() {
        checkRunningStat();
        initComponent();
        logger.info("Spider " + getUUID() + " started!");

        preRun();

        while (!Thread.currentThread().isInterrupted() && stat.get() == STAT_RUNNING) {
            Page page = scheduler.poll(this);
            if (page == null) {
                if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
                    //print out the un-parsed urls
                    List<Page> leftPages = scheduler.checkIfCompleteParse(this);
                    if(leftPages != null && leftPages.size() > 0){
                        logger.info("some urls are not parsed:");
                        for(Page p : leftPages){
                            logger.info(p.getUrl());
                        }
                        logger.info("restart un-parsed urls");
                        for(Page p : leftPages){
                            scheduler.push(p,this);
                        }
                        continue;
                    }
                    break;
                }
                // wait until new url added
                waitNewUrl();
            } else {
                final Request requestFinal = new Request(page);
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            processRequest(requestFinal);
                            onSuccess(requestFinal);
                        } catch (Exception e) {
                            onError(requestFinal);
                            logger.error("process page " + requestFinal + " error", e);
                        } finally {
                            if (site.getHttpProxyPool()!=null && site.getHttpProxyPool().isEnable()) {
                                site.returnHttpProxyToPool(requestFinal.getProxy(), requestFinal.getStatusCode());
                            }
                            pageCount.incrementAndGet();
                            signalNewUrl();
                        }
                    }
                });
            }
        }

        postRun();

        stat.set(STAT_STOPPED);
        // release some resources
        if (destroyWhenExit) {
            close();
        }
    }

    protected void preRun(){
        if(recoverUrlSet) {
            this.scheduler.recoverUrlSet(this);
        }
    }

    protected void postRun(){
        if(saveUrlSet) {
            this.scheduler.saveUrlSet(this);
        }
    }

    protected void onError(Request request) {
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onError(request);
            }
        }
    }

    protected void onSuccess(Request request) {
        scheduler.completeParse(request.getPage(), this);

        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onSuccess(request);
            }
        }
    }

    private void checkRunningStat() {
        while (true) {
            int statNow = stat.get();
            if (statNow == STAT_RUNNING) {
                throw new IllegalStateException("Spider is already running!");
            }
            if (stat.compareAndSet(statNow, STAT_RUNNING)) {
                break;
            }
        }
    }

    public void close() {
        destroyEach(downloader);
        destroyEach(pageProcessor);
        for (Pipeline pipeline : pipelines) {
            destroyEach(pipeline);
        }
        threadPool.shutdown();
    }

    private void destroyEach(Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void processRequest(Request request) {
        Page page;
        if(request.getPage().getDepth() >= site.getMaxDeep()){
            page = request.getPage();    //超出MaxDeep范围的不下载内容,仅仅做一次process，为了获得内容
            pageProcessor.process(page);
            return;
        }else{
            page = downloader.download(request, this);
        }

        if (page == null) {
            sleep(site.getSleepTime());
            onError(request);
            return;
        }

        // for cycle retry
        if (page.isNeedCycleRetry()) {
            extractAndAddPages(page, true);
            sleep(site.getSleepTime());
            return;
        }
        pageProcessor.process(page);
        extractAndAddPages(page, spawnUrl);
        for (Pipeline pipeline : pipelines) {
            pipeline.process(page, this);
        }
        //for proxy status management
        request.setStatusCode(page.getStatusCode());
        sleep(site.getSleepTime());
    }

    protected void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void extractAndAddPages(Page page, boolean spawnUrl) {
        if (spawnUrl && CollectionUtils.isNotEmpty(page.getTargetPages())) {
            List<Page> pages = page.getTargetPages();
            logger.info("get " + pages.size() + " links to follow.");
            for (Page p : pages) {
                p.setDepth(page.getDepth()+1);
                logger.info(p.getUrl());
            }

            List<Page> nextPages = page.getNextPages();
            logger.info("get " + nextPages.size() + " next pages to follow.");
            for(Page page1 : nextPages){
                page1.setDepth(page.getDepth());
                logger.info(page1.getUrl());
            }

            addPagesToScheduler(pages,nextPages);
        }
    }

    public Spider addStartPage(Page startPage){
        return addPageToScheduler(startPage);
    }

    public Spider addStartPages(List<Page> startPages){
        return addPagesToScheduler(startPages,null);
    }

    public Spider addPageToScheduler(Page page) {
        scheduler.push(page, this);
        signalNewUrl();
        return this;
    }

    public Spider addPagesToScheduler(List<Page> pages, List<Page> nextPages){
        if(site.isDeepFirst()){
            for(int i=nextPages.size()-1; i>=0; i--){
                scheduler.push(nextPages.get(i),this);
            }
            for(int i=pages.size()-1; i>=0; i--){
                scheduler.push(pages.get(i),this);
            }

        }else{
            for(int i=0; i<pages.size(); i++){
                scheduler.push(pages.get(i), this);
            }
            for(int i=nextPages.size()-1; i>=0; i--){
                scheduler.pushToHead(nextPages.get(i),this);
            }
        }

        signalNewUrl();
        return this;
    }

    protected void checkIfRunning() {
        if (stat.get() == STAT_RUNNING) {
            throw new IllegalStateException("Spider is already running!");
        }
    }

    public void runAsync() {
        Thread thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
    }
    

    private void waitNewUrl() {
        newUrlLock.lock();
        try {
            //double check
            if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
                return;
            }
            newUrlCondition.await(emptySleepTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("waitNewUrl - interrupted, error {}", e);
        } finally {
            newUrlLock.unlock();
        }
    }

    private void signalNewUrl() {
        try {
            newUrlLock.lock();
            newUrlCondition.signalAll();
        } finally {
            newUrlLock.unlock();
        }
    }

    public void start() {
        runAsync();
    }

    public void stop() {
        if (stat.compareAndSet(STAT_RUNNING, STAT_STOPPED)) {
            logger.info("Spider " + getUUID() + " stop success!");
        } else {
            logger.info("Spider " + getUUID() + " stop fail!");
        }
    }

    /**
     * start with more than one threads
     *
     * @param threadNum
     * @return this
     */
    public Spider thread(int threadNum) {
        checkIfRunning();
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }

    /**
     * start with more than one threads
     *
     * @param threadNum
     * @return this
     */
    public Spider thread(ExecutorService executorService, int threadNum) {
        checkIfRunning();
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }

    public boolean isExitWhenComplete() {
        return exitWhenComplete;
    }

    /**
     * Exit when complete. <br/>
     * True: exit when all url of the site is downloaded. <br/>
     * False: not exit until call stop() manually.<br/>
     *
     * @param exitWhenComplete
     * @return
     */
    public Spider setExitWhenComplete(boolean exitWhenComplete) {
        this.exitWhenComplete = exitWhenComplete;
        return this;
    }

    public boolean isSpawnUrl() {
        return spawnUrl;
    }

    /**
     * Get page count downloaded by spider.
     *
     * @return total downloaded page count
     * @since 0.4.1
     */
    public long getPageCount() {
        return pageCount.get();
    }

    /**
     * Get running status by spider.
     *
     * @return running status
     * @see Status
     * @since 0.4.1
     */
    public Status getStatus() {
        return Status.fromValue(stat.get());
    }


    public enum Status {
        Init(0), Running(1), Stopped(2);

        private Status(int value) {
            this.value = value;
        }

        private int value;

        int getValue() {
            return value;
        }

        public static Status fromValue(int value) {
            for (Status status : Status.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            //default value
            return Init;
        }
    }

    /**
     * Get thread count which is running
     *
     * @return thread count which is running
     * @since 0.4.1
     */
    public int getThreadAlive() {
        if (threadPool == null) {
            return 0;
        }
        return threadPool.getThreadAlive();
    }

    /**
     * Whether add urls extracted to download.<br>
     * Add urls to download when it is true, and just download seed urls when it is false. <br>
     * DO NOT set it unless you know what it means!
     *
     * @param spawnUrl
     * @return
     * @since 0.4.0
     */
    public Spider setSpawnUrl(boolean spawnUrl) {
        this.spawnUrl = spawnUrl;
        return this;
    }

    @Override
    public String getUUID() {
        if (uuid != null) {
            return uuid;
        }
        if (site != null) {
            return site.getDomain();
        }
        uuid = UUID.randomUUID().toString();
        return uuid;
    }

    public Spider setExecutorService(ExecutorService executorService) {
        checkIfRunning();
        this.executorService = executorService;
        return this;
    }

    @Override
    public Site getSite() {
        return site;
    }

    public List<SpiderListener> getSpiderListeners() {
        return spiderListeners;
    }

    public Spider setSpiderListeners(List<SpiderListener> spiderListeners) {
        this.spiderListeners = spiderListeners;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Set wait time when no url is polled.<br></br>
     *
     * @param emptySleepTime In MILLISECONDS.
     */
    public void setEmptySleepTime(int emptySleepTime) {
        this.emptySleepTime = emptySleepTime;
    }

    public Spider setRecoverUrlSet(boolean recoverUrlSet) {
        this.recoverUrlSet = recoverUrlSet;
        return this;
    }

    public Spider setSaveUrlSet(boolean saveUrlSet) {
        this.saveUrlSet = saveUrlSet;
        return this;
    }

    public PageProcessor getPageProcessor() {
        return pageProcessor;
    }
}
