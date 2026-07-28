package com.alibaba.ticketsystem.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserVo {

    /** 正式接口字段。 */
    private Long id;

    /** 兼容 7-28 前端，成员 B 完成类型升级后可移除。 */
    private Long userId;

    private String username;

    private String nickname;

    private String token;

    /** 正式接口字段。 */
    private String role;

    /** 兼容 7-28 前端，成员 B 完成类型升级后可移除。 */
    private List<String> roles;

    private List<String> permissions;
}
