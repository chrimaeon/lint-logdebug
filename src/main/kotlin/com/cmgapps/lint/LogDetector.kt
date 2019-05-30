/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmgapps.lint

import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.*

class LogDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String>? {
        return listOf("d", "v")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {

        val evaluator = context.evaluator

        if (!evaluator.isMemberInClass(method, LOG_CLS) && !evaluator.isMemberInClass(method, TIMBER_CLS) &&
                !evaluator.isMemberInClass(method, TREE_CLS)) {
            return
        }

        val withinConditional = checkWithinConditional(node.uastParent)

        if (!withinConditional) {
            val className = if (evaluator.isMemberInClass(method, LOG_CLS)) "Log" else "Timber"
            val message = "The log call $className.${node.methodName}(...) should be " +
                    "conditional: surround with `if (Log.isLoggable(...))` or " +
                    "`if (BuildConfig.DEBUG) { ... }`"
            context.report(ISSUE, node, context.getLocation(node), message, quickFix(node, method, evaluator, context))
        }
    }

    private fun checkWithinConditional(start: UElement?): Boolean {
        var curr = if (isKotlin(start?.sourcePsi)) start?.uastParent else start
        while (curr != null) {

            if (curr is UIfExpression) {
                var condition = curr.condition

                if (condition is UQualifiedReferenceExpression) {
                    condition = getLastInQualifiedChain(condition)
                }

                if (condition is UCallExpression && condition.methodName == ISLOGGABLE_FNC) {
                    return true
                }

                if (condition is USimpleNameReferenceExpression && condition.identifier == DEBUG_MEMBER) {
                    return true
                }

            } else if (curr is UCallExpression ||
                    curr is UMethod ||
                    curr is UClassInitializer ||
                    curr is UField ||
                    curr is UClass
            ) { // static block
                break
            }

            curr = curr.uastParent
        }
        return false
    }

    private fun quickFix(node: UCallExpression, method: PsiMethod, evaluator: JavaEvaluator, context: JavaContext): LintFix {
        val isKotlin = isKotlin(node.sourcePsi)

        val sourceString = if (isKotlin) {
            node.uastParent!!.asSourceString()
        } else {
            "${node.asSourceString()};"
        }

        val buildConfigFix = """
            |if (BuildConfig.DEBUG) {
            |    $sourceString
            |}
        """.trimMargin()

        val location = context.getRangeLocation(node.uastParent!!, 0, node, if (isKotlin) 0 else 1)

        return fix().group().apply {
            add(
                    fix().name("Surround with `if (BuildConfig.DEBUG)`")
                            .replace()
                            .range(location)
                            .text(sourceString)
                            .shortenNames()
                            .reformat(true)
                            .with(buildConfigFix)
                            .robot(true)
                            .build()
            )

            if (evaluator.isMemberInClass(method, LOG_CLS)) {
                val tag = node.valueArguments[0].asSourceString()

                val isLoggableFix = """
                            |if ($LOG_CLS.isLoggable($tag, ${getLogLevel(node.methodName!!)})) {
                            |   $sourceString
                            |}""".trimMargin()
                add(
                        fix().name("Surround with `if (Log.isLoggable(...))`")
                                .replace()
                                .range(location)
                                .text(sourceString)
                                .shortenNames()
                                .reformat(true)
                                .with(isLoggableFix)
                                .robot(true)
                                .build()
                )
            }
        }
                .build()
    }

    private fun getLogLevel(methodName: String) = when (methodName) {
        // see getApplicableMethodNames for valid method names
        "d" -> "Log.DEBUG"
        "v" -> "Log.VERBOSE"
        else -> ""
    }

    private fun getLastInQualifiedChain(node: UQualifiedReferenceExpression): UExpression {
        var last = node.selector
        while (last is UQualifiedReferenceExpression) {
            last = last.selector
        }
        return last
    }


    companion object {
        private val ISSUE = Issue.Companion.create(
                id = "LogDebugConditional",
                briefDescription = "Unconditional Logging calls",
                explanation = """
                    The BuildConfig class provides a constant, "DEBUG", which indicates \
                    whether the code is being built in release mode or in debug mode. In release mode, you typically \
                    want to strip out all the logging calls. Since the compiler will automatically remove all code \
                    which is inside a "if (false)" check, surrounding your logging calls with a check for \
                    BuildConfig.DEBUG is a good idea.

                    If you **really** intend for the logging to be present in release mode, you can suppress this \
                    warning with a @SuppressLint annotation for the intentional logging calls.""",
                category = Category.PERFORMANCE,
                priority = 5,
                severity = Severity.WARNING,
                androidSpecific = true,
                implementation = Implementation(LogDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )

        val issues = arrayOf(ISSUE)

        private const val LOG_CLS = "android.util.Log"
        private const val TIMBER_CLS = "timber.log.Timber"
        private const val TREE_CLS = "timber.log.Timber.Tree"
        private const val DEBUG_MEMBER = "DEBUG"
        private const val ISLOGGABLE_FNC = "isLoggable"
    }
}