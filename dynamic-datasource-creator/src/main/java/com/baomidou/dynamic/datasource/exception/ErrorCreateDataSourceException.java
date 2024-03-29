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
package com.baomidou.dynamic.datasource.exception;

/**
 * exception when  druid dataSource init failed
 *
 * @author TaoYu
 * @since 2.5.6
 */
public class ErrorCreateDataSourceException extends RuntimeException {

    /**
     * Constructor for ErrorCreateDataSourceException.
     *
     * @param message message
     */
    public ErrorCreateDataSourceException(String message) {
        super(message);
    }

    /**
     * Constructor for ErrorCreateDataSourceException.
     *
     * @param message message
     * @param cause   cause
     */
    public ErrorCreateDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}