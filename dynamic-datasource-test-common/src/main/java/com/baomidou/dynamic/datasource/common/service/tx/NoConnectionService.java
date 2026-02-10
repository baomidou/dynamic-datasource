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
package com.baomidou.dynamic.datasource.common.service.tx;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.tx.DsPropagation;
import org.springframework.stereotype.Service;

/**
 * Service with REQUIRES_NEW transaction but no JDBC connections
 */
@Service
@DS("order")
public class NoConnectionService {

    /**
     * Inner REQUIRES_NEW transaction without JDBC connection
     * This should not throw NPE when committing
     */
    @DSTransactional(propagation = DsPropagation.REQUIRES_NEW)
    public void innerRequiresNewWithoutConnection() {
        // No database operations - just business logic
        System.out.println("Business logic without database operations");
    }
}
