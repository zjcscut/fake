package org.throwable.fake.mapper.common.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.throwable.fake.mapper.common.constant.Constants;
import org.throwable.fake.mapper.support.plugins.generator.identity.PrimaryKeyGenerator;
import org.throwable.fake.mapper.support.plugins.generator.type.NoneTypeHandler;
import org.throwable.fake.mapper.utils.StringUtils;

import java.io.Serializable;

/**
 * @author throwable
 * @version v1.0
 * @description Column元数据
 * @since 2017/8/15 0:06
 */
@Getter
@Setter
public class ColumnMetadata implements Serializable {

	private static final long serialVersionUID = -1;

	private TableMetadata tableMetadata;
	private String property;
	private String column;
	private Class<?> javaType;
	private JdbcType jdbcType;
	private Class<? extends TypeHandler<?>> typeHandler;
	private Class<? extends PrimaryKeyGenerator> keyGenerator;

	//is key
	private boolean identity = false;
	//autoIncrement
	private boolean autoIncrement = false;
	//insertable
	private boolean insertable = true;
	//updatable
	private boolean updatable = true;

	private boolean nullable = false;

	public ColumnMetadata(TableMetadata tableMetadata) {
		this.tableMetadata = tableMetadata;
	}

	/**
	 * <if test = \"prefix.property neq null and prefix.property neq ''\" >
	 * column,
	 * </if>
	 */
	public String getIfNotEmptyColumnByPropertyEqualHolderWithComma(String prefix) {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(prefix).append(Constants.DOT).append(this.property).append(" neq null and ");
		builder.append(prefix).append(Constants.DOT).append(this.property).append(" neq ''\">");
		builder.append(this.column);
		builder.append("</if>");
		return builder.toString();
	}

	/**
	 * <if test = \"prefix.property neq null and prefix.property neq ''\" >
	 * column = #{prefix.property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	 * </if>
	 */
	public String getIfNotEmptyColumnPropertyEqualHolderWithComma(String prefix) {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(prefix).append(Constants.DOT).append(this.property).append(" neq null and ");
		builder.append(prefix).append(Constants.DOT).append(this.property).append(" neq ''\">");
		builder.append(getColumnPropertyEqualHolderWithComma(prefix));
		builder.append("</if>");
		return builder.toString();
	}

	/**
	 * <if test = \"prefix.property neq null\" >
	 * column = #{prefix.property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	 * </if>
	 */
	public String getIfNotNullColumnPropertyEqualHolderWithComma(String prefix) {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(prefix).append(Constants.DOT).append(this.property).append(" neq null\">");
		builder.append(getColumnPropertyEqualHolderWithComma(prefix));
		builder.append("</if>");
		return builder.toString();
	}

