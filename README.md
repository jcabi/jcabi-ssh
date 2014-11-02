<img src="http://img.jcabi.com/logo-square.svg" width="64px" height="64px" />

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-ssh)](http://www.rultor.com/p/jcabi/jcabi-ssh)

[![Build Status](https://travis-ci.org/jcabi/jcabi-ssh.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-ssh)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-ssh/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-ssh)

More details are here: [ssh.jcabi.com](http://ssh.jcabi.com/).
Also, read this blog post: [Simple Java SSH Client](http://www.yegor256.com/2014/09/02/java-ssh-client.html).

It is a convenient SSH client for Java:

```java
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.SSH;
Shell shell = new SSH("example.com", 22, "username", "key...");
String stdout = new Shell.Plain(shell).exec("echo 'Hello, world!'");
```

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

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```
