/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
 * @since 1.4
 */
final class Execution {
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
    Execution(final String cmd, final InputStream input,
        final OutputStream out, final OutputStream err,
        final Session sess) {
        this.command = cmd;
        this.stdin = input;
        this.stdout = out;
        this.stderr = err;
        this.session = sess;
    }

    /**
     * Executes some command.
     * @return Return code of the command.
     * @throws IOException If fails
     */
    public int exec() throws IOException {
        try {
            final ChannelExec channel = ChannelExec.class.cast(
                this.session.openChannel("exec")
            );
            channel.setErrStream(this.stderr, false);
            channel.setOutputStream(this.stdout, false);
            channel.setInputStream(this.stdin, false);
            channel.setCommand(this.command);
            channel.setPty(false);
            channel.connect((int) TimeUnit.SECONDS.toMillis(10L));
            Logger.info(this, "+ %s", this.command);
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
     * @param channel The channel
     * @return The exit code
     * @throws IOException If some IO problem inside
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private int code(final ChannelExec channel) throws IOException {
        final long start = System.currentTimeMillis();
        while (!channel.isClosed()) {
            try {
                this.session.sendKeepAliveMsg();
                // @checkstyle IllegalCatch (1 line)
            } catch (final Exception ex) {
                throw new IOException(
                    "Failed to sendKeepAliveMsg() to the SSH session",
                    ex
                );
            }
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException(
                    String.format(
                        "Interrupted after %[ms]s of waiting",
                        System.currentTimeMillis() - start
                    ),
                    ex
                );
            }
            if (Logger.isDebugEnabled(this)) {
                try {
                    Logger.debug(
                        this,
                        "Waiting for SSH session to %s:%d to close, already %[ms]s...",
                        channel.getSession().getHost(),
                        channel.getSession().getPort(),
                        System.currentTimeMillis() - start
                    );
                } catch (final JSchException ex) {
                    throw new IOException(ex);
                }
            }
        }
        return channel.getExitStatus();
    }
}
