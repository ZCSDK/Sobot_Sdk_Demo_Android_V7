/**
 * 内嵌的 ZXing 条码/二维码解析库源码（Apache 2.0 / 来源 com.google.zxing:core）
 * <p>
 * 安全说明（CWE-1104）：
 * <ul>
 *   <li>本目录为历史遗留的源码拷贝，版本不明确，难以跟踪 CVE 修复</li>
 *   <li>SDK 实际仅使用二维码（QR Code）扫描/识别功能，其余 DataMatrix / PDF417 / Aztec / 一维码等编解码器为冗余代码</li>
 * </ul>
 * <p>
 * 升级建议（下一个主版本）：
 * <ol>
 *   <li>评估实际依赖范围（{@code QRCodeReader} 及其依赖链），裁剪未使用的子包</li>
 *   <li>改为 Gradle 依赖：{@code implementation("com.google.zxing:core:3.5.3")} 以便集中跟踪 CVE</li>
 *   <li>或迁移到 Google ML Kit Barcode Scanning API</li>
 * </ol>
 */
package com.sobot.chat.widget.zxing;
