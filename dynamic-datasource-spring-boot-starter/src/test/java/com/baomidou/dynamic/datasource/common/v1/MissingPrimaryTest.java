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
package com.baomidou.dynamic.datasource.common.v1;

import com.baomidou.dynamic.datasource.common.base.AbstractMissingPrimaryTest;

/**
 * Boot2 侧启动时没有主库的场景：断言全部来自共享基类，此处仅绑定本版本的自动装配。
 */
public class MissingPrimaryTest extends AbstractMissingPrimaryTest {
}
