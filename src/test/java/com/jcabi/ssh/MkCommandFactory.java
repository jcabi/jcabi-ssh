/*
 * SPDX-FileCopyrightText: Copyright (c) 2014-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.ssh;

import com.jcabi.log.Logger;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;

/**
 * Factory for command.
 *
 * @since 1.6
 */
final class MkCommandFactory implements CommandFactory {
    @Override
    public Command createCommand(
        final ChannelSession session, final String cmd) {
        Logger.debug(this, "#createCommand(): command '%s' created", cmd);
        return new MkCommand(cmd);
    }
}
