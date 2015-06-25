/**
 * Copyright (c) 2014-2015, jcabi.com
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

import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

/**
 * SSH channel with authentication by password.
 * @author Georgy Vlasov (wlasowegor@gmail.com)
 * @version $Id$
 * @since 1.4
 * @see SSH For SSH channel with authenticaton using private key.
 */
@ToString
@EqualsAndHashCode(of = { "addr", "port", "login", "password" })
public final class SSHByPassword implements Shell {

    /**
     * IP address of the server.
     */
    private final transient String addr;

    /**
     * Port to use.
     */
    private final transient int port;

    /**
     * User name.
     */
    private final transient String login;

    /**
     * User password.
     */
    private final transient String password;

    /**
     * Constructor.
     * @param adr IP address
     * @param prt Port of server
     * @param user Login
     * @param passwd Password
     * @throws UnknownHostException If fails
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public SSHByPassword(final String adr, final int prt,
        final String user, final String passwd)
        throws UnknownHostException {
        this.addr = InetAddress.getByName(adr).getHostAddress();
        Validate.matchesPattern(
            this.addr,
            "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
            "Invalid IP address of the server `%s`",
            this.addr
        );
        this.port = prt;
        this.login = user;
        Validate.notEmpty(this.login, "user name can't be empty");
        this.password = passwd;
    }

    // @checkstyle ParameterNumberCheck (6 lines)
    @Override
    public int exec(final String command, final InputStream stdin,
        final OutputStream stdout, final OutputStream stderr)
        throws IOException {
        return new Execution.Default(
            command, stdin, stdout, stderr, this.session()
        ).exec();
    }

    /**
     * Create and return a session, connected.
     * @return JSch session
     * @throws IOException If some IO problem inside
     */
    @RetryOnFailure(
        attempts = Tv.SEVEN,
        delay = 1,
        unit = TimeUnit.MINUTES,
        verbose = false,
        randomize = true,
        types = IOException.class
    )
    private Session session() throws IOException {
        try {
            JSch.setConfig("StrictHostKeyChecking", "no");
            JSch.setLogger(new JschLogger());
            final JSch jsch = new JSch();
            Logger.debug(
                this,
                "Opening SSH session to %s@%s:%s (auth with password)...",
                this.login, this.addr, this.port
            );
            final Session session = jsch.getSession(
                this.login, this.addr, this.port
            );
            session.setPassword(this.password);
            session.setServerAliveInterval(
                (int) TimeUnit.SECONDS.toMillis((long) Tv.TEN)
            );
            session.setServerAliveCountMax(Tv.MILLION);
            session.connect();
            return session;
        } catch (final JSchException ex) {
            throw new IOException(ex);
        }
    }
}
