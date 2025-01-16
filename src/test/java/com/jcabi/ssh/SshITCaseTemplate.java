/*
 * Copyright (c) 2014-2025, jcabi.com
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.cactoos.io.DeadInputStream;
import org.cactoos.io.TeeOutputStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Integration test for ${@link Ssh}, which connects to
 * a real SSHD server.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (1000 lines)
 */
@SuppressWarnings("PMD.JUnitTestClassShouldBeFinal")
abstract class SshITCaseTemplate {

    /**
     * Make a shell.
     * @return The shell
     * @throws Exception If fails
     */
    public abstract Shell shell() throws Exception;

    @Test
    void executesCommandOnServer() throws Exception {
        MatcherAssert.assertThat(
            "should starts with 'jeff'",
            new Shell.Plain(
                this.shell()
            ).exec("whoami"),
            Matchers.startsWith("jeff")
        );
    }

    @Test
    void executesBrokenCommandOnServer() throws Exception {
        MatcherAssert.assertThat(
            "should not equal to 0",
            this.shell().exec(
                "this-command-doesnt-exist",
                new DeadInputStream(),
                Logger.stream(Level.INFO, Ssh.class),
                Logger.stream(Level.WARNING, Ssh.class)
            ),
            Matchers.not(Matchers.equalTo(0))
        );
    }

    @Test
    void consumesInputStream() throws Exception {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        MatcherAssert.assertThat(
            "should equal to 0",
            this.shell().exec(
                "cat",
                new ByteArrayInputStream("Hello, world!".getBytes()),
                new TeeOutputStream(stdout, Logger.stream(Level.INFO, Ssh.class)),
                Logger.stream(Level.WARNING, Ssh.class)
            ),
            Matchers.equalTo(0)
        );
        MatcherAssert.assertThat(
            "should starts with 'Hello'",
            stdout.toString(),
            Matchers.startsWith("Hello")
        );
    }

    @Test
    void dropsConnectionForNohup() throws Exception {
        final long start = System.currentTimeMillis();
        this.exec(
            "nohup sleep 5 > /dev/null 2>&1 &"
        );
        MatcherAssert.assertThat(
            "should less than 3 seconds",
            System.currentTimeMillis() - start,
            Matchers.lessThan(TimeUnit.SECONDS.toMillis(3L))
        );
    }

    @Test
    void dropsConnectionWithoutNohup() throws Exception {
        final long start = System.currentTimeMillis();
        this.exec(
            "echo 'Hello' ; sleep 5 >/dev/null 2>&1 & echo 'Bye'"
        );
        MatcherAssert.assertThat(
            "should less than 3 seconds",
            System.currentTimeMillis() - start,
            Matchers.lessThan(TimeUnit.SECONDS.toMillis(3L))
        );
    }

    /**
     * Exec this command at this shell and return stdout.
     * @param cmd The command
     * @return Stdout
     * @throws Exception If fails
     */
    private String exec(final String cmd) throws Exception {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final int exit = this.shell().exec(
            cmd,
            new DeadInputStream(),
            new TeeOutputStream(stdout, Logger.stream(Level.INFO, Ssh.class)),
            Logger.stream(Level.WARNING, Ssh.class)
        );
        MatcherAssert.assertThat("should be 0", exit, Matchers.is(0));
        return stdout.toString();
    }

}
