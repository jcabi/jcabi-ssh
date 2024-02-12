<img alt="logo" src="http://img.jcabi.com/logo-square.svg" width="64px" height="64px" />

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-ssh)](http://www.rultor.com/p/jcabi/jcabi-ssh)

[![mvn](https://github.com/jcabi/jcabi-ssh/actions/workflows/mvn.yml/badge.svg)](https://github.com/jcabi/jcabi-ssh/actions/workflows/mvn.yml)
[![PDD status](http://www.0pdd.com/svg?name=jcabi/jcabi-ssh)](http://www.0pdd.com/p?name=jcabi/jcabi-ssh)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-ssh/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-ssh)
[![Javadoc](https://javadoc.io/badge/com.jcabi/jcabi-ssh.svg)](http://www.javadoc.io/doc/com.jcabi/jcabi-ssh)
[![codecov](https://codecov.io/gh/jcabi/jcabi-ssh/branch/master/graph/badge.svg)](https://codecov.io/gh/jcabi/jcabi-ssh)
[![Hits-of-Code](https://hitsofcode.com/github/jcabi/jcabi-ssh)](https://hitsofcode.com/view/github/jcabi/jcabi-ssh)
[![jpeek report](https://i.jpeek.org/com.jcabi/jcabi-ssh/badge.svg)](https://i.jpeek.org/com.jcabi/jcabi-ssh/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/jcabi/jcabi-ssh/blob/master/LICENSE.txt)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/2f21909785c54690a355b9c6568795a9)](https://www.codacy.com/gh/jcabi/jcabi-ssh/dashboard)

More details are here: [ssh.jcabi.com](http://ssh.jcabi.com/).

Also, read this blog post: [Simple Java SSH Client](http://www.yegor256.com/2014/09/02/java-ssh-client.html).

It is a convenient SSH client for Java:

```java
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
Shell shell = new Ssh("example.com", 22, "username", "key...");
String stdout = new Shell.Plain(shell).exec("echo 'Hello, world!'");
```

The `key` here is your private SSH key,
usually the one you have in `~/.ssh/id_rsa`.

There is also a convenient `SSHD` class, a runner of ssh daemon,
for unit testing:

```java
try (Sshd sshd = new Sshd()) {
  String uptime = new Shell.Plain(
    Ssh(sshd.host(), sshd.login(), sshd.port(), sshd.key())
  ).exec("uptime");
}
```

Version 1.6 works under Java 1.8+. If your Java version is ealier, use
version 1.5.2.

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven (3.2+) build before submitting a pull request:

```
$ mvn clean install -Pqulice
```

Make sure you have Maven 3.3+ and Java 8+.
