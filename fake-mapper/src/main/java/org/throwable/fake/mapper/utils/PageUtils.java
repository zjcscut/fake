package org.throwable.fake.mapper.utils;


import static org.throwable.fake.mapper.support.plugins.pagination.Pageable.DEFAULT_PAGE_NUMBER;
import static org.throwable.fake.mapper.support.plugins.pagination.Pageable.USED_DEFAULT_PAGE_NUMBER;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/8 19:37
 */
public final class PageUtils {

	public static int getFirstPageNumber() {
		return getFirstPageNumber(USED_DEFAULT_PAGE_NUMBER);
	}

	public static int getFirstPageNumber(boolean useDefault) {
		return useDefault ? DEFAULT_PAGE_NUMBER : 0;
	}

	public static int getLastPageNumber(long total, int pageSize) {
		return (int) Math.ceil((double) total / (double) pageSize);
	}

	public static int getOffset(int firstPageNumber, int pageNumber, int pageSize) {
		return (pageNumber - firstPageNumber) * pageSize;
	}
}
