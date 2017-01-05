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

import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

/**
 * Common module for any ssh shell.
 * @author Jose Volmei Dal Pra Junior (jrdalpra@gmail.com)
 * @version $Id$
 * @since 1.5.2
 */
@ToString
@EqualsAndHashCode(of = { "addr", "port", "login" })
@Getter
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
abstract class AbstractSSHShell implements Shell {

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
     * Constructor.
     * @param adr Address that you want to connect to.
     * @param prt Port that you want to reach.
     * @param user User that will be used when connecting.
     * @throws UnknownHostException when host is unknown.
     */
    AbstractSSHShell(
        final String adr,
        final int prt,
        final String user) throws UnknownHostException {
        this.addr = InetAddress.getByName(adr).getHostAddress();
        this.port = prt;
        this.login = user;
        Validate.matchesPattern(
            this.addr,
            "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
            "Invalid IP address of the server `%s`",
            this.addr
        );
        Validate.notEmpty(user, "user name can't be empty");
    }

    // @checkstyle ParameterNumberCheck (2 lines)
    @Override
    public int exec(final String command, final InputStream stdin,
                    final OutputStream stdout, final OutputStream stderr)
        throws IOException {
        return new Execution.Default(
            command,
            stdin,
            stdout,
            stderr,
            this.session()
        ).exec();
    }

    /**
     * Create and return a session, connected.
     * @return JSch session
     * @throws IOException If some IO problem inside
     */
    protected abstract Session session()  throws IOException;

}
