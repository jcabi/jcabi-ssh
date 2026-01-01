/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
