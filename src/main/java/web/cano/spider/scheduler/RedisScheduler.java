package web.cano.spider.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import web.cano.spider.Page;
import web.cano.spider.Site;
import web.cano.spider.Spider;
import web.cano.spider.Task;
import web.cano.spider.scheduler.component.DuplicateRemover;

import java.util.ArrayList;
import java.util.List;

/**
 * Use Redis as url scheduler for distributed crawlers.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.2.0
 */
public class RedisScheduler extends DuplicateRemovedScheduler implements MonitorableScheduler, DuplicateRemover {

    Logger logger = LoggerFactory.getLogger(getClass());

    private JedisPool pool;

    private Site site = null;

    private boolean startOver = false;

    //存储将要爬取的pages
    private static final String QUEUE_PREFIX = "queue_";

    //存储正在爬取的pages
    private static final String MAP_PREFIX = "map_";

    //存储已经爬过的urls
    private static final String SET_PREFIX = "set_";

    private RedisScheduler() {

    }

    public RedisScheduler(String host, Site site){
        this(host,site,false);
    }

    public RedisScheduler(String host, Site site,boolean startOver){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(1000);
        config.setMaxIdle(20);
        config.setMaxWait(100000l);
        this.pool = new JedisPool(config, host,6379,100000);
        setDuplicateRemover(this);

        this.site = site;
        this.startOver = startOver;
    }

    @Override
    public boolean isDuplicate(Page page, Task task){
        Jedis jedis = pool.getResource();
        try {
            if(startOver){
                jedis.del(getSetKey(task));
                jedis.del(getQueueKey(task));
                jedis.del(getMapKey(task));
                startOver = false;
                logger.info("remove redis queue and start over parsing.");
            }

            boolean isDuplicate = jedis.sismember(getSetKey(task), page.getUrl());
            if (!isDuplicate || page.isRefresh()) {
                jedis.sadd(getSetKey(task), page.getUrl());
                return false;
            }
            return true;
        } finally {
            pool.returnResource(jedis);
        }

    }

    @Override
    protected void pushWhenNoDuplicate(Page page, Task task) {
        Jedis jedis = pool.getResource();
        try{
            if(site.isDeepFirst()){
                jedis.lpush(getQueueKey(task), page.toJson());
            }else {
                jedis.rpush(getQueueKey(task), page.toJson());
            }
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public Page poll(Task task){
        Jedis jedis = pool.getResource();
        try{
            String json = jedis.lpop(getQueueKey(task));
            if (json == null){
                return null;
            }

            String url = json.substring(json.indexOf("<$url>")+6,json.lastIndexOf("<$/url>"));
            jedis.hset(getMapKey(task),url,json);
            Page page = Page.fromJson(json);
            return page;
        }finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public void completeParse(Page page, Task task) {
        Jedis jedis = pool.getResource();

        try{
            jedis.hdel(getMapKey(task),page.getUrl());

        }finally {
            pool.returnResource(jedis);
        }
    }

    protected String getSetKey(Task task) {
        return SET_PREFIX + ((Spider) task).getUUID();
    }

    protected String getQueueKey(Task task) {
        return QUEUE_PREFIX + ((Spider) task).getUUID();
    }

    protected String getMapKey(Task task) {
        return MAP_PREFIX + ((Spider) task).getUUID();
    }

    @Override
    public int getLeftRequestsCount(Task task) {
        Jedis jedis = pool.getResource();
        try {
            Long size = jedis.llen(getQueueKey(task));
            return size.intValue();
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public int getTotalRequestsCount(Task task) {
        Jedis jedis = pool.getResource();
        try {
            Long size = jedis.scard(getQueueKey(task));
            return size.intValue();
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public List<Page> checkIfCompleteParse(Task task) {
        Jedis jedis = pool.getResource();
        List<Page> list = new ArrayList<Page>();
        try{
            List<String> jsons = jedis.hvals(getMapKey(task));
            for(String json : jsons){
                Page page = Page.fromJson(json);
                list.add(page);
            }
            return list;
        }finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public void saveQueue(Task task) {
//        Jedis jedis = pool.getResource();
//        BaseDAO dao = BaseDAO.getInstance("cano");
//
//        //create table
//        String tableName;
//        if(task instanceof ModelSpider){
//            tableName = ((ModelSpider) task).getPageModel().getClazz().getSimpleName() + "UrlSet";
//        }else{
//            tableName = task.getUUID() + "UrlSet";
//        }
//        String sql = "DROP TABLE IF EXISTS `" + tableName + "`;";
//        dao.executeUpdate(sql);
//        logger.info("drop table " + tableName + " and re-recreate again.");
//        logger.info("creating table " + tableName);
//        sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (`id` int(11) NOT NULL AUTO_INCREMENT,`url` varchar(1024) NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB;";
//        dao.executeUpdate(sql);
//
//        //store values
//        logger.info("storing parsed urls.");
//        try{
//            Set<String> urlSet = jedis.smembers(getSetKey(task));
//            for(String url : urlSet){
//                logger.info(url);
//                sql = "INSERT INTO `" + tableName + "` (`id`,`url`) VALUES (NULL,'" + url + "')";
//                dao.executeUpdate(sql);
//            }
//        }finally {
//            pool.returnResource(jedis);
//        }
    }

    @Override
    public void recoverQueue(Task task) {
//        BaseDAO dao = BaseDAO.getInstance("cano");
//        Jedis jedis = pool.getResource();
//
//        //get urls
//        String tableName;
//        if(task instanceof ModelSpider){
//            tableName = ((ModelSpider) task).getPageModel().getClazz().getSimpleName() + "UrlSet";
//        }else{
//            tableName = task.getUUID() + "UrlSet";
//        }
//
//        String sql = "select url from " + tableName;
//        try {
//            List<Map<String,Object>> list = dao.executeQuery(sql,null,null);
//            for (Map<String, Object> map : list) {
//                String url = (String) map.get("url");
//                jedis.sadd(getSetKey(task), url);
//            }
//        }finally {
//            pool.returnResource(jedis);
//        }
    }
}
