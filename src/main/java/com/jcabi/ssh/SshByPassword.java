/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.ssh;

import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.log.Logger;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * SSH channel with authentication by password.
 * @see Ssh For SSH channel with authenticaton using private key.
 * @since 1.4
 */
@ToString
@EqualsAndHashCode(of = "password", callSuper = true)
public final class SshByPassword extends AbstractSshShell {

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
    public SshByPassword(final String adr, final int prt,
        final String user, final String passwd)
        throws UnknownHostException {
        super(adr, prt, user);
        this.password = passwd;
    }

    // @checkstyle ProtectedMethodInFinalClassCheck (10 lines)
    @Override
    @RetryOnFailure(
        attempts = 7,
        delay = 1,
        unit = TimeUnit.MINUTES,
        verbose = false,
        types = IOException.class
    )
    protected Session session() throws IOException {
        try {
            JSch.setLogger(new JschLogger());
            final JSch jsch = new JSch();
            Logger.debug(
                this,
                "Opening SSH session to %s@%s:%s (auth with password)...",
                this.getLogin(), this.getAddr(), this.getPort()
            );
            final Session session = jsch.getSession(
                this.getLogin(), this.getAddr(), this.getPort()
            );
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(this.password);
            session.setServerAliveInterval(
                (int) TimeUnit.SECONDS.toMillis(10)
            );
            session.setServerAliveCountMax(1_000_000);
            session.connect((int) TimeUnit.SECONDS.toMillis(10L));
            return session;
        } catch (final JSchException ex) {
            throw new IOException(ex);
        }
    }
}
