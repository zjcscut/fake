package org.throwable.fake.mapper.support.plugins.sort;

import lombok.Data;
import org.throwable.fake.mapper.utils.AssertUtils;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/7 12:33
 */
@Data
public class Order {

	private static final Direction DEFAULT_DIRECTION = Direction.ASC;
	private final Direction direction;
	private final String property;

	public Order(Direction direction, String property) {
		AssertUtils.notNull(direction, "Direction of order clause must not be null!");
		AssertUtils.notBlank(property, "Property of order clause must not be blank!");
		this.direction = direction;
		this.property = property;
	}

	public Order(String property) {
		this(DEFAULT_DIRECTION, property);
	}
}
