/*
 * Copyright 2025 Kyant
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
 *
 * ---------------------------------------------------------------------------
 * 本文件改编自 Kyant0/AndroidLiquidGlass（Apache License 2.0）
 * 上游文件：app/src/androidMain/kotlin/com/kyant/backdrop/catalog/utils/Coroutines.kt
 * 仓库地址：https://github.com/Kyant0/AndroidLiquidGlass
 * 修改说明：包名改为 com.linuxlab.starter.ui.liquidglass；去掉 expect/actual 结构，
 *           只保留 Android 实现。
 * ---------------------------------------------------------------------------
 */

package com.linuxlab.starter.ui.liquidglass

import kotlinx.coroutines.android.awaitFrame

/** 等一帧，让动画在下一帧再收尾（上游 DampedDragAnimation 依赖它） */
suspend fun awaitFrame() {
    awaitFrame()
}
