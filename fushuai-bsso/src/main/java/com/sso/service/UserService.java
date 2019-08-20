/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: UserService
 * Author:   康鸿
 * Date:     2019/8/5 19:31
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.sso.service;

import com.kh.pojo.entity.MenuInfo;
import com.kh.pojo.entity.RoleInfo;
import com.kh.pojo.entity.UserInfo;
import com.sso.dao.MenuDao;
import com.sso.dao.RoleDao;
import com.sso.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/5
 * @since 1.0.0
 * Description
 */
@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private RoleDao roleDao;

    //登陆时获取用户信息
    public UserInfo getUserByLogin(String loginName) {
        //获取用户信息
        UserInfo byLoginName = userDao.findByLoginName(loginName);

        if (byLoginName != null) {
            //获得用户角色的信息
            RoleInfo roleInfoByUserId = roleDao.getRoleInfoByUserId(byLoginName.getId());
            //设置用户角色
            byLoginName.setRoleInfo(roleInfoByUserId);

            if (roleInfoByUserId != null) {
                //递归获得菜单权限   存入hashtable安全
                Map<String, String> authMap = new Hashtable<>();
                //调用递归
                List<MenuInfo> forMenuInfo = getForMenuInfo(roleInfoByUserId.getId(), 0L, authMap);
                //设置菜单子权限
                byLoginName.setAuthmap(authMap);
                byLoginName.setListMenuInfo(forMenuInfo);
            }
        }
        return byLoginName;
    }


    /**
     * 获取子权限递归方法
     */
    public List<MenuInfo> getForMenuInfo(Long roleId, Long pid, Map<String, String> authMap) {
        //获取下级菜单信息
        List<MenuInfo> firstMenuInfo1 = menuDao.getMenuInfo(roleId, pid);
        if (firstMenuInfo1 != null) {
            //整理后台的数据访问链接
            for (MenuInfo menu : firstMenuInfo1) {
                if (menu.getLeval() == 4) {
                    authMap.put(menu.getUrl(), "");
                }
                menu.setMenuInfoList(getForMenuInfo(roleId, menu.getId(), authMap));
            }
        }
        return firstMenuInfo1;
    }


    //通过手机号查找用户
    public UserInfo findByTel(String tel) {
        UserInfo byTel = userDao.findByTel(tel);
        return byTel;
    }


}