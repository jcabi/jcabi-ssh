/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.ssh;

import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Level;
import org.apache.sshd.server.SshServer;
import org.cactoos.io.DeadInputStream;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link Ssh}.
 *
 * @since 1.0
 */
final class SshTest {

    @Test
    void escapesArgument() {
        MatcherAssert.assertThat(
            "should equal to ''hi,\n '\\''$1'\\'''",
            Ssh.escape("hi,\n '$1'"),
            Matchers.equalTo("'hi,\n '\\''$1'\\'''")
        );
    }

    @Test
    void executeCommandOnServer() throws Exception {
        final int port = SshTest.port();
        final SshServer sshd = new MockSshServerBuilder(port)
            .usePublicKeyAuthentication().build();
        try {
            sshd.setCommandFactory(new MkCommandFactory());
            sshd.start();
            final String cmd = "some test command";
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final Shell shell = new Shell.Verbose(
                new Ssh(
                    InetAddress.getLocalHost().getCanonicalHostName(),
                    port,
                    "test",
                    new TextOf(
                        new ResourceOf("com/jcabi/ssh/private.key")
                    ).asString()
                )
            );
            final int exit = shell.exec(
                cmd,
                new DeadInputStream(),
                output,
                Logger.stream(Level.WARNING, true)
            );
            MatcherAssert.assertThat("should be 0", exit, Matchers.is(0));
            MatcherAssert.assertThat("should equal to cmd", output.toString(), Matchers.is(cmd));
        } finally {
            sshd.stop();
        }
    }

    @Test
    void executeCommandOnServerWithPrivateKey() throws Exception {
        final int port = SshTest.port();
        final SshServer sshd = new MockSshServerBuilder(port)
            .usePublicKeyAuthentication().build();
        try {
            sshd.setCommandFactory(new MkCommandFactory());
            sshd.start();
            final String cmd = "some other test command";
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final Shell shell = new Shell.Verbose(
                new Ssh(
                    InetAddress.getLocalHost().getCanonicalHostName(),
                    port,
                    "other test",
                    new TextOf(
                        new ResourceOf("com/jcabi/ssh/encrypted_private.key")
                    ).asString(),
                    "test-passphrase"
                )
            );
            final int exit = shell.exec(
                cmd,
                new DeadInputStream(),
                output,
                Logger.stream(Level.WARNING, true)
            );
            MatcherAssert.assertThat("should be 0", exit, Matchers.is(0));
            MatcherAssert.assertThat("should equal to cmd", output.toString(), Matchers.is(cmd));
        } finally {
            sshd.stop(true);
        }
    }

    /**
     * Allocate free port.
     * @return Found port.
     * @throws IOException In case of error.
     */
    private static int port() throws IOException {
        final int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        return port;
    }
}
