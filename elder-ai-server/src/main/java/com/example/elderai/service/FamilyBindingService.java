package com.example.elderai.service;

import com.example.elderai.common.BusinessException;
import java.util.List;
import java.util.Map;

/**
 * 家属-老人绑定服务。
 */
public interface FamilyBindingService {

    /**
     * 家属绑定某位老人档案（按档案ID，立即生效，无需老人确认）。
     */
    void bind(Long familyUserId, Long elderInfoId, String relation, String applicationNote);

    /** 查询我（家属）绑定的所有老人档案。 */
    List<Map<String, Object>> myElders(Long familyUserId);

    /** 查询某老人档案绑定的所有家属。 */
    List<Map<String, Object>> myFamilies(Long elderInfoId);

    /** 查询当前家属尚未绑定的老人档案列表（可绑定）。 */
    List<Map<String, Object>> availableElders(Long familyUserId);

    /** 家属查看自己发起的申请。 */
    List<Map<String, Object>> myRequests(Long familyUserId);

    /** 解除绑定：家属解绑老人档案，或管理员移除绑定。 */
    void unbind(Long currentUserId, String role, Long elderInfoId, Long familyUserId);

    /** 校验家属是否已绑定该老人档案，未绑定抛 403。 */
    void assertBound(Long familyUserId, Long elderInfoId);

    /** 家属查看已绑定老人的完整档案（基本信息+最新健康记录+绑定设备），只读。 */
    Map<String, Object> detail(Long familyUserId, Long elderInfoId);
}
