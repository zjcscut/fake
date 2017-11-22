package org.throwable.fake.mapper.support.plugins.condition.filter;

import com.google.common.collect.Sets;

import java.util.Set;

import static org.throwable.fake.mapper.common.constant.Constants.COMMA;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/3/31 0:14
 */
public class IncludeFieldFilter implements FieldFilter {

    private final String fields;

    public IncludeFieldFilter(String fields) {
        this.fields = fields;
    }

    @Override
    public boolean isIncludeFilter() {
        return true;
    }

    @Override
    public Set<String> accept() {
        return Sets.newHashSet(fields.split(COMMA));
    }
}
