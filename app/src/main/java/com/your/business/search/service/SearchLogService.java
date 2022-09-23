package com.your.business.search.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.your.business.search.entity.SearchLog;
import com.your.business.search.mapper.SearchLogMapper;
import com.your.common.constant.DatabaseConstant;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 搜索日志表 服务类
 * </p>
 *
 * @author 666
 * @since 2022-04-02
 */
@Service
@DS(DatabaseConstant.TESTDB)
public class SearchLogService extends ServiceImpl<SearchLogMapper, SearchLog> {

    /**
     * 分页查询全量数据
     *
     * @param page
     * @return
     */
    public Page<SearchLog> allPage(Page page) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.orderByDesc("id");
        return super.page(page, wrapper);
    }

}
