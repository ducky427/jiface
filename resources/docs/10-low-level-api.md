# jiface User's Guide

## The JInterface Package in Clojang

**NOTICE**: This document is a copy of the JInterface Users Guide,
[a version of which](erlang/jinterface_users_guide.html) is provided in the
published documentation for the Clojang project.

The [Jinterface](erlang/java/com/ericsson/otp/erlang/package-summary.html)
package provides a set of tools for communication with Erlang processes. It
can also be used for communication with other Java processes using the same
package, as well as C processes using the Erl_Interface library.

The set of classes in the package can be divided into two categories: those
that provide the actual communication, and those that provide a Java
representation of the Erlang data types. The latter are all subclasses of
OtpErlangObject, and they are identified by the ``OtpErlang`` prefix.

Since this package provides a mechanism for communicating with Erlang, message
recipients can be Erlang processes or instances of
com.ericsson.otp.erlang.OtpMbox, both of which are identified with pids and
possibly registered names. When pids or mailboxes are mentioned as message
senders or recipients in this section, it should assumed that even Erlang
processes are included, unless specified otherwise. The classes in
[Jinterface](erlang/java/com/ericsson/otp/erlang/package-summary.html) support
the following:

*    manipulation of data represented as Erlang data types
*    conversion of data between Java and Erlang formats
*    encoding and decoding of Erlang data types for transmission or storage
*    communication between Java nodes and Erlang processes

In the following sections, these topics are described:

*    mapping of Erlang types to Java
*    encoding, decoding, and sending Erlang terms
*    connecting to a distributed Erlang node
*    using nodes, mailboxes and EPMD
*    sending and receiving Erlang messages and data
*    remote procedure calls
*    linking to remote processes
*    compiling your code for use with Jinterface
*    tracing message flow


## Mapping of Basic Erlang Types to the JVM

This section describes the mapping of Erlang basic types to JVM types.

