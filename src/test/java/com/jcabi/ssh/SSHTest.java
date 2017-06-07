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

import com.jcabi.log.Logger;
import com.jcabi.ssh.mock.MkCommandCreator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.sshd.SshServer;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for ${@link SSH}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class SSHTest {

    /**
     * SSH can escape an argument.
     * @throws Exception In case of error.
     * @since 1.0.1
     */
    @Test
    public void escapesArgument() throws Exception {
        MatcherAssert.assertThat(
            SSH.escape("hi,\n '$1'"),
            Matchers.equalTo("'hi,\n '\\''$1'\\'''")
        );
    }

    /**
     * SSH can execute command on ssh server.
     * @throws Exception In case of error.
     */
    @Test
    public void executeCommandOnServer() throws Exception {
        final int port = SSHTest.port();
        final SshServer sshd = new MockSshServerBuilder(port)
            .usePublicKeyAuthentication().build();
        try {
            sshd.setCommandFactory(new MkCommandCreator());
            sshd.start();
            final String cmd = "some test command";
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final Shell shell = new Shell.Verbose(
                new SSH(
                    InetAddress.getLocalHost().getCanonicalHostName(),
                    port,
                    "test",
                    IOUtils.toString(
                        this.getClass().getResourceAsStream("private.key")
                    )
                )
            );
            final int exit = shell.exec(
                cmd,
                new NullInputStream(0L),
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
     * SSH can execute command on ssh server with encrypted private key.
     * @throws Exception In case of error.
     */
    @Test
    public void executeCommandOnServerWithPrivateKey() throws Exception {
        final int port = SSHTest.port();
        final SshServer sshd = new MockSshServerBuilder(port)
            .usePublicKeyAuthentication().build();
        try {
            sshd.setCommandFactory(new MkCommandCreator());
            sshd.start();
            final String cmd = "some other test command";
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final Shell shell = new Shell.Verbose(
                new SSH(
                    InetAddress.getLocalHost().getCanonicalHostName(),
                    port,
                    "other test",
                    IOUtils.toString(
                        this.getClass().getResourceAsStream(
                            "encrypted_private.key"
                        )
                    ),
                    "test-passphrase"
                )
            );
            final int exit = shell.exec(
                cmd,
                new NullInputStream(0L),
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
