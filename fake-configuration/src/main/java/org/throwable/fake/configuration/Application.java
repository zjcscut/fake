package org.throwable.fake.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.throwable.fake.configuration.mapper.UserDao;
import org.throwable.fake.configuration.mapper.entity.User;
import org.throwable.fake.mapper.support.plugins.condition.Condition;

import java.time.LocalDateTime;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/16 23:43
 */
@EnableFake
@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        mapper();
    }

    @Autowired
    private UserDao userDao;

    private void mapper() {
        Condition condition = Condition.create(User.class);
        condition.eq("name", "doge");
        System.out.println(userDao.selectOneByCondition(condition));
        User user = new User();
        user.setName("傻逼二号");
        user.setBirth(LocalDateTime.now());
        int insert = userDao.insert(user);
        System.out.println("插入数据" + (1 == insert ? "成功" : "失败"));
    }
}