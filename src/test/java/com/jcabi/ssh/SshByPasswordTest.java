/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2026 Yegor Bugayenko
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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SshByPassword}.
 *
 * @since 1.4
 */
final class SshByPasswordTest {

    @Test
    void executesCommand() throws Exception {
        final String username = "test";
        final String password = "password";
        final int port = SshByPasswordTest.port();
        final SshServer sshd = new MockSshServerBuilder(port)
            .usePasswordAuthentication(username, password).build();
        sshd.setCommandFactory(new MkCommandFactory());
        sshd.start();
        final String cmd = "some test command";
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final Shell shell = new Shell.Verbose(
            new SshByPassword(
                InetAddress.getLocalHost().getHostAddress(),
                port,
                username,
                password
            )
        );
        final int exit = shell.exec(
            cmd,
            new DeadInputStream(),
            output,
            Logger.stream(Level.WARNING, true)
        );
        sshd.stop();
        MatcherAssert.assertThat("should equal to 0", exit, Matchers.equalTo(0));
        MatcherAssert.assertThat("should equal to cmd", output.toString(), Matchers.equalTo(cmd));
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
