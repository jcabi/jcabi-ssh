/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

/**
 * SSH client.
 *
 * <p>Usage scenario is very simple:
 *
 * <pre> String hello = new Shell.Plain(
 *   new SSH(
 *     "ssh.example.com", 22,
 *     "yegor", "-----BEGIN RSА PRIVATE KEY-----..."
 *   )
 * ).exec("echo 'Hello, world!'");</pre>
 *
 * <p>The only dependency you need is (check our latest version available
 * at <a href="http://ssh.jcabi.com">ssh.jcabi.com</a>):
 *
 * <pre>&lt;dependency&gt;
 *   &lt;groupId&gt;com.jcabi&lt;/groupId&gt;
 *   &lt;artifactId&gt;jcabi-ssh&lt;/artifactId&gt;
 * &lt;/dependency&gt;</pre>
 *
 * @since 1.0
 * @see <a href="http://ssh.jcabi.com/">project website</a>
 * @see <a href="http://www.yegor256.com/2014/09/02/java-ssh-client.html">article by Yegor Bugayenko</a>
 */
package com.jcabi.ssh;
