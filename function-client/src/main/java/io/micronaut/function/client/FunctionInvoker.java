/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.function.client;

import io.micronaut.core.type.Argument;

/**
 * @author graemerocher
 * @since 1.0
 */
public interface FunctionInvoker<I, O> {

    /**
     * Invoke the given function definition for the given input and expected response type.
     *
     * @param definition The definition
     * @param input      The input
     * @param outputType The response type
     * @return The result
     */
    O invoke(FunctionDefinition definition, I input, Argument<O> outputType);
}
