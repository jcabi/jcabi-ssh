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
            MatcherAssert.assertThat(exit, Matchers.is(0));
            MatcherAssert.assertThat(output.toString(), Matchers.is(cmd));
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
            MatcherAssert.assertThat(exit, Matchers.is(0));
            MatcherAssert.assertThat(output.toString(), Matchers.is(cmd));
        } finally {
            sshd.stop();
        }
    }

    /**
     * Allocate free port.
     * @return Found port.
     * @throws IOException In case of error.
     */
    private static int port() throws IOException {
        final ServerSocket socket = new ServerSocket(0);
        final int port = socket.getLocalPort();
        socket.close();
        return port;
    }
}
