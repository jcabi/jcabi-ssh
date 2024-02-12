/*
 * Copyright (c) 2014-2024, jcabi.com
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
