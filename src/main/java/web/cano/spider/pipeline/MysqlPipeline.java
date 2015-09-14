package web.cano.spider.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.PageItems;
import web.cano.spider.Spider;
import web.cano.spider.Task;
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

    private MysqlPipeline(){}

    public MysqlPipeline(boolean shouldResetDb){
        this.shouldResetDb = shouldResetDb;
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

        insertToDb(page,tableName);
    }

    private void insertToDb(Page page, String tableName) {
        String sql = "INSERT DELAYED INTO `" + tableName + "` (";
        String keys = "`id`";
        String values = "NULL";

        for (Map.Entry<String,String> entry: page.getPageItems().getAllItems().entrySet()) {
            keys = keys + ", `" + entry.getKey() + "`";
            values = values + ", '" + entry.getValue() + "'";
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
        Map<String, PageItems.PageItemsType> fields = page.getPageItems().getAllFields();

        if(this.shouldResetDb){
            String sql = "DROP TABLE IF EXISTS `" + tableName + "`;";
            dao.executeUpdate(sql);
            logger.info("drop table " + tableName + " and re-recreate again.");
        }

        logger.info("creating table " + tableName);
        String sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (`id` int(11) NOT NULL AUTO_INCREMENT";
        Map<String,String> map = getTableFields(fields);
        for (Map.Entry<String,String> entry : map.entrySet()) {
            sql = sql + ", `" + entry.getKey() + "` " + entry.getValue() + " NULL";
        }
        sql = sql + ", PRIMARY KEY (`id`)) ENGINE=myisam;";
        logger.info(sql);
        dao.executeUpdate(sql);

        logger.info("create table " + tableName + " successfully");
        return true;
    }

    private Map<String,String> getTableFields(Map<String, PageItems.PageItemsType> fields){
        //TODO: need to change here to support filemap etc.
//        Map<String, String> map = new HashMap<String, String>();
//        Field[] fields = clazz.getDeclaredFields();
//        AccessibleObject.setAccessible(fields, true);
//        for(Field field : fields){
//            if(field.getName().startsWith("this")) { //this is field for inner-Class, so need to ignore it
//                continue;
//            }else if(PageModel.class.isAssignableFrom(field.getType())){
//                Map<String, String> subpageMap = getTableFieldsFromPageModel(field.getType());
//                map.putAll(subpageMap);
//            }else {
//                if (field.getAnnotation(DownloadFile.class) != null) {
//                    map.put(field.getName() + "FileMap", "text");
//                }
//                map.put(field.getName(), getAnnotationFieldType(field));
//            }
//        }

        Map<String, String> map = new HashMap<String, String>();
        for(Map.Entry<String,PageItems.PageItemsType> entry : fields.entrySet()){
            PageItems.PageItemsType pageItemsType = entry.getValue();
            switch (pageItemsType){
                case STRING:{
                    map.put(entry.getKey(), "varchar(1000)");
                    break;
                }
                case INT: {
                    map.put(entry.getKey(), "int(11)");
                    break;
                }
                case DATETIME:{
                    map.put(entry.getKey(), "datetime");
                    break;
                }
                case TEXT:{
                    map.put(entry.getKey(), "text");
                    break;
                }
                default:{
                    map.put(entry.getKey(), "varchar(1000)");
                }
            }
        }
        return map;
    }
}
