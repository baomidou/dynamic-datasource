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
package com.baomidou.dynamic.datasource.tx;

/**
 * SuspendedResourcesHolder
 *
 * @author Hzh
 */
public class SuspendedResourcesHolder {
    /**
     * 事务ID
     */
    private String xid;

    /**
     * Instantiates a new Suspended resources holder.
     *
     * @param xid 事务ID
     */
    public SuspendedResourcesHolder(String xid) {
        if (xid == null) {
            throw new IllegalArgumentException("xid must be not null");
        }
        this.xid = xid;
    }

    /**
     * 获得事务ID.
     *
     * @return 事务ID
     */
    public String getXid() {
        return xid;
    }
}