/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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

/**
 * Common module for any ssh shell.
 * @since 1.5.2
 */
@ToString
@EqualsAndHashCode(of = { "addr", "port", "login" })
@Getter
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
abstract class AbstractSshShell implements Shell {

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
    AbstractSshShell(
        final String adr,
        final int prt,
        final String user) throws UnknownHostException {
        this.addr = InetAddress.getByName(adr).getHostAddress();
        this.port = prt;
        this.login = user;
    }

    // @checkstyle ParameterNumberCheck (2 lines)
    @Override
    public int exec(final String command, final InputStream stdin,
        final OutputStream stdout, final OutputStream stderr)
        throws IOException {
        return new Execution(
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
