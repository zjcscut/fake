package org.throwable.fake.mapper.utils;

import org.throwable.fake.mapper.exception.CollectionDeepCopyException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/18 15:03
 */
public abstract class CollectionUtils {

	@SuppressWarnings("unchecked")
	public static <T> Collection<T> deepCopy(Collection<T> src) {
		try (ByteArrayOutputStream bao = new ByteArrayOutputStream();
			 ObjectOutputStream out = new ObjectOutputStream(bao)) {
			out.writeObject(src);
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bao.toByteArray());
				 ObjectInputStream in = new ObjectInputStream(byteIn)) {
				return (Collection<T>) in.readObject();
			}
		} catch (Exception e) {
			throw new CollectionDeepCopyException(e);
		}
	}

	public static <T> Set<T> deepCopySet(Set<T> src) {
		return (Set<T>) deepCopy(src);
	}

	public static <T> List<T> deepCopyList(List<T> src) {
		return (List<T>) deepCopy(src);
	}

	public static <T> boolean isNullOrEmpty(List<T> list) {
		return null == list || list.isEmpty();
	}
}
