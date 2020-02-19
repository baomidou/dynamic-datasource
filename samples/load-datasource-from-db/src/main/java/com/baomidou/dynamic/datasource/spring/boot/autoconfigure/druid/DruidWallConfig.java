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

import lombok.Data;

/**
 * Druid防火墙配置
 *
 * @author TaoYu
 */
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
}