package org.throwable.fake.event.common.model;

import lombok.Data;

import java.util.Date;

/**
 * @author throwable
 * @version v1.0
 * @description the source of event type
 * @since 2017/12/4 23:45
 */
@Data
public abstract class FakeEvent {

	protected Date sendTimestamp;
	protected Date callbackTimestamp;
	protected String dateMark;
	protected String traceId;
}
