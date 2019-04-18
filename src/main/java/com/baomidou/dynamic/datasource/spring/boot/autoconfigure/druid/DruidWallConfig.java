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
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class DruidWallConfig {


    private Boolean noneBaseStatementAllow;

    private Boolean callAllow;
    private Boolean selectAllow;
    private Boolean selectIntoAllow;
    private Boolean selectIntoOutfileAllow;
    private Boolean selectWhereAlwayTrueCheck;
    private Boolean selectHavingAlwayTrueCheck;
    private Boolean selectUnionCheck;
    private Boolean selectMinusCheck;
    private Boolean selectExceptCheck;
    private Boolean selectIntersectCheck;
    private Boolean createTableAllow;
    private Boolean dropTableAllow;
    private Boolean alterTableAllow;
    private Boolean renameTableAllow;
    private Boolean hintAllow;
    private Boolean lockTableAllow;
    private Boolean startTransactionAllow;
    private Boolean blockAllow;

    private Boolean conditionAndAlwayTrueAllow;
    private Boolean conditionAndAlwayFalseAllow;
    private Boolean conditionDoubleConstAllow;
    private Boolean conditionLikeTrueAllow;

    private Boolean selectAllColumnAllow;

    private Boolean deleteAllow;
    private Boolean deleteWhereAlwayTrueCheck;
    private Boolean deleteWhereNoneCheck;

    private Boolean updateAllow;
    private Boolean updateWhereAlayTrueCheck;
    private Boolean updateWhereNoneCheck;

    private Boolean insertAllow;
    private Boolean mergeAllow;
    private Boolean minusAllow;
    private Boolean intersectAllow;
    private Boolean replaceAllow;
    private Boolean setAllow;
    private Boolean commitAllow;
    private Boolean rollbackAllow;
    private Boolean useAllow;

    private Boolean multiStatementAllow;

    private Boolean truncateAllow;

    private Boolean commentAllow;
    private Boolean strictSyntaxCheck;
    private Boolean constArithmeticAllow;
    private Boolean limitZeroAllow;

    private Boolean describeAllow;
    private Boolean showAllow;

    private Boolean schemaCheck;
    private Boolean tableCheck;
    private Boolean functionCheck;
    private Boolean objectCheck;
    private Boolean variantCheck;

    private Boolean mustParameterized;

    private Boolean doPrivilegedAllow;

    private String dir;

    private String tenantTablePattern;
    private String tenantColumn;

    private Boolean wrapAllow;
    private Boolean metadataAllow;

    private Boolean conditionOpXorAllow;
    private Boolean conditionOpBitwseAllow;

    private Boolean caseConditionConstAllow;

    private Boolean completeInsertValuesCheck;
    private Integer insertValuesCheckSize;

    private Integer selectLimit;


    public WallConfig toWallConfig(DruidWallConfig config) {
        WallConfig wallConfig = new WallConfig();

        String tempDir = StringUtils.isEmpty(dir) ? config.getDir() : dir;
        if (!StringUtils.isEmpty(tempDir)) {
            wallConfig.loadConfig(tempDir);
        }
        String tempTenantTablePattern = StringUtils.isEmpty(tenantTablePattern) ? config.getTenantTablePattern() : tenantTablePattern;
        if (!StringUtils.isEmpty(tempTenantTablePattern)) {
            wallConfig.setTenantTablePattern(tempTenantTablePattern);
        }
        String tempTenantColumn = StringUtils.isEmpty(tenantColumn) ? config.getTenantColumn() : tenantColumn;
        if (!StringUtils.isEmpty(tempTenantColumn)) {
            wallConfig.setTenantTablePattern(tempTenantColumn);
        }
        Boolean tempNoneBaseStatementAllow = noneBaseStatementAllow == null ? config.getNoneBaseStatementAllow() : noneBaseStatementAllow;
        if (tempNoneBaseStatementAllow != null && tempNoneBaseStatementAllow) {
            wallConfig.setNoneBaseStatementAllow(true);
        }
        Integer tempInsertValuesCheckSize = insertValuesCheckSize == null ? config.getInsertValuesCheckSize() : insertValuesCheckSize;
        if (tempInsertValuesCheckSize != null) {
            wallConfig.setInsertValuesCheckSize(tempInsertValuesCheckSize);
        }
        Integer tempSelectLimit = selectLimit == null ? config.getSelectLimit() : selectLimit;
        if (tempSelectLimit != null) {
            config.setSelectLimit(tempSelectLimit);
        }

        Boolean tempCallAllow = callAllow == null ? config.getCallAllow() : callAllow;
        if (tempCallAllow != null && !tempCallAllow) {
            wallConfig.setCallAllow(false);
        }
        Boolean tempSelectAllow = selectAllow == null ? config.getSelectAllow() : selectAllow;
        if (tempSelectAllow != null && !tempSelectAllow) {
            wallConfig.setSelelctAllow(false);
        }
        Boolean tempSelectIntoAllow = selectIntoAllow == null ? config.getSelectIntoAllow() : selectIntoAllow;
        if (tempSelectIntoAllow != null && !tempSelectIntoAllow) {
            wallConfig.setSelectIntoAllow(false);
        }
        Boolean tempSelectIntoOutfileAllow = selectIntoOutfileAllow == null ? config.getSelectIntoOutfileAllow() : selectIntoOutfileAllow;
        if (tempSelectIntoOutfileAllow != null && tempSelectIntoOutfileAllow) {
            wallConfig.setSelectIntoOutfileAllow(true);
        }
        Boolean tempSelectWhereAlwayTrueCheck = selectWhereAlwayTrueCheck == null ? config.getSelectWhereAlwayTrueCheck() : selectWhereAlwayTrueCheck;
        if (tempSelectWhereAlwayTrueCheck != null && !tempSelectWhereAlwayTrueCheck) {
            wallConfig.setSelectWhereAlwayTrueCheck(false);
        }
        Boolean tempSelectHavingAlwayTrueCheck = selectHavingAlwayTrueCheck == null ? config.getSelectHavingAlwayTrueCheck() : selectHavingAlwayTrueCheck;
        if (tempSelectHavingAlwayTrueCheck != null && !tempSelectHavingAlwayTrueCheck) {
            wallConfig.setSelectHavingAlwayTrueCheck(false);
        }
        Boolean tempSelectUnionCheck = selectUnionCheck == null ? config.getSelectUnionCheck() : selectUnionCheck;
        if (tempSelectUnionCheck != null && !tempSelectUnionCheck) {
            wallConfig.setSelectUnionCheck(false);
        }
        Boolean tempSelectMinusCheck = selectMinusCheck == null ? config.getSelectMinusCheck() : selectMinusCheck;
        if (tempSelectMinusCheck != null && !tempSelectMinusCheck) {
            wallConfig.setSelectMinusCheck(false);
        }
        Boolean tempSelectExceptCheck = selectExceptCheck == null ? config.getSelectExceptCheck() : selectExceptCheck;
        if (tempSelectExceptCheck != null && !tempSelectExceptCheck) {
            wallConfig.setSelectExceptCheck(false);
        }
        Boolean tempSelectIntersectCheck = selectIntersectCheck == null ? config.getSelectIntersectCheck() : selectIntersectCheck;
        if (tempSelectIntersectCheck != null && !tempSelectIntersectCheck) {
            wallConfig.setSelectIntersectCheck(false);
        }
        Boolean tempCreateTableAllow = createTableAllow == null ? config.getCreateTableAllow() : createTableAllow;
        if (tempCreateTableAllow != null && !tempCreateTableAllow) {
            wallConfig.setCreateTableAllow(false);
        }
        Boolean tempDropTableAllow = dropTableAllow == null ? config.getDropTableAllow() : dropTableAllow;
        if (tempDropTableAllow != null && !tempDropTableAllow) {
            wallConfig.setDropTableAllow(false);
        }
        Boolean tempAlterTableAllow = alterTableAllow == null ? config.getAlterTableAllow() : alterTableAllow;
        if (tempAlterTableAllow != null && !tempAlterTableAllow) {
            wallConfig.setAlterTableAllow(false);
        }
        Boolean tempRenameTableAllow = renameTableAllow == null ? config.getRenameTableAllow() : renameTableAllow;
        if (tempRenameTableAllow != null && !tempRenameTableAllow) {
            wallConfig.setRenameTableAllow(false);
        }
        Boolean tempHintAllow = hintAllow == null ? config.getHintAllow() : hintAllow;
        if (tempHintAllow != null && !tempHintAllow) {
            wallConfig.setHintAllow(false);
        }
        Boolean tempLockTableAllow = lockTableAllow == null ? config.getLockTableAllow() : lockTableAllow;
        if (tempLockTableAllow != null && !tempLockTableAllow) {
            wallConfig.setLockTableAllow(false);
        }
        Boolean tempStartTransactionAllow = startTransactionAllow == null ? config.getStartTransactionAllow() : startTransactionAllow;
        if (tempStartTransactionAllow != null && !tempStartTransactionAllow) {
            wallConfig.setStartTransactionAllow(false);
        }
        Boolean tempBlockAllow = blockAllow == null ? config.getBlockAllow() : blockAllow;
        if (tempBlockAllow != null && !tempBlockAllow) {
            wallConfig.setBlockAllow(false);
        }
        Boolean tempConditionAndAlwayTrueAllow = conditionAndAlwayTrueAllow == null ? config.getConditionAndAlwayTrueAllow() : conditionAndAlwayTrueAllow;
        if (tempConditionAndAlwayTrueAllow != null && tempConditionAndAlwayTrueAllow) {
            wallConfig.setConditionAndAlwayTrueAllow(true);
        }
        Boolean tempConditionAndAlwayFalseAllow = conditionAndAlwayFalseAllow == null ? config.getConditionAndAlwayFalseAllow() : conditionAndAlwayFalseAllow;
        if (tempConditionAndAlwayFalseAllow != null && tempConditionAndAlwayFalseAllow) {
            wallConfig.setConditionAndAlwayFalseAllow(true);
        }
        Boolean tempConditionDoubleConstAllow = conditionDoubleConstAllow == null ? config.getConditionDoubleConstAllow() : conditionDoubleConstAllow;
        if (tempConditionDoubleConstAllow != null && tempConditionDoubleConstAllow) {
            wallConfig.setConditionDoubleConstAllow(true);
        }
        Boolean tempConditionLikeTrueAllow = conditionLikeTrueAllow == null ? config.getConditionLikeTrueAllow() : conditionLikeTrueAllow;
        if (tempConditionLikeTrueAllow != null && !tempConditionLikeTrueAllow) {
            wallConfig.setConditionLikeTrueAllow(false);
        }
        Boolean tempSelectAllColumnAllow = selectAllColumnAllow == null ? config.getSelectAllColumnAllow() : selectAllColumnAllow;
        if (tempSelectAllColumnAllow != null && !tempSelectAllColumnAllow) {
            wallConfig.setSelectAllColumnAllow(false);
        }
        Boolean tempDeleteAllow = deleteAllow == null ? config.getDeleteAllow() : deleteAllow;
        if (tempDeleteAllow != null && !tempDeleteAllow) {
            wallConfig.setDeleteAllow(false);
        }
        Boolean tempDeleteWhereAlwayTrueCheck = deleteWhereAlwayTrueCheck == null ? config.getDeleteWhereAlwayTrueCheck() : deleteWhereAlwayTrueCheck;
        if (tempDeleteWhereAlwayTrueCheck != null && !tempDeleteWhereAlwayTrueCheck) {
            wallConfig.setDeleteWhereAlwayTrueCheck(false);
        }
        Boolean tempDeleteWhereNoneCheck = deleteWhereNoneCheck == null ? config.getDeleteWhereNoneCheck() : deleteWhereNoneCheck;
        if (tempDeleteWhereNoneCheck != null && tempDeleteWhereNoneCheck) {
            wallConfig.setDeleteWhereNoneCheck(true);
        }
        Boolean tempUpdateAllow = updateAllow == null ? config.getUpdateAllow() : updateAllow;
        if (tempUpdateAllow != null && !tempUpdateAllow) {
            wallConfig.setUpdateAllow(false);
        }
        Boolean tempUpdateWhereAlayTrueCheck = updateWhereAlayTrueCheck == null ? config.getUpdateWhereAlayTrueCheck() : updateWhereAlayTrueCheck;
        if (tempUpdateWhereAlayTrueCheck != null && !tempUpdateWhereAlayTrueCheck) {
            wallConfig.setUpdateWhereAlayTrueCheck(false);
        }
        Boolean tempUpdateWhereNoneCheck = updateWhereNoneCheck == null ? config.getUpdateWhereNoneCheck() : updateWhereNoneCheck;
        if (tempUpdateWhereNoneCheck != null && tempUpdateWhereNoneCheck) {
            wallConfig.setUpdateWhereNoneCheck(true);
        }
        Boolean tempInsertAllow = insertAllow == null ? config.getInsertAllow() : insertAllow;
        if (tempInsertAllow != null && !tempInsertAllow) {
            wallConfig.setInsertAllow(false);
        }
        Boolean tempMergeAllow = mergeAllow == null ? config.getMergeAllow() : mergeAllow;
        if (tempMergeAllow != null && !tempMergeAllow) {
            wallConfig.setMergeAllow(false);
        }
        Boolean tempMinusAllow = minusAllow == null ? config.getMinusAllow() : minusAllow;
        if (tempMinusAllow != null && !tempMinusAllow) {
            wallConfig.setMinusAllow(false);
        }
        Boolean tempIntersectAllow = intersectAllow == null ? config.getIntersectAllow() : intersectAllow;
        if (tempIntersectAllow != null && !tempIntersectAllow) {
            wallConfig.setIntersectAllow(false);
        }
        Boolean tempReplaceAllow = replaceAllow == null ? config.getReplaceAllow() : replaceAllow;
        if (tempReplaceAllow != null && !tempReplaceAllow) {
            wallConfig.setReplaceAllow(false);
        }
        Boolean tempSetAllow = setAllow == null ? config.getSetAllow() : setAllow;
        if (tempSetAllow != null && !tempSetAllow) {
            wallConfig.setSetAllow(false);
        }
        Boolean tempCommitAllow = commitAllow == null ? config.getCommitAllow() : commitAllow;
        if (tempCommitAllow != null && !tempCommitAllow) {
            wallConfig.setCommitAllow(false);
        }
        Boolean tempRollbackAllow = rollbackAllow == null ? config.getRollbackAllow() : rollbackAllow;
        if (tempRollbackAllow != null && !tempRollbackAllow) {
            wallConfig.setRollbackAllow(false);
        }
        Boolean tempUseAllow = useAllow == null ? config.getUseAllow() : useAllow;
        if (tempUseAllow != null && !tempUseAllow) {
            wallConfig.setUseAllow(false);
        }
        Boolean tempMultiStatementAllow = multiStatementAllow == null ? config.getMultiStatementAllow() : multiStatementAllow;
        if (tempMultiStatementAllow != null && tempMultiStatementAllow) {
            wallConfig.setMultiStatementAllow(true);
        }
        Boolean tempTruncateAllow = truncateAllow == null ? config.getTruncateAllow() : truncateAllow;
        if (tempTruncateAllow != null && !tempTruncateAllow) {
            wallConfig.setTruncateAllow(false);
        }
        Boolean tempCommentAllow = commentAllow == null ? config.getCommentAllow() : commentAllow;
        if (tempCommentAllow != null && tempCommentAllow) {
            wallConfig.setCommentAllow(true);
        }
        Boolean tempStrictSyntaxCheck = strictSyntaxCheck == null ? config.getStrictSyntaxCheck() : strictSyntaxCheck;
        if (tempStrictSyntaxCheck != null && !tempStrictSyntaxCheck) {
            wallConfig.setStrictSyntaxCheck(false);
        }
        Boolean tempConstArithmeticAllow = constArithmeticAllow == null ? config.getConstArithmeticAllow() : constArithmeticAllow;
        if (tempConstArithmeticAllow != null && !tempConstArithmeticAllow) {
            wallConfig.setConstArithmeticAllow(false);
        }
        Boolean tempLimitZeroAllow = limitZeroAllow == null ? config.getLimitZeroAllow() : limitZeroAllow;
        if (tempLimitZeroAllow != null && tempLimitZeroAllow) {
            wallConfig.setLimitZeroAllow(true);
        }
        Boolean tempDescribeAllow = describeAllow == null ? config.getDescribeAllow() : describeAllow;
        if (tempDescribeAllow != null && !tempDescribeAllow) {
            wallConfig.setDescribeAllow(false);
        }
        Boolean tempShowAllow = showAllow == null ? config.getShowAllow() : showAllow;
        if (tempShowAllow != null && !tempShowAllow) {
            wallConfig.setShowAllow(false);
        }
        Boolean tempSchemaCheck = schemaCheck == null ? config.getSchemaCheck() : schemaCheck;
        if (tempSchemaCheck != null && !tempSchemaCheck) {
            wallConfig.setSchemaCheck(false);
        }
        Boolean tempTableCheck = tableCheck == null ? config.getTableCheck() : tableCheck;
        if (tempTableCheck != null && !tempTableCheck) {
            wallConfig.setTableCheck(false);
        }
        Boolean tempFunctionCheck = functionCheck == null ? config.getFunctionCheck() : functionCheck;
        if (tempFunctionCheck != null && !tempFunctionCheck) {
            wallConfig.setFunctionCheck(false);
        }
        Boolean tempObjectCheck = objectCheck == null ? config.getObjectCheck() : objectCheck;
        if (tempObjectCheck != null && !tempObjectCheck) {
            wallConfig.setObjectCheck(false);
        }
        Boolean tempVariantCheck = variantCheck == null ? config.getVariantCheck() : variantCheck;
        if (tempVariantCheck != null && !tempVariantCheck) {
            wallConfig.setVariantCheck(false);
        }
        Boolean tempMustParameterized = mustParameterized == null ? config.getMustParameterized() : mustParameterized;
        if (tempMustParameterized != null && tempMustParameterized) {
            wallConfig.setMustParameterized(true);
        }
        Boolean tempDoPrivilegedAllow = doPrivilegedAllow == null ? config.getDoPrivilegedAllow() : doPrivilegedAllow;
        if (tempDoPrivilegedAllow != null && tempDoPrivilegedAllow) {
            wallConfig.setDoPrivilegedAllow(true);
        }
        Boolean tempWrapAllow = wrapAllow == null ? config.getWrapAllow() : wrapAllow;
        if (tempWrapAllow != null && !tempWrapAllow) {
            wallConfig.setWrapAllow(false);
        }
        Boolean tempMetadataAllow = metadataAllow == null ? config.getMetadataAllow() : metadataAllow;
        if (tempMetadataAllow != null && !tempMetadataAllow) {
            wallConfig.setMetadataAllow(false);
        }
        Boolean tempConditionOpXorAllow = conditionOpXorAllow == null ? config.getConditionOpXorAllow() : conditionOpXorAllow;
        if (tempConditionOpXorAllow != null && tempConditionOpXorAllow) {
            wallConfig.setConditionOpXorAllow(true);
        }
        Boolean tempConditionOpBitwseAllow = conditionOpBitwseAllow == null ? config.getConditionOpBitwseAllow() : conditionOpBitwseAllow;
        if (tempConditionOpBitwseAllow != null && !tempConditionOpBitwseAllow) {
            wallConfig.setConditionOpBitwseAllow(false);
        }
        Boolean tempCaseConditionConstAllow = caseConditionConstAllow == null ? config.getCaseConditionConstAllow() : caseConditionConstAllow;
        if (tempCaseConditionConstAllow != null && tempCaseConditionConstAllow) {
            wallConfig.setCaseConditionConstAllow(true);
        }
        Boolean tempCompleteInsertValuesCheck = completeInsertValuesCheck == null ? config.getCompleteInsertValuesCheck() : completeInsertValuesCheck;
        if (tempCompleteInsertValuesCheck != null && tempCompleteInsertValuesCheck) {
            wallConfig.setCompleteInsertValuesCheck(true);
        }
        return wallConfig;
    }
}