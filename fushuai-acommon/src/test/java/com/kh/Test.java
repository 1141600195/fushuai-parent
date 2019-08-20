/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: Test
 * Author:   康鸿
 * Date:     2019/8/16 16:31
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.kh;

import com.kh.random.VerifyCodeUtils;
import com.kh.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/16
 * @since 1.0.0
 * Description 
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class Test {
    public void sentcode(){
        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "925b38d0e0fb49a49e698db5e1ab8085";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appcode);

        int a = (int)(Math.random()*9000)+1000;
        Map<String, String> querys = new HashMap<String, String>();


        querys.put("mobile", "15930112391");

        querys.put("param", "code:"+a);


        querys.put("tpl_id", "TP1711063");
        Map<String, String> bodys = new HashMap<String, String>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString()+"=============================返回值");
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}