| Erlang type         | JVM type
|---------------------|------------------------------------------------------
|atom                 | [atom](clojang/current/clojang.jinterface.erlang.atom.html)
|binary               | [OtpErlangBinary](erlang/java/com/ericsson/otp/erlang/OtpErlangBinary.html)
|floating point types | [OtpErlangFloat](erlang/java/com/ericsson/otp/erlang/OtpErlangFloat.html) or [OtpErlangDouble](erlang/java/com/ericsson/otp/erlang/OtpErlangDouble.html), depending on the floating point value size
|integral types       | One of [OtpErlangByte](erlang/java/com/ericsson/otp/erlang/OtpErlangByte.html), [char](clojang.jinterface.erlang.types.html#var-charhtml),[OtpErlangShort](erlang/java/com/ericsson/otp/erlang/OtpErlangShort.html), [OtpErlangUShort](erlang/java/com/ericsson/otp/erlang/OtpErlangUShort.html),[OtpErlangInt](erlang/java/com/ericsson/otp/erlang/OtpErlangInt.html),[OtpErlangUInt](erlang/java/com/ericsson/otp/erlang/OtpErlangUInt.html) or [OtpErlangLong](erlang/java/com/ericsson/otp/erlang/OtpErlangLong.html), depending on the integral value size and sign
|list                 | [list](clojang/current/clojang.jinterface.erlang.list.html)
|pid                  | [OtpErlangPid](erlang/java/com/ericsson/otp/erlang/OtpErlangPid.html)
|port                 | [OtpErlangPort](erlang/java/com/ericsson/otp/erlang/OtpErlangPort.html)
|ref                  | [OtpErlangRef](erlang/java/com/ericsson/otp/erlang/OtpErlangRef.html)
|tuple                | [tuple](clojang.jinterface.erlang.types.html#var-tuple.html)
|map                  | [OtpErlangMap](erlang/java/com/ericsson/otp/erlang/OtpErlangMap.html)
|term                 | [OtpErlangObject](clojang/current/clojang.jinterface.erlang.object.html)


## Special Mapping Issues

The atoms ``true`` and ``false`` are special atoms, used as boolean values.
The class  [boolean](clojang/current/clojang.jinterface.erlang.boolean.html)
can be used to represent these.

Lists in Erlang are also used to describe sequences of printable characters
(strings). A convenience class
[string](clojang/current/clojang.jinterface.erlang.string.html) is provided to
represent Erlang strings.


## Nodes

A node as defined by Erlang/OTP is an instance of the Erlang Runtime System, a
virtual machine roughly equivalent to a JVM. Each node has a unique name in
the form of an identifier composed partly of the hostname on which the node is
running, e.g ``gurka@sallad.com``. Several such nodes can run on the same host
as long as their names are unique. The class
[node](clojang/current/clojang.jinterface.otp.nodes.html#var-NodeObject)
represents an Erlang node. It is created with a name and optionally a port
number on which it listens for incoming connections. Before creating an
instance of [node](clojang/current/clojang.jinterface.otp.nodes.html#var-
NodeObject), ensure that EPMD is running on the host machine. See the Erlang
documentation for more information about EPMD. In this example, the host name
is appended automatically to the identifier, and the port number is chosen by
the underlying system:

```clojure
=> (require '[jiface.otp.nodes :as nodes])
nil
=> (def gurka (nodes/node "gurka"))
#'user/node
```


## Mailboxes

Erlang processes running on an Erlang node are identified by process
identifiers (pids) and, optionally, by registered names unique within the
node. Each Erlang process has an implicit mailbox that is used to receive
messages; the mailbox is identified with the pid of the process.

JInterface provides a similar mechanism with the class
[OtpMbox](erlang/java/com/ericsson/otp/erlang/OtpMbox.html), a mailbox that
can be used to send and receive messages asynchronously. Each OtpMbox is
identified with a unique pid and , optionally, a registered name unique within
the [OtpMbox](erlang/java/com/ericsson/otp/erlang/OtpMbox.html).

Applications are free to create mailboxes as necessary. This is done as
follows:

```clojure
user=> (def mbox (nodes/create-mbox gurka))
#'user/mbox
```

or like this:

```clojure
=> (require '[jiface.otp.messaging :as messaging])
nill
user=> (def mbox (messaging/mbox gurka))
#'user/mbox
```

The mailbox created in the above example has no registered name, although it
does have a pid. The pid can be obtained from the mailbox and included in
messages sent from the mailbox, so that remote processes are able to respond.

An application can register a name for a mailbox, either when the mailbox is
initially created:

```clojure
user=> (def mbox (nodes/create-mbox gurka "server"))
#'user/mbox
```

or later on, if need be. You may either use the ``register-mbox`` function for
the Node):

```clojure
=> (nodes/register-mbox gurka "server2" mbox)
true
```

or the ``register-name`` function for the Mbox:

```clojure
=> (messaging/register-name mbox "server3")
true
```

Registered names are usually necessary in order to start communication, since
it is impossible to know in advance the pid of a remote process. If a well-
known name for one of the processes is chosen in advance and known by all
communicating parties within an application, each mailbox can send an initial
message to the named mailbox, which then can identify the sender pid.


##  Connections

It is not necessary to explicitly set up communication with a remote node.
Simply sending a message to a mailbox on that node will cause the OtpNode to
create a connection if one does not already exist. Once the connection is
established, subsequent messages to the same node will reuse the same
connection.

It is possible to check for the existence of a remote node before attempting
to communicate with it. Here we send a ping message to the remote node to see
if it is alive and accepting connections. Paste the following function in your
REPL:

```clojure
(defn print-liveliness [node other]
  (if (nodes/ping node other 1000)
    (println "It's aliiiive!")
    (println "Mate, this node wouldn't go 'voom' if ...")))
```

Now let's use it:

```clojure
user=> (print-liveliness gurka "gurka")
It's aliiiive!
nil
user=> (print-liveliness gurka "nohost")
Mate, this node wouldn't go 'voom' if ...
nil
```

If the call to ``(nodes/ping ...)`` succeeds, a connection to the remote node
has been established. Note that it is not necessary to ping remote nodes
before communicating with them, but by using ping you can determine if the
remote exists before attempting to communicate with it.

Connections are only permitted by nodes using the same security cookie. The
cookie is a short string provided either as an argument when creating
[node](clojang/current/clojang.jinterface.otp.nodes.html#var-NodeObject)
objects, or found in the user's home directory in the file ``.erlang.cookie``.
When a connection attempt is made, the string is used as part of the
authentication process. If you are having trouble getting communication to
work, use the trace facility (described later in this document) to show the
connection establishment. A likely problem is that the cookies are different.

Connections are never broken explicitly. If a node fails or is closed, a
connection may be broken however.


##  Transport Factory

All necessary connections are made using methods of
[OtpTransportFactory](erlang/java/com/ericsson/otp/erlang/OtpTransportFactory.html)
interface. Default OtpTransportFactory implementation is based on standard
Socket class. User may provide custom transport factory as needed. See java
doc for details.


## Sending and Receiving Messages

Messages sent with this package must be instances of
[object](clojang/current/clojang.jinterface.erlang.object.html) or one of its
subclasses. Message can be sent to processes or pids, either by specifying the
pid of the remote, or its registered name and node.

In this example, we create a message containing our own pid so the echo
process can reply:

```clojure
=> (require '[jiface.erlang.types :as types])
nil
=> (def msg (into-array (types/object) [(messaging/self mbox)
                                        (types/atom "hello, world")]))
#'user/msg
=> (messaging/register-name mbox "echo")
true
=> (messaging/send mbox "echo" "gurka" (types/tuple msg))
nil
=> (messaging/receive mbox)
#object[com.ericsson.otp.erlang.OtpErlangTuple
        0xeed771a
        "{#Pid<gurka@mndltl01.1.0>,'hello, world'}"]
```

You can also send messages from Erlang VMs to your ``node``'s mailbox named
``"echo"``. Before you do that, though, start listening in your Clojure REPL:

```clojure
=> (messaging/receive mbox)
```

Next, start up LFE (Lisp Flavoured Erlang) on the same machine with a short
name:

```bash
$ /path/to/bin/lfe -sname lfe
LFE Shell V7.2 (abort with ^G)
(clojang-lfe@mndltl01)>
```

Once you're in the REPL, you're ready to send a message:

```cl
(clojang-lfe@mndltl01)> (! #(echo gurka@mndltl01) #(hej!))
#(hej!)
```

Looking at the Clojure REPL, you'll see that your ``receive `` call has
finished and you now have some data:

```clojure
#object[com.ericsson.otp.erlang.OtpErlangTuple 0x4a377f4e "{'hej!'}"]
```


##  Sending Arbitrary Data

TBD


## Linking to Remote Processes

Erlang defines a concept known as linked processes. A link is an implicit
connection between two processes that causes an exception to be raised in one
of the processes if the other process terminates for any reason. Links are
bidirectional: it does not matter which of the two processes created the link
or which of the linked processes eventually terminates; an exception will be
raised in the remaining process. Links are also idempotent: at most one link
can exist between two given processes, only one operation is necessary to
remove the link.

`jiface` provides a similar mechanism. Also here, no distinction is made
between mailboxes and Erlang processes. A link can be created to a remote
mailbox or process when its pid is known:

```clj
(messaging/link (messaging/get-pid inbox))
```

The link can be removed by either of the processes in a similar manner:

```clj
(messaging/unlink (messaging/get-pid inbox))
```

In the cases when only a "remote" message box is provided (as above), the
local node in the `link` and `unlink` function calls is assumed to be the
default node.

If the remote process terminates while the link is still in place, an
exception will be raised on a subsequent call to `receive`. For example, in
this case, the "remote" node's inbox pid to which we have linked is the
`OtpMbox` instance stored in the `inbox` variable. The local node is our
default node. Before unlinking `inbox`, if we instead call
`(messaging/close inbox)` and then try to receive on the local node's default
message box, we'll get an exception. Here's how to catch that exception:

```clj
(try
  (messaging/receive
    (messaging/default-mbox
      (nodes/default-node "clojang@host")
      "default"))
  (catch OtpErlangExit ex
    (println (format "Remote pid %s has terminated"
                     (exceptions/get-pid ex)))))
```


##  Using EPMD

`epmd` is the Erlang Port Mapper Daemon. Distributed Erlang nodes register with
`epmd` on the localhost to indicate to other nodes that they exist and can
accept connections. `epmd` maintains a register of node and port number
information, and when a node wishes to connect to another node, it first
contacts epmd in order to find out the correct port number to connect to.

The basic interaction with EPMD is done through the functions in the
`jiface.epmd` namespace.  Under the hood (at the JInterface level), nodes
wishing to contact other nodes  first request information from `epmd` before a
connection can be set up.

When manually creating connections to Erlang nodes with operations such as
`(nodes/connect ...)`, a connection is
first made to `epmd` and, if the node is known, a connection is then made to
the Erlang node.

Clojang nodes can also register themselves with `epmd` if they want other
nodes in the system to be able to find and connect to them. This is done by
call  to `(epmd/publish-port ...)`.

Be aware that on some systems (such as VxWorks), a failed node will not be
detected by this mechanism since the operating system does not automatically
close descriptors that were left open when the node failed. If a node has
failed in this way, `epmd` will prevent you from registering a new node with
the old name, since it thinks that the old name is still in use. In this case,
you must unregister the name explicitly, by using `(epmd/unpublish-port ...)`.
This will cause `epmd` to close the connection from the far end. Note that if
the name was in fact still in use by a node, the results of this operation are
unpredictable. Also, doing this does not cause the local end of the connection
to close, so resources may be consumed.


## Remote Procedure Calls

An Erlang node acting as a client to another Erlang node typically sends a
request and waits for a reply. Such a request is included in a function call
at a remote node and is called a remote procedure call. Remote procedure calls
are supported through the
[connection](http://oubiwann.github.io/clojang/current/clojang.jinterface.otp.connection.html#var-ConnectionObject)
Clojure protocol. The following example shows how the ``connection`` protocol
is used for remote procedure calls:

```clojure
(require '[jiface.otp.connection :as connection])
(def self (nodes/self "client"))
(def other (nodes/peer "clojang-lfe@mndltl01"))
(def connx (nodes/connect self other))

(connection/!rpc connx "erlang" "date" (types/list))
(connection/receive-rpc connx)
#object[com.ericsson.otp.erlang.OtpErlangTuple 0x385465c1 "{2016,1,30}"]
```
