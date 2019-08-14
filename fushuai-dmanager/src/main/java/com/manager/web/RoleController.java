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
import com.kh.pojo.entity.RoleInfo;

import com.kh.pojo.entity.RoleMenuInfo;
import com.kh.pojo.entity.UserRoleInfo;
import com.kh.utils.TwitterIdWorker;
import com.manager.dao.MenuDao;
import com.manager.service.RoleMenuService;
import com.manager.service.RoleService;
import com.manager.service.UserRoleService;
import com.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/8
 * @since 1.0.0
 * Description
 */

@Controller
public class RoleController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RoleService roleService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RoleMenuService roleMenuService;

    @Autowired
    private MenuDao menuDao;


    /**
     * 角色全查
     */
    @RequestMapping("roleList")
    @ResponseBody
    public Object userList(@RequestBody Map<String, String> info) {
        Integer page = Integer.parseInt(info.get("page"));
        Integer pageSize = Integer.parseInt(info.get("pageSize"));
        String name = info.get("name");
        return roleService.roleList(page, pageSize, name);
    }


    //角色删除
    @RequestMapping("deleteRoleById")
    @ResponseBody
    public ResponseResult deleteRoleById(@RequestBody RoleInfo map) {
        ResponseResult responseResult = new ResponseResult();
        Long id = map.getId();
        //检测有没有绑定用户
        UserRoleInfo userRoleInfo = userRoleService.selectUserRoleByRoleId(id);
        System.out.println(userRoleInfo + "=========================================roleMenuInfos");

        if (userRoleInfo != null) {
            responseResult.setCode(500);
            responseResult.setError("绑定有角色");
        } else {
            try {
                //删除角色权限
                roleMenuService.deleteByRoleId(id);
                //删除角色
                roleService.deleteRoleById(id);

                responseResult.setCode(200);
                responseResult.setSuccess("删除成功");
            } catch (Exception e) {
                responseResult.setCode(500);
                responseResult.setError("删除失败");
            }
        }
        return responseResult;
    }

    //角色添加
    @RequestMapping("addRole")
    @ResponseBody
    public ResponseResult addRole(@RequestBody RoleInfo r) {
        ResponseResult responseResult = new ResponseResult();

        //设置id为雪花id
        TwitterIdWorker twitterIdWorker = new TwitterIdWorker();
        long id = twitterIdWorker.nextId();
        r.setId(id);

        //添加
        try {
            roleService.addRole(r);
            responseResult.setCode(200);
            responseResult.setSuccess("添加成功");
        } catch (Exception e) {
            responseResult.setCode(500);
            responseResult.setError("添加失败");
        }
        return responseResult;
    }

    //权限回显
    @RequestMapping("/findMenu")
    @ResponseBody
    public List<MenuInfo> findMenu() {

        return getForMenuInfo(0L);
    }

    public List<MenuInfo> getForMenuInfo(Long mid) {
        List<MenuInfo> firstMenuInfo = menuDao.findByParentId(mid);
        if (firstMenuInfo != null) {
            for (MenuInfo menuInfo : firstMenuInfo) {
                menuInfo.setMenuInfoList(getForMenuInfo(menuInfo.getId()));
            }
        }
        return firstMenuInfo;
    }


    @Autowired
    JdbcTemplate jdbcTemplate;

    //角色绑定权限
    @RequestMapping("/addRm")
    @Transactional
    @ResponseBody
    public ResponseResult addRm(@RequestBody RoleInfo roleInfo) {
        ResponseResult responseResult = new ResponseResult();

        try {
            roleService.addRole(roleInfo);

            String sql1 = "delete from base_role_menu where roleId=?";
            jdbcTemplate.update(sql1, roleInfo.getId());

            String[] ids = roleInfo.getIds();
            if (ids != null && !ids.equals("")) {
                //中间表添加权限
                String sql = "insert into base_role_menu(id,roleId,menuId) values(?,?,?)";
                List<Object[]> list = new ArrayList<>();
                TwitterIdWorker twitterIdWorker = new TwitterIdWorker();
                for (String s : ids) {

                    list.add(new Object[]{twitterIdWorker.nextId(), roleInfo.getId(), Long.parseLong(s)});
                }
                jdbcTemplate.batchUpdate(sql, list);
            }

            responseResult.setCode(200);
        } catch (NumberFormatException e) {

            responseResult.setCode(500);
        } catch (Exception e) {
            responseResult.setCode(500);
        }

        return responseResult;
    }


}