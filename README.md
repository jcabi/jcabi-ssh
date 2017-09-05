<img src="http://img.jcabi.com/logo-square.svg" width="64px" height="64px" />

[![Managed by Zerocracy](http://www.zerocracy.com/badge.svg)](http://www.zerocracy.com)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-ssh)](http://www.rultor.com/p/jcabi/jcabi-ssh)

[![Build Status](https://travis-ci.org/jcabi/jcabi-ssh.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-ssh)
[![PDD status](http://www.0pdd.com/svg?name=jcabi/jcabi-ssh)](http://www.0pdd.com/p?name=jcabi/jcabi-ssh)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-ssh/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-ssh)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/com.jcabi/jcabi-ssh/badge.svg)](http://www.javadoc.io/doc/com.jcabi/jcabi-ssh)
[![Dependencies](https://www.versioneye.com/user/projects/561aa32ba193340f2f00118b/badge.svg?style=flat)](https://www.versioneye.com/user/projects/561aa32ba193340f2f00118b)

More details are here: [ssh.jcabi.com](http://ssh.jcabi.com/).
Also, read this blog post: [Simple Java SSH Client](http://www.yegor256.com/2014/09/02/java-ssh-client.html).

It is a convenient SSH client for Java:

```java
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.SSH;
Shell shell = new SSH("example.com", 22, "username", "key...");
String stdout = new Shell.Plain(shell).exec("echo 'Hello, world!'");
```

The `key` here is your private SSH key,
usually the one you have in `~/.ssh/id_rsa`.

There is also a convenient `SSHD` class, a runner of ssh daemon,
for unit testing:

```java
try (SSHD sshd = new SSHD()) {
  String uptime = new Shell.Plain(
    SSH(sshd.host(), sshd.login(), sshd.port(), sshd.key())
  ).exec("uptime");
}
```

## Questions?

If you have any questions about the framework, or something doesn't work as expected,
please [submit an issue here](https://github.com/jcabi/jcabi-ssh/issues/new).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven (3.2+) build before submitting a pull request:

```
$ mvn clean install -Pqulice
```

Make sure you have Java version 7 or higher.
