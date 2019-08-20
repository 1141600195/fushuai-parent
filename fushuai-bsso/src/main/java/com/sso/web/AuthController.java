/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: AuthController
 * Author:   康鸿
 * Date:     2019/8/5 16:03
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.sso.web;

import com.alibaba.fastjson.JSON;
import com.kh.exception.LoginException;
import com.kh.jwt.JWTUtils;
import com.kh.pojo.ResponseResult;
import com.kh.pojo.entity.UserInfo;
import com.kh.random.VerifyCodeUtils;
import com.kh.utils.HttpUtils;
import com.kh.utils.MD5;
import com.kh.utils.UID;
import com.sso.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/5
 * @since 1.0.0
 * Description
 */

@Controller
@Api(tags = "sso服务接口")
public class AuthController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserService userService;


    //      登录操作
    @ResponseBody
    @RequestMapping("login")
    @ApiOperation("登录方法")
    public ResponseResult toLogin(@RequestBody Map<String, Object> map) throws LoginException {
        ResponseResult responseResult = ResponseResult.getResponseResult();

        //获取生成的验证码
        String code = redisTemplate.opsForValue().get(map.get("codekey").toString());

        //传入的验证码是否与生成后存在redis中的一样
        if (code == null || !code.equals(map.get("code").toString())) {
            responseResult.setCode(500);
            responseResult.setError("验证码错误，请刷新页面登录");
            return responseResult;
        }

        //进行用户密码校验
        if (map != null && map.get("loginname") != null) {
            UserInfo user = userService.getUserByLogin(map.get("loginname").toString());
            if (user != null) {
                //对比密码
                String password = MD5.encryptPassword(map.get("password").toString(), "kh");//盐用作进一步加密
                if (user.getPassword().equals(password)) {
                    //将用户信息转化为字符串
                    String userinfo = JSON.toJSONString(user);

                    //将用户信息使用JWt进行加密，将加密信息作为票据
                    String token = JWTUtils.generateToken(userinfo);

                    //将加密信息存入返回值
                    responseResult.setToken(token);


                    //↓↓↓↓↓↓↓↓↓↓↓↓↓  折线图开始登录一次存数据   ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
                    Date date = new Date();
                    String currdate = simpleDateFormat.format(date);

                    if (!redisTemplate.hasKey("tj" + user.getId())) { //判断是否存在
                        redisTemplate.opsForValue().set("tj" + user.getId(), currdate);
                        redisTemplate.expire("tj" + user.getId(), 1440, TimeUnit.MINUTES);

                        //自增不重复   有则加1  无责存1
                        redisTemplate.opsForHash().increment("number", currdate, 1);

                        //总条数大于七天删除小健
                        if (redisTemplate.opsForHash().size("number") > 7) {
                            Set<Object> number = redisTemplate.opsForHash().keys("number");
                            for (Object o : number) {
                                redisTemplate.opsForHash().delete("number", o.toString());
                                break;
                            }
                        }
                    } else {
                        String s = redisTemplate.opsForValue().get("tj" + user.getId());
                        if (!s.equals(currdate)) {
                            redisTemplate.delete("tj" + user.getId());
                            redisTemplate.opsForValue().set("tj" + user.getId(), currdate);
                            redisTemplate.expire(user.getId().toString(), 1440, TimeUnit.MINUTES);
                            //将生成的token存储到redis库
                            redisTemplate.opsForHash().increment("number", currdate, 1);

                            if (redisTemplate.opsForHash().size("number") > 7) {
                                Set<Object> number = redisTemplate.opsForHash().keys("number");
                                for (Object o : number) {
                                    redisTemplate.opsForHash().delete("number", o.toString());
                                    break;
                                }
                            }
                        }
                    }
                    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑  折线图结束  ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑


                    //将生成的token存到redis
                    redisTemplate.opsForValue().set("USERINFO" + user.getId().toString(), token);

                    //将用户的权限信息存入缓存
                    redisTemplate.opsForHash().putAll("USERDATAAUTH" + user.getId().toString(), user.getAuthmap());

                    //设置过期时间30分钟
                    redisTemplate.expire("USERINFO" + user.getId().toString(), 1800, TimeUnit.SECONDS);
                    //设置返回值
                    responseResult.setResult(user);
                    responseResult.setCode(200);
                    //成功信息
                    responseResult.setSuccess("登陆成功");


                    return responseResult;

                } else {
                    throw new LoginException("用户名或密码错误");
                }
            } else {
                throw new LoginException("用户名或密码错误");
            }
        } else {
            throw new LoginException("用户名或密码错误");
        }
    }


    //      滑动获取验证码
    @ApiOperation("获取验证码方法")
    @RequestMapping("getCode")
    @ResponseBody
    public ResponseResult getCode(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        //生成一个长度是5的随机字符串
        String code = VerifyCodeUtils.generateVerifyCode(5);
        ResponseResult responseResult = ResponseResult.getResponseResult();
        //将随机字符串放入返回结果
        responseResult.setResult(code);
        String uidCode = "CODE" + UID.getUUID16();
        //将生成的随机字符串标识后存入redis
        redisTemplate.opsForValue().set(uidCode, code);
        //设置过期时间
        redisTemplate.expire(uidCode, 1, TimeUnit.MINUTES);
        //回写cookie
        Cookie cookie = new Cookie("authcode", uidCode);
        //本应用获取cookie
        cookie.setPath("/");
        //跨域获取cookie
        cookie.setDomain("localhost");
        response.addCookie(cookie);

        return responseResult;

    }


    //退出
    @ApiOperation("退出方法")
    @RequestMapping("loginout")
    @ResponseBody
    public ResponseResult loginout(@RequestBody Map<String, Object> map) {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        System.out.println(map.get("id").toString());
        //删除权限redis
        redisTemplate.delete("USERDATAAUTH" + map.get("id").toString());
        //删除用户redis
        redisTemplate.delete("USERINFO" + map.get("id").toString());

        responseResult.setSuccess("ok");
        return responseResult;
    }


    //发短信接口
    @ApiOperation("发送验证码方法")
    @RequestMapping("yanzhengma")
    @ResponseBody
    public ResponseResult sendCode(HttpServletRequest request, HttpServletResponse response1,@RequestBody Map<String, Object> map) {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "925b38d0e0fb49a49e698db5e1ab8085";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appcode);

        //生成四位验证码
        int code = (int) (Math.random() * 9000) + 1000;

        Cookie[] cookies = request.getCookies();
        String uidCode = "PCODE" + UID.getUUID16();
        //将生成的随机字符串标识后存入redis
        redisTemplate.opsForValue().set(uidCode, String.valueOf(code));
        //设置过期时间
        redisTemplate.expire(uidCode, 1, TimeUnit.MINUTES);
        //回写cookie
        Cookie cookie = new Cookie("phonecode", uidCode);

        //本应用获取cookie
        cookie.setPath("/");
        //跨域获取cookie
        cookie.setDomain("localhost");
        response1.addCookie(cookie);
        String tel = String.valueOf(map.get("tel"));

        Map<String, String> querys = new HashMap<String, String>();


        querys.put("mobile", tel);

        querys.put("param", "code:" + code);


        querys.put("tpl_id", "TP1711063");
        Map<String, String> bodys = new HashMap<String, String>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);

            System.out.println(response.toString() + "=============================返回值");
            responseResult.setCode(200);
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseResult;
    }


    //查折线图数据
    @ApiOperation("折线图")
    @RequestMapping("zhexian")
    @ResponseBody
    public Object zhexian() {
        Map<String, List> map = new HashMap<>();
        //获取hash中所有小键值对
        Cursor<Map.Entry<Object, Object>> number = redisTemplate.opsForHash().scan("number", ScanOptions.NONE);
        //存放小键
        List<String> key1 = new ArrayList<>();
        //值
        List<String> value1 = new ArrayList<>();
        while (number.hasNext()) {
            Map.Entry<Object, Object> entry = number.next();
            key1.add(entry.getKey().toString());
            value1.add(entry.getValue().toString());
        }
        map.put("key1", key1);
        map.put("value1", value1);
        return map;
    }


    //验证码登录操作
    @ResponseBody
    @RequestMapping("logintel")
    @ApiOperation("短信登录方法")
    public ResponseResult tophoneLogin(@RequestBody Map<String, Object> map) throws LoginException {
        ResponseResult responseResult = ResponseResult.getResponseResult();

        //获取前台输入的验证码
        String telcode = String.valueOf(map.get("telcode"));


//        //获取生成的验证码
//        String code = redisTemplate.opsForValue().get(map.get("phonecode").toString());

        //传入的验证码是否与生成后存在redis中的一样
        System.out.println(telcode+"=============传入的验证码");




        System.out.println(map.get("phonecode").toString()+"=================获取的key");
        String phonecode = redisTemplate.opsForValue().get(map.get("phonecode").toString());


        if (telcode == null || !telcode.equals(phonecode)) {
            responseResult.setCode(500);
            responseResult.setError("验证码错误");
            return responseResult;
        }

        String tel = String.valueOf(map.get("tel"));
        UserInfo byTel2 = userService.findByTel(tel);


        UserInfo user = userService.getUserByLogin(byTel2.getLoginName().toString());


        if (user != null) {
            //将用户信息转化为字符串
            String userinfo = JSON.toJSONString(user);

            //将用户信息使用JWt进行加密，将加密信息作为票据
            String token = JWTUtils.generateToken(userinfo);

            //将加密信息存入返回值
            responseResult.setToken(token);


            //↓↓↓↓↓↓↓↓↓↓↓↓↓  折线图开始登录一次存数据   ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
            Date date = new Date();
            String currdate = simpleDateFormat.format(date);

            if (!redisTemplate.hasKey("tj" + user.getId())) { //判断是否存在
                redisTemplate.opsForValue().set("tj" + user.getId(), currdate);
                redisTemplate.expire("tj" + user.getId(), 1440, TimeUnit.MINUTES);

                //自增不重复   有则加1  无责存1
                redisTemplate.opsForHash().increment("number", currdate, 1);

                //总条数大于七天删除小健
                if (redisTemplate.opsForHash().size("number") > 7) {
                    Set<Object> number = redisTemplate.opsForHash().keys("number");
                    for (Object o : number) {
                        redisTemplate.opsForHash().delete("number", o.toString());
                        break;
                    }
                }
            } else {
                String s = redisTemplate.opsForValue().get("tj" + user.getId());
                if (!s.equals(currdate)) {
                    redisTemplate.delete("tj" + user.getId());
                    redisTemplate.opsForValue().set("tj" + user.getId(), currdate);
                    redisTemplate.expire(user.getId().toString(), 1440, TimeUnit.MINUTES);
                    //将生成的token存储到redis库
                    redisTemplate.opsForHash().increment("number", currdate, 1);

                    if (redisTemplate.opsForHash().size("number") > 7) {
                        Set<Object> number = redisTemplate.opsForHash().keys("number");
                        for (Object o : number) {
                            redisTemplate.opsForHash().delete("number", o.toString());
                            break;
                        }
                    }
                }
            }
            //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑  折线图结束  ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
            //将生成的token存到redis
            redisTemplate.opsForValue().set("USERINFO" + user.getId().toString(), token);




            //将用户的权限信息存入缓存
            redisTemplate.opsForHash().putAll("USERDATAAUTH" + user.getId().toString(), user.getAuthmap());

            //设置过期时间30分钟
            redisTemplate.expire("USERINFO" + user.getId().toString(), 1800, TimeUnit.SECONDS);
            //设置返回值
            responseResult.setResult(user);
            responseResult.setCode(200);
            //成功信息
            responseResult.setSuccess("登陆成功");

        } else {
            throw new LoginException("手机号不存在");
        }


        return responseResult;

    }


}


