/*
 * Copyright © 2018 organization baomidou
 *
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
 */
package com.baomidou.dynamic.datasource.common.v4;

import com.baomidou.dynamic.datasource.common.base.AbstractDsTransactionalTest;

/**
 * Spring Boot 4.x 下运行共享的 {@code @DSTransactional} 集成场景。
 * 断言全部在 {@link AbstractDsTransactionalTest} 中，与其它 Boot 版本完全一致。
 */
public class DsTransactionalTest extends AbstractDsTransactionalTest {
}
