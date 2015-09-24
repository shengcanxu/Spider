package web.cano.spider.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.*;
import web.cano.spider.utils.BaseDAO;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by canoxu on 2015/1/20.
 */
public class MysqlPipeline implements Pipeline {

    public static enum STATUS {Success,Failure,NotStarted}

    private STATUS status = STATUS.NotStarted;
    private BaseDAO dao = BaseDAO.getInstance("canospider");
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //是否清空数据库
    private boolean shouldResetDb = false;

    //是否将多条记录分成多个记录存储
    private boolean shouldSplitMultiContent = false;

    private MysqlPipeline(){}

    public MysqlPipeline(boolean shouldResetDb){
        this(shouldResetDb,false);
    }

    public MysqlPipeline(boolean shouldResetDb,boolean shouldSplitMultiContent){
        this.shouldResetDb = shouldResetDb;
        this.shouldSplitMultiContent = shouldSplitMultiContent;
    }

    @Override
    public void process(Page page, Task task) {
        if(page.isSkip()) return;

        if(status == STATUS.Failure){
            logger.error("not able to create db table,stop processing");
            return;
        }

        String tableName = ((Spider)task).getUUID();
        if(status == STATUS.NotStarted){
            if(createTable(page,tableName)) {
                status = STATUS.Success;
            }else{
                status = STATUS.Failure;
                logger.error("create db table fails");
                return ;
            }
        }

        insertToDb(page, tableName);
    }

    private void insertToDb(Page page, String tableName) {
        String sql = "INSERT DELAYED INTO `" + tableName + "` (";
        String keys = "`id`";
        String values = "NULL";

        for(PageItem item : page.getPageItems().getItems()){
            keys = keys + ", `" + item.getItemName() + "`";
            values = values + ", '" + item.getItemValue() + "'";
        }
        sql = sql + keys + ") VALUES (" + values + ");";
        logger.info(sql);
        dao.executeUpdate(sql);
    }

    /**
     * create table with the definition of class
     * @return
     */
    private boolean createTable(Page page, String tableName) {
        //Map<String, PageItem.PageItemType> fields = page.getPageItems().getAllFields();

        if(this.shouldResetDb){
            String sql = "DROP TABLE IF EXISTS `" + tableName + "`;";
            dao.executeUpdate(sql);
            logger.info("drop table " + tableName + " and re-recreate again.");
        }

        logger.info("creating table " + tableName);
        String sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (`id` int(11) NOT NULL AUTO_INCREMENT";
        PageItems pageItems = page.getPageItems();
        for (PageItem item : pageItems.getItems()) {
            sql = sql + ", `" + item.getItemName() + "` " + getItemTypeString(item.getItemType()) + " NULL";
        }
        sql = sql + ", PRIMARY KEY (`id`)) ENGINE=myisam;";
        logger.info(sql);
        dao.executeUpdate(sql);

        logger.info("create table " + tableName + " successfully");
        return true;
    }

    private String getItemTypeString(PageItem.PageItemType itemType){
            switch (itemType){
                case STRING:{
                    return "varchar(1000)";
                }
                case INT: {
                    return "int(11)";
                }
                case DATETIME:{
                    return "datetime";
                }
                case TEXT:{
                    return "text";
                }
                default:{
                    return "varchar(1000)";
                }
            }
    }
}
