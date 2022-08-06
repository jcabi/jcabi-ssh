/*
 * Copyright (c) 2014-2022, jcabi.com
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

import com.jcabi.log.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.cactoos.io.DeadInputStream;
import org.cactoos.io.TeeOutputStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Integration test for ${@link Ssh}, which connects to
 * a real SSHD server over the Internet.
 *
 * @since 1.0
 */
public final class SshITCase {

    /**
     * Host to connect to.
     */
    private static final String HOST = System.getProperty("failsafe.host", "");

    /**
     * Port to connect to.
     */
    private static final String PORT = System.getProperty("failsafe.port", "");

    /**
     * Username to use.
     */
    private static final String LOGIN = System.getProperty("failsafe.login", "");

    /**
     * Private key to use for connection.
     */
    private static final String KEY = System.getProperty("failsafe.key", "");

    @Test
    public void executesCommandOnServer() throws Exception {
        MatcherAssert.assertThat(
            SshITCase.exec(SshITCase.shell(), "whoami"),
            Matchers.startsWith(SshITCase.LOGIN)
        );
    }

    @Test
    public void executesBrokenCommandOnServer() throws Exception {
        MatcherAssert.assertThat(
            SshITCase.shell().exec(
                "this-command-doesnt-exist",
                new DeadInputStream(),
                Logger.stream(Level.INFO, Ssh.class),
                Logger.stream(Level.WARNING, Ssh.class)
            ),
            Matchers.not(Matchers.equalTo(0))
        );
    }

    @Test
    public void consumesInputStream() throws Exception {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        MatcherAssert.assertThat(
            SshITCase.shell().exec(
                "cat",
                new ByteArrayInputStream("Hello, world!".getBytes()),
                new TeeOutputStream(stdout, Logger.stream(Level.INFO, Ssh.class)),
                Logger.stream(Level.WARNING, Ssh.class)
            ),
            Matchers.equalTo(0)
        );
        MatcherAssert.assertThat(
            stdout.toString(),
            Matchers.startsWith("Hello")
        );
    }

    @Test
    public void dropsConnectionForNohup() throws Exception {
        final long start = System.currentTimeMillis();
        SshITCase.exec(
            SshITCase.shell(),
            "( nohup echo 1 > /dev/null 2>&1; sleep 5 ) &"
        );
        MatcherAssert.assertThat(
            System.currentTimeMillis() - start,
            Matchers.lessThan(TimeUnit.SECONDS.toMillis(3L))
        );
    }

    @Test
    public void dropsConnectionWithoutNohup() throws Exception {
        final long start = System.currentTimeMillis();
        SshITCase.exec(
            SshITCase.shell(),
            "echo 'Hello' ; sleep 5 & ps ; echo 'Bye'"
        );
        MatcherAssert.assertThat(
            System.currentTimeMillis() - start,
            Matchers.lessThan(TimeUnit.SECONDS.toMillis(3L))
        );
    }

    /**
     * Make a shell.
     * @return The shell
     * @throws UnknownHostException If not found
     */
    private static Shell shell() throws UnknownHostException {
        Assumptions.assumeFalse(SshITCase.HOST.isEmpty());
        return new Shell.Verbose(
            new Ssh(
                SshITCase.HOST,
                Integer.parseInt(SshITCase.PORT),
                SshITCase.LOGIN,
                SshITCase.KEY
            )
        );
    }

    /**
     * Exec this command at this shell and return stdout.
     * @param shell The shell
     * @param cmd The command
     * @return Stdout
     * @throws IOException If fails
     */
    private static String exec(final Shell shell, final String cmd) throws IOException {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final int exit = shell.exec(
            cmd,
            new DeadInputStream(),
            new TeeOutputStream(stdout, Logger.stream(Level.INFO, Ssh.class)),
            Logger.stream(Level.WARNING, Ssh.class)
        );
        MatcherAssert.assertThat(exit, Matchers.is(0));
        return stdout.toString();
    }

}
