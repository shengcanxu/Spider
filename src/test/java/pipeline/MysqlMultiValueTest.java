package pipeline;

import org.junit.Test;
import web.cano.spider.*;
import web.cano.spider.pipeline.MysqlPipeline;
import web.cano.spider.pipeline.TestCallabckPipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.processor.TestableProcessor;
import web.cano.spider.utils.BaseDAO;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com <br>
 */
public class MysqlMultiValueTest extends DefaultPageProcessor implements TestableProcessor {
    private Page page;
    private Spider spider;



    private Site site = Site
            .me()
            .setDomain("blog.sina.com.cn")
            .setSleepTime(3000)
            .setShouldSplitToMultipleValues(true)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        PageItem title = new PageItem("title", PageItem.PageItemType.STRING,true,true);
        title = extratBy(page,"//span[@class=\"atc_title\"]/a/text()",PageProcessType.XPath,title);
        putItem(page, title);

        PageItem url = new PageItem("url",PageItem.PageItemType.STRING,true,true);
        url = extratBy(page, "//span[@class=\"atc_title\"]/a/@href", PageProcessType.XPath, url);
        putItem(page, url);
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Test
    public void testExtractContentUrl() throws Exception{
        PageProcessor processor = new MysqlMultiValueTest();
        Spider.create(processor)
                .addPipeline(new MysqlPipeline(true))
                .addPipeline(new TestCallabckPipeline())
                .addStartPage(new Page("blog_sina.html", true)) //网上url：http://blog.sina.com.cn/s/articlelist_1487828712_0_1.html
                .run();


        //做测试
        BaseDAO dao = BaseDAO.getInstance("canospider");
        String sql = "SELECT * FROM `mysqlmultivaluetest`";
        List<Map<String,Object>> result = dao.executeQuery(sql);

        assertThat(result.size()).isEqualTo(50);
        Map<String,Object> line1 = result.get(0);
        assertThat(line1.get("title")).isEqualTo("zzSed学习笔记");
        assertThat(line1.get("url")).isEqualTo("http://blog.sina.com.cn/s/blog_58ae76e80100to5q.html");
        Map<String,Object> line2 = result.get(10);
        assertThat(line2.get("title")).isEqualTo("一些IR相关概念");
        assertThat(line2.get("url")).isEqualTo("http://blog.sina.com.cn/s/blog_58ae76e80100mfqf.html");
        Map<String,Object> line3 = result.get(48);
        assertThat(line3.get("title")).isEqualTo("分治算法的一点思考--为什么大多使…");
        assertThat(line3.get("url")).isEqualTo("http://blog.sina.com.cn/s/blog_58ae76e80100gcsa.html");

        sql = "drop table `mysqlmultivaluetest`";
        dao.executeUpdate(sql);
    }

    @Override
    public void testCallback(Spider spider, Page page) {
        this.page = page;
        this.spider = spider;
    }

    @Override
    public Page getPage() {
        return this.page;
    }

    @Override
    public Spider getSpider() {
        return this.spider;
    }
}
