/**
 * Copyright (c) 2014-2017, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.ssh;

import com.jcabi.log.VerboseProcess;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

/**
 * Test SSHD daemon (only for Linux).
 *
 * <p>It is a convenient class for unit testing of your SSH
 * clients:
 *
 * <pre> try (SSHD sshd = new SSHD()) {
 *   String uptime = new Shell.Plain(
 *     SSH(sshd.host(), sshd.login(), sshd.port(), sshd.key())
 *   ).exec("uptime");
 * }</pre>
 *
 * <p>If you forget to call {@link #close()}, SSH daemon will be
 * up and running until a shutdown of the JVM.</p>
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class SSHD implements Closeable {

    /**
     * Temp directory.
     */
    private final transient File dir;

    /**
     * The process with SSHD.
     */
    private final transient Process process;

    /**
     * Port.
     */
    private final transient int prt;

    /**
     * Ctor.
     * @throws IOException If fails
     * @since 1.5
     */
    public SSHD() throws IOException {
        this(new File(System.getProperty("java.io.tmpdir")));
    }

    /**
     * Ctor.
     * @param path Directory to work in
     * @throws IOException If fails
     */
    public SSHD(final File path) throws IOException {
        this.dir = path;
        final File rsa = new File(this.dir, "host_rsa_key");
        IOUtils.copy(
            this.getClass().getResourceAsStream("ssh_host_rsa_key"),
            new FileOutputStream(rsa)
        );
        final File keys = new File(this.dir, "authorized");
        IOUtils.copy(
            this.getClass().getResourceAsStream("authorized_keys"),
            new FileOutputStream(keys)
        );
        new VerboseProcess(
            new ProcessBuilder().command(
                "chmod", "600",
                keys.getAbsolutePath(),
                rsa.getAbsolutePath()
            )
        ).stdout();
        this.prt = SSHD.reserve();
        this.process = new ProcessBuilder().command(
            "/usr/sbin/sshd",
            "-p",
            Integer.toString(this.prt),
            "-h",
            rsa.getAbsolutePath(),
            "-D",
            "-e",
            "-o", String.format("PidFile=%s", new File(this.dir, "pid")),
            "-o", "UsePAM=no",
            "-o", String.format("AuthorizedKeysFile=%s", keys),
            "-o", "StrictModes=no"
        ).start();
        new Thread(
            new Runnable() {
                @Override
                public void run() {
                    new VerboseProcess(SSHD.this.process).stdout();
                }
            }
        ).start();
        Runtime.getRuntime().addShutdownHook(
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        SSHD.this.close();
                    }
                }
            )
        );
        try {
            TimeUnit.SECONDS.sleep(1L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void close() {
        this.process.destroy();
    }

    /**
     * Get home dir.
     * @return Dir
     */
    public File home() {
        return this.dir;
    }

    /**
     * Get user name to login.
     * @return User name
     */
    public String login() {
        return new VerboseProcess(
            new ProcessBuilder().command("id", "-n", "-u")
        ).stdout().trim();
    }

    /**
     * Get host of SSH.
     * @return Hostname
     * @since 1.1
     */
    public String host() {
        return new VerboseProcess(
            new ProcessBuilder().command("hostname")
        ).stdout().trim();
    }

    /**
     * Get port.
     *
     * <p>Don't forget to start
     *
     * @return Port number
     * @since 1.1
     */
    public int port() {
        return this.prt;
    }

    /**
     * Get private SSH key for login.
     * @return Key
     * @throws IOException If fails
     */
    public String key() throws IOException {
        return IOUtils.toString(
            this.getClass().getResourceAsStream("id_rsa"),
            CharEncoding.UTF_8
        );
    }

    /**
     * Get an instance of Shell.
     * @return Shell
     * @throws IOException If fails
     */
    public Shell connect() throws IOException {
        return new SSH(this.host(), this.port(), this.login(), this.key());
    }

    /**
     * Get a random TCP port.
     * @return Port
     * @throws IOException If fails
     */
    private static int reserve() throws IOException {
        final ServerSocket socket = new ServerSocket(0);
        try {
            return socket.getLocalPort();
        } finally {
            socket.close();
        }
    }

}
