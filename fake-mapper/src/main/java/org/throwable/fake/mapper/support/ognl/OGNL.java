package org.throwable.fake.mapper.support.ognl;

import org.throwable.fake.mapper.support.plugins.condition.Condition;
import org.throwable.fake.mapper.support.plugins.generator.identity.PrimaryKeyChecker;

import java.io.Serializable;
import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/18 11:08
 */
public abstract class OGNL {

	private final static String CLASS_NAME = "@org.throwable.fake.mapper.support.ognl.OGNL";

	public final static String HAS_CONDITION_ORDER_BY_CLAUSE = CLASS_NAME + "@hasConditionOrderByClause(%s)";
	public final static String HAS_CONDITION_SELECT_COLUMNS = CLASS_NAME + "@hasConditionSelectColumns(%s)";
	public final static String HAS_NONE_CONDITION_SELECT_COLUMNS = CLASS_NAME + "@hasNoneConditionSelectColumns(%s)";
	public final static String CHECK_PRIMARY_KEY_VALID = CLASS_NAME + "@checkPrimaryKeyTypeValid(%s)";
	public final static String CHECK_RECORDS_NOT_EMPTY = CLASS_NAME + "@checkRecordsNotEmpty(%s)";

	public static boolean hasConditionOrderByClause(Object parameter) {
		if (parameter != null && parameter instanceof Condition) {
			Condition condition = (Condition) parameter;
			return condition.getSort().getOrders().size() > 0;
		}
		return false;
	}

	public static boolean hasConditionSelectColumns(Object parameter) {
		if (parameter != null && parameter instanceof Condition) {
			Condition condition = (Condition) parameter;
			return null != condition.getSelectColumns() && condition.getSelectColumns().size() > 0;
		}
		return false;
	}

	public static boolean hasNoneConditionSelectColumns(Object parameter) {
		return !hasConditionSelectColumns(parameter);
	}

	public static boolean checkPrimaryKeyTypeValid(Object parameter) {
		if (null != parameter && parameter instanceof Serializable) {
			return PrimaryKeyChecker.checkPrimaryKeyTypeValid(parameter.getClass());
		}
		throw new IllegalArgumentException("Primary key null or primary key type is invalid!");
	}

	public static boolean checkRecordsNotEmpty(Object parameters){
		if (null != parameters && List.class.isAssignableFrom(parameters.getClass())){
             if (!((List)parameters).isEmpty()){
             	return true;
			 }
		}
		throw new IllegalArgumentException("Parameter -> target records must not be empty!");
	}

}
