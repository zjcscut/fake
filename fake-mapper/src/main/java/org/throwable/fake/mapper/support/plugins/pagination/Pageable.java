package org.throwable.fake.mapper.support.plugins.pagination;

import org.throwable.fake.mapper.utils.PageUtils;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/19 22:07
 */
public interface Pageable {

	// 默認頁碼
	int DEFAULT_PAGE_NUMBER = 1;

	// 默认每页显示数量
	int DEFAULT_PAGE_SIZE = 20;

	boolean USED_DEFAULT_PAGE_NUMBER = true;

	boolean fixEdge();

	boolean isPageable();

	int getPageNumber();

	int getPageSize();

	default int getOffset() {
		return PageUtils.getOffset(PageUtils.getFirstPageNumber(), getPageNumber(), getPageSize());
	}

	default int getLastPage(long total){
		return PageUtils.getLastPageNumber(total,getPageSize());
	}
}
