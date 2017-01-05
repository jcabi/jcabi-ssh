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
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Execution of a single command.
 * @author Georgy Vlasov (wlasowegor@gmail.com)
 * @version $Id$
 * @since 1.4
 */
interface Execution {
    /**
     * Executes some command.
     * @return Return code of the command.
     * @throws IOException If fails
     */
    int exec() throws IOException;

    /**
     * Execution of a command in an SSH session.
     * @author Georgy Vlasov (wlasowegor@gmail.com)
     * @version $Id$
     * @since 1.4
     */
    final class Default implements Execution {
        /**
         * Command.
         */
        private final transient String command;

        /**
         * Stdin.
         */
        private final transient InputStream stdin;

        /**
         * Stdout.
         */
        private final transient OutputStream stdout;

        /**
         * Stderr.
         */
        private final transient OutputStream stderr;

        /**
         * Session.
         */
        private final transient Session session;

        /**
         * Uses an SSH session to execute a single command and disconnect
         * immediately.
         * @param cmd Command
         * @param input Stdin (will be closed)
         * @param out Stdout (will be closed)
         * @param err Stderr (will be closed)
         * @param sess SSH session (will be disconnected)
         * @checkstyle ParameterNumberCheck (6 lines)
         */
        Default(final String cmd, final InputStream input,
            final OutputStream out, final OutputStream err,
            final Session sess) {
            this.command = cmd;
            this.stdin = input;
            this.stdout = out;
            this.stderr = err;
            this.session = sess;
        }

        /**
         * Execute {@link #command} in {@link #session}.
         * @return Exit code
         * @throws IOException If fails
         */
        @Override
        public int exec() throws IOException {
            try {
                final ChannelExec channel = ChannelExec.class.cast(
                    this.session.openChannel("exec")
                );
                channel.setErrStream(this.stderr, false);
                channel.setOutputStream(this.stdout, false);
                channel.setInputStream(this.stdin, false);
                channel.setCommand(this.command);
                channel.connect();
                Logger.info(this, "$ %s", this.command);
                return this.exec(channel);
            } catch (final JSchException ex) {
                throw new IOException(ex);
            } finally {
                this.session.disconnect();
            }
        }

        /**
         * Exec this channel and return its exit code.
         * @param channel The channel to exec
         * @return Exit code (zero in case of success)
         * @throws IOException If fails
         */
        private int exec(final ChannelExec channel) throws IOException {
            try {
                return this.code(channel);
            } finally {
                channel.disconnect();
            }
        }

        /**
         * Wait until it's done and return its code.
         * @param exec The channel
         * @return The exit code
         * @throws IOException If some IO problem inside
         */
        @SuppressWarnings("PMD.AvoidCatchingGenericException")
        private int code(final ChannelExec exec) throws IOException {
            while (!exec.isClosed()) {
                try {
                    this.session.sendKeepAliveMsg();
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
    }
}
