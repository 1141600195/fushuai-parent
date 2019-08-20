/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: EmailServiceApplicationTests
 * Author:   康鸿
 * Date:     2019/8/19 10:43
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.manager;


import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;


/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/19
 * @since 1.0.0
 * Description 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailServiceApplicationTests {

    // 发件人 账号和密码
    public static final String MY_EMAIL_ACCOUNT = "kanghong0000@163.com";
    public static final String MY_EMAIL_PASSWORD = "8227252822a";// 密码,是你自己的设置的授权码

    // SMTP服务器(这里用的163 SMTP服务器)
    public static final String MEAIL_163_SMTP_HOST = "smtp.163.com";
    public static final String SMTP_163_PORT = "25";// 端口号,这个是163使用到的;QQ的应该是465或者875

    // 收件人
    public static final String RECEIVE_EMAIL_ACCOUNT = "1141600195@qq.com";

    @Test
    public static void main(String[] args) throws AddressException, MessagingException {
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
        message.setRecipients(Message.RecipientType.TO, RECEIVE_EMAIL_ACCOUNT);


        // 内容(这个内容还不能乱写,有可能会被SMTP拒绝掉;多试几次吧)
        message.setSubject("密码重置");
        message.setContent("尊敬的用户，请点击<a href='https://localhost:8080/zhaohui2/'>这里</a>找回密码~", "text/html;charset=UTF-8");
        message.setSentDate(new Date());
        message.saveChanges();
        System.out.println("准备发送");
        Transport.send(message);
    }

}