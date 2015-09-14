package com.jcabi.ssh;

import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.jcraft.jsch.Session;
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

@ToString
@EqualsAndHashCode(of = { "addr", "port", "login" })
abstract class SSHCommon implements Shell{

	 /**
     * Default SSH port.
     */
    public static final int PORT = 22;

    /**
     * IP address of the server.
     */
    protected final transient String addr;

    /**
     * Port to use.
     */
    protected final transient int port;

    /**
     * User name.
     */
    protected final transient String login;

    /**
     * Constructor.
     * @param adr IP address
     * @param user Login
     * @throws IOException If fails
     * @since 1.4
     */
    public SSHCommon(final String adr,final int prt, final String user) throws UnknownHostException 	{
        this.addr = InetAddress.getByName(adr).getHostAddress();
        Validate.matchesPattern(
            this.addr,
            "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
            "Invalid IP address of the server `%s`",
            this.addr
        );
        this.login = user;
        Validate.notEmpty(this.login, "user name can't be empty");
        this.port = prt;
    }


   // @checkstyle ParameterNumberCheck (5 lines)
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
    abstract Session session() throws IOException;

}