
// http server logs requests and responds with a dump of path counts

// mutable server state, maps requests to counts
counts = box([:]);

// request handler
handler(req) {
    // get incoming command
    cmd = strsplit(strsplit(req, "\n\n")[0], "\n")[0];

    // increment count for our request in counts map
    counts <- { reqs =>
        cur = mapgetd(reqs, cmd, 0);
        mapset(reqs, cmd, 1 + cur)
    };

    // get a snapshot of the current state
    reqs = get(counts);

    // helper, generates a table row for a (request, count) pair
    tr(r, c) {
        "<tr><td>" + tostr(c) + "</td><td>" + r + "</td></tr>"
    };
    
    // return current state report
    "HTTP/1.1 200 OK\nContent-Type: text/html\n\n" +
        "<html><body><h2>" + tostr(sum(values(reqs))) + " requests:</h2><table>\n" +
        strcat(entries(reqs) | tr) +
        "</table></body></html>"
};

// constants
PORT = 8080;
NTHREADS = 100;

// open a socket
socket = ssocket(PORT);

// spawn some listener tasks
repeat(NTHREADS, {
    spawn {
        while({!closed(socket)}, { accept(socket, handler) })
    }
});

// stop function
stop() { close(socket) };
