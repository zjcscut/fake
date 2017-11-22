package org.throwable.fake.configuration.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.throwable.fake.configuration.mapper.entity.User;
import org.throwable.fake.mapper.FakeMapper;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/22 15:47
 */
@Mapper
public interface UserDao extends FakeMapper<Long, User> {
}
