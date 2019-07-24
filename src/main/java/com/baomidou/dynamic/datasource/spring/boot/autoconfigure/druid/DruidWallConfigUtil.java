/**
 * Copyright © 2018 organization baomidou
 * <pre>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <pre/>
 */
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid;

import com.alibaba.druid.wall.WallConfig;
import org.springframework.util.StringUtils;

/**
 * 防火墙配置工具类 解决打war包外部部署的异常
 *
 * @author Taoyu
 * @since 2.5.4
 */
public final class DruidWallConfigUtil {

  /**
   * 根据当前的配置和全局的配置生成druid防火墙配置
   *
   * @param c 当前配置
   * @param g 全局配置
   * @return 防火墙配置
   */
  public static WallConfig toWallConfig(DruidWallConfig c, DruidWallConfig g) {
    WallConfig wallConfig = new WallConfig();

    String tempDir = StringUtils.isEmpty(c.getDir()) ? g.getDir() : c.getDir();
    if (!StringUtils.isEmpty(tempDir)) {
      wallConfig.loadConfig(tempDir);
    }
    String tempTenantTablePattern =
        StringUtils.isEmpty(c.getTenantTablePattern()) ? g.getTenantTablePattern()
            : c.getTenantTablePattern();
    if (!StringUtils.isEmpty(tempTenantTablePattern)) {
      wallConfig.setTenantTablePattern(tempTenantTablePattern);
    }
    String tempTenantColumn =
        StringUtils.isEmpty(c.getTenantColumn()) ? g.getTenantColumn() : c.getTenantColumn();
    if (!StringUtils.isEmpty(tempTenantColumn)) {
      wallConfig.setTenantTablePattern(tempTenantColumn);
    }
    Boolean tempNoneBaseStatementAllow =
        c.getNoneBaseStatementAllow() == null ? g.getNoneBaseStatementAllow()
            : c.getNoneBaseStatementAllow();
    if (tempNoneBaseStatementAllow != null && tempNoneBaseStatementAllow) {
      wallConfig.setNoneBaseStatementAllow(true);
    }
    Integer tempInsertValuesCheckSize =
        c.getInsertValuesCheckSize() == null ? g.getInsertValuesCheckSize()
            : c.getInsertValuesCheckSize();
    if (tempInsertValuesCheckSize != null) {
      wallConfig.setInsertValuesCheckSize(tempInsertValuesCheckSize);
    }
    Integer tempSelectLimit = c.getSelectLimit() == null ? g.getSelectLimit() : c.getSelectLimit();
    if (tempSelectLimit != null) {
      c.setSelectLimit(tempSelectLimit);
    }

    Boolean tempCallAllow = c.getCallAllow() == null ? g.getCallAllow() : c.getCallAllow();
    if (tempCallAllow != null && !tempCallAllow) {
      wallConfig.setCallAllow(false);
    }
    Boolean tempSelectAllow = c.getSelectAllow() == null ? g.getSelectAllow() : c.getSelectAllow();
    if (tempSelectAllow != null && !tempSelectAllow) {
      wallConfig.setSelelctAllow(false);
    }
    Boolean tempSelectIntoAllow =
        c.getSelectIntoAllow() == null ? g.getSelectIntoAllow() : c.getSelectIntoAllow();
    if (tempSelectIntoAllow != null && !tempSelectIntoAllow) {
      wallConfig.setSelectIntoAllow(false);
    }
    Boolean tempSelectIntoOutfileAllow =
        c.getSelectIntoOutfileAllow() == null ? g.getSelectIntoOutfileAllow()
            : c.getSelectIntoOutfileAllow();
    if (tempSelectIntoOutfileAllow != null && tempSelectIntoOutfileAllow) {
      wallConfig.setSelectIntoOutfileAllow(true);
    }
    Boolean tempSelectWhereAlwayTrueCheck =
        c.getSelectWhereAlwayTrueCheck() == null ? g.getSelectWhereAlwayTrueCheck()
            : c.getSelectWhereAlwayTrueCheck();
    if (tempSelectWhereAlwayTrueCheck != null && !tempSelectWhereAlwayTrueCheck) {
      wallConfig.setSelectWhereAlwayTrueCheck(false);
    }
    Boolean tempSelectHavingAlwayTrueCheck =
        c.getSelectHavingAlwayTrueCheck() == null ? g.getSelectHavingAlwayTrueCheck()
            : c.getSelectHavingAlwayTrueCheck();
    if (tempSelectHavingAlwayTrueCheck != null && !tempSelectHavingAlwayTrueCheck) {
      wallConfig.setSelectHavingAlwayTrueCheck(false);
    }
    Boolean tempSelectUnionCheck =
        c.getSelectUnionCheck() == null ? g.getSelectUnionCheck() : c.getSelectUnionCheck();
    if (tempSelectUnionCheck != null && !tempSelectUnionCheck) {
      wallConfig.setSelectUnionCheck(false);
    }
    Boolean tempSelectMinusCheck =
        c.getSelectMinusCheck() == null ? g.getSelectMinusCheck() : c.getSelectMinusCheck();
    if (tempSelectMinusCheck != null && !tempSelectMinusCheck) {
      wallConfig.setSelectMinusCheck(false);
    }
    Boolean tempSelectExceptCheck =
        c.getSelectExceptCheck() == null ? g.getSelectExceptCheck() : c.getSelectExceptCheck();
    if (tempSelectExceptCheck != null && !tempSelectExceptCheck) {
      wallConfig.setSelectExceptCheck(false);
    }
    Boolean tempSelectIntersectCheck =
        c.getSelectIntersectCheck() == null ? g.getSelectIntersectCheck()
            : c.getSelectIntersectCheck();
    if (tempSelectIntersectCheck != null && !tempSelectIntersectCheck) {
      wallConfig.setSelectIntersectCheck(false);
    }
    Boolean tempCreateTableAllow =
        c.getCreateTableAllow() == null ? g.getCreateTableAllow() : c.getCreateTableAllow();
    if (tempCreateTableAllow != null && !tempCreateTableAllow) {
      wallConfig.setCreateTableAllow(false);
    }
    Boolean tempDropTableAllow =
        c.getDropTableAllow() == null ? g.getDropTableAllow() : c.getDropTableAllow();
    if (tempDropTableAllow != null && !tempDropTableAllow) {
      wallConfig.setDropTableAllow(false);
    }
    Boolean tempAlterTableAllow =
        c.getAlterTableAllow() == null ? g.getAlterTableAllow() : c.getAlterTableAllow();
    if (tempAlterTableAllow != null && !tempAlterTableAllow) {
      wallConfig.setAlterTableAllow(false);
    }
    Boolean tempRenameTableAllow =
        c.getRenameTableAllow() == null ? g.getRenameTableAllow() : c.getRenameTableAllow();
    if (tempRenameTableAllow != null && !tempRenameTableAllow) {
      wallConfig.setRenameTableAllow(false);
    }
    Boolean tempHintAllow = c.getHintAllow() == null ? g.getHintAllow() : c.getHintAllow();
    if (tempHintAllow != null && !tempHintAllow) {
      wallConfig.setHintAllow(false);
    }
    Boolean tempLockTableAllow =
        c.getLockTableAllow() == null ? g.getLockTableAllow() : c.getLockTableAllow();
    if (tempLockTableAllow != null && !tempLockTableAllow) {
      wallConfig.setLockTableAllow(false);
    }
    Boolean tempStartTransactionAllow =
        c.getStartTransactionAllow() == null ? g.getStartTransactionAllow()
            : c.getStartTransactionAllow();
    if (tempStartTransactionAllow != null && !tempStartTransactionAllow) {
      wallConfig.setStartTransactionAllow(false);
    }
    Boolean tempBlockAllow = c.getBlockAllow() == null ? g.getBlockAllow() : c.getBlockAllow();
    if (tempBlockAllow != null && !tempBlockAllow) {
      wallConfig.setBlockAllow(false);
    }
    Boolean tempConditionAndAlwayTrueAllow =
        c.getConditionAndAlwayTrueAllow() == null ? g.getConditionAndAlwayTrueAllow()
            : c.getConditionAndAlwayTrueAllow();
    if (tempConditionAndAlwayTrueAllow != null && tempConditionAndAlwayTrueAllow) {
      wallConfig.setConditionAndAlwayTrueAllow(true);
    }
    Boolean tempConditionAndAlwayFalseAllow =
        c.getConditionAndAlwayFalseAllow() == null ? g.getConditionAndAlwayFalseAllow()
            : c.getConditionAndAlwayFalseAllow();
    if (tempConditionAndAlwayFalseAllow != null && tempConditionAndAlwayFalseAllow) {
      wallConfig.setConditionAndAlwayFalseAllow(true);
    }
    Boolean tempConditionDoubleConstAllow =
        c.getConditionDoubleConstAllow() == null ? g.getConditionDoubleConstAllow()
            : c.getConditionDoubleConstAllow();
    if (tempConditionDoubleConstAllow != null && tempConditionDoubleConstAllow) {
      wallConfig.setConditionDoubleConstAllow(true);
    }
    Boolean tempConditionLikeTrueAllow =
        c.getConditionLikeTrueAllow() == null ? g.getConditionLikeTrueAllow()
            : c.getConditionLikeTrueAllow();
    if (tempConditionLikeTrueAllow != null && !tempConditionLikeTrueAllow) {
      wallConfig.setConditionLikeTrueAllow(false);
    }
    Boolean tempSelectAllColumnAllow =
        c.getSelectAllColumnAllow() == null ? g.getSelectAllColumnAllow()
            : c.getSelectAllColumnAllow();
    if (tempSelectAllColumnAllow != null && !tempSelectAllColumnAllow) {
      wallConfig.setSelectAllColumnAllow(false);
    }
    Boolean tempDeleteAllow = c.getDeleteAllow() == null ? g.getDeleteAllow() : c.getDeleteAllow();
    if (tempDeleteAllow != null && !tempDeleteAllow) {
      wallConfig.setDeleteAllow(false);
    }
    Boolean tempDeleteWhereAlwayTrueCheck =
        c.getDeleteWhereAlwayTrueCheck() == null ? g.getDeleteWhereAlwayTrueCheck()
            : c.getDeleteWhereAlwayTrueCheck();
    if (tempDeleteWhereAlwayTrueCheck != null && !tempDeleteWhereAlwayTrueCheck) {
      wallConfig.setDeleteWhereAlwayTrueCheck(false);
    }
    Boolean tempDeleteWhereNoneCheck =
        c.getDeleteWhereNoneCheck() == null ? g.getDeleteWhereNoneCheck()
            : c.getDeleteWhereNoneCheck();
    if (tempDeleteWhereNoneCheck != null && tempDeleteWhereNoneCheck) {
      wallConfig.setDeleteWhereNoneCheck(true);
    }
    Boolean tempUpdateAllow = c.getUpdateAllow() == null ? g.getUpdateAllow() : c.getUpdateAllow();
    if (tempUpdateAllow != null && !tempUpdateAllow) {
      wallConfig.setUpdateAllow(false);
    }
    Boolean tempUpdateWhereAlayTrueCheck =
        c.getUpdateWhereAlayTrueCheck() == null ? g.getUpdateWhereAlayTrueCheck()
            : c.getUpdateWhereAlayTrueCheck();
    if (tempUpdateWhereAlayTrueCheck != null && !tempUpdateWhereAlayTrueCheck) {
      wallConfig.setUpdateWhereAlayTrueCheck(false);
    }
    Boolean tempUpdateWhereNoneCheck =
        c.getUpdateWhereNoneCheck() == null ? g.getUpdateWhereNoneCheck()
            : c.getUpdateWhereNoneCheck();
    if (tempUpdateWhereNoneCheck != null && tempUpdateWhereNoneCheck) {
      wallConfig.setUpdateWhereNoneCheck(true);
    }
    Boolean tempInsertAllow = c.getInsertAllow() == null ? g.getInsertAllow() : c.getInsertAllow();
    if (tempInsertAllow != null && !tempInsertAllow) {
      wallConfig.setInsertAllow(false);
    }
    Boolean tempMergeAllow = c.getMergeAllow() == null ? g.getMergeAllow() : c.getMergeAllow();
    if (tempMergeAllow != null && !tempMergeAllow) {
      wallConfig.setMergeAllow(false);
    }
    Boolean tempMinusAllow = c.getMinusAllow() == null ? g.getMinusAllow() : c.getMinusAllow();
    if (tempMinusAllow != null && !tempMinusAllow) {
      wallConfig.setMinusAllow(false);
    }
    Boolean tempIntersectAllow =
        c.getIntersectAllow() == null ? g.getIntersectAllow() : c.getIntersectAllow();
    if (tempIntersectAllow != null && !tempIntersectAllow) {
      wallConfig.setIntersectAllow(false);
    }
    Boolean tempReplaceAllow =
        c.getReplaceAllow() == null ? g.getReplaceAllow() : c.getReplaceAllow();
    if (tempReplaceAllow != null && !tempReplaceAllow) {
      wallConfig.setReplaceAllow(false);
    }
    Boolean tempSetAllow = c.getSetAllow() == null ? g.getSetAllow() : c.getSetAllow();
    if (tempSetAllow != null && !tempSetAllow) {
      wallConfig.setSetAllow(false);
    }
    Boolean tempCommitAllow = c.getCommitAllow() == null ? g.getCommitAllow() : c.getCommitAllow();
    if (tempCommitAllow != null && !tempCommitAllow) {
      wallConfig.setCommitAllow(false);
    }
    Boolean tempRollbackAllow =
        c.getRollbackAllow() == null ? g.getRollbackAllow() : c.getRollbackAllow();
    if (tempRollbackAllow != null && !tempRollbackAllow) {
      wallConfig.setRollbackAllow(false);
    }
    Boolean tempUseAllow = c.getUseAllow() == null ? g.getUseAllow() : c.getUseAllow();
    if (tempUseAllow != null && !tempUseAllow) {
      wallConfig.setUseAllow(false);
    }
    Boolean tempMultiStatementAllow =
        c.getMultiStatementAllow() == null ? g.getMultiStatementAllow()
            : c.getMultiStatementAllow();
    if (tempMultiStatementAllow != null && tempMultiStatementAllow) {
      wallConfig.setMultiStatementAllow(true);
    }
    Boolean tempTruncateAllow =
        c.getTruncateAllow() == null ? g.getTruncateAllow() : c.getTruncateAllow();
    if (tempTruncateAllow != null && !tempTruncateAllow) {
      wallConfig.setTruncateAllow(false);
    }
    Boolean tempCommentAllow =
        c.getCommentAllow() == null ? g.getCommentAllow() : c.getCommentAllow();
    if (tempCommentAllow != null && tempCommentAllow) {
      wallConfig.setCommentAllow(true);
    }
    Boolean tempStrictSyntaxCheck =
        c.getStrictSyntaxCheck() == null ? g.getStrictSyntaxCheck() : c.getStrictSyntaxCheck();
    if (tempStrictSyntaxCheck != null && !tempStrictSyntaxCheck) {
      wallConfig.setStrictSyntaxCheck(false);
    }
    Boolean tempConstArithmeticAllow =
        c.getConstArithmeticAllow() == null ? g.getConstArithmeticAllow()
            : c.getConstArithmeticAllow();
    if (tempConstArithmeticAllow != null && !tempConstArithmeticAllow) {
      wallConfig.setConstArithmeticAllow(false);
    }
    Boolean tempLimitZeroAllow =
        c.getLimitZeroAllow() == null ? g.getLimitZeroAllow() : c.getLimitZeroAllow();
    if (tempLimitZeroAllow != null && tempLimitZeroAllow) {
      wallConfig.setLimitZeroAllow(true);
    }
    Boolean tempDescribeAllow =
        c.getDescribeAllow() == null ? g.getDescribeAllow() : c.getDescribeAllow();
    if (tempDescribeAllow != null && !tempDescribeAllow) {
      wallConfig.setDescribeAllow(false);
    }
    Boolean tempShowAllow = c.getShowAllow() == null ? g.getShowAllow() : c.getShowAllow();
    if (tempShowAllow != null && !tempShowAllow) {
      wallConfig.setShowAllow(false);
    }
    Boolean tempSchemaCheck = c.getSchemaCheck() == null ? g.getSchemaCheck() : c.getSchemaCheck();
    if (tempSchemaCheck != null && !tempSchemaCheck) {
      wallConfig.setSchemaCheck(false);
    }
    Boolean tempTableCheck = c.getTableCheck() == null ? g.getTableCheck() : c.getTableCheck();
    if (tempTableCheck != null && !tempTableCheck) {
      wallConfig.setTableCheck(false);
    }
    Boolean tempFunctionCheck =
        c.getFunctionCheck() == null ? g.getFunctionCheck() : c.getFunctionCheck();
    if (tempFunctionCheck != null && !tempFunctionCheck) {
      wallConfig.setFunctionCheck(false);
    }
    Boolean tempObjectCheck = c.getObjectCheck() == null ? g.getObjectCheck() : c.getObjectCheck();
    if (tempObjectCheck != null && !tempObjectCheck) {
      wallConfig.setObjectCheck(false);
    }
    Boolean tempVariantCheck =
        c.getVariantCheck() == null ? g.getVariantCheck() : c.getVariantCheck();
    if (tempVariantCheck != null && !tempVariantCheck) {
      wallConfig.setVariantCheck(false);
    }
    Boolean tempMustParameterized =
        c.getMustParameterized() == null ? g.getMustParameterized() : c.getMustParameterized();
    if (tempMustParameterized != null && tempMustParameterized) {
      wallConfig.setMustParameterized(true);
    }
    Boolean tempDoPrivilegedAllow =
        c.getDoPrivilegedAllow() == null ? g.getDoPrivilegedAllow() : c.getDoPrivilegedAllow();
    if (tempDoPrivilegedAllow != null && tempDoPrivilegedAllow) {
      wallConfig.setDoPrivilegedAllow(true);
    }
    Boolean tempWrapAllow = c.getWrapAllow() == null ? g.getWrapAllow() : c.getWrapAllow();
    if (tempWrapAllow != null && !tempWrapAllow) {
      wallConfig.setWrapAllow(false);
    }
    Boolean tempMetadataAllow =
        c.getMetadataAllow() == null ? g.getMetadataAllow() : c.getMetadataAllow();
    if (tempMetadataAllow != null && !tempMetadataAllow) {
      wallConfig.setMetadataAllow(false);
    }
    Boolean tempConditionOpXorAllow =
        c.getConditionOpXorAllow() == null ? g.getConditionOpXorAllow()
            : c.getConditionOpXorAllow();
    if (tempConditionOpXorAllow != null && tempConditionOpXorAllow) {
      wallConfig.setConditionOpXorAllow(true);
    }
    Boolean tempConditionOpBitwseAllow =
        c.getConditionOpBitwseAllow() == null ? g.getConditionOpBitwseAllow()
            : c.getConditionOpBitwseAllow();
    if (tempConditionOpBitwseAllow != null && !tempConditionOpBitwseAllow) {
      wallConfig.setConditionOpBitwseAllow(false);
    }
    Boolean tempCaseConditionConstAllow =
        c.getCaseConditionConstAllow() == null ? g.getCaseConditionConstAllow()
            : c.getCaseConditionConstAllow();
    if (tempCaseConditionConstAllow != null && tempCaseConditionConstAllow) {
      wallConfig.setCaseConditionConstAllow(true);
    }
    Boolean tempCompleteInsertValuesCheck =
        c.getCompleteInsertValuesCheck() == null ? g.getCompleteInsertValuesCheck()
            : c.getCompleteInsertValuesCheck();
    if (tempCompleteInsertValuesCheck != null && tempCompleteInsertValuesCheck) {
      wallConfig.setCompleteInsertValuesCheck(true);
    }
    return wallConfig;
  }
}
