package org.throwable.fake.mapper;

import lombok.NonNull;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.throwable.fake.mapper.support.plugins.condition.Condition;
import org.throwable.fake.mapper.support.plugins.pagination.PageModel;
import org.throwable.fake.mapper.support.plugins.pagination.Pager;
import org.throwable.fake.mapper.support.provider.SelectMapperProvider;

import java.util.List;

import static org.throwable.fake.mapper.common.constant.Constants.FAKE_METHOD;
import static org.throwable.fake.mapper.common.constant.Constants.PARAM_CONDITION;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/16 23:24
 */
public interface SelectMapper<P, T> {

    default List<T> selectByConditionLimit(Condition condition, int limit) {
        condition.limit(0, limit);
        return selectByCondition(condition);
    }

    default PageModel<T> selectByConditionDirectLimit(Condition condition, int pageNumber, int pageSize) {
        long count = countByCondition(condition);
        condition.limit(pageNumber, pageSize);
        List<T> list = selectByCondition(condition);
        return new PageModel<>(pageNumber, pageSize, count, list);
    }

    default PageModel<T> selectByConditionPage(Condition condition, int pageNumber, int pageSize) {
        return selectByConditionPage(condition, new Pager(pageNumber, pageSize));
    }

    default PageModel<T> selectByConditionPage(Condition condition, Pager pager) {
        long count = countByCondition(condition);
        int lastPage = pager.getLastPage(count);
        int offset = pager.getOffset();
        if (offset > lastPage) {
            pager.setPageNumber(offset);
            condition.limit(lastPage, pager.getPageSize());
        } else {
            condition.limit(pager.getOffset(), pager.getPageSize());
        }
        List<T> list = selectByCondition(condition);
        return new PageModel<>(pager.getPageNumber(), pager.getPageSize(), count, list);
    }

    default T selectOneByCondition(Condition condition) {
        condition.limit(0, 1);
        List<T> list = selectByCondition(condition);
        if (null == list || list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    default PageModel<T> selectByConditionLimitOffset(Condition condition, int pageNumber, int pageSize) {
        return selectByConditionPage(condition, new Pager(pageNumber, pageSize));
    }

    default PageModel<T> selectByConditionLimitOffset(Condition condition, Pager pager) {
        long count = countByCondition(condition);
        int lastPage = pager.getLastPage(count);
        int offset = pager.getOffset();
        if (offset > lastPage) {
            pager.setPageNumber(offset);
            condition.limit(lastPage, pager.getPageSize());
        } else {
            condition.limit(pager.getOffset(), pager.getPageSize());
        }
        List<T> list = selectByConditionLimitOffset(condition);
        return new PageModel<>(pager.getPageNumber(), pager.getPageSize(), count, list);
    }

    @SelectProvider(type = SelectMapperProvider.class, method = FAKE_METHOD)
    Long countByCondition(@NonNull @Param(PARAM_CONDITION) Condition condition);

    @SelectProvider(type = SelectMapperProvider.class, method = FAKE_METHOD)
    List<T> selectByCondition(@NonNull @Param(PARAM_CONDITION) Condition condition);

    @SelectProvider(type = SelectMapperProvider.class, method = FAKE_METHOD)
    List<T> selectByConditionLimitOffset(@NonNull @Param(PARAM_CONDITION) Condition condition);
}
