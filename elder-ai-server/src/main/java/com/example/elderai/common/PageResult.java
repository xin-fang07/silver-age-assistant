package com.example.elderai.common;

import com.example.elderai.observability.TraceContext;
import java.util.List;

/**
 * 分页返回结果类（独立类，不继承 Result，避免泛型冲突）
 * <p>
 * T 表示列表元素类型。
 * </p>
 *
 * @param <T> 列表元素类型
 */
public class PageResult<T> {

    private int code = 200;
    private String message = "查询成功";
    private List<T> data;
    private long total;
    private long pageNum;
    private long pageSize;
    private String requestId = TraceContext.currentTraceId();

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getPageNum() { return pageNum; }
    public void setPageNum(long pageNum) { this.pageNum = pageNum; }

    public long getPageSize() { return pageSize; }
    public void setPageSize(long pageSize) { this.pageSize = pageSize; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    /**
     * 创建分页成功结果
     */
    public static <T> PageResult<T> pageSuccess(List<T> data, long total, long pageNum, long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.data = data;
        result.total = total;
        result.pageNum = pageNum;
        result.pageSize = pageSize;
        return result;
    }
}
