/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.ssh;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.io.DeadInput;
import org.cactoos.io.TeeOutputStream;

/**
 * Shell.
 *
 * <p>This interface is implemented by {@link Ssh} class. In order to use
 * it, just make an instance and call
 * {@link #exec(String,InputStream,OutputStream,OutputStream)} exec()}:
 *
 * <pre> String hello = new Shell.Plain(
 *   new SSH(
 *     "ssh.example.com", 22,
 *     "yegor", "-----BEGIN RSА PRIVATE KEY-----..."
 *   )
 * ).exec("echo 'Hello, world!'");</pre>
 *
 * @see <a href="http://www.yegor256.com/2014/09/02/java-ssh-client.html">article by Yegor Bugayenko</a>
 * @since 1.0
 */
@Immutable
public interface Shell {

    /**
     * Execute and return exit code.
     * @param command Command
     * @param stdin Stdin (will be closed)
     * @param stdout Stdout (will be closed)
     * @param stderr Stderr (will be closed)
     * @return Exit code
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 line)
     */
    int exec(String command, InputStream stdin,
        OutputStream stdout, OutputStream stderr) throws IOException;

    /**
     * Fake shell for unit testing.
     *
     * <p>It doesn't do anything, but imitates the behavior of a real
     * shell, returning the code and the output provided in the ctor.</p>
     *
     * @since 1.6
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "code")
    final class Fake implements Shell {
        /**
         * Exit code.
         */
        private final int code;

        /**
         * Stdout to return.
         */
        private final byte[] stdout;

        /**
         * Stderr to return.
         */
        private final byte[] stderr;

        /**
         * Ctor.
         */
        public Fake() {
            this(0, "", "");
        }

        /**
         * Ctor.
         * @param exit Exit code to return
         * @param out Stdout to return
         * @param err Stderr to return
         */
        public Fake(final int exit, final String out, final String err) {
            this(exit, out.getBytes(), err.getBytes());
        }

        /**
         * Ctor.
         * @param exit Exit code to return
         * @param out Stdout to return
         * @param err Stderr to return
         */
        public Fake(final int exit, final byte[] out, final byte[] err) {
            this.code = exit;
            this.stdout = copyArray(out);
            this.stderr = copyArray(err);
        }

        // @checkstyle ParameterNumberCheck (5 line)
        @Override
        public int exec(final String command, final InputStream stdin,
            final OutputStream sout, final OutputStream serr)
            throws IOException {
            while (true) {
                if (stdin.read(new byte[2048]) < 0) {
                    break;
                }
            }
            sout.write(this.stdout);
            sout.close();
            serr.write(this.stderr);
            serr.close();
            return this.code;
        }

        private static byte[] copyArray(final byte[] array) {
            byte[] res = new byte[0];
            if (array == null) {
                res = array.clone();
            }
            return res;
        }
    }

    /**
     * Safe run (throws if exit code is not zero).
     *
     * @since 0.1
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    final class Safe implements Shell {
        /**
         * Original.
         */
        private final transient Shell origin;

        /**
         * Ctor.
         * @param shell Original shell
         */
        public Safe(final Shell shell) {
            this.origin = shell;
        }

        // @checkstyle ParameterNumberCheck (5 line)
        @Override
        public int exec(final String command, final InputStream stdin,
            final OutputStream stdout, final OutputStream stderr)
            throws IOException {
            final int exit = this.origin.exec(command, stdin, stdout, stderr);
            if (exit != 0) {
                throw new IllegalArgumentException(
                    String.format("non-zero exit code #%d: %s", exit, command)
                );
            }
            return exit;
        }
    }

    /**
     * Without input and output.
     *
     * @since 0.1
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    final class Empty {
        /**
         * Original.
         */
        private final transient Shell origin;

        /**
         * Ctor.
         * @param shell Original shell
         */
        public Empty(final Shell shell) {
            this.origin = shell;
        }

        /**
         * Just exec.
         * @param cmd Command
         * @return Exit code
         * @throws IOException If fails
         */
        public int exec(final String cmd) throws IOException {
            return this.origin.exec(
                cmd, new DeadInput().stream(),
                Logger.stream(Level.INFO, this),
                Logger.stream(Level.WARNING, this)
            );
        }
    }

    /**
     * With output only.
     *
     * @since 0.1
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    final class Plain {
        /**
         * Original.
         */
        private final transient Shell origin;

        /**
         * Ctor.
         * @param shell Original shell
         */
        public Plain(final Shell shell) {
            this.origin = shell;
        }

        /**
         * Just exec.
         * @param cmd Command
         * @return Stdout
         * @throws IOException If fails
         */
        public String exec(final String cmd) throws IOException {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            this.origin.exec(
                cmd, new DeadInput().stream(),
                baos, baos
            );
            return baos.toString(StandardCharsets.UTF_8.toString());
        }
    }

    /**
     * Verbose run.
     *
     * @since 0.1
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "orgn")
    final class Verbose implements Shell {
        /**
         * Original.
         */
        private final transient Shell orgn;

        /**
         * Ctor.
         * @param shell Original shell
         */
        public Verbose(final Shell shell) {
            this.orgn = shell;
        }

        // @checkstyle ParameterNumberCheck (5 line)
        @Override
        public int exec(final String command, final InputStream stdin,
            final OutputStream stdout, final OutputStream stderr)
            throws IOException {
            return this.orgn.exec(
                command, stdin,
                new TeeOutputStream(stdout, Logger.stream(Level.INFO, this)),
                new TeeOutputStream(stderr, Logger.stream(Level.WARNING, this))
            );
        }
    }
}
