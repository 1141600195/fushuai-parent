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
import com.kh.pojo.entity.RoleInfo;
import com.kh.pojo.entity.UserInfo;
import com.kh.pojo.entity.UserRoleInfo;
import com.kh.utils.MD5;
import com.kh.utils.TwitterIdWorker;
import com.kh.utils.UID;
import com.manager.service.RoleService;
import com.manager.service.UserRoleService;
import com.manager.service.UserService;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/8
 * @since 1.0.0
 * Description
 */

@Controller
public class UserController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 用户全查
     */
    @RequestMapping("userList")
    @ResponseBody
    public Object userList(@RequestBody Map<String, String> info) {

        Integer page = Integer.parseInt(info.get("page"));
        Integer pageSize = Integer.parseInt(info.get("pageSize"));
        String data1 = info.get("data1");
        String data2 = info.get("data2");
        String sex = info.get("sex");
        String name = info.get("name");

        System.out.println(data1 + "     ==========data1");
        System.out.println(data2 + "     ==========data2");

        return userService.userList(page, pageSize, data1, data2, sex, name);
    }


    //删除
    @RequestMapping("deleteUserById")
    @ResponseBody
    public ResponseResult deleteUserById(@RequestBody Map<String, String> map) {
        ResponseResult responseResult = new ResponseResult();

        try {
            //删除用户角色
            userRoleService.deleteUserRoleByUserId(Long.valueOf(map.get("id")));
            //删除用户
            int i = userService.deleteUserById(Long.valueOf(map.get("id")));
            responseResult.setCode(200);
            responseResult.setSuccess("删除成功");
        } catch (Exception e) {
            responseResult.setCode(500);
            responseResult.setError("删除失败");
        }
        return responseResult;
    }


    //增加
    @RequestMapping("addUser")
    @ResponseBody
    public int addUser(@RequestBody UserInfo userInfo) {
        UserInfo userInfo1 = userService.selectUserByLoginName(userInfo.getLoginName());
        if (userInfo1 != null) {
            return 505;
        } else {
            //设置id为雪花id
            TwitterIdWorker twitterIdWorker = new TwitterIdWorker();
            long id = twitterIdWorker.nextId();
            userInfo.setId(id);


            //密码加密
            String password = MD5.encryptPassword(userInfo.getPassword(), "kh");
            userInfo.setPassword(password);
            int i = userService.addUser(userInfo);
            return i;
        }
    }


    //修改
    @RequestMapping("updateUser")
    @ResponseBody
    public int updateUser(@RequestBody UserInfo userInfo) {

        //密码加密
        String password = MD5.encryptPassword(userInfo.getPassword(), "kh");
        userInfo.setPassword(password);

        int i = userService.updateUser(userInfo);
        return i;
    }


    //绑定角色回显
    @RequestMapping("bdrolelist")
    @ResponseBody
    public List<RoleInfo> bdrolelist() {
        List<RoleInfo> bdrolelist = roleService.bdrolelist();
        return bdrolelist;
    }

    //绑定角色回显
    @RequestMapping("adduserRole")
    @ResponseBody
    public int adduserRole(@RequestBody Map<String, String> map) {
        Long userId = Long.valueOf(map.get("userId"));
        Long roleId = Long.valueOf(map.get("roleId"));

        //查找是否有没有绑定
        UserRoleInfo userRoleInfo = userRoleService.selectUserRole(userId);
        if (userRoleInfo != null) {
            userRoleInfo.setRoleId(roleId);
            int i = userRoleService.adduserRole(userRoleInfo);
            return i;
        } else {
            UserRoleInfo userRoleInfo1 = new UserRoleInfo();
            //添加
            userRoleInfo1.setRoleId(roleId);
            userRoleInfo1.setUserId(userId);
            //设置id为雪花id
            TwitterIdWorker twitterIdWorker = new TwitterIdWorker();
            long id = twitterIdWorker.nextId();
            userRoleInfo1.setId(id);

            int i = userRoleService.adduserRole(userRoleInfo1);
            return i;
        }
    }

    //上传
    @RequestMapping("toUpload")
    @ResponseBody
    @CrossOrigin
    public Object toUpload(@RequestParam("file") MultipartFile[] file) throws IOException {
        file[0].transferTo(new File("D:/imgs/" + file[0].getOriginalFilename()));
        return "ok";
    }


    //导出
    @RequestMapping("daochu")
    @ResponseBody
    private int downUserList(@RequestBody List<Map<String, Object>> listresult) {
        // getTime()是一个返回当前时间的字符串，用于做文件名称
        TwitterIdWorker twitterIdWorker = new TwitterIdWorker();
        String name = String.valueOf(twitterIdWorker.nextId());//雪花起名字

        //  csvFile是我的一个路径，自行设置就行
        String csvFile = "C:\\Users\\xiaiyu\\Desktop";
        String ys = csvFile + "//" + name + ".xlsx";
        // 1.生成Excel
        XSSFWorkbook userListExcel = createUserListExcel(listresult);
        try {
            // 输出成文件
            File file = new File(csvFile);
            if (file.exists() || !file.isDirectory()) {
                file.mkdirs();
            }
            // TODO 生成的wb对象传输
            FileOutputStream outputStream = new FileOutputStream(new File(ys));
            userListExcel.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }


    //创建文件
    private XSSFWorkbook createUserListExcel(List<Map<String, Object>> listresult) {
        // 1.创建HSSFWorkbook，一个HSSFWorkbook对应一个Excel文件
        XSSFWorkbook wb = new XSSFWorkbook();
        // 2.在workbook中添加一个sheet,对应Excel文件中的sheet
        XSSFSheet sheet = wb.createSheet("sheet1");
        // 3.设置表头，即每个列的列名
        String[] titel = {"用户名字", "登录名字", "性别", "电话", "时间"};
        // 3.1创建第一行
        XSSFRow row = sheet.createRow(0);
        // 此处创建一个序号列
        row.createCell(0).setCellValue("序号");
        // 将列名写入
        for (int i = 0; i < titel.length; i++) {
            // 给列写入数据,创建单元格，写入数据
            row.createCell(i + 1).setCellValue(titel[i]);
        }
        // 写入正式数据
        for (int i = 0; i < listresult.size(); i++) {
            // 创建行
            row = sheet.createRow(i + 1);
            // 编号
            row.createCell(0).setCellValue(i + 1);
            // 用户名
            row.createCell(1).setCellValue(listresult.get(i).get("userName").toString());
            sheet.autoSizeColumn(1, true);//自动列宽
            // 登录名
            row.createCell(2).setCellValue(listresult.get(i).get("loginName").toString());
            // 性别
            row.createCell(3).setCellValue(listresult.get(i).get("sex").toString());
            // 电话
            row.createCell(4).setCellValue(listresult.get(i).get("tel").toString());
            //创建时间
            row.createCell(5).setCellValue(listresult.get(i).get("createTime").toString());
        }
        /**
         * 上面的操作已经是生成一个完整的文件了，只需要将生成的流转换成文件即可；
         * 下面的设置宽度可有可无，对整体影响不大
         */
        // 设置单元格宽度
        int curColWidth = 0;
        for (int i = 0; i <= titel.length; i++) {
            // 列自适应宽度，对于中文半角不友好，如果列内包含中文需要对包含中文的重新设置。
            sheet.autoSizeColumn(i, true);
            // 为每一列设置一个最小值，方便中文显示
            curColWidth = sheet.getColumnWidth(i);
            if (curColWidth < 2500) {
                sheet.setColumnWidth(i, 2500);
            }
            // 第3列文字较多，设置较大点。
            sheet.setColumnWidth(3, 8000);
        }
        return wb;
    }


    // 发件人 账号和密码
    public static final String MY_EMAIL_ACCOUNT = "kanghong0000@163.com";
    public static final String MY_EMAIL_PASSWORD = "8227252822a";// 密码,是你自己的设置的授权码

    // SMTP服务器(这里用的163 SMTP服务器)
    public static final String MEAIL_163_SMTP_HOST = "smtp.163.com";
    public static final String SMTP_163_PORT = "25";// 端口号,这个是163使用到的;QQ的应该是465或者875


    //发送邮件
    @RequestMapping("sendEmail")
    @ResponseBody
    public ResponseResult sendmail(@RequestBody Map<String, String> map) throws AddressException, MessagingException {

        ResponseResult responseResult = new ResponseResult();

        UserInfo toEmail = userService.selectByEmail(map.get("toEmail"));

        String uuid16 = UID.getUUID16();
        redisTemplate.opsForValue().set("p"+uuid16,toEmail.getId().toString());
        redisTemplate.expire("p"+uuid16,10, TimeUnit.MINUTES);//10分钟有效

        if (toEmail != null) {

            Properties p = new Properties();
            p.setProperty("mail.smtp.host", MEAIL_163_SMTP_HOST);
            p.setProperty("mail.smtp.port", SMTP_163_PORT);
            p.setProperty("mail.smtp.socketFactory.port", SMTP_163_PORT);
            p.setProperty("mail.smtp.auth", "true");
            p.setProperty("mail.smtp.socketFactory.class", "SSL_FACTORY");

            Session session = Session.getInstance(p, new Authenticator() {
                // 设置认证账户信息
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(MY_EMAIL_ACCOUNT, MY_EMAIL_PASSWORD);
                }
            });
            session.setDebug(true);
            System.out.println("创建邮件");
            MimeMessage message = new MimeMessage(session);
            // 发件人
            message.setFrom(new InternetAddress(MY_EMAIL_ACCOUNT));
            // 收件人和抄送人
            message.setRecipients(Message.RecipientType.TO, map.get("toEmail"));


            // 内容(这个内容还不能乱写,有可能会被SMTP拒绝掉;多试几次吧)
            message.setSubject("密码重置");
            message.setContent("尊敬的用户，请点击<a href='https://localhost:8080/zhaohui2?sid=" + "p"+uuid16 + "'>这里</a>", "text/html;charset=UTF-8");
            message.setSentDate(new Date());
            message.saveChanges();
            System.out.println("准备发送");
            Transport.send(message);

            responseResult.setCode(200);
            responseResult.setSuccess("发送成功");
        } else {
            responseResult.setCode(500);
            responseResult.setSuccess("没有此邮箱");
        }


        return responseResult;
    }


    //修改密码
    @RequestMapping("updatePassword")
    @ResponseBody
    public ResponseResult updatePassword(@RequestBody Map<String, String> map) {

        String id = redisTemplate.opsForValue().get(map.get("id"));

        ResponseResult responseResult = new ResponseResult();

        UserInfo user = userService.selectByUserinfoById(Long.valueOf(id));

        String password = MD5.encryptPassword(map.get("password"), "kh");//盐用作进一步加密
        user.setPassword(password);

        userService.addUser(user);
        responseResult.setCode(200);
        responseResult.setSuccess("修改成功");
        return responseResult;
    }

    //修改密码
    @RequestMapping("jianCeRedisp")
    @ResponseBody
    public ResponseResult jianCeRedisp(@RequestBody Map<String, String> map) {
        ResponseResult responseResult = new ResponseResult();

        String id = redisTemplate.opsForValue().get(map.get("id"));
        if(id!=null&&!id.equals("")){
            responseResult.setCode(200);
        }else {
            responseResult.setCode(500);
        }


        return responseResult;
    }

    }