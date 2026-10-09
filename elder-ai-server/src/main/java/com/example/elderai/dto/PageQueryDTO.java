package com.example.elderai.dto;

import lombok.Data;

/**
 * 分页查询请求DTO
 * 通用的分页查询参数封装，支持关键字模糊搜索
 */
@Data
public class PageQueryDTO {

    /** 当前页码，默认第1页 */
    private Integer pageNum = 1;

    /** 每页记录数，默认10条 */
    private Integer pageSize = 10;

    /** 搜索关键字 */
    private String keyword;

    /** 资讯分类：HEALTH、POLICY、ACTIVITY */
    private String category;

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum == null ? 1 : Math.max(1, pageNum);
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize == null ? 10 : Math.max(1, Math.min(100, pageSize));
    }
}
