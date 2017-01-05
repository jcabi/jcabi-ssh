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
import org.apache.commons.io.input.NullInputStream;
import org.apache.sshd.SshServer;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Unit tests for {@link SSHByPassword}.
 *
 * @author Georgy Vlasov (wlasowegor@gmail.com)
 * @version $Id$
 * @since 1.4
 */
public final class SSHByPasswordTest {

    /**
     * Checks if {@link SSHByPassword} can execute a command on an SSH server.
     * @throws Exception If fails
     */
    @Test
    public void executesCommand() throws Exception {
        final String username = "test";
        final String password = "password";
        final int port = SSHByPasswordTest.port();
        final SshServer sshd = new MockSshServerBuilder(port)
            .usePasswordAuthentication(username, password).build();
        sshd.setCommandFactory(new MkCommandCreator());
        sshd.start();
        final String cmd = "some test command";
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final Shell shell = new Shell.Verbose(
            new SSHByPassword(
                InetAddress.getLocalHost().getHostAddress(),
                port,
                username,
                password
            )
        );
        final int exit = shell.exec(
            cmd,
            new NullInputStream(0L),
            output,
            Logger.stream(Level.WARNING, true)
        );
        sshd.stop();
        MatcherAssert.assertThat(exit, Matchers.equalTo(0));
        MatcherAssert.assertThat(output.toString(), Matchers.equalTo(cmd));
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
