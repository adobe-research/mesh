/**

	Provides a stopwatch to measure elapsed time.

<P>
<DL>
<DT><B>Example of use:</B></DT>
<DD>
<p>
<pre>
  Q = stopwatch();
<p>
	Q.start();
	//
	// code to be timed here ...
	//
	Q.stop();
	print("elapsed time was: ", Q.read(), " seconds.");
</pre>	

@author Keith McGuigan
@version 1 May 2013
*/

export StopWatch;


StopWatch() {

    members = box(running: false, last_time: 0.0, total: 0.0);

    seconds() -> Double {
        l2f(millitime()) /. 1000.0
    };

    set(running, last, total) { 
        members := (running: running, last_time: last, total: total);
    };

    _reset() { set(false, 0.0, 0.0); };

    _start() {
        guard((*members).running, (), { set(true, seconds(), 0.0); })
    };

    _resume() {
        this = *members;
        guard(this.running, (), { set(true, seconds(), this.total) })
    };

    _stop() {
        this = *members;
        guard(!this.running, (), 
            { set(false, this.last_time, seconds() -. this.last_time) })
    };

    _read() -> Double {
        this = *members;
        guard(!this.running, this.total, {
            t = seconds(); 
            total = t -. this.last_time; 
            set(true, t, total);
            total
        })
    };

    _reset();

    (
        reset: _reset,
        start: _start,
        resume: _resume,
        stop: _stop,
        read: _read
    )
};
