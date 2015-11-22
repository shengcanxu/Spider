package web.cano.projects.xiachufang;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by cano on 2015/11/4.
 */
public class Test {

    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("abc",12);
        jsonObject.put("eft", "eks");

        String json = jsonObject.toJSONString();
        System.out.println(json); 

        JSONObject jo = new JSONObject();
        jo.put("newobject", json);

        json = jo.toJSONString();
        System.out.println(json);
    }
}
