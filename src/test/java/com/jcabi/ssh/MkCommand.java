/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.ssh;

import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.cactoos.io.OutputTo;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.LengthOf;
import org.cactoos.scalar.Unchecked;

/**
 * Mock of a command that displays its name.
 * @since 1.6
 */
final class MkCommand implements Command {
    /**
     * Command being executed.
     */
    private final transient String command;

    /**
     * Exit callback.
     */
    private transient ExitCallback callback;

    /**
     * Output stream for use by command.
     */
    private transient OutputStream output;

    /**
     * Constructor.
     * @param cmd Command to echo.
     */
    MkCommand(final String cmd) {
        this.command = cmd;
    }

    @Override
    public void setInputStream(final InputStream input) {
        Logger.debug(this, "#setInputStream(): for '%s'", this.command);
    }

    @Override
    public void setOutputStream(final OutputStream stream) {
        Logger.debug(this, "#setOutputStream(): for '%s'", this.command);
        this.output = stream;
    }

    @Override
    public void setErrorStream(final OutputStream err) {
        Logger.debug(this, "#setErrorStream(): for '%s'", this.command);
    }

    @Override
    public void setExitCallback(final ExitCallback cllbck) {
        Logger.debug(this, "#setExitCallback(): for '%s'", this.command);
        this.callback = cllbck;
    }

    @Override
    public void start(final ChannelSession session, final Environment env) throws IOException {
        Logger.debug(this, "#start(): starting '%s'", this.command);
        new Unchecked<>(
            new LengthOf(
                new TeeInput(this.command, new OutputTo(this.output))
            )
        ).value();
        this.callback.onExit(0);
    }

    @Override
    public void destroy(final ChannelSession session) throws IOException {
        Logger.debug(this, "#destroy(): finishing '%s'", this.command);
        session.close();
    }

}
