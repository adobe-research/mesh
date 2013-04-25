
import * from std;

// Port of Go example http://golang.org/doc/codewalk/sharemem/, which uses
// channels and message passing to orchestrate a fixed number of concurrent
// consumer tasks over shared work queue. The code is somewhat heavily shaped
// by the channel topology and message passing logistics. Here we simply share
// state directly.
//
// This version also adds some object-like encapsulation: the pollsvc() function
// returns a record with "methods" for starting and stopping the service, for
// adding and removing URLs from the poll, and for obtaining a snapshot of the
// current status map.
//
// TODO library pollsvc() generalizing this self-consuming p/c shape.
// TODO handle new work item creation, here and/or in lib pollsvc()
//

NUM_POLLERS = 2;                    // number of poller tasks to launch
SEC_MILLIS = 1000;                  // second's worth of millis
POLL_INTERVAL = 10 * SEC_MILLIS;    // how often to poll each URL
STATUS_INTERVAL = 5 * SEC_MILLIS;   // how often to log status to stdout
ERR_TIMEOUT = 5 * SEC_MILLIS;       // back-off timeout on error

// starts URL polling/logging service, returns circuit
// breaker (boxed bool). service shuts down when boxed
// value is set to false.
pollsvc(urls)
{
    // boxed status map
    status = box([:]);

    // work queue of (url, error count)
    urlq = box(urls | { ($0, 0) });

    // circuit breaker
    cb = box(true);

    // polling tasks
    repeat(NUM_POLLERS, {
        spawn {
            consume(cb, urlq, { url, errct =>
                stat = poll(url);
                status <- { mapset($0, url, stat) };
                spawn {
                    newerr = guard(stat == "ok", 0, { errct + 1 });
                    sleep(POLL_INTERVAL + ERR_TIMEOUT * newerr);
                    urlq <- { append($0, (url, newerr)) }
                }
            });
            print(taskid(), "polling task stopping");
        }
    });

    // logging task
    spawn {
        while({*cb}, {
            print("Current status:");
            entries(*status) | print;
            sleep(STATUS_INTERVAL);
        });
        print(taskid(), "logging task stopping");
    };

    print("started");

    // return circuit breaker
    cb
};

// Poll the passed url and return the resulting status.
// TODO faked pending httphead once variants are back
poll(url) {
    guard(httpget(url) != "", "ok", {
        err = "(err)";
        print("Error", url, err);
        err
    })
};

// ---

//
// main. to stop, enter
//
// > cb := false
//
// from the shell.
//

URLS = [
    "http://www.google.com/",
    "http://golang.org/",
    "http://blog.golang.org/"
];

cb = pollsvc(URLS);

stop() { cb := false };