	//column = #{prefix.property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	public String getColumnPropertyEqualHolderWithComma(String prefix) {
		return this.column + " = " + getColumnPropertyHolder(prefix, Constants.COMMA);
	}


	//column = #{prefix.property,jdbcType=xxx,typeHandler=yyy,javaType=zzz}
	public String getColumnPropertyEqualHolder(String prefix) {
		return this.column + " = " + getColumnPropertyHolder(prefix,null);
	}

	/**
	 * <if test = \"property neq null and property neq ''\" >
	 * column,
	 * </if>
	 */
	public String getIfNotEmptyColumnByPropertyEqualHolderWithComma() {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(this.property).append(" neq null and ");
		builder.append(this.property).append(" neq ''\">");
		builder.append(this.column).append(Constants.COMMA);
		builder.append("</if>");
		return builder.toString();
	}

	/**
	 * <if test = \"property neq null and property neq ''\" >
	 * column = #{property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	 * </if>
	 */
	public String getIfNotEmptyColumnPropertyEqualHolderWithComma() {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(this.property).append(" neq null and ");
		builder.append(this.property).append(" neq ''\">");
		builder.append(getColumnPropertyEqualHolderWithComma());
		builder.append("</if>");
		return builder.toString();
	}

	/**
	 * <if test = \"prefix.property neq null\" >
	 * column,
	 * </if>
	 */
	public String getIfNotNullColumnByPropertyEqualHolderWithComma(String prefix) {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(prefix).append(Constants.DOT).append(this.property).append(" neq null\">");
		builder.append(this.column).append(Constants.COMMA);
		builder.append("</if>");
		return builder.toString();
	}

	/**
	 * <if test = \"property neq null\" >
	 * column,
	 * </if>
	 */
	public String getIfNotNullColumnByPropertyEqualHolderWithComma() {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(this.property).append(" neq null\">");
		builder.append(this.column).append(Constants.COMMA);
		builder.append("</if>");
		return builder.toString();
	}


	/**
	 * <if test = \"property neq null\" >
	 * column = #{property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	 * </if>
	 */
	public String getIfNotNullColumnPropertyEqualHolderWithComma() {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(this.property).append(" neq null\">");
		builder.append(getColumnPropertyEqualHolderWithComma());
		builder.append("</if>");
		return builder.toString();
	}

	//column = #{property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	public String getColumnPropertyEqualHolderWithComma() {
		return this.column + " = " + getColumnPropertyHolder(null, Constants.COMMA);
	}

	//column = #{property,jdbcType=xxx,typeHandler=yyy,javaType=zzz}
	public String getColumnPropertyEqualHolder() {
		return this.column + " = " + getColumnPropertyHolder(null, null);
	}

	/**
	 * <if test = \"prefix.property neq null\" >
	 * #{prefix.property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	 * </if>
	 */
	public String getIfNullColumnPropertyHolderWithComma(String prefix) {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(prefix).append(Constants.DOT).append(this.property).append(" neq null\">");
		builder.append(getColumnPropertyHolder(prefix, Constants.COMMA));
		builder.append("</if>");
		return builder.toString();
	}

	/**
	 * <if test = \"prefix.property neq null\" >
	 * #{prefix.property,jdbcType=xxx,typeHandler=yyy,javaType=zzz}
	 * </if>
	 */
	public String getIfNullColumnPropertyHolder(String prefix) {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(prefix).append(Constants.DOT).append(this.property).append(" neq null\">");
		builder.append(getColumnPropertyHolder(prefix, null));
		builder.append("</if>");
		return builder.toString();
	}

	/**
	 * <if test = \"prefix.property neq null and property neq ''\" >
	 * #{prefix.property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	 * </if>
	 */
	public String getIfEmptyColumnPropertyHolderWithComma(String prefix) {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(prefix).append(Constants.DOT).append(this.property).append(" neq null and ");
		builder.append(prefix).append(Constants.DOT).append(this.property).append(" neq ''\">");
		builder.append(prefix).append(getColumnPropertyHolder(prefix, Constants.COMMA));
		builder.append("</if>");
		return builder.toString();
	}

	//#{prefix.property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	public String getColumnPropertyHolderWithComma(String prefix) {
		return getColumnPropertyHolder(prefix, Constants.COMMA);
	}

	//#{prefix.property,jdbcType=xxx,typeHandler=yyy,javaType=zzz}
	public String getColumnPropertyHolder(String prefix) {
		return getColumnPropertyHolder(prefix, null);
	}

	/**
	 * <if test = \"property neq null\" >
	 * #{property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	 * </if>
	 */
	public String getIfNullColumnPropertyHolderWithComma() {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(this.property).append(" neq null\">");
		builder.append(getColumnPropertyHolderWithComma());
		builder.append("</if>");
		return builder.toString();
	}

	/**
	 * <if test = \"property neq null and property neq ''\" >
	 * #{property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	 * </if>
	 */
	public String getIfEmptyColumnPropertyHolderWithComma() {
		StringBuilder builder = new StringBuilder("<if test = \"");
		builder.append(this.property).append(" neq null and ");
		builder.append(this.property).append(" neq ''\">");
		builder.append(getColumnPropertyHolderWithComma());
		builder.append("</if>");
		return builder.toString();
	}

	//#{property,jdbcType=xxx,typeHandler=yyy,javaType=zzz},
	public String getColumnPropertyHolderWithComma() {
		return getColumnPropertyHolder(null, Constants.COMMA);
	}

	public String getColumnWithComma() {
		return this.column + Constants.COMMA;
	}

	//#{prefix.property,jdbcType=xxx,typeHandler=yyy,javaType=zzz}+separator
	public String getColumnPropertyHolder(String prefix, String separator) {
		StringBuilder builder = new StringBuilder("#{");
		if (StringUtils.isNotEmpty(prefix)) {
			builder.append(prefix).append(Constants.DOT);
		}
		builder.append(this.property);
		if (null != jdbcType && !JdbcType.UNDEFINED.equals(jdbcType)) {
			builder.append(",jdbcType=");
			builder.append(this.jdbcType.toString());
		}
		if (null != typeHandler && !NoneTypeHandler.class.equals(typeHandler)) {
			builder.append(",typeHandler=");
			builder.append(this.typeHandler.getCanonicalName());
		}
		if (!this.javaType.isArray()) {
			builder.append(",javaType=");
			builder.append(javaType.getCanonicalName());
		}
		builder.append("}");
		if (StringUtils.isNotEmpty(separator)) {
			builder.append(separator);
		}
		return builder.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ColumnMetadata that = (ColumnMetadata) o;
		return column != null ? column.equals(that.column) : that.column == null;
	}

	@Override
	public int hashCode() {
		return column != null ? column.hashCode() : 0;
	}
}
