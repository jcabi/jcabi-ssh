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
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.Validate;

/**
 * Single SSH Channel.
 *
 * <p>This class implements {@link Shell} interface. In order to use
 * it, just make an instance and call
 * {@link #exec(String,InputStream,OutputStream,OutputStream)} exec()}:
 *
 * <pre> String hello = new Shell.Plain(
 *   new SSH(
 *     "ssh.example.com", 22,
 *     "yegor", "-----BEGIN RSA PRIVATE KEY-----..."
 *   )
 * ).exec("echo 'Hello, world!'");</pre>
 *
 * <p>It is highly recommended to use classes from {@link Shell} interface,
 * they will simplify operations.</p>
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 * @see <a href="http://www.yegor256.com/2014/09/02/java-ssh-client.html">article by Yegor Bugayenko</a>
 */
@ToString
@EqualsAndHashCode(of = { "addr", "port", "login", "key" })
@SuppressWarnings("PMD.TooManyMethods")
public final class SSH implements Shell {

    /**
     * Default SSH port.
     */
    public static final int PORT = 22;

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
     * Private SSH key.
     */
    private final transient String key;

    /**
     * Constructor.
     * @param adr IP address
     * @param user Login
     * @param priv Private SSH key
     * @throws IOException If fails
     * @since 1.4
     */
    public SSH(final String adr, final String user, final URL priv)
        throws IOException {
        this(adr, SSH.PORT, user, priv);
    }

    /**
     * Constructor.
     * @param adr IP address
     * @param user Login
     * @param priv Private SSH key
     * @throws IOException If fails
     * @since 1.4
     */
    public SSH(final InetAddress adr, final String user, final URL priv)
        throws IOException {
        this(adr, SSH.PORT, user, priv);
    }

    /**
     * Constructor.
     * @param adr IP address
     * @param user Login
     * @param priv Private SSH key
     * @throws UnknownHostException If fails
     * @since 1.4
     */
    public SSH(final String adr, final String user, final String priv)
        throws UnknownHostException {
        this(adr, SSH.PORT, user, priv);
    }

    /**
     * Constructor.
     * @param adr IP address
     * @param user Login
     * @param priv Private SSH key
     * @throws UnknownHostException If fails
     * @since 1.4
     */
    public SSH(final InetAddress adr, final String user, final String priv)
        throws UnknownHostException {
        this(adr.getCanonicalHostName(), SSH.PORT, user, priv);
    }

    /**
     * Constructor.
     * @param adr IP address
     * @param prt Port of server
     * @param user Login
     * @param priv Private SSH key
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (6 lines)
     * @since 1.4
     */
    public SSH(final String adr, final int prt,
        final String user, final URL priv) throws IOException {
        this(adr, prt, user, IOUtils.toString(priv));
    }

    /**
     * Constructor.
     * @param adr IP address
     * @param prt Port of server
     * @param user Login
     * @param priv Private SSH key
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (6 lines)
     * @since 1.4
     */
    public SSH(final InetAddress adr, final int prt,
        final String user, final URL priv) throws IOException {
        this(adr.getCanonicalHostName(), prt, user, IOUtils.toString(priv));
    }

    /**
     * Constructor.
     * @param adr IP address
     * @param prt Port of server
     * @param user Login
     * @param priv Private SSH key
     * @throws UnknownHostException If fails
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public SSH(final String adr, final int prt,
        final String user, final String priv) throws UnknownHostException {
        this.addr = InetAddress.getByName(adr).getHostAddress();
        Validate.matchesPattern(
            this.addr,
            "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
            "Invalid IP address of the server `%s`",
            this.addr
        );
        this.login = user;
        Validate.notEmpty(this.login, "user name can't be empty");
        this.key = priv;
        this.port = prt;
    }

    // @checkstyle ParameterNumberCheck (5 lines)
    @Override
    public int exec(final String command, final InputStream stdin,
        final OutputStream stdout, final OutputStream stderr)
        throws IOException {
        try {
            final Session session = this.session();
            try {
                final ChannelExec channel = ChannelExec.class.cast(
                    session.openChannel("exec")
                );
                channel.setErrStream(stderr, false);
                channel.setOutputStream(stdout, false);
                channel.setInputStream(stdin, false);
                channel.setCommand(command);
                channel.connect();
                Logger.info(this, "$ %s", command);
                return this.exec(channel, session);
            } finally {
                session.disconnect();
            }
        } catch (final JSchException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Escape SSH argument.
     * @param arg Argument to escape
     * @return Escaped
     */
    public static String escape(final String arg) {
        return String.format("'%s'", arg.replace("'", "'\\''"));
    }

    /**
     * Exec this channel and return its exit code.
     * @param channel The channel to exec
     * @param session The session
     * @return Exit code (zero in case of success)
     * @throws IOException If fails
     */
    private int exec(final ChannelExec channel, final Session session)
        throws IOException {
        try {
            return this.code(channel, session);
        } finally {
            channel.disconnect();
        }
    }

    /**
     * Wait until it's done and return its code.
     * @param exec The channel
     * @param session The session
     * @return The exit code
     * @throws IOException If some IO problem inside
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private int code(final ChannelExec exec, final Session session)
        throws IOException {
        while (!exec.isClosed()) {
            try {
                session.sendKeepAliveMsg();
                // @checkstyle IllegalCatch (1 line)
            } catch (final Exception ex) {
                throw new IOException(ex);
            }
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException(ex);
            }
        }
        return exec.getExitStatus();
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
            final File file = File.createTempFile("jcabi-ssh", ".key");
            FileUtils.forceDeleteOnExit(file);
            FileUtils.write(
                file,
                this.key.replaceAll("\r", "")
                    .replaceAll("\n\\s+|\n{2,}", "\n")
                    .trim(),
                CharEncoding.UTF_8
            );
            jsch.setHostKeyRepository(new EasyRepo());
            jsch.addIdentity(file.getAbsolutePath());
            Logger.debug(
                this,
                "Opening SSH session to %s@%s:%s (%d bytes in RSA key)...",
                this.login, this.addr, this.port,
                file.length()
            );
            final Session session = jsch.getSession(
                this.login, this.addr, this.port
            );
            session.setServerAliveInterval(
                (int) TimeUnit.SECONDS.toMillis((long) Tv.TEN)
            );
            session.setServerAliveCountMax(Tv.MILLION);
            session.connect();
            FileUtils.deleteQuietly(file);
            return session;
        } catch (final JSchException ex) {
            throw new IOException(ex);
        }
    }

}
