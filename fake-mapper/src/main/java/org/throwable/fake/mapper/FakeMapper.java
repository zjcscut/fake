package org.throwable.fake.mapper;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/15 23:30
 */
public interface FakeMapper<P,T> extends Mapper, InsertMapper<P,T>, SelectMapper<P,T>, UpdateMapper<P,T>, DeleteMapper<P,T>,
		BatchMapper<P,T> {

}
