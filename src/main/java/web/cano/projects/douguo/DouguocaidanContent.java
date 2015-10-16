package web.cano.projects.douguo;

import web.cano.spider.Page;
import web.cano.spider.PageItem;
import web.cano.spider.Site;
import web.cano.spider.Spider;
import web.cano.spider.pipeline.MysqlPipeline;
import web.cano.spider.pipeline.SaveResourcePipeline;
import web.cano.spider.pipeline.SaveSourceFilePipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.scheduler.RedisScheduler;
import web.cano.spider.selector.Html;
import web.cano.spider.selector.XpathSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cano on 2015/5/28.
 */

public class DouguocaidanContent extends DefaultPageProcessor {

    private Site site = Site
            .me()
            .setDomain("douguo.com")
            .addHeader("Referer", "http://www.douguo.com/")
            .setDeepFirst(false)
            .setSleepTime(3000)
            //.setLocalSiteCopyLocation("D:\\software\\redis\\data\\contentsourcefile\\")
            .addUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        if(page.isResource()) return;

        PageItem pageUrl = new PageItem("pageUrl", PageItem.PageItemType.STRING,true,false);
        pageUrl = extractByUrl(page,".*",pageUrl);
        putItem(page,pageUrl);

        PageItem picutre = new PageItem("picutre", PageItem.PageItemType.STRING, true, false);
        picutre = extractBy(page, "//*[@id=\"main\"]//div[@class=\"bokpic\"]//a/@href", PageProcessType.XPath, picutre);
        putItem(page, picutre);
        downloadResources(page,picutre);

        PageItem title = new PageItem("title", PageItem.PageItemType.STRING,true,false);
        title = extractBy(page, "//*[@id=\"page_cm_id\"]/text()", PageProcessType.XPath, title);
        putItem(page,title);

        PageItem reads = new PageItem("reads", PageItem.PageItemType.INT, true,false);
        reads = extractBy(page, "//*[@id=\"main\"]//div[@class=\"falisc mbm mb40\"]/span[1]/text()", PageProcessType.XPath, reads);
        putItem(page,reads);

        PageItem souchang = new PageItem("souchang", PageItem.PageItemType.INT, true,false);
        souchang = extractBy(page, "//*[@id=\"collectsnum\"]/text()", PageProcessType.XPath, souchang);
        putItem(page,souchang);

        PageItem createDate = new PageItem("createDate", PageItem.PageItemType.DATE, true,false);
        createDate = extractBy(page, "//*[@id=\"main\"]//div[@class=\"falisc mbm mb40\"]/span[@class=\"fcc\"]/text()", PageProcessType.XPath, createDate);
        putItem(page,createDate);

        PageItem tips = new PageItem("tips", PageItem.PageItemType.TEXT,true,false);
        tips = extractBy(page, "//*[@id=\"fullStory\"]/text()|//*[@id=\"main\"]//div[@class=\"xtip\"]/text()", PageProcessType.XPath, tips);
        putItem(page,tips);

        PageItem difficulty = new PageItem("difficulty", PageItem.PageItemType.STRING,true,false);
        difficulty = extractBy(page, "//*[@id=\"main\"]//table[@class=\"retamr\"]/tbody/tr[@class=\"mtim\"][1]/td[1]/text()", PageProcessType.XPath, difficulty);
        putItem(page,difficulty );

        PageItem timeLast = new PageItem("timeLast", PageItem.PageItemType.STRING,true,false);
        timeLast = extractBy(page, "//*[@id=\"main\"]//table[@class=\"retamr\"]/tbody/tr[@class=\"mtim\"][1]/td[2]/text()", PageProcessType.XPath, timeLast);
        putItem(page,timeLast);

        PageItem zhuliao = new PageItem("zhuliao", PageItem.PageItemType.TEXT, true, true);
        zhuliao = extractBy(page, "//*[@id=\"main\"]//table[@class=\"retamr\"]/tbody/tr/td/html()",PageProcessType.XPath,zhuliao);
        zhuliao.setItemValue(getZhuLiao((List<String>)zhuliao.getItemValue(),page));
        putItem(page, zhuliao);

        PageItem fuliao = new PageItem("fuliao", PageItem.PageItemType.TEXT,true,true);
        fuliao = extractBy(page, "//*[@id=\"main\"]//table[@class=\"retamr\"]/tbody/tr/td/html()",PageProcessType.XPath,fuliao);
        fuliao.setItemValue(getFuLiao((List<String>)fuliao.getItemValue(),page));
        putItem(page, fuliao);

