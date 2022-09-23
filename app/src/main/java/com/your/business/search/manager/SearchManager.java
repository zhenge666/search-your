package com.your.business.search.manager;


import com.your.business.search.bo.SearchPageBO;
import com.your.business.search.es.dto.EsSearchLogDTO;
import com.your.business.search.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 666需求业务处理
 *
 * @author zhangzhen
 * @Date 2022/4/6 下午6:04
 */
@Slf4j
@Service
public class SearchManager extends BaseManager {

    /**
     * logo分页查询
     *
     * @param bo
     * @return
     */
    public PageVO<SearchPageVO> esPage(SearchPageBO bo) {
        PageVO pageVO = new PageVO();
        SearchHits<EsSearchLogDTO> hits = queryVisitor.page(bo, SearchPageVO.class, EsSearchLogDTO.class);
        List<EsSearchLogDTO> dtoList = (List) SearchHitSupport.unwrapSearchHits(hits);
        List<SearchPageVO> voList = new ArrayList(dtoList.size());
        pageVO.setTotal(hits.getTotalHits());
        pageVO.setRecords(voList);
        for (EsSearchLogDTO dto : dtoList) {
            SearchPageVO vo = new SearchPageVO();
            BeanUtils.copyProperties(dto.getLog(), vo);
            vo.setTimestamp(dto.getLog().getCreateTime());
            voList.add(vo);
        }
        return pageVO;
    }

}
