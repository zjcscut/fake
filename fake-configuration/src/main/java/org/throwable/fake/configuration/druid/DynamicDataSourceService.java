package org.throwable.fake.configuration.druid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.throwable.fake.configuration.mapper.UserDao;
import org.throwable.fake.configuration.mapper.entity.User;
import org.throwable.fake.druid.annotation.DynamicDataSource;
import org.throwable.fake.mapper.support.plugins.condition.Condition;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/24 12:14
 */
@Service
public class DynamicDataSourceService {

    @Autowired
    private UserDao userDao;

    @DynamicDataSource(lookupKey = "primary-doge")
    public User getUserByName(String name) {
        Condition condition = Condition.create(User.class);
        condition.eq("name", name);
        return userDao.selectOneByCondition(condition);
    }
}
