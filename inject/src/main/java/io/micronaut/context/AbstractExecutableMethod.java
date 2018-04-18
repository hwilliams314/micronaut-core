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
package io.micronaut.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.inject.ExecutableMethod;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Abstract base class for generated {@link ExecutableMethod} classes to implement. The generated classes should
 * implement the {@link ExecutableMethod#invoke(Object, Object...)} method at compile time providing direct dispatch
 * of the target method</p>
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Internal
public abstract class AbstractExecutableMethod extends AbstractExecutable implements ExecutableMethod {

    private final ReturnType returnType;

    @SuppressWarnings("WeakerAccess")
    protected AbstractExecutableMethod(Class<?> declaringType,
                                       String methodName,
                                       Argument genericReturnType,
                                       Argument... arguments) {
        super(declaringType, methodName, arguments);
        this.returnType = new ReturnTypeImpl(genericReturnType);

    }

    @SuppressWarnings("WeakerAccess")
    protected AbstractExecutableMethod(Class<?> declaringType,
                                       String methodName,
                                       Argument genericReturnType) {
        this(declaringType, methodName, genericReturnType, Argument.ZERO_ARGUMENTS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractExecutableMethod that = (AbstractExecutableMethod) o;
        return Objects.equals(declaringType, that.declaringType) &&
                Objects.equals(methodName, that.methodName) &&
                Arrays.equals(argTypes, that.argTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(declaringType, methodName);
        result = 31 * result + Arrays.hashCode(argTypes);
        return result;
    }

    @Override
    public String toString() {
        Stream<String> stringStream = Arrays.stream(getArguments()).map(Argument::toString);
        String text = stringStream.collect(Collectors.joining(","));
        return getReturnType().getType().getSimpleName() + " " + getMethodName() + "(" + text + ")";
    }

    @Override
    public ReturnType getReturnType() {
        return returnType;
    }

    @Override
    public Class[] getArgumentTypes() {
        return argTypes;
    }

    @Override
    public Class getDeclaringType() {
        return declaringType;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public final Object invoke(Object instance, Object... arguments) {
        validateArguments(arguments);
        return invokeInternal(instance, arguments);
    }

    @SuppressWarnings("WeakerAccess")
    protected abstract Object invokeInternal(Object instance, Object[] arguments);

    private void validateArguments(Object[] argArray) {
        Argument[] arguments = getArguments();
        int requiredCount = arguments.length;
        int actualCount = argArray == null ? 0 : argArray.length;
        if (requiredCount != actualCount) {
            throw new IllegalArgumentException("Wrong number of arguments to method: " + getMethodName());
        }
        if (requiredCount > 0) {
            for (int i = 0; i < arguments.length; i++) {
                Argument argument = arguments[i];
                Class type = ReflectionUtils.getWrapperType(argument.getType());
                Object value = argArray[i];
                if (value != null && !type.isInstance(value)) {
                    throw new IllegalArgumentException("Invalid type [" + argArray[i].getClass().getName() + "] for argument [" + argument + "] of method: " + getMethodName());
                }
            }
        }
    }

    class ReturnTypeImpl implements ReturnType<Object> {
        private final Argument<?> genericReturnType;

        ReturnTypeImpl(Argument genericReturnType) {
            this.genericReturnType = genericReturnType != null ? genericReturnType : Argument.of(getTargetMethod().getReturnType());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<Object> getType() {
            return (Class<Object>) genericReturnType.getType();
        }


        @Override
        public AnnotatedElement[] getAnnotatedElements() {
            Method method = getTargetMethod();
            if(method != null) {
                return new AnnotatedElement[]{method.getAnnotatedReturnType(), method};
            }
            else {
                return genericReturnType.getAnnotatedElements();
            }
        }

        @Override
        public Argument[] getTypeParameters() {
            return genericReturnType.getTypeParameters();
        }

        @Override
        public Map<String, Argument<?>> getTypeVariables() {
            return genericReturnType.getTypeVariables();
        }
    }
}
