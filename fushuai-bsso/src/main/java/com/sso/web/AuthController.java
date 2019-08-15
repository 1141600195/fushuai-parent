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
import com.kh.utils.MD5;
import com.kh.utils.UID;
import com.sso.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;
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
        //删除权限redis
        redisTemplate.delete("USERDATAAUTH" + map.get("id").toString());
        //删除用户redis
        redisTemplate.delete("USERINFO" + map.get("id").toString());

        responseResult.setSuccess("ok");
        return responseResult;
    }


}