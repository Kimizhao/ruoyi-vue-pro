package cn.iocoder.yudao.module.trade.dal.mysql.brokerage;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByPriceRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.UserBrokerageSummaryBO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 佣金记录 Mapper
 *
 * @author owen
 */
@Mapper
public interface BrokerageRecordMapper extends BaseMapperX<BrokerageRecordDO> {

    default PageResult<BrokerageRecordDO> selectPage(BrokerageRecordPageReqVO reqVO) {
        // 分页查询
        return selectPage(reqVO, new LambdaQueryWrapperX<BrokerageRecordDO>()
                .eqIfPresent(BrokerageRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(BrokerageRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(BrokerageRecordDO::getStatus, reqVO.getStatus())
                .eqIfPresent(BrokerageRecordDO::getSourceUserLevel, reqVO.getSourceUserLevel())
                .betweenIfPresent(BrokerageRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BrokerageRecordDO::getId));
    }

    default List<BrokerageRecordDO> selectListByStatusAndUnfreezeTimeLt(Integer status, LocalDateTime unfreezeTime) {
        return selectList(new LambdaQueryWrapper<BrokerageRecordDO>()
                .eq(BrokerageRecordDO::getStatus, status)
                .lt(BrokerageRecordDO::getUnfreezeTime, unfreezeTime));
    }

    default int updateByIdAndStatus(Integer id, Integer status, BrokerageRecordDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<BrokerageRecordDO>()
                .eq(BrokerageRecordDO::getId, id)
                .eq(BrokerageRecordDO::getStatus, status));
    }

    default BrokerageRecordDO selectByBizTypeAndBizIdAndUserId(Integer bizType, String bizId, Long userId) {
        return selectOne(BrokerageRecordDO::getBizType, bizType,
                BrokerageRecordDO::getBizId, bizId,
                BrokerageRecordDO::getUserId, userId);
    }

    @Select("SELECT COUNT(1), SUM(price) FROM trade_brokerage_record " +
            "WHERE user_id = #{userId} AND biz_type = #{bizType} AND status = #{status}")
    UserBrokerageSummaryBO selectCountAndSumPriceByUserIdAndBizTypeAndStatus(@Param("userId") Long userId,
                                                                             @Param("bizType") Integer bizType,
                                                                             @Param("status") Integer status);

    @Select("SELECT SUM(price) FROM trade_brokerage_record WHERE user_id = #{userId} AND biz_type = #{bizType} " +
            "AND create_time BETWEEN #{beginTime} AND #{endTime}")
    Integer selectSummaryPriceByUserIdAndBizTypeAndCreateTimeBetween(@Param("userId") Long userId,
                                                                     @Param("bizType") Integer bizType,
                                                                     @Param("beginTime") LocalDateTime beginTime,
                                                                     @Param("endTime") LocalDateTime endTime);

    @Select("SELECT user_id AS id, SUM(price) AS brokeragePrice FROM trade_brokerage_record " +
            "WHERE biz_type = #{bizType} AND status = #{status} AND deleted = FALSE " +
            "AND unfreeze_time BETWEEN #{beginTime} AND #{endTime} " +
            "GROUP BY user_id " +
            "ORDER BY brokeragePrice DESC")
    IPage<AppBrokerageUserRankByPriceRespVO> selectSummaryPricePageGroupByUserId(IPage<?> page,
                                                                                 @Param("bizType") Integer bizType,
                                                                                 @Param("status") Integer status,
                                                                                 @Param("beginTime") LocalDateTime beginTime,
                                                                                 @Param("endTime") LocalDateTime endTime);
}