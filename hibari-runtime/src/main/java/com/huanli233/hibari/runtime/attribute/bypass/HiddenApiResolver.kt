/*
 * Hikage - A real-time Android View runtime powered by Kotlin DSL.
 * Copyright (C) 2019 HighCapable
 * https://github.com/BetterAndroid/Hikage
 *
 * Apache License Version 2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is created by fankes on 2026/6/3.
 */
package com.huanli233.hibari.runtime.attribute.bypass

import android.os.Build
import com.highcapable.kavaref.resolver.processor.MemberProcessor
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Constructor
import java.lang.reflect.Method

/**
 * The resolver for hidden API access.
 */
internal object HiddenApiResolver {

    /**
     * The resolver for member processor (bypasses hidden API restrictions on P+).
     * @return [MemberProcessor.Resolver]
     */
    val processor = object : MemberProcessor.Resolver() {

        override fun <T : Any> getDeclaredConstructors(declaringClass: Class<T>): List<Constructor<T>> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                runCatching {
                    HiddenApiBypass.getDeclaredMethods(declaringClass).filterIsInstance<Constructor<T>>().toList()
                }.getOrElse { super.getDeclaredConstructors(declaringClass) }
            } else super.getDeclaredConstructors(declaringClass)

        override fun <T : Any> getDeclaredMethods(declaringClass: Class<T>): List<Method> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                runCatching {
                    HiddenApiBypass.getDeclaredMethods(declaringClass).filterIsInstance<Method>().toList()
                }.getOrElse { super.getDeclaredMethods(declaringClass) }
            } else super.getDeclaredMethods(declaringClass)
    }
}