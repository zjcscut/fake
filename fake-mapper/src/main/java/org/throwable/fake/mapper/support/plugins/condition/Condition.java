package org.throwable.fake.mapper.support.plugins.condition;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NonNull;
import org.throwable.fake.mapper.common.constant.Op;
import org.throwable.fake.mapper.common.model.ColumnMetadata;
import org.throwable.fake.mapper.common.model.TableMetadata;
import org.throwable.fake.mapper.configuration.StaticMapperRegistry;
import org.throwable.fake.mapper.configuration.TableMetadataParser;
import org.throwable.fake.mapper.exception.UnsupportedElementException;
import org.throwable.fake.mapper.support.assistant.FieldFilterAssistant;
import org.throwable.fake.mapper.support.plugins.condition.filter.FieldFilter;
import org.throwable.fake.mapper.support.plugins.sort.Direction;
import org.throwable.fake.mapper.support.plugins.sort.Order;
import org.throwable.fake.mapper.support.plugins.sort.Sort;
import org.throwable.fake.mapper.utils.AssertUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.throwable.fake.mapper.common.constant.Constants.*;


public class Condition {

	private static final boolean FORCE_MODE = true;

	@Getter
	private final Class<?> entity;

	@Getter
	private final Sort sort;

	@Getter
	private FieldFilter fieldFilter;

	@Getter
	private boolean isDistinct;

	@Getter
	private boolean forceMode = FORCE_MODE;

	private final TableMetadata tableMetadata;

	private final Map<String, ColumnMetadata> propertyMap;

	@Getter
	private Set<String> selectColumns;

	@Getter
	private Map<String, Object> updateColumnMap;

	@Getter
	private LinkedList<CriteriaCollection> criteriaCollection;

	@Getter
	private Limit limit;

	private Condition(Class<?> entity) {
		this.entity = entity;
		this.sort = new Sort();
		this.criteriaCollection = Lists.newLinkedList();
		this.criteriaCollection.addLast(new CriteriaCollection());
		TableMetadata tableMetadata = StaticMapperRegistry.getTableMetadataByEntityClass(entity);
		if (null == tableMetadata) {
			tableMetadata = TableMetadataParser.processTableMetadata(entity);
		}
		this.tableMetadata = tableMetadata;
		AssertUtils.notNull(this.tableMetadata, "Init condition instance failed,entity:[%s]", entity.getName());
		this.propertyMap = this.tableMetadata.getPropertyMap();
	}

	public static Condition create(Class<?> entity) {
		return new Condition(entity);
	}

	public Condition orderBy(String property, String clause) {
		if (checkMatchColumn(property)) {
			sort.addSort(new Order(Direction.fromString(clause), matchColumn(property)));
		}
		return this;
	}

	public Condition desc(String property) {
		if (checkMatchColumn(property)) {
			sort.addSort(new Order(Direction.DESC, matchColumn(property)));
		}
		return this;
	}

	public Condition asc(String property) {
		if (checkMatchColumn(property)) {
			sort.addSort(new Order(Direction.ASC, matchColumn(property)));
		}
		return this;
	}

	public Condition addFieldFilter(FieldFilter fieldFilter) {
		boolean pass = false;
		for (String temp : fieldFilter.accept()) {
			pass = checkMatchColumn(temp);
		}
		if (pass) {
			this.fieldFilter = fieldFilter;
			this.selectColumns = convertFieldFilterToSelectColumns(fieldFilter, entity);
		}
		return this;
	}

	public Condition distinct(boolean isDistinct) {
		this.isDistinct = isDistinct;
		return this;
	}

