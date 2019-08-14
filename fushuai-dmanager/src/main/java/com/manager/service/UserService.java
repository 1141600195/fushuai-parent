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
package com.manager.service;


import com.kh.pojo.entity.RoleInfo;
import com.kh.pojo.entity.UserInfo;


import com.manager.dao.RoleDao;
import com.manager.dao.UserDao;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
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
@Transactional
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @PersistenceContext
    EntityManager entityManager;


    public Object userList(Integer page, Integer pageSize, String data1, String data2, String sex, String userName) {

        //查询
        StringBuffer stringBuffer = new StringBuffer("select * from base_user where 1=1");
        //总条数
        StringBuffer stringBufferCount = new StringBuffer("select count(1) from base_user where 1=1");

        if (userName != null && !userName.equals("")) {
            //区间查询
            stringBuffer.append(" and userName like concat('%','" + userName + "','%')");
            //总条数
            stringBufferCount.append(" and userName like concat('%','" + userName + "','%')");
        }
        if ((data1 != null && !data1.equals("")) && (data2.equals("") || data2 == null)) {
            //区间查询
            stringBuffer.append(" and createTime > '" + data1 + "' ");
            //总条数
            stringBufferCount.append(" and createTime > '" + data1 + "' ");
        }
        if ((data1.equals("") || data1 == null) && (data2 != null && !data2.equals(""))) {
            //区间查询
            stringBuffer.append(" and createTime < '" + data2 + "' ");
            //总条数
            stringBufferCount.append(" and createTime < '" + data2 + "' ");
        }

        if ((data1 != null && !data1.equals("")) && (data2 != null && !data2.equals(""))) {
            //区间查询
            stringBuffer.append(" and createTime between '" + data1 + "' and '" + data2 + "'");
            //总条数
            stringBufferCount.append(" and createTime between '" + data1 + "' and '" + data2 + "' ");
        }
        if (sex != null && !sex.equals("")) {
            //区间查询
            stringBuffer.append(" and sex=" + sex);
            //总条数
            stringBufferCount.append(" and sex=" + sex);
        }

        stringBuffer.append(" limit " + (page - 1) * pageSize + "," + pageSize);

        System.out.println("stringBuffer=" + stringBuffer);
        System.out.println("stringBufferCount=" + stringBufferCount);
        //清一下缓存
        entityManager.clear();
        //查列表
        Query nativeQuery = entityManager.createNativeQuery(stringBuffer.toString(), UserInfo.class);
        //总条数
        Query nativeQueryCount = entityManager.createNativeQuery(stringBufferCount.toString());

        //查用户
        List<UserInfo> resultList = nativeQuery.getResultList();
        //角色放入权限
        if (resultList != null && resultList.size() > 0) {
            for (UserInfo userInfo : resultList) {
                RoleInfo roleInfo = roleDao.getRoleInfoByUserId(userInfo.getId());
                if (roleInfo != null) {
                    userInfo.setRoleName(roleInfo.getRoleName());
                }
            }
        }
        Map<String, Object> map = new HashMap<String, Object>();
        //用户信息
        map.put("content", resultList);
        map.put("totalElements", nativeQueryCount.getSingleResult());
        return map;

    }


    //通过id删除用户
    public int deleteUserById(Long id) {
        userDao.deleteById(id);
        return 200;
    }

    //增加用户
    public int addUser(UserInfo userInfo) {
        userDao.save(userInfo);
        return 200;
    }

    //修改用户
    public int updateUser(UserInfo userInfo) {
        userDao.save(userInfo);
        return 200;
    }

    //根据登录名查找用户做唯一验证

    public UserInfo selectUserByLoginName(String loginName) {
        UserInfo byLoginName = userDao.findByLoginName(loginName);
        return byLoginName;
    }

}