        PageItem xiaotieshi = new PageItem("xiaotieshi", PageItem.PageItemType.TEXT,true,false);
        xiaotieshi = extractBy(page,"//*[@id=\"main\"]//div[@class=\"xtieshi\"]/p/html()",PageProcessType.XPath,xiaotieshi);
        putItem(page,xiaotieshi);

        PageItem tags = new PageItem("tags", PageItem.PageItemType.STRING,true,true);
        tags = extractBy(page,"//*[@id=\"displaytag\"]//a[@class=\"btnta\"]/text()",PageProcessType.XPath, tags);
        putItem(page, tags);

        PageItem zuopinliang = new PageItem("zuopinliang", PageItem.PageItemType.STRING,true,false);
        zuopinliang = extractBy(page, "//h3[@class=\"mb15 fwb\"]/a/text()", PageProcessType.XPath,zuopinliang);
        putItem(page,zuopinliang);

        PageItem stepContent = new PageItem("stepContent", PageItem.PageItemType.TEXT,true,true);
        stepContent = extractBy(page, "//*[@id=\"main\"]//div[@class=\"step clearfix\"]/div/p/html()", PageProcessType.XPath, stepContent);
        putItem(page, stepContent);

        PageItem stepImage = new PageItem("stepImage", PageItem.PageItemType.TEXT, true, true);
        stepImage = extractBy(page, "//*[@id=\"main\"]//div[@class=\"step clearfix\"]/div//a/@href", PageProcessType.XPath,stepImage);
        putItem(page, stepImage);
        downloadResources(page,stepImage);

        PageItem author = new PageItem("author", PageItem.PageItemType.STRING, true, false);
        author = extractBy(page, "//*[@id=\"main\"]/div[@class=\"reright\"]//h4/a[1]/text()", PageProcessType.XPath, author);
        putItem(page, author);

        PageItem authorLink = new PageItem("authorLink", PageItem.PageItemType.STRING, true, false);
        authorLink = extractBy(page, "//*[@id=\"main\"]/div[@class=\"reright\"]//h4/a[1]/@href", PageProcessType.XPath, authorLink);
        putItem(page, authorLink);

    }

    private List<String> getZhuLiao(List<String> list, Page page){
        List<String> result = new ArrayList<String>();
        boolean start = false;
        for(int i=0; i<list.size(); i++){
            String str = list.get(i);
            if(!start){
                if(str.contains("主料")){
                    start = true;
                }
            }else{
                if(str.contains("辅料")){
                    break;
                }
                if(str.trim().length() == 0) continue;
                Html html = new Html(str);
                String name = html.selectDocument(new XpathSelector("//span[1]/allText()"));
                String shuliang = html.selectDocument(new XpathSelector("//span[2]/allText()"));
                if(name != null) result.add(name + "=" + shuliang );
            }
        }

        return result;
    }

    public List<String> getFuLiao(List<String> list, Page page){
        List<String> result = new ArrayList<String>();
        boolean start = false;
        for(int i=0; i<list.size(); i++){
            String str = list.get(i);
            if(!start){
                if(str.contains("辅料")){
                    start = true;
                }
            }else{
                if(str.trim().length() == 0) continue;

                Html html = new Html(str);
                String name = html.selectDocument(new XpathSelector("//span[1]/allText()"));
                String shuliang = html.selectDocument(new XpathSelector("//span[2]/allText()"));
                if(name != null) result.add(name + "=" + shuliang);
            }
        }

        return result;
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        int threadNum = 1;
        if(args.length > 0){
            threadNum = Integer.parseInt(args[0]);
        }
        System.out.println("thread NO.: " + threadNum);

        PageProcessor processor = new DouguocaidanContent();
        Spider spider = Spider.create(processor);

        if(args.length > 1 && args[1].length() > 0){
            spider.setUUID(args[1]);
            System.out.println("spider Name: " + args[1]);
        }

        spider.setUUID("douguo");

        spider.setScheduler(new RedisScheduler("127.0.0.1", processor.getSite(), false))
                .addPipeline(new SaveSourceFilePipeline("D:/software/redis/data/contentsourcefile/"))
                .addPipeline(new SaveResourcePipeline("D:/software/redis/data/contentresource/").setSeparateFolder(true))
                .addPipeline(new MysqlPipeline(true))
                //.addStartPage(new Page("http://www.douguo.com/cookbook/1257340.html"))
                .thread(threadNum).run();
    }

}
