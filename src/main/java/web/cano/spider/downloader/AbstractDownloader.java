package web.cano.spider.downloader;

import web.cano.spider.Page;
import web.cano.spider.Request;
import web.cano.spider.Site;
import web.cano.spider.selector.Html;

/**
 * Base class of downloader with some common methods.
 *
 * @author code4crafter@gmail.com
 * @since 0.5.0
 */
public abstract class AbstractDownloader implements Downloader {

    /**
     * A simple method to download a url.
     *
     * @param url
     * @return html
     */
    public Html download(String url) {
        return download(url, null);
    }

    /**
     * A simple method to download a url.
     *
     * @param url
     * @return html
     */
    public Html download(String url, String charset) {
        Page page = download(new Request(url), Site.me().setCharset(charset).toTask());
        return (Html) page.getHtml();
    }

    protected void onSuccess(Request request) {
    }

    protected void onError(Request request) {
    }

    protected Page addToCycleRetry(Request request, Site site) {
        Page page = new Page();
        int cycleTriedTimes = request.getCycleTriedTimes();
        if (cycleTriedTimes == 0) {
            page.addTargetRequest(request.setPriority(0).setCycleTriedTimes(1));
        } else {
            cycleTriedTimes++;
            if (cycleTriedTimes >= site.getCycleRetryTimes()) {
                return null;
            }
            page.addTargetRequest(request.setPriority(0).setCycleTriedTimes(cycleTriedTimes));
        }
        page.setNeedCycleRetry(true);
        return page;
    }
}
