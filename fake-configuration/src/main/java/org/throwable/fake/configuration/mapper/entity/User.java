package org.throwable.fake.configuration.mapper.entity;

import lombok.Data;
import lombok.ToString;
import org.throwable.fake.mapper.common.annotation.Column;
import org.throwable.fake.mapper.common.annotation.Id;
import org.throwable.fake.mapper.common.annotation.Table;

import java.time.LocalDateTime;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/22 15:46
 */
@Data
@ToString
@Table("t_user")
public class User {

    @Id("id")
    private Long id;
    @Column("name")
    private String name;
    @Column("birth")
    private LocalDateTime birth;
}
