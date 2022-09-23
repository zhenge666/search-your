package com.your.business.search.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.your.base.util.KmsUtil;
import com.your.base.util.SimpleSignUtil;
import com.your.business.search.es.EsQueryVisitor;
import com.your.business.search.es.dto.EsSearchLogDTO;
import com.your.business.search.vo.SearchVO;
import com.your.product.center.facade.enums.ProductTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 通用业务处理基类
 *
 * @author zhangzhen
 * @Date 2022/4/6 下午6:04
 */
@Slf4j
@Service
public class BaseManager {

    @Autowired
    protected EsQueryVisitor queryVisitor;

    /**
     * 获取搜索响应对象
     *
     * @param version
     * @return
     */
    protected SearchVO getSearchVO(String version) {
        SearchVO vo = new SearchVO();
        if (StringUtils.isNotBlank(version)) {
            EsSearchLogDTO dto = queryVisitor.getByDocId(version, EsSearchLogDTO.class);
            if (dto != null) {
                BeanUtils.copyProperties(dto.getLog(), vo);
            }
        }
        return vo;
    }


    /**
     * 批量获取搜索数据
     *
     * @param versionSet 版本号集合
     * @return key:版本号，value：搜索对象
     */
    protected Map<String, SearchVO> getSearchLogMap(Set<String> versionSet) {
        Map<String, SearchVO> dtoMap = new HashMap<>(50);
        if (CollectionUtils.isNotEmpty(versionSet)) {
            List<EsSearchLogDTO> dtoList = queryVisitor.getByDocIds(versionSet, EsSearchLogDTO.class);
            if (CollectionUtils.isNotEmpty(dtoList)) {
                for (EsSearchLogDTO dto : dtoList) {
                    if (dto == null) {
                        continue;
                    }
                    SearchVO vo = new SearchVO();
                    BeanUtils.copyProperties(dto.getLog(), vo);
                    dtoMap.put(dto.getVersion(), vo);
                }
            }
        }
        return dtoMap;
    }

}
