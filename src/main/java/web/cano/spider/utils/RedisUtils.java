package web.cano.spider.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.*;
import java.util.List;
import java.util.Set;

/**
 * Created by cano on 2015/10/9.
 */
public class RedisUtils {

    public static void fileToRedisList(String filePath, String listName){
        Jedis jedis = null;
        JedisPool pool;
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(100);
        config.setMaxIdle(20);
        config.setMaxWait(10000l);
        pool = new JedisPool(config, "127.0.0.1");

        jedis = pool.getResource();
        File storeFile = new File(filePath);
        if(!storeFile.exists()){
            System.out.println("file not exists");
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(storeFile));
            String str = br.readLine();
            int i=0;
            while(str != null) {
                jedis.rpush(listName, str);
                i++;
                System.out.println(i);
                str = br.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void fileToRedisSet(String filePath, String setName){
        Jedis jedis = null;
        JedisPool pool;
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(100);
        config.setMaxIdle(20);
        config.setMaxWait(10000l);
        pool = new JedisPool(config, "127.0.0.1");

        jedis = pool.getResource();
        File storeFile = new File(filePath);
        if(!storeFile.exists()){
            System.out.println("file not exists");
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(storeFile));
            String str = br.readLine();
            int i=0;
            while(str != null) {
                jedis.sadd(setName, str);
                i ++;
                System.out.println(i);
                str = br.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void redisListToFile(String filePath, String listName){
        Jedis jedis = null;
        JedisPool pool;
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(100);
        config.setMaxIdle(20);
        config.setMaxWait(10000l);
        pool = new JedisPool(config, "127.0.0.1");

        try {
            jedis = pool.getResource();

            //get file resource
            File storeFile = new File(filePath);
            if(storeFile.exists()){
                System.out.println("file exists for " + filePath);
            }
            FileOutputStream output = new FileOutputStream(storeFile);

            List<String> list = jedis.lrange((listName), 0, jedis.llen(listName));
            long len = list.size();
            for(int i=0; i<len; i++){
                String s = list.get(i);
                s = s + "\n";
                output.write(s.getBytes());
                System.out.println(i);
            }

            output.close();
            pool.returnResource(jedis);

        } catch (JedisConnectionException e) {
            if (jedis != null)
                pool.returnBrokenResource(jedis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void redisSetToFile(String filePath, String setName){
        Jedis jedis = null;
        JedisPool pool;
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(100);
        config.setMaxIdle(20);
        config.setMaxWait(10000l);
        pool = new JedisPool(config, "127.0.0.1");

        try {
            jedis = pool.getResource();

            //get file resource
            File storeFile = new File(filePath);
            if(storeFile.exists()){
                System.out.println("file exists for " + filePath);
            }
            FileOutputStream output = new FileOutputStream(storeFile);

            Set<String> urlset = jedis.smembers(setName);
            String[] urls = new String[urlset.size()];
            urlset.toArray(urls);
            for(int i=0; i<urls.length; i++){
                String content = urls[i] + "\n";
                output.write(content.getBytes());

                System.out.println(i+1);
            }

            output.close();
            pool.returnResource(jedis);

        } catch (JedisConnectionException e) {
            if (jedis != null)
                pool.returnBrokenResource(jedis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