	public Condition eq(String key, Object value) {
		if (checkMatchColumn(key)) {
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_EQ, value));
		}
		return this;
	}

	public Condition gt(String key, Object value) {
		if (checkMatchColumn(key)) {
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_GT, value));
		}
		return this;
	}

	public Condition gteq(String key, Object value) {
		if (checkMatchColumn(key)) {
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_GTEQ, value));
		}
		return this;
	}

	public Condition lt(String key, Object value) {
		if (checkMatchColumn(key)) {
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_LT, value));
		}
		return this;
	}

	public Condition lteq(String key, Object value) {
		if (checkMatchColumn(key)) {
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_LTEQ, value));
		}
		return this;
	}

	public Condition and(String key, @NonNull Op op, Object value) {
		switch (op) {
			case EQ:
				eq(key, value);
				break;
			case GT:
				gt(key, value);
				break;
			case LT:
				lt(key, value);
				break;
			case GTEQ:
				gteq(key, value);
				break;
			case LTEQ:
				lteq(key, value);
				break;
			case IN:
				in(key, value);
				break;
			case NOT_IN:
				notIn(key, value);
				break;
			case LIKE:
				like(key, value);
				break;
			case NOT_LIKE:
				notLike(key, value);
				break;
			case NOT_LIKE_LEFT:
				notLike(key, value, true, false);
				break;
			case NOT_LIKE_RIGHT:
				notLike(key, value, false, true);
				break;
			case LIKE_LEFT:
				like(key, value, true, false);
				break;
			case LIKE_RIGHT:
				like(key, value, false, true);
				break;
			default: {
				eq(key, value);
			}
		}
		return this;
	}

	public Condition in(String key, Object values) {
		return in(key, convertStringToCollection(values));
	}

	public Condition in(String key, Collection<?> values) {
		if (checkMatchColumn(key)) {
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_IN, values));
		}
		return this;
	}

	public Condition notIn(String key, Object values) {
		return notIn(key, convertStringToCollection(values));
	}

	public Condition notIn(String key, Collection<?> values) {
		if (checkMatchColumn(key)) {
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_NOT_IN, values));
		}
		return this;
	}

	public Condition notLike(String key, Object value) {
		return notLike(key, value, true, true);
	}

	public Condition notLike(String key, Object value, boolean left, boolean right) {
		if (checkMatchColumn(key)) {
			if (left) {
				value = "%" + value;
			}
			if (right) {
				value = value + "%";
			}
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_NOT_LIKE, value));
		}
		return this;
	}

	public Condition like(String key, Object value) {
		return like(key, value, true, true);
	}

	public Condition like(String key, Object value, boolean left, boolean right) {
		if (checkMatchColumn(key)) {
			if (left) {
				value = "%" + value;
			}
			if (right) {
				value = value + "%";
			}
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_LIKE, value));
		}
		return this;
	}

	public Condition between(String key, Object leftValue, Object rightValue) {
		if (checkMatchColumn(key)) {
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_BETWEEN, leftValue, rightValue));
		}
		return this;
	}

	public Condition isTrue(String key) {
		if (checkMatchColumn(key))
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_IS_TRUE));
		return this;
	}

	public Condition isNull(String key) {
		if (checkMatchColumn(key)) {
			this.criteriaCollection.getLast().addCriteria(new Criteria(matchColumn(key) + CONDITION_CLAUSE_IS_NULL));
		}
		return this;
	}

	public Condition or(String key, @NonNull Op op, Object value) {
		if (checkMatchColumn(key)) {
			this.criteriaCollection.addLast(new CriteriaCollection());
			and(key, op, value);
		}
		return this;
	}

	public Condition limit(int offset, int size) {
		this.limit = new Limit(offset, size);
		return this;
	}

	public Condition forceMode(boolean open) {
		this.forceMode = open;
		return this;
	}

	public Condition staticSql(String sql) {
		this.criteriaCollection.getLast().addCriteria(new Criteria(sql));
		return this;
	}

	public Condition setVar(String field, Object value) {
		if (checkMatchColumn(field)) {
			if (null == updateColumnMap) {
				updateColumnMap = new HashMap<>();
				updateColumnMap.put(matchColumn(field), value);
			} else {
				updateColumnMap.put(matchColumn(field), value);
			}
		}
		return this;
	}

	public Condition setVars(Map<String, Object> vars) {
		AssertUtils.notEmpty(vars, "Condition vars params to set must not be empty!");
		vars.forEach((key, value) -> {
			if (checkMatchColumn(key)) {
				if (null == updateColumnMap) {
					updateColumnMap = new HashMap<>();
					updateColumnMap.put(matchColumn(key), value);
				} else {
					updateColumnMap.put(matchColumn(key), value);
				}
			}
		});
		return this;
	}

	@Getter
	public static class CriteriaCollection {

		private LinkedList<Criteria> criteriaLinkedList;

		public CriteriaCollection() {
			criteriaLinkedList = Lists.newLinkedList();
		}

		public void addCriteria(Criteria criteria) {
			criteriaLinkedList.addLast(criteria);
		}

		public boolean valid() {
			return criteriaLinkedList.size() > 0;
		}
	}

	@Getter
	private static class Criteria {

		private String conditionClause;
		private Object leftValue;
		private Object rightValue;
		private boolean noneValue;
		private boolean singleValue;
		private boolean betweenValue;
		private boolean collectionValue;

		public Criteria(String conditionClause) {
			super();
			this.conditionClause = conditionClause;
			this.noneValue = true;
		}

		public Criteria(String conditionClause, Object value) {
			super();
			this.conditionClause = conditionClause;
			this.leftValue = value;
			if (value instanceof Collection<?>) {
				this.collectionValue = true;
			} else {
				this.singleValue = true;
			}
		}

		public Criteria(String conditionClause, Object leftValue, Object rightValue) {
			super();
			this.conditionClause = conditionClause;
			this.leftValue = leftValue;
			this.rightValue = rightValue;
			this.betweenValue = true;
		}

	}

	@Getter
	private static class Limit {

		private final int offset;
		private final int size;

		public Limit(int offset, int size) {
			if (offset < 0) {
				throw new IllegalArgumentException("Limit field offset must not be less than 0!");
			}
			if (size < 1) {
				throw new IllegalArgumentException("Limit field size must not be less than 1!");
			}
			this.offset = offset;
			this.size = size;
		}
	}

	private boolean checkMatchColumn(String key) {
		boolean match = propertyMap.containsKey(key);
		if (this.forceMode && !match) {
			throw new UnsupportedElementException(String.format("Entity class [%s] does not contain property [%s]",
					tableMetadata.getEntityClass().getCanonicalName(), key));
		}
		return match;
	}

	private String matchColumn(String key) {
		return propertyMap.get(key).getColumn();
	}

	private Collection<?> convertStringToCollection(Object value) {
		if (value instanceof String) {
			String[] values = ((String) value).replace("(", "").replace(")", "").split(COMMA);
			return Lists.newArrayList(values);
		}
		return (Collection<?>) value;
	}

	private Set<String> convertFieldFilterToSelectColumns(FieldFilter fieldFilter, Class<?> entityClass) {
		if (null != fieldFilter) {
			return FieldFilterAssistant.getFilterColumns(entityClass, fieldFilter, false)
					.stream()
					.map(ColumnMetadata::getColumn)
					.collect(Collectors.toSet());
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Condition condition = (Condition) o;

		if (!sort.equals(condition.sort)) return false;
		if (!tableMetadata.equals(condition.tableMetadata)) return false;
		if (!selectColumns.equals(condition.selectColumns)) return false;
		return criteriaCollection.equals(condition.criteriaCollection);
	}

	@Override
	public int hashCode() {
		int result = sort.hashCode();
		result = 31 * result + tableMetadata.hashCode();
		result = 31 * result + selectColumns.hashCode();
		result = 31 * result + criteriaCollection.hashCode();
		return result;
	}
}
