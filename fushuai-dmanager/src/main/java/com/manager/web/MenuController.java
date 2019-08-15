/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: UserController
 * Author:   康鸿
 * Date:     2019/8/8 14:17
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.manager.web;


import com.kh.pojo.ResponseResult;
import com.kh.pojo.entity.MenuInfo;
import com.kh.utils.TwitterIdWorker;
import com.manager.dao.MenuDao;
import com.manager.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/12
 * @since 1.0.0
 * Description
 */

@Controller
public class MenuController {
    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuDao menuDao;

    //获取权限列表
    @RequestMapping("menuList")
    @ResponseBody
    public ResponseResult findMenu() {
        ResponseResult responseResult = new ResponseResult();

        List<MenuInfo> forMenuInfo = menuService.getForMenuInfo(0L);
        responseResult.setResult(forMenuInfo);
        responseResult.setCode(200);
        responseResult.setSuccess("列表返回成功");
        return responseResult;
    }


    @RequestMapping("/addMenu")
    @ResponseBody
    public ResponseResult addMenu(@RequestBody MenuInfo menuInfo){
        ResponseResult responseResult=ResponseResult.getResponseResult();
        TwitterIdWorker twitterIdWorker=new TwitterIdWorker();
        long id = twitterIdWorker.nextId();
        menuInfo.setId(id);
        menuDao.save(menuInfo);
        menuDao.flush();
        responseResult.setCode(200);
        return responseResult;
    }
    @RequestMapping("/updateMenu")
    @ResponseBody
    public ResponseResult updateMenu(@RequestBody MenuInfo menuInfo){
        ResponseResult responseResult=ResponseResult.getResponseResult();
        menuDao.save(menuInfo);
        menuDao.flush();
        responseResult.setCode(200);
        return responseResult;
    }
    @Transactional
    @RequestMapping("/delMenu")
    @ResponseBody
    public ResponseResult delMenu(@RequestBody MenuInfo menuInfo){
        ResponseResult responseResult=ResponseResult.getResponseResult();
        Long[] ids = menuInfo.getIds();
        List<MenuInfo> allById = menuDao.findAllById(Arrays.asList(ids));
        menuDao.deleteAll(allById);
        responseResult.setCode(200);
        return responseResult;
    }



}