package org.throwable.fake.mapper.support.assistant;

import org.throwable.fake.mapper.common.model.ColumnMetadata;
import org.throwable.fake.mapper.support.ognl.OGNL;

import static org.throwable.fake.mapper.common.constant.Constants.PARAM_PRIMARY_KEY;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/19 22:00
 */
public class DeleteAppendMetadataAssistant extends ConditionAppendMetadataAssistant {

	public static String checkPrimaryKeyValid() {
		return getTestClause(String.format(OGNL.CHECK_PRIMARY_KEY_VALID, PARAM_PRIMARY_KEY));
	}

	public static String deleteFromTable(Class<?> entityClass) {
		return "DELETE FROM " + fetchExistTableName(entityClass);
	}

	public static String primaryKeyWhereClause(Class<?> entityClass) {
		ColumnMetadata keyMetadata = fetchExistPrimaryColumnMetadata(entityClass);
		return String.format("\n<where>\n%s\n</where>\n",
				String.format("%s = #{%s,javaType=%s}",
						keyMetadata.getColumn(),
						PARAM_PRIMARY_KEY,
						keyMetadata.getJavaType().getCanonicalName()));
	}

}
